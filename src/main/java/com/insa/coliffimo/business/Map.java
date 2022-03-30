package com.insa.coliffimo.business;

import java.util.HashMap;
import java.util.Objects;

/**
 * Class representing a map of intersections.
 *
 * @see com.insa.coliffimo.business.Intersection
 */
public class Map {
    /**
     * Variable containing the list of intersections.
     * Hashmap with intersection id as hashmap key and intersection as hashmap value.
     */
    private final HashMap<Long, Intersection> listIntersections = new HashMap<>();

    public HashMap<Long, Intersection> getListIntersections() {
        return listIntersections;
    }

    /**
     * Add Intersection to the hashmap of intersections of the map.
     *
     * @param i : the intersection to add.
     */
    public void addIntersection(Intersection i) {
        this.listIntersections.put(i.getId(), i);
    }

    /**
     * Find the closest intersection of the list from a point's coordinates.
     *
     * @param lat : the latitude of the point to find an intersection
     * @param longi : the longitude of the point to find an intersection
     */
    public Intersection findClosestIntersection(float lat, float longi){
        Intersection closest = null;
        double closestDistance = 100000;
        for (Intersection i : listIntersections.values()) {
            double distanceI = Math.pow(lat-i.getLatitude(),2.0) + Math.pow(longi-i.getLongitude(),2.0);
            if(distanceI<closestDistance){
                closest = i;
                closestDistance = distanceI;
            }
        }
        return closest;
    }

    @Override
    public String toString() {
        return "Map{" +
                "listIntersections=" + listIntersections +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Map map = (Map) o;
        return Objects.equals(listIntersections, map.listIntersections);
    }

    @Override
    public int hashCode() {
        return Objects.hash(listIntersections);
    }
}
