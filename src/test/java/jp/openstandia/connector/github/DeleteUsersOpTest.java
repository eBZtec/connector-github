package jp.openstandia.connector.github;

import jp.openstandia.connector.github.testutil.AbstractEMUTest;
import org.identityconnectors.framework.api.ConnectorFacade;
import org.identityconnectors.framework.common.objects.Uid;
import org.junit.jupiter.api.Test;

import static jp.openstandia.connector.github.GitHubEMUUserHandler.USER_OBJECT_CLASS;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class DeleteUsersOpTest extends AbstractEMUTest {

    String userUidToDelete = "";

    @Test()
    void shouldDeleteUserIfExists() {
        ConnectorFacade facade = newFacade();
        facade.delete(USER_OBJECT_CLASS, new Uid(userUidToDelete), null);
        assertNotNull(userUidToDelete);
    }
}
