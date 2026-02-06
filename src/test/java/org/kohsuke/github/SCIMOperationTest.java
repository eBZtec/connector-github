package org.kohsuke.github;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SCIMOperationTest {

    @Test
    void testStringValueOperation() {
        SCIMOperation<String> op = new SCIMOperation<>("replace", "displayName", "Alice");

        assertEquals("replace", op.op);
        assertEquals("displayName", op.path);
        assertEquals("Alice", op.value);
    }

    @Test
    void testIntegerValueOperation() {
        SCIMOperation<Integer> op = new SCIMOperation<>("add", "age", 30);

        assertEquals("add", op.op);
        assertEquals("age", op.path);
        assertEquals(30, op.value);
    }

    @Test
    void testComplexValueOperation() {
        DummyValue val = new DummyValue("key1", "val1");
        SCIMOperation<DummyValue> op = new SCIMOperation<>("replace", "metadata", val);

        assertEquals("replace", op.op);
        assertEquals("metadata", op.path);
        assertNotNull(op.value);
        assertEquals("key1", op.value.key);
        assertEquals("val1", op.value.value);
    }

    static class DummyValue {
        String key;
        String value;

        DummyValue(String key, String value) {
            this.key = key;
            this.value = value;
        }
    }
}
