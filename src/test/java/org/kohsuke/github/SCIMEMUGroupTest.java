package org.kohsuke.github;

import com.fasterxml.jackson.databind.ObjectMapper;
import jp.openstandia.connector.github.GitHubEMUConfiguration;
import jp.openstandia.connector.github.GitHubEMUGroupHandler;
import jp.openstandia.connector.github.GitHubEMUSchema;
import jp.openstandia.connector.util.ObjectHandler;
import jp.openstandia.connector.util.SchemaDefinition;
import org.identityconnectors.framework.common.objects.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class SCIMEMUGroupTest {

    private GitHubEMUConfiguration config;
    private jp.openstandia.connector.github.GitHubClient<GitHubEMUSchema> client;
    private GitHubEMUSchema schema;

    @BeforeEach
    void setup() {
        config = mock(GitHubEMUConfiguration.class);
        client = mock(jp.openstandia.connector.github.GitHubClient.class);
        schema = mock(GitHubEMUSchema.class);
    }

    @Test
    void testFieldAssignmentsAndAccess() {
        SCIMEMUGroup group = new SCIMEMUGroup();

        SCIMMeta meta = new SCIMMeta();
        meta.created = "2025-01-01T00:00:00Z";
        meta.lastModified = "2025-10-24T00:00:00Z";

        SCIMMember member = new SCIMMember();
        member.value = "123";

        group.schemas = new String[]{"urn:ietf:params:scim:schemas:core:2.0:Group"};
        group.meta = meta;
        group.id = "group-1";
        group.displayName = "Engineering";
        group.members = List.of(member);
        group.externalId = "ext-001";

        assertEquals("group-1", group.id);
        assertEquals("Engineering", group.displayName);
        assertEquals("ext-001", group.externalId);
        assertEquals("urn:ietf:params:scim:schemas:core:2.0:Group", group.schemas[0]);
        assertEquals(meta, group.meta);
        assertEquals(1, group.members.size());
        assertEquals("123", group.members.get(0).value);
    }

    @Test
    void testJacksonSerializationDeserialization() throws Exception {
        SCIMEMUGroup group = new SCIMEMUGroup();
        group.schemas = new String[]{"schema1", "schema2"};
        group.id = "G1";
        group.displayName = "Developers";
        group.externalId = "EXT-DEV";
        SCIMMeta meta = new SCIMMeta();
        meta.created = "2025-01-01T00:00:00Z";
        meta.lastModified = "2025-02-02T00:00:00Z";
        group.meta = meta;

        SCIMMember member = new SCIMMember();
        member.value = "U123";
        group.members = List.of(member);

        ObjectMapper mapper = new ObjectMapper();

        String json = mapper.writeValueAsString(group);
        assertTrue(json.contains("\"id\":\"G1\""));
        assertTrue(json.contains("\"displayName\":\"Developers\""));
        assertTrue(json.contains("\"externalId\":\"EXT-DEV\""));
        assertTrue(json.contains("\"members\""));
        assertTrue(json.contains("\"schemas\""));

        SCIMEMUGroup restored = mapper.readValue(json, SCIMEMUGroup.class);
        assertEquals("G1", restored.id);
        assertEquals("Developers", restored.displayName);
        assertEquals("EXT-DEV", restored.externalId);
        assertEquals(2, restored.schemas.length);
        assertEquals("U123", restored.members.get(0).value);
        assertNotNull(restored.meta);
        assertEquals("2025-01-01T00:00:00Z", restored.meta.created);
    }

    @Test
    void createSchemaShouldMapCreateDeltaAndReadPaths() {
        SchemaDefinition sd =
                GitHubEMUGroupHandler.createSchema(config, client)
                        .build();

        SCIMEMUGroup dest = new SCIMEMUGroup();

        Set<Attribute> attrs = Set.of(
                AttributeBuilder.build(Name.NAME, "Engineering"),
                AttributeBuilder.build("externalId", "ext-001"),
                AttributeBuilder.build("members.User.value", List.of("u1", "u2"))
        );

        SCIMEMUGroup mapped = sd.apply(attrs, dest);

        assertEquals("Engineering", mapped.displayName);
        assertEquals("ext-001", mapped.externalId);
        assertNotNull(mapped.members);
        assertEquals(2, mapped.members.size());
        assertEquals("u1", mapped.members.get(0).value);
        assertEquals("u2", mapped.members.get(1).value);

        SCIMPatchOperations patch = mock(SCIMPatchOperations.class);

        Set<AttributeDelta> deltas = Set.of(
                AttributeDeltaBuilder.build(Name.NAME, "Eng-Updated"),
                AttributeDeltaBuilder.build("externalId", "ext-002"),
                new AttributeDeltaBuilder()
                        .setName("members.User.value")
                        .addValueToAdd("u3")
                        .addValueToRemove("u2")
                        .build()
        );

        sd.applyDelta(deltas, patch);

        verify(patch).replace(eq("displayName"), eq("Eng-Updated"));
        verify(patch).replace(eq("externalId"), eq("ext-002"));
        verify(patch).addMembers(argThat(list -> list != null && list.contains("u3")));
        verify(patch).removeMembers(argThat(list -> list != null && list.contains("u2")));

        SCIMEMUGroup src = new SCIMEMUGroup();
        src.id = "gid-1";
        src.displayName = "Engineering";
        src.externalId = "ext-001";

        SCIMMember userMember = new SCIMMember();
        userMember.ref = "/Users/u1";
        userMember.value = "u1";

        SCIMMember notUserMember = new SCIMMember();
        notUserMember.ref = "/Groups/g2";
        notUserMember.value = "g2";

        src.members = List.of(userMember, notUserMember);

        SCIMMeta meta = new SCIMMeta();
        meta.created = OffsetDateTime.now().toString();
        meta.lastModified = OffsetDateTime.now().toString();
        src.meta = meta;

        ObjectHandler oh = new ObjectHandler() {
            @Override public ObjectHandler setInstanceName(String instanceName) { return this; }
            @Override public Uid create(Set<Attribute> attributes) { return null; }
            @Override public Set<AttributeDelta> updateDelta(Uid uid, Set<AttributeDelta> modifications, OperationOptions options) { return null; }
            @Override public void delete(Uid uid, OperationOptions options) {}
            @Override public SchemaDefinition getSchemaDefinition() { return sd; }
        };

        Set<String> attributesToGet = Set.of("members.User.value", "meta.created", "meta.lastModified");

        ConnectorObject co = oh.toConnectorObject(sd, src, attributesToGet, false);

        assertEquals("gid-1", co.getUid().getUidValue());
        assertEquals("Engineering", co.getName().getNameValue());

        Attribute membersAttr = co.getAttributeByName("members.User.value");
        assertNotNull(membersAttr);
        assertEquals(List.of("u1"), membersAttr.getValue());

        assertNotNull(co.getAttributeByName("meta.created"));
        assertNotNull(co.getAttributeByName("meta.lastModified"));
    }

    @Test
    void createShouldCallClientCreateEMUGroupAndReturnUid() {

        SchemaDefinition sd =
                GitHubEMUGroupHandler.createSchema(config, client)
                        .build();

        GitHubEMUGroupHandler handler =
                new GitHubEMUGroupHandler(mock(GitHubEMUConfiguration.class), client, schema, sd);

        Uid expected = new Uid("gid-1", new Name("Engineering"));
        when(client.createEMUGroup(eq(schema), any(SCIMEMUGroup.class))).thenReturn(expected);

        Set<Attribute> attrs = Set.of(
                AttributeBuilder.build(Name.NAME, "Engineering"),
                AttributeBuilder.build("externalId", "ext-001")
        );

        Uid actual = handler.create(attrs);

        assertSame(expected, actual);
        verify(client).createEMUGroup(eq(schema), any(SCIMEMUGroup.class));
    }

    @Test
    void updateDeltaShouldPatchOnlyWhenHasChangesAndReturnNull() {

        SchemaDefinition sd =
                GitHubEMUGroupHandler.createSchema(config, client)
                        .build();

        GitHubEMUGroupHandler handler =
                new GitHubEMUGroupHandler(mock(GitHubEMUConfiguration.class), client, schema, sd);

        Uid uid = new Uid("gid-1");

        Set<AttributeDelta> empty = Set.of();
        assertNull(handler.updateDelta(uid, empty, null));
        verify(client, never()).patchEMUGroup(any(), any());

        Set<AttributeDelta> mods = Set.of(AttributeDeltaBuilder.build(Name.NAME, "Eng-Updated"));
        assertNull(handler.updateDelta(uid, mods, null));
        verify(client).patchEMUGroup(eq(uid), any(SCIMPatchOperations.class));
    }

    @Test
    void deleteShouldCallClientDeleteEMUGroup() {

        SchemaDefinition sd =
                GitHubEMUGroupHandler.createSchema(config, client)
                        .build();

        GitHubEMUGroupHandler handler =
                new GitHubEMUGroupHandler(mock(GitHubEMUConfiguration.class), client, schema, sd);

        Uid uid = new Uid("gid-1");
        OperationOptions options = new OperationOptionsBuilder().build();

        handler.delete(uid, options);

        verify(client).deleteEMUGroup(eq(uid), eq(options));
    }

    @Test
    void getByUid_shouldReturn1WhenFound_else0() {

        SchemaDefinition sd =
                GitHubEMUGroupHandler.createSchema(config, client)
                        .build();

        GitHubEMUGroupHandler handler =
                new GitHubEMUGroupHandler(mock(GitHubEMUConfiguration.class), client, schema, sd);

        ResultsHandler rh = mock(ResultsHandler.class);
        when(rh.handle(any())).thenReturn(true);

        Uid uid = new Uid("gid-1");

        when(client.getEMUGroup(eq(uid), any(), any())).thenReturn(null);
        assertEquals(0, handler.getByUid(uid, rh, null, Set.of(), null, false, 0, 0));

        SCIMEMUGroup g = new SCIMEMUGroup();
        g.id = "gid-1";
        g.displayName = "Engineering";
        when(client.getEMUGroup(eq(uid), any(), any())).thenReturn(g);

        assertEquals(1, handler.getByUid(uid, rh, null, Set.of(), null, false, 0, 0));
        verify(rh).handle(any(ConnectorObject.class));
    }

    @Test
    void getByName_shouldReturn1WhenFound_else0() {

        SchemaDefinition sd =
                GitHubEMUGroupHandler.createSchema(config, client)
                        .build();

        GitHubEMUGroupHandler handler =
                new GitHubEMUGroupHandler(mock(GitHubEMUConfiguration.class), client, schema, sd);

        ResultsHandler rh = mock(ResultsHandler.class);
        when(rh.handle(any())).thenReturn(true);

        Name name = new Name("Engineering");

        when(client.getEMUGroup(eq(name), any(), any())).thenReturn(null);
        assertEquals(0, handler.getByName(name, rh, null, Set.of(), null, false, 0, 0));

        SCIMEMUGroup g = new SCIMEMUGroup();
        g.id = "gid-1";
        g.displayName = "Engineering";
        when(client.getEMUGroup(eq(name), any(), any())).thenReturn(g);

        assertEquals(1, handler.getByName(name, rh, null, Set.of(), null, false, 0, 0));
        verify(rh).handle(any(ConnectorObject.class));
    }


    @Test
    void getByMembersShouldFilterGroupsByMemberIdsCoverContainsTrueAndFalse() {
        SchemaDefinition sd =
                GitHubEMUGroupHandler.createSchema(config, client)
                        .build();

        GitHubEMUGroupHandler handler =
                new GitHubEMUGroupHandler(mock(GitHubEMUConfiguration.class), client, schema, sd);

        ResultsHandler rh = mock(ResultsHandler.class);
        when(rh.handle(any())).thenReturn(true);

        Attribute membersFilter = AttributeBuilder.build("members.User.value", List.of("u1", "u2"));

        SCIMEMUGroup match = new SCIMEMUGroup();
        match.id = "g-match";
        match.displayName = "Match";
        match.members = List.of(member("u1"), member("u2"));

        SCIMEMUGroup noMatch = new SCIMEMUGroup();
        noMatch.id = "g-nomatch";
        noMatch.displayName = "NoMatch";
        noMatch.members = List.of(member("u1"));

        when(client.getEMUGroups(any(), any(), any(), anyInt(), anyInt())).thenAnswer(inv -> {
            @SuppressWarnings("unchecked")
            jp.openstandia.connector.util.QueryHandler<SCIMEMUGroup> qh =
                    (jp.openstandia.connector.util.QueryHandler<SCIMEMUGroup>) inv.getArgument(0);

            boolean cont1 = qh.handle(match);
            assertTrue(cont1);

            boolean cont2 = qh.handle(noMatch);
            assertTrue(cont2);

            return 2;
        });

        int count = handler.getByMembers(membersFilter, rh, null, Set.of(), null, false, 10, 0);
        assertEquals(2, count);

        verify(rh, atLeastOnce()).handle(any(ConnectorObject.class));
    }

    private static SCIMMember member(String uid) {
        SCIMMember m = new SCIMMember();
        m.ref = "/Users/" + uid;
        m.value = uid;
        return m;
    }
}
