package org.kohsuke.github;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SCIMUserTest {

    @Test
    void testFields() {
        SCIMUser user = new SCIMUser();
        user.id = "u123";
        user.userName = "user1";
        user.externalId = "ext123";
        user.active = true;

        SCIMName name = new SCIMName();
        name.givenName = "Alice";
        user.name = name;

        SCIMEmail email = new SCIMEmail();
        email.value = "alice@example.com";
        user.emails = new SCIMEmail[]{email};

        assertEquals("u123", user.id);
        assertEquals("user1", user.userName);
        assertEquals("ext123", user.externalId);
        assertTrue(user.active);
        assertEquals("Alice", user.name.givenName);
        assertEquals("alice@example.com", user.emails[0].value);
    }

    @Test
    void testJsonSerializationDeserialization() throws Exception {
        SCIMUser user = new SCIMUser();
        user.id = "u321";
        user.userName = "bob";
        user.externalId = "ext-bob";
        user.active = false;

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(user);
        assertTrue(json.contains("u321"));
        SCIMUser restored = mapper.readValue(json, SCIMUser.class);
        assertEquals("u321", restored.id);
        assertEquals("bob", restored.userName);
        assertEquals("ext-bob", restored.externalId);
    }
}
