package jp.openstandia.connector.github;

import jp.openstandia.connector.github.testutil.AbstractEMUTest;
import org.identityconnectors.framework.api.ConnectorFacade;
import org.identityconnectors.framework.common.objects.*;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import java.util.HashSet;
import java.util.Set;

import static jp.openstandia.connector.github.GitHubEMUUserHandler.USER_OBJECT_CLASS;

public class CreateUserOpTest extends AbstractEMUTest {

    private Set<Attribute> userEntry() {

        Set<Attribute> attributeSet = new HashSet<>();
        attributeSet.add(AttributeBuilder.build(Name.NAME, ""));
        attributeSet.add(AttributeBuilder.build("externalId", ""));
        attributeSet.add(AttributeBuilder.build("displayName", ""));
        attributeSet.add(AttributeBuilder.build("primaryEmail", ""));
        attributeSet.add(AttributeBuilder.build("primaryRole", "User"));
        attributeSet.add(AttributeBuilder.build(OperationalAttributes.ENABLE_NAME, true));
        return attributeSet;
    }

    @Test()
    public void shouldCreateOrReturnExistentUser() {
        ConnectorFacade facade = newFacade();
        Uid uid = facade.create(USER_OBJECT_CLASS, userEntry(), null);
        AssertJUnit.assertNotNull(uid);
    }
}
