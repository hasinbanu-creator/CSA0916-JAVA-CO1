package com.smartparking.model;

/**
 * Represents an electric automobile (EV) in the Campus Smart-Parking System.
 *
 * Demonstrates Task 9 Abstraction concepts:
 * 1. Extending the abstract superclass {@link Vehicle} to inherit common vehicle identity and behaviour.
 * 2. Realizing (implementing) the {@link Chargeable} interface to acquire specialized EV charging capabilities.
 * 3. Preserving strict separation of concerns: base parking fees (slot occupancy) are governed by {@link Vehicle},
 *    while battery charging operations and energy costs are governed by {@link Chargeable}.
 */
public class ElectricCar extends Vehicle implements Chargeable {
    private final double batteryCapacityKWh;
    private double currentBatteryLevelKWh;
    private boolean isCharging;

    /**
     * Parameterized constructor for ElectricCar.
     * Invokes the superclass constructor with the category "ELECTRIC_CAR".
     * Defaults the initial battery level to 50% of total capacity.
     *
     * @param registrationNumber unique license plate number (non-null, non-blank)
     * @param batteryCapacityKWh total battery capacity in kilowatt-hours (must be strictly positive and finite)
     * @throws IllegalArgumentException if capacity is negative, zero, NaN, or infinite
     */
    public ElectricCar(String registrationNumber, double batteryCapacityKWh) {
        super(registrationNumber, "ELECTRIC_CAR");
        validateBatteryCapacity(batteryCapacityKWh);
        this.batteryCapacityKWh = batteryCapacityKWh;
        this.currentBatteryLevelKWh = batteryCapacityKWh * 0.5; // Default initial level to 50%
        this.isCharging = false;
    }

    /**
     * Overloaded constructor accepting an explicit initial battery level.
     *
     * @param registrationNumber     unique license plate number
     * @param batteryCapacityKWh     total battery capacity in kWh (must be strictly positive and finite)
     * @param initialBatteryLevelKWh initial charge level in kWh (between 0.0 and batteryCapacityKWh)
     * @throws IllegalArgumentException if parameters are invalid, NaN, infinite, or out of range
     */
    public ElectricCar(String registrationNumber, double batteryCapacityKWh, double initialBatteryLevelKWh) {
        super(registrationNumber, "ELECTRIC_CAR");
        validateBatteryCapacity(batteryCapacityKWh);
        if (Double.isNaN(initialBatteryLevelKWh) || Double.isInfinite(initialBatteryLevelKWh)
                || initialBatteryLevelKWh < 0.0 || initialBatteryLevelKWh > batteryCapacityKWh) {
            throw new IllegalArgumentException("Initial battery level must be finite and between 0 and total capacity: "
                    + initialBatteryLevelKWh);
        }
        this.batteryCapacityKWh = batteryCapacityKWh;
        this.currentBatteryLevelKWh = initialBatteryLevelKWh;
        this.isCharging = false;
    }

    private static void validateBatteryCapacity(double capacity) {
        if (Double.isNaN(capacity) || Double.isInfinite(capacity) || capacity <= 0.0) {
            throw new IllegalArgumentException("Battery capacity must be finite and strictly positive: " + capacity);
        }
    }

    // =========================================================================
    // Chargeable Interface Implementation
    // =========================================================================

    /**
     * {@inheritDoc}
     */
    @Override
    public double getBatteryCapacityKWh() {
        return batteryCapacityKWh;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getCurrentChargeKWh() {
        return currentBatteryLevelKWh;
    }

    /**
     * Backward-compatible accessor matching earlier Task 6–8 method signature.
     * Delegates directly to {@link #getCurrentChargeKWh()}.
     *
     * @return current battery level in kWh
     */
    public double getCurrentBatteryLevelKWh() {
        return getCurrentChargeKWh();
    }

    /**
     * {@inheritDoc}
     *
     * Invariants & Billing Policy:
     * - Energy must be strictly positive and finite.
     * - If energyKWh exceeds remaining capacity, charging is clamped at total capacity.
     * - Returns the actual energy accepted into the battery (min(energyKWh, remainingCapacity)).
     * - Billing is assessed strictly on this returned accepted energy.
     *
     * @param energyKWh requested energy in kWh (must be strictly positive and finite)
     * @return actual energy accepted and stored into the battery in kWh
     */
    @Override
    public double charge(double energyKWh) {
        Chargeable.validateEnergy(energyKWh);
        double remaining = getRemainingCapacityKWh();
        double accepted = Math.min(energyKWh, remaining);
        this.currentBatteryLevelKWh += accepted;
        return accepted;
    }

    // =========================================================================
    // Charging Session State Controls
    // =========================================================================

    /**
     * Returns whether the vehicle is currently connected to an active charging point.
     *
     * @return true if charging session is active, false otherwise
     */
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

    // =========================================================================
    // Polymorphic Fee Calculation (Task 7 & 8 Preservation)
    // =========================================================================

    /**
     * Calculates the parking fee for an electric car stay.
     * Base parking rate is Rs 30.0 per started hour (governed by {@link ParkingTariff}).
     *
     * Note: In accordance with Task 7 architectural principles, physical slot occupancy
     * is billed separately from any consumed charging electricity.
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
     * Specialized display method incorporating both Vehicle identity and Chargeable state.
     *
     * @return formatted string containing complete electric car details
     */
    public String getDetailedInfo() {
        return super.getVehicleInfo() +
                ", Battery: " + currentBatteryLevelKWh + "/" + batteryCapacityKWh + " kWh (" +
                String.format("%.1f", getBatteryPercentage()) + "%)" +
                ", Charging: " + (isCharging ? "Yes" : "No");
    }

    @Override
    public String toString() {
        return "ElectricCar[" + super.toString() +
                ", battery=" + currentBatteryLevelKWh + "/" + batteryCapacityKWh + "kWh" +
                ", charging=" + isCharging + "]";
    }
}
