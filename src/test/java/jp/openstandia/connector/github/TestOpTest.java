package jp.openstandia.connector.github;

import jp.openstandia.connector.github.testutil.AbstractEMUTest;
import org.identityconnectors.framework.api.ConnectorFacade;
import org.testng.annotations.Test;

public class TestOpTest extends AbstractEMUTest {

    @Test()
    public void shouldInitializeConnection() {
        ConnectorFacade facade = newFacade();
        facade.test();
    }
}
