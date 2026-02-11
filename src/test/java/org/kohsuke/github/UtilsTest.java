package org.kohsuke.github;

import jp.openstandia.connector.util.SchemaDefinition;
import jp.openstandia.connector.util.Utils;
import org.identityconnectors.framework.common.objects.*;
import org.junit.jupiter.api.Test;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class UtilsTest {

    static class Dummy {
        String id;
        String name;
        String defaultAttr;
        String notReturnedByDefault;
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

        return b.build();
    }

    @Test
    void toZoneDateTimeAndVariantsCoverNullAndConversions() {
        ZoneId sys = ZoneId.systemDefault();

        assertNull(Utils.toZoneDateTime((String) null));
        ZonedDateTime z1 = Utils.toZoneDateTime("2024-01-02");
        assertEquals(LocalDate.parse("2024-01-02").atStartOfDay(sys), z1);

        assertNull(Utils.toZoneDateTime(DateTimeFormatter.ISO_INSTANT, null));
        ZonedDateTime z2 = Utils.toZoneDateTime(DateTimeFormatter.ISO_INSTANT, "2024-01-01T00:00:00Z");
        assertEquals(Instant.parse("2024-01-01T00:00:00Z"), z2.toInstant());

        assertNull(Utils.toZoneDateTimeForEpochMilli(null));
        ZonedDateTime z3 = Utils.toZoneDateTimeForEpochMilli("0");
        assertEquals(Instant.EPOCH, z3.toInstant());

        assertNull(Utils.toZoneDateTimeForISO8601OffsetDateTime(null));
        ZonedDateTime z4 = Utils.toZoneDateTimeForISO8601OffsetDateTime("2024-01-01T10:20:30+02:00");
        assertEquals(OffsetDateTime.parse("2024-01-01T10:20:30+02:00").toInstant(), z4.toInstant());

        assertNull(Utils.toZoneDateTime((Date) null));
        Date d = Date.from(Instant.parse("2024-01-03T12:00:00Z"));
        ZonedDateTime z5 = Utils.toZoneDateTime(d);
        assertEquals(d.toInstant(), z5.toInstant());
    }

    @Test
    void shouldReturnOverloadsCoverNullAndContainsBranches() {
        assertTrue(Utils.shouldReturn(null, "a", true));
        assertFalse(Utils.shouldReturn(null, "a", false));

        Set<String> set = new HashSet<>(Arrays.asList("x", "y"));
        assertTrue(Utils.shouldReturn(set, "x", false));
        assertFalse(Utils.shouldReturn(set, "z", true));

        assertTrue(Utils.shouldReturn(set, "y"));
        assertFalse(Utils.shouldReturn(set, "nope"));
    }

    @Test
    void createIncompleteAttributeSetsNameCompletenessAndEmptyListValue() {
        Attribute a = Utils.createIncompleteAttribute("attr1");
        assertEquals("attr1", a.getName());
        assertEquals(AttributeValueCompleteness.INCOMPLETE, a.getAttributeValueCompleteness());

        assertNotNull(a.getValue());
        assertEquals(0, a.getValue().size());
        assertTrue(((List<?>) a.getValue()).isEmpty());
    }

    @Test
    void shouldAllowPartialAndReturnDefaultAttributesCoverTrueFalseNull() {
        OperationOptions allowTrue = new OperationOptionsBuilder()
                .setAllowPartialAttributeValues(true)
                .build();
        assertTrue(Utils.shouldAllowPartialAttributeValues(allowTrue));

        OperationOptions allowFalse = new OperationOptionsBuilder()
                .setAllowPartialAttributeValues(false)
                .build();
        assertFalse(Utils.shouldAllowPartialAttributeValues(allowFalse));

        OperationOptions allowNull = new OperationOptionsBuilder().build();
        assertFalse(Utils.shouldAllowPartialAttributeValues(allowNull));

        OperationOptions defTrue = new OperationOptionsBuilder()
                .setReturnDefaultAttributes(true)
                .build();
        assertTrue(Utils.shouldReturnDefaultAttributes(defTrue));

        OperationOptions defFalse = new OperationOptionsBuilder()
                .setReturnDefaultAttributes(false)
                .build();
        assertFalse(Utils.shouldReturnDefaultAttributes(defFalse));

        OperationOptions defNull = new OperationOptionsBuilder().build();
        assertFalse(Utils.shouldReturnDefaultAttributes(defNull));
    }

    @Test
    void createFullAttributesToGetWhenReturnDefaultTrueAddsDefaultsPlusKnownAttrsIgnoresUnknown() {
        SchemaDefinition schema = buildSchema();

        OperationOptions options = new OperationOptionsBuilder()
                .setReturnDefaultAttributes(true)
                .setAttributesToGet("notReturnedByDefault", "unknownAttr")
                .build();

        Map<String, String> m = Utils.createFullAttributesToGet(schema, options);

        assertTrue(m.containsKey(Uid.NAME));
        assertTrue(m.containsKey(Name.NAME));
        assertTrue(m.containsKey("defaultAttr"));

        assertEquals("notReturnedByDefault", m.get("notReturnedByDefault"));

        assertFalse(m.containsKey("unknownAttr"));
    }

    @Test
    void createFullAttributesToGetWhenAttrsToGetNullAndReturnDefaultNullDefaultsToReturnedByDefault() {
        SchemaDefinition schema = buildSchema();

        OperationOptions options = new OperationOptionsBuilder().build();

        Map<String, String> m = Utils.createFullAttributesToGet(schema, options);

        assertTrue(m.containsKey(Uid.NAME));
        assertTrue(m.containsKey(Name.NAME));
        assertTrue(m.containsKey("defaultAttr"));

        assertFalse(m.containsKey("notReturnedByDefault"));
    }

    @Test
    void createFullAttributesToGetWhenReturnDefaultFalseOnlyUsesAttrsToGet() {
        SchemaDefinition schema = buildSchema();

        OperationOptions options = new OperationOptionsBuilder()
                .setReturnDefaultAttributes(false)
                .setAttributesToGet("defaultAttr")
                .build();

        Map<String, String> m = Utils.createFullAttributesToGet(schema, options);

        assertFalse(m.containsKey(Uid.NAME));
        assertFalse(m.containsKey(Name.NAME));

        assertEquals("defaultAttr", m.get("defaultAttr"));
    }

    @Test
    void resolvePageSizeAndOffsetCoverBothBranches() {
        OperationOptions withValues = new OperationOptionsBuilder()
                .setPageSize(25)
                .setPagedResultsOffset(3)
                .build();

        assertEquals(25, Utils.resolvePageSize(withValues, 10));
        assertEquals(3, Utils.resolvePageOffset(withValues));

        OperationOptions noValues = new OperationOptionsBuilder().build();
        assertEquals(10, Utils.resolvePageSize(noValues, 10));
        assertEquals(0, Utils.resolvePageOffset(noValues));
    }

    @Test
    void handleEmptyAsNullAndHandleNullAsEmptyCoverAllBranches() {
        assertNull(Utils.handleEmptyAsNull(null));
        assertNull(Utils.handleEmptyAsNull(""));
        assertEquals("x", Utils.handleEmptyAsNull("x"));

        assertEquals("", Utils.handleNullAsEmpty(null));
        assertEquals("y", Utils.handleNullAsEmpty("y"));
    }
}

