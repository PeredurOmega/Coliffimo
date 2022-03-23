package com.insa.coliffimo.router;

import com.insa.coliffimo.business.Intersection;
import com.insa.coliffimo.business.Map;
import com.insa.coliffimo.utils.XmlParser;

import java.io.File;
import java.util.HashMap;

public class MapResource {

    private final HashMap<Long, Intersection> intersections;

    public MapResource(File xmlMapResourceFile) {
        XmlParser xmlParser = new XmlParser();
        Map map = xmlParser.convertXmlToMap(xmlMapResourceFile);
        intersections = map.getListIntersections();
    }

    public HashMap<Long, Intersection> getIntersections() {
        return intersections;
    }
}
