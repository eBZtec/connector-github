package jp.openstandia.connector.github;

import jp.openstandia.connector.util.QueryHandler;
import org.identityconnectors.framework.common.exceptions.AlreadyExistsException;
import org.identityconnectors.framework.common.exceptions.UnknownUidException;
import org.identityconnectors.framework.common.objects.Name;
import org.identityconnectors.framework.common.objects.OperationOptions;
import org.identityconnectors.framework.common.objects.Uid;
import org.junit.jupiter.api.Test;
import org.kohsuke.github.GitHubCopilotSeat;
import org.kohsuke.github.SCIMEMUGroup;
import org.kohsuke.github.SCIMEMUUser;
import org.kohsuke.github.SCIMPatchOperations;

import java.util.Collections;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class GitHubClientDefaultsUnsupportedTest {

    /**
     * Implementação mínima apenas para acessar os métodos default.
     */
    private static class DummyClient implements GitHubClient<AbstractGitHubSchema<AbstractGitHubConfiguration>> {
        @Override
        public void setInstanceName(String instanceName) {
        }

        @Override
        public void test() {
        }

        @Override
        public void auth() {
        }

        @Override
        public void close() {
        }
    }


    @SuppressWarnings("unchecked")
    private static AbstractGitHubSchema<AbstractGitHubConfiguration> mockSchema() {
        return mock(AbstractGitHubSchema.class);
    }

    private static OperationOptions emptyOptions() {
        return new OperationOptions(Collections.emptyMap());
    }

    // --------------------
    // EMU USER (defaults)
    // --------------------

    @Test
    void default_createEMUUser_throwsUnsupported() {
        DummyClient client = new DummyClient();
        SCIMEMUUser user = new SCIMEMUUser();

        assertThrows(UnsupportedOperationException.class, () -> client.createEMUUser(user));
    }

    @Test
    void default_patchEMUUser_throwsUnsupported() {
        DummyClient client = new DummyClient();
        Uid uid = new Uid("u1");
        SCIMPatchOperations ops = new SCIMPatchOperations();

        assertThrows(UnsupportedOperationException.class, () -> client.patchEMUUser(uid, ops));
    }

    @Test
    void default_deleteEMUUser_throwsUnsupported() {
        DummyClient client = new DummyClient();
        Uid uid = new Uid("u1");

        assertThrows(UnsupportedOperationException.class,
                () -> client.deleteEMUUser(uid, emptyOptions()));
    }

    @Test
    void default_getEMUUsers_throwsUnsupported() {
        DummyClient client = new DummyClient();
        QueryHandler<SCIMEMUUser> handler = u -> true;

        assertThrows(UnsupportedOperationException.class,
                () -> client.getEMUUsers(handler, emptyOptions(), Collections.emptySet(), 10, 0));
    }

    @Test
    void default_getEMUUser_byUid_throwsUnsupported() {
        DummyClient client = new DummyClient();

        assertThrows(UnsupportedOperationException.class,
                () -> client.getEMUUser(new Uid("u1"), emptyOptions(), Collections.emptySet()));
    }

    @Test
    void default_getEMUUser_byName_throwsUnsupported() {
        DummyClient client = new DummyClient();

        assertThrows(UnsupportedOperationException.class,
                () -> client.getEMUUser(new Name("alice"), emptyOptions(), Collections.emptySet()));
    }

    // --------------------
    // EMU GROUP (defaults)
    // --------------------

    @Test
    void default_createEMUGroup_throwsUnsupported() throws AlreadyExistsException {
        DummyClient client = new DummyClient();
        SCIMEMUGroup group = new SCIMEMUGroup();

        assertThrows(UnsupportedOperationException.class,
                () -> client.createEMUGroup(mockSchema(), group));
    }

    @Test
    void default_patchEMUGroup_throwsUnsupported() throws UnknownUidException {
        DummyClient client = new DummyClient();
        Uid uid = new Uid("g1");
        SCIMPatchOperations ops = new SCIMPatchOperations();

        assertThrows(UnsupportedOperationException.class,
                () -> client.patchEMUGroup(uid, ops));
    }

    @Test
    void default_deleteEMUGroup_throwsUnsupported() throws UnknownUidException {
        DummyClient client = new DummyClient();
        Uid uid = new Uid("g1");

        assertThrows(UnsupportedOperationException.class,
                () -> client.deleteEMUGroup(uid, emptyOptions()));
    }

    @Test
    void default_getEMUGroups_throwsUnsupported() {
        DummyClient client = new DummyClient();
        QueryHandler<SCIMEMUGroup> handler = g -> true;

        assertThrows(UnsupportedOperationException.class,
                () -> client.getEMUGroups(handler, emptyOptions(), Collections.emptySet(), 5, 0));
    }

    @Test
    void default_getEMUGroup_byUid_throwsUnsupported() {
        DummyClient client = new DummyClient();

        assertThrows(UnsupportedOperationException.class,
                () -> client.getEMUGroup(new Uid("g1"), emptyOptions(), Collections.emptySet()));
    }

    @Test
    void default_getEMUGroup_byName_throwsUnsupported() {
        DummyClient client = new DummyClient();

        assertThrows(UnsupportedOperationException.class,
                () -> client.getEMUGroup(new Name("groupA"), emptyOptions(), Collections.emptySet()));
    }

    // --------------------
    // COPILOT SEATS (defaults)
    // --------------------

    @Test
    void default_getCopilotSeat_byUid_throwsUnsupported() {
        DummyClient client = new DummyClient();

        assertThrows(UnsupportedOperationException.class,
                () -> client.getCopilotSeat(new Uid("seat-1"), emptyOptions(), Collections.emptySet()));
    }

    @Test
    void default_getCopilotSeat_byName_throwsUnsupported() {
        DummyClient client = new DummyClient();

        assertThrows(UnsupportedOperationException.class,
                () -> client.getCopilotSeat(new Name("user-login"), emptyOptions(), Collections.emptySet()));
    }

    @Test
    void default_getCopilotSeats_throwsUnsupported() {
        DummyClient client = new DummyClient();
        QueryHandler<GitHubCopilotSeat> handler = s -> true;
        Set<String> fetch = Collections.emptySet();

        assertThrows(UnsupportedOperationException.class,
                () -> client.getCopilotSeats(handler, emptyOptions(), fetch, 20, 0));
    }
}
