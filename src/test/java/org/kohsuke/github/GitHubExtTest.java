package org.kohsuke.github;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GitHubExtTest {

    @Test
    void testGetUserBuildsRequestAndSetsRoot() throws IOException {
        Requester requesterMock = mock(Requester.class);
        GHUser userMock = new GHUser();

        when(requesterMock.withUrlPath(anyString())).thenReturn(requesterMock);
        when(requesterMock.fetch(GHUser.class)).thenReturn(userMock);

        GitHubExt gitHubExt = new TestableGitHubExt("https://api.github.com", HttpConnector.DEFAULT, requesterMock);
        GHUser result = gitHubExt.getUser(42L);

        verify(requesterMock).withUrlPath("/user/42");
        verify(requesterMock).fetch(GHUser.class);
        assertSame(userMock, result);
        assertSame(gitHubExt, result.root);
    }
}

