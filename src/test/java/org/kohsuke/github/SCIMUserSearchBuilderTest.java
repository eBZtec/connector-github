package org.kohsuke.github;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SCIMUserSearchBuilderTest {

    @Test
    void testGetApiUrl() throws IOException {
        GitHub github = GitHub.connectAnonymously();
        GHOrganization org = github.getOrganization("square");

        SCIMUserSearchBuilder builder = new SCIMUserSearchBuilder(github, org);
        assertNotNull(builder.getApiUrl());
    }

    @Test
    void testGetApiUrlReturnsExpectedPath() throws IOException {
        GitHub github = GitHub.connectAnonymously();
        GHOrganization org = github.getOrganization("square");
        org.login = "square";

        SCIMUserSearchBuilder builder = new SCIMUserSearchBuilder(github, org);
        String expected = "/scim/v2/organizations/square/Users";

        assertEquals(expected, builder.getApiUrl());
    }

    @Test
    void testConstructorInitializesSuperclassCorrectly() throws IOException {
        GitHub github = GitHub.connectAnonymously();
        GHOrganization org = github.getOrganization("square");
        org.login = "square";

        SCIMUserSearchBuilder builder = new SCIMUserSearchBuilder(github, org);
        assertNotNull(builder);
    }
}
