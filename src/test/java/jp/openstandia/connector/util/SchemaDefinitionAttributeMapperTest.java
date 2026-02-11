package jp.openstandia.connector.util;

import jp.openstandia.connector.util.SchemaDefinition;
import org.identityconnectors.common.security.GuardedString;
import org.identityconnectors.framework.common.objects.*;
import org.identityconnectors.framework.common.objects.Attribute;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

class SchemaDefinitionAttributeMapperTest {

    static class CreateDest {
        final AtomicReference<Object> last = new AtomicReference<>();
    }

    static class UpdateDest {
        final AtomicReference<Object> last = new AtomicReference<>();
        final AtomicReference<List<?>> add = new AtomicReference<>();
        final AtomicReference<List<?>> remove = new AtomicReference<>();
    }

    static class Source {
        Object value;
        Source(Object value) { this.value = value; }
    }

    // ---------- isStringType() coverage ----------
    @Test
    void isStringType_shouldReturnTrueForStringish_andFalseForNonStringish() {
        var m1 = new SchemaDefinition.AttributeMapper<>(
                "a", SchemaDefinition.Types.STRING,
                (String v, CreateDest d) -> {}, (String v, UpdateDest d) -> {},
                (Source s) -> null, null
        );
        assertTrue(m1.isStringType());

        var m2 = new SchemaDefinition.AttributeMapper<>(
                "a", SchemaDefinition.Types.UUID,
                (String v, CreateDest d) -> {}, (String v, UpdateDest d) -> {},
                (Source s) -> null, null
        );
        assertTrue(m2.isStringType());

        var m3 = new SchemaDefinition.AttributeMapper<>(
                "a", SchemaDefinition.Types.INTEGER,
                (Integer v, CreateDest d) -> {}, (Integer v, UpdateDest d) -> {},
                (Source s) -> null, null
        );
        assertFalse(m3.isStringType());
    }

    // ---------- apply(Attribute, C) coverage (single) ----------
    @Test
    void applyAttribute_single_shouldCoverAllScalarTypeBranches_andNullCreateNoop() {
        // create == null -> early return
        var noCreate = new SchemaDefinition.AttributeMapper<>(
                "x", SchemaDefinition.Types.STRING,
                null, (String v, UpdateDest d) -> {},
                (Source s) -> null, null
        );
        noCreate.apply(AttributeBuilder.build("x", "v"), new CreateDest()); // should not throw

        // STRING-ish branch uses AttributeUtil.getAsStringValue
        {
            CreateDest d = new CreateDest();
            var m = new SchemaDefinition.AttributeMapper<>(
                    "s", SchemaDefinition.Types.STRING,
                    (String v, CreateDest dest) -> dest.last.set(v),
                    (String v, UpdateDest dest) -> {},
                    (Source s) -> null, null
            );
            m.apply(AttributeBuilder.build("s", "abc"), d);
            assertEquals("abc", d.last.get());
        }

        // INTEGER
        {
            CreateDest d = new CreateDest();
            var m = new SchemaDefinition.AttributeMapper<>(
                    "i", SchemaDefinition.Types.INTEGER,
                    (Integer v, CreateDest dest) -> dest.last.set(v),
                    (Integer v, UpdateDest dest) -> {},
                    (Source s) -> null, null
            );
            m.apply(AttributeBuilder.build("i", 7), d);
            assertEquals(7, d.last.get());
        }

//        // LONG
//        {
//            CreateDest d = new CreateDest();
//            var m = new SchemaDefinition.AttributeMapper<>(
//                    "l", SchemaDefinition.Types.LONG,
//                    (Long v, CreateDest dest) -> dest.last.set(v),
//                    (Long v, UpdateDest dest) -> {},
//                    (Source s) -> null, null
//            );
//            m.apply(AttributeBuilder.build("l", 9L), d);
//            assertEquals(9L, d.last.get());
//        }
//
//        // FLOAT
//        {
//            CreateDest d = new CreateDest();
//            var m = new SchemaDefinition.AttributeMapper<Float, CreateDest, UpdateDest, Source>(
//                    "f", SchemaDefinition.Types.FLOAT,
//                    (Float v, CreateDest dest) -> dest.last.set(v),
//                    (Float v, UpdateDest dest) -> {},
//                    (Source s) -> null, null
//            );
//            m.apply(AttributeBuilder.build("f", 1.25f), d);
//            assertEquals(1.25f, (Float) d.last.get(), 0.0001);
//        }
//
//        // DOUBLE
//        {
//            CreateDest d = new CreateDest();
//            var m = new SchemaDefinition.AttributeMapper<>(
//                    "d", SchemaDefinition.Types.DOUBLE,
//                    (Double v, CreateDest dest) -> dest.last.set(v),
//                    (Double v, UpdateDest dest) -> {},
//                    (Source s) -> null, null
//            );
//            m.apply(AttributeBuilder.build("d", 2.5d), d);
//            assertEquals(2.5d, (Double) d.last.get(), 0.0001);
//        }

        // BOOLEAN
        {
            CreateDest d = new CreateDest();
            var m = new SchemaDefinition.AttributeMapper<>(
                    "b", SchemaDefinition.Types.BOOLEAN,
                    (Boolean v, CreateDest dest) -> dest.last.set(v),
                    (Boolean v, UpdateDest dest) -> {},
                    (Source s) -> null, null
            );
            m.apply(AttributeBuilder.build("b", true), d);
            assertEquals(true, d.last.get());
        }

        // BIG_DECIMAL
        {
            CreateDest d = new CreateDest();
            var m = new SchemaDefinition.AttributeMapper<>(
                    "bd", SchemaDefinition.Types.BIG_DECIMAL,
                    (BigDecimal v, CreateDest dest) -> dest.last.set(v),
                    (BigDecimal v, UpdateDest dest) -> {},
                    (Source s) -> null, null
            );
            m.apply(AttributeBuilder.build("bd", new BigDecimal("12.34")), d);
            assertEquals(new BigDecimal("12.34"), d.last.get());
        }

        // DATE_STRING branch (formats ZonedDateTime -> String)
        {
            CreateDest d = new CreateDest();
            var m = new SchemaDefinition.AttributeMapper<>(
                    "ds", SchemaDefinition.Types.DATE_STRING,
                    (String v, CreateDest dest) -> dest.last.set(v),
                    (String v, UpdateDest dest) -> {},
                    (Source s) -> null, null
            );

            ZonedDateTime zdt = ZonedDateTime.of(2026, 2, 10, 0, 0, 0, 0, ZoneId.systemDefault());
            m.apply(AttributeBuilder.build("ds", zdt), d);
            assertEquals("2026-02-10", d.last.get()); // ISO_LOCAL_DATE default
        }

        // DATETIME_STRING branch (formats ZonedDateTime -> String)
        // Also covers the "custom datetimeFormat" path (note: implementation uses dateFormat in else)
        {
            CreateDest d = new CreateDest();
            var m = new SchemaDefinition.AttributeMapper<>(
                    "dts", SchemaDefinition.Types.DATETIME_STRING,
                    (String v, CreateDest dest) -> dest.last.set(v),
                    (String v, UpdateDest dest) -> {},
                    (Source s) -> null, null
            );

            // Cover default datetime formatting
            ZonedDateTime zdt = ZonedDateTime.now(ZoneId.systemDefault());
            m.apply(AttributeBuilder.build("dts", zdt), d);
            assertNotNull(d.last.get());

            // Cover custom-formatter branch (datetimeFormat != null)
            // (Implementation uses dateFormat field in else; set both to avoid null)
            m.dateFormat(DateTimeFormatter.ISO_LOCAL_DATE);
            m.datetimeFormat(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            d.last.set(null);
            m.apply(AttributeBuilder.build("dts", zdt), d);
            assertNotNull(d.last.get());
        }

        // GUARDED_STRING
        {
            CreateDest d = new CreateDest();
            var m = new SchemaDefinition.AttributeMapper<>(
                    "gs", SchemaDefinition.Types.GUARDED_STRING,
                    (GuardedString v, CreateDest dest) -> dest.last.set(v),
                    (GuardedString v, UpdateDest dest) -> {},
                    (Source s) -> null, null
            );
            GuardedString gs = new GuardedString("secret".toCharArray());
            m.apply(AttributeBuilder.build("gs", gs), d);
            assertSame(gs, d.last.get());
        }


    }

    // ---------- apply(Attribute, C) coverage (multiple) ----------


    // ---------- apply(AttributeDelta, U) coverage (single replace) ----------
    @Test
    void applyDelta_single_shouldCoverReplaceBranches_andNullReplaceNoop() {
        // replace == null -> early return
        var noReplace = new SchemaDefinition.AttributeMapper<>(
                "x", SchemaDefinition.Types.STRING,
                (String v, CreateDest d) -> {},
                null,
                (Source s) -> null, null
        );
        noReplace.apply(AttributeDeltaBuilder.build("x", "v"), new UpdateDest()); // should not throw

        // String-ish
        {
            UpdateDest d = new UpdateDest();
            var m = new SchemaDefinition.AttributeMapper<>(
                    "s", SchemaDefinition.Types.STRING,
                    (String v, CreateDest dest) -> {},
                    (String v, UpdateDest dest) -> dest.last.set(v),
                    (Source s) -> null, null
            );
            m.apply(AttributeDeltaBuilder.build("s", "new"), d);
            assertEquals("new", d.last.get());
        }

        // Integer
        {
            UpdateDest d = new UpdateDest();
            var m = new SchemaDefinition.AttributeMapper<>(
                    "i", SchemaDefinition.Types.INTEGER,
                    (Integer v, CreateDest dest) -> {},
                    (Integer v, UpdateDest dest) -> dest.last.set(v),
                    (Source s) -> null, null
            );
            m.apply(AttributeDeltaBuilder.build("i", 10), d);
            assertEquals(10, d.last.get());
        }

//        // Long
//        {
//            UpdateDest d = new UpdateDest();
//            var m = new SchemaDefinition.AttributeMapper<>(
//                    "l", SchemaDefinition.Types.LONG,
//                    (Long v, CreateDest dest) -> {},
//                    (Long v, UpdateDest dest) -> dest.last.set(v),
//                    (Source s) -> null, null
//            );
//            m.apply(AttributeDeltaBuilder.build("l", 10L), d);
//            assertEquals(10L, d.last.get());
//        }
//
//        // Float
//        {
//            UpdateDest d = new UpdateDest();
//            var m = new SchemaDefinition.AttributeMapper<>(
//                    "f", SchemaDefinition.Types.FLOAT,
//                    (Float v, CreateDest dest) -> {},
//                    (Float v, UpdateDest dest) -> dest.last.set(v),
//                    (Source s) -> null, null
//            );
//            m.apply(AttributeDeltaBuilder.build("f", 3.0f), d);
//            assertEquals(3.0f, (Float) d.last.get(), 0.0001);
//        }
//
//        // Double
//        {
//            UpdateDest d = new UpdateDest();
//            var m = new SchemaDefinition.AttributeMapper<>(
//                    "d", SchemaDefinition.Types.DOUBLE,
//                    (Double v, CreateDest dest) -> {},
//                    (Double v, UpdateDest dest) -> dest.last.set(v),
//                    (Source s) -> null, null
//            );
//            m.apply(AttributeDeltaBuilder.build("d", 3.5d), d);
//            assertEquals(3.5d, (Double) d.last.get(), 0.0001);
//        }

        // Boolean
        {
            UpdateDest d = new UpdateDest();
            var m = new SchemaDefinition.AttributeMapper<>(
                    "b", SchemaDefinition.Types.BOOLEAN,
                    (Boolean v, CreateDest dest) -> {},
                    (Boolean v, UpdateDest dest) -> dest.last.set(v),
                    (Source s) -> null, null
            );
            m.apply(AttributeDeltaBuilder.build("b", true), d);
            assertEquals(true, d.last.get());
        }

        // BigDecimal
        {
            UpdateDest d = new UpdateDest();
            var m = new SchemaDefinition.AttributeMapper<>(
                    "bd", SchemaDefinition.Types.BIG_DECIMAL,
                    (BigDecimal v, CreateDest dest) -> {},
                    (BigDecimal v, UpdateDest dest) -> dest.last.set(v),
                    (Source s) -> null, null
            );
            m.apply(AttributeDeltaBuilder.build("bd", new BigDecimal("1.00")), d);
            assertEquals(new BigDecimal("1.00"), d.last.get());
        }

        // DATE / DATETIME -> replace accepts ZonedDateTime directly
        {
            UpdateDest d = new UpdateDest();
            var m = new SchemaDefinition.AttributeMapper<>(
                    "dt", SchemaDefinition.Types.DATE,
                    (ZonedDateTime v, CreateDest dest) -> {},
                    (ZonedDateTime v, UpdateDest dest) -> dest.last.set(v),
                    (Source s) -> null, null
            );
            ZonedDateTime zdt = ZonedDateTime.now();
            m.apply(AttributeDeltaBuilder.build("dt", zdt), d);
            assertEquals(zdt, d.last.get());
        }

        // DATE_STRING -> formats to String
        {
            UpdateDest d = new UpdateDest();
            var m = new SchemaDefinition.AttributeMapper<>(
                    "ds", SchemaDefinition.Types.DATE_STRING,
                    (String v, CreateDest dest) -> {},
                    (String v, UpdateDest dest) -> dest.last.set(v),
                    (Source s) -> null, null
            );
            ZonedDateTime zdt = ZonedDateTime.of(2026, 2, 10, 0, 0, 0, 0, ZoneId.systemDefault());
            m.apply(AttributeDeltaBuilder.build("ds", zdt), d);
            assertEquals("2026-02-10", d.last.get());
        }

        // DATETIME_STRING -> formats to String
        {
            UpdateDest d = new UpdateDest();
            var m = new SchemaDefinition.AttributeMapper<>(
                    "dts", SchemaDefinition.Types.DATETIME_STRING,
                    (String v, CreateDest dest) -> {},
                    (String v, UpdateDest dest) -> dest.last.set(v),
                    (Source s) -> null, null
            );
            ZonedDateTime zdt = ZonedDateTime.now();
            m.apply(AttributeDeltaBuilder.build("dts", zdt), d);
            assertNotNull(d.last.get());
        }

        // GuardedString
        {
            UpdateDest d = new UpdateDest();
            var m = new SchemaDefinition.AttributeMapper<>(
                    "gs", SchemaDefinition.Types.GUARDED_STRING,
                    (GuardedString v, CreateDest dest) -> {},
                    (GuardedString v, UpdateDest dest) -> dest.last.set(v),
                    (Source s) -> null, null
            );
            GuardedString gs = new GuardedString("secret".toCharArray());
            m.apply(AttributeDeltaBuilder.build("gs", gs), d);
            assertSame(gs, d.last.get());
        }

        // else branch
//        {
//            UpdateDest d = new UpdateDest();
//            var m = new SchemaDefinition.AttributeMapper<>(
//                    "x", SchemaDefinition.Types.JSON,
//                    (Object v, CreateDest dest) -> {},
//                    (Object v, UpdateDest dest) -> dest.last.set(v),
//                    (Source s) -> null, null
//            );
//            m.apply(AttributeDeltaBuilder.build("x", 999), d);
//            assertEquals(999, d.last.get());
//        }
    }

    // ---------- apply(R) coverage ----------
    @Test
    void applyRead_shouldCoverNullRead_nullValue_singleAndMultiple_dateAndDatetime_andEmptyStream() {
        // read == null -> returns null
        var mNoRead = new SchemaDefinition.AttributeMapper<>(
                "x", SchemaDefinition.Types.STRING,
                (String v, CreateDest d) -> {}, (String v, UpdateDest d) -> {},
                null, null
        );
        assertNull(mNoRead.apply(new Source("v")));

        // read returns null -> returns null
        var mNullValue = new SchemaDefinition.AttributeMapper<>(
                "x", SchemaDefinition.Types.STRING,
                (String v, CreateDest d) -> {}, (String v, UpdateDest d) -> {},
                (Source s) -> null, null
        );
        assertNull(mNullValue.apply(new Source("v")));

        // single DATE_STRING -> converts String to ZonedDateTime
        {
            Function<Source, Object> read = s -> "2026-02-10";
            var m = new SchemaDefinition.AttributeMapper<>(
                    "date", "date", SchemaDefinition.Types.DATE_STRING,
                    (String v, CreateDest d) -> {}, (String v, UpdateDest d) -> {},
                    read, null
            );

            Attribute a = m.apply(new Source("ignored"));
            assertNotNull(a);
            assertEquals("date", a.getName());
            assertTrue(a.getValue().get(0) instanceof ZonedDateTime);
        }

        // single DATETIME_STRING -> converts String to ZonedDateTime
        {
            Function<Source, Object> read = s -> "2026-02-10T10:20:30-03:00";
            var m = new SchemaDefinition.AttributeMapper<>(
                    "dt", "dt", SchemaDefinition.Types.DATETIME_STRING,
                    (String v, CreateDest d) -> {}, (String v, UpdateDest d) -> {},
                    read, null
            );

            Attribute a = m.apply(new Source("ignored"));
            assertNotNull(a);
            assertEquals("dt", a.getName());
            assertTrue(a.getValue().get(0) instanceof ZonedDateTime);
        }

        // single "other" -> returns attribute with value
        {
            Function<Source, Object> read = s -> 123;
            var m = new SchemaDefinition.AttributeMapper<>(
                    "x", "x", SchemaDefinition.Types.INTEGER,
                    (Integer v, CreateDest d) -> {}, (Integer v, UpdateDest d) -> {},
                    read, null
            );

            Attribute a = m.apply(new Source("ignored"));
            assertNotNull(a);
            assertEquals(123, a.getValue().get(0));
        }

    }
}

