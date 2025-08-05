package org.kohsuke.github;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class SeatSearchBuilder<T> extends GHQueryBuilder<T> {
    protected final Map<String, String> filter = new HashMap<>();

    private final Class<? extends SeatSearchResult<T>> receiverType;

    protected final GHEnterpriseExt enterprise;
    protected final GHOrganization organization;

    SeatSearchBuilder(GitHub root, GHEnterpriseExt enterprise, Class<? extends SeatSearchResult<T>> receiverType) {
        super(root);
        this.enterprise = enterprise;
        this.organization = null;
        this.receiverType = receiverType;
        req.withUrlPath(getApiUrl());
        req.withHeader(SCIMConstants.HEADER_ACCEPT, "application/json");
        req.withHeader(SCIMConstants.HEADER_API_VERSION, SCIMConstants.GITHUB_API_VERSION);
        req.rateLimit(RateLimitTarget.SEARCH);
    }

    public GHQueryBuilder<T> eq(String key, String value) {
        filter.put(key, value);
        return this;
    }

    @Override
    public SeatPagedSearchIterable<T> list() {
        try {
            return new SeatPagedSearchIterable<>(root, req.build(), receiverType);
        } catch (MalformedURLException e) {
            throw new GHException("", e);
        }
    }

    private String escape(String value) {
        return value.replace("\"", "\\\"");
    }

    protected abstract String getApiUrl();
}