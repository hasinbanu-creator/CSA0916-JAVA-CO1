package com.smartparking.model;

/**
 * Represents an electric automobile (EV) requiring standard parking or dedicated charging slots.
 *
 * Demonstrates:
 * - Direct inheritance from the abstract Vehicle superclass (Hierarchical inheritance)
 * - Constructor chaining to the superclass via super(...)
 * - Addition of specialized battery and charging state
 * - Clean encapsulation of charging behavior without prematurely implementing Task 7 fee calculations
 */
public class ElectricCar extends Vehicle {
    private final double batteryCapacityKWh;
    private double currentBatteryLevelKWh;
    private boolean isCharging;

    /**
     * Parameterized constructor for ElectricCar.
     * Invokes the superclass constructor with the category "ELECTRIC_CAR".
     *
     * @param registrationNumber unique license plate number
     * @param batteryCapacityKWh total battery capacity in kilowatt-hours (must be positive)
     */
    public ElectricCar(String registrationNumber, double batteryCapacityKWh) {
        super(registrationNumber, "ELECTRIC_CAR");
        if (batteryCapacityKWh <= 0) {
            throw new IllegalArgumentException("Battery capacity must be strictly positive.");
        }
        this.batteryCapacityKWh = batteryCapacityKWh;
        this.currentBatteryLevelKWh = batteryCapacityKWh * 0.5; // Default initial level to 50%
        this.isCharging = false;
    }

    /**
     * Overloaded constructor accepting an initial battery level.
     *
     * @param registrationNumber     unique license plate number
     * @param batteryCapacityKWh     total battery capacity in kWh
     * @param initialBatteryLevelKWh initial charge level in kWh
     */
    public ElectricCar(String registrationNumber, double batteryCapacityKWh, double initialBatteryLevelKWh) {
        super(registrationNumber, "ELECTRIC_CAR");
        if (batteryCapacityKWh <= 0) {
            throw new IllegalArgumentException("Battery capacity must be strictly positive.");
        }
        if (initialBatteryLevelKWh < 0 || initialBatteryLevelKWh > batteryCapacityKWh) {
            throw new IllegalArgumentException("Initial battery level must be between 0 and total capacity.");
        }
        this.batteryCapacityKWh = batteryCapacityKWh;
        this.currentBatteryLevelKWh = initialBatteryLevelKWh;
        this.isCharging = false;
    }

    public double getBatteryCapacityKWh() {
        return batteryCapacityKWh;
    }

    public double getCurrentBatteryLevelKWh() {
        return currentBatteryLevelKWh;
    }

    public boolean isCharging() {
        return isCharging;
    }

    /**
     * Connects the vehicle to an active charging point.
     */
    public void startCharging() {
        this.isCharging = true;
    }

    /**
     * Disconnects the vehicle from the charging point.
     */
    public void stopCharging() {
        this.isCharging = false;
    }

    /**
     * Simulates charging the battery by a specified amount of energy in kWh.
     *
     * @param energyKWh energy delivered to the battery
     */
    public void charge(double energyKWh) {
        if (energyKWh <= 0) {
            throw new IllegalArgumentException("Delivered charging energy must be strictly positive.");
        }
        this.currentBatteryLevelKWh = Math.min(this.batteryCapacityKWh, this.currentBatteryLevelKWh + energyKWh);
    }

    /**
     * Specialized display method demonstrating invocation of inherited superclass behaviour.
     *
     * @return complete electric car details
     */
    public String getDetailedInfo() {
        return super.getVehicleInfo() +
                ", Battery: " + currentBatteryLevelKWh + "/" + batteryCapacityKWh + " kWh" +
                ", Charging: " + (isCharging ? "Yes" : "No");
    }
}

