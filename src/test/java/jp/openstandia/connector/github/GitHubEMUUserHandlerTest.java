package jp.openstandia.connector.github;

import jp.openstandia.connector.util.SchemaDefinition;
import org.identityconnectors.framework.common.objects.*;
import org.junit.jupiter.api.Test;
import org.kohsuke.github.*;
import jp.openstandia.connector.util.QueryHandler;
import org.mockito.ArgumentCaptor;

import java.util.*;

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

    @Test
    void instanciaHandlerOk() {
        GitHubEMUUserHandler handler = new GitHubEMUUserHandler(
                mock(GitHubEMUConfiguration.class),
                new DummyEMUClient(),
                mock(GitHubEMUSchema.class),
                mock(SchemaDefinition.class)
        );
        assertNotNull(handler);
    }

    @Test
    void testCreateSchema() {
        GitHubEMUConfiguration config = mock(GitHubEMUConfiguration.class);
        GitHubClient<GitHubEMUSchema> client = mock(GitHubClient.class);
        SchemaDefinition.Builder builder = GitHubEMUUserHandler.createSchema(config, client);
        assertNotNull(builder);
    }

    @Test
    void toConnectorObject_coversGetters() {
        GitHubEMUConfiguration config = mock(GitHubEMUConfiguration.class);
        GitHubClient<GitHubEMUSchema> client = mock(GitHubClient.class);
        GitHubEMUSchema schema = mock(GitHubEMUSchema.class);

        SchemaDefinition schemaDefinition = GitHubEMUUserHandler.createSchema(config, client).build();

        GitHubEMUUserHandler handler = new GitHubEMUUserHandler(config, client, schema, schemaDefinition);

        Set<String> returnAttributesSet = Set.of(
                "__UID__", "__NAME__", OperationalAttributes.ENABLE_NAME,
                "externalId", "displayName",
                "name.formatted", "name.givenName", "name.familyName",
                "primaryEmail", "primaryRole", "groups",
                "meta.created", "meta.lastModified"
        );

        SCIMEMUUser fullUser = createFullUser();

        ConnectorObject co = handler.toConnectorObject(schemaDefinition, fullUser, returnAttributesSet, false);

        assertNotNull(co);
    }

    @Test
    void getAll() {
        GitHubEMUConfiguration config = mock(GitHubEMUConfiguration.class);
        GitHubClient<GitHubEMUSchema> client = mock(GitHubClient.class);
        GitHubEMUSchema schema = mock(GitHubEMUSchema.class);

        SchemaDefinition schemaDefinition = GitHubEMUUserHandler.createSchema(config, client).build();

        GitHubEMUUserHandler handler = new GitHubEMUUserHandler(config, client, schema, schemaDefinition);

        OperationOptions options = new OperationOptionsBuilder().build();
        Set<String> returnAttributesSet = Set.of("__UID__", "__NAME__");
        Set<String> fetchFieldsSet = Collections.emptySet();

        ResultsHandler resultsHandler = mock(ResultsHandler.class);

        ArgumentCaptor<QueryHandler<SCIMEMUUser>> captor = ArgumentCaptor.forClass(QueryHandler.class);

        when(client.getEMUUsers(captor.capture(), eq(options), eq(fetchFieldsSet), eq(100), eq(0)))
                .thenReturn(1);

        handler.getAll(resultsHandler, options, returnAttributesSet, fetchFieldsSet, false, 100, 0);

        captor.getValue().handle(createMinimalUser());
        verify(resultsHandler).handle(any(ConnectorObject.class));
    }

    private SCIMEMUUser createFullUser() {
        SCIMEMUUser u = new SCIMEMUUser();
        u.id = "user-123";
        u.userName = "testuser";
        u.active = true;
        u.externalId = "ext-123";
        u.displayName = "Test User";

        u.name = new SCIMName();
        u.name.formatted = "Test User Full";
        u.name.givenName = "Test";
        u.name.familyName = "User";

        SCIMEmail email = new SCIMEmail();
        email.value = "test@example.com";
        email.primary = true;
        u.emails = List.of(email);

        SCIMRole role = new SCIMRole();
        role.value = "admin";
        role.primary = true;
        u.roles = List.of(role);

        SCIMMember g = new SCIMMember();
        g.value = "group-uuid";
        g.ref = "/scim/v2/Groups/group-uuid";
        u.groups = List.of(g);

        SCIMMeta meta = new SCIMMeta();
        meta.created = "2025-03-03T10:00:00Z";
        meta.lastModified = "2025-03-03T11:00:00Z";
        u.meta = meta;

        return u;
    }

    private SCIMEMUUser createMinimalUser() {
        SCIMEMUUser u = new SCIMEMUUser();
        u.id = "u1";
        u.userName = "user1";
        return u;
    }
}