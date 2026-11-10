package com.smartparking.model;

/**
 * Represents a standard four-wheeled passenger automobile.
 *
 * Demonstrates:
 * - Direct inheritance from the abstract Vehicle superclass (Hierarchical inheritance)
 * - Constructor chaining to the superclass via super(...)
 * - Addition of specialized subclass state (seatingCapacity)
 * - Reuse of inherited behaviour via super.getVehicleInfo()
 */
public class Car extends Vehicle {
    private final int seatingCapacity;

    /**
     * Parameterized constructor for Car.
     * Invokes the superclass constructor with the category "CAR".
     *
     * @param registrationNumber unique license plate number
     * @param seatingCapacity    number of passenger seats (must be strictly positive)
     */
    public Car(String registrationNumber, int seatingCapacity) {
        super(registrationNumber, "CAR");
        if (seatingCapacity <= 0) {
            throw new IllegalArgumentException("Seating capacity must be strictly positive.");
        }
        this.seatingCapacity = seatingCapacity;
    }

    /**
     * Returns the seating capacity of the car.
     *
     * @return passenger seat count
     */
    public int getSeatingCapacity() {
        return seatingCapacity;
    }

    /**
     * Specialized display method demonstrating invocation of inherited superclass behaviour.
     *
     * @return complete car details
     */
    public String getDetailedInfo() {
        return super.getVehicleInfo() + ", Seating Capacity: " + seatingCapacity;
    }

    @Override
    public String toString() {
        return "Car[" + super.toString() + ", seats=" + seatingCapacity + "]";
    }
}
