package org.kohsuke.github;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SCIMSearchResultTest {

    static class DummyResource {
        public String name;
    }

    @Test
    void testFieldAssignmentAndAccess() {
        SCIMSearchResult<DummyResource> result = new SCIMSearchResult<>();

        result.totalResults = 5;
        result.itemsPerPage = 2;
        result.startIndex = 1;

        DummyResource res1 = new DummyResource();
        res1.name = "first";
        DummyResource res2 = new DummyResource();
        res2.name = "second";
        result.Resources = new DummyResource[]{res1, res2};

        assertEquals(5, result.totalResults);
        assertEquals(2, result.itemsPerPage);
        assertEquals(1, result.startIndex);
        assertEquals("second", result.Resources[1].name);
    }

    @Test
    void testJacksonSerializationDeserialization() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        // Build a sample result
        SCIMSearchResult<DummyResource> original = new SCIMSearchResult<>();
        original.totalResults = 10;
        original.itemsPerPage = 5;
        original.startIndex = 2;
        DummyResource dummy = new DummyResource();
        dummy.name = "foo";
        original.Resources = new DummyResource[]{dummy};

        // Serialize to JSON
        String json = mapper.writeValueAsString(original);
        assertTrue(json.contains("\"totalResults\":10"));
        assertTrue(json.contains("\"itemsPerPage\":5"));
        assertTrue(json.contains("\"startIndex\":2"));
        assertTrue(json.contains("\"Resources\""));

        // Deserialize back
        SCIMSearchResult<DummyResource> restored =
                mapper.readValue(json, mapper.getTypeFactory()
                        .constructParametricType(SCIMSearchResult.class, DummyResource.class));

        assertEquals(10, restored.totalResults);
        assertEquals(5, restored.itemsPerPage);
        assertEquals(2, restored.startIndex);
        assertEquals("foo", restored.Resources[0].name);
    }
}
