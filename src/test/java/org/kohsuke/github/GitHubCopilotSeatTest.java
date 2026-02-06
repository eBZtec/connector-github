package org.kohsuke.github;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GitHubCopilotSeatTest {

    @Test
    void testFieldAssignments() {
        GitHubCopilotSeat seat = new GitHubCopilotSeat();
        seat.created_at = "2025-01-01";
        seat.pending_cancellation_date = "2025-02-02";
        seat.plan_type = "pro";
        seat.last_authenticated_at = "2025-01-10";
        seat.updated_at = "2025-02-15";
        seat.last_activity_at = "2025-02-20";
        seat.last_activity_editor = "VSCode";

        GitHubCopilotSeatAssignee assignee = new GitHubCopilotSeatAssignee();
        assignee.login = "dev1";
        seat.assignee = assignee;

        GitHubCopilotSeatAssigningTeam team = new GitHubCopilotSeatAssigningTeam();
        team.id = "t1";
        team.name = "Team1";
        seat.assigning_team = team;

        assertEquals("dev1", seat.assignee.login);
        assertEquals("Team1", seat.assigning_team.name);
    }

    @Test
    void testJsonSerializationDeserialization() throws Exception {
        GitHubCopilotSeat seat = new GitHubCopilotSeat();
        seat.created_at = "2025-01-01";
        seat.plan_type = "enterprise";
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(seat);
        assertTrue(json.contains("2025-01-01"));
        GitHubCopilotSeat restored = mapper.readValue(json, GitHubCopilotSeat.class);
        assertEquals("2025-01-01", restored.created_at);
        assertEquals("enterprise", restored.plan_type);
    }
}
