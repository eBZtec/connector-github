package org.kohsuke.github;

/**
 * Search SCIM users.
 *
 * @author Hiroyuki Wada
 */
public class EMUSeatSearchBuilder extends SeatSearchBuilder<EMUSeat> {

    EMUSeatSearchBuilder(GitHub root, GHEnterpriseExt enterprise) {
        super(root, enterprise, EMUSeatSearchResult.class);
    }

    private static class EMUSeatSearchResult extends SeatSearchResult<EMUSeat> {
    }

    @Override
    protected String getApiUrl() { return String.format("/enterprises/%s/copilot/billing/seats", enterprise.login); }
}
