package org.kohsuke.github;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GitHubCopilotSeatAssigneeTest {

    @Test
    void testFieldAssignmentsAndAccess() {
        GitHubCopilotSeatAssignee a = new GitHubCopilotSeatAssignee();
        a.login = "user1";
        a.id = "123";
        a.node_id = "node123";
        a.url = "https://api.github.com/user1";
        a.type = "User";
        a.user_view_type = "public";
        a.site_admin = "false";

        assertEquals("user1", a.login);
        assertEquals("123", a.id);
        assertEquals("node123", a.node_id);
        assertEquals("https://api.github.com/user1", a.url);
        assertEquals("User", a.type);
        assertEquals("public", a.user_view_type);
        assertEquals("false", a.site_admin);
    }

    @Test
    void testJsonSerialization() throws Exception {
        GitHubCopilotSeatAssignee a = new GitHubCopilotSeatAssignee();
        a.login = "alice";
        a.id = "001";
        a.type = "User";
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(a);
        assertTrue(json.contains("alice"));
        GitHubCopilotSeatAssignee copy = mapper.readValue(json, GitHubCopilotSeatAssignee.class);
        assertEquals("alice", copy.login);
        assertEquals("001", copy.id);
        assertEquals("User", copy.type);
    }
}
