package org.kohsuke.github;
import jp.openstandia.connector.github.*;
import jp.openstandia.connector.github.GitHubClient;
import jp.openstandia.connector.util.SchemaDefinition;
import org.identityconnectors.framework.common.objects.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GitHubEMUUserHandlerTest {
    private GitHubEMUConfiguration config;
    private jp.openstandia.connector.github.GitHubClient<GitHubEMUSchema> client;
    private GitHubEMUSchema schema;
    private SchemaDefinition schemaDefinition;
    private GitHubEMUUserHandler handler;

    @BeforeEach
    void setup() {
        config = mock(GitHubEMUConfiguration.class);
        client = mock(jp.openstandia.connector.github.GitHubClient.class);
        schema = mock(GitHubEMUSchema.class);
        schemaDefinition = mock(SchemaDefinition.class);
        handler = new GitHubEMUUserHandler(config, client, schema, schemaDefinition);
    }

    @Test
    void testCreateSchema() {
        // Just ensure it builds without throwing exceptions
        SchemaDefinition.Builder builder = GitHubEMUUserHandler.createSchema(config, client);
        assertNotNull(builder);
    }

    @Test
    void testCreateUser() {
        // Prepare
        Set<Attribute> attrs = new HashSet<>();
        attrs.add(AttributeBuilder.build("userName", "testuser"));

        SCIMEMUUser mockMapped = new SCIMEMUUser();
        when(schemaDefinition.apply(anySet(), any(SCIMEMUUser.class))).thenReturn(mockMapped);

        Uid expectedUid = new Uid("123");
        when(client.createEMUUser(any(SCIMEMUUser.class))).thenReturn(expectedUid);

        // Act
        Uid result = handler.create(attrs);

        // Assert
        assertEquals("123", result.getUidValue());
        verify(client).createEMUUser(mockMapped);
    }

    @Test
    void testUpdateDeltaWithChanges() {
        Uid uid = new Uid("abc");
        Set<AttributeDelta> deltas = new HashSet<>();

        SCIMPatchOperations patchOps = mock(SCIMPatchOperations.class);
        when(schemaDefinition.applyDelta(anySet(), any())).then(invocation -> {
            SCIMPatchOperations dest = invocation.getArgument(1);
            return null;
        });
        when(patchOps.hasAttributesChange()).thenReturn(true);

        jp.openstandia.connector.github.GitHubClient<GitHubEMUSchema> spyClient = client;
        handler = new GitHubEMUUserHandler(config, spyClient, schema, schemaDefinition);

        SCIMPatchOperations dest = new SCIMPatchOperations();
        dest.replace("displayName", "newName");
        when(schemaDefinition.applyDelta(eq(deltas), any())).thenAnswer(inv -> {
            SCIMPatchOperations d = inv.getArgument(1);
            d.replace("displayName", "newName");
            return null;
        });

        handler.updateDelta(uid, deltas, new OperationOptionsBuilder().build());

        verify(spyClient).patchEMUUser(eq(uid), any(SCIMPatchOperations.class));
    }

    @Test
    void testUpdateDeltaWithoutChanges() {
        Uid uid = new Uid("abc");
        Set<AttributeDelta> deltas = new HashSet<>();

        when(schemaDefinition.applyDelta(eq(deltas), any())).thenAnswer(inv -> null);

        GitHubClient<GitHubEMUSchema> spyClient = client;
        handler = new GitHubEMUUserHandler(config, spyClient, schema, schemaDefinition);

        handler.updateDelta(uid, deltas, new OperationOptionsBuilder().build());

        verify(spyClient, never()).patchEMUUser(any(), any());
    }

    @Test
    void testDeleteUser() {
        Uid uid = new Uid("id-1");
        OperationOptions options = new OperationOptionsBuilder().build();

        handler.delete(uid, options);

        verify(client).deleteEMUUser(uid, options);
    }

    @Test
    void testGetByUidFound() {
        Uid uid = new Uid("uid-1");
        OperationOptions options = new OperationOptionsBuilder().build();
        ResultsHandler handlerMock = mock(ResultsHandler.class);

        SCIMEMUUser user = new SCIMEMUUser();

        when(client.getEMUUser(eq(uid), eq(options), any())).thenReturn(user);
        when(schemaDefinition.toConnectorObjectBuilder(user, Set.of(), false)).thenReturn(mock(ConnectorObjectBuilder.class));

        int result = handler.getByUid(uid, handlerMock, options, Set.of(), Set.of(), false, 0, 0);

        assertEquals(1, result);
        verify(handlerMock).handle(any());
    }

    @Test
    void testGetByUid_notFound() {
        Uid uid = new Uid("uid-1");
        OperationOptions options = new OperationOptionsBuilder().build();
        ResultsHandler handlerMock = mock(ResultsHandler.class);

        when(client.getEMUUser(eq(uid), eq(options), any())).thenReturn(null);

        int result = handler.getByUid(uid, handlerMock, options, Set.of(), Set.of(), false, 0, 0);

        assertEquals(0, result);
        verify(handlerMock, never()).handle(any());
    }

    @Test
    void testGetByName_found() {
        Name name = new Name("testuser");
        OperationOptions options = new OperationOptionsBuilder().build();
        ResultsHandler handlerMock = mock(ResultsHandler.class);

        SCIMEMUUser user = new SCIMEMUUser();
        when(client.getEMUUser(eq(name), eq(options), any())).thenReturn(user);
        when(schemaDefinition.toConnectorObjectBuilder(user, Set.of(), false)).thenReturn(mock(ConnectorObjectBuilder.class));

        int result = handler.getByName(name, handlerMock, options, Set.of(), Set.of(), false, 0, 0);

        assertEquals(1, result);
        verify(handlerMock).handle(any());
    }

    @Test
    void testGetByName_notFound() {
        Name name = new Name("testuser");
        OperationOptions options = new OperationOptionsBuilder().build();
        ResultsHandler handlerMock = mock(ResultsHandler.class);

        when(client.getEMUUser(eq(name), eq(options), any())).thenReturn(null);

        int result = handler.getByName(name, handlerMock, options, Set.of(), Set.of(), false, 0, 0);

        assertEquals(0, result);
    }

    @Test
    void testGetAllUsers() {
        ResultsHandler handlerMock = mock(ResultsHandler.class);
        when(handlerMock.handle(any())).thenReturn(true);
        when(client.getEMUUsers(any(), any(), any(), anyInt(), anyInt())).thenReturn(3);

        int result = handler.getAll(handlerMock, new OperationOptionsBuilder().build(), Set.of(), Set.of(), false, 10, 0);

        assertEquals(3, result);
        verify(client).getEMUUsers(any(), any(), any(), anyInt(), anyInt());
    }

    @Test
    void createSchemaShouldBuildSchemaAndMapAllAttributes() {
        SchemaDefinition.Builder builder =
                GitHubEMUUserHandler.createSchema(config, client);

        assertNotNull(builder);

        SchemaDefinition schema =
                builder.build();

        // ---------- CREATE mapping ----------
        SCIMEMUUser target = new SCIMEMUUser();

        Set<Attribute> attrs = Set.of(
                AttributeBuilder.build(Name.NAME, "jdoe"),
                AttributeBuilder.build("externalId", "ext-1"),
                AttributeBuilder.build("displayName", "John Doe"),
                AttributeBuilder.build("name.formatted", "John Doe"),
                AttributeBuilder.build("name.givenName", "John"),
                AttributeBuilder.build("name.familyName", "Doe"),
                AttributeBuilder.build("primaryEmail", "john@acme.com"),
                AttributeBuilder.build("primaryRole", "developer"),
                AttributeBuilder.buildEnabled(true)
        );

        SCIMEMUUser mapped = schema.apply(attrs, target);

        assertEquals("jdoe", mapped.userName);
        assertEquals("ext-1", mapped.externalId);
        assertEquals("John Doe", mapped.displayName);
        assertTrue(mapped.active);

        assertNotNull(mapped.name);
        assertEquals("John Doe", mapped.name.formatted);
        assertEquals("John", mapped.name.givenName);
        assertEquals("Doe", mapped.name.familyName);

        assertEquals(1, mapped.emails.size());
        assertEquals("john@acme.com", mapped.emails.get(0).value);
        assertTrue(mapped.emails.get(0).primary);

        assertEquals(1, mapped.roles.size());
        assertEquals("developer", mapped.roles.get(0).value);
        assertTrue(mapped.roles.get(0).primary);

        // ---------- READ mapping ----------
        SCIMEMUUser source = new SCIMEMUUser();
        source.id = "uuid-1";
        source.userName = "jdoe";
        source.externalId = "ext-1";
        source.active = true;

        SCIMName name = new SCIMName();
        name.formatted = "John Doe";
        name.givenName = "John";
        name.familyName = "Doe";
        source.name = name;

        SCIMEmail email = new SCIMEmail();
        email.value = "john@acme.com";
        email.primary = true;
        source.emails = List.of(email);

        SCIMRole role = new SCIMRole();
        role.value = "developer";
        role.primary = true;
        source.roles = List.of(role);

        SCIMMember group = new SCIMMember();
        group.ref = "/Groups/123";
        group.value = "123";
        source.groups = List.of(group);

        SCIMMeta meta = new SCIMMeta();
        meta.created = String.valueOf(OffsetDateTime.now());
        meta.lastModified = String.valueOf(OffsetDateTime.now());
        source.meta = meta;

        Set<String> attrsToGetSet = new HashSet<>(Set.of());
        attrsToGetSet.add("primaryEmail");
        attrsToGetSet.add("primaryRole");
        attrsToGetSet.add("groups");
        attrsToGetSet.add("meta.created");
        attrsToGetSet.add("meta.lastModified");

        ConnectorObject co =
                handler.toConnectorObject(
                        schema, source, attrsToGetSet, false
                );

        assertEquals("uuid-1", co.getUid().getUidValue());
        assertEquals("jdoe", co.getName().getNameValue());

        assertEquals("john@acme.com",
                co.getAttributeByName("primaryEmail").getValue().get(0));

        assertEquals("developer",
                co.getAttributeByName("primaryRole").getValue().get(0));

        assertNotNull(co.getAttributeByName("meta.created"));
        assertNotNull(co.getAttributeByName("meta.lastModified"));
    }

    @Test
    void createSchemaShouldHandleNullEmailAndRoleOnUpdate() {
        SchemaDefinition.Builder builder =
                GitHubEMUUserHandler.createSchema(
                        config,
                        client
                );

        SchemaDefinition schema =
                builder.build();

        SCIMPatchOperations dest = new SCIMPatchOperations();

        Set<AttributeDelta> deltas = Set.of(
                AttributeDeltaBuilder.build("primaryEmail", ""),
                AttributeDeltaBuilder.build("primaryRole", "")
        );

        schema.applyDelta(deltas, dest);

        assertTrue(dest.hasAttributesChange());
    }

    @Test
    void applyDeltaShouldCallReplace_forSimpleAttributes() {
        SchemaDefinition schema = buildSchema();
        SCIMPatchOperations dest = mock(SCIMPatchOperations.class);

        Set<AttributeDelta> deltas = Set.of(
                AttributeDeltaBuilder.build(Name.NAME, "newUserName"),

                AttributeDeltaBuilder.build(OperationalAttributes.ENABLE_NAME, true),

                AttributeDeltaBuilder.build("externalId", "ext-123"),

                AttributeDeltaBuilder.build("displayName", "John Doe"),

                AttributeDeltaBuilder.build("name.formatted", "John Doe"),
                AttributeDeltaBuilder.build("name.givenName", "John"),
                AttributeDeltaBuilder.build("name.familyName", "Doe")
        );

        schema.applyDelta(deltas, dest);

        verify(dest).replace(eq("userName"), eq("newUserName"));
        verify(dest).replace(eq("active"), eq(true));
        verify(dest).replace(eq("externalId"), eq("ext-123"));
        verify(dest).replace(eq("displayName"), eq("John Doe"));

        verify(dest).replace(eq("name.formatted"), eq("John Doe"));
        verify(dest).replace(eq("name.givenName"), eq("John"));
        verify(dest).replace(eq("name.familyName"), eq("Doe"));

        verifyNoMoreInteractions(dest);
    }

    private SchemaDefinition buildSchema() {
        return GitHubEMUUserHandler.createSchema(config, client).build();
    }

    @Test
    void applyDeltaShouldCallReplace_withNewPrimaryEmailObjectWhenPrimaryEmailIsNonNull() {
        SchemaDefinition schema = buildSchema();
        SCIMPatchOperations dest = mock(SCIMPatchOperations.class);

        Set<AttributeDelta> deltas = Set.of(
                AttributeDeltaBuilder.build("primaryEmail", "john@acme.com")
        );

        schema.applyDelta(deltas, dest);

        verify(dest).replace(argThat((SCIMEmail e) ->
                e != null
                        && "john@acme.com".equals(e.value)
                        && Boolean.TRUE.equals(e.primary)
        ));
        verifyNoMoreInteractions(dest);
    }

    @Test
    void applyDeltaShouldCallReplace_withNullEmailWhenPrimaryEmailIsNull() {
        SchemaDefinition schema = buildSchema();
        SCIMPatchOperations dest = mock(SCIMPatchOperations.class);

        Set<AttributeDelta> deltas = Set.of(
                AttributeDeltaBuilder.build("primaryEmail", (Object) null)
        );

        schema.applyDelta(deltas, dest);

        verify(dest).replace((SCIMEmail) isNull());
        verifyNoMoreInteractions(dest);
    }

    @Test
    void applyDeltaShouldCallReplaceWithNewPrimaryRoleObjectWhenPrimaryRoleIsNonNull() {
        SchemaDefinition schema = buildSchema();
        SCIMPatchOperations dest = mock(SCIMPatchOperations.class);

        Set<AttributeDelta> deltas = Set.of(
                AttributeDeltaBuilder.build("primaryRole", "developer")
        );

        schema.applyDelta(deltas, dest);

        verify(dest).replace(argThat((SCIMRole r) ->
                r != null
                        && "developer".equals(r.value)
                        && Boolean.TRUE.equals(r.primary)
        ));
        verifyNoMoreInteractions(dest);
    }

    @Test
    void applyDeltaShouldCallReplaceWithNullRoleWhenPrimaryRoleIsNull() {
        SchemaDefinition schema = buildSchema();
        SCIMPatchOperations dest = mock(SCIMPatchOperations.class);

        Set<AttributeDelta> deltas = Set.of(
                AttributeDeltaBuilder.build("primaryRole", (Object) null)
        );

        schema.applyDelta(deltas, dest);

        verify(dest).replace((SCIMRole) isNull());
        verifyNoMoreInteractions(dest);
    }
}
