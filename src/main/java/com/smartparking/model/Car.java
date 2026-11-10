package com.smartparking.model;

/**
 * Represents a standard four-wheeled passenger automobile.
 *
 * Demonstrates Task 7 concepts:
 * - Overriding calculateFee(long) to implement specialized car tariff
 * - Use of the @Override annotation for compiler verification
 * - Overriding toString() to augment base vehicle description with seating capacity
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
     * Calculates the parking fee for a standard car stay.
     * Charges Rs 30.0 per started hour.
     *
     * @param durationMinutes parking stay duration in minutes (must be non-negative)
     * @return calculated parking fee in INR
     */
    @Override
    public double calculateFee(long durationMinutes) {
        long billableHours = ParkingTariff.calculateStartedHours(durationMinutes);
        return billableHours * ParkingTariff.CAR_RATE_PER_HOUR;
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
