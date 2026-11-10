package com.smartparking.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Represents a commercial delivery van in the Campus Smart-Parking System.
 *
 * Implements Task 12 Day-4 Design Challenge Requirement B:
 * - Specializes {@link Vehicle} with time-of-day dynamic surge pricing.
 * - During peak delivery window (9:00 AM to 5:00 PM / 17:00), charges twice the standard car rate
 *   ({@value ParkingTariff#DELIVERY_VAN_PEAK_RATE_PER_HOUR} INR/hr).
 * - Outside the peak delivery window (before 9:00 AM or after 5:00 PM), charges the standard car rate
 *   ({@value ParkingTariff#DELIVERY_VAN_OFF_PEAK_RATE_PER_HOUR} INR/hr) as an explicit documented assumption.
 * - Stays spanning peak and off-peak windows aggregate all minutes falling in each tariff category across
 *   the entire stay prior to applying started-hour ceiling rounding.
 *   Implementation Assumption: The assignment does not specify whether started-hour rounding
 *   should occur per contiguous time interval or per tariff category across the stay. Under our
 *   category-aggregation policy, disconnected off-peak periods (such as 08:30-09:00 and 17:00-17:30)
 *   are summed (30 + 30 = 60 min) and rounded once to 1 started hour (Rs 30), preventing double-rounding
 *   penalties for split off-peak stays.
 * - Handles boundary conditions accurately:
 *   - Entry before 9:00 AM (entirely off-peak)
 *   - Entry exactly at 9:00 AM (peak billing begins)
 *   - Entry during 9:00 AM - 5:00 PM (peak billing)
 *   - Entry exactly at 5:00 PM / 17:00 (off-peak billing resumes)
 *   - Entry after 5:00 PM (entirely off-peak)
 *   - Stays spanning window boundaries (segmented started-hour billing for peak and off-peak minutes)
 *   - Instantaneous stays (0 minutes) incur Rs 0.0
 */
public class DeliveryVan extends Vehicle {

    public static final String VEHICLE_TYPE = "DELIVERY_VAN";

    private final String deliveryCompany;

    /**
     * Parameterized constructor with delivery company affiliation.
     *
     * @param registrationNumber unique license plate (non-null, non-blank)
     * @param deliveryCompany    name of the commercial logistics provider (nullable, defaults to "INDEPENDENT")
     */
    public DeliveryVan(String registrationNumber, String deliveryCompany) {
        super(registrationNumber, VEHICLE_TYPE);
        this.deliveryCompany = (deliveryCompany != null && !deliveryCompany.isBlank())
                ? deliveryCompany.trim()
                : "INDEPENDENT";
    }

    /**
     * Convenience constructor defaulting delivery company affiliation to "INDEPENDENT".
     *
     * @param registrationNumber unique license plate
     */
    public DeliveryVan(String registrationNumber) {
        this(registrationNumber, "INDEPENDENT");
    }

    public String getDeliveryCompany() {
        return deliveryCompany;
    }

    /**
     * Duration-only calculation fulfilling abstract {@link Vehicle#calculateFee(long)} contract.
     * Since time-of-day is not supplied in this signature, applies off-peak rate as a documented default.
     * Callers should prefer {@link #calculateFee(LocalDateTime, LocalDateTime)} for time-aware pricing.
     *
     * @param durationMinutes parking stay duration in minutes (must be non-negative)
     * @return calculated parking fee in rupees
     */
    @Override
    public double calculateFee(long durationMinutes) {
        if (durationMinutes < 0) {
            throw new IllegalArgumentException("Parking duration cannot be negative: " + durationMinutes);
        }
        if (durationMinutes == 0) {
            return 0.0;
        }
        return computeStartedHours(durationMinutes) * ParkingTariff.DELIVERY_VAN_OFF_PEAK_RATE_PER_HOUR;
    }

    /**
     * Time-window-aware fee calculation evaluating peak (9 AM - 5 PM) and off-peak hours.
     *
     * Tariff Aggregation & Rounding Policy:
     * - Minutes are categorized minute-by-minute into peak minutes and off-peak minutes.
     * - Disconnected intervals with the same tariff category (e.g., morning off-peak before 09:00
     *   and evening off-peak after 17:00) are aggregated across the entire stay before started-hour rounding.
     * - Implementation Assumption: The assignment prompt does not specify whether started-hour
     *   rounding should occur per contiguous interval or per tariff category. Aggregating across the
     *   entire stay ensures fair, consistent billing (e.g., 08:30 to 17:30 yields 480 min peak = Rs 480,
     *   and 30 + 30 = 60 min off-peak = 1 started hour = Rs 30, totaling Rs 510).
     *
     * @param entryTime timestamp of entry (non-null)
     * @param exitTime  timestamp of exit (non-null, must not be before entryTime)
     * @return calculated parking fee in rupees
     */
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

        long totalDurationMinutes = Duration.between(entryTime, exitTime).toMinutes();
        if (totalDurationMinutes == 0) {
            return 0.0;
        }

        long peakMinutes = calculatePeakMinutes(entryTime, exitTime);
        long offPeakMinutes = totalDurationMinutes - peakMinutes;

        double totalFee = 0.0;
        if (peakMinutes > 0) {
            long peakHours = computeStartedHours(peakMinutes);
            totalFee += peakHours * ParkingTariff.DELIVERY_VAN_PEAK_RATE_PER_HOUR;
        }
        if (offPeakMinutes > 0) {
            long offPeakHours = computeStartedHours(offPeakMinutes);
            totalFee += offPeakHours * ParkingTariff.DELIVERY_VAN_OFF_PEAK_RATE_PER_HOUR;
        }

        return totalFee;
    }

    /**
     * Calculates the total elapsed minutes falling within the 9:00 AM to 5:00 PM (17:00) peak window.
     *
     * Window Definition:
     * - A minute starting at time T is a peak minute if: T >= 09:00:00 AND T < 17:00:00.
     * - Minute 09:00 to 09:01 is peak.
     * - Minute 16:59 to 17:00 is peak.
     * - Minute 17:00 to 17:01 is off-peak.
     *
     * @param entryTime timestamp of entry
     * @param exitTime  timestamp of exit
     * @return total peak minutes
     */
    public static long calculatePeakMinutes(LocalDateTime entryTime, LocalDateTime exitTime) {
        if (entryTime == null || exitTime == null || exitTime.isBefore(entryTime)) {
            return 0;
        }

        long totalMinutes = Duration.between(entryTime, exitTime).toMinutes();
        if (totalMinutes == 0) {
            return 0;
        }

        long peakMinutes = 0;
        LocalTime peakStart = ParkingTariff.DELIVERY_VAN_PEAK_START; // 09:00
        LocalTime peakEnd = ParkingTariff.DELIVERY_VAN_PEAK_END;     // 17:00

        for (long m = 0; m < totalMinutes; m++) {
            LocalDateTime step = entryTime.plusMinutes(m);
            LocalTime time = step.toLocalTime();
            if (!time.isBefore(peakStart) && time.isBefore(peakEnd)) {
                peakMinutes++;
            }
        }
        return peakMinutes;
    }

    @Override
    public String getVehicleInfo() {
        return super.getVehicleInfo() + ", Delivery Company: " + deliveryCompany;
    }
}
