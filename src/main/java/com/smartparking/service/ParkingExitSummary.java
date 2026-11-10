package com.smartparking.service;

import com.smartparking.model.ParkingTicket;
import com.smartparking.model.Vehicle;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Immutable exit summary record representing a completed parking transaction.
 *
 * Encapsulates:
 * - The closed {@link ParkingTicket}
 * - The departing {@link Vehicle}
 * - Duration of the stay in minutes
 * - Itemized billing breakdown (parking fee and separate charging fee)
 * - Total fee collected
 * - The released parking slot coordinates
 * - The exit timestamp
 */
public record ParkingExitSummary(
        ParkingTicket ticket,
        Vehicle vehicle,
        long durationMinutes,
        double parkingFee,
        double chargingFee,
        double totalFee,
        int releasedFloor,
        int releasedSlot,
        LocalDateTime exitTime
) {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /**
     * Compact constructor validating all summary invariants.
     */
    public ParkingExitSummary {
        if (ticket == null) {
            throw new IllegalArgumentException("Parking ticket cannot be null.");
        }
        if (vehicle == null) {
            throw new IllegalArgumentException("Vehicle cannot be null.");
        }
        if (durationMinutes < 0) {
            throw new IllegalArgumentException("Duration cannot be negative: " + durationMinutes);
        }
        if (parkingFee < 0.0) {
            throw new IllegalArgumentException("Parking fee cannot be negative: " + parkingFee);
        }
        if (chargingFee < 0.0) {
            throw new IllegalArgumentException("Charging fee cannot be negative: " + chargingFee);
        }
        if (totalFee < 0.0) {
            throw new IllegalArgumentException("Total fee cannot be negative: " + totalFee);
        }
        if (releasedFloor < 0) {
            throw new IllegalArgumentException("Released floor cannot be negative: " + releasedFloor);
        }
        if (releasedSlot < 0) {
            throw new IllegalArgumentException("Released slot cannot be negative: " + releasedSlot);
        }
        if (exitTime == null) {
            throw new IllegalArgumentException("Exit timestamp cannot be null.");
        }
    }

    /**
     * Formats an itemized receipt suitable for display and auditing.
     *
     * @return formatted receipt string
     */
    public String formatReceipt() {
        return String.format(
                "========================================\n" +
                "       PARKING EXIT RECEIPT             \n" +
                "========================================\n" +
                "Ticket ID      : %s\n" +
                "Registration   : %s\n" +
                "Vehicle Category: %s\n" +
                "Slot Released  : Floor %d, Slot %d\n" +
                "Entry Time     : %s\n" +
                "Exit Time      : %s\n" +
                "Duration       : %d minutes\n" +
                "----------------------------------------\n" +
                "Parking Fee    : Rs %.2f\n" +
                "Charging Fee   : Rs %.2f\n" +
                "TOTAL PAID     : Rs %.2f\n" +
                "========================================",
                ticket.getTicketNumber(),
                vehicle.getRegistrationNumber(),
                vehicle.getVehicleType(),
                releasedFloor,
                releasedSlot,
                ticket.getEntryTime().format(TIME_FORMATTER),
                exitTime.format(TIME_FORMATTER),
                durationMinutes,
                parkingFee,
                chargingFee,
                totalFee
        );
    }
}
