package jp.openstandia.connector.github;

import jp.openstandia.connector.github.testutil.AbstractEMUTest;
import org.identityconnectors.framework.api.ConnectorFacade;
import org.identityconnectors.framework.common.objects.*;
import org.identityconnectors.framework.common.objects.filter.EqualsFilter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static jp.openstandia.connector.github.GitHubEMUUserHandler.USER_OBJECT_CLASS;

class UpdateUsersOpTest extends AbstractEMUTest {

    String userUid = "";
    String attrToUpdate = "";
    String attrNewValue = "";

    @Test()
    void shouldActivateUser() {
        Set<AttributeDelta> attributes = new HashSet<>();

        AttributeDeltaBuilder deltaBuilder = new AttributeDeltaBuilder();
        deltaBuilder.setName(OperationalAttributes.ENABLE_NAME);
        deltaBuilder.addValueToReplace(true);
        attributes.add(deltaBuilder.build());

        ConnectorFacade facade = newFacade();
        facade.updateDelta(USER_OBJECT_CLASS, new Uid(userUid), attributes, null);

        ListResultHandler handler = new ListResultHandler();
        Attribute attribute = AttributeBuilder.build(Uid.NAME, userUid);
        EqualsFilter filter = new EqualsFilter(attribute);

        facade.search(USER_OBJECT_CLASS, filter, handler, null);
        List<ConnectorObject> objects =  handler.getObjects();
        assertEquals(1, objects.size());
    }

    @Test()
    void shouldInactivateUser() {
        Set<AttributeDelta> attributes = new HashSet<>();

        AttributeDeltaBuilder deltaBuilder = new AttributeDeltaBuilder();
        deltaBuilder.setName(OperationalAttributes.ENABLE_NAME);
        deltaBuilder.addValueToReplace(false);
        attributes.add(deltaBuilder.build());

        ConnectorFacade facade = newFacade();
        facade.updateDelta(USER_OBJECT_CLASS, new Uid(userUid), attributes, null);

        ListResultHandler handler = new ListResultHandler();
        Attribute attribute = AttributeBuilder.build(Uid.NAME, userUid);
        EqualsFilter filter = new EqualsFilter(attribute);

        facade.search(USER_OBJECT_CLASS, filter, handler, null);
        List<ConnectorObject> objects =  handler.getObjects();
        assertEquals(1, objects.size());
    }

    @Test()
    void shouldUpdateAttrValue() {
        ConnectorFacade facade = newFacade();

        // Create an AttributeDelta to update the status of uid
        Set<AttributeDelta> attributes = new HashSet<>();
        AttributeDeltaBuilder deltaBuilder = new AttributeDeltaBuilder();
        deltaBuilder.setName(attrToUpdate);
        deltaBuilder.addValueToReplace(attrNewValue);
        attributes.add(deltaBuilder.build());

        // Call updateDelta to update the status
        facade.updateDelta(USER_OBJECT_CLASS, new Uid(userUid), attributes, null);

        // Retrieve and verify the updated object
        ListResultHandler handler = new ListResultHandler();
        Attribute attribute = AttributeBuilder.build(Uid.NAME, userUid);
        EqualsFilter filter = new EqualsFilter(attribute);

        facade.search(USER_OBJECT_CLASS, filter, handler, null);
        List<ConnectorObject> objects =  handler.getObjects();
        assertEquals(1, objects.size());

        ConnectorObject object = objects.get(0);
        Attribute nameAttr = object.getAttributeByName(attrToUpdate);
        assertNotNull(nameAttr);
        assertEquals(attrNewValue, nameAttr.getValue().get(0));
    }
}
