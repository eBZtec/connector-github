package jp.openstandia.connector.github;

import jp.openstandia.connector.util.ObjectHandler;
import jp.openstandia.connector.util.SchemaDefinition;
import org.identityconnectors.framework.common.exceptions.ConnectorException;
import org.identityconnectors.framework.common.exceptions.InvalidAttributeValueException;
import org.identityconnectors.framework.common.objects.*;
import org.identityconnectors.framework.spi.SearchResultsHandler;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AbstractGitHubHandlerTest {
    static class TestConfiguration extends AbstractGitHubConfiguration {
        @Override
        public void validate() {
            // no-op for unit tests
        }
    }

    static class TestHandler implements ObjectHandler {
        static final ObjectClass OC = new ObjectClass("Test");

        private String instanceName;

        private final SchemaDefinition schemaDefinition;
        private final QueryBehavior behavior;

        interface QueryBehavior {
            void query(GitHubFilter filter, ResultsHandler resultsHandler, OperationOptions options);
        }

        TestHandler(SchemaDefinition schemaDefinition, QueryBehavior behavior) {
            this.schemaDefinition = schemaDefinition;
            this.behavior = behavior;
        }

        @Override
        public ObjectHandler setInstanceName(String instanceName) {
            this.instanceName = instanceName;
            return this;
        }

        public String getInstanceName() {
            return instanceName;
        }

        @Override
        public Uid create(Set<Attribute> attributes) {
            return new Uid("created");
        }

        @Override
        public Set<AttributeDelta> updateDelta(Uid uid, Set<AttributeDelta> modifications, OperationOptions options) {
            return Collections.emptySet();
        }

        @Override
        public void delete(Uid uid, OperationOptions options) {
            // no-op
        }

        @Override
        public void query(GitHubFilter filter, ResultsHandler resultsHandler, OperationOptions options) {
            behavior.query(filter, resultsHandler, options);
        }

        @Override
        public SchemaDefinition getSchemaDefinition() {
            return schemaDefinition;
        }

        @Override
        public int getByUid(Uid uid, ResultsHandler resultsHandler, OperationOptions options,
                            Set<String> returnAttributesSet, Set<String> fetchFieldsSet,
                            boolean allowPartialAttributeValues, int pageSize, int pageOffset) {
            resultsHandler.handle(new ConnectorObjectBuilder().setObjectClass(OC).setUid(uid).setName("n1").build());
            resultsHandler.handle(new ConnectorObjectBuilder().setObjectClass(OC).setUid(uid).setName("n2").build());
            return 10;
        }

        @Override
        public int getByName(Name name, ResultsHandler resultsHandler, OperationOptions options,
                             Set<String> returnAttributesSet, Set<String> fetchFieldsSet,
                             boolean allowPartialAttributeValues, int pageSize, int pageOffset) {
            resultsHandler.handle(new ConnectorObjectBuilder().setObjectClass(OC).setUid("u1").setName(name).build());
            return 10;
        }

        @Override
        public int getByMembers(Attribute attribute, ResultsHandler resultsHandler, OperationOptions options,
                                Set<String> returnAttributesSet, Set<String> fetchFieldSet,
                                boolean allowPartialAttributeValues, int pageSize, int pageOffset) {
            resultsHandler.handle(new ConnectorObjectBuilder().setObjectClass(OC).setUid("u1").setName("n").build());
            return 10;
        }

        @Override
        public int getAll(ResultsHandler resultsHandler, OperationOptions options,
                          Set<String> returnAttributesSet, Set<String> fetchFieldsSet,
                          boolean allowPartialAttributeValues, int pageSize, int pageOffset) {
            resultsHandler.handle(new ConnectorObjectBuilder().setObjectClass(OC).setUid("u1").setName("n").build());
            return 10;
        }
    }

    static class TestConnector extends AbstractGitHubConnector<TestConfiguration, AbstractGitHubSchema<TestConfiguration>> {

        final GitHubClient<AbstractGitHubSchema<TestConfiguration>> clientFactory;
        final AbstractGitHubSchema<TestConfiguration> schemaFactory;

        TestConnector(GitHubClient<AbstractGitHubSchema<TestConfiguration>> clientFactory,
                      AbstractGitHubSchema<TestConfiguration> schemaFactory) {
            this.clientFactory = clientFactory;
            this.schemaFactory = schemaFactory;
        }

        @Override
        protected GitHubClient<AbstractGitHubSchema<TestConfiguration>> newClient(TestConfiguration configuration) {
            return clientFactory;
        }

        @Override
        protected AbstractGitHubSchema<TestConfiguration> newGitHubSchema(TestConfiguration configuration,
                                                                          GitHubClient<AbstractGitHubSchema<TestConfiguration>> client) {
            return schemaFactory;
        }
    }

    static class TestEMUConnector extends GitHubEMUConnector {
        private final GitHubClient<GitHubEMUSchema> clientFactory;
        private final GitHubEMUSchema schemaFactory;

        TestEMUConnector(GitHubClient<GitHubEMUSchema> clientFactory, GitHubEMUSchema schemaFactory) {
            this.clientFactory = clientFactory;
            this.schemaFactory = schemaFactory;
        }

        @Override
        protected GitHubClient<GitHubEMUSchema> newClient(GitHubEMUConfiguration configuration) {
            return clientFactory;
        }

        @Override
        protected GitHubEMUSchema newGitHubSchema(GitHubEMUConfiguration configuration, GitHubClient<GitHubEMUSchema> client) {
            return schemaFactory;
        }
    }

    @Test
    void getConfigurationShouldReturnConfiguration() {
        TestConnector connector = new TestConnector(mock(GitHubClient.class), mock(AbstractGitHubSchema.class));
        TestConfiguration cfg = new TestConfiguration();
        connector.configuration = cfg;

        assertSame(cfg, connector.getConfiguration());
    }

    @Test
    void initShouldCreateClientAndLoadSchemaAndWrapRuntimeException() {
        GitHubClient<AbstractGitHubSchema<TestConfiguration>> client = mock(GitHubClient.class);
        AbstractGitHubSchema<TestConfiguration> schema = mock(AbstractGitHubSchema.class);
        when(schema.getSchema()).thenReturn(minimalSchema());

        TestConnector connector = new TestConnector(client, schema);

        connector.init(new TestConfiguration());
        verify(schema).getSchema();

        TestConnector failing = new TestConnector(client, schema) {
            @Override
            protected GitHubClient<AbstractGitHubSchema<TestConfiguration>> newClient(TestConfiguration configuration) {
                throw new RuntimeException("boom");
            }
        };

        ConnectorException ex = assertThrows(ConnectorException.class, () -> failing.init(new TestConfiguration()));
        assertNotNull(ex.getCause());
    }

    @Test
    void schemaShouldReturnSchemaAndWrapRuntimeException() {
        GitHubClient<AbstractGitHubSchema<TestConfiguration>> client = mock(GitHubClient.class);
        Schema expected = minimalSchema();

        AbstractGitHubSchema<TestConfiguration> schema = mock(AbstractGitHubSchema.class);
        when(schema.getSchema()).thenReturn(expected);

        TestConnector connector = new TestConnector(client, schema);
        connector.configuration = new TestConfiguration();
        connector.client = client;

        assertSame(expected, connector.schema());

        TestConnector failing = new TestConnector(client, schema) {
            @Override
            protected AbstractGitHubSchema<TestConfiguration> newGitHubSchema(TestConfiguration configuration,
                                                                              GitHubClient<AbstractGitHubSchema<TestConfiguration>> client) {
                throw new RuntimeException("boom");
            }
        };
        failing.configuration = new TestConfiguration();
        failing.client = client;

        assertThrows(ConnectorException.class, failing::schema);
    }

    private static ObjectClassInfo objectClassInfoWithUidAndName(String objectClassName) {
        Set<AttributeInfo.Flags> set = new HashSet<>();
        set.add(AttributeInfo.Flags.REQUIRED);
        set.add(AttributeInfo.Flags.NOT_CREATABLE);

        return new ObjectClassInfoBuilder()
                .setType(objectClassName)
                .addAttributeInfo(AttributeInfoBuilder.build(Uid.NAME, String.class, set))
                .addAttributeInfo(AttributeInfoBuilder.build(Name.NAME, String.class, set))
                .build();
    }

    private static Schema minimalSchema() {
        SchemaBuilder sb = new SchemaBuilder(AbstractGitHubConnector.class);

        sb.defineObjectClass(objectClassInfoWithUidAndName(ObjectClass.ACCOUNT_NAME));

        return sb.build();
    }

    @Test
    void createUpdateDeleteShouldValidateInputsAndWrapRuntimeExceptions() {
        ObjectClass oc = TestHandler.OC;


        ObjectHandler handler = mock(ObjectHandler.class);
        when(handler.setInstanceName(anyString())).thenReturn(handler);

        AbstractGitHubSchema<TestConfiguration> schema = mock(AbstractGitHubSchema.class);

        when(schema.getSchema()).thenReturn(minimalSchema());
        when(schema.getSchemaHandler(oc)).thenReturn(handler);

        TestConnector connector = new TestConnector(mock(GitHubClient.class), schema);
        connector.configuration = new TestConfiguration();
        connector.client = mock(GitHubClient.class);
        connector.instanceName = "inst";

        assertThrows(InvalidAttributeValueException.class, () -> connector.create(oc, null, null));
        assertThrows(InvalidAttributeValueException.class, () -> connector.create(oc, Collections.emptySet(), null));

        when(handler.create(anySet())).thenThrow(new RuntimeException("boom"));
        assertThrows(ConnectorException.class,
                () -> connector.create(oc, Set.of(AttributeBuilder.build("a", "b")), null));

        assertThrows(InvalidAttributeValueException.class,
                () -> connector.updateDelta(oc, null, Set.of(), null));
        assertThrows(InvalidAttributeValueException.class,
                () -> connector.updateDelta(oc, new Uid("u"), null, null));
        assertThrows(InvalidAttributeValueException.class,
                () -> connector.updateDelta(oc, new Uid("u"), Collections.emptySet(), null));

        when(handler.updateDelta(any(), anySet(), any())).thenThrow(new RuntimeException("boom"));
        assertThrows(ConnectorException.class,
                () -> connector.updateDelta(oc, new Uid("u"), Set.of(AttributeDeltaBuilder.build("x", "y")), null));

        assertThrows(InvalidAttributeValueException.class, () -> connector.delete(oc, null, null));

        doThrow(new RuntimeException("boom")).when(handler).delete(any(), any());
        assertThrows(ConnectorException.class, () -> connector.delete(oc, new Uid("u"), null));
    }

    @Test
    void getSchemaHandlerShouldValidateObjectClassAndUnsupportedClassAndSetInstanceName() {
        ObjectHandler handler = mock(ObjectHandler.class);
        when(handler.setInstanceName(anyString())).thenReturn(handler);
        when(handler.create(anySet())).thenReturn(new Uid("ok"));

        AbstractGitHubSchema<TestConfiguration> schema = mock(AbstractGitHubSchema.class);
        when(schema.getSchema()).thenReturn(minimalSchema());

        when(schema.getSchemaHandler(any())).thenReturn(null);

        TestConnector connector = new TestConnector(mock(GitHubClient.class), schema);
        connector.configuration = new TestConfiguration();
        connector.client = mock(GitHubClient.class);

        assertThrows(InvalidAttributeValueException.class,
                () -> connector.create(null, Set.of(AttributeBuilder.build("a", "b")), null));

        assertThrows(InvalidAttributeValueException.class,
                () -> connector.create(new ObjectClass("Unknown"), Set.of(AttributeBuilder.build("a", "b")), null));

        when(schema.getSchemaHandler(TestHandler.OC)).thenReturn(handler);
        connector.instanceName = "inst";

        Uid uid = connector.create(TestHandler.OC, Set.of(AttributeBuilder.build("a", "b")), null);
        assertEquals("ok", uid.getUidValue());
        verify(handler).setInstanceName("inst");
    }

    @Test
    void createFilterTranslatorShouldReturnTranslator() {
        TestConnector connector = new TestConnector(mock(GitHubClient.class), mock(AbstractGitHubSchema.class));
        assertNotNull(connector.createFilterTranslator(new ObjectClass("x"), new OperationOptionsBuilder().build()));
    }

    @Test
    void executeQueryShouldUseSchemaHandlerQueryForNonEmuConnectorAndWrapRuntimeException() {
        SchemaDefinition sd = mock(SchemaDefinition.class);

        TestHandler handler = new TestHandler(sd, (filter, resultsHandler, options) ->
                resultsHandler.handle(new ConnectorObjectBuilder().setObjectClass(TestHandler.OC).setUid("u").setName("n").build())
        );

        AbstractGitHubSchema<TestConfiguration> schema = mock(AbstractGitHubSchema.class);
        when(schema.getSchemaHandler(TestHandler.OC)).thenReturn(handler);
        when(schema.getSchema()).thenReturn(minimalSchema());

        TestConnector connector = new TestConnector(mock(GitHubClient.class), schema);
        connector.configuration = new TestConfiguration();
        connector.client = mock(GitHubClient.class);
        connector.schema = schema;

        ResultsHandler rh = mock(ResultsHandler.class);
        when(rh.handle(any())).thenReturn(true);

        connector.executeQuery(TestHandler.OC, null, rh, new OperationOptionsBuilder().build());
        verify(rh).handle(any());

        TestHandler throwing = new TestHandler(sd, (filter, resultsHandler, options) -> { throw new RuntimeException("boom"); });
        when(schema.getSchemaHandler(TestHandler.OC)).thenReturn(throwing);

        assertThrows(ConnectorException.class,
                () -> connector.executeQuery(TestHandler.OC, null, rh, new OperationOptionsBuilder().build()));
    }

    @Test
    void executeQueryWithSearchResultShouldHandleAllFilterTypesAndPaginationResult() {
        SchemaDefinition sd = mock(SchemaDefinition.class);
        when(sd.getReturnedByDefaultAttributesSet()).thenReturn(Map.of(
                Uid.NAME, "id",
                Name.NAME, "userName"
        ));
        when(sd.getFetchField(anyString())).thenAnswer(inv -> inv.getArgument(0));

        TestHandler handler = new TestHandler(sd, (filter, resultsHandler, options) -> {});

        GitHubEMUSchema emuSchema = mock(GitHubEMUSchema.class);
        when(emuSchema.getSchemaHandler(TestHandler.OC)).thenReturn(handler);
        when(emuSchema.getSchema()).thenReturn(minimalSchema());

        GitHubClient<GitHubEMUSchema> client = mock(GitHubClient.class);

        TestEMUConnector connector = new TestEMUConnector(client, emuSchema);
        GitHubEMUConfiguration cfg = mock(GitHubEMUConfiguration.class);
        when(cfg.getQueryPageSize()).thenReturn(2);

        connector.init(cfg);
        connector.setInstanceName("inst");

        SearchResultsHandler srh = mock(SearchResultsHandler.class);
        when(srh.handle(any())).thenReturn(true);

        OperationOptions options = new OperationOptionsBuilder()
                .setPageSize(2)
                .setPagedResultsOffset(3)
                .build();

        connector.executeQuery(TestHandler.OC, GitHubFilter.By(new Uid("u1")), srh, options);
        connector.executeQuery(TestHandler.OC, GitHubFilter.By(new Name("n1")), srh, options);
        connector.executeQuery(TestHandler.OC, GitHubFilter.ByMember(
                "members.User.value",
                GitHubFilter.FilterType.EXACT_MATCH,
                AttributeBuilder.build("members.User.value", "x")
        ), srh, options);
        connector.executeQuery(TestHandler.OC, null, srh, options);

        ArgumentCaptor<SearchResult> captor = ArgumentCaptor.forClass(SearchResult.class);
        verify(srh, atLeastOnce()).handleResult(captor.capture());
        assertTrue(captor.getAllValues().stream().allMatch(r -> r.getRemainingPagedResults() >= 0));
    }

    @Test
    void testShouldRecreateClientSetInstanceNameAndCallClientTestWrapRuntimeException() {
        GitHubClient<AbstractGitHubSchema<TestConfiguration>> client1 = mock(GitHubClient.class);
        GitHubClient<AbstractGitHubSchema<TestConfiguration>> client2 = mock(GitHubClient.class);

        AbstractGitHubSchema<TestConfiguration> schema = mock(AbstractGitHubSchema.class);
        when(schema.getSchema()).thenReturn(minimalSchema());

        TestConnector connector = new TestConnector(client2, schema);
        connector.configuration = new TestConfiguration();
        connector.client = client1;
        connector.instanceName = "inst";

        connector.test();

        verify(client1).close();
        verify(client2).setInstanceName("inst");
        verify(client2).test();

        doThrow(new RuntimeException("boom")).when(client2).test();
        assertThrows(ConnectorException.class, connector::test);
    }

    @Test
    void disposeShouldCloseAndNullOutClient() {
        GitHubClient<AbstractGitHubSchema<TestConfiguration>> client = mock(GitHubClient.class);

        TestConnector connector = new TestConnector(client, mock(AbstractGitHubSchema.class));
        connector.client = client;

        connector.dispose();
        verify(client).close();
        assertNull(connector.client);

        connector.dispose(); // should be safe when already null
    }

    @Test
    void checkAliveShouldDoNothing() {
        TestConnector connector = new TestConnector(mock(GitHubClient.class), mock(AbstractGitHubSchema.class));
        connector.checkAlive();
    }

    @Test
    void setInstanceNameShouldSetAndForwardToClient() {
        GitHubClient<AbstractGitHubSchema<TestConfiguration>> client = mock(GitHubClient.class);
        TestConnector connector = new TestConnector(client, mock(AbstractGitHubSchema.class));
        connector.client = client;

        connector.setInstanceName("inst");
        assertEquals("inst", connector.instanceName);
        verify(client).setInstanceName("inst");
    }

    @Test
    void processRuntimeExceptionShouldReturnSameConnectorExceptionOrWrapOtherRuntime() {
        TestConnector connector = new TestConnector(mock(GitHubClient.class), mock(AbstractGitHubSchema.class));

        ConnectorException ce = new ConnectorException("x");
        assertSame(ce, connector.processRuntimeException(ce));

        RuntimeException re = new RuntimeException("boom");
        ConnectorException wrapped = connector.processRuntimeException(re);
        assertSame(re, wrapped.getCause());
    }

}