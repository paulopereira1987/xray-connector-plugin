package com.xpandit.plugins.xrayjenkins.model;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.xpandit.plugins.xrayjenkins.Utils.ProxyUtil;
import com.xpandit.xray.service.impl.XrayClientImpl;
import com.xpandit.xray.service.impl.XrayCloudClientImpl;
import com.xpandit.xray.service.impl.bean.ConnectionResult;
import com.xpandit.xray.service.impl.delegates.HttpRequestProvider;
import hudson.Extension;
import hudson.model.Item;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import java.util.Collections;
import java.util.Objects;
import java.util.stream.Collectors;
import jenkins.model.GlobalConfiguration;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.interceptor.RequirePOST;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static com.cloudbees.plugins.credentials.CredentialsMatchers.withId;
import static com.xpandit.plugins.xrayjenkins.Utils.CredentialUtil.getAllSystemCredentials;
import static com.xpandit.plugins.xrayjenkins.Utils.CredentialUtil.getAllCredentialsListBoxModel;

@Extension
public class ServerConfiguration extends GlobalConfiguration {

    private static final int MAX_ERROR_TEXT_LENGTH = 200; // This is around 2-3 lines in the Server Configuration UI.

    private static final Logger logger = LoggerFactory.getLogger(ServerConfiguration.class);

    private List<XrayInstance> serverInstances = new ArrayList<>();
	
	public ServerConfiguration(){
		load();
        checkForCompatibility();
	}
	
	@Override
    public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
        
        req.bindJSON(this, formData.getJSONObject("xrayinstance"));
        
        save();
        return true;
    }
	
	public void setServerInstances(List<XrayInstance> serverInstances){
        this.serverInstances = serverInstances;
    }

    public List<XrayInstance> getServerInstances(){
		return this.serverInstances;
	}

	public String getCloudHostingTypeName(){
	    return HostingType.getCloudHostingName();
    }

    public String getServerHostingTypeName(){
        return HostingType.getServerHostingName();
    }
	
	public static ServerConfiguration get() {
	    return GlobalConfiguration.all().get(ServerConfiguration.class);
	}

    public ListBoxModel doFillCredentialIdItems(@AncestorInPath Item item, @QueryParameter String credentialId) {
        return getAllCredentialsListBoxModel(item, credentialId);
    }

    public FormValidation doCheckCredentialId(
            @AncestorInPath Item item,
            @QueryParameter String value
    ) {
        if (item == null && !Jenkins.get().hasPermission(Jenkins.ADMINISTER)
                || item != null && !item.hasPermission(Item.EXTENDED_READ) && !item.hasPermission(CredentialsProvider.USE_ITEM)) {
            return FormValidation.ok();
        }

        if (StringUtils.isBlank(value)) {
            return FormValidation.warning("Leave the credentials field empty if you want to pick user scoped credentials for each Build Task.");
        }
        
        if (!credentialExists(item, value)) {
            return FormValidation.error("Cannot find currently selected credentials");
        }
        return FormValidation.ok();
    }

    public FormValidation doCheckServerAddress(
            @AncestorInPath final Item item,
            @QueryParameter("hosting") final String hosting,
            @QueryParameter("serverAddress") final String serverAddress
    ) {
        if (item == null && !Jenkins.get().hasPermission(Jenkins.ADMINISTER)
                || item != null && !item.hasPermission(Item.EXTENDED_READ) && !item.hasPermission(CredentialsProvider.USE_ITEM)) {
            // If the user is not authenticated, we follow Jenkins standards and always return OK.
            // This is to avoid giving up more information than required.
            return FormValidation.ok();
        }

        // We only need to check the URL for Server instances, since Cloud API URL is hardcoded and controlled by us.
        if (Objects.equals(hosting, HostingType.SERVER.toString()) && StringUtils.isNotBlank(serverAddress)) {
            HttpRequestProvider.ProxyBean proxyBean = ProxyUtil.createProxyBean();
            XrayClientImpl xrayClient = new XrayClientImpl(serverAddress, null, null, proxyBean);
            if (!xrayClient.isJiraInstance()) {
                logger.error("URL provided is not from a Jira instance -> {}/rest/api/2/serverInfo didn't return a valid result.", serverAddress);
                return FormValidation.error("URL provided is not from a Jira instance (check if your /serverInfo endpoint is not blocked)");
            }
        }

        return FormValidation.ok();
    }

    @RequirePOST
	public FormValidation doTestConnection(@AncestorInPath final Item item,
                                           @QueryParameter("hosting") final String hosting,
	                                       @QueryParameter("serverAddress") final String serverAddress,
                                           @QueryParameter("credentialId") final String credentialId) {

        Jenkins.get().checkPermission(Jenkins.ADMINISTER);
        if (StringUtils.isBlank(credentialId)) {
            return FormValidation.error("Authentication is optional, however it is required in order to test the connection.");
        }

        if (StringUtils.isBlank(hosting)) {
            return FormValidation.error("Hosting type can't be blank.");
        }

        final StandardUsernamePasswordCredentials credential = CredentialsMatchers.firstOrNull(getAllSystemCredentials(item), withId(credentialId));
        if (credential == null) {
            return FormValidation.error("Cannot find currently selected credentials");
        }
        
        final String username = credential.getUsername();
        final String password = credential.getPassword().getPlainText();
        final HttpRequestProvider.ProxyBean proxyBean = ProxyUtil.createProxyBean();
        final ConnectionResult connectionResult;

        if (hosting.equals(HostingType.CLOUD.toString())) {
            connectionResult = new XrayCloudClientImpl(username, password, proxyBean).testConnection();
        } else if (hosting.equals(HostingType.SERVER.toString())) {
            if(StringUtils.isBlank(serverAddress)) {
                return FormValidation.error("Server address can't be empty");
            }

            XrayClientImpl xrayClient = new XrayClientImpl(serverAddress, username, password, proxyBean);
            connectionResult = jiraServerTestConnection(serverAddress, xrayClient);
        } else {
            return FormValidation.error("Hosting type not recognized.");
        }

        if (connectionResult.isSuccessful()) {
            return FormValidation.ok("Connection: Success!");
        } else {
            final String errorText = "Could not establish connection.\n" +
                    limitStringSize(connectionResult.getErrorText()) +
                    "\n\nFor more information please check the logs.";

            logger.error("Error while connecting to instance:\n{}", connectionResult.getErrorText());
            return FormValidation.error(errorText);
        }
    }

    private ConnectionResult jiraServerTestConnection(String serverAddress, XrayClientImpl xrayClient) {
        if (!xrayClient.isJiraInstance()) {
            logger.error("URL provided is not from a Jira instance -> {}/rest/api/2/serverInfo didn't return a valid result.", serverAddress);
            return ConnectionResult.connectionFailed("URL provided is not from a Jira instance (check if your /serverInfo endpoint is not blocked)");
        } else {
            return xrayClient.testConnection();
        }
    }

    private String limitStringSize(final String errorText) {
	    return StringUtils.trim(StringUtils.substring(errorText, 0, MAX_ERROR_TEXT_LENGTH));
    }

    private void checkForCompatibility(){
        for(XrayInstance instance : serverInstances){
            if(instance.getHosting() == null){
                instance.setHosting(HostingType.getDefaultType());
            }
        }
    }
    
    @Nullable
    private StandardUsernamePasswordCredentials findCredential(@Nullable final Item item, @Nullable final String credentialId) {
	    if (StringUtils.isBlank(credentialId)) {
	        return null;
        }
        return CredentialsMatchers.firstOrNull(getAllSystemCredentials(item), withId(credentialId));
    }

    private boolean credentialExists(@Nullable final Item item, @Nullable final String credentialId) {
        return findCredential(item, credentialId) != null;
    }
}
