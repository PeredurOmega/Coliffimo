package com.insa.coliffimo.utils;

import com.insa.coliffimo.metier.Intersection;
import com.insa.coliffimo.metier.Map;
import com.insa.coliffimo.metier.Segment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;

public class XmlParser {
    private static final String INTERSECTION_TAG_NAME = "intersection";
    private static final String SEGMENT_TAG_NAME = "segment";
    private static final String INTERSECTION_ID_ATTRIBUTE_NAME = "id";
    private static final String INTERSECTION_LONGITUDE_ATTRIBUTE_NAME = "longitude";
    private static final String INTERSECTION_LATITUDE_ATTRIBUTE_NAME = "latitude";
    private static final String SEGMENT_NAME_ATTRIBUTE_NAME = "name";
    private static final String SEGMENT_LENGTH_ATTRIBUTE_NAME = "length";
    private static final String SEGMENT_ORIGIN_ATTRIBUTE_NAME = "origin";
    private static final String SEGMENT_DESTINATION_ATTRIBUTE_NAME = "destination";

    private static final String FILENAME = "/Users/louisepietropaoli/git/pld-agile/fichiersXML/smallMap.xml";

    /**
     * @param filePath todo : mentionner source
     * @source https://mkyong.com/java/how-to-read-xml-file-in-java-dom-parser/
     */
    public Map parse(String filePath) {
        filePath = FILENAME;
        Map map = new Map();

        // Instantiate the Factory
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        try {
            // optional, but recommended
            // process XML securely, avoid attacks like XML External Entities (XXE)
            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

            // parse XML file
            DocumentBuilder db = dbf.newDocumentBuilder();

            Document doc = db.parse(new File(FILENAME));

            // optional, but recommended
            // http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
            doc.getDocumentElement().normalize();

            addSegmentsToMap(map, doc);
            addIntersectionsToMap(map, doc);

        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }

        return map;
    }

    private void addSegmentsToMap(Map m, Document d)
    {
        NodeList nodesIntersection = d.getElementsByTagName(INTERSECTION_TAG_NAME);

        for (int temp = 0; temp < nodesIntersection.getLength(); temp++) {
            Node node = nodesIntersection.item(temp);

            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;

                Long id = Long.parseLong(element.getAttribute(INTERSECTION_ID_ATTRIBUTE_NAME));
                float latitude = Float.parseFloat(element.getAttribute(INTERSECTION_LATITUDE_ATTRIBUTE_NAME));
                float longitude = Float.parseFloat(element.getAttribute(INTERSECTION_LONGITUDE_ATTRIBUTE_NAME));

                m.addIntersection(new Intersection(id, latitude, longitude));
            }
        }
    }

    private void addIntersectionsToMap(Map m, Document d)
    {
        NodeList nodesSegment = d.getElementsByTagName(SEGMENT_TAG_NAME);

        for (int temp = 0; temp < nodesSegment.getLength(); temp++) {
            Node node = nodesSegment.item(temp);

            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;

                String name = element.getAttribute(SEGMENT_NAME_ATTRIBUTE_NAME);
                float length = Float.parseFloat(element.getAttribute(SEGMENT_LENGTH_ATTRIBUTE_NAME));
                Long origin = Long.parseLong(element.getAttribute(SEGMENT_ORIGIN_ATTRIBUTE_NAME));
                Long destination = Long.parseLong(element.getAttribute(SEGMENT_DESTINATION_ATTRIBUTE_NAME));

                m.addSegment(new Segment(name, length, origin, destination));
            }
        }
    }

}