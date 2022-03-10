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
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;

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


    private static final String XML_MAPS_RESOURCE_FILE_NAME = "smallMap.xml";

    /**
     * @brief parse xml file into a map object
     * @param filePath
     * @source https://mkyong.com/java/how-to-read-xml-file-in-java-dom-parser/
     */
    public Map parse(String filePath) {
        Map map = new Map();

        // Instantiate the Factory
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        try {
            // optional, but recommended
            // process XML securely, avoid attacks like XML External Entities (XXE)
            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

            // parse XML file
            DocumentBuilder db = dbf.newDocumentBuilder();

            Document doc = db.parse(getFileFromResource(XML_MAPS_RESOURCE_FILE_NAME));

            // optional, but recommended
            // http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
            doc.getDocumentElement().normalize();

            addIntersectionsToMap(map, doc);

        } catch (ParserConfigurationException | SAXException | IOException | URISyntaxException e) {
            e.printStackTrace();
        }

        return map;
    }

    /**
     * Add an intersection to the map from the document
     * @param m the map to update
     * @param d the document from which to get informations
     */
    private void addIntersectionsToMap(Map m, Document d) {
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

    /**
     * Create a file with the given resource file name
     * @param fileName the filename of the resource
     * @return new File from the resource
     * @throws URISyntaxException
     */
    private File getFileFromResource(String fileName) throws URISyntaxException {
        ClassLoader classLoader = getClass().getClassLoader();
        URL resource = classLoader.getResource(fileName);
        if (resource == null) {
            throw new IllegalArgumentException("File not found! " + fileName);
        } else {
            System.out.println(resource.getPath());
            return new File(resource.toURI());
        }
    }
}