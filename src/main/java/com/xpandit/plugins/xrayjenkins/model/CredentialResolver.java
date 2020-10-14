package com.xpandit.plugins.xrayjenkins.model;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;
import com.xpandit.plugins.xrayjenkins.Utils.CredentialUtil;
import hudson.model.Cause;
import hudson.model.Item;
import hudson.model.Run;
import hudson.model.User;
import hudson.util.Secret;

import java.net.Authenticator;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;

import jenkins.model.Jenkins;
import org.acegisecurity.Authentication;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.Stapler;

public class CredentialResolver {
    private final String credentialId;
    private final Run<?, ?> run;
    
    private String username = null;
    private Secret password = null;
    
    public CredentialResolver(final String credentialId, final Run<?, ?> run) {
        this.credentialId = credentialId;
        this.run = run;
    }

    @Nullable
    public String getUsername() {
        resolveUsernamePassword();
        return username;
    }

    @Nullable
    public String getPassword() {
        resolveUsernamePassword();
        if (password != null) {
            return password.getPlainText();
        }
        
        return null;
    }
    
    private void resolveUsernamePassword() {
        if (StringUtils.isNotBlank(this.credentialId)) {
            final StandardUsernamePasswordCredentials credential = findCredentialById();
            if (credential != null) {
                this.username = credential.getUsername();
                this.password = credential.getPassword();
            }
        }
    }

    @Nullable
    private StandardUsernamePasswordCredentials findCredentialById() {
        // Find the Credential at "System" level.
        final StandardUsernamePasswordCredentials credential =
                CredentialsProvider.findCredentialById(this.credentialId,
                                                       StandardUsernamePasswordCredentials.class,
                                                       run,
                                                       (List<DomainRequirement>) null);

        if (credential != null) {
            return credential;
        }

        // Find the Credential at "User" (who is running the build) level.
        final Authentication buildUserAuth = Optional.ofNullable(run.getCause(Cause.UserIdCause.class))
                                                          .map(Cause.UserIdCause::getUserId)
                                                          .filter(StringUtils::isNotBlank)
                                                          .map(userId -> User.getById(userId, false))
                                                          .map(User::impersonate)
                                                          .orElse(null);

        return CredentialUtil.getAllUserScopedCredentials(run.getParent(), buildUserAuth)
                .stream()
                .filter(cred -> StringUtils.equals(cred.getId(), this.credentialId))
                .findFirst()
                .orElse(null);

    }
}
