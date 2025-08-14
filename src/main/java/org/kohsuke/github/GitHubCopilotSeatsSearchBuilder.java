package org.kohsuke.github;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Search for GitHub Copilot seats - Enterprise plan.
 *
 * @author Hiroyuki Wada
 * @author Nikolas Correa
 */

public class GitHubCopilotSeatsSearchBuilder extends GHQueryBuilder<GitHubCopilotSeat> {
    protected final Map<String, String> filter = new HashMap<>();

    private final Class<? extends GitHubCopilotSeatsSearchResult<GitHubCopilotSeat>> receiverType;

    protected final GHEnterpriseExt enterprise;

    GitHubCopilotSeatsSearchBuilder(GitHub root, GHEnterpriseExt enterprise) {
        super(root);
        this.enterprise = enterprise;
        this.receiverType = CopilotSeatSearchResult.class;

        req.withUrlPath(getApiUrl());
        req.withHeader(SCIMConstants.HEADER_ACCEPT, "application/json");
        req.withHeader(SCIMConstants.HEADER_API_VERSION, SCIMConstants.GITHUB_API_VERSION);
        req.rateLimit(RateLimitTarget.SEARCH);
    }

    public GitHubCopilotSeatsSearchBuilder eq(String key, String value) {
        filter.put(key, value);
        return this;
    }

    @Override
    public GitHubCopilotSeatPagedSearchIterable<GitHubCopilotSeat> list() {
        try {
            return new GitHubCopilotSeatPagedSearchIterable<>(root, req.build(), receiverType);
        } catch (MalformedURLException e) {
            throw new GHException("", e);
        }
    }

    protected String getApiUrl() {
        return String.format("/enterprises/%s/copilot/billing/seats", enterprise.login);
    }

    private static class CopilotSeatSearchResult extends GitHubCopilotSeatsSearchResult<GitHubCopilotSeat> {
    }
}
