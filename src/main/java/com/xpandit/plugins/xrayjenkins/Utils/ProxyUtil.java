package com.xpandit.plugins.xrayjenkins.Utils;

import com.xpandit.xray.service.impl.delegates.HttpRequestProvider;
import hudson.ProxyConfiguration;
import jenkins.model.Jenkins;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;

import javax.annotation.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ProxyUtil {
    private ProxyUtil() {}

    /**
     * Gets the Proxy Bean based on the jenkins configuration.
     *
     * @return If there is an proxy configured, it will return the bean with this information, otherwise, it will return null.
     */
    @Nullable
    public static HttpRequestProvider.ProxyBean createProxyBean() {
        ProxyConfiguration proxyConfiguration = Optional.ofNullable(Jenkins.getInstanceOrNull())
                .map(jenkins -> jenkins.proxy)
                .orElse(null);

        if (proxyConfiguration != null) {
            final HttpHost proxy = getProxy(proxyConfiguration);
            final CredentialsProvider credentialsProvider = getCredentialsProvider(proxyConfiguration);
            final List<Pattern> noProxyUrlPatterns = getNoProxyUrlPatterns(proxyConfiguration.noProxyHost);

            return new HttpRequestProvider.ProxyBean(proxy, credentialsProvider, noProxyUrlPatterns);
        }

        return null;
    }

    // Similar to hudson.ProxyConfiguration.getNoProxyHostPatterns(String), but returns URL patterns, instead of domain patterns.
    private static List<Pattern> getNoProxyUrlPatterns(String noProxyHost) {
        if (StringUtils.isBlank(noProxyHost)) {
            return Collections.emptyList();
        }
        return Stream.of(noProxyHost.split("[ \t\n,|]+"))
            .filter(StringUtils::isNotEmpty)
            .map(ProxyUtil::makeUrlPattern)
            .collect(Collectors.toList());
    }

    private static Pattern makeUrlPattern(String noProxyHostElement) {
        final String regexp;
        if (noProxyHostElement.contains("://")) {
            // looks like an URL pattern; it's not supported in Jenkins in general, but it was in previous versions
            // of this plugin (2.3.0, 2.3.1), so let's keep that unchanged in case someone ever used it successfuly...
            regexp = noProxyHostElement.replace(".", "\\.").replace("*", ".*");
        } else {
            // looks like a domain pattern; *.blah.com becomes https?://[^/:]*\.blah\.com(:[0-9]+)?(/.*)?
            regexp = "https?://" + noProxyHostElement.replace(".", "\\.").replace("*", "[^/:]*") + "(:[0-9]+)?(/.*)?";
        }
        return Pattern.compile(regexp);
    }

    private static HttpHost getProxy(ProxyConfiguration proxyConfiguration) {
        return new HttpHost(proxyConfiguration.name, proxyConfiguration.port);
    }

    private static CredentialsProvider getCredentialsProvider(ProxyConfiguration proxyConfiguration) {
        if (StringUtils.isBlank(proxyConfiguration.getUserName())) {
            return null;
        }

        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        final AuthScope authScope = new AuthScope(proxyConfiguration.name, proxyConfiguration.port);
        final Credentials credentials = new UsernamePasswordCredentials(proxyConfiguration.getUserName(), proxyConfiguration.getPassword());

        credentialsProvider.setCredentials(authScope, credentials);
        return credentialsProvider;
    }
}
