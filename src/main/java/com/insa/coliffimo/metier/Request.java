package com.insa.coliffimo.metier;

import java.util.Objects;

/**
 * Class representing a couple of points for a delivery (pickup point and delivery point).
 */
public class Request {
    /**
     * Variable containing the identifiant of the pickup intersection.
     */
    private Long pickupAddress;

    /**
     * Variable containing the identifiant of the delivery intersection.
     */
    private Long deliveryAddress;

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
    public Request(Long pickupAddress, Long deliveryAddress, int pickupDuration, int deliveryDuration){
        this.pickupAddress = pickupAddress;
        this.deliveryAddress = deliveryAddress;
        this.pickupDuration = pickupDuration;
        this.deliveryDuration = deliveryDuration;
    }

    public Long getPickupAddress(){
        return pickupAddress;
    }

    public void setPickupAddress(Long pickupAddress){
        this.pickupAddress = pickupAddress;
    }

    public Long getDeliveryAddress(){
        return deliveryAddress;
    }

    public void setDeliveryAddress(Long deliveryAddress){
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Request request = (Request) o;
        return pickupDuration == request.pickupDuration && deliveryDuration == request.deliveryDuration && Objects.equals(pickupAddress, request.pickupAddress) && Objects.equals(deliveryAddress, request.deliveryAddress);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pickupAddress, deliveryAddress, pickupDuration, deliveryDuration);
    }
}
