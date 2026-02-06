package org.kohsuke.github;

import org.jetbrains.annotations.NotNull;
import org.kohsuke.github.authorization.AuthorizationProvider;

import java.io.IOException;

class TestableGitHubExt extends GitHubExt {
    private final Requester requester;

    TestableGitHubExt(String apiUrl, HttpConnector connector, Requester requester) throws IOException {
        super(apiUrl, connector, RateLimitHandler.WAIT, AbuseLimitHandler.WAIT, new GitHubRateLimitChecker(), AuthorizationProvider.ANONYMOUS);
        this.requester = requester;
    }

    @NotNull
    @Override
    Requester createRequest() {
        return requester;
    }
}
