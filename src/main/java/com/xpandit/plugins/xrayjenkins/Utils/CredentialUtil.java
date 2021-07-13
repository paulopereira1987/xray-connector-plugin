package com.xpandit.plugins.xrayjenkins.Utils;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import hudson.model.Item;
import hudson.security.ACL;
import hudson.util.ListBoxModel;
import jenkins.model.Jenkins;
import org.acegisecurity.Authentication;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class CredentialUtil {

    private CredentialUtil() {}

    /**
     * Gets all the System credentials from a given Item context.
     *
     * @param item the context.
     * @return All the System credentials from a given Item context.
     */
    public static List<StandardUsernamePasswordCredentials> getAllSystemCredentials(@Nullable final Item item) {
        if (Jenkins.get().hasPermission(Jenkins.ADMINISTER)) {
            return getStandardUsernamePasswordCredentials(item, ACL.SYSTEM);
        }

        return Collections.emptyList();
    }

    /**
     * Gets all the System credentials from a given Item context, as a ListBoxModel.
     *
     * @param item the context.
     * @param credentialId the previously selected Credential ID.
     * @return All the System credentials from a given Item context.
     */
    public static ListBoxModel getAllCredentialsListBoxModel(@Nullable final Item item, final String credentialId) {
        return getCredentialsListBoxModel(credentialId, getAllSystemCredentials(item));
    }

    /**
     * Gets all the User credentials (for the currently logged in user) from a given Item context.
     *
     * @param item the context.
     * @return All the User credentials from a given Item context.
     */
    public static List<StandardUsernamePasswordCredentials> getAllUserScopedCredentials(@Nullable final Item item) {
        return getAllUserScopedCredentials(item, Jenkins.getAuthentication());
    }

    /**
     * Gets all the User credentials from a given Item context.
     *
     * @param item the context.
     * @param authentication the authentication of the user where the Credentials are located.
     * @return All the User credentials from a given Item context.
     */
    public static List<StandardUsernamePasswordCredentials> getAllUserScopedCredentials(@Nullable final Item item,
                                                                                        @Nullable final Authentication authentication) {
        return Jenkins.get().hasPermission(CredentialsProvider.USE_OWN) ?
                getStandardUsernamePasswordCredentials(item, authentication) :
                Collections.emptyList();
    }

    /**
     * Gets all the User credentials from a given Item context, as a ListBoxModel.
     *
     * @param item the context.
     * @param credentialId the previously selected Credential ID.
     * @return All the User credentials from a given Item context.
     */
    public static ListBoxModel getUserScopedCredentialsListBoxModel(@Nullable final Item item, final String credentialId) {
        return getCredentialsListBoxModel(credentialId, getAllUserScopedCredentials(item));
    }

    private static ListBoxModel getCredentialsListBoxModel(final String credentialId,
                                                           final List<StandardUsernamePasswordCredentials> credentials) {
        final StandardListBoxModel result = new StandardListBoxModel();

        result.includeEmptyValue();
        for (StandardUsernamePasswordCredentials credential : credentials) {
            result.with(credential);
        }
        return result.includeCurrentValue(credentialId);
    }

    private static List<StandardUsernamePasswordCredentials> getStandardUsernamePasswordCredentials(
            @Nullable Item item,
            @Nullable Authentication authentication
    ) {
        List<StandardUsernamePasswordCredentials> credentials = CredentialsProvider.lookupCredentials(
                StandardUsernamePasswordCredentials.class,
                item,
                authentication,
                Collections.emptyList());
        return Collections.unmodifiableList(credentials);
    }
}
