/*
 * XP.RAVEN Project
 * <p/>
 * Copyright (C) 2018 Xpand IT.
 * <p/>
 * This software is proprietary.
 */
package com.xpandit.plugins.xrayjenkins.Utils;

import com.google.common.collect.Iterables;
import com.xpandit.plugins.xrayjenkins.model.HostingType;
import com.xpandit.plugins.xrayjenkins.model.ServerConfiguration;
import com.xpandit.plugins.xrayjenkins.model.XrayInstance;
import java.util.List;
import java.util.Optional;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;

public class ConfigurationUtils {

    private static final Logger LOG = LoggerFactory.getLogger(ConfigurationUtils.class);

    /**
     * Utility method to get an XrayInstance
     * @param serverConfigurationId the server configuration ID
     * @return <code>XrayInstance</code> if found, <code>null</code> otherwise
     */
    @Nullable
    public static XrayInstance getConfiguration(String serverConfigurationId) {
        if (StringUtils.isBlank(serverConfigurationId)) {
            return null;
        }

        if(serverConfigurationId.startsWith(HostingType.CLOUD.toString())){
            serverConfigurationId = StringUtils.removeStart(serverConfigurationId, HostingType.getCloudHostingName() + "-");
        } else if(serverConfigurationId.startsWith(HostingType.SERVER.toString())) {
            serverConfigurationId = StringUtils.removeStart(serverConfigurationId, HostingType.getServerHostingName() + "-");
        }

        XrayInstance config =  null;
        List<XrayInstance> serverInstances = ServerConfiguration.get().getServerInstances();
        for(XrayInstance sc : serverInstances){
            if(sc.getConfigID().equals(serverConfigurationId)){
                config = sc;
                break;
            }
        }
        if(config == null){
            LOG.error("No XrayInstance could be found with configuration id '{}'", serverConfigurationId);
        }
        return config;
    }

    @Nullable
    public static XrayInstance getConfigurationOrFirstAvailable(String serverConfigurationId) {
        return Optional.ofNullable(getConfiguration(serverConfigurationId))
                .orElseGet(() -> Iterables.getFirst(ServerConfiguration.get().getServerInstances(), null));
    }
        /**
     * Utility method to check if any xray Jira server configuration exists
     * @return <code>true</code> if any server configuration is available, <code>false</code> otherwise
     */
    public static boolean anyAvailableConfiguration(){
        ServerConfiguration configuration = ServerConfiguration.get();
        return configuration != null
                && configuration.getServerInstances() != null
                && configuration.getServerInstances().size() > 0;
    }

}
