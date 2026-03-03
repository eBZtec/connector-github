package jp.openstandia.connector.github;

import jp.openstandia.connector.util.ObjectHandler;
import jp.openstandia.connector.util.SchemaDefinition;
import jp.openstandia.connector.util.Utils;
import org.identityconnectors.framework.common.objects.*;
import org.identityconnectors.framework.spi.SearchResultsHandler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AbstractGitHubConnectorTest {

    @Mock
    private GitHubEMUConfiguration configuration;

    @Mock
    private GitHubClient<GitHubEMUSchema> client;

    @Mock
    private GitHubEMUSchema schema;

    @Mock
    private ObjectHandler objectHandler;

    @Mock
    private SchemaDefinition schemaDefinition;

    @Mock
    private ResultsHandler resultsHandler;

    @Mock
    private SearchResultsHandler searchResultsHandler;

    private static class TestConnector extends AbstractGitHubConnector<GitHubEMUConfiguration, GitHubEMUSchema> {

        TestConnector(GitHubEMUConfiguration cfg, GitHubClient<GitHubEMUSchema> cl, GitHubEMUSchema sch) {
            this.configuration = cfg;
            this.client = cl;
            this.schema = sch;
        }

        @Override
        protected GitHubClient<GitHubEMUSchema> newClient(GitHubEMUConfiguration configuration) {
            return client;
        }

        @Override
        protected GitHubEMUSchema newGitHubSchema(GitHubEMUConfiguration configuration, GitHubClient<GitHubEMUSchema> client) {
            return schema;
        }
    }

    private TestConnector connector;

    private void setUpCommonMocks() {
        when(configuration.getQueryPageSize()).thenReturn(100);
        when(schema.getSchemaHandler(any(ObjectClass.class))).thenReturn(objectHandler);
        when(objectHandler.getSchemaDefinition()).thenReturn(schemaDefinition);
    }

    private void setFilterField(Object filter, String fieldName, Object value) {
        try {
            Field f = GitHubFilter.class.getDeclaredField(fieldName);
            f.setAccessible(true);
            f.set(filter, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set field " + fieldName + " on GitHubFilter mock", e);
        }
    }

    @Test
    void executeQueryWithSearchResult_byUid_coversLine138() {
        setUpCommonMocks();
        connector = new TestConnector(configuration, client, schema);

        ObjectClass oc = new ObjectClass("EMUUser");
        OperationOptions options = new OperationOptionsBuilder().build();

        GitHubFilter filter = mock(GitHubFilter.class);
        doReturn(true).when(filter).isByUid();
        setFilterField(filter, "uid", new Uid("uid-123"));

        when(objectHandler.getByUid(any(), any(), any(), anySet(), anySet(), anyBoolean(), anyInt(), anyInt()))
                .thenReturn(5);

        try (MockedStatic<Utils> utils = mockStatic(Utils.class)) {
            utils.when(() -> Utils.resolvePageSize(any(), anyInt())).thenReturn(100);
            utils.when(() -> Utils.resolvePageOffset(any())).thenReturn(0);
            utils.when(() -> Utils.createFullAttributesToGet(any(), any())).thenReturn(Map.of());
            utils.when(() -> Utils.shouldAllowPartialAttributeValues(any())).thenReturn(false);

            connector.executeQueryWithSearchResult(oc, filter, resultsHandler, options);
        }

        verify(objectHandler).getByUid(eq(new Uid("uid-123")), any(), any(), anySet(), anySet(), anyBoolean(), anyInt(), anyInt());
    }

    @Test
    void executeQueryWithSearchResult_byName_coversLine152() {
        setUpCommonMocks();
        connector = new TestConnector(configuration, client, schema);

        ObjectClass oc = new ObjectClass("EMUUser");
        OperationOptions options = new OperationOptionsBuilder().build();

        GitHubFilter filter = mock(GitHubFilter.class);
        doReturn(true).when(filter).isByName();
        setFilterField(filter, "name", new Name("testuser"));

        when(objectHandler.getByName(any(), any(), any(), anySet(), anySet(), anyBoolean(), anyInt(), anyInt()))
                .thenReturn(3);

        try (MockedStatic<Utils> utils = mockStatic(Utils.class)) {
            utils.when(() -> Utils.resolvePageSize(any(), anyInt())).thenReturn(100);
            utils.when(() -> Utils.resolvePageOffset(any())).thenReturn(0);
            utils.when(() -> Utils.createFullAttributesToGet(any(), any())).thenReturn(Map.of());
            utils.when(() -> Utils.shouldAllowPartialAttributeValues(any())).thenReturn(false);

            connector.executeQueryWithSearchResult(oc, filter, resultsHandler, options);
        }

        verify(objectHandler).getByName(any(), any(), any(), anySet(), anySet(), anyBoolean(), anyInt(), anyInt());
    }

    @Test
    void executeQueryWithSearchResult_byMembers() {
        setUpCommonMocks();
        connector = new TestConnector(configuration, client, schema);

        ObjectClass oc = new ObjectClass("EMUGroup");
        OperationOptions options = new OperationOptionsBuilder().build();

        GitHubFilter filter = mock(GitHubFilter.class);
        doReturn(true).when(filter).isByMembers();
        setFilterField(filter, "attributeValue", AttributeBuilder.build("members.User.value", "member-uuid"));

        when(objectHandler.getByMembers(any(), any(), any(), anySet(), anySet(), anyBoolean(), anyInt(), anyInt()))
                .thenReturn(2);

        try (MockedStatic<Utils> utils = mockStatic(Utils.class)) {
            utils.when(() -> Utils.resolvePageSize(any(), anyInt())).thenReturn(100);
            utils.when(() -> Utils.resolvePageOffset(any())).thenReturn(0);
            utils.when(() -> Utils.createFullAttributesToGet(any(), any())).thenReturn(Map.of());
            utils.when(() -> Utils.shouldAllowPartialAttributeValues(any())).thenReturn(false);

            connector.executeQueryWithSearchResult(oc, filter, resultsHandler, options);
        }

        verify(objectHandler).getByMembers(any(), any(), any(), anySet(), anySet(), anyBoolean(), anyInt(), anyInt());
    }

    @Test
    void executeQueryWithSearchResult_noFilter_coversGetAll() {
        setUpCommonMocks();
        connector = new TestConnector(configuration, client, schema);

        ObjectClass oc = new ObjectClass("EMUUser");
        OperationOptions options = new OperationOptionsBuilder().build();

        when(objectHandler.getAll(any(), any(), anySet(), anySet(), anyBoolean(), anyInt(), anyInt()))
                .thenReturn(10);

        try (MockedStatic<Utils> utils = mockStatic(Utils.class)) {
            utils.when(() -> Utils.resolvePageSize(any(), anyInt())).thenReturn(100);
            utils.when(() -> Utils.resolvePageOffset(any())).thenReturn(0);
            utils.when(() -> Utils.createFullAttributesToGet(any(), any())).thenReturn(Map.of());
            utils.when(() -> Utils.shouldAllowPartialAttributeValues(any())).thenReturn(false);

            connector.executeQueryWithSearchResult(oc, null, resultsHandler, options);
        }

        verify(objectHandler).getAll(any(), any(), anySet(), anySet(), anyBoolean(), anyInt(), anyInt());
    }

    @Test
    void executeQueryWithSearchResult_pagination_coversSearchResult() {
        setUpCommonMocks();
        connector = new TestConnector(configuration, client, schema);

        ObjectClass oc = new ObjectClass("EMUUser");
        OperationOptions options = new OperationOptionsBuilder().build();

        when(objectHandler.getAll(any(), any(), anySet(), anySet(), anyBoolean(), anyInt(), anyInt()))
                .thenReturn(50);

        try (MockedStatic<Utils> utils = mockStatic(Utils.class)) {
            utils.when(() -> Utils.resolvePageSize(any(), anyInt())).thenReturn(100);
            utils.when(() -> Utils.resolvePageOffset(any())).thenReturn(2);
            utils.when(() -> Utils.createFullAttributesToGet(any(), any())).thenReturn(Map.of());
            utils.when(() -> Utils.shouldAllowPartialAttributeValues(any())).thenReturn(false);

            connector.executeQueryWithSearchResult(oc, null, searchResultsHandler, options);
        }

        verify(searchResultsHandler).handleResult(any(SearchResult.class));
    }

    @Test
    void testMethod_coversLines232And233() {
        GitHubClient<GitHubEMUSchema> newClientMock = mock(GitHubClient.class);
        TestConnectorForTest conn = new TestConnectorForTest(configuration, newClientMock);

        conn.setInstanceName("test-instance");

        conn.test();

        verify(newClientMock, times(2)).setInstanceName("test-instance");
        verify(newClientMock, times(1)).close();
        verify(newClientMock, times(1)).test();
    }

    private static class TestConnectorForTest extends AbstractGitHubConnector<GitHubEMUConfiguration, GitHubEMUSchema> {
        private final GitHubClient<GitHubEMUSchema> forcedClient;

        TestConnectorForTest(GitHubEMUConfiguration cfg, GitHubClient<GitHubEMUSchema> cl) {
            this.configuration = cfg;
            this.forcedClient = cl;
            this.client = cl;
        }

        @Override
        protected GitHubClient<GitHubEMUSchema> newClient(GitHubEMUConfiguration configuration) {
            return forcedClient;
        }

        @Override
        protected GitHubEMUSchema newGitHubSchema(GitHubEMUConfiguration configuration, GitHubClient<GitHubEMUSchema> client) {
            return mock(GitHubEMUSchema.class);
        }
    }
}