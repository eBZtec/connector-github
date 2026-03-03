package jp.openstandia.connector.github;

import jp.openstandia.connector.util.QueryHandler;
import jp.openstandia.connector.util.SchemaDefinition;
import org.identityconnectors.framework.common.objects.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kohsuke.github.SCIMEMUGroup;
import org.kohsuke.github.SCIMMember;
import org.kohsuke.github.SCIMPatchOperations;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class GitHubEMUGroupHandlerTest {

    @Mock
    private GitHubEMUConfiguration configuration;

    @Mock
    private GitHubClient<GitHubEMUSchema> client;

    @Mock
    private GitHubEMUSchema schema;

    @Mock
    private ResultsHandler resultsHandler;

    private SchemaDefinition schemaDefinition;
    private GitHubEMUGroupHandler handler;

    @BeforeEach
    void setUp() {
        SchemaDefinition.Builder builder = GitHubEMUGroupHandler.createSchema(configuration, client);
        schemaDefinition = builder.build();

        handler = new GitHubEMUGroupHandler(configuration, client, schema, schemaDefinition);
    }

    @Test
    void testCreate() {
        Set<Attribute> attributes = Set.of(
                new Name("test-group"),
                AttributeBuilder.build("externalId", "ext-123"),
                AttributeBuilder.build("members.User.value", "user-uuid-1", "user-uuid-2")
        );

        when(client.createEMUGroup(eq(schema), any(SCIMEMUGroup.class)))
                .thenReturn(new Uid("group-created-uuid"));

        Uid uid = handler.create(attributes);

        assertEquals("group-created-uuid", uid.getUidValue());
        verify(client, times(1)).createEMUGroup(eq(schema), any(SCIMEMUGroup.class));
    }

    @Test
    void testUpdateDelta() {
        Uid uid = new Uid("group-uuid");
        Set<AttributeDelta> modifications = Set.of(
                AttributeDeltaBuilder.build(Name.NAME, "new-display-name"),
                AttributeDeltaBuilder.build("externalId", "new-ext-456")
        );

        doNothing().when(client).patchEMUGroup(eq(uid), any(SCIMPatchOperations.class));

        handler.updateDelta(uid, modifications, null);

        verify(client, times(1)).patchEMUGroup(eq(uid), any(SCIMPatchOperations.class));
    }

    @Test
    void testDelete() {
        Uid uid = new Uid("group-to-delete");
        OperationOptions options = new OperationOptionsBuilder().build();

        doNothing().when(client).deleteEMUGroup(eq(uid), eq(options));

        handler.delete(uid, options);

        verify(client, times(1)).deleteEMUGroup(eq(uid), eq(options));
    }

    @Test
    void testGetByUid() {
        Uid uid = new Uid("group-uuid");
        OperationOptions options = new OperationOptionsBuilder().build();
        Set<String> returnAttributesSet = Set.of("__UID__", "__NAME__", "externalId");
        Set<String> fetchFieldsSet = Collections.emptySet();

        SCIMEMUGroup group = new SCIMEMUGroup();
        group.id = "group-uuid";
        group.displayName = "Test Group";
        group.externalId = "ext-123";

        when(client.getEMUGroup(eq(uid), eq(options), eq(fetchFieldsSet))).thenReturn(group);

        int count = handler.getByUid(uid, resultsHandler, options,
                returnAttributesSet, fetchFieldsSet, false, 100, 0);

        assertEquals(1, count);
        verify(resultsHandler, times(1)).handle(any(ConnectorObject.class));
    }

    @Test
    void testGetByName() {
        Name name = new Name("test-group");
        OperationOptions options = new OperationOptionsBuilder().build();
        Set<String> returnAttributesSet = Set.of("__UID__", "__NAME__", "externalId");
        Set<String> fetchFieldsSet = Collections.emptySet();

        SCIMEMUGroup group = new SCIMEMUGroup();
        group.id = "group-uuid";
        group.displayName = "test-group";
        group.externalId = "ext-456";

        when(client.getEMUGroup(eq(name), eq(options), eq(fetchFieldsSet))).thenReturn(group);

        int count = handler.getByName(name, resultsHandler, options,
                returnAttributesSet, fetchFieldsSet, false, 100, 0);

        assertEquals(1, count);
        verify(resultsHandler, times(1)).handle(any(ConnectorObject.class));
    }

    @Test
    void testGetAll() {
        OperationOptions options = new OperationOptionsBuilder().build();
        Set<String> returnAttributesSet = Set.of("__UID__", "displayName", "externalId");
        Set<String> fetchFieldsSet = Collections.emptySet();
        int pageSize = 100;
        int pageOffset = 0;

        ArgumentCaptor<QueryHandler<SCIMEMUGroup>> callbackCaptor =
                ArgumentCaptor.forClass((Class) QueryHandler.class);

        when(client.getEMUGroups(callbackCaptor.capture(), eq(options), eq(fetchFieldsSet), eq(pageSize), eq(pageOffset)))
                .thenReturn(1);

        int count = handler.getAll(resultsHandler, options,
                returnAttributesSet, fetchFieldsSet, false, pageSize, pageOffset);

        assertEquals(1, count);

        QueryHandler<SCIMEMUGroup> callback = callbackCaptor.getValue();
        SCIMEMUGroup group = new SCIMEMUGroup();
        group.id = "g1";
        group.displayName = "Group 1";
        callback.handle(group);

        verify(resultsHandler, times(1)).handle(any(ConnectorObject.class));
    }

    @Test
    void testGetByMembers() {
        String memberId = "8a8a8a8a-8a8a-8a8a-8a8a-8a8a8a8a8a8a";
        Attribute attribute = AttributeBuilder.build("members.User.value", memberId);

        OperationOptions options = new OperationOptionsBuilder().build();
        Set<String> returnAttributesSet = Set.of("__UID__", "displayName", "externalId");
        Set<String> fetchFieldsSet = Collections.emptySet();
        int pageSize = 100;
        int pageOffset = 0;

        ArgumentCaptor<QueryHandler<SCIMEMUGroup>> callbackCaptor =
                ArgumentCaptor.forClass((Class) QueryHandler.class);

        when(client.getEMUGroups(callbackCaptor.capture(), eq(options), eq(fetchFieldsSet), eq(pageSize), eq(pageOffset)))
                .thenReturn(2);

        int count = handler.getByMembers(attribute, resultsHandler, options,
                returnAttributesSet, fetchFieldsSet, false, pageSize, pageOffset);

        assertEquals(2, count);

        QueryHandler<SCIMEMUGroup> callback = callbackCaptor.getValue();

        // Grupo que MATCH → precisa ter id + displayName
        SCIMEMUGroup matching = new SCIMEMUGroup();
        matching.id = "group-matching-uuid";
        matching.displayName = "Matching Group";
        matching.externalId = "ext-matching-123";
        SCIMMember m1 = new SCIMMember();
        m1.value = memberId;
        matching.members = List.of(m1);
        callback.handle(matching);

        // Grupo que NÃO match
        SCIMEMUGroup nonMatching = new SCIMEMUGroup();
        nonMatching.id = "group-nonmatching-uuid";
        nonMatching.displayName = "Non-matching Group";
        nonMatching.members = List.of();
        callback.handle(nonMatching);

        verify(resultsHandler, times(1)).handle(any(ConnectorObject.class));
    }
}