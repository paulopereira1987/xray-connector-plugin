package com.xpandit.plugins.xrayjenkins.Utils;

import com.xpandit.xray.service.impl.delegates.HttpRequestProvider;
import hudson.ProxyConfiguration;
import jenkins.model.Jenkins;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.io.IOException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Jenkins.class, ProxyConfiguration.class})
@PowerMockIgnore({"javax.net.ssl.*", "javax.management.*"})
public class ProxyUtilTest {

    private static final String PROXY_DOMAIN = "proxydomain.com";
    private static final int PROXY_PORT = 8080;
    private static final String JIRA_DOMAIN = "jiradomain.com";
    private static final String JIRA_ALTERNATIVE_DOMAIN = "www.my-jira-server.com/";
    private static final int JIRA_PORT = 8080;
    private static final String JIRA_HTTP_URL = "http://" + JIRA_DOMAIN;
    private static final String JIRA_ALTERNATIVE_HTTP_URL = "http://" + JIRA_ALTERNATIVE_DOMAIN;
    private static final String JIRA_HTTP_URL_WITH_PORT = "http://" + JIRA_DOMAIN + ":" + JIRA_PORT;
    private static final String JIRA_ALTERNATIVE_HTTP_URL_WITH_PORT = "http://" + JIRA_ALTERNATIVE_DOMAIN + ":" + JIRA_PORT;

    @Mock
    private Jenkins jenkins;

    @Mock
    private ProxyConfiguration proxyConfiguration;

    private HttpRequestProvider.ProxyBean proxyBean;

    @Before
    public void setup() throws IOException {
        PowerMockito.mockStatic(Jenkins.class);
        PowerMockito.mockStatic(ProxyConfiguration.class);

        Whitebox.setInternalState(jenkins, "proxy", proxyConfiguration);

        when(ProxyConfiguration.load()).thenReturn(proxyConfiguration);
        when(Jenkins.getInstanceOrNull()).thenReturn(jenkins);
    }

    private void setProxyConfiguration(String hostname, int port) {
        Whitebox.setInternalState(proxyConfiguration, "name", hostname);
        Whitebox.setInternalState(proxyConfiguration, "port", port);
    }

    private void setNoProxyHost(String url) {
        Whitebox.setInternalState(proxyConfiguration, "noProxyHost", url);
    }

    private void createProxyBeanAndXrayClient() {
        proxyBean = ProxyUtil.createProxyBean();
        assertNotNull(proxyBean);
    }

    @Test
    public void testCreateProxyWithProxy() {
        setProxyConfiguration(PROXY_DOMAIN, PROXY_PORT);

        createProxyBeanAndXrayClient();
        assertTrue(proxyBean.useProxy(JIRA_HTTP_URL));
        assertTrue(proxyBean.useProxy(JIRA_HTTP_URL_WITH_PORT));
        assertTrue(proxyBean.useProxy(JIRA_ALTERNATIVE_HTTP_URL));
        assertTrue(proxyBean.useProxy(JIRA_ALTERNATIVE_HTTP_URL_WITH_PORT));
    }

    @Test
    public void testCreateProxyWithProxyAndNoProxyHost() {
        setProxyConfiguration(PROXY_DOMAIN, PROXY_PORT);
        setNoProxyHost(JIRA_DOMAIN + "\n" +
                       JIRA_ALTERNATIVE_DOMAIN);

        createProxyBeanAndXrayClient();
        assertFalse(proxyBean.useProxy(JIRA_HTTP_URL));
        assertFalse(proxyBean.useProxy(JIRA_HTTP_URL_WITH_PORT));
        assertFalse(proxyBean.useProxy(JIRA_ALTERNATIVE_HTTP_URL));
        assertFalse(proxyBean.useProxy(JIRA_ALTERNATIVE_HTTP_URL_WITH_PORT));
    }

    @Test
    public void testCreateProxyWithProxyAndNoProxyHostAndPort() {
        setProxyConfiguration(PROXY_DOMAIN, PROXY_PORT);
        setNoProxyHost(JIRA_DOMAIN + ":" + JIRA_PORT + "\n" +
                       JIRA_ALTERNATIVE_DOMAIN + ":" + JIRA_PORT);

        createProxyBeanAndXrayClient();
        assertTrue(proxyBean.useProxy(JIRA_HTTP_URL));
        assertFalse(proxyBean.useProxy(JIRA_HTTP_URL_WITH_PORT));
        assertTrue(proxyBean.useProxy(JIRA_ALTERNATIVE_HTTP_URL));
        assertFalse(proxyBean.useProxy(JIRA_ALTERNATIVE_HTTP_URL_WITH_PORT));
    }

    @Test
    public void testCreateProxyWithProxyAndNoProxyHosts() {
        setProxyConfiguration(PROXY_DOMAIN, PROXY_PORT);
        setNoProxyHost("host1.com \n" +
                       "host2.com \n" +
                       "host3.com \n" +
                       JIRA_DOMAIN + "\n" +
                       JIRA_ALTERNATIVE_DOMAIN);

        createProxyBeanAndXrayClient();
        assertFalse(proxyBean.useProxy(JIRA_HTTP_URL));
        assertFalse(proxyBean.useProxy(JIRA_HTTP_URL_WITH_PORT));
        assertFalse(proxyBean.useProxy(JIRA_ALTERNATIVE_HTTP_URL));
        assertFalse(proxyBean.useProxy(JIRA_ALTERNATIVE_HTTP_URL_WITH_PORT));
    }

    @Test
    public void testCreateProxyWithProxyAndRandomNoProxyHost() {
        setProxyConfiguration(PROXY_DOMAIN, PROXY_PORT);
        setNoProxyHost("host1");

        createProxyBeanAndXrayClient();
        assertTrue(proxyBean.useProxy(JIRA_HTTP_URL));
        assertTrue(proxyBean.useProxy(JIRA_HTTP_URL_WITH_PORT));
        assertTrue(proxyBean.useProxy(JIRA_ALTERNATIVE_HTTP_URL));
        assertTrue(proxyBean.useProxy(JIRA_ALTERNATIVE_HTTP_URL_WITH_PORT));
    }

    @Test
    public void testCreateProxyWithProxyAndWrongPortNoProxyHost() {
        setProxyConfiguration(PROXY_DOMAIN, PROXY_PORT);
        setNoProxyHost(JIRA_HTTP_URL + ":" + (JIRA_PORT + 1));

        createProxyBeanAndXrayClient();
        assertTrue(proxyBean.useProxy(JIRA_HTTP_URL));
        assertTrue(proxyBean.useProxy(JIRA_HTTP_URL_WITH_PORT));
        assertTrue(proxyBean.useProxy(JIRA_ALTERNATIVE_HTTP_URL));
        assertTrue(proxyBean.useProxy(JIRA_ALTERNATIVE_HTTP_URL_WITH_PORT));
    }

}
