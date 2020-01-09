package com.xpandit.plugins.xrayjenkins.Utils;

import hudson.EnvVars;
import org.apache.commons.lang3.StringUtils;

public class EnvironmentVariableUtil {
    enum XrayEnvironmentVariable {
        XRAY_IS_REQUEST_SUCCESSFUL, // "true", if the latest Xray request was successful. "false" otherwise.
        XRAY_RAW_RESPONSE, // The raw response String of the latest Xray request.
        XRAY_TEST_EXECS, // Test Execution Issues created/modified, separated by a semicolon.
        XRAY_TESTS  // Test Issues created/modified, separated by a semicolon.
    }

    private EnvironmentVariableUtil() {}

    /**
     * Tries to expand a variable value, using the Jenkins Variable Environment.
     * Example: ${ISSUEKEY} will be replaced by the value defined in the Environment, if it's defined.
     * 
     * @param environment Jenkins Variable environment.
     * @param variable the variable to be replaced
     * @return the variable value, if it's defined, otherwise, it will return the variable itself.
     */
    public static String expandVariable(final EnvVars environment, final String variable) {
        if (environment == null) {
            return StringUtils.defaultString(variable);
        } else if (StringUtils.isNotBlank(variable)) {
            final String expanded = environment.expand(variable);
            return StringUtils.equals(expanded, variable) ? variable : expanded;
        }
        return StringUtils.EMPTY;
    }

    /**
     * Adds a new Jenkins Environment Variable, if none of the parameters is null.
     *
     * @param environment Jenkins Variable environment.
     * @param key an Xray Variable.
     * @param value the value oif the variable
     */
    public static void addVariable(final EnvVars environment, final XrayEnvironmentVariable key, final String value) {
        if (environment != null && key != null) {
            environment.putIfNotNull(key.name(), value);
        }
    }

    /**
     * Writes an empty string in all Xray environment variables.
     *
     * @param environment Jenkins Variable environment.
     */
    public static void resetAllVariables(final EnvVars environment) {
        for (XrayEnvironmentVariable variable : XrayEnvironmentVariable.values()) {
            addVariable(environment, variable, StringUtils.EMPTY);
        }
    }
}
