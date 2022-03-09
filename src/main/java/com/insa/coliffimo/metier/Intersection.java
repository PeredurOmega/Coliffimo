package com.insa.coliffimo.metier;

/**
 * Class representing an intersection between several streets on a map.
 */
public class Intersection {

    /**
     * Variable containing the identifiant of intersection.
     */
    private int id;

    /**
     * Variable containing the latitude of intersection.
     */
    private float latitude;

    /**
     * Variable containing the longitude of intersection.
     */
    private float longitude;

    /**
     * Constructor of the class.
     * @param id
     * @param latitude
     * @param longitude
     */
    public Intersection(int id, float latitude, float longitude){
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public float getLatitude() {
        return latitude;
    }

    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }

    public float getLongitude() {
        return longitude;
    }

    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }

    @Override
    public String toString() {
        return "Intersection{" +
                "id=" + id +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }

}
