package com.xpandit.plugins.xrayjenkins.services;

import com.xpandit.xray.model.UploadResult;
import hudson.model.Run;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class XrayEnvironmentVariableSetter {

    private enum XrayEnvironmentVariable {
        XRAY_IS_REQUEST_SUCCESSFUL, // "true", if the latest Xray request was successful. "false" otherwise.
        XRAY_RAW_RESPONSE, // The raw response String of the latest Xray request.
        XRAY_TEST_EXECS, // Test Execution Issues created/modified, separated by a semicolon.
        XRAY_TESTS  // Test Issues created/modified, separated by a semicolon.
    }


    private static final String TRUE_STRING = Boolean.toString(true);
    private static final String FALSE_STRING = Boolean.toString(false);

    private final Map<XrayEnvironmentVariable, String> newVariables;

    private XrayEnvironmentVariableSetter() {
        newVariables = new ConcurrentHashMap<>();

        for (XrayEnvironmentVariable variable : XrayEnvironmentVariable.values()) {
            newVariables.put(variable, StringUtils.EMPTY);
        }
    }

    public static XrayEnvironmentVariableSetter parseResponse(final UploadResult result) {
        //TODO
        return successful();
    }

    public static XrayEnvironmentVariableSetter successful() {
        final XrayEnvironmentVariableSetter variableSetter = new XrayEnvironmentVariableSetter();

        variableSetter.newVariables.put(XrayEnvironmentVariable.XRAY_IS_REQUEST_SUCCESSFUL, TRUE_STRING);

        return variableSetter;
    }

    public static XrayEnvironmentVariableSetter successful(final String message) {
        final XrayEnvironmentVariableSetter variableSetter = new XrayEnvironmentVariableSetter();

        variableSetter.newVariables.put(XrayEnvironmentVariable.XRAY_IS_REQUEST_SUCCESSFUL, TRUE_STRING);
        if (message != null) {
            variableSetter.newVariables.put(XrayEnvironmentVariable.XRAY_RAW_RESPONSE, message);
        }

        return variableSetter;
    }

    public static XrayEnvironmentVariableSetter failed() {
        final XrayEnvironmentVariableSetter variableSetter = new XrayEnvironmentVariableSetter();

        variableSetter.newVariables.put(XrayEnvironmentVariable.XRAY_IS_REQUEST_SUCCESSFUL, FALSE_STRING);

        return variableSetter;
    }

    public static XrayEnvironmentVariableSetter failed(final String message) {
        final XrayEnvironmentVariableSetter variableSetter = new XrayEnvironmentVariableSetter();

        variableSetter.newVariables.put(XrayEnvironmentVariable.XRAY_IS_REQUEST_SUCCESSFUL, FALSE_STRING);
        if (message != null) {
            variableSetter.newVariables.put(XrayEnvironmentVariable.XRAY_RAW_RESPONSE, message);
        }

        return variableSetter;
    }

    public void setAction(Run<?,?> build) {
        if (build != null) {
            // Builds the same name, but with the name of each XrayEnvironmentVariable.
            final Map<String, String> newVariablesByName = new HashMap<>();
            for (Map.Entry<XrayEnvironmentVariable, String> entry : newVariables.entrySet()) {
                newVariablesByName.put(entry.getKey().name(), entry.getValue());
            }

            // Adds action to Build
            final XrayEnvironmentInjectAction action = new XrayEnvironmentInjectAction(newVariablesByName, Collections.<String>emptyList());
            build.addOrReplaceAction(action);
        }
    }
}
