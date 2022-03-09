package com.insa.coliffimo.metier;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Class representing a map of segments and intersections.
 *
 * @see com.insa.coliffimo.metier.Segment
 * @see com.insa.coliffimo.metier.Intersection
 */
public class Map {

    /**
     * Variable containing the list of segments.
     */
    private ArrayList<Segment> listSegments;

    /**
     * Variable containing the list of intersections.
     * Hashmap with intersection id as hashmap key and intersection as hashmap value.
     */
    private HashMap<Integer, Intersection> listIntersections;

    /**
     * Constructor of the class.
     */
    public Map(){
        this.listSegments = new ArrayList<>();
    }

    public ArrayList<Segment> getListSegments() {
        return listSegments;
    }

    public HashMap<Integer, Intersection> getListIntersections() {
        return listIntersections;
    }

    @Override
    public String toString() {
        return "Map{" +
                "listSegments=" + listSegments +
                ", listIntersections=" + listIntersections +
                '}';
    }

    /**
     * Method for add Segment to the list of segments of the map.
     * @param s : the segment to add.
     */
    public void addSegment(Segment s){
        this.listSegments.add(s);
    }

    /**
     * Method for add Intersection to the hashmap of intersections of the map.
     * @param i : the intersection to add.
     */
    public void addIntersection(Intersection i){
        this.listIntersections.put(i.getId(), i);
    }
}
