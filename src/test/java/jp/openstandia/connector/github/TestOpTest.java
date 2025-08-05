package jp.openstandia.connector.github;

import jp.openstandia.connector.github.testutil.AbstractEMUTest;
import org.identityconnectors.framework.api.ConnectorFacade;
import org.identityconnectors.framework.common.objects.*;
import org.identityconnectors.framework.common.objects.filter.EqualsFilter;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static jp.openstandia.connector.github.GitHubEMUGroupHandler.GROUP_OBJECT_CLASS;
import static jp.openstandia.connector.github.GitHubEMUSeatHandler.SEAT_OBJECT_CLASS;

public class TestOpTest extends AbstractEMUTest {

    public static final ObjectClass USER_OBJECT_CLASS = new ObjectClass("EMUUser");

    static class ListResultHandler implements ResultsHandler {

        private final List<ConnectorObject> objects = new ArrayList<>();
        @Override
        public boolean handle(ConnectorObject connectorObject) {
            objects.add(connectorObject);
            return true;
        }

        public List<ConnectorObject> getObjects() { return objects; }
    }

    @Test(priority = 1)
    public void shouldReturnAccessToken() {
        ConnectorFacade facade = setupConnector();
        facade.test();
    }

    @Test(priority = 2)
    public void shouldReturnAllUsers() {
        ConnectorFacade facade = setupConnector();
        ListResultHandler handler = new ListResultHandler();

        facade.search(USER_OBJECT_CLASS, null, handler, null);
        List<ConnectorObject> objects = handler.getObjects();
        AssertJUnit.assertTrue("Size: " + objects.size(), objects.size() > 1);
    }

    @Test(priority = 3)
    public void shouldReturnUserByLogin() {
        ConnectorFacade facade = setupConnector();
        ListResultHandler handler = new ListResultHandler();

        Attribute attribute = AttributeBuilder.build(Name.NAME, "");
        EqualsFilter filter = new EqualsFilter(attribute);

        facade.search(USER_OBJECT_CLASS, filter, handler, null);
        List<ConnectorObject> objects = handler.getObjects();
        AssertJUnit.assertEquals(1, objects.size());
    }

    @Test(priority = 4)
    public void shouldReturnUserByUid()  {
        ConnectorFacade facade = setupConnector();
        ListResultHandler handler = new ListResultHandler();

        Attribute attribute = AttributeBuilder.build(Uid.NAME, "");
        EqualsFilter filter = new EqualsFilter(attribute);

        facade.search(USER_OBJECT_CLASS, filter, handler, null);
        List<ConnectorObject> objects = handler.getObjects();
        AssertJUnit.assertEquals(1, objects.size());
    }

    @Test(priority = 5)
    public void shouldReturnAllGroups()  {
        ConnectorFacade facade = setupConnector();
        ListResultHandler handler = new ListResultHandler();

        facade.search(GROUP_OBJECT_CLASS, null, handler, null);
        List<ConnectorObject> objects = handler.getObjects();
        AssertJUnit.assertTrue("Size: " + objects.size(), objects.size() > 1);
    }

    @Test(priority = 6)
    public void shouldReturnGroupByName() {
        ConnectorFacade facade = setupConnector();
        ListResultHandler handler = new ListResultHandler();

        Attribute attribute = AttributeBuilder.build(Name.NAME, "");
        EqualsFilter filter = new EqualsFilter(attribute);

        facade.search(GROUP_OBJECT_CLASS, filter, handler, null);
        List<ConnectorObject> objects = handler.getObjects();
        AssertJUnit.assertEquals(1, objects.size());
    }

    @Test()
    public void shouldReturnAllSeats()  {
        ConnectorFacade facade = setupConnector();
        ListResultHandler handler = new ListResultHandler();

        facade.search(SEAT_OBJECT_CLASS, null, handler, null);
        List<ConnectorObject> objects = handler.getObjects();
        AssertJUnit.assertTrue("Size: " + objects.size(), objects.size() > 1);
    }

    @Test()
    public void shouldReturnSeatByName()  {
        ConnectorFacade facade = setupConnector();
        ListResultHandler handler = new ListResultHandler();

        Attribute attribute = AttributeBuilder.build(Name.NAME, "");
        EqualsFilter filter = new EqualsFilter(attribute);

        facade.search(SEAT_OBJECT_CLASS, filter, handler, null);
        List<ConnectorObject> objects = handler.getObjects();
        AssertJUnit.assertEquals(1, objects.size());
    }

    @Test()
    public void shouldReturnSeatByUid()  {
        ConnectorFacade facade = setupConnector();
        ListResultHandler handler = new ListResultHandler();

        Attribute attribute = AttributeBuilder.build(Uid.NAME, "");
        EqualsFilter filter = new EqualsFilter(attribute);

        facade.search(SEAT_OBJECT_CLASS, filter, handler, null);
        List<ConnectorObject> objects = handler.getObjects();
        AssertJUnit.assertEquals(1, objects.size());
    }
}