package com.smartparking.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit Tests for Task 12 Model Entities:
 * - {@link ElectricBike}
 * - {@link DeliveryVan}
 * - {@link MonthlyPass}
 * - {@link VisitorOneDayPass}
 */
class Task12ModelTest {

    private final LocalDateTime baseDate = LocalDateTime.of(2026, 11, 10, 0, 0);

    // =========================================================================
    // ElectricBike Tests
    // =========================================================================

    @Test
    @DisplayName("ElectricBike: Flat fee applies regardless of positive stay duration")
    void testElectricBikeFlatFeeCalculation() {
        ElectricBike bike = new ElectricBike("EB-1001");
        assertEquals(ParkingTariff.FLAT_EBIKE_FEE, bike.getFlatFee(), 0.001);
        assertEquals("ELECTRIC_BIKE", bike.getVehicleType());

        // 10 min, 60 min, 120 min, 300 min all cost flat Rs 15.0
        assertEquals(15.0, bike.calculateFee(10), 0.001);
        assertEquals(15.0, bike.calculateFee(60), 0.001);
        assertEquals(15.0, bike.calculateFee(120), 0.001);
        assertEquals(15.0, bike.calculateFee(300), 0.001);

        // Timestamps overload
        LocalDateTime entry = LocalDateTime.of(2026, 11, 10, 8, 0);
        assertEquals(15.0, bike.calculateFee(entry, entry.plusMinutes(75)), 0.001);
    }

    @Test
    @DisplayName("ElectricBike: Zero duration costs Rs 0.0 and negative duration throws exception")
    void testElectricBikeZeroAndBoundaryDuration() {
        ElectricBike bike = new ElectricBike("EB-1002", 20.0);
        assertEquals(20.0, bike.getFlatFee(), 0.001);

        // Zero duration is free under system policy
        assertEquals(0.0, bike.calculateFee(0), 0.001);
        LocalDateTime entry = LocalDateTime.of(2026, 11, 10, 8, 0);
        assertEquals(0.0, bike.calculateFee(entry, entry), 0.001);

        // Negative duration throws IllegalArgumentException
        assertThrows(IllegalArgumentException.class, () -> bike.calculateFee(-1));
        assertThrows(IllegalArgumentException.class, () -> bike.calculateFee(entry, entry.minusMinutes(10)));
    }

    @Test
    @DisplayName("ElectricBike: Invalid constructor arguments are rejected")
    void testElectricBikeInvalidConstructorInputs() {
        assertThrows(IllegalArgumentException.class, () -> new ElectricBike(null));
        assertThrows(IllegalArgumentException.class, () -> new ElectricBike("   "));
        assertThrows(IllegalArgumentException.class, () -> new ElectricBike("EB-01", -5.0));
        assertThrows(IllegalArgumentException.class, () -> new ElectricBike("EB-01", Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> new ElectricBike("EB-01", Double.POSITIVE_INFINITY));
    }

    @Test
    @DisplayName("ElectricBike: Is not Chargeable and does not implement EV charging")
    void testElectricBikeIsNotChargeable() {
        ElectricBike bike = new ElectricBike("EB-1003");
        assertFalse(bike instanceof Chargeable, "ElectricBike must not implement Chargeable");
    }

    // =========================================================================
    // DeliveryVan Tests
    // =========================================================================

    @Test
    @DisplayName("DeliveryVan: Entry before 9:00 AM is billed at off-peak car rate (Rs 30/hr)")
    void testDeliveryVanBefore9AMOffPeak() {
        DeliveryVan van = new DeliveryVan("VAN-01", "DHL");
        assertEquals("DELIVERY_VAN", van.getVehicleType());
        assertEquals("DHL", van.getDeliveryCompany());

        LocalDateTime entry = LocalDateTime.of(2026, 11, 10, 7, 0);
        LocalDateTime exit = LocalDateTime.of(2026, 11, 10, 8, 30); // 90 min = 2 started hours @ Rs 30 = Rs 60

        assertEquals(60.0, van.calculateFee(entry, exit), 0.001);
    }

    @Test
    @DisplayName("DeliveryVan: Entry exactly at 9:00 AM is billed at peak surge rate (Rs 60/hr)")
    void testDeliveryVanExactlyAt9AMPeak() {
        DeliveryVan van = new DeliveryVan("VAN-02");
        LocalDateTime entry = LocalDateTime.of(2026, 11, 10, 9, 0);
        LocalDateTime exit = LocalDateTime.of(2026, 11, 10, 11, 0); // 120 min = 2 started hours @ Rs 60 = Rs 120

        assertEquals(120.0, van.calculateFee(entry, exit), 0.001);
    }

    @Test
    @DisplayName("DeliveryVan: Stay during 9:00 AM - 5:00 PM is billed at peak surge rate (Rs 60/hr)")
    void testDeliveryVanDuringPeakHours() {
        DeliveryVan van = new DeliveryVan("VAN-03");
        LocalDateTime entry = LocalDateTime.of(2026, 11, 10, 10, 0);
        LocalDateTime exit = LocalDateTime.of(2026, 11, 10, 12, 30); // 150 min = 3 started hours @ Rs 60 = Rs 180

        assertEquals(180.0, van.calculateFee(entry, exit), 0.001);
    }

    @Test
    @DisplayName("DeliveryVan: Entry exactly at 5:00 PM (17:00) is billed at off-peak rate (Rs 30/hr)")
    void testDeliveryVanExactlyAt5PMOffPeak() {
        DeliveryVan van = new DeliveryVan("VAN-04");
        LocalDateTime entry = LocalDateTime.of(2026, 11, 10, 17, 0);
        LocalDateTime exit = LocalDateTime.of(2026, 11, 10, 18, 0); // 60 min = 1 started hour @ Rs 30 = Rs 30

        assertEquals(30.0, van.calculateFee(entry, exit), 0.001);
    }

    @Test
    @DisplayName("DeliveryVan: Entry after 5:00 PM is billed at off-peak rate (Rs 30/hr)")
    void testDeliveryVanAfter5PMOffPeak() {
        DeliveryVan van = new DeliveryVan("VAN-05");
        LocalDateTime entry = LocalDateTime.of(2026, 11, 10, 18, 0);
        LocalDateTime exit = LocalDateTime.of(2026, 11, 10, 20, 0); // 120 min = 2 started hours @ Rs 30 = Rs 60

        assertEquals(60.0, van.calculateFee(entry, exit), 0.001);
    }

    @Test
    @DisplayName("DeliveryVan: Stay spanning 9:00 AM window start is segmented into off-peak and peak started hours")
    void testDeliveryVanSpanningWindowStart() {
        DeliveryVan van = new DeliveryVan("VAN-06");
        // 08:00 to 10:00 (120 min total):
        // 08:00 to 09:00 = 60 min off-peak (1 hr @ Rs 30 = Rs 30)
        // 09:00 to 10:00 = 60 min peak (1 hr @ Rs 60 = Rs 60)
        // Total = Rs 90.0
        LocalDateTime entry = LocalDateTime.of(2026, 11, 10, 8, 0);
        LocalDateTime exit = LocalDateTime.of(2026, 11, 10, 10, 0);

        assertEquals(90.0, van.calculateFee(entry, exit), 0.001);
    }

    @Test
    @DisplayName("DeliveryVan: Stay spanning 5:00 PM window end is segmented into peak and off-peak started hours")
    void testDeliveryVanSpanningWindowEnd() {
        DeliveryVan van = new DeliveryVan("VAN-07");
        // 16:00 to 18:00 (120 min total):
        // 16:00 to 17:00 = 60 min peak (1 hr @ Rs 60 = Rs 60)
        // 17:00 to 18:00 = 60 min off-peak (1 hr @ Rs 30 = Rs 30)
        // Total = Rs 90.0
        LocalDateTime entry = LocalDateTime.of(2026, 11, 10, 16, 0);
        LocalDateTime exit = LocalDateTime.of(2026, 11, 10, 18, 0);

        assertEquals(90.0, van.calculateFee(entry, exit), 0.001);
    }

    @Test
    @DisplayName("DeliveryVan: Fallback duration-only calculateFee uses off-peak baseline")
    void testDeliveryVanFallbackDurationOnly() {
        DeliveryVan van = new DeliveryVan("VAN-08");
        assertEquals(0.0, van.calculateFee(0), 0.001);
        assertEquals(30.0, van.calculateFee(45), 0.001);
        assertEquals(60.0, van.calculateFee(120), 0.001);
        assertThrows(IllegalArgumentException.class, () -> van.calculateFee(-10));
    }

    @Test
    @DisplayName("DeliveryVan: Overnight stay crossing midnight entirely off-peak (23:00 to 02:00)")
    void testDeliveryVanOvernightMidnightCrossingStay() {
        DeliveryVan van = new DeliveryVan("VAN-MIDNIGHT-1");
        LocalDateTime entry = LocalDateTime.of(2026, 11, 10, 23, 0);
        LocalDateTime exit = LocalDateTime.of(2026, 11, 11, 2, 0); // 180 min total = 3 started hours off-peak

        assertEquals(0, DeliveryVan.calculatePeakMinutes(entry, exit));
        assertEquals(90.0, van.calculateFee(entry, exit), 0.001);
    }

    @Test
    @DisplayName("DeliveryVan: Multi-day stay across two days correctly computes non-overlapping peak and off-peak started hours")
    void testDeliveryVanMultiDayStay() {
        DeliveryVan van = new DeliveryVan("VAN-MULTIDAY-1");
        // 2026-11-10 08:00 to 2026-11-11 10:00 (26 hours = 1,560 minutes)
        // Day 1: 08:00-09:00 (60 off-peak), 09:00-17:00 (480 peak), 17:00-24:00 (420 off-peak)
        // Day 2: 00:00-09:00 (540 off-peak), 09:00-10:00 (60 peak)
        // Total peak = 540 min = 9 hr @ Rs 60 = Rs 540.0
        // Total off-peak = 1,020 min = 17 hr @ Rs 30 = Rs 510.0
        // Total fee = Rs 1,050.00
        LocalDateTime entry = LocalDateTime.of(2026, 11, 10, 8, 0);
        LocalDateTime exit = LocalDateTime.of(2026, 11, 11, 10, 0);

        assertEquals(540, DeliveryVan.calculatePeakMinutes(entry, exit));
        assertEquals(1050.0, van.calculateFee(entry, exit), 0.001);
    }

    @Test
    @DisplayName("DeliveryVan: Disconnected off-peak intervals (08:30-09:00 and 17:00-17:30) aggregate to 60 min (1 started hr)")
    void testDeliveryVanAggregatedDisconnectedOffPeakIntervals() {
        DeliveryVan van = new DeliveryVan("VAN-AGGREGATE-1");
        // Entry: 2026-11-10 08:30, Exit: 2026-11-10 17:30
        // Peak window: 09:00 to 17:00 = 480 minutes (8 started hours @ Rs 60 = Rs 480)
        // Disconnected off-peak: 08:30 to 09:00 (30 min) + 17:00 to 17:30 (30 min) = 60 minutes aggregate (1 started hour @ Rs 30 = Rs 30)
        // Expected total: Rs 480 + Rs 30 = Rs 510
        LocalDateTime entry = LocalDateTime.of(2026, 11, 10, 8, 30);
        LocalDateTime exit = LocalDateTime.of(2026, 11, 10, 17, 30);

        long peakMinutes = DeliveryVan.calculatePeakMinutes(entry, exit);
        assertEquals(480, peakMinutes, "Peak duration must be exactly 480 minutes (09:00 to 17:00)");

        long totalDurationMinutes = java.time.Duration.between(entry, exit).toMinutes();
        assertEquals(540, totalDurationMinutes, "Total stay duration must be 540 minutes");

        long offPeakMinutes = totalDurationMinutes - peakMinutes;
        assertEquals(60, offPeakMinutes, "Aggregate off-peak duration must be exactly 60 minutes");

        assertEquals(510.0, van.calculateFee(entry, exit), 0.001,
                "Fee must be Rs 510.0 (Rs 480 peak + Rs 30 off-peak) under category-aggregation policy");
    }

    // =========================================================================
    // VisitorOneDayPass Tests
    // =========================================================================

    @Test
    @DisplayName("VisitorOneDayPass: Valid on calendar day of issue and waives parking fee")
    void testVisitorOneDayPassValidOnDate() {
        LocalDate passDate = LocalDate.of(2026, 11, 10);
        VisitorOneDayPass pass = new VisitorOneDayPass("VP-101", "John Doe", "KA01V1", passDate);

        assertEquals("VP-101", pass.getPassId());
        assertEquals("JOHN DOE", pass.getHolderId());
        assertEquals("KA01V1", pass.getVehicleRegNo());
        assertEquals("VISITOR_ONE_DAY_PASS", pass.getPassType());
        assertTrue(pass.isParkingFeeWaived());

        // Valid times during the day
        assertTrue(pass.isValidAt(LocalDateTime.of(2026, 11, 10, 0, 0, 0)));
        assertTrue(pass.isValidAt(LocalDateTime.of(2026, 11, 10, 12, 30, 0)));
        assertTrue(pass.isValidAt(LocalDateTime.of(2026, 11, 10, 23, 59, 59)));

        // Expired before or after
        assertFalse(pass.isValidAt(LocalDateTime.of(2026, 11, 9, 23, 59, 59)));
        assertFalse(pass.isValidAt(LocalDateTime.of(2026, 11, 11, 0, 0, 0)));
        assertFalse(pass.isValidAt(null));
    }

    @Test
    @DisplayName("VisitorOneDayPass: Precise window constructor enforces validFrom < validTo")
    void testVisitorOneDayPassPreciseWindow() {
        LocalDateTime from = LocalDateTime.of(2026, 11, 10, 10, 0);
        LocalDateTime to = LocalDateTime.of(2026, 11, 11, 10, 0);
        VisitorOneDayPass pass = new VisitorOneDayPass("VP-102", "Jane Visitor", "TN09V2", from, to);

        assertTrue(pass.isValidAt(from));
        assertTrue(pass.isValidAt(from.plusHours(12)));
        assertTrue(pass.isValidAt(to));
        assertFalse(pass.isValidAt(from.minusSeconds(1)));
        assertFalse(pass.isValidAt(to.plusSeconds(1)));

        // Inverted dates throw exception
        assertThrows(IllegalArgumentException.class,
                () -> new VisitorOneDayPass("VP-103", "Jane", "TN09V2", to, from));
    }

    @Test
    @DisplayName("VisitorOneDayPass: Invalid constructor inputs throw IllegalArgumentException")
    void testVisitorOneDayPassInvalidInputs() {
        LocalDate date = LocalDate.of(2026, 11, 10);
        assertThrows(IllegalArgumentException.class, () -> new VisitorOneDayPass(null, "Visitor", "REG-1", date));
        assertThrows(IllegalArgumentException.class, () -> new VisitorOneDayPass("P1", null, "REG-1", date));
        assertThrows(IllegalArgumentException.class, () -> new VisitorOneDayPass("P1", "Visitor", null, date));
        assertThrows(IllegalArgumentException.class, () -> new VisitorOneDayPass("P1", "Visitor", "REG-1", (LocalDate) null));
    }

    // =========================================================================
    // MonthlyPass Tests
    // =========================================================================

    @Test
    @DisplayName("MonthlyPass: Valid for the entire calendar month for staff and students")
    void testMonthlyPassValidity() {
        YearMonth month = YearMonth.of(2026, 11);
        MonthlyPass staffPass = new MonthlyPass("MP-STAFF-1", "EMP-901", "STAFF", "KA05ST1", month);
        MonthlyPass studentPass = new MonthlyPass("MP-STU-1", "STU-555", "STUDENT", "KA05ST2", month);

        assertEquals("STAFF", staffPass.getUserRole());
        assertEquals("STUDENT", studentPass.getUserRole());
        assertTrue(staffPass.isParkingFeeWaived());

        // Valid inside November 2026
        assertTrue(staffPass.isValidAt(LocalDateTime.of(2026, 11, 1, 0, 0)));
        assertTrue(staffPass.isValidAt(LocalDateTime.of(2026, 11, 15, 12, 0)));
        assertTrue(staffPass.isValidAt(LocalDateTime.of(2026, 11, 30, 23, 59)));

        // Invalid outside November 2026
        assertFalse(staffPass.isValidAt(LocalDateTime.of(2026, 10, 31, 23, 59)));
        assertFalse(staffPass.isValidAt(LocalDateTime.of(2026, 12, 1, 0, 0)));

        // Invalid role rejected
        assertThrows(IllegalArgumentException.class,
                () -> new MonthlyPass("MP-X", "USER", "VISITOR", "KA01A1", month));
    }
}
