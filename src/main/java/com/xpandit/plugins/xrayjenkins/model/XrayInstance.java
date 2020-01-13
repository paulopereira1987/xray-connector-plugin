/**
 * XP.RAVEN Project
 * <p>
 * Copyright (C) 2016 Xpand IT.
 * <p>
 * This software is proprietary.
 */
package com.xpandit.plugins.xrayjenkins.model;

import com.xpandit.plugins.xrayjenkins.exceptions.XrayJenkinsGenericException;
import hudson.model.Run;
import java.util.UUID;
import javax.annotation.Nonnull;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Represents a Jira/Xray instance.
 */
public class XrayInstance {
	
	private String configID;
	private String alias;
	private String serverAddress;
	private HostingType hosting;
    private String credentialId;
    private CredentialResolver credentialResolver;

	@DataBoundConstructor
 	public XrayInstance(String configID, String alias, HostingType hosting, String serverAddress, String credentialId) {
		this.configID = StringUtils.isBlank(configID) ? UUID.randomUUID().toString() : configID;
		this.alias = alias;
		this.hosting = hosting;
		this.serverAddress = serverAddress;
		this.credentialId = credentialId;
 	}

    public String getConfigID(){
			return this.configID;
		}
		
	public void setConfigID(String configID){
		this.configID = configID;
	}
		
	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public String getServerAddress() {
		return serverAddress;
	}

	public void setServerAddress(String serverAddress) {
		this.serverAddress = serverAddress;
	}

	@Nonnull
	public CredentialResolver getCredential(final Run<?, ?> runContext) {
		this.credentialResolver = ObjectUtils.defaultIfNull(this.credentialResolver, new CredentialResolver(this.credentialId, runContext));
		return this.credentialResolver;
	}
	
	public HostingType getHosting() { return hosting; }

	public void setHosting(HostingType hosting) { this.hosting = hosting; }

	public String getCredentialId() {
		return credentialId;
	}

	public void setCredentialId(String credentialId) {
		this.credentialId = credentialId;
	}
}

