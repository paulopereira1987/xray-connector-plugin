package com.xpandit.plugins.xrayjenkins.services;

import hudson.EnvVars;
import hudson.model.AbstractBuild;
import hudson.model.EnvironmentContributingAction;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

public class XrayEnvironmentInjectAction implements EnvironmentContributingAction {

    private final Map<String, String> newVariablesToAdd;
    private final Set<String> variablesToRemove;

    public XrayEnvironmentInjectAction(@Nonnull Map<String, String> variablesToAdd, @Nonnull Collection<String> variablesToRemove) {
        Objects.requireNonNull(variablesToAdd, "'variablesToAdd' can't be null!");
        Objects.requireNonNull(variablesToRemove, "'variablesToRemove' can't be null!");

        this.newVariablesToAdd = new ConcurrentHashMap<>(variablesToAdd);
        this.variablesToRemove = new ConcurrentSkipListSet<>(variablesToRemove);
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
