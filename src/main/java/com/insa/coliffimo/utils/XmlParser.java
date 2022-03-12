package com.insa.coliffimo.utils;

import com.insa.coliffimo.metier.Intersection;
import com.insa.coliffimo.metier.Map;
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

public class XmlParser {
    private static final String INTERSECTION_TAG_NAME = "intersection";
    private static final String INTERSECTION_ID_ATTRIBUTE_NAME = "id";
    private static final String INTERSECTION_LONGITUDE_ATTRIBUTE_NAME = "longitude";
    private static final String INTERSECTION_LATITUDE_ATTRIBUTE_NAME = "latitude";

    /**
     * Parse xml file into a map object
     * @param xmlFile The xml file to parse
     * @source https://mkyong.com/java/how-to-read-xml-file-in-java-dom-parser/
     */
    public Map ConvertXmlToMap(File xmlFile) {
        Map map = new Map();

        // Instantiate the Factory
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        try {
            // optional, but recommended
            // process XML securely, avoid attacks like XML External Entities (XXE)
            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

            // parse XML file
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(xmlFile);

            // optional, but recommended
            // http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
            doc.getDocumentElement().normalize();

            addIntersectionsToMap(map, doc);

        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }

        return map;
    }

    /**
     * Add an intersection to the map from the document
     * @param m The map to update
     * @param d The document from which to get information
     */
    private void addIntersectionsToMap(Map m, Document d)
    {
        NodeList nodesIntersection = d.getElementsByTagName(INTERSECTION_TAG_NAME);

        for (int i = 0; i < nodesIntersection.getLength(); i++) {
            Node node = nodesIntersection.item(i);

            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;

                Long id = Long.parseLong(element.getAttribute(INTERSECTION_ID_ATTRIBUTE_NAME));
                float latitude = Float.parseFloat(element.getAttribute(INTERSECTION_LATITUDE_ATTRIBUTE_NAME));
                float longitude = Float.parseFloat(element.getAttribute(INTERSECTION_LONGITUDE_ATTRIBUTE_NAME));

                m.addIntersection(new Intersection(id, latitude, longitude));
            }
        }
    }
}