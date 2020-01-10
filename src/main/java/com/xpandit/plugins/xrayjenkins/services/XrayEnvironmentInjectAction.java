package com.xpandit.plugins.xrayjenkins.services;

import hudson.EnvVars;
import hudson.model.AbstractBuild;
import hudson.model.EnvironmentContributingAction;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class XrayEnvironmentInjectAction implements EnvironmentContributingAction, Serializable {

    private final Map<String, String> newVariablesToAdd;
    private final Set<String> variablesToRemove;

    public XrayEnvironmentInjectAction(@Nonnull Map<String, String> variablesToAdd, @Nonnull Collection<String> variablesToRemove) {
        Objects.requireNonNull(variablesToAdd, "'variablesToAdd' can't be null!");
        Objects.requireNonNull(variablesToRemove, "'variablesToRemove' can't be null!");

        // We can't use Java native Synchronized structures since they are blocked by Jenkins in a Pipeline project
        // See: https://jenkins.io/blog/2018/01/13/jep-200/
        this.newVariablesToAdd = Collections.synchronizedMap(new HashMap<>(variablesToAdd));
        this.variablesToRemove = Collections.synchronizedSet(new HashSet<>(variablesToRemove));
    }

    @Override
    public void buildEnvVars(AbstractBuild<?, ?> build, EnvVars env) {
        for (String keyToRemove : variablesToRemove) {
            env.remove(keyToRemove);
        }

        env.putAll(newVariablesToAdd);
    }

    @CheckForNull
    @Override
    public String getIconFileName() {
        return null;
    }

    @CheckForNull
    @Override
    public String getDisplayName() {
        return null;
    }

    @CheckForNull
    @Override
    public String getUrlName() {
        return null;
    }
}
