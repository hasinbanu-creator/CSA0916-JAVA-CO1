package com.smartparking.model;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Represents a parking ticket issued to a vehicle upon entry.
 * Encapsulates ticket identification, vehicle details, allocated location,
 * timestamps, and active parking session state.
 *
 * Demonstrates:
 * - Parameterized constructors with input validation
 * - Constructor chaining using this(...)
 * - Use of the this keyword to disambiguate fields from parameters
 * - Copy constructors distinguishing archival snapshots from reissued tickets
 */
public class ParkingTicket {
    private final String ticketNumber;
    private final String vehicleRegNo;
    private final String vehicleType;
    private final int floorNumber;
    private final int slotNumber;
    private final LocalDateTime entryTime;
    private LocalDateTime exitTime;
    private boolean isActive;
    private final ParkingPass pass;

    /**
     * Canonical Parameterized Constructor with ParkingPass.
     * Enforces complete initialization and validation of all mandatory fields.
     *
     * @param ticketNumber unique ticket identifier (non-null, non-blank)
     * @param vehicleRegNo vehicle registration number (non-null, non-blank)
     * @param vehicleType  category of vehicle (non-null, non-blank)
     * @param floorNumber  allocated floor index (non-negative)
     * @param slotNumber   allocated slot index (non-negative)
     * @param entryTime    time of entry (non-null)
     * @param pass         optional access pass associated with this parking session (nullable)
     */
    public ParkingTicket(String ticketNumber, String vehicleRegNo, String vehicleType,
                         int floorNumber, int slotNumber, LocalDateTime entryTime, ParkingPass pass) {
        if (ticketNumber == null || ticketNumber.isBlank()) {
            throw new IllegalArgumentException("Ticket number cannot be null or blank.");
        }
        if (vehicleRegNo == null || vehicleRegNo.isBlank()) {
            throw new IllegalArgumentException("Vehicle registration number cannot be null or blank.");
        }
        if (vehicleType == null || vehicleType.isBlank()) {
            throw new IllegalArgumentException("Vehicle type cannot be null or blank.");
        }
        if (floorNumber < 0) {
            throw new IllegalArgumentException("Floor number cannot be negative.");
        }
        if (slotNumber < 0) {
            throw new IllegalArgumentException("Slot number cannot be negative.");
        }
        if (entryTime == null) {
            throw new IllegalArgumentException("Entry time cannot be null.");
        }

        this.ticketNumber = ticketNumber.trim();
        this.vehicleRegNo = vehicleRegNo.trim().toUpperCase();
        this.vehicleType = vehicleType.trim().toUpperCase();
        this.floorNumber = floorNumber;
        this.slotNumber = slotNumber;
        this.entryTime = entryTime;
        this.exitTime = null;
        this.isActive = true;
        this.pass = pass;
    }

    /**
     * Parameterized Constructor without pass. Chains to {@link #ParkingTicket(String, String, String, int, int, LocalDateTime, ParkingPass)}.
     */
    public ParkingTicket(String ticketNumber, String vehicleRegNo, String vehicleType,
                         int floorNumber, int slotNumber, LocalDateTime entryTime) {
        this(ticketNumber, vehicleRegNo, vehicleType, floorNumber, slotNumber, entryTime, null);
    }

    /**
     * Overloaded convenience constructor with entry time defaulted to the current timestamp.
     * Demonstrates constructor chaining using this(...) to avoid duplicated validation logic.
     *
     * @param ticketNumber unique ticket identifier
     * @param vehicleRegNo vehicle registration number
     * @param vehicleType  category of vehicle
     * @param floorNumber  allocated floor index
     * @param slotNumber   allocated slot index
     */
    public ParkingTicket(String ticketNumber, String vehicleRegNo, String vehicleType,
                         int floorNumber, int slotNumber) {
        this(ticketNumber, vehicleRegNo, vehicleType, floorNumber, slotNumber, LocalDateTime.now());
    }

    /**
     * Overloaded convenience constructor defaulting vehicle category to "CAR" and entry time to now.
     * Demonstrates multi-level constructor chaining using this(...).
     *
     * @param ticketNumber unique ticket identifier
     * @param vehicleRegNo vehicle registration number
     * @param floorNumber  allocated floor index
     * @param slotNumber   allocated slot index
     */
    public ParkingTicket(String ticketNumber, String vehicleRegNo, int floorNumber, int slotNumber) {
        this(ticketNumber, vehicleRegNo, "CAR", floorNumber, slotNumber, LocalDateTime.now());
    }

    /**
     * Archival Copy Constructor.
     * Creates an exact historical snapshot of an existing ticket for audit and logging.
     * Note: This preserves the original ticketNumber and state for archival purposes.
     * It must NOT be used to admit a second concurrent active vehicle session.
     *
     * @param source the ticket to copy (must not be null)
     */
    public ParkingTicket(ParkingTicket source) {
        if (source == null) {
            throw new IllegalArgumentException("Source ticket cannot be null.");
        }
        this.ticketNumber = source.ticketNumber;
        this.vehicleRegNo = source.vehicleRegNo;
        this.vehicleType = source.vehicleType;
        this.floorNumber = source.floorNumber;
        this.slotNumber = source.slotNumber;
        this.entryTime = source.entryTime;
        this.exitTime = source.exitTime;
        this.isActive = source.isActive;
        this.pass = source.pass;
    }

    /**
     * Reissue Copy Constructor.
     * Reissues a duplicate ticket with a NEW unique ticket number while preserving
     * the underlying parking session details (session coordinates and entry time).
     * This avoids violating Rule 2 (unique, non-reused ticket identifiers).
     *
     * @param newTicketNumber newly issued unique ticket identifier
     * @param source          original ticket whose session data is being reissued
     */
    public ParkingTicket(String newTicketNumber, ParkingTicket source) {
        this(newTicketNumber,
             source != null ? source.vehicleRegNo : null,
             source != null ? source.vehicleType : null,
             source != null ? source.floorNumber : -1,
             source != null ? source.slotNumber : -1,
             source != null ? source.entryTime : null,
             source != null ? source.pass : null);
        if (source == null) {
            throw new IllegalArgumentException("Source ticket cannot be null.");
        }
        this.exitTime = source.exitTime;
        this.isActive = source.isActive;
    }

    /**
     * Marks the ticket as completed upon vehicle departure.
     *
     * @param exitTime timestamp of departure (must be on or after entryTime)
     */
    public void closeTicket(LocalDateTime exitTime) {
        if (exitTime == null) {
            throw new IllegalArgumentException("Exit time cannot be null.");
        }
        if (exitTime.isBefore(this.entryTime)) {
            throw new IllegalArgumentException("Exit time cannot be prior to entry time.");
        }
        this.exitTime = exitTime;
        this.isActive = false;
    }

    /**
     * Computes elapsed parking duration in minutes.
     * If ticket is closed, uses exitTime; if active, computes up to the current time.
     *
     * @return duration in whole minutes
     */
    public long getDurationMinutes() {
        LocalDateTime effectiveEnd = (this.exitTime != null) ? this.exitTime : LocalDateTime.now();
        return Duration.between(this.entryTime, effectiveEnd).toMinutes();
    }

    public String getTicketNumber() {
        return ticketNumber;
    }

    public String getVehicleRegNo() {
        return vehicleRegNo;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public int getFloorNumber() {
        return floorNumber;
    }

    public int getSlotNumber() {
        return slotNumber;
    }

    public LocalDateTime getEntryTime() {
        return entryTime;
    }

    public LocalDateTime getExitTime() {
        return exitTime;
    }

    public boolean isActive() {
        return isActive;
    }

    public ParkingPass getPass() {
        return pass;
    }

    public boolean hasPass() {
        return pass != null;
    }

    @Override
    public String toString() {
        return "ParkingTicket{" +
                "ticket='" + ticketNumber + '\'' +
                ", regNo='" + vehicleRegNo + '\'' +
                ", type='" + vehicleType + '\'' +
                ", slot=[" + floorNumber + ", " + slotNumber + ']' +
                ", entry=" + entryTime +
                ", exit=" + exitTime +
                ", active=" + isActive +
                '}';
    }
}

