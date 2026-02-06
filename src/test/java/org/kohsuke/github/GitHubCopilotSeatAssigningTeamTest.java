package org.kohsuke.github;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GitHubCopilotSeatAssigningTeamTest {

    @Test
    void testFieldAssignmentsAndAccess() {
        GitHubCopilotSeatAssigningTeam team = new GitHubCopilotSeatAssigningTeam();

        team.id = "123";
        team.name = "Dev Team";
        team.slug = "dev-team";
        team.group_name = "Engineering";
        team.created_at = "2025-01-01T00:00:00Z";
        team.updated_at = "2025-10-24T00:00:00Z";

        assertEquals("123", team.id);
        assertEquals("Dev Team", team.name);
        assertEquals("dev-team", team.slug);
        assertEquals("Engineering", team.group_name);
        assertEquals("2025-01-01T00:00:00Z", team.created_at);
        assertEquals("2025-10-24T00:00:00Z", team.updated_at);
    }

    @Test
    void testJacksonSerializationDeserialization() throws Exception {
        GitHubCopilotSeatAssigningTeam team = new GitHubCopilotSeatAssigningTeam();
        team.id = "001";
        team.name = "AI Wizards";
        team.slug = "ai-wizards";
        team.group_name = "R&D";
        team.created_at = "2025-01-01T00:00:00Z";
        team.updated_at = "2025-10-24T00:00:00Z";

        ObjectMapper mapper = new ObjectMapper();

        // Serialize to JSON
        String json = mapper.writeValueAsString(team);
        assertTrue(json.contains("\"id\":\"001\""));
        assertTrue(json.contains("\"name\":\"AI Wizards\""));
        assertTrue(json.contains("\"slug\":\"ai-wizards\""));
        assertTrue(json.contains("\"group_name\":\"R&D\""));
        assertTrue(json.contains("\"created_at\":\"2025-01-01T00:00:00Z\""));
        assertTrue(json.contains("\"updated_at\":\"2025-10-24T00:00:00Z\""));

        // Deserialize back
        GitHubCopilotSeatAssigningTeam restored =
                mapper.readValue(json, GitHubCopilotSeatAssigningTeam.class);

        assertEquals("001", restored.id);
        assertEquals("AI Wizards", restored.name);
        assertEquals("ai-wizards", restored.slug);
        assertEquals("R&D", restored.group_name);
        assertEquals("2025-01-01T00:00:00Z", restored.created_at);
        assertEquals("2025-10-24T00:00:00Z", restored.updated_at);
    }
}
