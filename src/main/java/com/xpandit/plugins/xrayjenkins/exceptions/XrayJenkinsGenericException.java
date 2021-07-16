package com.xpandit.plugins.xrayjenkins.exceptions;

public class XrayJenkinsGenericException extends RuntimeException {
    public XrayJenkinsGenericException(String s) {
        super(s);
    }
    
    public XrayJenkinsGenericException(Exception e) {
        super(e);
    }
}
