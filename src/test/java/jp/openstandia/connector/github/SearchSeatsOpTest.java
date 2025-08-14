package jp.openstandia.connector.github;

import jp.openstandia.connector.github.testutil.AbstractEMUTest;
import org.identityconnectors.framework.api.ConnectorFacade;
import org.identityconnectors.framework.common.objects.*;
import org.identityconnectors.framework.common.objects.filter.EqualsFilter;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import java.util.List;

import static jp.openstandia.connector.github.GitHubCopilotSeatHandler.SEAT_OBJECT_CLASS;

public class SearchSeatsOpTest extends AbstractEMUTest {

    String seatUid = "";
    String seatName = "";

    @Test()
    public void shouldReturnAllSeats()  {
        ConnectorFacade facade = newFacade();
        ListResultHandler handler = new ListResultHandler();

        facade.search(SEAT_OBJECT_CLASS, null, handler, null);
        List<ConnectorObject> objects = handler.getObjects();
        AssertJUnit.assertTrue("Size: " + objects.size(), objects.size() > 1);
    }

    @Test()
    public void shouldReturnSeatByUid()  {
        ConnectorFacade facade = newFacade();
        ListResultHandler handler = new ListResultHandler();

        Attribute attribute = AttributeBuilder.build(Uid.NAME, seatUid);
        EqualsFilter filter = new EqualsFilter(attribute);

        facade.search(SEAT_OBJECT_CLASS, filter, handler, null);
        List<ConnectorObject> objects = handler.getObjects();
        AssertJUnit.assertEquals(1, objects.size());
    }

    @Test()
    public void shouldReturnSeatByName()  {
        ConnectorFacade facade = newFacade();
        ListResultHandler handler = new ListResultHandler();

        Attribute attribute = AttributeBuilder.build(Name.NAME, seatName);
        EqualsFilter filter = new EqualsFilter(attribute);

        facade.search(SEAT_OBJECT_CLASS, filter, handler, null);
        List<ConnectorObject> objects = handler.getObjects();
        AssertJUnit.assertEquals(1, objects.size());
    }
}
