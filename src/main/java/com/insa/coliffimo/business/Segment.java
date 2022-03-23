package com.insa.coliffimo.business;

import java.util.Objects;

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
     * Variable containing the id of the origin intersection.
     */
    private Long origin;

    /**
     * Variable containing the id of the destination intersection.
     */
    private Long destination;

    /**
     * Constructor of the class.
     * @param name Name of the segment.
     * @param length Length of the segment (in meters).
     * @param origin Intersection's id of the origin.
     * @param destination Intersection's id of the destination.
     */
    public Segment(String name, float length, Long origin, Long destination) {
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

    public Long getOrigin() {
        return origin;
    }

    public void setOrigin(Long origin) {
        this.origin = origin;
    }

    public Long getDestination() {
        return destination;
    }

    public void setDestination(Long destination) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Segment segment = (Segment) o;
        return Float.compare(segment.length, length) == 0 && Objects.equals(name, segment.name) && Objects.equals(origin, segment.origin) && Objects.equals(destination, segment.destination);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, length, origin, destination);
    }
}
