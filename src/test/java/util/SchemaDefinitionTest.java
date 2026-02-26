package util;

import jp.openstandia.connector.util.SchemaDefinition;
import jp.openstandia.connector.util.Utils;
import org.identityconnectors.framework.common.objects.*;
import org.junit.jupiter.api.Test;
import org.kohsuke.github.SCIMPatchOperations;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static jp.openstandia.connector.util.Utils.toZoneDateTime;
import static jp.openstandia.connector.util.Utils.toZoneDateTimeForISO8601OffsetDateTime;
import static org.identityconnectors.framework.common.objects.AttributeInfo.Flags.*;
import static org.junit.jupiter.api.Assertions.*;

class SchemaDefinitionTest {

    private static final DateTimeFormatter DEFAULT_DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;

    @Test
    void testNewBuilderOverload() {
        ObjectClass objectClass = new ObjectClass("TestClass");

        SchemaDefinition.Builder<String, String, Integer> builder =
                SchemaDefinition.newBuilder(objectClass, String.class, Integer.class);

        assertNotNull(builder);
    }

    @Test
    void testAddUidShouldCreateAndAddAttributeMapper() throws Exception {
        ObjectClass objectClass = new ObjectClass("testClass");

        SchemaDefinition.Builder<String, String, String> builder =
                new SchemaDefinition.Builder<>(objectClass, String.class, String.class, String.class);

        BiConsumer<String, String> create = (value, obj) -> {};
        BiConsumer<String, String> update = (value, obj) -> {};
        Function<String, String> read = s -> "valor-" + s;

        builder.addUid(
                "uidField",
                SchemaDefinition.Types.STRING,
                create,
                update,
                read,
                "fetchUid",
                AttributeInfo.Flags.REQUIRED
        );

        Field field = builder.getClass().getDeclaredField("attributes");
        field.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<?> attributes = (List<?>) field.get(builder);

        assertEquals(1, attributes.size(), "Deveria conter 1 AttributeMapper");
        Object attr = attributes.get(0);
        assertNotNull(attr, "AttributeMapper não deveria ser nulo");

        Field connectorNameField = attr.getClass().getDeclaredField("connectorName");
        connectorNameField.setAccessible(true);
        assertEquals("__UID__", connectorNameField.get(attr));

        Field nameField = attr.getClass().getDeclaredField("name");
        nameField.setAccessible(true);
        assertEquals("uidField", nameField.get(attr));

        Field fetchField = attr.getClass().getDeclaredField("fetchField");
        fetchField.setAccessible(true);
        assertEquals("fetchUid", fetchField.get(attr));
    }

    @Test
    void testAddNameShouldCreateAndAddAttributeMapper() throws Exception {
        ObjectClass objectClass = new ObjectClass("testClass");

        SchemaDefinition.Builder<String, String, String> builder =
                new SchemaDefinition.Builder<>(objectClass, String.class, String.class, String.class);

        BiConsumer<String, String> createOrUpdate = (value, obj) -> {};
        Function<String, String> read = s -> "name-" + s;

        builder.addName(
                "displayName",
                SchemaDefinition.Types.STRING,
                createOrUpdate,
                read,
                "fetchName",
                AttributeInfo.Flags.NOT_UPDATEABLE
        );

        Field field = builder.getClass().getDeclaredField("attributes");
        field.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<?> attributes = (List<?>) field.get(builder);

        assertEquals(1, attributes.size(), "Deveria conter 1 AttributeMapper");
        Object attr = attributes.get(0);
        assertNotNull(attr, "AttributeMapper não deveria ser nulo");

        Field connectorNameField = attr.getClass().getDeclaredField("connectorName");
        connectorNameField.setAccessible(true);
        assertEquals(Name.NAME, connectorNameField.get(attr));

        Field nameField = attr.getClass().getDeclaredField("name");
        nameField.setAccessible(true);
        assertEquals("displayName", nameField.get(attr));

        Field fetchField = attr.getClass().getDeclaredField("fetchField");
        fetchField.setAccessible(true);
        assertEquals("fetchName", fetchField.get(attr));
    }

    static class Dummy {
        public String id;
        public Float attr;
        public String displayName;
        public Double attrDouble;
        public Long attrLong;
        public List<String> attrList;
        public List<String> attrListDatetime;
        public String attrDate;
        public String attrDatetime;
        public String attrJson;
    }

    private static SchemaDefinition getSchemaDefinition(Dummy dummy) {
        ObjectClass objectClass = new ObjectClass("testClass");

        SchemaDefinition.Builder<Dummy, SCIMPatchOperations, Dummy> builder = new SchemaDefinition.Builder<>(objectClass, Dummy.class, SCIMPatchOperations.class, Dummy.class);

        builder.addUid("dummyId",
                SchemaDefinition.Types.UUID,
                null,
                (source) -> source.id,
                "id",
                NOT_CREATABLE, NOT_UPDATEABLE
        );

        builder.addName("displayName",
                SchemaDefinition.Types.STRING_CASE_IGNORE,
                (source, dest) -> dest.displayName = source,
                (source, dest) -> dest.replace("displayName", source),
                (source) -> source.displayName,
                null,
                REQUIRED
        );

        builder.add("attr",
                SchemaDefinition.Types.FLOAT,
                (value, obj) -> obj.attr = value,
                (value, obj) -> obj.replace("attr", String.valueOf(value)),
                (source) -> source.attr,
                "attr"
        );

        builder.add("attrDouble",
                SchemaDefinition.Types.DOUBLE,
                (value, obj) -> obj.attrDouble = value,
                (value, obj) -> obj.replace("attrDouble", String.valueOf(value)),
                (source) -> source.attrDouble,
                "attrDouble"
        );

        builder.add("attrLong",
                SchemaDefinition.Types.LONG,
                (value, obj) -> obj.attrLong = value,
                (value, obj) -> obj.replace("attrLong", String.valueOf(value)),
                (source) -> source.attrLong,
                "attrLong"
        );

        builder.addAsMultiple(
                "attrList",
                SchemaDefinition.Types.DATE_STRING,
                (source, obj) -> obj.attrList = source,
                (add, obj) -> {},
                (remove, obj) -> {},
                (source) -> source.attrList != null ? source.attrList.stream() : null,
                null
        );

        builder.addAsMultiple(
                "attrListDatetime",
                SchemaDefinition.Types.DATETIME_STRING,
                (source, obj) -> obj.attrListDatetime = source,
                (add, obj) -> {},
                (remove, obj) -> {},
                (source) -> source.attrListDatetime != null ? source.attrListDatetime.stream() : null,
                null
        );

        builder.add("attrDate",
                SchemaDefinition.Types.DATE_STRING,
                (value, obj) -> obj.attrDate = value,
                (value, obj) -> obj.replace("attrDate", String.valueOf(value)),
                (source) -> source.attrDate,
                "attrDate"
        );

        builder.add("attrDateTime",
                SchemaDefinition.Types.DATETIME_STRING,
                (value, obj) -> obj.attrDatetime = value,
                (value, obj) -> obj.replace("attrDateTime", String.valueOf(value)),
                (source) -> source.attrDatetime,
                "attrDateTime"
        );

        builder.add("attrJson",
                SchemaDefinition.Types.JSON,
                (value, obj) -> obj.attrJson = value,
                (value, obj) -> obj.replace("attrJson", String.valueOf(value)),
                (source) -> source.attrJson,
                "attrJson"
        );

        return builder.build();
    }

    @Test
    void testSchemaDefinitionApplyForSetOfAttributes() {
        Dummy dummy = new Dummy();
        SchemaDefinition schemaDefinition = getSchemaDefinition(dummy);

        Set<Attribute> attributeSet = new HashSet<>();
        attributeSet.add(AttributeBuilder.build("attr", 20.0f));
        attributeSet.add(AttributeBuilder.build("attrDouble", 20.0));
        attributeSet.add(AttributeBuilder.build("attrLong", 20L));
        attributeSet.add(AttributeBuilder.build("attrList", List.of(toZoneDateTime("2023-02-23"))));
        attributeSet.add(AttributeBuilder.build("attrListDatetime", List.of(toZoneDateTime("2023-02-23"))));
        attributeSet.add(AttributeBuilder.build("attrJson", "{}"));

        schemaDefinition.apply(attributeSet, dummy);

        assertEquals(20, dummy.attr, "Deveria conter o valor 20 no atributo dummy.attr");
        assertEquals(20.0, dummy.attrDouble);
        assertEquals(20L, dummy.attrLong);
        assertEquals(1, dummy.attrList.size());
        assertEquals(1, dummy.attrListDatetime.size());
    }

    @Test
    void testSchemaDefinitionApplyForReadAttributesAndBuildConnectorObject() {
        Dummy dummy = new Dummy();
        dummy.id = "dummyId";
        dummy.displayName = "displayName";
        dummy.attr = 20.0F;
        dummy.attrDouble = 20.0;
        dummy.attrLong = 20L;
        dummy.attrListDatetime = List.of("2023-03-02T02:01:00+01:00");
        dummy.attrList = List.of("2026-08-09");

        Set<String> attrToGet = new HashSet<>();
        attrToGet.add("attr");
        attrToGet.add("attrDouble");
        attrToGet.add("attrLong");
        attrToGet.add("attrListDatetime");
        attrToGet.add("attrList");

        SchemaDefinition schemaDefinition = getSchemaDefinition(dummy);

        ConnectorObjectBuilder connectorObjectBuilder = schemaDefinition.toConnectorObjectBuilder(dummy, attrToGet, false);
        ConnectorObject connectorObject = connectorObjectBuilder.build();

        assertNotNull(connectorObjectBuilder);
        assertNotNull(connectorObject);
    }

    @Test
    void testSchemaDefinitionApplyForReadAttributesTypeDateNull() {
        Dummy dummy = new Dummy();
        dummy.id = "dummyId";
        dummy.displayName = "displayName";
        dummy.attrList = new ArrayList<>();

        Set<String> attrToGet = new HashSet<>();
        attrToGet.add("attrList");

        SchemaDefinition schemaDefinition = getSchemaDefinition(dummy);

        ConnectorObjectBuilder connectorObjectBuilder = schemaDefinition.toConnectorObjectBuilder(dummy, attrToGet, false);
        ConnectorObject connectorObject = connectorObjectBuilder.build();

        assertNotNull(connectorObjectBuilder);
        assertNotNull(connectorObject);
    }

    @Test
    void testSchemaDefinitionApplyForSetOfAttributeDeltasWithValueToAdd() {
        Dummy dummy = new Dummy();
        SchemaDefinition schemaDefinition = getSchemaDefinition(dummy);

        Set<AttributeDelta> deltas = new HashSet<>();

        AttributeDeltaBuilder delta1 = new AttributeDeltaBuilder();
        delta1.setName("attrList");
        delta1.addValueToAdd(List.of(toZoneDateTime("2023-02-23")));
        deltas.add(delta1.build());

        SCIMPatchOperations dest = new SCIMPatchOperations();

        schemaDefinition.applyDelta(deltas, dest);

    }

    @Test
    void testSchemaDefinitionApplyForSetOfAttributeDeltasWithValueToRemove() {
        Dummy dummy = new Dummy();
        SchemaDefinition schemaDefinition = getSchemaDefinition(dummy);

        Set<AttributeDelta> deltas = new HashSet<>();

        AttributeDeltaBuilder delta1 = new AttributeDeltaBuilder();
        delta1.setName("attrList");
        delta1.addValueToRemove(List.of(toZoneDateTime("2023-02-23")));
        deltas.add(delta1.build());

        SCIMPatchOperations dest = new SCIMPatchOperations();

        schemaDefinition.applyDelta(deltas, dest);

    }

    @Test
    void testSchemaDefinitionApplyForDatetimeStringWithSetOfAttributeDeltasWithValueToAdd() {
        Dummy dummy = new Dummy();
        SchemaDefinition schemaDefinition = getSchemaDefinition(dummy);
        Set<AttributeDelta> deltas = new HashSet<>();

        AttributeDeltaBuilder delta1 = new AttributeDeltaBuilder();
        delta1.setName("attrListDatetime");
        delta1.addValueToAdd(List.of(toZoneDateTime("2023-02-23")));
        deltas.add(delta1.build());

        SCIMPatchOperations dest = new SCIMPatchOperations();

        schemaDefinition.applyDelta(deltas, dest);

    }

    @Test
    void testSchemaDefinitionApplyForDatetimeStringWithSetOfAttributeDeltasWithValueToRemove() {
        Dummy dummy = new Dummy();

        SchemaDefinition schemaDefinition = getSchemaDefinition(dummy);
        Set<AttributeDelta> deltas = new HashSet<>();

        AttributeDeltaBuilder delta1 = new AttributeDeltaBuilder();
        delta1.setName("attrListDatetime");
        delta1.addValueToRemove(List.of(toZoneDateTime("2023-02-23")));
        deltas.add(delta1.build());

        SCIMPatchOperations dest = new SCIMPatchOperations();

        schemaDefinition.applyDelta(deltas, dest);

    }

    @Test
    void testSchemaDefinitionApplyForDoubleLongFloatWithSetOfAttributeDeltasWithValueToReplace() {
        Dummy dummy = new Dummy();
        SchemaDefinition schemaDefinition = getSchemaDefinition(dummy);
        Set<AttributeDelta> deltas = new HashSet<>();

        AttributeDeltaBuilder delta1 = new AttributeDeltaBuilder();
        delta1.setName("attrDouble");
        delta1.addValueToReplace(20.0);
        deltas.add(delta1.build());

        AttributeDeltaBuilder delta2 = new AttributeDeltaBuilder();
        delta2.setName("attr");
        delta2.addValueToReplace(20.0F);
        deltas.add(delta2.build());

        AttributeDeltaBuilder delta3 = new AttributeDeltaBuilder();
        delta3.setName("attrLong");
        delta3.addValueToReplace(20L);
        deltas.add(delta3.build());

        SCIMPatchOperations dest = new SCIMPatchOperations();

        schemaDefinition.applyDelta(deltas, dest);

    }

    @Test
    void testAddEnableShouldCreateAndAddAttributeMapper() throws Exception {
        ObjectClass objectClass = new ObjectClass("testClass");

        SchemaDefinition.Builder<String, String, String> builder =
                new SchemaDefinition.Builder<>(objectClass, String.class, String.class, String.class);

        BiConsumer<String, String> create = (value, obj) -> {};
        BiConsumer<String, String> update = (value, obj) -> {};
        Function<String, String> read = s -> "enabled-" + s;

        builder.addEnable(
                "enabledFlag",
                SchemaDefinition.Types.STRING,
                create,
                update,
                read,
                "fetchEnable",
                AttributeInfo.Flags.NOT_CREATABLE
        );


        Field field = builder.getClass().getDeclaredField("attributes");
        field.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<?> attributes = (List<?>) field.get(builder);

        assertEquals(1, attributes.size(), "Deveria conter 1 AttributeMapper");
        Object attr = attributes.get(0);
        assertNotNull(attr, "AttributeMapper não deveria ser nulo");

        Field connectorNameField = attr.getClass().getDeclaredField("connectorName");
        connectorNameField.setAccessible(true);
        assertEquals(OperationalAttributes.ENABLE_NAME, connectorNameField.get(attr));

        Field nameField = attr.getClass().getDeclaredField("name");
        nameField.setAccessible(true);
        assertEquals("enabledFlag", nameField.get(attr));

        Field fetchField = attr.getClass().getDeclaredField("fetchField");
        fetchField.setAccessible(true);
        assertEquals("fetchEnable", fetchField.get(attr));
    }

    @Test
    void testBuildSchemaInfo_UUID() throws Exception {
        ObjectClassInfo info = buildForType(SchemaDefinition.Types.UUID, AttributeInfo.Flags.REQUIRED);
        assertNotNull(info);
    }

    @Test
    void testBuildSchemaInfo_STRING_CASE_IGNORE() throws Exception {
        ObjectClassInfo info = buildForType(SchemaDefinition.Types.STRING_CASE_IGNORE, AttributeInfo.Flags.NOT_CREATABLE);
        assertNotNull(info);
    }

    @Test
    void testBuildSchemaInfo_STRING_URI() throws Exception {
        ObjectClassInfo info = buildForType(SchemaDefinition.Types.STRING_URI, AttributeInfo.Flags.NOT_UPDATEABLE);
        assertNotNull(info);
    }

    @Test
    void testBuildSchemaInfo_STRING_LDAP_DN() throws Exception {
        ObjectClassInfo info = buildForType(SchemaDefinition.Types.STRING_LDAP_DN,
                AttributeInfo.Flags.NOT_READABLE, AttributeInfo.Flags.NOT_RETURNED_BY_DEFAULT);
        assertNotNull(info);
    }

    @Test
    void testBuildSchemaInfo_XML() throws Exception {
        ObjectClassInfo info = buildForType(SchemaDefinition.Types.XML, AttributeInfo.Flags.NOT_RETURNED_BY_DEFAULT);
        assertNotNull(info);
    }

    @Test
    void testBuildSchemaInfo_JSON() throws Exception {
        ObjectClassInfo info = buildForType(SchemaDefinition.Types.JSON);
        assertNotNull(info);
    }

    @SuppressWarnings("unchecked")
    private ObjectClassInfo buildForType(SchemaDefinition.Types<?> type, AttributeInfo.Flags... flags) throws Exception {
        ObjectClass objectClass = new ObjectClass("testClass");
        SchemaDefinition.Builder<String, String, String> builder =
                new SchemaDefinition.Builder<>(objectClass, String.class, String.class, String.class);

        Field field = builder.getClass().getDeclaredField("attributes");
        field.setAccessible(true);
        List<Object> attributes = (List<Object>) field.get(builder);

        Constructor<?> ctor = Class.forName("jp.openstandia.connector.util.SchemaDefinition$AttributeMapper").getDeclaredConstructor(
                String.class, String.class, SchemaDefinition.Types.class,
                BiConsumer.class, BiConsumer.class,
                Function.class, String.class, AttributeInfo.Flags[].class
        );
        ctor.setAccessible(true);

        attributes.add(ctor.newInstance(
                "attr_" + type.hashCode(),
                "attr_" + type.hashCode(),
                type,
                null, null, null,
                "fetch_" + type.hashCode(),
                flags.length == 0 ? new AttributeInfo.Flags[]{} : flags
        ));

        Method method = builder.getClass().getDeclaredMethod("buildSchemaInfo");
        method.setAccessible(true);
        return (ObjectClassInfo) method.invoke(builder);
    }
}

