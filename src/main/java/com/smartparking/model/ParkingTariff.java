package com.smartparking.model;

/**
 * Final utility class defining campus parking tariff constants and duration calculations.
 *
 * Demonstrates:
 * 1. A final utility class that cannot be extended (subclassed)
 * 2. A private constructor to strictly prevent instantiation
 * 3. Public static final constants for baseline hourly parking rates
 * 4. A reusable static duration calculation method based on started hours (ceiling rounding)
 *
 * Tariff Assumptions:
 * - Two-Wheeler: Rs 10.0 per started hour
 * - Car: Rs 30.0 per started hour
 * - Electric Car: Rs 30.0 per started hour (base parking tariff)
 * - Billing rule: Every started hour is charged in full; 0 minutes stay is charged Rs 0.0.
 */
public final class ParkingTariff {

    /** Baseline hourly tariff for two-wheelers in INR. */
    public static final double TWO_WHEELER_RATE_PER_HOUR = 10.0;

    /** Baseline hourly tariff for passenger cars in INR. */
    public static final double CAR_RATE_PER_HOUR = 30.0;

    /** Baseline hourly tariff for electric cars in INR. */
    public static final double ELECTRIC_CAR_RATE_PER_HOUR = 30.0;

    /** Configurable baseline flat parking fee for electric bikes in INR (Task 12). */
    public static final double FLAT_EBIKE_FEE = 15.0;

    /** Delivery van peak surcharge multiplier (Task 12). */
    public static final double DELIVERY_VAN_PEAK_MULTIPLIER = 2.0;

    /** Delivery van peak window start time (09:00 AM inclusive). */
    public static final java.time.LocalTime DELIVERY_VAN_PEAK_START = java.time.LocalTime.of(9, 0);

    /** Delivery van peak window end time (05:00 PM / 17:00 boundary). */
    public static final java.time.LocalTime DELIVERY_VAN_PEAK_END = java.time.LocalTime.of(17, 0);

    /** Hourly tariff for delivery vans during peak delivery hours (9 AM - 5 PM) in INR (Rs 60.0). */
    public static final double DELIVERY_VAN_PEAK_RATE_PER_HOUR = CAR_RATE_PER_HOUR * DELIVERY_VAN_PEAK_MULTIPLIER;

    /** Hourly tariff for delivery vans outside peak hours in INR (standard car rate = Rs 30.0). */
    public static final double DELIVERY_VAN_OFF_PEAK_RATE_PER_HOUR = CAR_RATE_PER_HOUR;

    /** Minutes per whole hour constant. */
    public static final int MINUTES_PER_HOUR = 60;

    /**
     * Private constructor to prevent instantiation of this utility class.
     * Enforces the utility class design pattern.
     */
    private ParkingTariff() {
        throw new UnsupportedOperationException("ParkingTariff is a utility class and cannot be instantiated.");
    }

    /**
     * Computes billable hours rounded up to the nearest full started hour (ceiling arithmetic).
     *
     * Rules:
     * - A negative duration is invalid and throws IllegalArgumentException.
     * - A stay of exactly 0 minutes costs 0 billable hours (Rs 0.0).
     * - Any duration between 1 and 60 minutes is charged for 1 full hour.
     * - Stays beyond 60 minutes are charged for each commenced 60-minute window.
     *
     * @param durationMinutes parking stay duration in whole minutes
     * @return billable whole hours
     */
    public static long calculateStartedHours(long durationMinutes) {
        if (durationMinutes < 0) {
            throw new IllegalArgumentException("Parking duration cannot be negative: " + durationMinutes);
        }
        if (durationMinutes == 0) {
            return 0;
        }
        // Overflow-safe ceiling arithmetic: avoids arithmetic overflow when durationMinutes is large (e.g. Long.MAX_VALUE)
        return 1 + (durationMinutes - 1) / MINUTES_PER_HOUR;
    }
}

