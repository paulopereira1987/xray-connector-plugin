package com.xpandit.plugins.xrayjenkins.services.enviromentvariables.util;

import com.xpandit.plugins.xrayjenkins.model.HostingType;
import com.xpandit.xray.model.UploadResult;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class XrayEnvironmentVariableSetterHelperUtil {

    private static final char SEPARATOR = ';';

    public static final String TRUE_STRING = Boolean.toString(true);
    public static final String FALSE_STRING = Boolean.toString(false);

    private XrayEnvironmentVariableSetterHelperUtil() {}

    public static String getRawResponses(@Nonnull Collection<UploadResult> results) {
        final List<String> resultsString = new ArrayList<>(results.size());
        for (UploadResult result : results) {
            resultsString.add(result.getMessage());
        }

        return StringUtils.join(resultsString, SEPARATOR);
    }

    public static String getModifiedTestExecutionsKeys(Collection<UploadResult> results, HostingType hostingType, @Nullable PrintStream logger) {
        final Set<String> testExecutionKeys = new HashSet<>(results.size());
        for (UploadResult result : results) {
            final String testExecutionKey = getTestExecutionKey(result, hostingType, logger);

            if (StringUtils.isNotBlank(testExecutionKey)) {
                testExecutionKeys.add(testExecutionKey);
            }
        }

        return StringUtils.join(testExecutionKeys, SEPARATOR);
    }

    public static String getTestExecutionKey(UploadResult result, HostingType hostingType, @Nullable PrintStream logger) {
        JSONObject root;
        try {
            root = new JSONObject(result.getMessage());
        } catch (Exception e) {
            return StringUtils.EMPTY;
        }

        switch (hostingType) {
            case CLOUD:
                if (root.has("key")) {
                    return root.getString("key");
                }

                // Test Exec Key not found in the place we were expecting it.
                return StringUtils.EMPTY;
            case SERVER:
                if (root.has("testExecIssue")) {
                    JSONObject testExecIssue = root.getJSONObject("testExecIssue");
                    if (testExecIssue.has("key")) {
                        return testExecIssue.getString("key");
                    }
                }

                // Test Exec Key not found in the place we were expecting it.
                return StringUtils.EMPTY;
            default:
                if (logger != null) {
                    logger.println("[getTestExecutionKey] Hosting Type not implemented!");
                }
                return StringUtils.EMPTY;
        }
    }

    public static String getCreatedTestsKeys(Collection<UploadResult> results, HostingType hostingType, @Nullable PrintStream logger) {
        final Set<String> testKeys = new HashSet<>(results.size());
        for (UploadResult result : results) {
            final String testKey = getTestKey(result, hostingType, logger);

            if (StringUtils.isNotBlank(testKey)) {
                testKeys.add(testKey);
            }
        }

        return StringUtils.join(testKeys, SEPARATOR);
    }

    public static String getTestKey(UploadResult result, HostingType hostingType, @Nullable PrintStream logger) {
        JSONObject root;
        try {
            root = new JSONObject(result.getMessage());
        } catch (Exception e) {
            return StringUtils.EMPTY;
        }

        switch (hostingType) {
            case CLOUD:
                // Xray Cloud doesn't provide information about created Tests
                return StringUtils.EMPTY;
            case SERVER:
                Set<String> testKeys = extractTestKeysFromJson(root);

                // Test Exec Key not found in the place we were expecting it.
                return StringUtils.join(testKeys, SEPARATOR);
            default:
                if (logger != null) {
                    logger.println("[getTestKey] Hosting Type not implemented!");
                }
                return StringUtils.EMPTY;
        }
    }

    private static Set<String> extractTestKeysFromJson(JSONObject root) {
        final Set<String> testKeys = new HashSet<>();

        if (root.has("testIssues")) {
            JSONObject testIssuesResult = root.getJSONObject("testIssues");
            if (testIssuesResult.has("success")) {
                JSONArray createdTestIssues = testIssuesResult.getJSONArray("success");
                for (int i = 0; i < createdTestIssues.length(); i++) {
                    JSONObject testIssue = createdTestIssues.getJSONObject(i);
                    if (testIssue.has("key")) {
                        testKeys.add(testIssue.getString("key"));
                    }
                }
            }
        }

        return testKeys;
    }

    public static String isUploadSuccessful(Collection<UploadResult> results) {
        for (UploadResult result : results) {
            int statusCode = result.getStatusCode();

            // If one of the requests was not on the OK status code "family", than we consider that was action failed at some point.
            if (statusCode < 200 || statusCode > 299) {
                return FALSE_STRING;
            }
        }
        return TRUE_STRING;
    }
}
