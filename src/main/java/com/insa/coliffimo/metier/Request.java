package com.insa.coliffimo.metier;

/**
 * Class representing a couple of points for a delivery (pickup point and delivery point).
 */
public class Request {
    /**
     * Variable containing the identifiant of the pickup intersection.
     */
    private int pickupAddress;

    /**
     * Variable containing the identifiant of the delivery intersection.
     */
    private int deliveryAddress;

    /**
     * Variable representing the time needed for picking the pack up at the pickup point (in seconds).
     */
    private int pickupDuration;

    /**
     * Variable representing the time needed for delivering the pack at the pickup point (in seconds).
     */
    private int deliveryDuration;

    /**
     * Constructor of the class.
     * @param pickupAddress
     * @param deliveryAddress
     * @param pickupDuration
     * @param deliveryDuration
     */
    public Request(int pickupAddress, int deliveryAddress, int pickupDuration, int deliveryDuration){
        this.pickupAddress = pickupAddress;
        this.deliveryAddress = deliveryAddress;
        this.pickupDuration = pickupDuration;
        this.deliveryDuration = deliveryDuration;
    }

    public int getPickupAddress(){
        return pickupAddress;
    }

    public void setPickupAddress(int pickupAddress){
        this.pickupAddress = pickupAddress;
    }

    public int getDeliveryAddress(){
        return deliveryAddress;
    }

    public void setDeliveryAddress(int deliveryAddress){
        this.deliveryAddress = deliveryAddress;
    }

    public int getPickupDuration(){
        return pickupDuration;
    }

    public void setPickupDuration(int pickupDuration){
        this.pickupDuration = pickupDuration;
    }

    public int getDeliveryDuration(){
        return deliveryDuration;
    }

    public void setDeliveryDuration(int deliveryDuration){
        this.deliveryDuration = deliveryDuration;
    }

    @Override
    public String toString() {
        return "Request{" +
                "pickupAddress=" + pickupAddress +
                ", deliveryAddress=" + deliveryAddress +
                ", pickupDuration=" + pickupDuration +
                ", deliveryDuration=" + deliveryDuration +
                '}';
    }
}
