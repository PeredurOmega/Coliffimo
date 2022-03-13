package com.insa.coliffimo.utils;

import com.insa.coliffimo.metier.Intersection;
import com.insa.coliffimo.metier.Map;
import com.insa.coliffimo.metier.PlanningRequest;
import com.insa.coliffimo.metier.Request;
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
import java.time.LocalTime;

public class XmlParser {
    /**
     * Constants for map xml file
     */
    private static final String INTERSECTION_TAG_NAME = "intersection";
    private static final String INTERSECTION_ID_ATTRIBUTE_NAME = "id";
    private static final String INTERSECTION_LONGITUDE_ATTRIBUTE_NAME = "longitude";
    private static final String INTERSECTION_LATITUDE_ATTRIBUTE_NAME = "latitude";

    /**
     * Constants for planning request xml file
     */
    private static final String PLANNING_REQUEST_TAG_NAME = "planningRequest";
    private static final String PLANNING_REQUEST_DEPOT_TAG_NAME = "depot";
    private static final String PLANNING_REQUEST_REQUEST_TAG_NAME = "request";
    private static final String PLANNING_REQUEST_DEPOT_ADDRESS_ATTRIBUTE_NAME = "address";
    private static final String PLANNING_REQUEST_DEPOT_DEPARTURE_TIME_ATTRIBUTE_NAME = "departureTime";
    private static final String PLANNING_REQUEST_REQUEST_PICKUP_ADDRESS_ATTRIBUTE_NAME = "pickupAddress";
    private static final String PLANNING_REQUEST_REQUEST_DELIVERY_ADDRESS_ATTRIBUTE_NAME = "deliveryAddress";
    private static final String PLANNING_REQUEST_REQUEST_PICKUP_DURATION_ADDRESS_ATTRIBUTE_NAME = "pickupDuration";
    private static final String PLANNING_REQUEST_REQUEST_DELIVERY_DURATION_ADDRESS_ATTRIBUTE_NAME = "deliveryDuration";

    /**
     * Parse xml file into a map object
     * @param xmlFile The xml file to parse
     * @return Map The map based on xml file
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
     * Parse xml file into a planning reuqest object
     * @param xmlFile The xml file to parse
     * @return PlanningRequest The planningRequest based on xml file
     * @source https://mkyong.com/java/how-to-read-xml-file-in-java-dom-parser/
     */
    public PlanningRequest ConvertXmlToPlanningRequest(File xmlFile) {
        PlanningRequest planningRequest = new PlanningRequest();

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

            setDepotIntoPlanningRequest(planningRequest, doc);
            addRequestsToPlanningRequest(planningRequest, doc);

        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }

        return planningRequest;
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

    /**
     * Add requests to the planning request from the document
     * @param p The planning request to update
     * @param d The document from which to get information
     */
    private void addRequestsToPlanningRequest(PlanningRequest p, Document d)
    {
        NodeList nodesRequests = d.getElementsByTagName(PLANNING_REQUEST_REQUEST_TAG_NAME);

        for (int i = 0; i < nodesRequests.getLength(); i++) {
            Node node = nodesRequests.item(i);

            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;

                Long pickupAddress = Long.parseLong(element.getAttribute(PLANNING_REQUEST_REQUEST_PICKUP_ADDRESS_ATTRIBUTE_NAME));
                Long deliveryAddress = Long.parseLong(element.getAttribute(PLANNING_REQUEST_REQUEST_DELIVERY_ADDRESS_ATTRIBUTE_NAME));
                int pickupDuration = Integer.parseInt(element.getAttribute(PLANNING_REQUEST_REQUEST_PICKUP_DURATION_ADDRESS_ATTRIBUTE_NAME));
                int deliveryDuration = Integer.parseInt(element.getAttribute(PLANNING_REQUEST_REQUEST_DELIVERY_DURATION_ADDRESS_ATTRIBUTE_NAME));

                p.addRequest(new Request(pickupAddress, deliveryAddress, pickupDuration, deliveryDuration));
            }
        }
    }

    /**
     * Set the depot adress and departure local time into the planning request from the document
     * @param p The planning request to update
     * @param d The document from which to get information
     */
    private void setDepotIntoPlanningRequest(PlanningRequest p, Document d)
    {
        char TIME_SEPARATOR = ':';
        Node nodeDepot = d.getElementsByTagName(PLANNING_REQUEST_DEPOT_TAG_NAME).item(0);

        if (nodeDepot != null && nodeDepot.getNodeType() == Node.ELEMENT_NODE) {
            Element element = (Element) nodeDepot;

            Long depotAddress = Long.parseLong(element.getAttribute(PLANNING_REQUEST_DEPOT_ADDRESS_ATTRIBUTE_NAME));

            String depotDepartureLocalTimeStr = element.getAttribute(PLANNING_REQUEST_DEPOT_DEPARTURE_TIME_ATTRIBUTE_NAME);
            String[] depotDepartureLocalTimeStrSplit = depotDepartureLocalTimeStr.split(":");
            String depotDepartureLocalTimeWithValidFormat = new String();
            for (int i = 0; i < depotDepartureLocalTimeStrSplit.length; i++) {
                if (depotDepartureLocalTimeStrSplit[i].length() == 1) {
                    depotDepartureLocalTimeStrSplit[i] = "0" + depotDepartureLocalTimeStrSplit[i];
                }
                depotDepartureLocalTimeWithValidFormat += depotDepartureLocalTimeStrSplit[i];
                if (i < depotDepartureLocalTimeStrSplit.length - 1) {
                    depotDepartureLocalTimeWithValidFormat += TIME_SEPARATOR;
                }
            }
            LocalTime depotDepartureLocalTime = LocalTime.parse(depotDepartureLocalTimeWithValidFormat);

            p.setDepotAddress(depotAddress);
            p.setDepotDepartureLocalTime(depotDepartureLocalTime);
        }
    }
}