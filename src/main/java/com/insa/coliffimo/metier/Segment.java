package com.insa.coliffimo.metier;

/**
 * Class representing a section of street between two intersections.
 */
public class Segment {

    /**
     * Variable containing the name of the street.
     */
    private String name;

    /**
     * Variable containing the length of the segment.
     */
    private float length;

    /**
     * Variable containing the identifiant of the origin intersection.
     */
    private int origin;

    /**
     * Variable containing the identifiant of the destination intersection.
     */
    private int destination;

    /**
     * Constructor of the class.
     * @param name
     * @param length
     * @param origin
     * @param destination
     */
    public Segment(String name, float length, int origin, int destination) {
        this.name = name;
        this.length = length;
        this.origin = origin;
        this.destination = destination;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getLength() {
        return length;
    }

    public void setLength(float length) {
        this.length = length;
    }

    public int getOrigin() {
        return origin;
    }

    public void setOrigin(int origin) {
        this.origin = origin;
    }

    public int getDestination() {
        return destination;
    }

    public void setDestination(int destination) {
        this.destination = destination;
    }

    @Override
    public String toString() {
        return "Segment{" +
                "name='" + name + '\'' +
                ", length=" + length +
                ", origin=" + origin +
                ", destination=" + destination +
                '}';
    }

}
