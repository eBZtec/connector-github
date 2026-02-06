package org.kohsuke.github;

/**
 * Search SCIM users.
 *
 * @author Hiroyuki Wada
 */
public class SCIMUserSearchBuilder extends SCIMSearchBuilder<SCIMUser> {

    public SCIMUserSearchBuilder(GitHub root, GHOrganization org) {
        super(root, org, SCIMUserSearchResult.class);
    }

    private static class SCIMUserSearchResult extends SCIMSearchResult<SCIMUser> {
    }

    @Override
    public String getApiUrl() {
        return String.format("/scim/v2/organizations/%s/Users", organization.login);
    }
}
