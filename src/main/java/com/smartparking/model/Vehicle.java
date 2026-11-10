package com.smartparking.model;

/**
 * Abstract superclass representing a generic vehicle in the Campus Smart-Parking System.
 *
 * Encapsulates the common state and behaviour shared by all vehicle categories:
 * - Registration number (unique license plate identifier)
 * - Vehicle category identifier (e.g., TWO_WHEELER, CAR, ELECTRIC_CAR)
 *
 * Serves as the base class for hierarchical inheritance in the vehicle domain.
 * Cannot be directly instantiated; concrete vehicle types must extend this class.
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
     * Returns the vehicle registration number.
     *
     * @return normalized uppercase registration number
     */
    public String getRegistrationNumber() {
        return registrationNumber;
    }

    /**
     * Returns the vehicle category.
     *
     * @return vehicle type string
     */
    public String getVehicleType() {
        return vehicleType;
    }

    /**
     * Common method to display generic vehicle information.
     * Demonstrates common behaviour accessible to all subclasses.
     *
     * @return formatted string containing core vehicle details
     */
    public String getVehicleInfo() {
        return "Registration No: " + registrationNumber + ", Type: " + vehicleType;
    }

    @Override
    public String toString() {
        return "Vehicle[regNo=" + registrationNumber + ", type=" + vehicleType + "]";
    }
}
