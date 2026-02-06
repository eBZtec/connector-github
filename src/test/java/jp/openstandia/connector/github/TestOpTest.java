package jp.openstandia.connector.github;

import jp.openstandia.connector.github.testutil.AbstractEMUTest;
import org.identityconnectors.framework.api.ConnectorFacade;
import org.junit.jupiter.api.Test;

class TestOpTest extends AbstractEMUTest {

    @Test()
    void shouldInitializeConnection() {
        ConnectorFacade facade = newFacade();
        facade.test();
    }
}

