package org.kohsuke.github;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kohsuke.github.authorization.AuthorizationProvider;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
class GHEnterpriseExtTest {

    private GHEnterpriseExt enterprise;
    private Requester mockRequester;

    @BeforeEach
    void setup() {
        enterprise = spy(new GHEnterpriseExt());
        enterprise.login = "test-enterprise";

        GitHub mockGitHub = mock(GitHub.class);
        mockRequester = mock(Requester.class, RETURNS_SELF);
        enterprise.root = mockGitHub;

        when(mockGitHub.createRequest()).thenReturn(mockRequester);
    }

    // ==== SCIM USERS ====

    @Test
    void testCreateSCIMUser() throws Exception {
        SCIMEMUUser newUser = new SCIMEMUUser();
        SCIMEMUUser mockResponse = new SCIMEMUUser();
        mockResponse.id = "123";

        when(mockRequester.fetch(SCIMEMUUser.class)).thenReturn(mockResponse);

        SCIMEMUUser result = enterprise.createSCIMEMUUser(newUser);

        assertEquals("123", result.id);
        assertArrayEquals(new String[]{SCIMConstants.SCIM_USER_SCHEMA}, newUser.schemas);
        verify(mockRequester).method("POST");
        verify(mockRequester).withUrlPath(contains("/Users"));
    }

    @Test
    void testUpdateSCIMUser() throws Exception {
        SCIMPatchOperations ops = new SCIMPatchOperations();
        SCIMEMUUser mockResponse = new SCIMEMUUser();
        mockResponse.id = "456";

        when(mockRequester.fetch(SCIMEMUUser.class)).thenReturn(mockResponse);

        SCIMEMUUser result = enterprise.updateSCIMEMUUser("456", ops);

        assertEquals("456", result.id);
        verify(mockRequester).method("PATCH");
        verify(mockRequester).withUrlPath(contains("/Users/456"));
    }

    @Test
    void testGetSCIMUser() throws Exception {
        SCIMEMUUser mockResponse = new SCIMEMUUser();
        mockResponse.userName = "sample-user";

        when(mockRequester.fetch(SCIMEMUUser.class)).thenReturn(mockResponse);

        SCIMEMUUser result = enterprise.getSCIMEMUUser("42");
        assertEquals("sample-user", result.userName);
        verify(mockRequester).withUrlPath(contains("/Users/42"));
    }

    @Test
    void testGetSCIMUserByUserNameSingleResult() throws IOException {
        SCIMEMUUser user = new SCIMEMUUser();
        user.userName = "sample-user";

        SCIMEMUUserSearchBuilder mockBuilder = mock(SCIMEMUUserSearchBuilder.class);
        SCIMPagedSearchIterable<SCIMEMUUser> mockIterable = mock(SCIMPagedSearchIterable.class);

        when(mockIterable.toList()).thenReturn(Arrays.asList(user));
        when(mockBuilder.eq(anyString(), anyString())).thenReturn(mockBuilder);
        when(mockBuilder.list()).thenReturn(mockIterable);

        doReturn(mockBuilder).when(enterprise).searchSCIMUsers();

        SCIMEMUUser result = enterprise.getSCIMEMUUserByUserName("sample-user");
        assertEquals("sample-user", result.userName);
    }

    @Test
    void testGetSCIMUserByUserNameNoResult() throws IOException {
        SCIMEMUUserSearchBuilder mockBuilder = mock(SCIMEMUUserSearchBuilder.class);
        SCIMPagedSearchIterable<SCIMEMUUser> mockIterable = mock(SCIMPagedSearchIterable.class);

        when(mockIterable.toList()).thenReturn(Collections.emptyList());
        when(mockBuilder.eq(anyString(), anyString())).thenReturn(mockBuilder);
        when(mockBuilder.list()).thenReturn(mockIterable);

        doReturn(mockBuilder).when(enterprise).searchSCIMUsers();

        SCIMEMUUser result = enterprise.getSCIMEMUUserByUserName("unknown");
        assertNull(result);
    }

    @Test
    void testDeleteSCIMUser() throws Exception {
        enterprise.deleteSCIMUser("999");
        verify(mockRequester).method("DELETE");
        verify(mockRequester).withUrlPath(contains("/Users/999"));
        verify(mockRequester).send();
    }

    // ==== SCIM GROUPS ====

    @Test
    void testCreateSCIMGroup() throws Exception {
        SCIMEMUGroup newGroup = new SCIMEMUGroup();
        SCIMEMUGroup mockResponse = new SCIMEMUGroup();
        mockResponse.id = "group123";

        when(mockRequester.fetch(SCIMEMUGroup.class)).thenReturn(mockResponse);

        SCIMEMUGroup result = enterprise.createSCIMEMUGroup(newGroup);

        assertEquals("group123", result.id);
        assertArrayEquals(new String[]{SCIMConstants.SCIM_GROUP_SCHEMA}, newGroup.schemas);
        verify(mockRequester).method("POST");
        verify(mockRequester).withUrlPath(contains("/Groups"));
    }

    @Test
    void testUpdateSCIMGroup() throws Exception {
        SCIMPatchOperations ops = new SCIMPatchOperations();
        SCIMEMUGroup mockResponse = new SCIMEMUGroup();
        mockResponse.id = "group456";

        when(mockRequester.fetch(SCIMEMUGroup.class)).thenReturn(mockResponse);

        SCIMEMUGroup result = enterprise.updateSCIMEMUGroup("group456", ops);

        assertEquals("group456", result.id);
        verify(mockRequester).method("PATCH");
        verify(mockRequester).withUrlPath(contains("/Groups/group456"));
    }

    @Test
    void testGetSCIMGroup() throws Exception {
        SCIMEMUGroup mockResponse = new SCIMEMUGroup();
        mockResponse.displayName = "QA Team";

        when(mockRequester.fetch(SCIMEMUGroup.class)).thenReturn(mockResponse);

        SCIMEMUGroup result = enterprise.getSCIMEMUGroup("group999");

        assertEquals("QA Team", result.displayName);
        verify(mockRequester).withUrlPath(contains("/Groups/group999"));
    }

    @Test
    void testDeleteSCIMGroup() throws Exception {
        enterprise.deleteSCIMGroup("group888");

        verify(mockRequester).method("DELETE");
        verify(mockRequester).withUrlPath(contains("/Groups/group888"));
        verify(mockRequester).send();
    }

    // ==== COPILOT SEATS ====

    @Test
    void testGetCopilotSeatByDisplayName() throws Exception {
        GitHubCopilotSeat seat = new GitHubCopilotSeat();
        seat.assignee = new GitHubCopilotSeatAssignee();
        seat.assignee.login = "sample-user";

        GitHubCopilotSeatsSearchBuilder mockBuilder = mock(GitHubCopilotSeatsSearchBuilder.class);
        GitHubCopilotSeatPagedSearchIterable<GitHubCopilotSeat> mockIterable = mock(GitHubCopilotSeatPagedSearchIterable.class);
        when(mockIterable.toList()).thenReturn(List.of(seat));
        when(mockBuilder.list()).thenReturn(mockIterable);

        doReturn(mockBuilder).when(enterprise).searchCopilotSeats();

        GitHubCopilotSeat result = enterprise.getCopilotSeatByDisplayName("sample-user");

        assertNotNull(result);
        assertEquals("sample-user", result.assignee.login);
    }

    @Test
    void testGetCopilotSeatByDisplayNameMultipleResultsReturnsNull() throws IOException {
        GitHubCopilotSeat seat1 = new GitHubCopilotSeat();
        seat1.assignee = new GitHubCopilotSeatAssignee();
        seat1.assignee.login = "user1";
        GitHubCopilotSeat seat2 = new GitHubCopilotSeat();
        seat2.assignee = new GitHubCopilotSeatAssignee();
        seat2.assignee.login = "user2";

        GitHubCopilotSeatsSearchBuilder mockBuilder = mock(GitHubCopilotSeatsSearchBuilder.class);
        GitHubCopilotSeatPagedSearchIterable<GitHubCopilotSeat> mockIterable = mock(GitHubCopilotSeatPagedSearchIterable.class);
        when(mockIterable.toList()).thenReturn(Arrays.asList(seat1, seat2));
        when(mockBuilder.list()).thenReturn(mockIterable);

        doReturn(mockBuilder).when(enterprise).searchCopilotSeats();

        GitHubCopilotSeat result = enterprise.getCopilotSeatByDisplayName("user3");
        assertNull(result);
    }

    @Test
    void testGetCopilotSeatByUid() throws Exception {
        GitHubCopilotSeat seat = new GitHubCopilotSeat();
        seat.assignee = new GitHubCopilotSeatAssignee();
        seat.assignee.id = "12345";

        GitHubCopilotSeatsSearchBuilder mockBuilder = mock(GitHubCopilotSeatsSearchBuilder.class);
        GitHubCopilotSeatPagedSearchIterable<GitHubCopilotSeat> mockIterable = mock(GitHubCopilotSeatPagedSearchIterable.class);
        when(mockIterable.toList()).thenReturn(Arrays.asList(seat));
        when(mockBuilder.list()).thenReturn(mockIterable);

        doReturn(mockBuilder).when(enterprise).searchCopilotSeats();

        GitHubCopilotSeat result = enterprise.getCopilotSeatByUid("12345");

        assertNotNull(result);
        assertEquals("12345", result.assignee.id);
    }

    @Test
    void testGetCopilotSeatByUidNoMatchReturnsNull() throws IOException {
        GitHubCopilotSeatsSearchBuilder mockBuilder = mock(GitHubCopilotSeatsSearchBuilder.class);
        GitHubCopilotSeatPagedSearchIterable<GitHubCopilotSeat> mockIterable = mock(GitHubCopilotSeatPagedSearchIterable.class);
        when(mockIterable.toList()).thenReturn(Collections.emptyList());
        when(mockBuilder.list()).thenReturn(mockIterable);
        doReturn(mockBuilder).when(enterprise).searchCopilotSeats();

        GitHubCopilotSeat result = enterprise.getCopilotSeatByUid("9999");
        assertNull(result);
    }

    // ==== PAGINATION HELPERS ====

    @Test
    void testSearchAndListHelpers() throws Exception {
        assertNotNull(enterprise.searchSCIMUsers());
        assertNotNull(enterprise.searchSCIMGroups());
        assertNotNull(enterprise.searchCopilotSeats());

        SCIMPagedSearchIterable<SCIMEMUUser> mockPaged = mock(SCIMPagedSearchIterable.class);
        SCIMEMUUserSearchBuilder mockBuilder = mock(SCIMEMUUserSearchBuilder.class);

        when(mockBuilder.list()).thenReturn(mockPaged);
        when(mockPaged.withPageSize(anyInt())).thenReturn(mockPaged);
        when(mockPaged.withPageOffset(anyInt())).thenReturn(mockPaged);

        doReturn(mockBuilder).when(enterprise).searchSCIMUsers();

        SCIMPagedSearchIterable<SCIMEMUUser> result = enterprise.listSCIMUsers(10, 0);
        assertNotNull(result);
        verify(mockPaged).withPageSize(10);
        verify(mockPaged).withPageOffset(0);
    }

    @Test
    void testListSCIMGroupsAndAllSeats() throws IOException {
        SCIMPagedSearchIterable<SCIMEMUGroup> mockGroupPaged = mock(SCIMPagedSearchIterable.class);
        SCIMEMUGroupSearchBuilder mockGroupBuilder = mock(SCIMEMUGroupSearchBuilder.class);
        when(mockGroupBuilder.list()).thenReturn(mockGroupPaged);
        when(mockGroupPaged.withPageSize(anyInt())).thenReturn(mockGroupPaged);
        when(mockGroupPaged.withPageOffset(anyInt())).thenReturn(mockGroupPaged);
        doReturn(mockGroupBuilder).when(enterprise).searchSCIMGroups();

        GitHubCopilotSeatPagedSearchIterable<GitHubCopilotSeat> mockSeatPaged = mock(GitHubCopilotSeatPagedSearchIterable.class);
        GitHubCopilotSeatsSearchBuilder mockSeatBuilder = mock(GitHubCopilotSeatsSearchBuilder.class);
        when(mockSeatBuilder.list()).thenReturn(mockSeatPaged);
        when(mockSeatPaged.withPageSize(anyInt())).thenReturn(mockSeatPaged);
        when(mockSeatPaged.withPageOffset(anyInt())).thenReturn(mockSeatPaged);
        doReturn(mockSeatBuilder).when(enterprise).searchCopilotSeats();

        assertNotNull(enterprise.listSCIMGroups(20, 1));
        assertNotNull(enterprise.listAllSeats(50, 2));

        verify(mockGroupPaged).withPageSize(20);
        verify(mockSeatPaged).withPageSize(50);
    }

    // ==== WRAPUP ====

    @Test
    void testWrapUp() {
        GitHub mockRoot = mock(GitHub.class);
        GHEnterpriseExt result = enterprise.wrapUp(mockRoot);
        assertNotNull(result);
        assertTrue(true);
    }

    @Test
    void testGetSCIMGroupByDisplayNameSingleResult() throws IOException {
        SCIMEMUGroup group = new SCIMEMUGroup();
        group.displayName = "Dev Team";

        SCIMEMUGroupSearchBuilder mockBuilder = mock(SCIMEMUGroupSearchBuilder.class);
        SCIMPagedSearchIterable<SCIMEMUGroup> mockIterable = mock(SCIMPagedSearchIterable.class);

        when(mockIterable.toList()).thenReturn(List.of(group));
        when(mockBuilder.eq(anyString(), anyString())).thenReturn(mockBuilder);
        when(mockBuilder.list()).thenReturn(mockIterable);

        doReturn(mockBuilder).when(enterprise).searchSCIMGroups();

        SCIMEMUGroup result = enterprise.getSCIMEMUGroupByDisplayName("Dev Team");
        assertNotNull(result);
        assertEquals("Dev Team", result.displayName);
    }

    @Test
    void testGetSCIMGroupByDisplayNameNoOrMultipleResultsReturnsNull() throws IOException {
        SCIMEMUGroupSearchBuilder mockBuilder = mock(SCIMEMUGroupSearchBuilder.class);
        SCIMPagedSearchIterable<SCIMEMUGroup> mockIterable = mock(SCIMPagedSearchIterable.class);
        when(mockIterable.toList()).thenReturn(Collections.emptyList());
        when(mockBuilder.eq(anyString(), anyString())).thenReturn(mockBuilder);
        when(mockBuilder.list()).thenReturn(mockIterable);
        doReturn(mockBuilder).when(enterprise).searchSCIMGroups();

        SCIMEMUGroup result1 = enterprise.getSCIMEMUGroupByDisplayName("Unknown");
        assertNull(result1);

        SCIMEMUGroup g1 = new SCIMEMUGroup();
        SCIMEMUGroup g2 = new SCIMEMUGroup();
        when(mockIterable.toList()).thenReturn(Arrays.asList(g1, g2));

        SCIMEMUGroup result2 = enterprise.getSCIMEMUGroupByDisplayName("DuplicatedGroup");
        assertNull(result2);
    }

}
