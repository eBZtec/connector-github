package org.kohsuke.github;

import jp.openstandia.connector.github.GitHubCopilotSeatHandler;
import jp.openstandia.connector.github.GitHubEMUConfiguration;
import jp.openstandia.connector.github.GitHubEMUSchema;
import jp.openstandia.connector.util.QueryHandler;
import jp.openstandia.connector.util.SchemaDefinition;
import org.identityconnectors.framework.common.objects.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class GitHubCopilotSeatHandlerTest {

    private GitHubEMUConfiguration config;
    private jp.openstandia.connector.github.GitHubClient<GitHubEMUSchema> client;

    @BeforeEach
    void setup() {
        config = mock(GitHubEMUConfiguration.class);
        client = mock(jp.openstandia.connector.github.GitHubClient.class);
    }

    @Test
    void createSchemaShouldMapCreateUpdateAndReadPathsIncludingNullBranches() throws Exception {
        SchemaDefinition sd =
                GitHubCopilotSeatHandler.createSchema(config, client)
                        .build();

        GitHubCopilotSeat dest = new GitHubCopilotSeat();
        ensureNested(dest);

        Set<Attribute> attrs = Set.of(
                AttributeBuilder.build(Name.NAME, "login1"),
                AttributeBuilder.build("last_activity_editor", "vscode"),
                AttributeBuilder.build("plan_type", "business"),
                AttributeBuilder.build("assignee.type", "User"),
                AttributeBuilder.build("assigning_team.slug", "team-a")
        );

        GitHubCopilotSeat created = sd.apply(attrs, dest);
        assertNotNull(created);

        GitHubCopilotSeat dest2 = new GitHubCopilotSeat();
        ensureNested(dest2);
        sd.apply(Set.of(AttributeBuilder.build("assigning_team.slug", (Object) null)), dest2);

        SCIMPatchOperations patch = mock(SCIMPatchOperations.class);
        sd.applyDelta(Set.of(
                AttributeDeltaBuilder.build(Name.NAME, "login2"),
                AttributeDeltaBuilder.build("last_activity_editor", "idea"),
                AttributeDeltaBuilder.build("plan_type", "enterprise"),
                AttributeDeltaBuilder.build("assignee.type", "Organization"),
                AttributeDeltaBuilder.build("assigning_team.slug", "team-b")
        ), patch);

        verify(patch).replace(eq("displayName"), eq("login2"));
        verify(patch).replace(eq("last_activity_editor"), eq("idea"));
        verify(patch).replace(eq("plan_type"), eq("enterprise"));
        verify(patch).replace(eq("assignee.type"), eq("Organization"));
        verify(patch).replace(eq("assigning_team.slug"), eq("team-b"));

        GitHubCopilotSeat seat = new GitHubCopilotSeat();
        ensureNested(seat);

        setNestedField(seat, "assignee", "id", "seat-id-1");
        setNestedField(seat, "assignee", "login", "login-read");
        setNestedField(seat, "assignee", "type", "User");

        setField(seat, "created_at", "2025-01-01T00:00:00Z");
        setField(seat, "last_authenticated_at", null);
        setField(seat, "updated_at", "2025-01-01T00:00:00Z");
        setField(seat, "last_activity_at", null);

        setField(seat, "pending_cancellation_date", "2025-10-24");

        setField(seat, "last_activity_editor", "vscode");
        setField(seat, "plan_type", "business");
        setNestedField(seat, "assigning_team", "slug", "team-read");

        Set<String> attrToGet = Set.of(
                "created_at",
                "updated_at",
                "pending_cancellation_date",
                "last_authenticated_at",
                "last_activity_at",
                "last_activity_editor",
                "plan_type",
                "assignee.type",
                "assigning_team.slug"
        );

        ConnectorObject co = sd.toConnectorObjectBuilder(seat, attrToGet, false).build();

        assertEquals("seat-id-1", co.getUid().getUidValue());
        assertEquals("login-read", co.getName().getNameValue());

        assertNotNull(co.getAttributeByName("created_at"));
        assertNotNull(co.getAttributeByName("updated_at"));
        assertNotNull(co.getAttributeByName("pending_cancellation_date"));

        assertNull(co.getAttributeByName("last_authenticated_at"));
        assertNull(co.getAttributeByName("last_activity_at"));

        assertEquals("vscode", co.getAttributeByName("last_activity_editor").getValue().get(0));
        assertEquals("business", co.getAttributeByName("plan_type").getValue().get(0));
        assertEquals("User", co.getAttributeByName("assignee.type").getValue().get(0));
        assertEquals("team-read", co.getAttributeByName("assigning_team.slug").getValue().get(0));
    }

    @Test
    void createUpdateDeltaDeleteShouldReturnDefaults() {
        SchemaDefinition sd =
                GitHubCopilotSeatHandler.createSchema(config, client)
                        .build();
        GitHubCopilotSeatHandler handler = new GitHubCopilotSeatHandler(
                mock(GitHubEMUConfiguration.class),
                client,
                mock(GitHubEMUSchema.class),
                sd
        );

        assertNull(handler.create(Set.of(AttributeBuilder.build(Name.NAME, "x"))));
        assertEquals(Set.of(), handler.updateDelta(new Uid("u"), Set.of(), null));
        handler.delete(new Uid("u"), null);
    }

    @Test
    void getByUidShouldReturn1WhenFoundElse0() throws Exception {
        SchemaDefinition sd =
                GitHubCopilotSeatHandler.createSchema(config, client)
                        .build();
        GitHubCopilotSeatHandler handler = new GitHubCopilotSeatHandler(
                mock(GitHubEMUConfiguration.class),
                client,
                mock(GitHubEMUSchema.class),
                sd
        );

        ResultsHandler rh = mock(ResultsHandler.class);
        when(rh.handle(any())).thenReturn(true);

        Uid uid = new Uid("seat-id-1");

        when(client.getCopilotSeat(eq(uid), any(), any())).thenReturn(null);
        assertEquals(0, handler.getByUid(uid, rh, null, null, null, false, 0, 0));

        GitHubCopilotSeat seat = new GitHubCopilotSeat();
        ensureNested(seat);
        setNestedField(seat, "assignee", "id", "seat-id-1");
        setNestedField(seat, "assignee", "login", "login1");

        when(client.getCopilotSeat(eq(uid), any(), any())).thenReturn(seat);
        assertEquals(1, handler.getByUid(uid, rh, null, Set.of(), null, false, 0, 0));

        verify(rh).handle(any(ConnectorObject.class));
    }

    @Test
    void getByNameShouldReturn1WhenFoundElse0() throws Exception {
        SchemaDefinition sd =
                GitHubCopilotSeatHandler.createSchema(config, client)
                        .build();
        GitHubCopilotSeatHandler handler = new GitHubCopilotSeatHandler(
                mock(GitHubEMUConfiguration.class),
                client,
                mock(GitHubEMUSchema.class),
                sd
        );

        ResultsHandler rh = mock(ResultsHandler.class);
        when(rh.handle(any())).thenReturn(true);

        Name name = new Name("login1");

        when(client.getCopilotSeat(eq(name), any(), any())).thenReturn(null);
        assertEquals(0, handler.getByName(name, rh, null, Set.of(), null, false, 0, 0));

        GitHubCopilotSeat seat = new GitHubCopilotSeat();
        ensureNested(seat);
        setNestedField(seat, "assignee", "id", "seat-id-1");
        setNestedField(seat, "assignee", "login", "login1");

        when(client.getCopilotSeat(eq(name), any(), any())).thenReturn(seat);
        assertEquals(1, handler.getByName(name, rh, null, Set.of(), null, false, 0, 0));

        verify(rh).handle(any(ConnectorObject.class));
    }

    @Test
    void getAllShouldDelegateToClientAndMapResults() throws Exception {
        SchemaDefinition sd =
                GitHubCopilotSeatHandler.createSchema(config, client)
                        .build();
        GitHubCopilotSeatHandler handler = new GitHubCopilotSeatHandler(
                mock(GitHubEMUConfiguration.class),
                client,
                mock(GitHubEMUSchema.class),
                sd
        );

        ResultsHandler rh = mock(ResultsHandler.class);
        when(rh.handle(any())).thenReturn(true);

        when(client.getCopilotSeats(any(), any(), any(), anyInt(), anyInt())).thenAnswer(inv -> {
            @SuppressWarnings("unchecked")
            QueryHandler<GitHubCopilotSeat> qh = (QueryHandler<GitHubCopilotSeat>) inv.getArgument(0);

            GitHubCopilotSeat seat = new GitHubCopilotSeat();
            ensureNested(seat);
            setNestedField(seat, "assignee", "id", "seat-id-1");
            setNestedField(seat, "assignee", "login", "login1");

            qh.handle(seat);
            return 1;
        });

        int count = handler.getAll(rh, null, Set.of(), null, false, 10, 0);

        assertEquals(1, count);
        verify(rh).handle(any(ConnectorObject.class));
    }

    @Test
    void getByMembersShouldReturn0FromDefaultImplementation() {
        SchemaDefinition sd =
                GitHubCopilotSeatHandler.createSchema(config, client)
                        .build();
        GitHubCopilotSeatHandler handler = new GitHubCopilotSeatHandler(
                mock(GitHubEMUConfiguration.class),
                client,
                mock(GitHubEMUSchema.class),
                sd
        );

        int count = handler.getByMembers(
                AttributeBuilder.build("members.User.value", "x"),
                mock(ResultsHandler.class),
                null, Set.of(), null, false, 10, 0
        );

        assertEquals(0, count);
    }

    @Test
    void queryShouldThrowUnsupportedOperationExceptionViaSuper() {
        SchemaDefinition sd =
                GitHubCopilotSeatHandler.createSchema(config, client)
                        .build();
        GitHubCopilotSeatHandler handler = new GitHubCopilotSeatHandler(
                mock(GitHubEMUConfiguration.class),
                client,
                mock(GitHubEMUSchema.class),
                sd
        );

        assertThrows(UnsupportedOperationException.class,
                () -> handler.query(null, mock(ResultsHandler.class), null));
    }

    @Test
    void toConnectorObjectShouldDelegateToDefaultImplementation() throws Exception {
        SchemaDefinition sd =
                GitHubCopilotSeatHandler.createSchema(config, client)
                        .build();
        GitHubCopilotSeatHandler handler = new GitHubCopilotSeatHandler(
                mock(GitHubEMUConfiguration.class),
                client,
                mock(GitHubEMUSchema.class),
                sd
        );

        GitHubCopilotSeat seat = new GitHubCopilotSeat();
        ensureNested(seat);
        setNestedField(seat, "assignee", "id", "seat-id-1");
        setNestedField(seat, "assignee", "login", "login1");

        ConnectorObject co = handler.toConnectorObject(handler.getSchemaDefinition(), seat, Set.of(), false);

        assertEquals("seat-id-1", co.getUid().getUidValue());
        assertEquals("login1", co.getName().getNameValue());
    }

    private static void ensureNested(GitHubCopilotSeat seat) throws Exception {
        ensureFieldObject(seat, "assignee");
        ensureFieldObject(seat, "assigning_team");
    }

    private static void ensureFieldObject(Object obj, String fieldName) throws Exception {
        Field f = findField(obj.getClass(), fieldName);
        f.setAccessible(true);
        Object current = f.get(obj);
        if (current == null) {
            Object nested = f.getType().getDeclaredConstructor().newInstance();
            f.set(obj, nested);
        }
    }

    private static void setNestedField(Object root, String nestedField, String innerField, Object value) throws Exception {
        Field nf = findField(root.getClass(), nestedField);
        nf.setAccessible(true);
        Object nested = nf.get(root);
        if (nested == null) {
            nested = nf.getType().getDeclaredConstructor().newInstance();
            nf.set(root, nested);
        }
        Field inner = findField(nested.getClass(), innerField);
        inner.setAccessible(true);
        inner.set(nested, value);
    }

    private static void setField(Object root, String field, Object value) throws Exception {
        Field f = findField(root.getClass(), field);
        f.setAccessible(true);
        f.set(root, value);
    }

    private static Field findField(Class<?> type, String name) throws NoSuchFieldException {
        Class<?> c = type;
        while (c != null) {
            try {
                return c.getDeclaredField(name);
            } catch (NoSuchFieldException ignored) {
                c = c.getSuperclass();
            }
        }
        throw new NoSuchFieldException(type.getName() + "#" + name);
    }
}
