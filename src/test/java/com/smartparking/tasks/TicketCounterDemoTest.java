package com.smartparking.tasks;

import com.smartparking.model.ParkingTicket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests verifying Task 5 static members and object lifecycle semantics:
 * - Shared static counter across multiple facility gates
 * - Independent instance counters in the alternative design
 * - Object reference pass-by-value mutation and reassignment immunity
 * - Returning object references
 */
class TicketCounterDemoTest {

    @BeforeEach
    void setUp() {
        // Uses package-private test helper to guarantee clean test isolation
        TicketCounterDemo.resetSharedCounterForTesting(1000);
    }

    @Test
    void testSharedStaticCounterAcrossGates() {
        TicketCounterDemo.FacilityGate northGate = new TicketCounterDemo.FacilityGate("North-Gate");
        TicketCounterDemo.FacilityGate southGate = new TicketCounterDemo.FacilityGate("South-Gate");

        ParkingTicket t1 = northGate.issueTicketShared("TN01A1000", "CAR", 0, 1);
        assertEquals("TKT-1001", t1.getTicketNumber());
        assertEquals(1001, TicketCounterDemo.getSharedTicketCounter());

        ParkingTicket t2 = southGate.issueTicketShared("TN02B2000", "TWO_WHEELER", 0, 2);
        assertEquals("TKT-1002", t2.getTicketNumber());
        assertEquals(1002, TicketCounterDemo.getSharedTicketCounter());

        ParkingTicket t3 = northGate.issueTicketShared("TN03C3000", "CAR", 1, 0);
        assertEquals("TKT-1003", t3.getTicketNumber());
        assertEquals(1003, TicketCounterDemo.getSharedTicketCounter());
    }

    @Test
    void testAlternativeInstanceCountersAreIndependent() {
        TicketCounterDemo.FacilityGate northGate = new TicketCounterDemo.FacilityGate("North-Gate");
        TicketCounterDemo.FacilityGate southGate = new TicketCounterDemo.FacilityGate("South-Gate");

        ParkingTicket altNorth1 = northGate.issueTicketInstance("TN01A1", "CAR", 0, 1);
        ParkingTicket altNorth2 = northGate.issueTicketInstance("TN01A2", "CAR", 0, 2);
        ParkingTicket altSouth1 = southGate.issueTicketInstance("TN02B1", "TWO_WHEELER", 0, 1);

        assertEquals("North-Gate-1", altNorth1.getTicketNumber());
        assertEquals("North-Gate-2", altNorth2.getTicketNumber());
        assertEquals("South-Gate-1", altSouth1.getTicketNumber());

        assertEquals(2, northGate.getInstanceTicketCounter());
        assertEquals(1, southGate.getInstanceTicketCounter());
    }

    @Test
    void testPassByValueInPlaceMutation() {
        TicketCounterDemo.FacilityGate gate = new TicketCounterDemo.FacilityGate("Main-Gate");
        ParkingTicket ticket = gate.issueTicketShared("TN01TEST", "CAR", 0, 0);

        assertTrue(ticket.isActive());
        assertNull(ticket.getExitTime());

        LocalDateTime departure = LocalDateTime.now().plusHours(1);
        TicketCounterDemo.completeParkingSession(ticket, departure);

        assertFalse(ticket.isActive(), "Caller's ticket object must be modified in-place");
        assertEquals(departure, ticket.getExitTime());
    }

    @Test
    void testPassByValueReassignmentImmunity() {
        TicketCounterDemo.FacilityGate gate = new TicketCounterDemo.FacilityGate("Main-Gate");
        ParkingTicket original = gate.issueTicketShared("TN01ORIG", "CAR", 0, 0);
        ParkingTicket replacement = new ParkingTicket("TKT-9999", "TN01REPL", "CAR", 1, 1);

        TicketCounterDemo.attemptReferenceReassignment(original, replacement);

        assertEquals("TKT-1001", original.getTicketNumber(), "Caller reference must NOT be changed by reassignment inside method");
        assertEquals("TN01ORIG", original.getVehicleRegNo());
    }

    @Test
    void testReturningObjectReference() {
        TicketCounterDemo.FacilityGate gate = new TicketCounterDemo.FacilityGate("Main-Gate");
        ParkingTicket original = gate.issueTicketShared("TN01TEST", "CAR", 0, 0);

        ParkingTicket archival = TicketCounterDemo.issueArchivalCopy(original);

        assertNotNull(archival);
        assertNotSame(original, archival, "Returned object should be a new distinct reference");
        assertEquals(original.getTicketNumber(), archival.getTicketNumber());
        assertEquals(original.getVehicleRegNo(), archival.getVehicleRegNo());
    }
}
