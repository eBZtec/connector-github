package jp.openstandia.connector.github;

import jp.openstandia.connector.util.SchemaDefinition;
import org.identityconnectors.framework.common.objects.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class GitHubCopilotSeatHandlerTest {

    @Test
    void query_coversTheSuperCall() {
        GitHubEMUConfiguration config = mock(GitHubEMUConfiguration.class);
        GitHubClient<GitHubEMUSchema> client = mock(GitHubClient.class);
        GitHubEMUSchema schema = mock(GitHubEMUSchema.class);
        SchemaDefinition schemaDefinition = mock(SchemaDefinition.class);

        GitHubCopilotSeatHandler handler = new GitHubCopilotSeatHandler(config, client, schema, schemaDefinition) {
            @Override
            public void query(GitHubFilter filter, ResultsHandler resultsHandler, OperationOptions options) {
                super.query(filter, resultsHandler, options);
            }
        };

        GitHubFilter filter = mock(GitHubFilter.class);
        ResultsHandler resultsHandler = mock(ResultsHandler.class);
        OperationOptions options = new OperationOptionsBuilder().build();

        assertThrows(UnsupportedOperationException.class, () ->
                handler.query(filter, resultsHandler, options)
        );
    }
}