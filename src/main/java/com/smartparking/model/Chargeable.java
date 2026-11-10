package com.smartparking.model;

/**
 * Interface defining the capability of an electric vehicle or equipment to be charged.
 *
 * Demonstrates Task 9 Abstraction concepts:
 * 1. Interface definition representing a capability / contract ("can-do") rather than an identity ("is-a").
 * 2. Abstract methods that any chargeable entity must implement (charge, capacity, current charge).
 * 3. Default methods providing reusable concrete behaviour derived from abstract contracts
 *    (remaining capacity, battery percentage, estimated charging time under stated assumptions).
 * 4. Static utility methods for input validation and electricity cost calculation.
 *
 * Energy Delivery and Billing Policy:
 * - Requested Energy (E_requested): The volume of energy requested by a charging session.
 * - Accepted Energy (E_accepted): The actual energy transferred into the battery pack, bounded
 *   strictly by remaining capacity: E_accepted = min(E_requested, remainingCapacity).
 * - Billing Policy: Electricity fees are assessed STRICTLY on accepted energy (E_accepted),
 *   never on unabsorbed surplus requested energy: Cost = E_accepted * ratePerKWh.
 */
public interface Chargeable {

    /** Standard baseline electricity rate for EV charging in INR per kWh (as specified in assignment Slide 14). */
    double STANDARD_CHARGING_RATE_PER_KWH = 12.0;

    /**
     * Charges the battery by up to the specified amount of electrical energy in kilowatt-hours (kWh).
     *
     * Invariants & Billing Contract:
     * - Energy must be strictly positive and finite.
     * - If energyKWh exceeds remaining battery space, charging is clamped at total capacity,
     *   and only the remaining space is accepted into the battery.
     * - Returns the actual energy accepted into the battery in kWh.
     * - Billing systems must calculate customer charges using the returned accepted energy,
     *   ensuring drivers are never billed for energy the battery could not absorb.
     *
     * @param energyKWh requested energy in kWh (must be strictly positive and finite)
     * @return actual energy accepted and stored into the battery in kWh
     * @throws IllegalArgumentException if energyKWh is negative, zero, NaN, or infinite
     */
    double charge(double energyKWh);

    /**
     * Returns the total battery capacity in kilowatt-hours (kWh).
     *
     * @return total capacity in kWh (strictly positive)
     */
    double getBatteryCapacityKWh();

    /**
     * Returns the current electrical energy stored in the battery in kilowatt-hours (kWh).
     *
     * @return current charge in kWh (between 0.0 and getBatteryCapacityKWh())
     */
    double getCurrentChargeKWh();

    // =========================================================================
    // Default Interface Methods (Concrete Reusable Behaviour)
    // =========================================================================

    /**
     * Default method calculating the remaining battery capacity needed to reach 100% full charge.
     *
     * @return remaining energy capacity in kWh (always non-negative)
     */
    default double getRemainingCapacityKWh() {
        return Math.max(0.0, getBatteryCapacityKWh() - getCurrentChargeKWh());
    }

    /**
     * Default method calculating the current state of charge (SoC) as a percentage (0.0% to 100.0%).
     *
     * @return battery percentage (0.0 to 100.0)
     */
    default double getBatteryPercentage() {
        double capacity = getBatteryCapacityKWh();
        if (capacity <= 0.0) {
            return 0.0;
        }
        return (getCurrentChargeKWh() / capacity) * 100.0;
    }

    /**
     * Default method estimating the charging duration in minutes required to reach 100% full charge,
     * accounting for charging station power output and charging efficiency.
     *
     * Engineering Assumptions:
     * 1. Constant Charging Power: Assumes linear power delivery from the charger without thermal
     *    throttling or battery management system (BMS) tapering above 80% State of Charge (SoC).
     * 2. Charging Efficiency: Accounts for conversion losses (e.g. AC-to-DC rectification and thermal
     *    dissipation), where 0.0 < efficiency <= 1.0 (typically ~0.85-0.92 for real-world EV chargers).
     *
     * Formula:
     * {@code durationMinutes = (remainingCapacityKWh / (chargingPowerKW * efficiency)) * 60.0}
     *
     * @param chargingPowerKW station output rate in kilowatts (must be strictly positive and finite)
     * @param efficiency      charging efficiency factor (must be strictly in range (0.0, 1.0])
     * @return estimated duration in minutes
     * @throws IllegalArgumentException if power or efficiency values are invalid or out of range
     */
    default double estimateChargingTimeMinutes(double chargingPowerKW, double efficiency) {
        validatePower(chargingPowerKW);
        if (Double.isNaN(efficiency) || Double.isInfinite(efficiency) || efficiency <= 0.0 || efficiency > 1.0) {
            throw new IllegalArgumentException("Charging efficiency must be strictly between 0.0 (exclusive) and 1.0 (inclusive): " + efficiency);
        }
        double remainingKWh = getRemainingCapacityKWh();
        return (remainingKWh / (chargingPowerKW * efficiency)) * 60.0;
    }

    /**
     * Default method estimating the charging duration in minutes required to reach 100% full charge
     * under the assumption of ideal charging efficiency (efficiency = 1.0, 100%).
     *
     * Engineering Assumptions:
     * 1. Constant Charging Power: Assumes linear power delivery without BMS tapering.
     * 2. Ideal Efficiency (100%): Assumes zero thermal dissipation or power conversion losses.
     *
     * @param chargingPowerKW station output rate in kilowatts (must be strictly positive and finite)
     * @return estimated duration in minutes under ideal conditions
     * @throws IllegalArgumentException if chargingPowerKW is negative, zero, NaN, or infinite
     */
    default double estimateChargingTimeMinutes(double chargingPowerKW) {
        return estimateChargingTimeMinutes(chargingPowerKW, 1.0);
    }

    // =========================================================================
    // Static Utility Methods (Interface Helper Logic)
    // =========================================================================

    /**
     * Static utility method calculating the charging cost for a specified energy quantity at a given unit rate.
     *
     * Billing Policy: When computing charging bills, the caller must pass the ACTUAL energy accepted
     * by the battery, ensuring drivers are only billed for energy delivered.
     *
     * @param energyKWh   actual energy consumed/accepted in kilowatt-hours (must be strictly positive and finite)
     * @param ratePerKWh  electricity rate in INR per kWh (must be non-negative and finite)
     * @return total charging fee in INR
     * @throws IllegalArgumentException if parameters are invalid, negative, NaN, or infinite
     */
    static double calculateChargingCost(double energyKWh, double ratePerKWh) {
        validateEnergy(energyKWh);
        if (Double.isNaN(ratePerKWh) || Double.isInfinite(ratePerKWh) || ratePerKWh < 0.0) {
            throw new IllegalArgumentException("Charging rate cannot be negative or invalid: " + ratePerKWh);
        }
        return energyKWh * ratePerKWh;
    }

    /**
     * Static helper method validating that a charging energy value is strictly positive and finite.
     *
     * @param energyKWh energy value in kWh
     * @throws IllegalArgumentException if energyKWh is NaN, infinite, negative, or zero
     */
    static void validateEnergy(double energyKWh) {
        if (Double.isNaN(energyKWh) || Double.isInfinite(energyKWh) || energyKWh <= 0.0) {
            throw new IllegalArgumentException("Charging energy must be finite and strictly positive: " + energyKWh);
        }
    }

    /**
     * Static helper method validating that a charging power output value is strictly positive and finite.
     *
     * @param powerKW charging power in kW
     * @throws IllegalArgumentException if powerKW is NaN, infinite, negative, or zero
     */
    static void validatePower(double powerKW) {
        if (Double.isNaN(powerKW) || Double.isInfinite(powerKW) || powerKW <= 0.0) {
            throw new IllegalArgumentException("Charging power must be finite and strictly positive: " + powerKW);
        }
    }
}
