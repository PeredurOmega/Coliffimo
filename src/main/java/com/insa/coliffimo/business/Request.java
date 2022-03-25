package com.insa.coliffimo.business;

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.job.Shipment;

import java.util.Objects;
import java.util.UUID;

/**
 * Class representing a couple of points for a delivery (pickup point and delivery point).
 */
public class Request {
    /**
     * Intersection of the pickup.
     */
    private Intersection pickup;

    /**
     * Intersection of the delivery.
     */
    private Intersection delivery;

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
     *
     * @param pickup           Intersection of the pickup address.
     * @param delivery         Intersection of the delivery address.
     * @param pickupDuration   Time spent picking up (in ms).
     * @param deliveryDuration Time spent delivering (in ms).
     */
    public Request(Intersection pickup, Intersection delivery, int pickupDuration, int deliveryDuration) {
        this.pickup = pickup;
        this.delivery = delivery;
        this.pickupDuration = pickupDuration;
        this.deliveryDuration = deliveryDuration;
    }

    public Intersection getPickupAddress() {
        return pickup;
    }

    public void setPickupAddress(Intersection pickup) {
        this.pickup = pickup;
    }

    public Intersection getDeliveryAddress() {
        return delivery;
    }

    public void setDeliveryAddress(Intersection delivery) {
        this.delivery = delivery;
    }

    public int getPickupDuration() {
        return pickupDuration;
    }

    public void setPickupDuration(int pickupDuration) {
        this.pickupDuration = pickupDuration;
    }

    public int getDeliveryDuration() {
        return deliveryDuration;
    }

    public void setDeliveryDuration(int deliveryDuration) {
        this.deliveryDuration = deliveryDuration;
    }

    public Shipment asShipment() {
        return Shipment.Builder.newInstance(UUID.randomUUID().toString())
                .setPickupLocation(Location.newInstance(pickup.getLatitude(), pickup.getLongitude()))
                .setDeliveryLocation(Location.newInstance(delivery.getLatitude(), delivery.getLongitude()))
                .setDeliveryServiceTime(getDeliveryDuration() * 1000)
                .setPickupServiceTime(getDeliveryDuration() * 1000)
                .build();
    }

    @Override
    public String toString() {
        return "Request{" +
                "pickup=" + pickup.toString() +
                ", delivery=" + delivery.toString() +
                ", pickupDuration=" + pickupDuration +
                ", deliveryDuration=" + deliveryDuration +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Request request = (Request) o;
        return pickupDuration == request.pickupDuration && deliveryDuration == request.deliveryDuration && Objects.equals(pickup, request.pickup) && Objects.equals(delivery, request.delivery);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pickup, delivery, pickupDuration, deliveryDuration);
    }
}
