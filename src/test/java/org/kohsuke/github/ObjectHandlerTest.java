package org.kohsuke.github;

import jp.openstandia.connector.github.GitHubFilter;
import jp.openstandia.connector.util.ObjectHandler;
import jp.openstandia.connector.util.SchemaDefinition;
import org.identityconnectors.framework.common.objects.*;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ObjectHandlerTest {

    static class Dummy {
        String id;
        String name;
    }

    private static SchemaDefinition buildSchema() {
        SchemaDefinition.Builder<Dummy, Dummy, Dummy> b = SchemaDefinition.newBuilder(
                new ObjectClass("Dummy"), Dummy.class, Dummy.class, Dummy.class);

        b.addUid("id", SchemaDefinition.Types.STRING,
                (val, dest) -> dest.id = val,
                (src) -> src.id,
                "id");

        b.addName("name", SchemaDefinition.Types.STRING,
                (val, dest) -> dest.name = val,
                (src) -> src.name,
                "name");

        return b.build();
    }

    static class TestHandler implements ObjectHandler {
        private final SchemaDefinition schema;

        TestHandler(SchemaDefinition schema) {
            this.schema = schema;
        }

        @Override
        public ObjectHandler setInstanceName(String instanceName) {
            return this;
        }

        @Override
        public Uid create(Set<Attribute> attributes) {
            return new Uid("x");
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
        public SchemaDefinition getSchemaDefinition() {
            return schema;
        }
    }

    @Test
    void getByUidThrowsUnsupportedOperationException() {
        ObjectHandler h = new TestHandler(buildSchema());

        assertThrows(UnsupportedOperationException.class, () ->
                h.getByUid(
                        new Uid("1"),
                        obj -> true,
                        new OperationOptionsBuilder().build(),
                        Collections.emptySet(),
                        Collections.emptySet(),
                        false,
                        10,
                        0
                )
        );
    }

    @Test
    void getByNameThrowsUnsupportedOperationException() {
        ObjectHandler h = new TestHandler(buildSchema());

        assertThrows(UnsupportedOperationException.class, () ->
                h.getByName(
                        new Name("n"),
                        obj -> true,
                        new OperationOptionsBuilder().build(),
                        Collections.emptySet(),
                        Collections.emptySet(),
                        false,
                        10,
                        0
                )
        );
    }

    @Test
    void getByMembersReturnsZero() {
        ObjectHandler h = new TestHandler(buildSchema());

        int out = h.getByMembers(
                AttributeBuilder.build("members", "a"),
                obj -> true,
                new OperationOptionsBuilder().build(),
                Collections.emptySet(),
                Collections.emptySet(),
                false,
                10,
                0
        );

        assertEquals(0, out);
    }

    @Test
    void getAllThrowsUnsupportedOperationException() {
        ObjectHandler h = new TestHandler(buildSchema());

        assertThrows(UnsupportedOperationException.class, () ->
                h.getAll(
                        obj -> true,
                        new OperationOptionsBuilder().build(),
                        Collections.emptySet(),
                        Collections.emptySet(),
                        false,
                        10,
                        0
                )
        );
    }

    @Test
    void queryThrowsUnsupportedOperationException() {
        ObjectHandler h = new TestHandler(buildSchema());

        assertThrows(UnsupportedOperationException.class, () ->
                h.query(
                        (GitHubFilter) null,
                        obj -> true,
                        new OperationOptionsBuilder().build()
                )
        );
    }

    @Test
    void toConnectorObjectBuildsConnectorObjectFromSchema() {
        SchemaDefinition schema = buildSchema();
        ObjectHandler h = new TestHandler(schema);

        Dummy src = new Dummy();
        src.id = "id-1";
        src.name = "name-1";

        Set<String> returnAttrs = new HashSet<>();
        returnAttrs.add(Uid.NAME);
        returnAttrs.add(Name.NAME);

        ConnectorObject co = h.toConnectorObject(schema, src, returnAttrs, false);

        assertNotNull(co);
        assertEquals("id-1", co.getUid().getUidValue());
        assertEquals("name-1", co.getName().getNameValue());
    }
}
