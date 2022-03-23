package com.insa.coliffimo;

import com.insa.coliffimo.metier.Intersection;
import com.insa.coliffimo.metier.Map;
import com.insa.coliffimo.metier.PlanningRequest;
import com.insa.coliffimo.metier.Request;
import com.insa.coliffimo.utils.XmlParser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.nio.file.Paths;
import java.time.LocalTime;

public class TestRequest {
    private static final String XML_MAP_RESOURCE_DIRECTORY_PATH = Paths.get("src", "test", "resources", "map").toAbsolutePath() + "/";
    private static final String XML_PLANNING_REQUEST_RESOURCE_DIRECTORY_PATH = Paths.get("src", "test", "resources", "planningRequest").toAbsolutePath() + "/";

    XmlParser xmlParser;

    @BeforeEach
    void setUp() {
        xmlParser = new XmlParser();
    }

    @Test
    void Test_Update_Coordinates_With_Map(){
        String xmlMapResourceFilePath = XML_MAP_RESOURCE_DIRECTORY_PATH + "map with only intersections tags.xml";
        String xmlPlanningRequestResourceFilePath = XML_PLANNING_REQUEST_RESOURCE_DIRECTORY_PATH + "planning request for update.xml";
        Map m = xmlParser.ConvertXmlToMap(new File(xmlMapResourceFilePath));
        System.out.println(m);
        PlanningRequest pr = xmlParser.ConvertXmlToPlanningRequest(new File(xmlPlanningRequestResourceFilePath));
        Request r = pr.getListRequests().get(0);
        Assertions.assertEquals(r.getLatitudeDelivery(), 0);
        Assertions.assertEquals(r.getLongitudeDelivery(), 0);
        Assertions.assertEquals(r.getLatitudePickup(), 0);
        Assertions.assertEquals(r.getLongitudePickup(), 0);

        pr.updateCoordinatesRequests(m);

        Intersection delivery = m.getListIntersections().get(r.getDeliveryAddress());
        System.out.println(delivery);
        Intersection pickup = m.getListIntersections().get(r.getPickupAddress());
        Assertions.assertEquals(r.getLatitudeDelivery(), delivery.getLatitude());
        Assertions.assertEquals(r.getLongitudeDelivery(), delivery.getLongitude());
        Assertions.assertEquals(r.getLatitudePickup(), pickup.getLatitude());
        Assertions.assertEquals(r.getLongitudePickup(), pickup.getLongitude());

    }

}