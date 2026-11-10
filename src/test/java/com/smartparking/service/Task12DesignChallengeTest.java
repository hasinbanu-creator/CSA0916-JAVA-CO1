package com.smartparking.service;

import com.smartparking.model.Car;
import com.smartparking.model.DeliveryVan;
import com.smartparking.model.ElectricBike;
import com.smartparking.model.ElectricCar;
import com.smartparking.model.MonthlyPass;
import com.smartparking.model.ParkingTicket;
import com.smartparking.model.TwoWheeler;
import com.smartparking.model.VisitorOneDayPass;
import com.smartparking.tasks.ParkingFacility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Comprehensive Integration Tests for Task 12: Day-4 Final Design Challenge (8 Marks).
 *
 * Verifies all four newly introduced capabilities:
 * 1. Electric Bike flat-fee parking model.
 * 2. Delivery Van dynamic time-of-day surge pricing (9 AM - 5 PM).
 * 3. Visitor One-Day Pass validation, fee waiver, and separate EV charging billing.
 * 4. Multi-level Parking Facility expanded to 5 unequal floors using jagged arrays.
 */
class Task12DesignChallengeTest {

    private ParkingManagementService service;
    private final LocalDateTime baseTime = LocalDateTime.of(2026, 11, 10, 8, 0);

    @BeforeEach
    void setUp() {
        // Default 2-floor facility for vehicle-level and pass-level tests
        int[] slotsPerFloor = new int[]{3, 3};
        service = new ParkingManagementService(slotsPerFloor);
    }

    // =========================================================================
    // 1. Electric Bike Integration Tests
    // =========================================================================

    @Test
    @DisplayName("Task 12 - E-Bike: Admitted and charged flat parking fee on departure")
    void testElectricBikeEntryAndFlatFeeExit() {
        ElectricBike bike = new ElectricBike("EB-101");
        ParkingTicket ticket = service.parkVehicle(bike, baseTime);

        assertNotNull(ticket);
        assertEquals("ELECTRIC_BIKE", ticket.getVehicleType());
        assertEquals("EB-101", ticket.getVehicleRegNo());

        // Stay 120 minutes: flat fee of Rs 15.0 applies
        ParkingExitSummary summary = service.processExit(ticket.getTicketNumber(), baseTime.plusMinutes(120));
        assertEquals(120, summary.durationMinutes());
        assertEquals(15.0, summary.parkingFee(), 0.001);
        assertEquals(0.0, summary.chargingFee(), 0.001);
        assertEquals(15.0, summary.totalFee(), 0.001);
        assertEquals(15.0, service.getTotalDailyCollection(), 0.001);
    }

    @Test
    @DisplayName("Task 12 - E-Bike: Zero-duration stay costs Rs 0.0")
    void testElectricBikeZeroDurationExit() {
        ElectricBike bike = new ElectricBike("EB-102");
        ParkingTicket ticket = service.parkVehicle(bike, baseTime);

        ParkingExitSummary summary = service.processExit(ticket.getTicketNumber(), baseTime);
        assertEquals(0, summary.durationMinutes());
        assertEquals(0.0, summary.parkingFee(), 0.001);
        assertEquals(0.0, summary.totalFee(), 0.001);
    }

    @Test
    @DisplayName("Task 12 - E-Bike: Rejects EV charging requests as non-chargeable vehicle")
    void testElectricBikeRejectsChargingRequest() {
        ElectricBike bike = new ElectricBike("EB-103");
        ParkingTicket ticket = service.parkVehicle(bike, baseTime);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.processExit(ticket.getTicketNumber(), baseTime.plusMinutes(60), 5.0));
        assertTrue(ex.getMessage().contains("does not support EV charging"));
    }

    // =========================================================================
    // 2. Delivery Van Dynamic Time-Window Tests
    // =========================================================================

    @Test
    @DisplayName("Task 12 - Delivery Van: Stays during 9 AM - 5 PM peak window are charged Rs 60/hr (double car rate)")
    void testDeliveryVanDuringPeakHours() {
        DeliveryVan van = new DeliveryVan("VAN-PEAK-1", "BlueDart");
        LocalDateTime entry = LocalDateTime.of(2026, 11, 10, 10, 0);
        LocalDateTime exit = LocalDateTime.of(2026, 11, 10, 12, 0); // 120 min = 2 hrs @ Rs 60 = Rs 120

        ParkingTicket ticket = service.parkVehicle(van, entry);
        ParkingExitSummary summary = service.processExit(ticket.getTicketNumber(), exit);

        assertEquals(120, summary.durationMinutes());
        assertEquals(120.0, summary.parkingFee(), 0.001);
        assertEquals(120.0, summary.totalFee(), 0.001);
    }

    @Test
    @DisplayName("Task 12 - Delivery Van: Entry before 9:00 AM charged at standard off-peak car rate (Rs 30/hr)")
    void testDeliveryVanBefore9AMOffPeak() {
        DeliveryVan van = new DeliveryVan("VAN-EARLY-1");
        LocalDateTime entry = LocalDateTime.of(2026, 11, 10, 7, 0);
        LocalDateTime exit = LocalDateTime.of(2026, 11, 10, 8, 30); // 90 min = 2 hrs @ Rs 30 = Rs 60

        ParkingTicket ticket = service.parkVehicle(van, entry);
        ParkingExitSummary summary = service.processExit(ticket.getTicketNumber(), exit);

        assertEquals(90, summary.durationMinutes());
        assertEquals(60.0, summary.parkingFee(), 0.001);
    }

    @Test
    @DisplayName("Task 12 - Delivery Van: Entry exactly at 9:00 AM window boundary incurs peak surge rate")
    void testDeliveryVanExactlyAt9AM() {
        DeliveryVan van = new DeliveryVan("VAN-9AM");
        LocalDateTime entry = LocalDateTime.of(2026, 11, 10, 9, 0);
        LocalDateTime exit = LocalDateTime.of(2026, 11, 10, 10, 0); // 60 min = 1 hr @ Rs 60 = Rs 60

        ParkingTicket ticket = service.parkVehicle(van, entry);
        ParkingExitSummary summary = service.processExit(ticket.getTicketNumber(), exit);

        assertEquals(60.0, summary.parkingFee(), 0.001);
    }

    @Test
    @DisplayName("Task 12 - Delivery Van: Entry exactly at 5:00 PM (17:00) incurs off-peak rate")
    void testDeliveryVanExactlyAt5PM() {
        DeliveryVan van = new DeliveryVan("VAN-5PM");
        LocalDateTime entry = LocalDateTime.of(2026, 11, 10, 17, 0);
        LocalDateTime exit = LocalDateTime.of(2026, 11, 10, 18, 0); // 60 min = 1 hr @ Rs 30 = Rs 30

        ParkingTicket ticket = service.parkVehicle(van, entry);
        ParkingExitSummary summary = service.processExit(ticket.getTicketNumber(), exit);

        assertEquals(30.0, summary.parkingFee(), 0.001);
    }

    @Test
    @DisplayName("Task 12 - Delivery Van: Entry after 5:00 PM incurs off-peak rate")
    void testDeliveryVanAfter5PM() {
        DeliveryVan van = new DeliveryVan("VAN-LATE-1");
        LocalDateTime entry = LocalDateTime.of(2026, 11, 10, 18, 0);
        LocalDateTime exit = LocalDateTime.of(2026, 11, 10, 20, 0); // 120 min = 2 hrs @ Rs 30 = Rs 60

        ParkingTicket ticket = service.parkVehicle(van, entry);
        ParkingExitSummary summary = service.processExit(ticket.getTicketNumber(), exit);

        assertEquals(60.0, summary.parkingFee(), 0.001);
    }

    @Test
    @DisplayName("Task 12 - Delivery Van: Stay spanning 9:00 AM window start segments off-peak and peak started hours")
    void testDeliveryVanSpanning9AMWindowStart() {
        DeliveryVan van = new DeliveryVan("VAN-SPAN-1");
        LocalDateTime entry = LocalDateTime.of(2026, 11, 10, 8, 0);
        LocalDateTime exit = LocalDateTime.of(2026, 11, 10, 10, 0); // 60 min off-peak + 60 min peak = Rs 30 + Rs 60 = Rs 90

        ParkingTicket ticket = service.parkVehicle(van, entry);
        ParkingExitSummary summary = service.processExit(ticket.getTicketNumber(), exit);

        assertEquals(90.0, summary.parkingFee(), 0.001);
    }

    @Test
    @DisplayName("Task 12 - Delivery Van: Stay spanning 5:00 PM window end segments peak and off-peak started hours")
    void testDeliveryVanSpanning5PMWindowEnd() {
        DeliveryVan van = new DeliveryVan("VAN-SPAN-2");
        LocalDateTime entry = LocalDateTime.of(2026, 11, 10, 16, 0);
        LocalDateTime exit = LocalDateTime.of(2026, 11, 10, 18, 0); // 60 min peak + 60 min off-peak = Rs 60 + Rs 30 = Rs 90

        ParkingTicket ticket = service.parkVehicle(van, entry);
        ParkingExitSummary summary = service.processExit(ticket.getTicketNumber(), exit);

        assertEquals(90.0, summary.parkingFee(), 0.001);
    }

    @Test
    @DisplayName("Task 12 - Delivery Van: Overnight stay crossing midnight is billed entirely at off-peak car rate")
    void testDeliveryVanOvernightMidnightCrossingIntegration() {
        DeliveryVan van = new DeliveryVan("VAN-MIDNIGHT-INT");
        LocalDateTime entry = LocalDateTime.of(2026, 11, 10, 23, 0);
        LocalDateTime exit = LocalDateTime.of(2026, 11, 11, 2, 0); // 180 min off-peak = 3 started hrs @ Rs 30 = Rs 90.0

        ParkingTicket ticket = service.parkVehicle(van, entry);
        ParkingExitSummary summary = service.processExit(ticket.getTicketNumber(), exit);

        assertEquals(180, summary.durationMinutes());
        assertEquals(90.0, summary.parkingFee(), 0.001);
        assertEquals(90.0, summary.totalFee(), 0.001);
    }

    @Test
    @DisplayName("Task 12 - Delivery Van: Multi-day stay across two days correctly segments peak and off-peak started hours")
    void testDeliveryVanMultiDayStayIntegration() {
        DeliveryVan van = new DeliveryVan("VAN-MULTIDAY-INT");
        LocalDateTime entry = LocalDateTime.of(2026, 11, 10, 8, 0);
        LocalDateTime exit = LocalDateTime.of(2026, 11, 11, 10, 0); // 1,560 min = 9 hr peak @ Rs 60 + 17 hr off-peak @ Rs 30 = Rs 1,050.0

        ParkingTicket ticket = service.parkVehicle(van, entry);
        ParkingExitSummary summary = service.processExit(ticket.getTicketNumber(), exit);

        assertEquals(1560, summary.durationMinutes());
        assertEquals(1050.0, summary.parkingFee(), 0.001);
        assertEquals(1050.0, summary.totalFee(), 0.001);
    }

    @Test
    @DisplayName("Task 12 - Delivery Van: Disconnected off-peak intervals (08:30-09:00 and 17:00-17:30) aggregate to 60 min (Rs 510 total)")
    void testDeliveryVanAggregatedDisconnectedOffPeakIntervalsIntegration() {
        DeliveryVan van = new DeliveryVan("VAN-AGG-INT", "Prime Logistics");
        LocalDateTime entry = LocalDateTime.of(2026, 11, 10, 8, 30);
        LocalDateTime exit = LocalDateTime.of(2026, 11, 10, 17, 30); // 540 min: 480 peak + 60 aggregate off-peak

        ParkingTicket ticket = service.parkVehicle(van, entry);
        ParkingExitSummary summary = service.processExit(ticket.getTicketNumber(), exit);

        assertEquals(540, summary.durationMinutes());
        assertEquals(510.0, summary.parkingFee(), 0.001, "Expected Rs 510.0 (Rs 480 peak + Rs 30 off-peak)");
        assertEquals(0.0, summary.chargingFee(), 0.001);
        assertEquals(510.0, summary.totalFee(), 0.001);
    }

    // =========================================================================
    // 3. Visitor One-Day Pass Integration Tests
    // =========================================================================

    @Test
    @DisplayName("Task 12 - Visitor Pass: Valid one-day pass waives base parking fee completely")
    void testVisitorOneDayPassWaivesParkingFee() {
        LocalDate date = LocalDate.of(2026, 11, 10);
        VisitorOneDayPass pass = new VisitorOneDayPass("VP-101", "Alice Visitor", "KA01VP1", date);

        Car car = new Car("KA01VP1", 5);
        LocalDateTime entry = LocalDateTime.of(2026, 11, 10, 9, 30);
        LocalDateTime exit = LocalDateTime.of(2026, 11, 10, 15, 30); // 6 hours stay (standard = Rs 180)

        ParkingTicket ticket = service.parkVehicle(car, entry, pass);
        assertTrue(ticket.hasPass());
        assertSame(pass, ticket.getPass());

        ParkingExitSummary summary = service.processExit(ticket.getTicketNumber(), exit);
        assertEquals(360, summary.durationMinutes());
        assertEquals(0.0, summary.parkingFee(), 0.001, "Parking fee must be waived for valid visitor pass");
        assertEquals(0.0, summary.totalFee(), 0.001);
    }

    @Test
    @DisplayName("Task 12 - Visitor Pass with EV: Parking fee is waived, but EV charging fee remains strictly billed")
    void testVisitorOneDayPassWithElectricCarSeparateChargingBilling() {
        LocalDate date = LocalDate.of(2026, 11, 10);
        VisitorOneDayPass pass = new VisitorOneDayPass("VP-EV-1", "Bob EV Visitor", "DL01EV1", date);

        ElectricCar ev = new ElectricCar("DL01EV1", 60.0, 20.0);
        LocalDateTime entry = LocalDateTime.of(2026, 11, 10, 10, 0);
        LocalDateTime exit = LocalDateTime.of(2026, 11, 10, 14, 0); // 4 hours stay

        ParkingTicket ticket = service.parkVehicle(ev, entry, pass);

        // Requested 25 kWh charging (@ Rs 12 = Rs 300)
        ParkingExitSummary summary = service.processExit(ticket.getTicketNumber(), exit, 25.0);
        assertEquals(0.0, summary.parkingFee(), 0.001, "Base parking fee must be waived by pass");
        assertEquals(300.0, summary.chargingFee(), 0.001, "EV charging must be billed independently!");
        assertEquals(300.0, summary.totalFee(), 0.001);
        assertEquals(45.0, ev.getCurrentChargeKWh(), 0.001);
    }

    @Test
    @DisplayName("Task 12 - Visitor Pass: Pass registration mismatch throws IllegalArgumentException at entry")
    void testVisitorPassRegistrationMismatchAtEntry() {
        LocalDate date = LocalDate.of(2026, 11, 10);
        VisitorOneDayPass pass = new VisitorOneDayPass("VP-102", "Alice", "KA01TRUE", date);
        Car car = new Car("KA01FALSE", 5);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.parkVehicle(car, baseTime, pass));
        assertTrue(ex.getMessage().contains("does not match vehicle registration"));
    }

    @Test
    @DisplayName("Task 12 - Visitor Pass: Expired pass at entry throws IllegalArgumentException")
    void testVisitorPassExpiredAtEntry() {
        LocalDate yesterday = LocalDate.of(2026, 11, 9);
        VisitorOneDayPass expiredPass = new VisitorOneDayPass("VP-EXPIRED", "Late Visitor", "KA01A1", yesterday);
        Car car = new Car("KA01A1", 5);

        LocalDateTime entryToday = LocalDateTime.of(2026, 11, 10, 8, 0);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.parkVehicle(car, entryToday, expiredPass));
        assertTrue(ex.getMessage().contains("is not valid at entry timestamp"));
    }

    @Test
    @DisplayName("Task 12 - Visitor Pass: Pass expiring before departure throws IllegalArgumentException at exit")
    void testVisitorPassExpiredAtExit() {
        LocalDate date = LocalDate.of(2026, 11, 10);
        VisitorOneDayPass pass = new VisitorOneDayPass("VP-OVERNIGHT", "Visitor", "KA01A1", date);
        Car car = new Car("KA01A1", 5);

        LocalDateTime entry = LocalDateTime.of(2026, 11, 10, 18, 0);
        LocalDateTime exitNextDay = LocalDateTime.of(2026, 11, 11, 8, 0); // Overnight past midnight!

        ParkingTicket ticket = service.parkVehicle(car, entry, pass);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.processExit(ticket.getTicketNumber(), exitNextDay));
        assertTrue(ex.getMessage().contains("is expired at departure timestamp"));
    }

    @Test
    @DisplayName("Task 12 - Visitor Pass: Pass presented directly at exit gate waives fee")
    void testVisitorPassPresentedAtExitGate() {
        Car car = new Car("KA01GATE", 5);
        ParkingTicket ticket = service.parkVehicle(car, baseTime); // Entered as visitor without pass initially
        assertFalse(ticket.hasPass());

        // Purchased one-day pass at campus kiosk during visit, presented at exit
        LocalDate date = LocalDate.of(2026, 11, 10);
        VisitorOneDayPass kioskPass = new VisitorOneDayPass("VP-KIOSK", "Kiosk Buyer", "KA01GATE", date);

        ParkingExitSummary summary = service.processExit(ticket.getTicketNumber(), baseTime.plusMinutes(180), 0.0, kioskPass);
        assertEquals(0.0, summary.parkingFee(), 0.001, "Pass presented at exit gate must waive parking fee");
        assertEquals(0.0, summary.totalFee(), 0.001);
    }

    @Test
    @DisplayName("Task 12 - Visitor Pass: Expired pass at exit gate preserves transactional state and aborts without mutation")
    void testPassValidationFailureAtExitPreservesTransactionalState() {
        ElectricCar ev = new ElectricCar("KA01EVSAFE", 60.0, 20.0);
        ParkingTicket ticket = service.parkVehicle(ev, baseTime);

        int initialActiveCount = service.getActiveTicketCount();
        double initialParkingRev = service.getTotalDailyCollection();
        double initialChargingRev = service.getDailyChargingCollection();
        int initialExitsCount = service.getCompletedExits().size();

        // Present an expired pass at exit gate
        LocalDate yesterday = LocalDate.of(2026, 11, 9);
        VisitorOneDayPass expiredPass = new VisitorOneDayPass("VP-EXP-GATE", "Late Visitor", "KA01EVSAFE", yesterday);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.processExit(ticket.getTicketNumber(), baseTime.plusHours(2), 15.0, expiredPass));
        assertTrue(ex.getMessage().contains("is expired at departure timestamp"));

        // Assert rollback & transactional safety:
        assertTrue(ticket.isActive(), "Ticket must remain active after exit validation failure");
        assertNull(ticket.getExitTime(), "Exit time must remain null");
        assertEquals(initialActiveCount, service.getActiveTicketCount(), "Active ticket count must be unchanged");
        assertTrue(service.isVehicleParked("KA01EVSAFE"), "Vehicle must still be registered as active");
        assertEquals(20.0, ev.getCurrentChargeKWh(), 0.001, "EV battery must not be mutated");
        assertEquals(initialParkingRev, service.getTotalDailyCollection(), 0.001, "Parking revenue must not change");
        assertEquals(initialChargingRev, service.getDailyChargingCollection(), 0.001, "Charging revenue must not change");
        assertEquals(initialExitsCount, service.getCompletedExits().size(), "No completed exit summary should be logged");
    }

    @Test
    @DisplayName("Task 12 - Visitor Pass: Mismatched pass registration at exit gate throws IllegalArgumentException and preserves state")
    void testVisitorPassWithMismatchedRegistrationAtExitGateRejected() {
        Car car = new Car("KA01MATCH1", 5);
        ParkingTicket ticket = service.parkVehicle(car, baseTime);

        LocalDate today = LocalDate.of(2026, 11, 10);
        VisitorOneDayPass wrongVehiclePass = new VisitorOneDayPass("VP-MISMATCH", "Visitor", "KA01OTHER", today);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.processExit(ticket.getTicketNumber(), baseTime.plusHours(2), 0.0, wrongVehiclePass));
        assertTrue(ex.getMessage().contains("does not match parked vehicle"));

        assertTrue(ticket.isActive(), "Ticket must remain active");
        assertEquals(1, service.getActiveTicketCount());
    }

    // =========================================================================
    // 4. Monthly Pass Integration Tests
    // =========================================================================

    @Test
    @DisplayName("Task 12 - Monthly Pass: Staff and student passes waive parking fee")
    void testMonthlyPassIntegration() {
        YearMonth month = YearMonth.of(2026, 11);
        MonthlyPass staffPass = new MonthlyPass("MP-ST-1", "EMP-100", "STAFF", "TN01STAFF", month);
        Car car = new Car("TN01STAFF", 5);

        ParkingTicket ticket = service.parkVehicle(car, baseTime, staffPass);
        ParkingExitSummary summary = service.processExit(ticket.getTicketNumber(), baseTime.plusMinutes(240));

        assertEquals(0.0, summary.parkingFee(), 0.001);
        assertEquals(0.0, summary.totalFee(), 0.001);
    }

    // =========================================================================
    // 5. Five-Floor Parking Facility with Unequal Capacities
    // =========================================================================

    @Test
    @DisplayName("Task 12 - Five-Floor Facility: Correct unequal capacities on all 5 floors [4, 3, 3, 2, 2]")
    void testFiveFloorFacilityConfiguration() {
        // Floor 0: 4 slots, Floor 1: 3 slots, Floor 2: 3 slots, Floor 3: 2 slots, Floor 4: 2 slots (Total = 14)
        int[] fiveFloors = new int[]{4, 3, 3, 2, 2};
        ParkingFacility facility = new ParkingFacility(fiveFloors);
        ParkingManagementService fiveFloorService = new ParkingManagementService(facility, 14);

        assertEquals(5, facility.getFloorCount());
        assertEquals(4, facility.getSlotCount(0));
        assertEquals(3, facility.getSlotCount(1));
        assertEquals(3, facility.getSlotCount(2));
        assertEquals(2, facility.getSlotCount(3));
        assertEquals(2, facility.getSlotCount(4));
        assertEquals(14, facility.getTotalCapacity());
        assertEquals(14, fiveFloorService.getCapacity());
    }

    @Test
    @DisplayName("Task 12 - Five-Floor Facility: Sequential allocation reaches every floor and fills last slot on Floor 4")
    void testFiveFloorAllocationReachesEveryFloor() {
        int[] fiveFloors = new int[]{4, 3, 3, 2, 2}; // Total = 14 slots across 5 floors
        ParkingManagementService fiveFloorService = new ParkingManagementService(fiveFloors);

        // Admit 14 vehicles
        int count = 0;
        for (int f = 0; f < fiveFloors.length; f++) {
            for (int s = 0; s < fiveFloors[f]; s++) {
                count++;
                Car car = new Car("CAR-" + count, 4);
                ParkingTicket ticket = fiveFloorService.parkVehicle(car, baseTime);

                assertEquals(f, ticket.getFloorNumber(), "Vehicle " + count + " must be allocated to floor " + f);
                assertEquals(s, ticket.getSlotNumber(), "Vehicle " + count + " must be allocated to slot " + s);
            }
        }

        assertEquals(14, fiveFloorService.getActiveTicketCount());

        // 15th vehicle rejected as facility is full
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> fiveFloorService.parkVehicle(new Car("CAR-OVERFLOW", 4), baseTime));
        assertTrue(ex.getMessage().contains("full"));
    }

    @Test
    @DisplayName("Task 12 - Five-Floor Facility: Slot release on any floor and re-allocation into freed slot")
    void testFiveFloorReleaseAndReallocation() {
        int[] fiveFloors = new int[]{4, 3, 3, 2, 2}; // 5 floors, capacities [4, 3, 3, 2, 2] (Total = 14)
        ParkingFacility facility = new ParkingFacility(fiveFloors);
        ParkingManagementService fiveFloorService = new ParkingManagementService(facility, 14);

        // Admit 14 vehicles to completely fill facility
        ParkingTicket[] tickets = new ParkingTicket[14];
        for (int i = 0; i < 14; i++) {
            tickets[i] = fiveFloorService.parkVehicle(new Car("CAR-" + (i + 1), 4), baseTime);
        }

        // Floor 0: tickets 0,1,2,3 (slots 0,1,2,3)
        // Floor 1: tickets 4,5,6 (slots 0,1,2)
        // Floor 2: tickets 7,8,9 (slots 0,1,2) -> ticket 8 is Floor 2, Slot 1
        ParkingTicket floor2Ticket = tickets[8];
        assertEquals(2, floor2Ticket.getFloorNumber());
        assertEquals(1, floor2Ticket.getSlotNumber());

        ParkingExitSummary summary = fiveFloorService.processExit(floor2Ticket.getTicketNumber(), baseTime.plusMinutes(60));
        assertEquals(2, summary.releasedFloor());
        assertEquals(1, summary.releasedSlot());
        assertFalse(facility.isSlotOccupied(2, 1));

        // Admit a new vehicle: must be allocated into the freed slot [2, 1]!
        ParkingTicket newTicket = fiveFloorService.parkVehicle(new Car("CAR-NEW-REALLOC", 4), baseTime.plusMinutes(70));
        assertEquals(2, newTicket.getFloorNumber());
        assertEquals(1, newTicket.getSlotNumber());
    }

    @Test
    @DisplayName("Task 12 - Five-Floor Facility: Defensive coordinate checking on all 5 floors")
    void testFiveFloorFacilityDefensiveCoordinates() {
        // Floor 0 has 5 slots, Floor 4 has 1 slot
        ParkingFacility facility = new ParkingFacility(new int[]{5, 4, 3, 2, 1});

        // Negative floor and out-of-bounds floor
        assertFalse(facility.release(-1, 0));
        assertFalse(facility.release(5, 0));
        assertFalse(facility.release(99, 0));

        // Negative slot and out-of-bounds slot on floor 4 (only 1 slot: index 0)
        assertFalse(facility.release(4, -1));
        assertFalse(facility.release(4, 1)); // slot 1 does not exist on floor 4
        assertFalse(facility.release(4, 99));

        // Non-existent slot count returns -1
        assertEquals(-1, facility.getSlotCount(-1));
        assertEquals(-1, facility.getSlotCount(5));
    }
}
