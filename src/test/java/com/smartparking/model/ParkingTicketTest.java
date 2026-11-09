package com.smartparking.model;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests verifying Task 4 constructor concepts on ParkingTicket:
 * - Parameterized canonical construction
 * - Overloaded constructors with constructor chaining (this(...))
 * - Input validation
 * - Archival copy constructor and Reissue copy constructor
 * - Lifecycle state changes
 */
class ParkingTicketTest {

    @Test
    void testCanonicalConstructorValidData() {
        LocalDateTime entry = LocalDateTime.of(2026, 11, 9, 10, 0);
        ParkingTicket ticket = new ParkingTicket("TKT-001", "tn01ab1234", "car", 0, 1, entry);

        assertEquals("TKT-001", ticket.getTicketNumber());
        assertEquals("TN01AB1234", ticket.getVehicleRegNo(), "RegNo should be upper-cased");
        assertEquals("CAR", ticket.getVehicleType(), "VehicleType should be upper-cased");
        assertEquals(0, ticket.getFloorNumber());
        assertEquals(1, ticket.getSlotNumber());
        assertEquals(entry, ticket.getEntryTime());
        assertNull(ticket.getExitTime());
        assertTrue(ticket.isActive());
    }

    @Test
    void testOverloadedConstructorChaining() {
        ParkingTicket ticket = new ParkingTicket("TKT-002", "TN02CD5678", "TWO_WHEELER", 1, 3);

        assertEquals("TKT-002", ticket.getTicketNumber());
        assertEquals("TN02CD5678", ticket.getVehicleRegNo());
        assertEquals("TWO_WHEELER", ticket.getVehicleType());
        assertEquals(1, ticket.getFloorNumber());
        assertEquals(3, ticket.getSlotNumber());
        assertNotNull(ticket.getEntryTime(), "Entry time should be defaulted via chaining");
        assertTrue(ticket.isActive());
    }

    @Test
    void testOverloadedConstructorDefaultingVehicleType() {
        ParkingTicket ticket = new ParkingTicket("TKT-003", "TN03EF9999", 2, 5);

        assertEquals("TKT-003", ticket.getTicketNumber());
        assertEquals("TN03EF9999", ticket.getVehicleRegNo());
        assertEquals("CAR", ticket.getVehicleType(), "Default vehicle type should be CAR");
        assertEquals(2, ticket.getFloorNumber());
        assertEquals(5, ticket.getSlotNumber());
        assertTrue(ticket.isActive());
    }

    @Test
    void testConstructorValidationRejectsInvalidInputs() {
        LocalDateTime now = LocalDateTime.now();

        assertThrows(IllegalArgumentException.class, () ->
                new ParkingTicket(null, "TN01A1", "CAR", 0, 0, now), "Null ticket number must be rejected");

        assertThrows(IllegalArgumentException.class, () ->
                new ParkingTicket("   ", "TN01A1", "CAR", 0, 0, now), "Blank ticket number must be rejected");

        assertThrows(IllegalArgumentException.class, () ->
                new ParkingTicket("TKT-1", null, "CAR", 0, 0, now), "Null regNo must be rejected");

        assertThrows(IllegalArgumentException.class, () ->
                new ParkingTicket("TKT-1", "TN01A1", null, 0, 0, now), "Null type must be rejected");

        assertThrows(IllegalArgumentException.class, () ->
                new ParkingTicket("TKT-1", "TN01A1", "CAR", -1, 0, now), "Negative floor must be rejected");

        assertThrows(IllegalArgumentException.class, () ->
                new ParkingTicket("TKT-1", "TN01A1", "CAR", 0, -1, now), "Negative slot must be rejected");

        assertThrows(IllegalArgumentException.class, () ->
                new ParkingTicket("TKT-1", "TN01A1", "CAR", 0, 0, null), "Null entry time must be rejected");
    }

    @Test
    void testArchivalCopyConstructor() {
        LocalDateTime entry = LocalDateTime.of(2026, 11, 9, 8, 30);
        LocalDateTime exit = LocalDateTime.of(2026, 11, 9, 10, 45);

        ParkingTicket original = new ParkingTicket("TKT-100", "TN10XY1010", "CAR", 1, 2, entry);
        original.closeTicket(exit);

        ParkingTicket copy = new ParkingTicket(original);

        assertNotSame(original, copy, "Copy must be a distinct object reference on the heap");
        assertEquals(original.getTicketNumber(), copy.getTicketNumber(), "Snapshot preserves ticket ID");
        assertEquals(original.getVehicleRegNo(), copy.getVehicleRegNo());
        assertEquals(original.getEntryTime(), copy.getEntryTime());
        assertEquals(original.getExitTime(), copy.getExitTime());
        assertFalse(copy.isActive());
        assertEquals(135, copy.getDurationMinutes());
    }

    @Test
    void testReissueCopyConstructorDistinctIdentity() {
        LocalDateTime entry = LocalDateTime.of(2026, 11, 9, 9, 0);
        ParkingTicket original = new ParkingTicket("TKT-100", "TN10XY1010", "CAR", 1, 2, entry);

        // Reissue lost ticket with a new unique ticket ID
        ParkingTicket reissued = new ParkingTicket("TKT-999-REISSUE", original);

        assertNotEquals(original.getTicketNumber(), reissued.getTicketNumber(),
                "Reissued ticket must have a new unique ticket ID to prevent identity collision");
        assertEquals("TKT-999-REISSUE", reissued.getTicketNumber());
        assertEquals(original.getVehicleRegNo(), reissued.getVehicleRegNo());
        assertEquals(original.getEntryTime(), reissued.getEntryTime());
        assertEquals(original.getFloorNumber(), reissued.getFloorNumber());
        assertEquals(original.getSlotNumber(), reissued.getSlotNumber());
        assertTrue(reissued.isActive());
    }

    @Test
    void testCloseTicketValidatesChronology() {
        LocalDateTime entry = LocalDateTime.of(2026, 11, 9, 14, 0);
        ParkingTicket ticket = new ParkingTicket("TKT-005", "TN05ZZ5555", "CAR", 0, 0, entry);

        LocalDateTime priorTime = LocalDateTime.of(2026, 11, 9, 13, 0);
        assertThrows(IllegalArgumentException.class, () -> ticket.closeTicket(priorTime),
                "Exit time cannot precede entry time");

        LocalDateTime validExit = LocalDateTime.of(2026, 11, 9, 15, 30);
        ticket.closeTicket(validExit);
        assertFalse(ticket.isActive());
        assertEquals(90, ticket.getDurationMinutes());
    }
}

