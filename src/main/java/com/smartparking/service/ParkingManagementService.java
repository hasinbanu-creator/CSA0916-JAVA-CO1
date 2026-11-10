package com.smartparking.service;

import com.smartparking.model.Chargeable;
import com.smartparking.model.ParkingPass;
import com.smartparking.model.ParkingTicket;
import com.smartparking.model.Vehicle;
import com.smartparking.tasks.ParkingFacility;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Task 11 Core Service: Integrated Campus Smart-Parking Management Service.
 *
 * Coordinates vehicle identification, slot allocation across the physical facility,
 * ticket generation, active ticket storage (array-based as specified in PPT Slide 18),
 * departure processing, polymorphic fee calculation, separate EV charging billing,
 * slot de-allocation, and audited daily collection accounting.
 *
 * Architectural Invariants:
 * 1. Encapsulation: Internal active ticket array and revenue accumulators are private.
 * 2. Uniqueness: No vehicle may hold two concurrent active parking sessions.
 * 3. Array-based Active Storage with Compaction: Implements densely packed array storage
 *    for active tickets (activeTickets[]) and active vehicles (activeVehicles[]). Removal
 *    shifts elements left in lockstep, preserving strict 1-to-1 index association.
 * 4. Capacity Pre-Check & Transactional Integrity: Active storage capacity is strictly
 *    verified before allocating a physical slot. If any post-allocation failure occurs,
 *    the slot is rolled back immediately to prevent orphaned slots.
 * 5. Atomic Exit Processing & EV Charging Safety: Validations, fee calculations, and
 *    physical slot release verification execute BEFORE EV battery mutation, ticket closure,
 *    array compaction, or revenue updates. A failed exit never mutates the EV battery.
 * 6. Tariff Separation: Parking slot occupancy fees (Task 7) and EV charging energy fees (Task 9)
 *    are tracked separately without double-counting.
 */
public class ParkingManagementService {

    private static final String TICKET_PREFIX = "TKT-";

    /**
     * Recognized, supported vehicle categories in the Task 11 system.
     */
    private static final Set<String> SUPPORTED_VEHICLE_TYPES = Set.of(
            "TWO_WHEELER",
            "CAR",
            "ELECTRIC_CAR",
            "ELECTRIC_BIKE",
            "DELIVERY_VAN"
    );

    private final ParkingFacility facility;
    private final ParkingTicket[] activeTickets;
    private final Vehicle[] activeVehicles;
    private final int capacity;
    private int activeCount;
    private int ticketSequence;

    private double dailyParkingCollection;
    private double dailyChargingCollection;
    private final List<ParkingExitSummary> completedExits;

    /**
     * Constructs a parking service managing the specified facility with a custom active capacity.
     *
     * @param facility physical parking facility managing floor and slot matrices
     * @param capacity maximum number of active concurrent tickets supported
     */
    public ParkingManagementService(ParkingFacility facility, int capacity) {
        if (facility == null) {
            throw new IllegalArgumentException("Parking facility cannot be null.");
        }
        if (capacity <= 0) {
            throw new IllegalArgumentException("Active ticket capacity must be strictly positive: " + capacity);
        }
        this.facility = facility;
        this.capacity = capacity;
        this.activeTickets = new ParkingTicket[capacity];
        this.activeVehicles = new Vehicle[capacity];
        this.activeCount = 0;
        this.ticketSequence = 1000;
        this.dailyParkingCollection = 0.0;
        this.dailyChargingCollection = 0.0;
        this.completedExits = new ArrayList<>();
    }

    /**
     * Convenience constructor initializing the facility and capacity from floor slot dimensions.
     *
     * @param slotsPerFloor array specifying number of slots on each floor
     */
    public ParkingManagementService(int[] slotsPerFloor) {
        this(new ParkingFacility(slotsPerFloor), calculateTotalSlots(slotsPerFloor));
    }

    private static int calculateTotalSlots(int[] slotsPerFloor) {
        if (slotsPerFloor == null || slotsPerFloor.length == 0) {
            throw new IllegalArgumentException("Slots per floor array cannot be null or empty.");
        }
        int total = 0;
        for (int slots : slotsPerFloor) {
            if (slots < 0) {
                throw new IllegalArgumentException("Slot count cannot be negative: " + slots);
            }
            total += slots;
        }
        if (total == 0) {
            throw new IllegalArgumentException("Total parking slots must be greater than zero.");
        }
        return total;
    }

    /**
     * Checks whether the given vehicle category is recognized and supported by this service.
     *
     * @param type vehicle category string
     * @return true if supported, false otherwise
     */
    public static boolean isSupportedVehicleType(String type) {
        return type != null && SUPPORTED_VEHICLE_TYPES.contains(type.trim().toUpperCase());
    }

    // =========================================================================
    // Vehicle Entry Workflow
    // =========================================================================

    /**
     * Admits a vehicle into the facility, allocates an available slot, generates a unique ticket,
     * and stores it in the active ticket array.
     *
     * Invariants enforced:
     * - Checks for duplicate active registration.
     * - Pre-checks active ticket storage capacity BEFORE requesting a physical parking slot.
     * - Rolls back slot allocation if ticket instantiation or storage insertion fails.
     *
     * @param vehicle   the vehicle requesting entry (non-null)
     * @param entryTime timestamp of entry (non-null)
     * @return the newly issued, active {@link ParkingTicket}
     * @throws IllegalArgumentException if vehicle or entryTime is null
     * @throws IllegalStateException    if vehicle already has an active ticket, or facility is full
     */
    /**
     * Admits a vehicle into the facility with an optional access pass, allocates an available slot,
     * generates a unique ticket, and stores it in the active ticket array.
     *
     * Invariants enforced:
     * - Validates pass association against vehicle registration and entry timestamp.
     * - Checks for duplicate active registration.
     * - Pre-checks active ticket storage capacity BEFORE requesting a physical parking slot.
     * - Rolls back slot allocation if ticket instantiation or storage insertion fails.
     *
     * @param vehicle   the vehicle requesting entry (non-null)
     * @param entryTime timestamp of entry (non-null)
     * @param pass      optional access pass (e.g. MonthlyPass, VisitorOneDayPass), nullable
     * @return the newly issued, active {@link ParkingTicket}
     * @throws IllegalArgumentException if vehicle or entryTime is null, or pass is invalid/mismatched
     * @throws IllegalStateException    if vehicle already has an active ticket, or facility is full
     */
    public synchronized ParkingTicket parkVehicle(Vehicle vehicle, LocalDateTime entryTime, ParkingPass pass) {
        if (vehicle == null) {
            throw new IllegalArgumentException("Vehicle cannot be null.");
        }
        if (entryTime == null) {
            throw new IllegalArgumentException("Entry timestamp cannot be null.");
        }

        String vehicleType = vehicle.getVehicleType();
        if (vehicleType == null || vehicleType.isBlank()) {
            throw new IllegalArgumentException("Vehicle type cannot be null or blank.");
        }
        if (!isSupportedVehicleType(vehicleType)) {
            throw new IllegalArgumentException("Unknown or unsupported vehicle type: '" + vehicleType
                    + "'. Supported types: TWO_WHEELER, CAR, ELECTRIC_CAR, ELECTRIC_BIKE, DELIVERY_VAN.");
        }

        String regNo = vehicle.getRegistrationNumber();

        // Validate access pass if provided
        if (pass != null) {
            if (!pass.getVehicleRegNo().equalsIgnoreCase(regNo)) {
                throw new IllegalArgumentException("Pass registration (" + pass.getVehicleRegNo()
                        + ") does not match vehicle registration (" + regNo + ").");
            }
            if (!pass.isValidAt(entryTime)) {
                throw new IllegalArgumentException("Pass '" + pass.getPassId()
                        + "' is not valid at entry timestamp: " + entryTime);
            }
        }

        // 1. Check for duplicate active registration number
        for (int i = 0; i < activeCount; i++) {
            if (activeTickets[i].getVehicleRegNo().equalsIgnoreCase(regNo)) {
                throw new IllegalStateException("Duplicate entry rejected: Vehicle " + regNo
                        + " already has an active parking session (Ticket: " + activeTickets[i].getTicketNumber() + ").");
            }
        }

        // 2. Pre-check active ticket storage capacity BEFORE allocating a physical slot
        // Guarantees a failed entry never leaves an occupied slot without an active ticket
        if (activeCount >= capacity) {
            throw new IllegalStateException("Active ticket storage is full. Maximum concurrent capacity of "
                    + capacity + " reached.");
        }

        // 3. Allocate physical slot in ParkingFacility
        int[] coordinates = facility.allocate(regNo);
        if (coordinates == null) {
            throw new IllegalStateException("Parking facility is full. No available slots for vehicle: " + regNo);
        }
        int floor = coordinates[0];
        int slot = coordinates[1];

        // 4. Ticket instantiation and storage insertion wrapped with rollback safety
        ParkingTicket ticket;
        try {
            ticketSequence++;
            String ticketId = TICKET_PREFIX + ticketSequence;

            ticket = new ParkingTicket(
                    ticketId,
                    regNo,
                    vehicle.getVehicleType(),
                    floor,
                    slot,
                    entryTime,
                    pass
            );

            // Densely append to active arrays at index activeCount, maintaining 1-to-1 association
            activeTickets[activeCount] = ticket;
            activeVehicles[activeCount] = vehicle;
            activeCount++;
        } catch (RuntimeException e) {
            // Rollback physical slot allocation to preserve facility consistency
            facility.release(floor, slot);
            throw e;
        }

        return ticket;
    }

    /**
     * Standard entry without pass.
     *
     * @param vehicle   the vehicle requesting entry
     * @param entryTime timestamp of entry
     * @return issued active ticket
     */
    public synchronized ParkingTicket parkVehicle(Vehicle vehicle, LocalDateTime entryTime) {
        return parkVehicle(vehicle, entryTime, null);
    }

    /**
     * Convenience entry with pass defaulting entry timestamp to now.
     *
     * @param vehicle vehicle requesting entry
     * @param pass    access pass
     * @return issued active ticket
     */
    public ParkingTicket parkVehicle(Vehicle vehicle, ParkingPass pass) {
        return parkVehicle(vehicle, LocalDateTime.now(), pass);
    }

    /**
     * Overloaded convenience method defaulting entry timestamp to current time without pass.
     *
     * @param vehicle vehicle requesting entry
     * @return issued active ticket
     */
    public ParkingTicket parkVehicle(Vehicle vehicle) {
        return parkVehicle(vehicle, LocalDateTime.now(), null);
    }

    // =========================================================================
    // Vehicle Exit Workflow
    // =========================================================================

    /**
     * Processes vehicle departure by ticket identifier, calculating parking fees, optional
     * EV charging fees, releasing the facility slot, closing the ticket, and updating collection totals.
     *
     * Invariants enforced:
     * - All validations and fee computations execute BEFORE slot release, ticket closure, or array modification.
     * - Active arrays are compacted in lockstep, preserving ticket-to-vehicle association.
     * - Daily collection cannot be updated twice for the same ticket.
     *
     * @param ticketNumber       unique identifier of the active ticket (non-null, non-blank)
     * @param exitTime           timestamp of departure (must not be prior to entry timestamp)
     * @param chargingEnergyKWh  energy delivered in kWh if vehicle was charged (0.0 if not charged)
     * @return complete, immutable {@link ParkingExitSummary}
     * @throws IllegalArgumentException if ticket is not found, exitTime is invalid, or energy is negative
     * @throws IllegalStateException    if ticket is already closed or facility state is inconsistent
     */
    /**
     * Processes vehicle departure by ticket identifier with an optional presented pass, calculating
     * parking fees, optional EV charging fees, releasing the facility slot, closing the ticket,
     * and updating collection totals.
     *
     * Invariants enforced:
     * - Evaluates time-of-day surge tariffs polymorphically via {@link Vehicle#calculateFee(LocalDateTime, LocalDateTime)}.
     * - Validates presented or ticket-associated {@link ParkingPass} against registration and exit time.
     * - Valid passes waive the base parking fee (Rs 0.0), while EV charging remains separate and billed.
     * - All validations and fee computations execute BEFORE slot release, ticket closure, or array modification.
     * - Active arrays are compacted in lockstep, preserving ticket-to-vehicle association.
     * - Daily collection cannot be updated twice for the same ticket.
     *
     * @param ticketNumber       unique identifier of the active ticket (non-null, non-blank)
     * @param exitTime           timestamp of departure (must not be prior to entry timestamp)
     * @param chargingEnergyKWh  energy delivered in kWh if vehicle was charged (0.0 if not charged)
     * @param presentedPass      optional pass presented at exit gate (nullable)
     * @return complete, immutable {@link ParkingExitSummary}
     * @throws IllegalArgumentException if ticket is not found, exitTime is invalid, or energy is negative/non-finite
     * @throws IllegalStateException    if ticket is already closed or facility state is inconsistent
     */
    public synchronized ParkingExitSummary processExit(String ticketNumber, LocalDateTime exitTime,
                                                       double chargingEnergyKWh, ParkingPass presentedPass) {
        if (!Double.isFinite(chargingEnergyKWh) || chargingEnergyKWh < 0.0) {
            throw new IllegalArgumentException("Charging energy must be a finite, non-negative number: " + chargingEnergyKWh);
        }
        if (ticketNumber == null || ticketNumber.isBlank()) {
            throw new IllegalArgumentException("Ticket number cannot be null or blank.");
        }
        if (exitTime == null) {
            throw new IllegalArgumentException("Exit timestamp cannot be null.");
        }

        String normalizedTicket = ticketNumber.trim();

        // 1. Linear scan of active ticket array to locate target ticket index
        int targetIndex = -1;
        for (int i = 0; i < activeCount; i++) {
            if (activeTickets[i].getTicketNumber().equalsIgnoreCase(normalizedTicket)) {
                targetIndex = i;
                break;
            }
        }

        if (targetIndex == -1) {
            throw new IllegalArgumentException("Invalid ticket: No active parking session found for ticket '"
                    + ticketNumber + "'.");
        }

        ParkingTicket ticket = activeTickets[targetIndex];
        Vehicle vehicle = activeVehicles[targetIndex];

        // 2. Validate ticket active state
        if (!ticket.isActive()) {
            throw new IllegalStateException("Ticket '" + ticketNumber + "' is already closed.");
        }

        // 3. Validate exit timestamp
        if (exitTime.isBefore(ticket.getEntryTime())) {
            throw new IllegalArgumentException("Exit timestamp (" + exitTime + ") cannot be prior to entry timestamp ("
                    + ticket.getEntryTime() + ").");
        }

        // 4. Calculate duration in minutes
        long durationMinutes = Duration.between(ticket.getEntryTime(), exitTime).toMinutes();

        // 5. Calculate polymorphic parking fee via Vehicle.calculateFee(entryTime, exitTime)
        // Dynamically dispatches to DeliveryVan time-window surge or default duration tariff
        double calculatedParkingFee = vehicle.calculateFee(ticket.getEntryTime(), exitTime);

        // 6. Access Pass Evaluation & Fee Waiver (Task 12 Requirement C)
        ParkingPass effectivePass = (presentedPass != null) ? presentedPass : ticket.getPass();
        double parkingFee = calculatedParkingFee;
        if (effectivePass != null) {
            if (!effectivePass.getVehicleRegNo().equalsIgnoreCase(vehicle.getRegistrationNumber())) {
                throw new IllegalArgumentException("Presented pass registration (" + effectivePass.getVehicleRegNo()
                        + ") does not match parked vehicle (" + vehicle.getRegistrationNumber() + ").");
            }
            if (!effectivePass.isValidAt(exitTime)) {
                throw new IllegalArgumentException("Pass '" + effectivePass.getPassId()
                        + "' is expired at departure timestamp: " + exitTime);
            }
            if (effectivePass.isParkingFeeWaived()) {
                parkingFee = 0.0;
            }
        }

        // 7. Pre-validate EV charging inputs and compute charging fee WITHOUT mutating battery state
        double chargingFee = 0.0;
        double acceptedEnergy = 0.0;
        if (chargingEnergyKWh > 0.0) {
            if (vehicle instanceof Chargeable chargeable) {
                Chargeable.validateEnergy(chargingEnergyKWh);
                acceptedEnergy = Math.min(chargingEnergyKWh, chargeable.getRemainingCapacityKWh());
                chargingFee = Chargeable.calculateChargingCost(acceptedEnergy, Chargeable.STANDARD_CHARGING_RATE_PER_KWH);
            } else {
                throw new IllegalArgumentException("Vehicle " + vehicle.getRegistrationNumber()
                        + " (" + vehicle.getVehicleType() + ") does not support EV charging.");
            }
        }

        // 8. Verify and release physical slot in ParkingFacility BEFORE committing any state changes
        int floor = ticket.getFloorNumber();
        int slot = ticket.getSlotNumber();
        boolean released = facility.release(floor, slot);
        if (!released) {
            // Physical slot could not be released. Abort immediately without mutating battery, ticket, or revenue!
            throw new IllegalStateException("Failed to release facility slot [" + floor + ", " + slot
                    + "]. Inconsistent facility state.");
        }

        // 9. Commit state mutations only AFTER physical slot release is confirmed
        if (chargingEnergyKWh > 0.0 && vehicle instanceof Chargeable chargeable) {
            chargeable.charge(chargingEnergyKWh);
        }

        // 10. Close the ticket
        ticket.closeTicket(exitTime);

        // 11. Compact active ticket and vehicle arrays in lockstep (shift elements left)
        for (int j = targetIndex; j < activeCount - 1; j++) {
            activeTickets[j] = activeTickets[j + 1];
            activeVehicles[j] = activeVehicles[j + 1];
        }
        activeTickets[activeCount - 1] = null;
        activeVehicles[activeCount - 1] = null;
        activeCount--;

        // 12. Accumulate daily collection (strictly once upon successful exit completion)
        dailyParkingCollection += parkingFee;
        dailyChargingCollection += chargingFee;
        double totalFee = parkingFee + chargingFee;

        // 13. Create immutable exit summary and append to audit log
        ParkingExitSummary summary = new ParkingExitSummary(
                ticket,
                vehicle,
                durationMinutes,
                parkingFee,
                chargingFee,
                totalFee,
                floor,
                slot,
                exitTime
        );
        completedExits.add(summary);

        return summary;
    }

    /**
     * Departure processing without explicit pass presentation (uses ticket pass if attached).
     *
     * @param ticketNumber      active ticket identifier
     * @param exitTime          timestamp of departure
     * @param chargingEnergyKWh energy delivered in kWh
     * @return exit summary
     */
    public synchronized ParkingExitSummary processExit(String ticketNumber, LocalDateTime exitTime, double chargingEnergyKWh) {
        return processExit(ticketNumber, exitTime, chargingEnergyKWh, null);
    }

    /**
     * Overloaded departure processing for vehicles without EV charging.
     *
     * @param ticketNumber active ticket identifier
     * @param exitTime     timestamp of departure
     * @return exit summary
     */
    public ParkingExitSummary processExit(String ticketNumber, LocalDateTime exitTime) {
        return processExit(ticketNumber, exitTime, 0.0, null);
    }

    /**
     * Overloaded departure processing defaulting exit time to current time with no charging.
     *
     * @param ticketNumber active ticket identifier
     * @return exit summary
     */
    public ParkingExitSummary processExit(String ticketNumber) {
        return processExit(ticketNumber, LocalDateTime.now(), 0.0, null);
    }

    /**
     * Convenience departure processing by vehicle registration number with optional pass.
     *
     * @param registrationNumber license plate of the active vehicle
     * @param exitTime           departure timestamp
     * @param chargingEnergyKWh  optional charging energy delivered
     * @param pass               optional pass presented at exit
     * @return exit summary
     */
    public synchronized ParkingExitSummary processExitByRegistration(String registrationNumber,
                                                                     LocalDateTime exitTime,
                                                                     double chargingEnergyKWh,
                                                                     ParkingPass pass) {
        if (!Double.isFinite(chargingEnergyKWh) || chargingEnergyKWh < 0.0) {
            throw new IllegalArgumentException("Charging energy must be a finite, non-negative number: " + chargingEnergyKWh);
        }
        if (registrationNumber == null || registrationNumber.isBlank()) {
            throw new IllegalArgumentException("Registration number cannot be null or blank.");
        }
        ParkingTicket ticket = findActiveTicketByRegistration(registrationNumber);
        if (ticket == null) {
            throw new IllegalArgumentException("No active parking session found for vehicle registration: "
                    + registrationNumber);
        }
        return processExit(ticket.getTicketNumber(), exitTime, chargingEnergyKWh, pass);
    }

    /**
     * Convenience departure processing by vehicle registration number without pass.
     *
     * @param registrationNumber license plate of the active vehicle
     * @param exitTime           departure timestamp
     * @param chargingEnergyKWh  optional charging energy delivered
     * @return exit summary
     */
    public ParkingExitSummary processExitByRegistration(String registrationNumber, LocalDateTime exitTime, double chargingEnergyKWh) {
        return processExitByRegistration(registrationNumber, exitTime, chargingEnergyKWh, null);
    }

    // =========================================================================
    // Active Ticket & Vehicle Queries (Array Inspection & Association Integrity)
    // =========================================================================

    /**
     * Searches the active ticket array sequentially for a ticket by its identifier.
     *
     * @param ticketNumber ticket ID to locate
     * @return matching active {@link ParkingTicket}, or null if not found
     */
    public synchronized ParkingTicket findActiveTicketByNumber(String ticketNumber) {
        if (ticketNumber == null || ticketNumber.isBlank()) {
            return null;
        }
        String normalized = ticketNumber.trim();
        for (int i = 0; i < activeCount; i++) {
            if (activeTickets[i].getTicketNumber().equalsIgnoreCase(normalized)) {
                return activeTickets[i];
            }
        }
        return null;
    }

    /**
     * Searches the active ticket array sequentially for a ticket by vehicle registration number.
     *
     * @param registrationNumber license plate number
     * @return matching active {@link ParkingTicket}, or null if not found
     */
    public synchronized ParkingTicket findActiveTicketByRegistration(String registrationNumber) {
        if (registrationNumber == null || registrationNumber.isBlank()) {
            return null;
        }
        String normalized = registrationNumber.trim();
        for (int i = 0; i < activeCount; i++) {
            if (activeTickets[i].getVehicleRegNo().equalsIgnoreCase(normalized)) {
                return activeTickets[i];
            }
        }
        return null;
    }

    /**
     * Returns the active {@link Vehicle} associated with a given ticket identifier.
     * Preserves and validates the 1-to-1 association between activeTickets[] and activeVehicles[].
     *
     * @param ticketNumber ticket ID to locate
     * @return matching active {@link Vehicle}, or null if not found
     */
    public synchronized Vehicle findActiveVehicleByTicketNumber(String ticketNumber) {
        if (ticketNumber == null || ticketNumber.isBlank()) {
            return null;
        }
        String normalized = ticketNumber.trim();
        for (int i = 0; i < activeCount; i++) {
            if (activeTickets[i].getTicketNumber().equalsIgnoreCase(normalized)) {
                return activeVehicles[i];
            }
        }
        return null;
    }

    /**
     * Returns the active {@link Vehicle} associated with a given license plate.
     *
     * @param registrationNumber vehicle license plate
     * @return matching active {@link Vehicle}, or null if not found
     */
    public synchronized Vehicle findActiveVehicleByRegistration(String registrationNumber) {
        if (registrationNumber == null || registrationNumber.isBlank()) {
            return null;
        }
        String normalized = registrationNumber.trim();
        for (int i = 0; i < activeCount; i++) {
            if (activeTickets[i].getVehicleRegNo().equalsIgnoreCase(normalized)) {
                return activeVehicles[i];
            }
        }
        return null;
    }

    /**
     * Returns whether a vehicle with the given registration number is currently parked.
     *
     * @param registrationNumber vehicle license plate
     * @return true if currently parked, false otherwise
     */
    public synchronized boolean isVehicleParked(String registrationNumber) {
        return findActiveTicketByRegistration(registrationNumber) != null;
    }

    /**
     * Returns the current number of active parking sessions.
     *
     * @return active ticket count
     */
    public synchronized int getActiveTicketCount() {
        return activeCount;
    }

    /**
     * Returns the maximum active ticket capacity of the service.
     *
     * @return maximum active tickets
     */
    public int getCapacity() {
        return capacity;
    }

    /**
     * Returns a defensive snapshot of active tickets.
     *
     * @return array of active tickets (length equals activeCount)
     */
    public synchronized ParkingTicket[] getActiveTicketsSnapshot() {
        ParkingTicket[] snapshot = new ParkingTicket[activeCount];
        System.arraycopy(activeTickets, 0, snapshot, 0, activeCount);
        return snapshot;
    }

    /**
     * Returns a defensive snapshot of active vehicles, aligned with the tickets snapshot.
     *
     * @return array of active vehicles (length equals activeCount)
     */
    public synchronized Vehicle[] getActiveVehiclesSnapshot() {
        Vehicle[] snapshot = new Vehicle[activeCount];
        System.arraycopy(activeVehicles, 0, snapshot, 0, activeCount);
        return snapshot;
    }

    /**
     * Returns the underlying physical facility.
     *
     * @return parking facility
     */
    public ParkingFacility getFacility() {
        return facility;
    }

    // =========================================================================
    // Financial and Audit Queries
    // =========================================================================

    /**
     * Returns the cumulative parking revenue from completed exit transactions.
     *
     * @return parking revenue in INR
     */
    public synchronized double getDailyParkingCollection() {
        return dailyParkingCollection;
    }

    /**
     * Returns the cumulative EV charging revenue from completed exit transactions.
     *
     * @return charging revenue in INR
     */
    public synchronized double getDailyChargingCollection() {
        return dailyChargingCollection;
    }

    /**
     * Returns the total daily collection combining parking occupancy fees and EV charging revenue.
     *
     * @return total revenue in INR
     */
    public synchronized double getTotalDailyCollection() {
        return dailyParkingCollection + dailyChargingCollection;
    }

    /**
     * Returns an unmodifiable view of all completed exit summaries for audit and reporting.
     *
     * @return unmodifiable list of exit summaries
     */
    public synchronized List<ParkingExitSummary> getCompletedExits() {
        return Collections.unmodifiableList(new ArrayList<>(completedExits));
    }

    /**
     * Returns the total number of completed exit transactions processed today.
     *
     * @return completed exit count
     */
    public synchronized int getCompletedExitCount() {
        return completedExits.size();
    }
}
