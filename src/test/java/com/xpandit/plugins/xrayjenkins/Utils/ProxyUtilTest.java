package com.xpandit.plugins.xrayjenkins.Utils;

import com.xpandit.xray.service.impl.XrayClientImpl;
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

    public static final String PROXY_HOSTNAME = "whatever";
    public static final int PROXY_PORT = 8080;
    public static final String JIRA_DOMAIN = "localhost";
    public static final int JIRA_PORT = 8084;
    public static final String JIRA_HTTP_URL = "http://" + JIRA_DOMAIN + ":" + JIRA_PORT;
    public static final String JIRA_USERNAME = "admin";
    public static final String JIRA_PASSWORD = "admin";

    @Mock
    private Jenkins jenkins;

    @Mock
    private ProxyConfiguration proxyConfiguration;

    private XrayClientImpl xrayClient;

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
        final HttpRequestProvider.ProxyBean proxyBean = ProxyUtil.createProxyBean();
        assertNotNull(proxyBean);

        xrayClient = new XrayClientImpl(JIRA_HTTP_URL,
                                        JIRA_USERNAME,
                                        JIRA_PASSWORD,
                                        proxyBean);
    }

    @Test
    public void testCreateProxyWithProxy_success() {
        setProxyConfiguration(JIRA_DOMAIN, JIRA_PORT);
        createProxyBeanAndXrayClient();

        assertTrue(xrayClient.testConnection().isSuccessful());
    }

    @Test
    public void testCreateProxyWithProxy_failure() {
        setProxyConfiguration(PROXY_HOSTNAME, PROXY_PORT);
        createProxyBeanAndXrayClient();

        assertFalse(xrayClient.testConnection().isSuccessful());
    }

    @Test
    public void testCreateProxyWithProxyAndNoProxyHost_success() {
        setProxyConfiguration(PROXY_HOSTNAME, PROXY_PORT);
        setNoProxyHost(JIRA_DOMAIN);

        createProxyBeanAndXrayClient();
        assertTrue(xrayClient.testConnection().isSuccessful());
    }

    @Test
    public void testCreateProxyWithProxyAndNoProxyHosts_success() {
        setProxyConfiguration(PROXY_HOSTNAME, PROXY_PORT);
        setNoProxyHost("host1 \n" +
                       "host2 \n" +
                       "host3 \n" +
                       JIRA_DOMAIN);

        createProxyBeanAndXrayClient();
        assertTrue(xrayClient.testConnection().isSuccessful());
    }

    @Test
    public void testCreateProxyWithProxyAndNoProxyHost_failure() {
        setProxyConfiguration(PROXY_HOSTNAME, PROXY_PORT);
        setNoProxyHost("host1");

        createProxyBeanAndXrayClient();
        assertFalse(xrayClient.testConnection().isSuccessful());
    }

    @Test
    public void testCreateProxyWithProxyAndNoProxyHostAndPort_failure() {
        setProxyConfiguration(PROXY_HOSTNAME, PROXY_PORT);
        setNoProxyHost("http://" + JIRA_DOMAIN + ":" + (JIRA_PORT + 1));

        createProxyBeanAndXrayClient();
        assertFalse(xrayClient.testConnection().isSuccessful());
    }

}
