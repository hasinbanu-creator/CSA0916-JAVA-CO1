package com.smartparking.service;

import com.smartparking.model.Car;
import com.smartparking.model.ElectricCar;
import com.smartparking.model.ParkingTicket;
import com.smartparking.model.TwoWheeler;
import com.smartparking.model.Vehicle;
import com.smartparking.tasks.ParkingFacility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Comprehensive Unit Tests for {@link ParkingManagementService}.
 *
 * Verifies Task 11 Integration Requirements:
 * 1. 19 Documented Scenarios (TC-1101 to TC-1119) with 1-to-1 matching JUnit test methods.
 * 2. Association integrity: activeTickets[] and activeVehicles[] 1-to-1 mapping across insertion,
 *    removal, array compaction, and defensive snapshot queries.
 * 3. Pre-allocation capacity checks: Ensuring no physical slot is claimed if storage is full.
 * 4. Transactional consistency on exit failures: All-or-nothing atomicity.
 * 5. Strict prevention of revenue double-counting.
 */
class ParkingManagementServiceTest {

    private ParkingManagementService service;
    private final LocalDateTime baseTime = LocalDateTime.of(2026, 11, 10, 8, 0);

    @BeforeEach
    void setUp() {
        // Multi-level facility: Floor 0 has 2 slots, Floor 1 has 1 slot (Total capacity = 3 slots)
        int[] slotsPerFloor = new int[]{2, 1};
        service = new ParkingManagementService(slotsPerFloor);
    }

    // =========================================================================
    // 19 Documented Scenarios (TC-1101 through TC-1119)
    // =========================================================================

    @Test
    @DisplayName("TC-1101: Normal TwoWheeler entry and exit (75 min stay = Rs 20.0)")
    void testTC1101_NormalTwoWheelerEntryAndExit() {
        TwoWheeler bike = new TwoWheeler("KA01AB1001", true);
        ParkingTicket ticket = service.parkVehicle(bike, baseTime);

        assertNotNull(ticket);
        assertEquals("TKT-1001", ticket.getTicketNumber());
        assertEquals("KA01AB1001", ticket.getVehicleRegNo());
        assertEquals(0, ticket.getFloorNumber());
        assertEquals(0, ticket.getSlotNumber());

        ParkingExitSummary summary = service.processExit(ticket.getTicketNumber(), baseTime.plusMinutes(75));
        assertEquals(75, summary.durationMinutes());
        assertEquals(20.0, summary.parkingFee(), 0.001);
        assertEquals(0.0, summary.chargingFee(), 0.001);
        assertEquals(20.0, summary.totalFee(), 0.001);
        assertEquals(0, summary.releasedFloor());
        assertEquals(0, summary.releasedSlot());
        assertFalse(ticket.isActive());
        assertEquals(0, service.getActiveTicketCount());
        assertEquals(20.0, service.getTotalDailyCollection(), 0.001);
    }

    @Test
    @DisplayName("TC-1102: Normal Car entry and exit (120 min stay = Rs 60.0)")
    void testTC1102_NormalCarEntryAndExit() {
        Car car = new Car("TN09CD2002", 5);
        ParkingTicket ticket = service.parkVehicle(car, baseTime);

        ParkingExitSummary summary = service.processExit(ticket.getTicketNumber(), baseTime.plusMinutes(120));
        assertEquals(120, summary.durationMinutes());
        assertEquals(60.0, summary.parkingFee(), 0.001);
        assertEquals(0.0, summary.chargingFee(), 0.001);
        assertEquals(60.0, summary.totalFee(), 0.001);
        assertEquals(60.0, service.getTotalDailyCollection(), 0.001);
    }

    @Test
    @DisplayName("TC-1103: Normal ElectricCar entry and exit with charging (150 min + 25 kWh = Rs 390.0)")
    void testTC1103_NormalElectricCarEntryAndExitWithCharging() {
        ElectricCar ev = new ElectricCar("DL01EV3003", 60.0, 20.0);
        ParkingTicket ticket = service.parkVehicle(ev, baseTime);

        // Stay 150 min (3 hrs @ Rs 30 = Rs 90) + 25 kWh charging (@ Rs 12 = Rs 300) = Rs 390.0
        ParkingExitSummary summary = service.processExit(ticket.getTicketNumber(), baseTime.plusMinutes(150), 25.0);
        assertEquals(150, summary.durationMinutes());
        assertEquals(90.0, summary.parkingFee(), 0.001);
        assertEquals(300.0, summary.chargingFee(), 0.001);
        assertEquals(390.0, summary.totalFee(), 0.001);
        assertEquals(45.0, ev.getCurrentChargeKWh(), 0.001);
        assertEquals(90.0, service.getDailyParkingCollection(), 0.001);
        assertEquals(300.0, service.getDailyChargingCollection(), 0.001);
        assertEquals(390.0, service.getTotalDailyCollection(), 0.001);
    }

    @Test
    @DisplayName("TC-1104: Ticket identifier uniqueness and sequential generation")
    void testTC1104_TicketIdentifierUniqueness() {
        TwoWheeler bike = new TwoWheeler("KA01BB2222", true);
        Car car = new Car("TN02CC3333", 4);
        ElectricCar ev = new ElectricCar("DL03DD4444", 60.0);

        ParkingTicket t1 = service.parkVehicle(bike, baseTime);
        ParkingTicket t2 = service.parkVehicle(car, baseTime.plusMinutes(5));
        ParkingTicket t3 = service.parkVehicle(ev, baseTime.plusMinutes(10));

        assertNotEquals(t1.getTicketNumber(), t2.getTicketNumber());
        assertNotEquals(t2.getTicketNumber(), t3.getTicketNumber());
        assertEquals("TKT-1001", t1.getTicketNumber());
        assertEquals("TKT-1002", t2.getTicketNumber());
        assertEquals("TKT-1003", t3.getTicketNumber());
    }

    @Test
    @DisplayName("TC-1105: Allocation of last free slot in facility")
    void testTC1105_LastFreeSlotAllocation() {
        service.parkVehicle(new TwoWheeler("TN01A1", true), baseTime);
        service.parkVehicle(new Car("TN02B2", 5), baseTime.plusMinutes(5));

        // 3rd vehicle occupies the final available slot (Floor 1, Slot 0)
        ParkingTicket lastTicket = service.parkVehicle(new ElectricCar("TN03C3", 40.0), baseTime.plusMinutes(10));
        assertEquals(1, lastTicket.getFloorNumber());
        assertEquals(0, lastTicket.getSlotNumber());
        assertEquals(3, service.getActiveTicketCount());
    }

    @Test
    @DisplayName("TC-1106: Facility full entry rejection")
    void testTC1106_FacilityFullEntryRejection() {
        service.parkVehicle(new TwoWheeler("TN01A1", true), baseTime);
        service.parkVehicle(new Car("TN02B2", 5), baseTime);
        service.parkVehicle(new ElectricCar("TN03C3", 40.0), baseTime);

        // 4th vehicle must be rejected
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> service.parkVehicle(new Car("TN04D4", 5), baseTime));
        assertTrue(ex.getMessage().contains("full") || ex.getMessage().contains("capacity reached"));
        assertEquals(3, service.getActiveTicketCount());
    }

    @Test
    @DisplayName("TC-1107: Slot re-allocation after vehicle departure")
    void testTC1107_FreedSlotReallocationAfterExit() {
        Car car1 = new Car("TN01A1", 5);
        ParkingTicket t1 = service.parkVehicle(car1, baseTime); // occupies [0, 0]
        service.parkVehicle(new Car("TN02B2", 5), baseTime);   // occupies [0, 1]
        service.parkVehicle(new Car("TN03C3", 5), baseTime);   // occupies [1, 0]

        // Depart vehicle from [0, 0]
        service.processExit(t1.getTicketNumber(), baseTime.plusMinutes(60));
        assertEquals(2, service.getActiveTicketCount());

        // New vehicle claims newly freed [0, 0]
        Car newCar = new Car("TN04D4", 4);
        ParkingTicket tNew = service.parkVehicle(newCar, baseTime.plusMinutes(65));
        assertEquals(0, tNew.getFloorNumber());
        assertEquals(0, tNew.getSlotNumber());
        assertEquals(3, service.getActiveTicketCount());
    }

    @Test
    @DisplayName("TC-1108: Zero-minute parking stay incurs Rs 0.0 fee")
    void testTC1108_ZeroMinuteParkingStayFeeIsZero() {
        Car car = new Car("TN01ZERO", 5);
        ParkingTicket ticket = service.parkVehicle(car, baseTime);
        ParkingExitSummary summary = service.processExit(ticket.getTicketNumber(), baseTime);

        assertEquals(0, summary.durationMinutes());
        assertEquals(0.0, summary.parkingFee(), 0.001);
        assertEquals(0.0, summary.totalFee(), 0.001);
    }

    @Test
    @DisplayName("TC-1109: Exact 60-minute fee slab boundary charges 1 started hour (Rs 30.0)")
    void testTC1109_Exact60MinuteFeeSlabBoundary() {
        Car car = new Car("TN02SIXTY", 5);
        ParkingTicket ticket = service.parkVehicle(car, baseTime);
        ParkingExitSummary summary = service.processExit(ticket.getTicketNumber(), baseTime.plusMinutes(60));

        assertEquals(60, summary.durationMinutes());
        assertEquals(30.0, summary.parkingFee(), 0.001);
    }

    @Test
    @DisplayName("TC-1110: Exact 120-minute fee slab boundary charges 2 started hours (Rs 60.0)")
    void testTC1110_Exact120MinuteFeeSlabBoundary() {
        Car car = new Car("TN04ONETWENTY", 5);
        ParkingTicket ticket = service.parkVehicle(car, baseTime);
        ParkingExitSummary summary = service.processExit(ticket.getTicketNumber(), baseTime.plusMinutes(120));

        assertEquals(120, summary.durationMinutes());
        assertEquals(60.0, summary.parkingFee(), 0.001);
    }

    @Test
    @DisplayName("TC-1111: Next-hour roll boundary at 61 minutes charges 2 started hours (Rs 60.0)")
    void testTC1111_NextHourRollBoundaryAt61Minutes() {
        Car car = new Car("TN03SIXTYONE", 5);
        ParkingTicket ticket = service.parkVehicle(car, baseTime);
        ParkingExitSummary summary = service.processExit(ticket.getTicketNumber(), baseTime.plusMinutes(61));

        assertEquals(61, summary.durationMinutes());
        assertEquals(60.0, summary.parkingFee(), 0.001);
    }

    @Test
    @DisplayName("TC-1112: Duplicate active registration rejection")
    void testTC1112_DuplicateActiveRegistrationRejection() {
        Car car = new Car("TN01AA1111", 5);
        service.parkVehicle(car, baseTime);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> service.parkVehicle(new Car("TN01AA1111", 4), baseTime.plusMinutes(15)));

        assertTrue(ex.getMessage().contains("Duplicate entry rejected"));
        assertEquals(1, service.getActiveTicketCount());
    }

    @Test
    @DisplayName("TC-1113: Unknown ticket exit rejection")
    void testTC1113_UnknownTicketExitRejection() {
        assertThrows(IllegalArgumentException.class,
                () -> service.processExit("TKT-INVALID-999", baseTime.plusMinutes(30)));
    }

    @Test
    @DisplayName("TC-1114: Exit timestamp prior to entry timestamp rejection")
    void testTC1114_ExitTimePriorToEntryTimeRejection() {
        Car car = new Car("TN01AA1111", 5);
        ParkingTicket ticket = service.parkVehicle(car, baseTime);

        assertThrows(IllegalArgumentException.class,
                () -> service.processExit(ticket.getTicketNumber(), baseTime.minusMinutes(10)));
        assertTrue(ticket.isActive());
        assertEquals(1, service.getActiveTicketCount());
    }

    @Test
    @DisplayName("TC-1115: Repeated exit on already closed ticket rejection")
    void testTC1115_RepeatedExitOnClosedTicketRejection() {
        Car car = new Car("TN01AA1111", 5);
        ParkingTicket ticket = service.parkVehicle(car, baseTime);
        service.processExit(ticket.getTicketNumber(), baseTime.plusMinutes(30));

        assertThrows(IllegalArgumentException.class,
                () -> service.processExit(ticket.getTicketNumber(), baseTime.plusMinutes(45)));
    }

    @Test
    @DisplayName("TC-1116: Non-chargeable vehicle charging rejection")
    void testTC1116_NonChargeableVehicleRejectsCharging() {
        Car regularCar = new Car("TN01GAS1", 5);
        ParkingTicket ticket = service.parkVehicle(regularCar, baseTime);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.processExit(ticket.getTicketNumber(), baseTime.plusMinutes(60), 10.0));

        assertTrue(ex.getMessage().contains("does not support EV charging"));
        assertTrue(ticket.isActive());
        assertEquals(1, service.getActiveTicketCount());
    }

    @Test
    @DisplayName("TC-1117: Daily revenue accumulation across multiple departures")
    void testTC1117_DailyRevenueAccumulation() {
        TwoWheeler bike = new TwoWheeler("TN01A1", true);
        Car car = new Car("TN02B2", 5);

        ParkingTicket t1 = service.parkVehicle(bike, baseTime);
        ParkingTicket t2 = service.parkVehicle(car, baseTime);

        service.processExit(t1.getTicketNumber(), baseTime.plusMinutes(60)); // Rs 10.0
        service.processExit(t2.getTicketNumber(), baseTime.plusMinutes(120)); // Rs 60.0

        assertEquals(70.0, service.getTotalDailyCollection(), 0.001);
        assertEquals(2, service.getCompletedExitCount());
    }

    @Test
    @DisplayName("TC-1118: No revenue double-counting on failed operations")
    void testTC1118_NoRevenueDoubleCountingOnFailedOperations() {
        Car car = new Car("TN01A1", 5);
        ParkingTicket ticket = service.parkVehicle(car, baseTime);
        service.processExit(ticket.getTicketNumber(), baseTime.plusMinutes(60));
        assertEquals(30.0, service.getTotalDailyCollection(), 0.001);

        // Attempting repeated exit must fail and not modify revenue
        assertThrows(IllegalArgumentException.class,
                () -> service.processExit(ticket.getTicketNumber(), baseTime.plusMinutes(90)));
        assertEquals(30.0, service.getTotalDailyCollection(), 0.001);
        assertEquals(1, service.getCompletedExitCount());
    }

    @Test
    @DisplayName("TC-1119: Defensive snapshot array isolation")
    void testTC1119_DefensiveSnapshotArrayIsolation() {
        Car car1 = new Car("TN01AA1111", 5);
        TwoWheeler bike = new TwoWheeler("TN02BB2222", true);

        service.parkVehicle(car1, baseTime);
        service.parkVehicle(bike, baseTime.plusMinutes(5));

        ParkingTicket[] snapshot = service.getActiveTicketsSnapshot();
        assertEquals(2, snapshot.length);

        // Mutating external array does not alter internal state
        snapshot[0] = null;
        assertEquals(2, service.getActiveTicketCount());
        assertNotNull(service.findActiveTicketByNumber("TKT-1001"));
    }

    // =========================================================================
    // Targeted Code-Level Audit Tests (Audit Checklist Items 2–5)
    // =========================================================================

    @Test
    @DisplayName("Audit Item 2: Ticket-to-vehicle association is preserved across insertion, removal, compaction and snapshots")
    void testTicketToVehicleAssociationPreservedAcrossInsertionRemovalCompaction() {
        TwoWheeler bike = new TwoWheeler("KA01A1", true);
        Car car = new Car("TN02B2", 5);
        ElectricCar ev = new ElectricCar("DL03C3", 60.0);

        ParkingTicket t1 = service.parkVehicle(bike, baseTime);
        ParkingTicket t2 = service.parkVehicle(car, baseTime.plusMinutes(5));
        ParkingTicket t3 = service.parkVehicle(ev, baseTime.plusMinutes(10));

        // 1. Initial 1-to-1 association verification
        assertSame(bike, service.findActiveVehicleByTicketNumber(t1.getTicketNumber()));
        assertSame(car, service.findActiveVehicleByTicketNumber(t2.getTicketNumber()));
        assertSame(ev, service.findActiveVehicleByTicketNumber(t3.getTicketNumber()));

        assertSame(bike, service.findActiveVehicleByRegistration("KA01A1"));
        assertSame(car, service.findActiveVehicleByRegistration("TN02B2"));
        assertSame(ev, service.findActiveVehicleByRegistration("DL03C3"));

        // 2. Remove MIDDLE element (t2 / car) to trigger array compaction shifting t3 left
        service.processExit(t2.getTicketNumber(), baseTime.plusMinutes(60));
        assertEquals(2, service.getActiveTicketCount());

        // Verify remaining elements preserve exact association post-compaction
        assertNull(service.findActiveVehicleByTicketNumber(t2.getTicketNumber()));
        assertSame(bike, service.findActiveVehicleByTicketNumber(t1.getTicketNumber()));
        assertSame(ev, service.findActiveVehicleByTicketNumber(t3.getTicketNumber()));

        // 3. Insert new vehicle into compacted array
        Car newCar = new Car("MH04D4", 4);
        ParkingTicket t4 = service.parkVehicle(newCar, baseTime.plusMinutes(70));
        assertEquals(3, service.getActiveTicketCount());

        assertSame(bike, service.findActiveVehicleByTicketNumber(t1.getTicketNumber()));
        assertSame(ev, service.findActiveVehicleByTicketNumber(t3.getTicketNumber()));
        assertSame(newCar, service.findActiveVehicleByTicketNumber(t4.getTicketNumber()));

        // 4. Verify snapshot arrays are perfectly aligned
        ParkingTicket[] ticketSnap = service.getActiveTicketsSnapshot();
        Vehicle[] vehicleSnap = service.getActiveVehiclesSnapshot();
        assertEquals(3, ticketSnap.length);
        assertEquals(3, vehicleSnap.length);

        for (int i = 0; i < ticketSnap.length; i++) {
            assertEquals(ticketSnap[i].getVehicleRegNo(), vehicleSnap[i].getRegistrationNumber());
        }
    }

    @Test
    @DisplayName("Audit Item 3: Capacity pre-check prevents orphaned parking slots when ticket storage is full")
    void testCapacityCheckedBeforeSlotAllocationPreventsOrphanedSlots() {
        // Create custom facility with 5 physical slots, but service restricted to capacity = 2 active tickets
        ParkingFacility facility = new ParkingFacility(new int[]{5});
        ParkingManagementService limitedService = new ParkingManagementService(facility, 2);

        limitedService.parkVehicle(new Car("TN01A1", 5), baseTime);
        limitedService.parkVehicle(new Car("TN02B2", 5), baseTime);
        assertEquals(2, limitedService.getActiveTicketCount());

        // 3rd vehicle attempts entry: capacity pre-check throws IllegalStateException
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> limitedService.parkVehicle(new Car("TN03C3", 5), baseTime));
        assertTrue(ex.getMessage().contains("Active ticket storage is full"));

        // Verify that the physical facility was NOT allocated for TN03C3 (only 2 slots occupied in facility)
        // Releasing slots [0, 2], [0, 3], [0, 4] must return false because they were NEVER claimed!
        assertFalse(facility.release(0, 2), "Slot [0, 2] must remain free; no orphaned allocation allowed!");
        assertFalse(facility.release(0, 3), "Slot [0, 3] must remain free!");
        assertFalse(facility.release(0, 4), "Slot [0, 4] must remain free!");
    }

    @Test
    @DisplayName("Audit Item 4: Transactional consistency is strictly preserved if exit operation fails")
    void testTransactionalConsistencyOnExitFailure() {
        Car car = new Car("TN01A1", 5);
        ParkingTicket ticket = service.parkVehicle(car, baseTime);
        int initialActive = service.getActiveTicketCount();
        double initialRevenue = service.getTotalDailyCollection();

        // Failure Case 1: Negative charging energy requested on exit
        assertThrows(IllegalArgumentException.class,
                () -> service.processExit(ticket.getTicketNumber(), baseTime.plusMinutes(60), -5.0));

        // State invariants must be 100% preserved
        assertTrue(ticket.isActive(), "Ticket must remain active when exit validation fails");
        assertEquals(initialActive, service.getActiveTicketCount(), "Active ticket count must be unchanged");
        assertEquals(initialRevenue, service.getTotalDailyCollection(), "Revenue must not be modified");
        assertTrue(service.isVehicleParked("TN01A1"), "Vehicle must remain parked");

        // Failure Case 2: Charging requested on non-chargeable vehicle
        assertThrows(IllegalArgumentException.class,
                () -> service.processExit(ticket.getTicketNumber(), baseTime.plusMinutes(60), 10.0));

        assertTrue(ticket.isActive());
        assertEquals(initialActive, service.getActiveTicketCount());
        assertEquals(initialRevenue, service.getTotalDailyCollection());

        // Failure Case 3: Invalid exit timestamp (prior to entry)
        assertThrows(IllegalArgumentException.class,
                () -> service.processExit(ticket.getTicketNumber(), baseTime.minusMinutes(30)));

        assertTrue(ticket.isActive());
        assertEquals(initialActive, service.getActiveTicketCount());
        assertEquals(initialRevenue, service.getTotalDailyCollection());
    }

    @Test
    @DisplayName("Audit Item 5: Daily collection cannot be updated twice for the same ticket")
    void testDailyCollectionCannotBeUpdatedTwiceForSameTicket() {
        Car car = new Car("TN01A1", 5);
        ParkingTicket ticket = service.parkVehicle(car, baseTime);

        ParkingExitSummary firstExit = service.processExit(ticket.getTicketNumber(), baseTime.plusMinutes(60));
        assertEquals(30.0, service.getTotalDailyCollection(), 0.001);
        assertEquals(1, service.getCompletedExitCount());

        // Second exit attempt on the same ticket must be rejected
        assertThrows(IllegalArgumentException.class,
                () -> service.processExit(ticket.getTicketNumber(), baseTime.plusMinutes(90)));

        // Revenue must remain strictly 30.0 and completed exits strictly 1
        assertEquals(30.0, service.getTotalDailyCollection(), 0.001);
        assertEquals(1, service.getCompletedExitCount());
    }

    @Test
    @DisplayName("Fix EV Charging Consistency: Failed slot release leaves battery, ticket, active count, and revenue unchanged")
    void testEvChargingConsistencyWhenSlotReleaseFails() {
        ElectricCar ev = new ElectricCar("TN03EV77", 60.0, 20.0);
        ParkingTicket ticket = service.parkVehicle(ev, baseTime);

        double initialBattery = ev.getCurrentChargeKWh();
        int initialActiveCount = service.getActiveTicketCount();
        double initialRevenue = service.getTotalDailyCollection();
        int initialCompletedExits = service.getCompletedExitCount();

        // Simulate physical slot release failure by making the slot unavailable out-of-band:
        // Calling facility.release() directly frees the slot (sets it to null).
        // Consequently, when processExit later attempts facility.release(), it will return false!
        boolean directRelease = service.getFacility().release(ticket.getFloorNumber(), ticket.getSlotNumber());
        assertTrue(directRelease, "Setup: Direct release of slot succeeded to simulate release failure on exit");

        // Attempt exit with positive charging energy (15.0 kWh)
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> service.processExit(ticket.getTicketNumber(), baseTime.plusMinutes(90), 15.0));

        assertTrue(ex.getMessage().contains("Failed to release facility slot"));

        // Verify that NO state was mutated:
        assertEquals(initialBattery, ev.getCurrentChargeKWh(), 0.0001,
                "Battery level MUST NOT change if exit fails!");
        assertTrue(ticket.isActive(),
                "Ticket MUST remain active if exit fails!");
        assertEquals(initialActiveCount, service.getActiveTicketCount(),
                "Active ticket count MUST remain unchanged!");
        assertEquals(initialRevenue, service.getTotalDailyCollection(), 0.0001,
                "Revenue MUST NOT be updated if exit fails!");
        assertEquals(initialCompletedExits, service.getCompletedExitCount(),
                "Completed exits ledger MUST NOT be modified if exit fails!");
        assertNotNull(service.findActiveTicketByNumber(ticket.getTicketNumber()),
                "Ticket MUST still be present in active storage!");
        assertSame(ev, service.findActiveVehicleByTicketNumber(ticket.getTicketNumber()),
                "Vehicle association MUST remain intact!");
    }

    @Test
    @DisplayName("Invalid Case: Unknown or unsupported vehicle type is strictly rejected")
    void testUnknownOrUnsupportedVehicleTypeRejection() {
        // A. Custom Vehicle subclass with unsupported category
        Vehicle unsupportedVehicle = new Vehicle("TN01TRUCK1", "TRUCK") {
            @Override
            public double calculateFee(long durationMinutes) {
                return 50.0;
            }
        };

        IllegalArgumentException ex1 = assertThrows(IllegalArgumentException.class,
                () -> service.parkVehicle(unsupportedVehicle, baseTime));
        assertTrue(ex1.getMessage().contains("Unknown or unsupported vehicle type"));

        // B. Null vehicle reference
        IllegalArgumentException ex2 = assertThrows(IllegalArgumentException.class,
                () -> service.parkVehicle(null, baseTime));
        assertTrue(ex2.getMessage().contains("Vehicle cannot be null"));

        assertEquals(0, service.getActiveTicketCount(), "No vehicle should be admitted on validation failure");
    }

    @Test
    @DisplayName("Invalid Case: Invalid floor and slot coordinates handling in facility and ticket")
    void testInvalidFloorOrSlotCoordinatesHandling() {
        ParkingFacility facility = service.getFacility();

        // A. Out-of-bounds coordinates on ParkingFacility.release() return false
        assertFalse(facility.release(-1, 0), "Negative floor must return false");
        assertFalse(facility.release(99, 0), "Out-of-bounds floor must return false");
        assertFalse(facility.release(0, -1), "Negative slot must return false");
        assertFalse(facility.release(0, 99), "Out-of-bounds slot must return false");

        // B. Negative coordinates in ParkingTicket constructor throw IllegalArgumentException
        assertThrows(IllegalArgumentException.class,
                () -> new ParkingTicket("TKT-TEST", "TN01A1", "CAR", -1, 0, baseTime));
        assertThrows(IllegalArgumentException.class,
                () -> new ParkingTicket("TKT-TEST", "TN01A1", "CAR", 0, -1, baseTime));
    }

    @Test
    @DisplayName("Exit summary receipt formatting produces structured receipt output")
    void testExitSummaryReceiptFormatting() {
        Car car = new Car("TN01AA1111", 5);
        ParkingTicket ticket = service.parkVehicle(car, baseTime);
        ParkingExitSummary summary = service.processExit(ticket.getTicketNumber(), baseTime.plusMinutes(60));

        String receipt = summary.formatReceipt();
        assertTrue(receipt.contains("PARKING EXIT RECEIPT"));
        assertTrue(receipt.contains("Ticket ID      : TKT-1001"));
        assertTrue(receipt.contains("Registration   : TN01AA1111"));
        assertTrue(receipt.contains("Parking Fee    : Rs 30.00"));
        assertTrue(receipt.contains("TOTAL PAID     : Rs 30.00"));
    }

    @Test
    @DisplayName("Invalid Case: Double.NaN charging energy is rejected before state changes, leaving all fields unchanged")
    void testExitWithNaNChargingEnergyRejectionPreservesState() {
        ElectricCar ev = new ElectricCar("TN03EV99", 60.0, 20.0);
        ParkingTicket ticket = service.parkVehicle(ev, baseTime);

        double initialBattery = ev.getCurrentChargeKWh();
        int initialActiveCount = service.getActiveTicketCount();
        double initialParkingRev = service.getDailyParkingCollection();
        double initialChargingRev = service.getDailyChargingCollection();
        double initialTotalRev = service.getTotalDailyCollection();
        int initialCompletedExits = service.getCompletedExitCount();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.processExit(ticket.getTicketNumber(), baseTime.plusMinutes(60), Double.NaN));
        assertTrue(ex.getMessage().contains("finite"), "Error message should identify non-finite input");

        // Verify that ticket state, active count, battery level, parking revenue, charging revenue,
        // and completed exit count remain completely unchanged
        assertTrue(ticket.isActive(), "Ticket must remain active after rejection");
        assertEquals(initialActiveCount, service.getActiveTicketCount(), "Active count must remain unchanged");
        assertEquals(initialBattery, ev.getCurrentChargeKWh(), 0.0001, "Battery level must remain unchanged");
        assertEquals(initialParkingRev, service.getDailyParkingCollection(), 0.0001, "Parking revenue must remain unchanged");
        assertEquals(initialChargingRev, service.getDailyChargingCollection(), 0.0001, "Charging revenue must remain unchanged");
        assertEquals(initialTotalRev, service.getTotalDailyCollection(), 0.0001, "Total revenue must remain unchanged");
        assertEquals(initialCompletedExits, service.getCompletedExitCount(), "Completed exit count must remain unchanged");
        assertNotNull(service.findActiveTicketByNumber(ticket.getTicketNumber()), "Ticket must still be active in storage");
        assertSame(ev, service.findActiveVehicleByTicketNumber(ticket.getTicketNumber()), "Vehicle association must remain intact");
    }

    @Test
    @DisplayName("Invalid Case: Double.POSITIVE_INFINITY charging energy is rejected before state changes, leaving all fields unchanged")
    void testExitWithPositiveInfinityChargingEnergyRejectionPreservesState() {
        ElectricCar ev = new ElectricCar("TN03EV88", 60.0, 20.0);
        ParkingTicket ticket = service.parkVehicle(ev, baseTime);

        double initialBattery = ev.getCurrentChargeKWh();
        int initialActiveCount = service.getActiveTicketCount();
        double initialParkingRev = service.getDailyParkingCollection();
        double initialChargingRev = service.getDailyChargingCollection();
        double initialTotalRev = service.getTotalDailyCollection();
        int initialCompletedExits = service.getCompletedExitCount();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.processExit(ticket.getTicketNumber(), baseTime.plusMinutes(60), Double.POSITIVE_INFINITY));
        assertTrue(ex.getMessage().contains("finite"), "Error message should identify non-finite input");

        // Verify that ticket state, active count, battery level, parking revenue, charging revenue,
        // and completed exit count remain completely unchanged
        assertTrue(ticket.isActive(), "Ticket must remain active after rejection");
        assertEquals(initialActiveCount, service.getActiveTicketCount(), "Active count must remain unchanged");
        assertEquals(initialBattery, ev.getCurrentChargeKWh(), 0.0001, "Battery level must remain unchanged");
        assertEquals(initialParkingRev, service.getDailyParkingCollection(), 0.0001, "Parking revenue must remain unchanged");
        assertEquals(initialChargingRev, service.getDailyChargingCollection(), 0.0001, "Charging revenue must remain unchanged");
        assertEquals(initialTotalRev, service.getTotalDailyCollection(), 0.0001, "Total revenue must remain unchanged");
        assertEquals(initialCompletedExits, service.getCompletedExitCount(), "Completed exit count must remain unchanged");
        assertNotNull(service.findActiveTicketByNumber(ticket.getTicketNumber()), "Ticket must still be active in storage");
        assertSame(ev, service.findActiveVehicleByTicketNumber(ticket.getTicketNumber()), "Vehicle association must remain intact");
    }
}
