package com.insa.coliffimo;

import com.insa.coliffimo.metier.Intersection;
import com.insa.coliffimo.metier.Map;
import com.insa.coliffimo.utils.XmlParser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.nio.file.Paths;

public class TestXmlParser {
    private static final String XML_MAP_RESOURCE_DIRECTORY_PATH = Paths.get("src", "test", "resources").toAbsolutePath() + "/";

    XmlParser xmlParser;

    @BeforeEach
    void setUp() {
        xmlParser = new XmlParser();
    }

    @ParameterizedTest
    @ValueSource(strings = { "empty file.xml", "empty file with xml tag.xml", "empty file with xml and map tags.xml" })
    @DisplayName("Test_ConvertXmlToMap_WithEmptyFile_ReturnsEmptyMap")
    void Test_ConvertXmlToMap_WithEmptyFile_ReturnsEmptyMap(String xmlMapResourceFileName) {
        String xmlMapResourceFilePath = XML_MAP_RESOURCE_DIRECTORY_PATH + xmlMapResourceFileName;
        Map expectedMap = new Map();

        Map actualMap = xmlParser.ConvertXmlToMap(new File(xmlMapResourceFilePath));

        Assertions.assertEquals(actualMap, expectedMap);
    }

    @ParameterizedTest
    @ValueSource(strings = { "map with intersections and other tags.xml", "map with only intersections tags.xml" })
    @DisplayName("Test_ConvertXmlToMap_WithIntersectionsAndOtherTags_ReturnsMap")
    void Test_ConvertXmlToMap_WithIntersectionsAndOtherTags_ReturnsMap(String xmlMapResourceFileName) {
        String xmlMapResourceFilePath = XML_MAP_RESOURCE_DIRECTORY_PATH + xmlMapResourceFileName;
        Map expectedMap = new Map();
        expectedMap.addIntersection(new Intersection(21992964L, (float)45.74778, (float)4.8682485));
        expectedMap.addIntersection(new Intersection(208769133L, (float)45.759453, (float)4.8698664));
        expectedMap.addIntersection(new Intersection(342873658L, (float)45.76038, (float)4.8775625));
        expectedMap.addIntersection(new Intersection(342873532L, (float)45.76051, (float)4.8783274));
        expectedMap.addIntersection(new Intersection(208769499L, (float)45.760597, (float)4.87622));
        expectedMap.addIntersection(new Intersection(975886496L, (float)45.756874, (float)4.8574047));

        Map actualMap = xmlParser.ConvertXmlToMap(new File(xmlMapResourceFilePath));

        Assertions.assertEquals(actualMap, expectedMap);
    }
}
