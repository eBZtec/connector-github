package org.kohsuke.github;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SCIMEMUGroupTest {

    @Test
    void testFieldAssignmentsAndAccess() {
        SCIMEMUGroup group = new SCIMEMUGroup();

        SCIMMeta meta = new SCIMMeta();
        meta.created = "2025-01-01T00:00:00Z";
        meta.lastModified = "2025-10-24T00:00:00Z";

        SCIMMember member = new SCIMMember();
        member.value = "123";

        group.schemas = new String[]{"urn:ietf:params:scim:schemas:core:2.0:Group"};
        group.meta = meta;
        group.id = "group-1";
        group.displayName = "Engineering";
        group.members = List.of(member);
        group.externalId = "ext-001";

        assertEquals("group-1", group.id);
        assertEquals("Engineering", group.displayName);
        assertEquals("ext-001", group.externalId);
        assertEquals("urn:ietf:params:scim:schemas:core:2.0:Group", group.schemas[0]);
        assertEquals(meta, group.meta);
        assertEquals(1, group.members.size());
        assertEquals("123", group.members.get(0).value);
    }

    @Test
    void testJacksonSerializationDeserialization() throws Exception {
        SCIMEMUGroup group = new SCIMEMUGroup();
        group.schemas = new String[]{"schema1", "schema2"};
        group.id = "G1";
        group.displayName = "Developers";
        group.externalId = "EXT-DEV";
        SCIMMeta meta = new SCIMMeta();
        meta.created = "2025-01-01T00:00:00Z";
        meta.lastModified = "2025-02-02T00:00:00Z";
        group.meta = meta;

        SCIMMember member = new SCIMMember();
        member.value = "U123";
        group.members = List.of(member);

        ObjectMapper mapper = new ObjectMapper();

        // Serialize
        String json = mapper.writeValueAsString(group);
        assertTrue(json.contains("\"id\":\"G1\""));
        assertTrue(json.contains("\"displayName\":\"Developers\""));
        assertTrue(json.contains("\"externalId\":\"EXT-DEV\""));
        assertTrue(json.contains("\"members\""));
        assertTrue(json.contains("\"schemas\""));

        // Deserialize
        SCIMEMUGroup restored = mapper.readValue(json, SCIMEMUGroup.class);
        assertEquals("G1", restored.id);
        assertEquals("Developers", restored.displayName);
        assertEquals("EXT-DEV", restored.externalId);
        assertEquals(2, restored.schemas.length);
        assertEquals("U123", restored.members.get(0).value);
        assertNotNull(restored.meta);
        assertEquals("2025-01-01T00:00:00Z", restored.meta.created);
    }
}
