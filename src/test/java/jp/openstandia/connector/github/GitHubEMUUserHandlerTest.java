package jp.openstandia.connector.github;

import jp.openstandia.connector.util.SchemaDefinition;
import org.junit.jupiter.api.Test;
import org.kohsuke.github.SCIMEMUUser;
import org.kohsuke.github.SCIMPatchOperations;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.NoSuchElementException;
import java.util.stream.Stream;

import static jp.openstandia.connector.github.GitHubEMUUserHandler.USER_OBJECT_CLASS;
import static org.identityconnectors.framework.common.objects.AttributeInfo.Flags.NOT_CREATABLE;
import static org.identityconnectors.framework.common.objects.AttributeInfo.Flags.NOT_UPDATEABLE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class GitHubEMUUserHandlerTest {

    private static class DummyEMUClient implements GitHubClient<GitHubEMUSchema> {
        @Override public void setInstanceName(String instanceName) {}
        @Override public void test() {}
        @Override public void auth() {}
        @Override public void close() {}
    }

    private static GitHubEMUUserHandler newHandler() {
        GitHubEMUConfiguration configuration = mock(GitHubEMUConfiguration.class);
        GitHubClient<GitHubEMUSchema> client = new DummyEMUClient();
        GitHubEMUSchema schema = mock(GitHubEMUSchema.class);
        SchemaDefinition schemaDefinition = mock(SchemaDefinition.class);

        return new GitHubEMUUserHandler(configuration, client, schema, schemaDefinition);
    }

    @Test
    void instancia_handler_ok() {
        GitHubEMUUserHandler handler = newHandler();
        assertNotNull(handler);
    }


    @Test
    public void testCreateSchema() {
        GitHubEMUConfiguration config = mock(GitHubEMUConfiguration.class);
        GitHubClient<GitHubEMUSchema> client = mock(GitHubClient.class);
        SchemaDefinition.Builder builder = GitHubEMUUserHandler.createSchema(config, client);
        assertNotNull(builder);
    }

    @Test
    public void testUidLambdaExecution() {
        GitHubEMUConfiguration config = mock(GitHubEMUConfiguration.class);
        GitHubClient<GitHubEMUSchema> client = mock(GitHubClient.class);

        SchemaDefinition.Builder builder = GitHubEMUUserHandler.createSchema(config, client);
        SchemaDefinition definition = builder.build();

        // Agora precisamos de um "source" que tenha o campo 'id'
        SCIMEMUUser user = new SCIMEMUUser();
        user.id = UUID.randomUUID().toString();

        // A mágica: simular a extração do UID usando o schema
        String extractedId = definition.getReturnedByDefaultAttributesSet().get(user.id);

        assertEquals(user.id, extractedId);
    }
}


