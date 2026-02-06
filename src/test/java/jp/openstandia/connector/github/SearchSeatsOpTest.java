package jp.openstandia.connector.github;

import jp.openstandia.connector.github.testutil.AbstractEMUTest;
import org.identityconnectors.framework.api.ConnectorFacade;
import org.identityconnectors.framework.common.objects.*;
import org.identityconnectors.framework.common.objects.filter.EqualsFilter;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static jp.openstandia.connector.github.GitHubCopilotSeatHandler.SEAT_OBJECT_CLASS;

import java.util.List;

class SearchSeatsOpTest extends AbstractEMUTest {

    String seatUid = "";
    String seatName = "";

    @Test()
    void shouldReturnAllSeats()  {
        ConnectorFacade facade = newFacade();
        ListResultHandler handler = new ListResultHandler();

        facade.search(SEAT_OBJECT_CLASS, null, handler, null);
        List<ConnectorObject> objects = handler.getObjects();
        assertTrue(objects.size() > 1, "Size: " + objects.size());
    }

    @Test()
    void shouldReturnSeatByUid()  {
        ConnectorFacade facade = newFacade();
        ListResultHandler handler = new ListResultHandler();

        Attribute attribute = AttributeBuilder.build(Uid.NAME, seatUid);
        EqualsFilter filter = new EqualsFilter(attribute);

        facade.search(SEAT_OBJECT_CLASS, filter, handler, null);
        List<ConnectorObject> objects = handler.getObjects();
        assertEquals(1, objects.size());
    }

    @Test()
    void shouldReturnSeatByName()  {
        ConnectorFacade facade = newFacade();
        ListResultHandler handler = new ListResultHandler();

        Attribute attribute = AttributeBuilder.build(Name.NAME, seatName);
        EqualsFilter filter = new EqualsFilter(attribute);

        facade.search(SEAT_OBJECT_CLASS, filter, handler, null);
        List<ConnectorObject> objects = handler.getObjects();
        assertEquals(1, objects.size());
    }
}
