package org.kohsuke.github;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GitHubCopilotSeatsSearchBuilderTest {

    private GitHub mockGitHub;
    private GHEnterpriseExt mockEnterprise;

    @BeforeEach
    void setup() {
        mockGitHub = mock(GitHub.class);
        mockEnterprise = mock(GHEnterpriseExt.class);
        when(mockEnterprise.getLogin()).thenReturn("test-enterprise");
        mockEnterprise.login = "test-enterprise";
    }

    @Test
    void testEqAddsFilter() {
        GitHubCopilotSeatsSearchBuilder builder = spy(new GitHubCopilotSeatsSearchBuilder(mockGitHub, mockEnterprise));

        builder.eq("team", "AI");

        Map<String, String> filters = builder.filter;
        assertEquals(1, filters.size());
        assertEquals("AI", filters.get("team"));
    }

    @Test
    void testGetApiUrl() {
        GitHubCopilotSeatsSearchBuilder builder = new GitHubCopilotSeatsSearchBuilder(mockGitHub, mockEnterprise);
        String url = builder.getApiUrl();
        assertEquals("/enterprises/test-enterprise/copilot/billing/seats", url);
    }

    @Test
    void testListReturnsPagedIterable() throws MalformedURLException {
        GitHubCopilotSeatsSearchBuilder builder = spy(new GitHubCopilotSeatsSearchBuilder(mockGitHub, mockEnterprise));

        GitHubRequest mockRequest = mock(GitHubRequest.class);
        doReturn(mockRequest).when(builder.req).build();

        GitHubCopilotSeatPagedSearchIterable<GitHubCopilotSeat> iterable = builder.list();

        assertNotNull(iterable);
        assertTrue(true);
    }

    @Test
    void testListThrowsGHExceptionOnMalformedURL() throws MalformedURLException {
        GitHubCopilotSeatsSearchBuilder builder = spy(new GitHubCopilotSeatsSearchBuilder(mockGitHub, mockEnterprise));

        doThrow(new MalformedURLException("bad url"))
                .when(builder.req)
                .build();

        GHException ex = assertThrows(GHException.class, builder::list);
        assertTrue(ex.getCause() instanceof MalformedURLException);
    }

    @Test
    void testConstructorSetsHeadersAndRateLimit() {
        GitHubCopilotSeatsSearchBuilder builder = new GitHubCopilotSeatsSearchBuilder(mockGitHub, mockEnterprise);

        assertNotNull(builder.enterprise);
        assertNotNull(builder.receiverType);
        assertTrue(builder.filter.isEmpty());
    }

    @Test
    void testListThrowsGHExceptionWhenMalformedUrl() throws Exception {
        GitHub github = GitHub.connectAnonymously();

        GHEnterpriseExt enterprise = mock(GHEnterpriseExt.class);
        when(enterprise.getLogin()).thenReturn("my-enterprise");

        GitHubCopilotSeatsSearchBuilder builder = new GitHubCopilotSeatsSearchBuilder(github, enterprise);
        Requester reqMock = mock(Requester.class);
        when(reqMock.build()).thenThrow(new MalformedURLException("URL malformada!"));

        java.lang.reflect.Field reqField = GHQueryBuilder.class.getDeclaredField("req");
        reqField.setAccessible(true);
        reqField.set(builder, reqMock);

        GHException thrown = assertThrows(GHException.class, builder::list);
        assertTrue(thrown.getCause() instanceof MalformedURLException);
        assertEquals("URL malformada!", thrown.getCause().getMessage());
    }

    @Test
    void testEqAddsFilterAndReturnsThis() throws Exception {
        GitHub github = GitHub.connectAnonymously();
        GHEnterpriseExt enterprise = mock(GHEnterpriseExt.class);
        when(enterprise.getLogin()).thenReturn("enterprise-login");

        GitHubCopilotSeatsSearchBuilder builder =
                new GitHubCopilotSeatsSearchBuilder(github, enterprise) {
                    @Override
                    protected String getApiUrl() {
                        return "/test/api/url";
                    }
                };

        GitHubCopilotSeatsSearchBuilder returned = builder.eq("status", "active");
        assertSame(builder, returned, "O método eq deve retornar a mesma instância (fluent API).");

        Map<String, String> filterMap = builder.filter;
        assertEquals(1, filterMap.size(), "Deve conter exatamente um elemento no filtro.");
        assertEquals("active", filterMap.get("status"), "O valor do filtro não corresponde ao esperado.");
    }
}