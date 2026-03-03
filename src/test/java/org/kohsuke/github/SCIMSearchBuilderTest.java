package org.kohsuke.github;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.MalformedURLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class SCIMSearchBuilderTest {

    @Mock
    private GitHub root;

    @Mock
    private GHOrganization organization;

    @Mock
    private GHEnterpriseExt enterprise;

    @Mock
    private Requester requester;

    @Mock
    private GitHubRequest gitHubRequest;

    @BeforeEach
    void setUp() {
        when(root.createRequest()).thenReturn(requester);
    }

    @Test
    void constructor_withOrganization_setsCorrectHeadersAndPath() {
        TestSCIMSearchBuilderOrg builder = new TestSCIMSearchBuilderOrg(root, organization);

        verify(requester).withUrlPath("/scim/test");
        verify(requester).withHeader(SCIMConstants.HEADER_ACCEPT, SCIMConstants.SCIM_ACCEPT);
        verify(requester).withHeader(SCIMConstants.HEADER_API_VERSION, SCIMConstants.GITHUB_API_VERSION);
        verify(requester).rateLimit(RateLimitTarget.SEARCH);
    }

    @Test
    void constructor_withEnterprise_setsCorrectHeadersAndPath() {
        TestSCIMSearchBuilderEnterprise builder = new TestSCIMSearchBuilderEnterprise(root, enterprise);

        verify(requester).withUrlPath("/scim/test");
        verify(requester).withHeader(SCIMConstants.HEADER_ACCEPT, SCIMConstants.SCIM_ACCEPT);
        verify(requester).withHeader(SCIMConstants.HEADER_API_VERSION, SCIMConstants.GITHUB_API_VERSION);
        verify(requester).rateLimit(RateLimitTarget.SEARCH);
    }

    @Test
    void list_emptyFilter_doesNotSetFilter() throws MalformedURLException {
        doReturn(gitHubRequest).when(requester).build();

        TestSCIMSearchBuilderOrg builder = new TestSCIMSearchBuilderOrg(root, organization);

        SCIMPagedSearchIterable<Object> iterable = builder.list();

        assertNotNull(iterable);
        verify(requester, never()).set(eq("filter"), anyString());
    }

    @Test
    void list_withOneFilter_setsFilterCorrectly() throws MalformedURLException {
        doReturn(gitHubRequest).when(requester).build();

        TestSCIMSearchBuilderOrg builder = new TestSCIMSearchBuilderOrg(root, organization);
        builder.eq("userName", "john.doe");

        SCIMPagedSearchIterable<Object> iterable = builder.list();

        assertNotNull(iterable);
        verify(requester).set("filter", "userName eq \"john.doe\"");
    }

    @Test
    void list_withMultipleFilters_joinsWithAnd() throws MalformedURLException {
        doReturn(gitHubRequest).when(requester).build();

        TestSCIMSearchBuilderOrg builder = new TestSCIMSearchBuilderOrg(root, organization);
        builder.eq("userName", "john.doe");
        builder.eq("active", "true");

        SCIMPagedSearchIterable<Object> iterable = builder.list();

        assertNotNull(iterable);

        verify(requester).set(eq("filter"), argThat((String str) ->
                str.contains("userName eq \"john.doe\"") &&
                        str.contains("active eq \"true\"") &&
                        str.contains(" and ")
        ));
    }

    @Test
    void list_escapesDoubleQuotes() throws MalformedURLException {
        doReturn(gitHubRequest).when(requester).build();

        TestSCIMSearchBuilderOrg builder = new TestSCIMSearchBuilderOrg(root, organization);
        builder.eq("displayName", "O'Reilly \"quoted\"");

        builder.list();

        verify(requester).set("filter", "displayName eq \"O'Reilly \\\"quoted\\\"\"");
    }

    @Test
    void list_whenBuildThrowsMalformedURLException_wrapsInGHException() throws MalformedURLException {
        doThrow(new MalformedURLException("bad url")).when(requester).build();

        TestSCIMSearchBuilderOrg builder = new TestSCIMSearchBuilderOrg(root, organization);

        GHException ex = assertThrows(GHException.class, builder::list);
        assertInstanceOf(MalformedURLException.class, ex.getCause());
    }

    private static class TestSCIMSearchBuilderOrg extends SCIMSearchBuilder<Object> {

        @SuppressWarnings("unchecked")
        TestSCIMSearchBuilderOrg(GitHub root, GHOrganization org) {
            super(root, org, (Class<? extends SCIMSearchResult<Object>>) (Class<?>) SCIMSearchResult.class);
        }

        @Override
        protected String getApiUrl() {
            return "/scim/test";
        }
    }

    private static class TestSCIMSearchBuilderEnterprise extends SCIMSearchBuilder<Object> {

        @SuppressWarnings("unchecked")
        TestSCIMSearchBuilderEnterprise(GitHub root, GHEnterpriseExt ent) {
            super(root, ent, (Class<? extends SCIMSearchResult<Object>>) (Class<?>) SCIMSearchResult.class);
        }

        @Override
        protected String getApiUrl() {
            return "/scim/test";
        }
    }
}