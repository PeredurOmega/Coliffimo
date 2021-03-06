package com.insa.coliffimo;

import com.insa.coliffimo.business.Intersection;
import com.insa.coliffimo.business.Map;
import com.insa.coliffimo.business.PlanningRequest;
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

public class TestXmlParser {
    private static final String XML_MAP_RESOURCE_DIRECTORY_PATH = Paths.get("src", "test", "resources", "map").toAbsolutePath() + "/";
    private static final String XML_PLANNING_REQUEST_RESOURCE_DIRECTORY_PATH = Paths.get("src", "test", "resources", "planningRequest").toAbsolutePath() + "/";

    XmlParser xmlParser;

    @BeforeEach
    void setUp() {
        xmlParser = new XmlParser();
    }

    @ParameterizedTest
    @ValueSource(strings = {"map without intersection tag.xml", "map with empty map tag.xml"})
    @DisplayName("Test_ConvertXmlToMap_WithEmptyFile_ReturnsEmptyMap")
    void Test_ConvertXmlToMap_WithEmptyFile_ReturnsEmptyMap(String xmlMapResourceFileName) {
        String xmlMapResourceFilePath = XML_MAP_RESOURCE_DIRECTORY_PATH + xmlMapResourceFileName;

        Map expectedMap = new Map();

        Map actualMap = xmlParser.convertXmlToMap(new File(xmlMapResourceFilePath));

        Assertions.assertEquals(actualMap, expectedMap);
    }

    @ParameterizedTest
    @ValueSource(strings = {"map with intersections and other tags.xml", "map with only intersections tags.xml"})
    @DisplayName("Test_ConvertXmlToMap_WithIntersectionsAndOtherTags_ReturnsMap")
    void Test_ConvertXmlToMap_WithIntersectionsAndOtherTags_ReturnsMap(String xmlMapResourceFileName) {
        String xmlMapResourceFilePath = XML_MAP_RESOURCE_DIRECTORY_PATH + xmlMapResourceFileName;

        Map expectedMap = new Map();
        expectedMap.addIntersection(new Intersection(21992964L, (float) 45.74778, (float) 4.8682485));
        expectedMap.addIntersection(new Intersection(25327124L, (float) 45.74778, (float) 4.8682485));
        expectedMap.addIntersection(new Intersection(208769133L, (float) 45.759453, (float) 4.8698664));
        expectedMap.addIntersection(new Intersection(342873658L, (float) 45.76038, (float) 4.8775625));
        expectedMap.addIntersection(new Intersection(342873532L, (float) 45.76051, (float) 4.8783274));
        expectedMap.addIntersection(new Intersection(208769499L, (float) 45.760597, (float) 4.87622));
        expectedMap.addIntersection(new Intersection(975886496L, (float) 45.756874, (float) 4.8574047));

        Map actualMap = xmlParser.convertXmlToMap(new File(xmlMapResourceFilePath));

        Assertions.assertEquals(actualMap, expectedMap);
    }

    @Test
    @DisplayName("Test_ConvertXmlToPlanningRequest_WithEmptyFile_ReturnsEmptyPlanningRequest")
    void Test_ConvertXmlToPlanningRequest_WithEmptyFile_ReturnsEmptyPlanningRequest() {
        String xmlPlanningRequestResourceFileName = "planning request with empty planning request tag.xml";
        String xmlPlanningRequestResourceFilePath = XML_PLANNING_REQUEST_RESOURCE_DIRECTORY_PATH + xmlPlanningRequestResourceFileName;
        PlanningRequest expectedPlanningRequest = new PlanningRequest();

        String xmlMapResourceFilePath = XML_MAP_RESOURCE_DIRECTORY_PATH + "map with intersections and other tags.xml";
        Map map = xmlParser.convertXmlToMap(new File(xmlMapResourceFilePath));
        PlanningRequest actualPlanningRequest = xmlParser.convertXmlToPlanningRequest(new File(xmlPlanningRequestResourceFilePath), map.getListIntersections());

        Assertions.assertEquals(actualPlanningRequest, expectedPlanningRequest);
    }

    @Test
    @DisplayName("Test_ConvertXmlToPlanningRequest_WithDepotAndNoRequest_ReturnsPlanningRequest")
    void Test_ConvertXmlToPlanningRequest_WithDepotAndNoRequest_ReturnsPlanningRequest() {
        String xmlPlanningRequestResourceFileName = "planning request with depot and no request tag.xml";
        String xmlPlanningRequestResourceFilePath = XML_PLANNING_REQUEST_RESOURCE_DIRECTORY_PATH + xmlPlanningRequestResourceFileName;
        String xmlMapResourceFilePath = XML_MAP_RESOURCE_DIRECTORY_PATH + "map with intersections and other tags.xml";
        Map map = xmlParser.convertXmlToMap(new File(xmlMapResourceFilePath));
        PlanningRequest actualPlanningRequest = xmlParser.convertXmlToPlanningRequest(new File(xmlPlanningRequestResourceFilePath), map.getListIntersections());

        Assertions.assertEquals(25327124L, actualPlanningRequest.getDepot().getId().longValue());
        Assertions.assertEquals(LocalTime.parse("16:20:30"), actualPlanningRequest.getDepotDepartureLocalTime());
    }

    @Test
    @DisplayName("Test_ConvertXmlToPlanningRequest_WithDepotAndOnlyOneRequest_ReturnsPlanningRequest")
    void Test_ConvertXmlToPlanningRequest_WithDepotAndOnlyOneRequest_ReturnsPlanningRequest() {
        String xmlPlanningRequestResourceFileName = "planning request with depot and only one request tag.xml";
        String xmlPlanningRequestResourceFilePath = XML_PLANNING_REQUEST_RESOURCE_DIRECTORY_PATH + xmlPlanningRequestResourceFileName;
        String xmlMapResourceFilePath = XML_MAP_RESOURCE_DIRECTORY_PATH + "map with intersections and other tags.xml";
        Map map = xmlParser.convertXmlToMap(new File(xmlMapResourceFilePath));
        PlanningRequest actualPlanningRequest = xmlParser.convertXmlToPlanningRequest(new File(xmlPlanningRequestResourceFilePath), map.getListIntersections());

        Assertions.assertEquals(25327124L, actualPlanningRequest.getDepot().getId().longValue());
        Assertions.assertEquals(LocalTime.parse("16:20:30"), actualPlanningRequest.getDepotDepartureLocalTime());
        Assertions.assertEquals(1, actualPlanningRequest.getListRequests().size());
    }

    @Test
    @DisplayName("Test_ConvertXmlToPlanningRequest_WithDepotAndRequests_ReturnsPlanningRequest")
    void Test_ConvertXmlToPlanningRequest_WithDepotAndRequests_ReturnsPlanningRequest() {
        String xmlPlanningRequestResourceFileName = "planning request with depot and request tags.xml";
        String xmlPlanningRequestResourceFilePath = XML_PLANNING_REQUEST_RESOURCE_DIRECTORY_PATH + xmlPlanningRequestResourceFileName;
        String xmlMapResourceFilePath = XML_MAP_RESOURCE_DIRECTORY_PATH + "map with intersections and other tags.xml";
        Map map = xmlParser.convertXmlToMap(new File(xmlMapResourceFilePath));
        PlanningRequest actualPlanningRequest = xmlParser.convertXmlToPlanningRequest(new File(xmlPlanningRequestResourceFilePath), map.getListIntersections());

        Assertions.assertEquals(25327124L, actualPlanningRequest.getDepot().getId().longValue());
        Assertions.assertEquals(LocalTime.parse("16:20:30"), actualPlanningRequest.getDepotDepartureLocalTime());
        Assertions.assertEquals(9, actualPlanningRequest.getListRequests().size());
    }

    @ParameterizedTest
    @CsvSource({
            "planning request with one digit departure time.xml, 08:01:04",
            "planning request with one digit hour departure time.xml, 08:01:04",
            "planning request with one digit minute departure time.xml, 16:04:40",
            "planning request with one digit second departure time.xml, 16:08:03",
    })
    @DisplayName("Test_ConvertXmlToPlanningRequest_WithOneDigitDepartureTime_ReturnsPlanningRequest")
    void Test_ConvertXmlToPlanningRequest_WithOneDigitDepartureTime_ReturnsPlanningRequest(String xmlPlanningRequestResourceFileName, String departureTime) {
        String xmlPlanningRequestResourceFilePath = XML_PLANNING_REQUEST_RESOURCE_DIRECTORY_PATH + xmlPlanningRequestResourceFileName;
        String xmlMapResourceFilePath = XML_MAP_RESOURCE_DIRECTORY_PATH + "map with intersections and other tags.xml";
        Map map = xmlParser.convertXmlToMap(new File(xmlMapResourceFilePath));
        PlanningRequest actualPlanningRequest = xmlParser.convertXmlToPlanningRequest(new File(xmlPlanningRequestResourceFilePath), map.getListIntersections());

        Assertions.assertEquals(25327124L, actualPlanningRequest.getDepot().getId().longValue());
        Assertions.assertEquals(LocalTime.parse(departureTime), actualPlanningRequest.getDepotDepartureLocalTime());
        Assertions.assertEquals(1, actualPlanningRequest.getListRequests().size());
    }
}