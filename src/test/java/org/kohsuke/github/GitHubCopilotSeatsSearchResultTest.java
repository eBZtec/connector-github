package org.kohsuke.github;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class GitHubCopilotSeatsSearchResultTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testSerializationAndDeserialization() throws Exception {
        // Arrange
        GitHubCopilotSeatsSearchResult<String> result = new GitHubCopilotSeatsSearchResult<>();
        result.total_seats = 5;
        result.seats = new String[]{"Alice", "Bob"};

        // Act: serialize to JSON
        String json = mapper.writeValueAsString(result);

        // Assert JSON contains correct keys
        assertTrue(json.contains("\"total_seats\":5"));
        assertTrue(json.contains("\"seats\":[\"Alice\",\"Bob\"]"));

        // Act: deserialize back
        GitHubCopilotSeatsSearchResult<String> deserialized =
                mapper.readValue(json, mapper.getTypeFactory()
                        .constructParametricType(GitHubCopilotSeatsSearchResult.class, String.class));

        // Assert round-trip consistency
        assertEquals(5, deserialized.total_seats);
        assertArrayEquals(new String[]{"Alice", "Bob"}, deserialized.seats);
    }

    @Test
    public void testEmptySeatsArray() throws Exception {
        // Arrange
        GitHubCopilotSeatsSearchResult<String> result = new GitHubCopilotSeatsSearchResult<>();
        result.total_seats = 0;
        result.seats = new String[]{};

        // Act
        String json = mapper.writeValueAsString(result);
        GitHubCopilotSeatsSearchResult<String> deserialized =
                mapper.readValue(json, mapper.getTypeFactory()
                        .constructParametricType(GitHubCopilotSeatsSearchResult.class, String.class));

        // Assert
        assertEquals(0, deserialized.total_seats);
        assertNotNull(deserialized.seats);
        assertEquals(0, deserialized.seats.length);
    }

    @Test
    public void testNullSeatsField() throws Exception {
        // Arrange
        GitHubCopilotSeatsSearchResult<String> result = new GitHubCopilotSeatsSearchResult<>();
        result.total_seats = 3;
        result.seats = null;

        // Act
        String json = mapper.writeValueAsString(result);

        // Assert JSON includes total_seats but not seats
        assertTrue(json.contains("\"total_seats\":3"));

        // Deserialize again — Jackson will set seats=null
        GitHubCopilotSeatsSearchResult<String> restored =
                mapper.readValue(json, mapper.getTypeFactory()
                        .constructParametricType(GitHubCopilotSeatsSearchResult.class, String.class));

        assertEquals(3, restored.total_seats);
        assertNull(restored.seats);
    }
}
