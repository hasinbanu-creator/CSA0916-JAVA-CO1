package com.smartparking.model;

/**
 * Represents an electric automobile (EV) requiring standard parking or dedicated charging slots.
 *
 * Demonstrates Task 7 concepts:
 * - Overriding calculateFee(long) to implement base EV parking tariff
 * - Use of the @Override annotation for compiler verification
 * - Overriding toString() to display battery capacity, charge level, and charging status
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
     * Calculates the parking fee for an electric car stay.
     * Base parking rate is Rs 30.0 per started hour.
     * Note: Energy charging fee is separate and handled via dedicated charging capabilities.
     *
     * @param durationMinutes parking stay duration in minutes (must be non-negative)
     * @return calculated parking fee in INR
     */
    @Override
    public double calculateFee(long durationMinutes) {
        long billableHours = ParkingTariff.calculateStartedHours(durationMinutes);
        return billableHours * ParkingTariff.ELECTRIC_CAR_RATE_PER_HOUR;
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

    @Override
    public String toString() {
        return "ElectricCar[" + super.toString() +
                ", battery=" + currentBatteryLevelKWh + "/" + batteryCapacityKWh + "kWh" +
                ", charging=" + isCharging + "]";
    }
}
