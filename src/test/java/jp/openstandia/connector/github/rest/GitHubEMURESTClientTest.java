package jp.openstandia.connector.github.rest;

import jp.openstandia.connector.github.GitHubEMUConfiguration;
import jp.openstandia.connector.github.GitHubEMUSchema;
import org.kohsuke.github.TestSCIMPagedSearchIterable;
import jp.openstandia.connector.util.QueryHandler;
import org.identityconnectors.framework.common.exceptions.*;
import org.identityconnectors.framework.common.objects.Name;
import org.identityconnectors.framework.common.objects.OperationOptions;
import org.identityconnectors.framework.common.objects.Uid;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kohsuke.github.*;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GitHubEMURESTClientTest {

    @Mock GitHubEMUConfiguration configuration;

    @Mock
    GitHubExt apiClient;
    @Mock
    GHEnterpriseExt enterprise;

    @Mock OperationOptions options;

    // ---------- testable client (constructor-safe) ----------
    static class TestableClient extends GitHubEMURESTClient {
        static final AtomicInteger authCalls = new AtomicInteger(0);

        TestableClient(GitHubEMUConfiguration configuration) {
            super(configuration);
        }

        @Override
        public void auth() {
            //TestableClient.authCalls.set(0);
            // No network; just count calls
            authCalls.incrementAndGet();
        }
    }

    private TestableClient client;

    @BeforeEach
    void setUp() throws Exception {
        // Avoid NPE if something accidentally calls configuration in overridden auth()
        //when(configuration.getEnterpriseSlug()).thenReturn("ent");

        client = new TestableClient(configuration);

        // inject mocks (apiClient is private -> reflection)
        setPrivateField(client, "apiClient", apiClient);

        // enterpriseApiClient is package-private -> direct access
        client.enterpriseApiClient = enterprise;
    }

    // ---------- reflection helpers ----------
    private static void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field f = target.getClass().getSuperclass().getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(target, value);
    }

    private static void setPrivateLong(Object target, String fieldName, long value) throws Exception {
        Field f = target.getClass().getSuperclass().getDeclaredField(fieldName);
        f.setAccessible(true);
        f.setLong(target, value);
    }

    // =======================================================
    // setInstanceName
    // =======================================================
    @Test
    void setInstanceName_setsValue() {
        client.setInstanceName("myInstance");
        // no getter; just validate no exception and used in logging paths
        assertDoesNotThrow(() -> client.setInstanceName("another"));
    }

    // =======================================================
    // test()
    // =======================================================
    @Test
    void test_success_callsApiUrlValidity() throws Exception {
        setPrivateLong(client, "lastAuthenticated", 0L); // prevent withAuth calling auth()

        client.test();

        verify(apiClient, times(1)).checkApiUrlValidity();
    }

    @Test
    void test_whenRuntimeException_wrapsConnectorException() throws Exception {
        setPrivateLong(client, "lastAuthenticated", 0L);

        doThrow(new RuntimeException("boom")).when(apiClient).checkApiUrlValidity();

        ConnectorException ex = assertThrows(ConnectorException.class, () -> client.test());
        assertTrue(ex.getMessage().contains("isn't active"));
    }

    // =======================================================
    // auth()
    // =======================================================
    @Test
    void auth_isOverridden_noNetwork_calledByCtorAtLeastOnce() {
        // Constructor calls auth(); our override increments counter
        assertTrue(client.authCalls.get() >= 1);
    }

//    // =======================================================
//    // handleApiException(Exception)
//    // =======================================================
    @Test
    void handleApiException_400_mapsToInvalidAttributeValueException() {
        Exception e = ghFileNotFoundWithStatus("HTTP/1.1 400 Bad Request");
        ConnectorException mapped = client.handleApiException(e);

        assertTrue(mapped instanceof InvalidAttributeValueException);
    }

    @Test
    void handleApiException_401_mapsToConnectionFailedUnauthorized() {
        Exception e = ghFileNotFoundWithStatus("HTTP/1.1 401 Unauthorized");
        ConnectorException mapped = client.handleApiException(e);
        assertTrue(mapped instanceof ConnectionFailedException); // your UnauthorizedException extends ConnectionFailedException
    }

    @Test
    void handleApiException_403_mapsToPermissionDenied() {
        Exception e = ghFileNotFoundWithStatus("HTTP/1.1 403 Forbidden");
        ConnectorException mapped = client.handleApiException(e);
        assertTrue(mapped instanceof PermissionDeniedException);
    }

    @Test
    void handleApiException_404_mapsToUnknownUid() {
        Exception e = ghFileNotFoundWithStatus("HTTP/1.1 404 Not Found");
        ConnectorException mapped = client.handleApiException(e);
        assertTrue(mapped instanceof UnknownUidException);
    }

    @Test
    void handleApiException_409_mapsToAlreadyExists() {
        Exception e = ghFileNotFoundWithStatus("HTTP/1.1 409 Conflict");
        ConnectorException mapped = client.handleApiException(e);
        assertTrue(mapped instanceof AlreadyExistsException);
    }

    @Test
    void handleApiException_429_mapsToRetryable() {
        Exception e = ghFileNotFoundWithStatus("HTTP/1.1 429 Too Many Requests");
        ConnectorException mapped = client.handleApiException(e);
        assertTrue(mapped instanceof RetryableException);
    }

    @Test
    void handleApiException_otherException_mapsToConnectorIOException() {
        ConnectorException mapped = client.handleApiException(new RuntimeException("x"));
        assertTrue(mapped instanceof ConnectorIOException);
    }

    private GHFileNotFoundException ghFileNotFoundWithStatus(String statusLine) {
        GHFileNotFoundException ex = mock(GHFileNotFoundException.class);
        Map<String, List<String>> headers = new HashMap<>();
        headers.put(null, List.of(statusLine)); // code reads get(null)
        when(ex.getResponseHeaderFields()).thenReturn(headers);
        return ex;
    }

    // =======================================================
    // withAuth(Callable<T>)
    // =======================================================
    @Test
    void withAuth_whenLastAuthenticatedNonZero_callsAuth() throws Exception {
        setPrivateLong(client, "lastAuthenticated", 123L);

        String out = client.withAuth(() -> "ok");

        assertEquals("ok", out);
        assertTrue(client.authCalls.get() >= 2); // ctor auth + this auth
    }

    @Test
    void withAuth_whenCallableThrows_mapsAndThrowsConnectorException() throws Exception {
        setPrivateLong(client, "lastAuthenticated", 0L);

        Exception e = ghFileNotFoundWithStatus("HTTP/1.1 404 Not Found");

        assertThrows(UnknownUidException.class, () ->
                client.withAuth(() -> { throw e; })
        );
    }

    // =======================================================
    // createEMUUser
    // =======================================================
    @Test
    void createEMUUser_returnsUidWithName() throws Exception {
        setPrivateLong(client, "lastAuthenticated", 0L);

        SCIMEMUUser created = new SCIMEMUUser();
        created.id = "123";
        created.userName = "jdoe";

        when(enterprise.createSCIMEMUUser(any())).thenReturn(created);

        Uid uid = client.createEMUUser(new SCIMEMUUser());

        assertEquals("123", uid.getUidValue());
        assertEquals("jdoe", uid.getNameHintValue());
        verify(enterprise).createSCIMEMUUser(any(SCIMEMUUser.class));
    }

    // =======================================================
    // patchEMUUser
    // =======================================================
    @Test
    void patchEMUUser_callsUpdate() throws Exception {
        setPrivateLong(client, "lastAuthenticated", 0L);

        SCIMPatchOperations ops = new SCIMPatchOperations();
        when(enterprise.updateSCIMEMUUser(eq("u1"), eq(ops))).thenReturn(new SCIMEMUUser());

        client.patchEMUUser(new Uid("u1"), ops);

        verify(enterprise).updateSCIMEMUUser("u1", ops);
    }

    // =======================================================
    // deleteEMUUser
    // =======================================================
    @Test
    void deleteEMUUser_callsDelete() throws Exception {
        setPrivateLong(client, "lastAuthenticated", 0L);

        client.deleteEMUUser(new Uid("u1"), options);

        verify(enterprise).deleteSCIMUser("u1");
    }

    // =======================================================
    // getEMUUser(Uid)
    // =======================================================
    @Test
    void getEMUUser_byUid_callsGet() throws Exception {
        setPrivateLong(client, "lastAuthenticated", 0L);

        SCIMEMUUser user = new SCIMEMUUser();
        user.id = "u1";
        when(enterprise.getSCIMEMUUser("u1")).thenReturn(user);

        SCIMEMUUser out = client.getEMUUser(new Uid("u1"), options, Set.of());

        assertSame(user, out);
        verify(enterprise).getSCIMEMUUser("u1");
    }

    // =======================================================
    // getEMUUser(Name)
    // =======================================================
    @Test
    void getEMUUser_byName_callsGetByUserName() throws Exception {
        setPrivateLong(client, "lastAuthenticated", 0L);

        SCIMEMUUser user = new SCIMEMUUser();
        user.userName = "jdoe";
        when(enterprise.getSCIMEMUUserByUserName("jdoe")).thenReturn(user);

        SCIMEMUUser out = client.getEMUUser(new Name("jdoe"), options, Set.of());

        assertSame(user, out);
        verify(enterprise).getSCIMEMUUserByUserName("jdoe");
    }

    // =======================================================
    // getEMUUsers
    // =======================================================
    @Test
    void getEMUUsers_noOffset_iteratesAllUntilHandlerFalse_returnsTotal() throws Exception {
        setPrivateLong(client, "lastAuthenticated", 0L);

        SCIMEMUUser u1 = new SCIMEMUUser(); u1.id = "1";
        SCIMEMUUser u2 = new SCIMEMUUser(); u2.id = "2";

        SCIMPagedSearchIterable<SCIMEMUUser> iterable = new TestSCIMPagedSearchIterable<>(List.of(u1, u2), 77);
        when(enterprise.listSCIMUsers(10, 0)).thenReturn(iterable);

        @SuppressWarnings("unchecked")
        QueryHandler<SCIMEMUUser> handler = mock(QueryHandler.class);
        when(handler.handle(u1)).thenReturn(true);
        when(handler.handle(u2)).thenReturn(false); // stop early

        int total = client.getEMUUsers(handler, options, Set.of(), 10, 0);

        assertEquals(77, total);
        verify(handler).handle(u1);
        verify(handler).handle(u2);
    }

    @Test
    void getEMUUsers_withOffset_paginatesAndStopsAtPageSize_returnsTotal() throws Exception {
        setPrivateLong(client, "lastAuthenticated", 0L);

        SCIMEMUUser u1 = new SCIMEMUUser(); u1.id = "1";
        SCIMEMUUser u2 = new SCIMEMUUser(); u2.id = "2";
        SCIMEMUUser u3 = new SCIMEMUUser(); u3.id = "3";

        SCIMPagedSearchIterable<SCIMEMUUser> iterable = new TestSCIMPagedSearchIterable<>(List.of(u1, u2, u3), 99);
        when(enterprise.listSCIMUsers(2, 1)).thenReturn(iterable);

        @SuppressWarnings("unchecked")
        QueryHandler<SCIMEMUUser> handler = mock(QueryHandler.class);
        when(handler.handle(any())).thenReturn(true);

        int total = client.getEMUUsers(handler, options, Set.of(), 2, 1);

        assertEquals(99, total);
        verify(handler, times(2)).handle(any()); // stops at pageSize=2
    }

    private static <T> org.kohsuke.github.PagedIterator<T> pagedIteratorOf(List<T> items) {
        Iterator<T> it = items.iterator();

        @SuppressWarnings("unchecked")
        org.kohsuke.github.PagedIterator<T> pit = mock(org.kohsuke.github.PagedIterator.class);

        when(pit.hasNext()).thenAnswer(inv -> it.hasNext());
        when(pit.next()).thenAnswer(inv -> it.next());

        return pit;
    }



    // helper for SCIMPagedSearchIterable
    @SuppressWarnings("unchecked")
    private static <T> SCIMPagedSearchIterable<T> mockPagedIterable(List<T> items, int totalCount) {
        SCIMPagedSearchIterable<T> iterable = mock(SCIMPagedSearchIterable.class);
        when(iterable.iterator()).thenReturn(pagedIteratorOf(items));
        when(iterable.getTotalCount()).thenReturn(totalCount);
        return iterable;
    }

    // =======================================================
    // createEMUGroup
    // =======================================================
    @Test
    void createEMUGroup_returnsUidWithName() throws Exception {
        setPrivateLong(client, "lastAuthenticated", 0L);

        SCIMEMUGroup created = new SCIMEMUGroup();
        created.id = "g1";
        created.displayName = "devs";

        when(enterprise.createSCIMEMUGroup(any())).thenReturn(created);

        Uid uid = client.createEMUGroup(mock(GitHubEMUSchema.class), new SCIMEMUGroup());

        assertEquals("g1", uid.getUidValue());
        assertEquals("devs", uid.getNameHintValue());
        verify(enterprise).createSCIMEMUGroup(any(SCIMEMUGroup.class));
    }

    // =======================================================
    // patchEMUGroup
    // =======================================================
    @Test
    void patchEMUGroup_callsUpdate() throws Exception {
        setPrivateLong(client, "lastAuthenticated", 0L);

        SCIMPatchOperations ops = new SCIMPatchOperations();
        when(enterprise.updateSCIMEMUGroup(eq("g1"), eq(ops))).thenReturn(new SCIMEMUGroup());

        client.patchEMUGroup(new Uid("g1"), ops);

        verify(enterprise).updateSCIMEMUGroup("g1", ops);
    }

    // =======================================================
    // deleteEMUGroup
    // =======================================================
    @Test
    void deleteEMUGroup_callsDelete() throws Exception {
        setPrivateLong(client, "lastAuthenticated", 0L);

        client.deleteEMUGroup(new Uid("g1"), options);

        verify(enterprise).deleteSCIMGroup("g1");
    }

    // =======================================================
    // getEMUGroup(Uid)
    // =======================================================
    @Test
    void getEMUGroup_byUid_callsGet() throws Exception {
        setPrivateLong(client, "lastAuthenticated", 0L);

        SCIMEMUGroup g = new SCIMEMUGroup(); g.id = "g1";
        when(enterprise.getSCIMEMUGroup("g1")).thenReturn(g);

        SCIMEMUGroup out = client.getEMUGroup(new Uid("g1"), options, Set.of());

        assertSame(g, out);
        verify(enterprise).getSCIMEMUGroup("g1");
    }

    // =======================================================
    // getEMUGroup(Name)
    // =======================================================
    @Test
    void getEMUGroup_byName_callsGetByDisplayName() throws Exception {
        setPrivateLong(client, "lastAuthenticated", 0L);

        SCIMEMUGroup g = new SCIMEMUGroup(); g.displayName = "devs";
        when(enterprise.getSCIMEMUGroupByDisplayName("devs")).thenReturn(g);

        SCIMEMUGroup out = client.getEMUGroup(new Name("devs"), options, Set.of());

        assertSame(g, out);
        verify(enterprise).getSCIMEMUGroupByDisplayName("devs");
    }

    // =======================================================
    // getCopilotSeat(Uid)
    // =======================================================
    @Test
    void getCopilotSeat_byUid_callsGetByUid() throws Exception {
        setPrivateLong(client, "lastAuthenticated", 0L);

        GitHubCopilotSeat seat = new GitHubCopilotSeat();
        when(enterprise.getCopilotSeatByUid("u1")).thenReturn(seat);

        GitHubCopilotSeat out = client.getCopilotSeat(new Uid("u1"), options, Set.of());

        assertSame(seat, out);
        verify(enterprise).getCopilotSeatByUid("u1");
    }

    // =======================================================
    // getCopilotSeat(Name)
    // =======================================================
    @Test
    void getCopilotSeat_byName_callsGetByDisplayName() throws Exception {
        setPrivateLong(client, "lastAuthenticated", 0L);

        GitHubCopilotSeat seat = new GitHubCopilotSeat();
        when(enterprise.getCopilotSeatByDisplayName("Jane Doe")).thenReturn(seat);

        GitHubCopilotSeat out = client.getCopilotSeat(new Name("Jane Doe"), options, Set.of());

        assertSame(seat, out);
        verify(enterprise).getCopilotSeatByDisplayName("Jane Doe");
    }

    // =======================================================
    // getCopilotSeats
    // =======================================================
    @Test
    void getCopilotSeats_noOffset_iteratesAllUntilHandlerFalse_returnsTotalSeats() throws Exception {
        setPrivateLong(client, "lastAuthenticated", 0L);

        GitHubCopilotSeat s1 = new GitHubCopilotSeat();
        GitHubCopilotSeat s2 = new GitHubCopilotSeat();

        GitHubCopilotSeatPagedSearchIterable<GitHubCopilotSeat> iterable =
                new TestGitHubCopilotSeatPagedSearchIterable<>(List.of(s1, s2), 50);

        when(enterprise.listAllSeats(10, 0)).thenReturn(iterable);

        @SuppressWarnings("unchecked")
        QueryHandler<GitHubCopilotSeat> handler = mock(QueryHandler.class);
        when(handler.handle(s1)).thenReturn(true);
        when(handler.handle(s2)).thenReturn(false);

        int total = client.getCopilotSeats(handler, options, Set.of(), 10, 0);

        assertEquals(50, total);
        verify(handler).handle(s1);
        verify(handler).handle(s2);
    }

    @Test
    void getCopilotSeats_withOffset_stopsAtPageSize_returnsTotalSeats() throws Exception {
        setPrivateLong(client, "lastAuthenticated", 0L);

        GitHubCopilotSeat s1 = new GitHubCopilotSeat();
        GitHubCopilotSeat s2 = new GitHubCopilotSeat();
        GitHubCopilotSeat s3 = new GitHubCopilotSeat();

        GitHubCopilotSeatPagedSearchIterable<GitHubCopilotSeat> iterable =
                new TestGitHubCopilotSeatPagedSearchIterable<>(List.of(s1, s2, s3), 123);

        when(enterprise.listAllSeats(2, 1)).thenReturn(iterable);

        @SuppressWarnings("unchecked")
        QueryHandler<GitHubCopilotSeat> handler = mock(QueryHandler.class);
        when(handler.handle(any())).thenReturn(true);

        int total = client.getCopilotSeats(handler, options, Set.of(), 2, 1);

        assertEquals(123, total);
        verify(handler, times(2)).handle(any());
    }
//
//    private static <T> GitHubCopilotSeatPagedSearchIterable<T> mockSeatIterable(List<T> items, int totalSeats) {
//        @SuppressWarnings("unchecked")
//        GitHubCopilotSeatPagedSearchIterable<T> iterable = mock(GitHubCopilotSeatPagedSearchIterable.class);
//        when(iterable.iterator()).thenReturn((PagedIterator<T>) items.iterator());
//        when(iterable.getTotalSeats()).thenReturn(totalSeats);
//        return iterable;
//    }
//
//    // =======================================================
//    // getEMUGroups
//    // =======================================================
    @Test
    void getEMUGroups_noOffset_iteratesAllUntilHandlerFalse_returnsTotal() throws Exception {
        setPrivateLong(client, "lastAuthenticated", 0L);

        SCIMEMUGroup g1 = new SCIMEMUGroup(); g1.id = "1";
        SCIMEMUGroup g2 = new SCIMEMUGroup(); g2.id = "2";

        SCIMPagedSearchIterable<SCIMEMUGroup> iterable = new TestSCIMPagedSearchIterable<>(List.of(g1, g2), 88);
        when(enterprise.listSCIMGroups(10, 0)).thenReturn(iterable);

        @SuppressWarnings("unchecked")
        QueryHandler<SCIMEMUGroup> handler = mock(QueryHandler.class);
        when(handler.handle(g1)).thenReturn(true);
        when(handler.handle(g2)).thenReturn(false);

        int total = client.getEMUGroups(handler, options, Set.of(), 10, 0);

        assertEquals(88, total);
        verify(handler).handle(g1);
        verify(handler).handle(g2);
    }

    @Test
    void getEMUGroups_withOffset_stopsAtPageSize_returnsTotal() throws Exception {
        setPrivateLong(client, "lastAuthenticated", 0L);

        SCIMEMUGroup g1 = new SCIMEMUGroup(); g1.id = "1";
        SCIMEMUGroup g2 = new SCIMEMUGroup(); g2.id = "2";
        SCIMEMUGroup g3 = new SCIMEMUGroup(); g3.id = "3";

        SCIMPagedSearchIterable<SCIMEMUGroup> iterable = new TestSCIMPagedSearchIterable<>(List.of(g1, g2, g3), 200);
        when(enterprise.listSCIMGroups(2, 1)).thenReturn(iterable);

        @SuppressWarnings("unchecked")
        QueryHandler<SCIMEMUGroup> handler = mock(QueryHandler.class);
        when(handler.handle(any())).thenReturn(true);

        int total = client.getEMUGroups(handler, options, Set.of(), 2, 1);

        assertEquals(200, total);
        verify(handler, times(2)).handle(any());
    }

    // =======================================================
    // close()
    // =======================================================
    @Test
    void close_doesNothing() {
        assertDoesNotThrow(() -> client.close());
    }
}
