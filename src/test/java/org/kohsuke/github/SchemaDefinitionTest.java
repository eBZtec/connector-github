package org.kohsuke.github;

import jp.openstandia.connector.util.SchemaDefinition;
import org.identityconnectors.framework.common.exceptions.InvalidAttributeValueException;
import org.identityconnectors.framework.common.objects.*;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class SchemaDefinitionTest {

    static class Dummy {
        String id;
        String name;
        String defaultAttr;
        String notReturnedByDefault;
        String notReadable;
        String nullRead;
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
                "name",
                AttributeInfo.Flags.REQUIRED);

        b.add("defaultAttr", SchemaDefinition.Types.STRING,
                (val, dest) -> dest.defaultAttr = val,
                (src) -> src.defaultAttr,
                "defaultAttr");

        b.add("notReturnedByDefault", SchemaDefinition.Types.STRING,
                (val, dest) -> dest.notReturnedByDefault = val,
                (src) -> src.notReturnedByDefault,
                "notReturnedByDefault",
                AttributeInfo.Flags.NOT_RETURNED_BY_DEFAULT);

        b.add("notReadable", SchemaDefinition.Types.STRING,
                (val, dest) -> dest.notReadable = val,
                (src) -> src.notReadable,
                "notReadable",
                AttributeInfo.Flags.NOT_READABLE, AttributeInfo.Flags.NOT_RETURNED_BY_DEFAULT);

        b.add("nullRead", SchemaDefinition.Types.STRING,
                (val, dest) -> dest.nullRead = val,
                (src) -> null,
                "nullRead");

        return b.build();
    }

    @Test
    void getReturnedByDefaultAttributesSetAndIsReturnedByDefaultAttribute() {
        SchemaDefinition schema = buildSchema();

        Map<String, String> returned = schema.getReturnedByDefaultAttributesSet();

        assertTrue(returned.containsKey(Uid.NAME));
        assertTrue(returned.containsKey(Name.NAME));
        assertTrue(returned.containsKey("defaultAttr"));
        assertTrue(returned.containsKey("nullRead"));
        assertFalse(returned.containsKey("notReturnedByDefault"));

        assertTrue(schema.isReturnedByDefaultAttribute("defaultAttr"));
        assertFalse(schema.isReturnedByDefaultAttribute("notReturnedByDefault"));
        assertFalse(schema.isReturnedByDefaultAttribute("norReadable"));
    }

    @Test
    void isReadableAttributes() {
        SchemaDefinition schema = buildSchema();

        assertTrue(schema.isReadableAttributes("defaultAttr"));
        assertFalse(schema.isReadableAttributes("notReadable"));

        assertTrue(schema.isReadableAttributes("unknown"));
    }

    @Test
    void applyAppliesAllAttributesAndThrowsOnInvalidAttribute() {
        SchemaDefinition schema = buildSchema();

        Dummy dest = new Dummy();
        Set<Attribute> attrs = new HashSet<>();
        attrs.add(AttributeBuilder.build(Uid.NAME, "u-1"));
        attrs.add(AttributeBuilder.build(Name.NAME, "n-1"));
        attrs.add(AttributeBuilder.build("defaultAttr", "v1"));
        attrs.add(AttributeBuilder.build("notReturnedByDefault", "v2"));
        attrs.add(AttributeBuilder.build("notReadable", "v3"));

        Dummy out = schema.apply(attrs, dest);
        assertSame(dest, out);

        assertEquals("u-1", dest.id);
        assertEquals("n-1", dest.name);
        assertEquals("v1", dest.defaultAttr);
        assertEquals("v2", dest.notReturnedByDefault);
        assertEquals("v3", dest.notReadable);

        InvalidAttributeValueException ex = assertThrows(
                InvalidAttributeValueException.class,
                () -> schema.apply(Set.of(AttributeBuilder.build("doesNotExist", "x")), new Dummy()));
        assertTrue(ex.getMessage().contains("Invalid attribute"));
    }

    @Test
    void applyDeltaReturnsChangedFlagAndThrowsOnInvalidDelta() {
        SchemaDefinition schema = buildSchema();

        assertFalse(schema.applyDelta(Collections.emptySet(), new Dummy()));

        Dummy dest = new Dummy();
        Set<AttributeDelta> deltas = Set.of(
                AttributeDeltaBuilder.build("defaultAttr", "dv")
        );

        assertTrue(schema.applyDelta(deltas, dest));
        assertEquals("dv", dest.defaultAttr);

        assertThrows(InvalidAttributeValueException.class,
                () -> schema.applyDelta(Set.of(AttributeDeltaBuilder.build("bad", "x")), new Dummy()));
    }

    @Test
    void toConnectorObjectBuilderReturnsIncompleteAttributesWhenAllowed() {
        SchemaDefinition schema = buildSchema();

        Dummy src = new Dummy();
        src.id = "id-1";
        src.name = "name-1";
        src.defaultAttr = "d";
        src.notReturnedByDefault = "nr";
        src.notReadable = "x";
        src.nullRead = "ignored";

        Set<String> attrsToGet = new HashSet<>(Arrays.asList(
                Uid.NAME, Name.NAME,
                "defaultAttr", "notReturnedByDefault", "notReadable", "nullRead"));

        ConnectorObject co = schema.toConnectorObjectBuilder(src, attrsToGet, true).build();

        assertEquals("id-1", co.getUid().getUidValue());
        assertEquals("name-1", co.getName().getNameValue());

        assertEquals("d", AttributeUtil.getStringValue(co.getAttributeByName("defaultAttr")));

        Attribute notReturned = co.getAttributeByName("notReturnedByDefault");
        assertNotNull(notReturned);
        assertEquals(AttributeValueCompleteness.INCOMPLETE, notReturned.getAttributeValueCompleteness());

        assertEquals("x", AttributeUtil.getStringValue(co.getAttributeByName("notReadable")));

        assertNull(co.getAttributeByName("nullRead"));
    }

    @Test
    void toConnectorObjectBuilderReturnsActualValuesWhenPartialNotAllowed() {
        SchemaDefinition schema = buildSchema();

        Dummy src = new Dummy();
        src.id = "id-2";
        src.name = "name-2";
        src.defaultAttr = "d2";
        src.notReturnedByDefault = "nr2";

        Set<String> attrsToGet = new HashSet<>(Arrays.asList(
                Uid.NAME, Name.NAME,
                "defaultAttr", "notReturnedByDefault"));

        ConnectorObject co = schema.toConnectorObjectBuilder(src, attrsToGet, false).build();

        assertEquals("id-2", co.getUid().getUidValue());
        assertEquals("name-2", co.getName().getNameValue());
        assertEquals("d2", AttributeUtil.getStringValue(co.getAttributeByName("defaultAttr")));

        Attribute notReturned = co.getAttributeByName("notReturnedByDefault");
        assertNotNull(notReturned);
        assertEquals("nr2", AttributeUtil.getStringValue(notReturned));
        assertEquals(AttributeValueCompleteness.COMPLETE, notReturned.getAttributeValueCompleteness());
    }
}
