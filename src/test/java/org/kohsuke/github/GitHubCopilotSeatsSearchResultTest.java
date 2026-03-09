package org.kohsuke.github;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class GitHubCopilotSeatsSearchResultTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testSerializationAndDeserialization() throws Exception {
        GitHubCopilotSeatsSearchResult<String> result = new GitHubCopilotSeatsSearchResult<>();
        result.total_seats = 5;
        result.seats = new String[]{"Alice", "Bob"};

        String json = mapper.writeValueAsString(result);

        assertTrue(json.contains("\"total_seats\":5"));
        assertTrue(json.contains("\"seats\":[\"Alice\",\"Bob\"]"));

        GitHubCopilotSeatsSearchResult<String> deserialized =
                mapper.readValue(json, mapper.getTypeFactory()
                        .constructParametricType(GitHubCopilotSeatsSearchResult.class, String.class));

        assertEquals(5, deserialized.total_seats);
        assertArrayEquals(new String[]{"Alice", "Bob"}, deserialized.seats);
    }

    @Test
    public void testEmptySeatsArray() throws Exception {
        GitHubCopilotSeatsSearchResult<String> result = new GitHubCopilotSeatsSearchResult<>();
        result.total_seats = 0;
        result.seats = new String[]{};

        String json = mapper.writeValueAsString(result);
        GitHubCopilotSeatsSearchResult<String> deserialized =
                mapper.readValue(json, mapper.getTypeFactory()
                        .constructParametricType(GitHubCopilotSeatsSearchResult.class, String.class));

        assertEquals(0, deserialized.total_seats);
        assertNotNull(deserialized.seats);
        assertEquals(0, deserialized.seats.length);
    }

    @Test
    public void testNullSeatsField() throws Exception {
        GitHubCopilotSeatsSearchResult<String> result = new GitHubCopilotSeatsSearchResult<>();
        result.total_seats = 3;
        result.seats = null;

        String json = mapper.writeValueAsString(result);

        assertTrue(json.contains("\"total_seats\":3"));

        GitHubCopilotSeatsSearchResult<String> restored =
                mapper.readValue(json, mapper.getTypeFactory()
                        .constructParametricType(GitHubCopilotSeatsSearchResult.class, String.class));

        assertEquals(3, restored.total_seats);
        assertNull(restored.seats);
    }
}
