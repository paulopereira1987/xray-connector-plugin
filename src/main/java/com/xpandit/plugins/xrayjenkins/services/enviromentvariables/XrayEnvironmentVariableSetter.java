package com.xpandit.plugins.xrayjenkins.services.enviromentvariables;

import com.xpandit.plugins.xrayjenkins.model.HostingType;
import com.xpandit.plugins.xrayjenkins.services.enviromentvariables.util.XrayEnvironmentVariableSetterHelperUtil;
import com.xpandit.xray.model.UploadResult;
import hudson.model.Run;
import hudson.model.TaskListener;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.xpandit.plugins.xrayjenkins.services.enviromentvariables.util.XrayEnvironmentVariableSetterHelperUtil.FALSE_STRING;
import static com.xpandit.plugins.xrayjenkins.services.enviromentvariables.util.XrayEnvironmentVariableSetterHelperUtil.TRUE_STRING;
import static com.xpandit.plugins.xrayjenkins.services.enviromentvariables.util.XrayEnvironmentVariableSetterHelperUtil.getCreatedTestKeys;
import static com.xpandit.plugins.xrayjenkins.services.enviromentvariables.util.XrayEnvironmentVariableSetterHelperUtil.getImportedFeatureIssueKeys;
import static com.xpandit.plugins.xrayjenkins.services.enviromentvariables.util.XrayEnvironmentVariableSetterHelperUtil.getModifiedTestExecutionsKeys;
import static com.xpandit.plugins.xrayjenkins.services.enviromentvariables.util.XrayEnvironmentVariableSetterHelperUtil.getRawResponses;
import static com.xpandit.plugins.xrayjenkins.services.enviromentvariables.util.XrayEnvironmentVariableSetterHelperUtil.isUploadSuccessful;

public class XrayEnvironmentVariableSetter {

    private enum XrayEnvironmentVariable {
        XRAY_IS_REQUEST_SUCCESSFUL, // "true", if the latest Xray request was successful. "false" otherwise.
        XRAY_ISSUES_MODIFIED, // All issues created or modified
        XRAY_RAW_RESPONSE, // The raw response String of the latest Xray request.
        XRAY_TEST_EXECS, // Test Execution Issues created/modified, separated by a semicolon.
        XRAY_TESTS  // Test Issues created/modified, separated by a semicolon.
    }

    private final Map<XrayEnvironmentVariable, String> newVariables;

    private XrayEnvironmentVariableSetter() {
        // We can't use Java native Synchronized structures since they are blocked by Jenkins in a Pipeline project
        // See: https://jenkins.io/blog/2018/01/13/jep-200/
        newVariables = Collections.synchronizedMap(new HashMap<XrayEnvironmentVariable, String>());

        for (XrayEnvironmentVariable variable : XrayEnvironmentVariable.values()) {
            newVariables.put(variable, StringUtils.EMPTY);
        }
    }

    public static XrayEnvironmentVariableSetter parseCucumberFeatureImportResponse(final Collection<UploadResult> results,
                                                                                   final HostingType hostingType,
                                                                                   final PrintStream logger) {
        final XrayEnvironmentVariableSetter variableSetter = new XrayEnvironmentVariableSetter();

        variableSetter.newVariables.put(XrayEnvironmentVariable.XRAY_RAW_RESPONSE, getRawResponses(results));
        variableSetter.newVariables.put(XrayEnvironmentVariable.XRAY_IS_REQUEST_SUCCESSFUL, isUploadSuccessful(results));
        variableSetter.newVariables.put(XrayEnvironmentVariable.XRAY_ISSUES_MODIFIED, getImportedFeatureIssueKeys(results, hostingType, logger));

        return variableSetter;
    }

    public static XrayEnvironmentVariableSetter parseResultImportResponse(final Collection<UploadResult> results,
                                                                          final HostingType hostingType,
                                                                          final PrintStream logger) {
        if (results == null) {
            return failed();
        }

        final XrayEnvironmentVariableSetter variableSetter = new XrayEnvironmentVariableSetter();

        variableSetter.newVariables.put(XrayEnvironmentVariable.XRAY_RAW_RESPONSE, getRawResponses(results));
        variableSetter.newVariables.put(XrayEnvironmentVariable.XRAY_IS_REQUEST_SUCCESSFUL, isUploadSuccessful(results));

        final String testExecKeys = getModifiedTestExecutionsKeys(results, hostingType, logger);
        final String testKeys = getCreatedTestKeys(results, hostingType, logger);
        variableSetter.newVariables.put(XrayEnvironmentVariable.XRAY_TEST_EXECS, testExecKeys);
        variableSetter.newVariables.put(XrayEnvironmentVariable.XRAY_TESTS, testKeys);
        variableSetter.newVariables.put(XrayEnvironmentVariable.XRAY_ISSUES_MODIFIED, getAllKeys(testExecKeys, testKeys));

        return variableSetter;
    }

    public static XrayEnvironmentVariableSetter success() {
        return success(null);
    }

    public static XrayEnvironmentVariableSetter success(@Nullable final String message) {
        final XrayEnvironmentVariableSetter variableSetter = new XrayEnvironmentVariableSetter();

        variableSetter.newVariables.put(XrayEnvironmentVariable.XRAY_IS_REQUEST_SUCCESSFUL, TRUE_STRING);
        if (message != null) {
            variableSetter.newVariables.put(XrayEnvironmentVariable.XRAY_RAW_RESPONSE, message);
        }

        return variableSetter;
    }

    public static XrayEnvironmentVariableSetter failed() {
        return failed(null);
    }

    public static XrayEnvironmentVariableSetter failed(@Nullable final String message) {
        final XrayEnvironmentVariableSetter variableSetter = new XrayEnvironmentVariableSetter();

        variableSetter.newVariables.put(XrayEnvironmentVariable.XRAY_IS_REQUEST_SUCCESSFUL, FALSE_STRING);
        if (message != null) {
            variableSetter.newVariables.put(XrayEnvironmentVariable.XRAY_RAW_RESPONSE, message);
        }

        return variableSetter;
    }

    public void setAction(Run<?,?> build, TaskListener taskListener) {
        setAction(build, taskListener.getLogger());
    }

    public void setAction(Run<?,?> build, @Nullable PrintStream logger) {
        if (build != null) {
            // Builds the same name, but with the name of each XrayEnvironmentVariable.
            final Map<String, String> newVariablesByName = new HashMap<>();
            for (Map.Entry<XrayEnvironmentVariable, String> entry : newVariables.entrySet()) {
                final String variableName = entry.getKey().name();
                final String variableValue = entry.getValue();

                newVariablesByName.put(variableName, variableValue);

                if (logger != null) {
                    logger.println(variableName + ": " + variableValue);
                }
            }

            // Adds action to Build
            final XrayEnvironmentInjectAction action = new XrayEnvironmentInjectAction(newVariablesByName, Collections.<String>emptyList());
            build.addOrReplaceAction(action);
        }
    }

    private static String getAllKeys(String... allKeys) {
        final List<String> keyList = new ArrayList<>(allKeys.length);
        for (String keys : allKeys) {
            if (StringUtils.isNotBlank(keys)) {
                keyList.add(keys);
            }
        }

        return StringUtils.join(keyList, XrayEnvironmentVariableSetterHelperUtil.SEPARATOR);
    }
}
