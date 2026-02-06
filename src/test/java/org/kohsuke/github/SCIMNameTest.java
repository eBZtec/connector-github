package org.kohsuke.github;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SCIMNameTest {

    @Test
    void testFields() {
        SCIMName name = new SCIMName();
        name.givenName = "John";
        name.familyName = "Doe";
        name.formatted = "John Doe";

        assertEquals("John", name.givenName);
        assertEquals("Doe", name.familyName);
        assertEquals("John Doe", name.formatted);
    }

    @Test
    void testJsonSerializationDeserialization() throws Exception {
        SCIMName n = new SCIMName();
        n.givenName = "Jane";
        n.familyName = "Smith";
        n.formatted = "Jane Smith";

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(n);
        assertTrue(json.contains("Jane"));
        SCIMName restored = mapper.readValue(json, SCIMName.class);
        assertEquals("Jane", restored.givenName);
        assertEquals("Smith", restored.familyName);
        assertEquals("Jane Smith", restored.formatted);
    }
}
