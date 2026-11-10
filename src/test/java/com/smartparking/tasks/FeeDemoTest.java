package com.smartparking.tasks;

import com.smartparking.model.Car;
import com.smartparking.model.ElectricCar;
import com.smartparking.model.TwoWheeler;
import com.smartparking.model.Vehicle;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests verifying Task 8 polymorphism requirements:
 * 1. Method overloading across supported parameter types.
 * 2. Polymorphic upcasting in a Vehicle[] array.
 * 3. Runtime dynamic method dispatch.
 * 4. Correctness of expected fee calculations.
 * 5. Type identification via instanceof.
 * 6. Safe downcasting and subtype feature access.
 * 7. Cross-subclass equality contract compliance.
 * 8. Exact console output verification of FeeDemo.main().
 */
class FeeDemoTest {

    @Test
    @DisplayName("Overloaded calculateFee(Vehicle, long) computes correct fee")
    void testOverloadedCalculateFeeWithMinutes() {
        Vehicle bike = new TwoWheeler("TN01A1", false);
        Vehicle car = new Car("TN02B2", 5);
        Vehicle ev = new ElectricCar("TN03C3", 60.0);

        assertEquals(30.0, FeeDemo.calculateFee(bike, 130L), 0.001);
        assertEquals(90.0, FeeDemo.calculateFee(car, 130L), 0.001);
        assertEquals(210.0, FeeDemo.calculateFee(ev, 420L), 0.001);

        assertThrows(NullPointerException.class, () -> FeeDemo.calculateFee(null, 60L));
        assertThrows(IllegalArgumentException.class, () -> FeeDemo.calculateFee(car, -10L));
    }

    @Test
    @DisplayName("Overloaded calculateFee(Vehicle, LocalTime, LocalTime) computes correct fee")
    void testOverloadedCalculateFeeWithLocalTime() {
        Vehicle bike = new TwoWheeler("TN01A1", true);
        Vehicle car = new Car("TN02B2", 4);

        LocalTime entry = LocalTime.of(10, 0);
        LocalTime exit = LocalTime.of(12, 10); // 130 minutes -> 3 started hours

        assertEquals(30.0, FeeDemo.calculateFee(bike, entry, exit), 0.001);
        assertEquals(90.0, FeeDemo.calculateFee(car, entry, exit), 0.001);

        // Overnight stay spanning midnight (23:00 to 01:10 = 130 minutes)
        LocalTime lateEntry = LocalTime.of(23, 0);
        LocalTime earlyExit = LocalTime.of(1, 10);
        assertEquals(30.0, FeeDemo.calculateFee(bike, lateEntry, earlyExit), 0.001);

        assertThrows(NullPointerException.class, () -> FeeDemo.calculateFee(bike, null, exit));
        assertThrows(NullPointerException.class, () -> FeeDemo.calculateFee(bike, entry, null));
    }

    @Test
    @DisplayName("Overloaded calculateFee(Vehicle, int) computes correct fee for whole hours")
    void testOverloadedCalculateFeeWithHours() {
        Vehicle bike = new TwoWheeler("TN01A1", false);
        Vehicle car = new Car("TN02B2", 5);
        Vehicle ev = new ElectricCar("TN03C3", 60.0);

        assertEquals(30.0, FeeDemo.calculateFee(bike, 3), 0.001);
        assertEquals(90.0, FeeDemo.calculateFee(car, 3), 0.001);
        assertEquals(210.0, FeeDemo.calculateFee(ev, 7), 0.001);

        assertThrows(IllegalArgumentException.class, () -> FeeDemo.calculateFee(car, -1));
    }

    @Test
    @DisplayName("Overloaded calculateFee(Vehicle, LocalDateTime, LocalDateTime) computes correct fee")
    void testOverloadedCalculateFeeWithLocalDateTime() {
        Vehicle car = new Car("TN02B2", 5);

        LocalDateTime entry = LocalDateTime.of(2026, 11, 10, 9, 0);
        LocalDateTime exit = LocalDateTime.of(2026, 11, 10, 11, 10); // 130 minutes -> 3 hours

        assertEquals(90.0, FeeDemo.calculateFee(car, entry, exit), 0.001);

        LocalDateTime invalidExit = LocalDateTime.of(2026, 11, 10, 8, 0);
        assertThrows(IllegalArgumentException.class, () -> FeeDemo.calculateFee(car, entry, invalidExit));
    }

    @Test
    @DisplayName("Vehicle[] array successfully upcasts TwoWheeler, Car, and ElectricCar")
    void testVehicleArrayPolymorphicUpcasting() {
        Vehicle[] parked = {
            new TwoWheeler("TN01A1", false),
            new Car("TN02B2", 5),
            new ElectricCar("TN03C3", 60.0)
        };

        assertEquals(3, parked.length);
        for (Vehicle v : parked) {
            assertNotNull(v);
            assertInstanceOf(Vehicle.class, v, "Each element must be an instance of Vehicle (upcasting)");
        }

        assertInstanceOf(TwoWheeler.class, parked[0]);
        assertInstanceOf(Car.class, parked[1]);
        assertInstanceOf(ElectricCar.class, parked[2]);
    }

    @Test
    @DisplayName("Dynamic method dispatch invokes correct subclass calculateFee at runtime")
    void testRuntimeDynamicDispatch() {
        Vehicle bike = new TwoWheeler("TN01A1", false);
        Vehicle car = new Car("TN02B2", 5);
        Vehicle ev = new ElectricCar("TN03C3", 60.0);

        // 60 minutes = 1 started hour
        assertEquals(10.0, bike.calculateFee(60L), "TwoWheeler dynamic dispatch rate is Rs 10/hr");
        assertEquals(30.0, car.calculateFee(60L), "Car dynamic dispatch rate is Rs 30/hr");
        assertEquals(30.0, ev.calculateFee(60L), "ElectricCar dynamic dispatch base rate is Rs 30/hr");

        // 61 minutes = 2 started hours
        assertEquals(20.0, bike.calculateFee(61L));
        assertEquals(60.0, car.calculateFee(61L));
        assertEquals(60.0, ev.calculateFee(61L));
    }

    @Test
    @DisplayName("Fee calculations match the exact expected assignment values")
    void testExactExpectedFeeValues() {
        Vehicle bike = new TwoWheeler("TN01A1", false);
        Vehicle car = new Car("TN02B2", 5);
        Vehicle ev = new ElectricCar("TN03C3", 60.0);

        // Durations matching expected amounts
        assertEquals(30.0, bike.calculateFee(130L), 0.001);
        assertEquals(90.0, car.calculateFee(130L), 0.001);
        assertEquals(210.0, ev.calculateFee(420L), 0.001);
    }

    @Test
    @DisplayName("instanceof correctly identifies runtime vehicle types")
    void testInstanceOfTypeIdentification() {
        Vehicle bike = new TwoWheeler("TN01A1", true);
        Vehicle car = new Car("TN02B2", 5);
        Vehicle ev = new ElectricCar("TN03C3", 60.0);

        assertTrue(bike instanceof TwoWheeler);
        assertFalse(bike instanceof Car);
        assertFalse(bike instanceof ElectricCar);

        assertTrue(car instanceof Car);
        assertFalse(car instanceof TwoWheeler);
        assertFalse(car instanceof ElectricCar);

        assertTrue(ev instanceof ElectricCar);
        assertFalse(ev instanceof TwoWheeler);
        assertFalse(ev instanceof Car);

        assertTrue(bike instanceof Vehicle);
        assertTrue(car instanceof Vehicle);
        assertTrue(ev instanceof Vehicle);
    }

    @Test
    @DisplayName("Safe downcasting accesses subclass-specific members safely")
    void testSafeDowncasting() {
        Vehicle v1 = new TwoWheeler("TN01A1", true);
        Vehicle v2 = new Car("TN02B2", 7);
        Vehicle v3 = new ElectricCar("TN03C3", 75.0);

        if (v1 instanceof TwoWheeler tw) {
            assertTrue(tw.hasHelmetStorage());
        } else {
            throw new AssertionError("v1 should be TwoWheeler");
        }

        if (v2 instanceof Car c) {
            assertEquals(7, c.getSeatingCapacity());
        } else {
            throw new AssertionError("v2 should be Car");
        }

        if (v3 instanceof ElectricCar ec) {
            assertEquals(75.0, ec.getBatteryCapacityKWh(), 0.001);
            assertFalse(ec.isCharging());
        } else {
            throw new AssertionError("v3 should be ElectricCar");
        }

        // Subclass inspection helper
        assertTrue(FeeDemo.getSubclassSpecificFeature(v1).contains("TwoWheeler"));
        assertTrue(FeeDemo.getSubclassSpecificFeature(v2).contains("Car"));
        assertTrue(FeeDemo.getSubclassSpecificFeature(v3).contains("ElectricCar"));
        assertEquals("Null reference", FeeDemo.getSubclassSpecificFeature(null));

        // Negative check: casting without instanceof would fail
        assertThrows(ClassCastException.class, () -> {
            Car invalidCast = (Car) v1;
        });
    }

    @Test
    @DisplayName("Cross-subclass vehicle equality contract is preserved")
    void testVehicleCrossSubclassEquality() {
        Vehicle car = new Car("TN02B2", 5);
        Vehicle evSameReg = new ElectricCar("TN02B2", 60.0);
        Vehicle carDiffReg = new Car("TN99ZZ9999", 5);

        // Symmetric cross-subclass equality based on registration number
        assertTrue(car.equals(evSameReg), "Vehicles with identical registration numbers must be equal");
        assertTrue(evSameReg.equals(car), "Equality must be symmetric across subclasses");
        assertEquals(car.hashCode(), evSameReg.hashCode(), "Equal objects must have identical hash codes");

        // Different registration numbers must evaluate to false
        assertFalse(car.equals(carDiffReg));
        assertFalse(evSameReg.equals(carDiffReg));
    }

    @Test
    @DisplayName("FeeDemo.main() prints the exact expected four lines of output")
    void testFeeDemoMainOutput() {
        PrintStream originalOut = System.out;
        ByteArrayOutputStream captured = new ByteArrayOutputStream();

        try {
            System.setOut(new PrintStream(captured, true, StandardCharsets.UTF_8));
            FeeDemo.main(new String[0]);
        } finally {
            System.setOut(originalOut);
        }

        String output = captured.toString(StandardCharsets.UTF_8).replace("\r\n", "\n").trim();
        String expected = "TwoWheeler TN01A1 -> Rs 30.0\n"
                        + "Car TN02B2 -> Rs 90.0\n"
                        + "ElectricCar TN03C3 -> Rs 210.0\n"
                        + "true";

        assertEquals(expected, output, "FeeDemo.main() output must match assignment specification exactly");
    }
}

