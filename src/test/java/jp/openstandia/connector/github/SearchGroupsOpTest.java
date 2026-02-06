package jp.openstandia.connector.github;

import jp.openstandia.connector.github.testutil.AbstractEMUTest;
import org.identityconnectors.framework.api.ConnectorFacade;
import org.identityconnectors.framework.common.objects.*;
import org.identityconnectors.framework.common.objects.filter.EqualsFilter;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import static jp.openstandia.connector.github.GitHubEMUGroupHandler.GROUP_OBJECT_CLASS;

class SearchGroupsOpTest extends AbstractEMUTest {

    String groupUid = "";
    String groupName = "";

    @Test()
    void shouldReturnAllGroups()  {
        ConnectorFacade facade = newFacade();
        ListResultHandler handler = new ListResultHandler();

        facade.search(GROUP_OBJECT_CLASS, null, handler, null);
        List<ConnectorObject> objects = handler.getObjects();
        assertTrue(objects.size() > 1, "Size: " + objects.size());
    }

    @Test()
    void shouldReturnGroupByUid() {
        ConnectorFacade facade = newFacade();
        ListResultHandler handler = new ListResultHandler();

        Attribute attribute = AttributeBuilder.build(Uid.NAME, groupUid);
        EqualsFilter filter = new EqualsFilter(attribute);

        facade.search(GROUP_OBJECT_CLASS, filter, handler, null);
        List<ConnectorObject> objects = handler.getObjects();
        assertEquals(1, objects.size());
    }

    @Test()
    void shouldReturnGroupByName() {
        ConnectorFacade facade = newFacade();
        ListResultHandler handler = new ListResultHandler();

        Attribute attribute = AttributeBuilder.build(Name.NAME, groupName);
        EqualsFilter filter = new EqualsFilter(attribute);

        facade.search(GROUP_OBJECT_CLASS, filter, handler, null);
        List<ConnectorObject> objects = handler.getObjects();
        assertEquals(1, objects.size());
    }
}
