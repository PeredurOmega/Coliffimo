package com.insa.coliffimo.metier;

import java.util.ArrayList;

/**
 * Class representing a map of segments.
 *
 * @see com.insa.coliffimo.metier.Segment
 */
public class Map {

    /**
     * Variable containing the list of segments.
     */
    private ArrayList<Segment> listSegments;

    /**
     * Constructor of the class.
     */
    public Map(){
        this.listSegments = new ArrayList<>();
    }

    public ArrayList<Segment> getListSegments() {
        return listSegments;
    }

    public void setListSegments(ArrayList<Segment> listSegments) {
        this.listSegments = listSegments;
    }

    @Override
    public String toString() {
        return "Map{" +
                "listSegments=" + listSegments +
                '}';
    }

    /**
     * Method for add Segment to the list of the map.
     * @param s : the segment to add.
     */
    public void add(Segment s){
        this.listSegments.add(s);
    }

}
