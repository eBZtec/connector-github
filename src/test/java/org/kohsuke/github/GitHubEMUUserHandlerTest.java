package org.kohsuke.github;
import jp.openstandia.connector.github.GitHubClient;
import jp.openstandia.connector.github.GitHubEMUConfiguration;
import jp.openstandia.connector.github.GitHubEMUSchema;
import jp.openstandia.connector.github.GitHubEMUUserHandler;
import jp.openstandia.connector.util.SchemaDefinition;
import org.identityconnectors.framework.common.objects.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
    void testUpdateDelta_withChanges() {
        Uid uid = new Uid("abc");
        Set<AttributeDelta> deltas = new HashSet<>();

        SCIMPatchOperations patchOps = mock(SCIMPatchOperations.class);
        when(schemaDefinition.applyDelta(anySet(), any())).then(invocation -> {
            SCIMPatchOperations dest = invocation.getArgument(1);
            return null;
        });
        when(patchOps.hasAttributesChange()).thenReturn(true);

        // Spy to check call
        jp.openstandia.connector.github.GitHubClient<GitHubEMUSchema> spyClient = spy(client);
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
    void testUpdateDelta_withoutChanges() {
        Uid uid = new Uid("abc");
        Set<AttributeDelta> deltas = new HashSet<>();

        when(schemaDefinition.applyDelta(eq(deltas), any())).thenAnswer(inv -> null);

        GitHubClient<GitHubEMUSchema> spyClient = spy(client);
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
    void testGetByUid_found() {
        Uid uid = new Uid("uid-1");
        OperationOptions options = new OperationOptionsBuilder().build();
        ResultsHandler handlerMock = mock(ResultsHandler.class);

        SCIMEMUUser user = new SCIMEMUUser();
        when(client.getEMUUser(eq(uid), eq(options), any())).thenReturn(user);
        //when(schemaDefinition.toConnectorObject(any(), anyBoolean(), any())).thenReturn(mock(ConnectorObject.class));

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
        //when(schemaDefinition.toConnectorObjectBuilder(any(), anyBoolean(), any())).thenReturn(mock(ConnectorObject.class));

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
}
