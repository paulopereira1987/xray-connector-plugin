package com.xpandit.plugins.xrayjenkins.factory;

import com.cloudbees.plugins.credentials.common.StandardCredentials;
import com.cloudbees.plugins.credentials.common.UsernamePasswordCredentials;
import com.xpandit.xray.service.XrayCloudCredentials;
import com.xpandit.xray.service.XrayExporter;
import com.xpandit.xray.service.XrayImporter;
import com.xpandit.xray.service.XrayServerCredentials;
import com.xpandit.xray.service.XrayTestImporter;
import com.xpandit.xray.service.impl.XrayClientImpl;
import com.xpandit.xray.service.impl.XrayCloudClientImpl;
import com.xpandit.xray.service.impl.XrayExporterCloudImpl;
import com.xpandit.xray.service.impl.XrayExporterImpl;
import com.xpandit.xray.service.impl.XrayImporterCloudImpl;
import com.xpandit.xray.service.impl.XrayImporterImpl;
import com.xpandit.xray.service.impl.XrayTestImporterCloudImpl;
import com.xpandit.xray.service.impl.XrayTestImporterImpl;
import com.xpandit.xray.service.impl.delegates.HttpRequestProvider;
import com.xpandit.xray.service.impl.delegates.authentication.BearerTokenAuthentication;
import com.xpandit.xray.service.impl.delegates.authentication.UsernamePasswordAuthentication;
import java.util.Optional;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientFactory {

    private static final Logger logger = LoggerFactory.getLogger(ClientFactory.class);

    private ClientFactory() {}

    public static Optional<XrayCloudCredentials> getCloudClient(StandardCredentials credentials,
                                                                HttpRequestProvider.ProxyBean proxyBean) {
        if (credentials instanceof UsernamePasswordCredentials) {
            UsernamePasswordCredentials usernamePasswordCredentials = (UsernamePasswordCredentials) credentials;
            String username = usernamePasswordCredentials.getUsername();
            String password = usernamePasswordCredentials.getPassword().getPlainText();
            return Optional.of(new XrayCloudClientImpl(username, password, proxyBean));
        }

        logger.error("Unable to create Xray Cloud client! (Credential of type: {})", credentials.getClass().getName());
        return Optional.empty();
    }

    public static Optional<XrayServerCredentials> getServerClient(String jiraURL,
                                                                  StandardCredentials credentials,
                                                                  HttpRequestProvider.ProxyBean proxyBean) {
        if (credentials instanceof UsernamePasswordCredentials) {
            UsernamePasswordAuthentication usernamePasswordAuthentication = getUsernamePasswordAuthentication((UsernamePasswordCredentials) credentials);
            return Optional.of(new XrayClientImpl(jiraURL, usernamePasswordAuthentication, proxyBean));
        } else if (credentials instanceof StringCredentials) {
            BearerTokenAuthentication bearerTokenAuthentication = getBearerTokenAuthentication((StringCredentials) credentials);
            return Optional.of(new XrayClientImpl(jiraURL, bearerTokenAuthentication, proxyBean));
        }

        logger.error("Unable to create Xray Server/DC client! (Credential of type: {})", credentials.getClass().getName());
        return Optional.empty();
    }

    public static Optional<XrayServerCredentials> getServerClientWithoutCredentials(String jiraURL,
                                                                                    HttpRequestProvider.ProxyBean proxyBean) {
        return Optional.of(new XrayClientImpl(jiraURL, null, proxyBean));
    }

    public static Optional<XrayTestImporter> getCloudFeatureImportClient(StandardCredentials credentials,
                                                                         HttpRequestProvider.ProxyBean proxyBean) {
        if (credentials instanceof UsernamePasswordCredentials) {
            UsernamePasswordCredentials usernamePasswordCredentials = (UsernamePasswordCredentials) credentials;
            String username = usernamePasswordCredentials.getUsername();
            String password = usernamePasswordCredentials.getPassword().getPlainText();
            return Optional.of(new XrayTestImporterCloudImpl(username, password, proxyBean));
        }

        logger.error("Unable to create Xray Cloud feature import client! (Credential of type: {})", credentials.getClass().getName());
        return Optional.empty();
    }

    public static Optional<XrayTestImporter> getServerFeatureImportClient(String jiraURL,
                                                                          StandardCredentials credentials,
                                                                          HttpRequestProvider.ProxyBean proxyBean) {
        if (credentials instanceof UsernamePasswordCredentials) {
            UsernamePasswordAuthentication usernamePasswordAuthentication = getUsernamePasswordAuthentication((UsernamePasswordCredentials) credentials);
            return Optional.of(new XrayTestImporterImpl(jiraURL, usernamePasswordAuthentication, proxyBean));
        } else if (credentials instanceof StringCredentials) {
            BearerTokenAuthentication bearerTokenAuthentication = getBearerTokenAuthentication((StringCredentials) credentials);
            return Optional.of(new XrayTestImporterImpl(jiraURL, bearerTokenAuthentication, proxyBean));
        }

        logger.error("Unable to create Xray Server/DC feature import client! (Credential of type: {})", credentials.getClass().getName());
        return Optional.empty();
    }

    public static Optional<XrayExporter> getCloudFeatureExportClient(StandardCredentials credentials,
                                                                     HttpRequestProvider.ProxyBean proxyBean) {
        if (credentials instanceof UsernamePasswordCredentials) {
            UsernamePasswordCredentials usernamePasswordCredentials = (UsernamePasswordCredentials) credentials;
            String username = usernamePasswordCredentials.getUsername();
            String password = usernamePasswordCredentials.getPassword().getPlainText();
            return Optional.of(new XrayExporterCloudImpl(username, password, proxyBean));
        }

        logger.error("Unable to create Xray Cloud feature export client! (Credential of type: {})", credentials.getClass().getName());
        return Optional.empty();
    }

    public static Optional<XrayExporter> getServerFeatureExportClient(String jiraURL,
                                                                          StandardCredentials credentials,
                                                                          HttpRequestProvider.ProxyBean proxyBean) {
        if (credentials instanceof UsernamePasswordCredentials) {
            UsernamePasswordAuthentication usernamePasswordAuthentication = getUsernamePasswordAuthentication((UsernamePasswordCredentials) credentials);
            return Optional.of(new XrayExporterImpl(jiraURL, usernamePasswordAuthentication, proxyBean));
        } else if (credentials instanceof StringCredentials) {
            BearerTokenAuthentication bearerTokenAuthentication = getBearerTokenAuthentication((StringCredentials) credentials);
            return Optional.of(new XrayExporterImpl(jiraURL, bearerTokenAuthentication, proxyBean));
        }

        logger.error("Unable to create Xray Server/DC feature export client! (Credential of type: {})", credentials.getClass().getName());
        return Optional.empty();
    }

    public static Optional<XrayImporter> getCloudResultsImportClient(StandardCredentials credentials,
                                                                     HttpRequestProvider.ProxyBean proxyBean) {
        if (credentials instanceof UsernamePasswordCredentials) {
            UsernamePasswordCredentials usernamePasswordCredentials = (UsernamePasswordCredentials) credentials;
            String username = usernamePasswordCredentials.getUsername();
            String password = usernamePasswordCredentials.getPassword().getPlainText();
            return Optional.of(new XrayImporterCloudImpl(username, password, proxyBean));
        }

        logger.error("Unable to create Xray Cloud test results import client! (Credential of type: {})", credentials.getClass().getName());
        return Optional.empty();
    }

    public static Optional<XrayImporter> getServerResultsImportClient(String jiraURL,
                                                                      StandardCredentials credentials,
                                                                      HttpRequestProvider.ProxyBean proxyBean) {
        if (credentials instanceof UsernamePasswordCredentials) {
            UsernamePasswordAuthentication usernamePasswordAuthentication = getUsernamePasswordAuthentication((UsernamePasswordCredentials) credentials);
            return Optional.of(new XrayImporterImpl(jiraURL, usernamePasswordAuthentication, proxyBean));
        } else if (credentials instanceof StringCredentials) {
            BearerTokenAuthentication bearerTokenAuthentication = getBearerTokenAuthentication((StringCredentials) credentials);
            return Optional.of(new XrayImporterImpl(jiraURL, bearerTokenAuthentication, proxyBean));
        }

        logger.error("Unable to create Xray Server/DC test results import client! (Credential of type: {})", credentials.getClass().getName());
        return Optional.empty();
    }

    private static BearerTokenAuthentication getBearerTokenAuthentication(StringCredentials credentials) {
        String bearerToken = credentials.getSecret().getPlainText();

        return new BearerTokenAuthentication(bearerToken);
    }

    private static UsernamePasswordAuthentication getUsernamePasswordAuthentication(UsernamePasswordCredentials credentials) {
        String username = credentials.getUsername();
        String password = credentials.getPassword().getPlainText();

        return new UsernamePasswordAuthentication(username, password);
    }
}
