package com.smartparking.model;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests verifying Task 7 method overriding, specialisation, final keywords,
 * and Object-class method overrides (equals, hashCode, toString).
 */
class VehicleOverrideTest {

    @Test
    void testTwoWheelerFeeCalculation() {
        Vehicle bike = new TwoWheeler("TN01A1", true);

        // 0 minutes -> Rs 0.0
        assertEquals(0.0, bike.calculateFee(0), 0.001);

        // 1 to 60 minutes -> 1 started hour * 10 = Rs 10.0
        assertEquals(10.0, bike.calculateFee(1), 0.001);
        assertEquals(10.0, bike.calculateFee(30), 0.001);
        assertEquals(10.0, bike.calculateFee(60), 0.001);

        // 61 to 120 minutes -> 2 started hours * 10 = Rs 20.0
        assertEquals(20.0, bike.calculateFee(61), 0.001);
        assertEquals(20.0, bike.calculateFee(120), 0.001);

        // 130 minutes -> 3 started hours * 10 = Rs 30.0
        assertEquals(30.0, bike.calculateFee(130), 0.001);
    }

    @Test
    void testCarFeeCalculation() {
        Vehicle car = new Car("TN02B2", 5);

        // 0 minutes -> Rs 0.0
        assertEquals(0.0, car.calculateFee(0), 0.001);

        // 1 to 60 minutes -> 1 started hour * 30 = Rs 30.0
        assertEquals(30.0, car.calculateFee(1), 0.001);
        assertEquals(30.0, car.calculateFee(45), 0.001);
        assertEquals(30.0, car.calculateFee(60), 0.001);

        // 130 minutes -> 3 started hours * 30 = Rs 90.0
        assertEquals(90.0, car.calculateFee(130), 0.001);
    }

    @Test
    void testElectricCarFeeCalculation() {
        Vehicle ev = new ElectricCar("TN03C3", 60.0);

        // 0 minutes -> Rs 0.0
        assertEquals(0.0, ev.calculateFee(0), 0.001);

        // 1 to 60 minutes -> 1 started hour * 30 = Rs 30.0
        assertEquals(30.0, ev.calculateFee(60), 0.001);

        // 130 minutes -> 3 started hours * 30 = Rs 90.0
        assertEquals(90.0, ev.calculateFee(130), 0.001);
    }

    @Test
    void testNegativeDurationRejection() {
        Vehicle bike = new TwoWheeler("TN01A1", true);
        Vehicle car = new Car("TN02B2", 4);
        Vehicle ev = new ElectricCar("TN03C3", 50.0);

        assertThrows(IllegalArgumentException.class, () -> bike.calculateFee(-1),
                "Negative duration must throw IllegalArgumentException");
        assertThrows(IllegalArgumentException.class, () -> car.calculateFee(-30),
                "Negative duration must throw IllegalArgumentException");
        assertThrows(IllegalArgumentException.class, () -> ev.calculateFee(-120),
                "Negative duration must throw IllegalArgumentException");
    }

    @Test
    void testPolymorphicFeeCalculationThroughVehicleArray() {
        Vehicle[] fleet = new Vehicle[] {
            new TwoWheeler("TN01A1", false), // 130 min -> 3 hrs * 10 = 30
            new Car("TN02B2", 5),            // 130 min -> 3 hrs * 30 = 90
            new ElectricCar("TN03C3", 75.0)  // 130 min -> 3 hrs * 30 = 90
        };

        double[] expectedFees = { 30.0, 90.0, 90.0 };
        for (int i = 0; i < fleet.length; i++) {
            assertEquals(expectedFees[i], fleet[i].calculateFee(130), 0.001,
                    "Polymorphic dispatch failed for " + fleet[i].getClass().getSimpleName());
        }
    }

    @Test
    void testToStringFormatting() {
        Vehicle bike = new TwoWheeler("TN01A1", true);
        Vehicle car = new Car("TN02B2", 5);
        Vehicle ev = new ElectricCar("TN03C3", 60.0, 30.0);

        assertTrue(bike.toString().contains("TwoWheeler"), "Must contain class name");
        assertTrue(bike.toString().contains("TN01A1"), "Must contain registration number");
        assertTrue(bike.toString().contains("helmetStorage=true"), "Must contain subclass detail");

        assertTrue(car.toString().contains("Car"));
        assertTrue(car.toString().contains("TN02B2"));
        assertTrue(car.toString().contains("seats=5"));

        assertTrue(ev.toString().contains("ElectricCar"));
        assertTrue(ev.toString().contains("TN03C3"));
        assertTrue(ev.toString().contains("battery=30.0/60.0kWh"));
    }

    @Test
    void testEqualsReflexiveSymmetricTransitive() {
        Vehicle v1 = new Car("TN02B2", 4);
        Vehicle v2 = new Car("TN02B2", 5); // same reg, different seats
        Vehicle v3 = new ElectricCar("TN02B2", 60.0); // same reg, different subclass

        // Reflexive: x.equals(x) is true
        assertTrue(v1.equals(v1));

        // Symmetric: x.equals(y) == y.equals(x)
        assertTrue(v1.equals(v2));
        assertTrue(v2.equals(v1));
        assertTrue(v1.equals(v3));
        assertTrue(v3.equals(v1));

        // Transitive: x.equals(y) and y.equals(z) => x.equals(z)
        assertTrue(v2.equals(v3));

        // Non-nullity: x.equals(null) is false
        assertFalse(v1.equals(null));

        // Unrelated type comparison is false
        assertFalse(v1.equals("TN02B2"));
        assertFalse(v1.equals(12345));
    }

    @Test
    void testEqualsWithDifferentRegistrationNumbers() {
        Vehicle car1 = new Car("TN01AA1111", 5);
        Vehicle car2 = new Car("TN01BB2222", 5);

        assertFalse(car1.equals(car2), "Different registration numbers must not be equal");
    }

    @Test
    void testHashCodeConsistency() {
        Vehicle car1 = new Car("TN02B2", 4);
        Vehicle car2 = new Car("TN02B2", 7);
        Vehicle ev = new ElectricCar("TN02B2", 80.0);

        assertEquals(car1.hashCode(), car2.hashCode(), "Equal objects must have identical hash codes");
        assertEquals(car1.hashCode(), ev.hashCode(), "Cross-subclass equal objects must have identical hash codes");
    }

    @Test
    void testHashSetDeduplication() {
        Set<Vehicle> set = new HashSet<>();
        set.add(new Car("TN02B2", 4));
        set.add(new Car("TN02B2", 6));
        set.add(new ElectricCar("TN02B2", 50.0));
        set.add(new TwoWheeler("TN99ZZ9999", true));

        // There are only 2 distinct registration numbers ("TN02B2" and "TN99ZZ9999")
        assertEquals(2, set.size(), "Set should deduplicate objects sharing the same registration number");
    }

    @Test
    void testFinalDesignVerifications() throws Exception {
        // 1. Verify getRegistrationNumber() is declared final in Vehicle
        assertTrue(Modifier.isFinal(Vehicle.class.getMethod("getRegistrationNumber").getModifiers()),
                "getRegistrationNumber() should be declared final");

        // 2. Verify getVehicleType() is declared final in Vehicle
        assertTrue(Modifier.isFinal(Vehicle.class.getMethod("getVehicleType").getModifiers()),
                "getVehicleType() should be declared final");

        // 3. Verify ParkingTariff class is declared final
        assertTrue(Modifier.isFinal(ParkingTariff.class.getModifiers()),
                "ParkingTariff utility class should be declared final");

        // 4. Verify ParkingTariff constructor is private and non-instantiable
        Constructor<ParkingTariff> constructor = ParkingTariff.class.getDeclaredConstructor();
        assertTrue(Modifier.isPrivate(constructor.getModifiers()),
                "ParkingTariff constructor must be private");
        constructor.setAccessible(true);
        assertThrows(InvocationTargetException.class, constructor::newInstance,
                "Instantiating ParkingTariff via reflection must throw InvocationTargetException wrapping UnsupportedOperationException");
    }
}

