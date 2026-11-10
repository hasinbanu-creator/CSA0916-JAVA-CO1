package com.smartparking.model;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Represents an electric bicycle or lightweight e-bike in the Campus Smart-Parking System.
 *
 * Implements Task 12 Day-4 Design Challenge Requirement A:
 * - Specializes {@link Vehicle} with a flat fee tariff model rather than hourly billing.
 * - Flat fee is configurable via {@link ParkingTariff#FLAT_EBIKE_FEE} or constructor override.
 * - Preserves system zero-duration policy: instantaneous stays (0 minutes) incur Rs 0.0.
 * - Negative durations throw {@link IllegalArgumentException}.
 * - Distinct from electric cars: does not implement {@link Chargeable} or incur EV charging rates.
 */
public class ElectricBike extends Vehicle {

    public static final String VEHICLE_TYPE = "ELECTRIC_BIKE";

    private final double flatFee;

    /**
     * Parameterized constructor with custom flat fee.
     *
     * @param registrationNumber unique license plate (non-null, non-blank)
     * @param flatFee            flat parking fee in INR (must be non-negative)
     * @throws IllegalArgumentException if registrationNumber is invalid or flatFee is negative
     */
    public ElectricBike(String registrationNumber, double flatFee) {
        super(registrationNumber, VEHICLE_TYPE);
        if (!Double.isFinite(flatFee) || flatFee < 0.0) {
            throw new IllegalArgumentException("Flat fee must be a finite, non-negative number: " + flatFee);
        }
        this.flatFee = flatFee;
    }

    /**
     * Convenience constructor utilizing the standard configurable flat fee from {@link ParkingTariff#FLAT_EBIKE_FEE}.
     *
     * @param registrationNumber unique license plate
     */
    public ElectricBike(String registrationNumber) {
        this(registrationNumber, ParkingTariff.FLAT_EBIKE_FEE);
    }

    public double getFlatFee() {
        return flatFee;
    }

    @Override
    public double calculateFee(long durationMinutes) {
        if (durationMinutes < 0) {
            throw new IllegalArgumentException("Parking duration cannot be negative: " + durationMinutes);
        }
        if (durationMinutes == 0) {
            return 0.0;
        }
        return flatFee;
    }

    @Override
    public double calculateFee(LocalDateTime entryTime, LocalDateTime exitTime) {
        if (entryTime == null) {
            throw new IllegalArgumentException("Entry time cannot be null.");
        }
        if (exitTime == null) {
            throw new IllegalArgumentException("Exit time cannot be null.");
        }
        if (exitTime.isBefore(entryTime)) {
            throw new IllegalArgumentException("Exit time (" + exitTime + ") cannot be before entry time (" + entryTime + ").");
        }
        long durationMinutes = Duration.between(entryTime, exitTime).toMinutes();
        return calculateFee(durationMinutes);
    }

    @Override
    public String getVehicleInfo() {
        return super.getVehicleInfo() + ", Billing: Flat Fee (Rs " + String.format("%.2f", flatFee) + ")";
    }
}
