package org.kohsuke.github;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class SCIMPatchOperationsTest {

    @Test
    void testReplaceString() {
        SCIMPatchOperations ops = new SCIMPatchOperations();

        ops.replace("displayName", "Alice");
        assertEquals(1, ops.operations.size());

        SCIMPatchOperations.Operation op = ops.operations.get(0);
        assertEquals("replace", op.op);
        assertEquals("displayName", op.path);
        assertEquals("Alice", op.value);
    }

    @Test
    void testReplaceString_nullValue() {
        SCIMPatchOperations ops = new SCIMPatchOperations();

        ops.replace("displayName", (String) null);
        assertEquals("", ops.operations.get(0).value); // Should become empty string
    }

    @Test
    void testReplaceBoolean() {
        SCIMPatchOperations ops = new SCIMPatchOperations();

        ops.replace("active", true);
        SCIMPatchOperations.Operation op = ops.operations.get(0);

        assertEquals("replace", op.op);
        assertEquals("active", op.path);
        assertEquals(true, op.value);
    }

    @Test
    void testReplaceEmail_nonNull() {
        SCIMPatchOperations ops = new SCIMPatchOperations();
        SCIMEmail email = new SCIMEmail();
        email.value = "user@example.com";
        email.primary = true;

        ops.replace(email);
        assertEquals(1, ops.operations.size());

        SCIMPatchOperations.Operation op = ops.operations.get(0);
        assertEquals("emails", op.path);
        assertTrue(op.value instanceof List);

        List<?> list = (List<?>) op.value;
        assertEquals(1, list.size());
        assertTrue(list.get(0) instanceof SCIMEmail);
    }

    @Test
    void testReplaceEmail_null() {
        SCIMPatchOperations ops = new SCIMPatchOperations();
        ops.replace((SCIMEmail) null);

        assertEquals(1, ops.operations.size());
        SCIMPatchOperations.Operation op = ops.operations.get(0);
        assertEquals("emails", op.path);

        // Value should be a list of maps with {"value": ""}
        List<?> list = (List<?>) op.value;
        assertTrue(list.get(0) instanceof Map);
        assertEquals("", ((Map<?, ?>) list.get(0)).get("value"));
    }

    @Test
    void testReplaceRole_nonNull() {
        SCIMPatchOperations ops = new SCIMPatchOperations();
        SCIMRole role = new SCIMRole();
        role.value = "developer";
        role.primary = true;

        ops.replace(role);
        assertEquals(1, ops.operations.size());
        SCIMPatchOperations.Operation op = ops.operations.get(0);
        assertEquals("roles", op.path);

        List<?> roles = (List<?>) op.value;
        assertEquals(1, roles.size());
        assertTrue(roles.get(0) instanceof SCIMRole);
    }

    @Test
    void testReplaceRole_null() {
        SCIMPatchOperations ops = new SCIMPatchOperations();
        ops.replace((SCIMRole) null);

        SCIMPatchOperations.Operation op = ops.operations.get(0);
        assertEquals("roles", op.path);
        List<?> values = (List<?>) op.value;
        assertEquals("", ((Map<?, ?>) values.get(0)).get("value"));
    }

    @Test
    void testAddMembers() {
        SCIMPatchOperations ops = new SCIMPatchOperations();
        List<String> members = Arrays.asList("m1", "m2");

        ops.addMembers(members);

        assertEquals(1, ops.operations.size());
        SCIMPatchOperations.Operation op = ops.operations.get(0);
        assertEquals("add", op.op);
        assertEquals("members", op.path);

        List<?> list = (List<?>) op.value;
        assertEquals(2, list.size());
        assertTrue(list.get(0) instanceof SCIMPatchOperations.Member);
    }

    @Test
    void testRemoveMembers() {
        SCIMPatchOperations ops = new SCIMPatchOperations();
        List<String> members = Arrays.asList("m3", "m4");

        ops.removeMembers(members);

        assertEquals(1, ops.operations.size());
        SCIMPatchOperations.Operation op = ops.operations.get(0);
        assertEquals("remove", op.op);
        assertEquals("members", op.path);

        List<?> list = (List<?>) op.value;
        assertEquals("m3", ((SCIMPatchOperations.Member) list.get(0)).value);
    }

    @Test
    void testHasAttributesChange_true_false() {
        SCIMPatchOperations ops = new SCIMPatchOperations();
        assertFalse(ops.hasAttributesChange());

        ops.replace("x", "y");
        assertTrue(ops.hasAttributesChange());
    }

    @Test
    void testJsonSerializationDeserialization() throws Exception {
        SCIMPatchOperations ops = new SCIMPatchOperations();
        ops.replace("field", "value");

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(ops);
        assertTrue(json.contains("PatchOp"));
        assertTrue(json.contains("replace"));

        SCIMPatchOperations restored = mapper.readValue(json, SCIMPatchOperations.class);
        assertNotNull(restored.schemas);
        assertEquals(1, restored.operations.size());
    }
}
