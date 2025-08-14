package jp.openstandia.connector.github;

import jp.openstandia.connector.github.testutil.AbstractEMUTest;
import org.identityconnectors.framework.api.ConnectorFacade;
import org.identityconnectors.framework.common.objects.*;
import org.identityconnectors.framework.common.objects.filter.EqualsFilter;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import java.util.List;

import static jp.openstandia.connector.github.GitHubEMUUserHandler.USER_OBJECT_CLASS;

public class SearchUsersOpTest extends AbstractEMUTest {

    String userUid = "";
    String userName = "";

    @Test()
    public void shouldReturnAllUsers() {
        ConnectorFacade facade = newFacade();
        ListResultHandler handler = new ListResultHandler();

        facade.search(USER_OBJECT_CLASS, null, handler, null);
        List<ConnectorObject> objects = handler.getObjects();
        AssertJUnit.assertTrue("Size: " + objects.size(), objects.size() > 1);
    }

    @Test()
    public void shouldReturnUserByUid()  {
        ConnectorFacade facade = newFacade();
        ListResultHandler handler = new ListResultHandler();

        Attribute attribute = AttributeBuilder.build(Uid.NAME, userUid);
        EqualsFilter filter = new EqualsFilter(attribute);

        facade.search(USER_OBJECT_CLASS, filter, handler, null);
        List<ConnectorObject> objects = handler.getObjects();
        AssertJUnit.assertEquals(1, objects.size());
    }

    @Test()
    public void shouldReturnUserByUsername() {
        ConnectorFacade facade = newFacade();
        ListResultHandler handler = new ListResultHandler();

        Attribute attribute = AttributeBuilder.build(Name.NAME, userName);
        EqualsFilter filter = new EqualsFilter(attribute);

        facade.search(USER_OBJECT_CLASS, filter, handler, null);
        List<ConnectorObject> objects = handler.getObjects();
        AssertJUnit.assertEquals(1, objects.size());
    }
}
