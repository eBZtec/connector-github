package jp.openstandia.connector.github;

import jp.openstandia.connector.github.testutil.AbstractEMUTest;
import org.identityconnectors.framework.api.ConnectorFacade;
import org.identityconnectors.framework.common.objects.*;
import org.identityconnectors.framework.common.objects.filter.EqualsFilter;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import java.util.List;

import static jp.openstandia.connector.github.GitHubEMUGroupHandler.GROUP_OBJECT_CLASS;

public class SearchGroupsOpTest extends AbstractEMUTest {

    String groupUid = "";
    String groupName = "";

    @Test()
    public void shouldReturnAllGroups()  {
        ConnectorFacade facade = newFacade();
        ListResultHandler handler = new ListResultHandler();

        facade.search(GROUP_OBJECT_CLASS, null, handler, null);
        List<ConnectorObject> objects = handler.getObjects();
        AssertJUnit.assertTrue("Size: " + objects.size(), objects.size() > 1);
    }

    @Test()
    public void shouldReturnGroupByUid() {
        ConnectorFacade facade = newFacade();
        ListResultHandler handler = new ListResultHandler();

        Attribute attribute = AttributeBuilder.build(Uid.NAME, groupUid);
        EqualsFilter filter = new EqualsFilter(attribute);

        facade.search(GROUP_OBJECT_CLASS, filter, handler, null);
        List<ConnectorObject> objects = handler.getObjects();
        AssertJUnit.assertEquals(1, objects.size());
    }

    @Test()
    public void shouldReturnGroupByName() {
        ConnectorFacade facade = newFacade();
        ListResultHandler handler = new ListResultHandler();

        Attribute attribute = AttributeBuilder.build(Name.NAME, groupName);
        EqualsFilter filter = new EqualsFilter(attribute);

        facade.search(GROUP_OBJECT_CLASS, filter, handler, null);
        List<ConnectorObject> objects = handler.getObjects();
        AssertJUnit.assertEquals(1, objects.size());
    }
}
