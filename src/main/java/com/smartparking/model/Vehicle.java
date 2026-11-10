package com.smartparking.model;

import java.util.Objects;

/**
 * Abstract superclass representing a generic vehicle in the Campus Smart-Parking System.
 *
 * Encapsulates the common state and behaviour shared by all vehicle categories:
 * - Registration number (unique license plate identifier)
 * - Vehicle category identifier (e.g., TWO_WHEELER, CAR, ELECTRIC_CAR)
 *
 * Demonstrates Task 7 concepts:
 * - Abstract method contract for specialized fee calculation (calculateFee)
 * - Use of the final keyword for invariant accessors to protect state encapsulation
 * - Overridden Object-class methods: equals(), hashCode(), and toString()
 */
public abstract class Vehicle {
    private final String registrationNumber;
    private final String vehicleType;

    /**
     * Parameterized constructor for the Vehicle abstract superclass.
     * Enforces fail-fast validation to ensure every vehicle has valid identification.
     *
     * @param registrationNumber unique vehicle registration number / license plate
     * @param vehicleType        category of vehicle (non-null, non-blank)
     */
    public Vehicle(String registrationNumber, String vehicleType) {
        if (registrationNumber == null || registrationNumber.isBlank()) {
            throw new IllegalArgumentException("Vehicle registration number cannot be null or blank.");
        }
        if (vehicleType == null || vehicleType.isBlank()) {
            throw new IllegalArgumentException("Vehicle type cannot be null or blank.");
        }
        this.registrationNumber = registrationNumber.trim().toUpperCase();
        this.vehicleType = vehicleType.trim().toUpperCase();
    }

    /**
     * Final accessor returning the vehicle registration number.
     * Declared final to prevent subclasses from altering the core identity retrieval logic.
     *
     * @return normalized uppercase registration number
     */
    public final String getRegistrationNumber() {
        return registrationNumber;
    }

    /**
     * Final accessor returning the generic vehicle category.
     * Declared final to protect category invariant integrity.
     *
     * @return vehicle type string
     */
    public final String getVehicleType() {
        return vehicleType;
    }

    /**
     * Common method to display generic vehicle information.
     *
     * @return formatted string containing core vehicle details
     */
    public String getVehicleInfo() {
        return "Registration No: " + registrationNumber + ", Type: " + vehicleType;
    }

    /**
     * Calculates the parking fee for a stay of the specified duration.
     * Each concrete vehicle subclass must override this method to implement
     * its specialized category-specific pricing policy.
     *
     * @param durationMinutes parking stay duration in minutes (must be non-negative)
     * @return calculated parking fee in rupees
     */
    public abstract double calculateFee(long durationMinutes);

    /**
     * Helper method to compute billable hours rounded up to the next full started hour.
     * A stay of 0 minutes costs 0 hours. Any fraction of an hour is charged in full.
     *
     * @param durationMinutes parking duration in minutes
     * @return billable whole hours
     */
    protected static long computeStartedHours(long durationMinutes) {
        if (durationMinutes < 0) {
            throw new IllegalArgumentException("Parking duration cannot be negative.");
        }
        if (durationMinutes == 0) {
            return 0;
        }
        return (durationMinutes + 59) / 60;
    }

    /**
     * Evaluates logical equality between two Vehicle objects based on registration number.
     * Two vehicles with the identical registration number represent the same physical vehicle
     * license plate, regardless of whether they are referenced via Vehicle, Car, or ElectricCar.
     *
     * @param obj target object for comparison
     * @return true if obj is a Vehicle with the identical normalized registration number
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Vehicle other)) {
            return false;
        }
        return this.registrationNumber.equals(other.registrationNumber);
    }

    /**
     * Returns a hash code consistent with equals().
     * Conforms strictly to the equals/hashCode contract by deriving hash code
     * exclusively from the unique registration number.
     *
     * @return computed hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(registrationNumber);
    }

    /**
     * Returns a human-readable representation of the Vehicle.
     * Subclasses can override or augment this to present complete vehicle information.
     *
     * @return formatted string description
     */
    @Override
    public String toString() {
        return "Vehicle[regNo=" + registrationNumber + ", type=" + vehicleType + "]";
    }
}
