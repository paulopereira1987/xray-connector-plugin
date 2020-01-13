package com.xpandit.plugins.xrayjenkins.model;

public enum HostingType {
    SERVER, CLOUD;

    public static String getCloudHostingName(){
        return HostingType.CLOUD.toString();
    }

    public static String getServerHostingName(){
        return HostingType.SERVER.toString();
    }

    public static HostingType getDefaultType() {
        return HostingType.SERVER;
    }
}
