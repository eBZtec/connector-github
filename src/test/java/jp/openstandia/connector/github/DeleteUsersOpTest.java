package jp.openstandia.connector.github;

import jp.openstandia.connector.github.testutil.AbstractEMUTest;
import org.identityconnectors.framework.api.ConnectorFacade;
import org.identityconnectors.framework.common.objects.Uid;
import org.testng.annotations.Test;

import static jp.openstandia.connector.github.GitHubEMUUserHandler.USER_OBJECT_CLASS;

public class DeleteUsersOpTest extends AbstractEMUTest {

    String userUidToDelete = "";

    @Test()
    public void shouldDeleteUserIfExists() {
        ConnectorFacade facade = newFacade();
        facade.delete(USER_OBJECT_CLASS, new Uid(userUidToDelete), null);
    }
}
