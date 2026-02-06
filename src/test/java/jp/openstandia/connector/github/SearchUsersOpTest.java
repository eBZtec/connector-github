package jp.openstandia.connector.github;

import jp.openstandia.connector.github.testutil.AbstractEMUTest;
import org.identityconnectors.framework.api.ConnectorFacade;
import org.identityconnectors.framework.common.objects.*;
import org.identityconnectors.framework.common.objects.filter.EqualsFilter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import static jp.openstandia.connector.github.GitHubEMUUserHandler.USER_OBJECT_CLASS;

class SearchUsersOpTest extends AbstractEMUTest {

    String userUid = "";
    String userName = "";

    @Test()
    void shouldReturnAllUsers() {
        ConnectorFacade facade = newFacade();
        ListResultHandler handler = new ListResultHandler();

        facade.search(USER_OBJECT_CLASS, null, handler, null);
        List<ConnectorObject> objects = handler.getObjects();
        assertTrue(objects.size() > 1, "Size: " + objects.size());
    }

    @Test()
    void shouldReturnUserByUid()  {
        ConnectorFacade facade = newFacade();
        ListResultHandler handler = new ListResultHandler();

        Attribute attribute = AttributeBuilder.build(Uid.NAME, userUid);
        EqualsFilter filter = new EqualsFilter(attribute);

        facade.search(USER_OBJECT_CLASS, filter, handler, null);
        List<ConnectorObject> objects = handler.getObjects();
        assertEquals(1, objects.size());
    }

    @Test()
    void shouldReturnUserByUsername() {
        ConnectorFacade facade = newFacade();
        ListResultHandler handler = new ListResultHandler();

        Attribute attribute = AttributeBuilder.build(Name.NAME, userName);
        EqualsFilter filter = new EqualsFilter(attribute);

        facade.search(USER_OBJECT_CLASS, filter, handler, null);
        List<ConnectorObject> objects = handler.getObjects();
        assertEquals(1, objects.size());
    }
}
