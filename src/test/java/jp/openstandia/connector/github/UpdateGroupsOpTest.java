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

import static jp.openstandia.connector.github.GitHubEMUGroupHandler.GROUP_OBJECT_CLASS;
import static org.junit.jupiter.api.Assertions.assertEquals;

class UpdateGroupsOpTest extends AbstractEMUTest {

    String userUid = "";
    String groupUidToUpdate = "";

    @Test()
    void shouldAddUserToGroup() {
        // Create an AttributeDelta to add user uid
        Set<AttributeDelta> attributes = new HashSet<>();
        AttributeDeltaBuilder deltaBuilder = new AttributeDeltaBuilder();
        deltaBuilder.setName("members.User.value");
        deltaBuilder.addValueToAdd(userUid);
        attributes.add(deltaBuilder.build());

        // Call updateDelta to update the group
        ConnectorFacade facade = newFacade();
        facade.updateDelta(GROUP_OBJECT_CLASS, new Uid(groupUidToUpdate), attributes, null);

        // Retrieve and verify the updated object
        ListResultHandler handler = new ListResultHandler();
        Attribute attribute = AttributeBuilder.build(Uid.NAME, groupUidToUpdate);
        EqualsFilter filter = new EqualsFilter(attribute);

        facade.search(GROUP_OBJECT_CLASS, filter, handler, null);
        List<ConnectorObject> objects =  handler.getObjects();
        assertEquals(1, objects.size());

        ConnectorObject object = objects.get(0);
        Attribute memberOfAttr = object.getAttributeByName("members.User.value");
        assertNotNull(memberOfAttr);

        List<Object> grupos = memberOfAttr.getValue();
        assertTrue(grupos.contains(userUid));
    }

    @Test()
    void shouldRemoveUserFromGroup() {
        // Create an AttributeDelta to add user uid
        Set<AttributeDelta> attributes = new HashSet<>();
        AttributeDeltaBuilder deltaBuilder = new AttributeDeltaBuilder();
        deltaBuilder.setName("members.User.value");
        deltaBuilder.addValueToRemove(userUid);
        attributes.add(deltaBuilder.build());

        // Call updateDelta to update the group
        ConnectorFacade facade = newFacade();
        facade.updateDelta(GROUP_OBJECT_CLASS, new Uid(groupUidToUpdate), attributes, null);

        // Retrieve and verify the updated object
        ListResultHandler handler = new ListResultHandler();
        Attribute attribute = AttributeBuilder.build(Uid.NAME, groupUidToUpdate);
        EqualsFilter filter = new EqualsFilter(attribute);

        facade.search(GROUP_OBJECT_CLASS, filter, handler, null);
        List<ConnectorObject> objects =  handler.getObjects();
        assertEquals(1, objects.size());

        ConnectorObject object = objects.get(0);
        Attribute memberOfAttr = object.getAttributeByName("members.User.value");
        assertNotNull(memberOfAttr);

        List<Object> grupos = memberOfAttr.getValue();
        assertFalse(grupos.contains(userUid));
    }
}
