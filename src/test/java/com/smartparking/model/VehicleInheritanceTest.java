package com.smartparking.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests verifying Task 6 inheritance hierarchy concepts:
 * - Common state and behaviour inheritance from abstract Vehicle
 * - Subclass-specific field and method initialization
 * - Constructor chaining via super(...)
 * - Fail-fast input validation across superclass and subclasses
 * - EV charging state transitions
 */
class VehicleInheritanceTest {

    @Test
    void testTwoWheelerInheritanceAndFields() {
        TwoWheeler bike = new TwoWheeler("tn01ab1234", true);

        // Verify inherited fields and getters
        assertEquals("TN01AB1234", bike.getRegistrationNumber(), "Registration number must be normalized to uppercase");
        assertEquals("TWO_WHEELER", bike.getVehicleType(), "Vehicle type must match superclass assignment");

        // Verify subclass-specific fields and getters
        assertTrue(bike.hasHelmetStorage(), "Helmet storage flag should be true");

        // Verify inherited behaviour
        assertEquals("Registration No: TN01AB1234, Type: TWO_WHEELER", bike.getVehicleInfo());
        assertTrue(bike.getDetailedInfo().contains("Helmet Storage: Yes"));
    }

    @Test
    void testCarInheritanceAndFields() {
        Car car = new Car("tn02cd5678", 5);

        // Verify inherited fields
        assertEquals("TN02CD5678", car.getRegistrationNumber());
        assertEquals("CAR", car.getVehicleType());

        // Verify subclass-specific fields
        assertEquals(5, car.getSeatingCapacity());

        // Verify inherited and specialized behaviour
        assertEquals("Registration No: TN02CD5678, Type: CAR", car.getVehicleInfo());
        assertTrue(car.getDetailedInfo().contains("Seating Capacity: 5"));
    }

    @Test
    void testElectricCarInheritanceAndChargingBehaviour() {
        ElectricCar ev = new ElectricCar("tn03ef9012", 60.0, 20.0);

        // Verify inherited fields
        assertEquals("TN03EF9012", ev.getRegistrationNumber());
        assertEquals("ELECTRIC_CAR", ev.getVehicleType());

        // Verify EV specific fields
        assertEquals(60.0, ev.getBatteryCapacityKWh(), 0.001);
        assertEquals(20.0, ev.getCurrentBatteryLevelKWh(), 0.001);
        assertFalse(ev.isCharging());

        // Test charging simulation
        ev.startCharging();
        assertTrue(ev.isCharging());

        ev.charge(25.0);
        assertEquals(45.0, ev.getCurrentBatteryLevelKWh(), 0.001);

        // Charge cannot exceed total battery capacity
        ev.charge(50.0);
        assertEquals(60.0, ev.getCurrentBatteryLevelKWh(), 0.001, "Charge must be capped at capacity");

        ev.stopCharging();
        assertFalse(ev.isCharging());
    }

    @Test
    void testElectricCarDefaultConstructor() {
        ElectricCar ev = new ElectricCar("TN04GH3456", 80.0);
        assertEquals(80.0, ev.getBatteryCapacityKWh(), 0.001);
        assertEquals(40.0, ev.getCurrentBatteryLevelKWh(), 0.001, "Default battery level should be 50%");
        assertFalse(ev.isCharging());
    }

    @Test
    void testSuperclassValidationRejectsInvalidRegistration() {
        assertThrows(IllegalArgumentException.class, () -> new TwoWheeler(null, true),
                "Null registration number must be rejected");

        assertThrows(IllegalArgumentException.class, () -> new TwoWheeler("   ", true),
                "Blank registration number must be rejected");

        assertThrows(IllegalArgumentException.class, () -> new Car(null, 4),
                "Null registration number must be rejected");

        assertThrows(IllegalArgumentException.class, () -> new ElectricCar("", 50.0),
                "Empty registration number must be rejected");
    }

    @Test
    void testSubclassValidationRejectsInvalidParameters() {
        assertThrows(IllegalArgumentException.class, () -> new Car("TN05IJ7890", 0),
                "Zero seating capacity must be rejected");

        assertThrows(IllegalArgumentException.class, () -> new Car("TN05IJ7890", -4),
                "Negative seating capacity must be rejected");

        assertThrows(IllegalArgumentException.class, () -> new ElectricCar("TN06KL1234", 0.0),
                "Zero battery capacity must be rejected");

        assertThrows(IllegalArgumentException.class, () -> new ElectricCar("TN06KL1234", -10.0),
                "Negative battery capacity must be rejected");

        assertThrows(IllegalArgumentException.class, () -> new ElectricCar("TN06KL1234", 50.0, -5.0),
                "Negative initial battery level must be rejected");

        assertThrows(IllegalArgumentException.class, () -> new ElectricCar("TN06KL1234", 50.0, 55.0),
                "Initial battery level exceeding capacity must be rejected");

        ElectricCar ev = new ElectricCar("TN06KL1234", 50.0);
        assertThrows(IllegalArgumentException.class, () -> ev.charge(0.0),
                "Zero charge energy must be rejected");
        assertThrows(IllegalArgumentException.class, () -> ev.charge(-10.0),
                "Negative charge energy must be rejected");
    }

    @Test
    void testPolymorphicVehicleReference() {
        // Verify that Vehicle is an abstract type that holds subclass instances
        Vehicle v1 = new TwoWheeler("TN10A1", false);
        Vehicle v2 = new Car("TN10B2", 4);
        Vehicle v3 = new ElectricCar("TN10C3", 64.0);

        assertEquals("TWO_WHEELER", v1.getVehicleType());
        assertEquals("CAR", v2.getVehicleType());
        assertEquals("ELECTRIC_CAR", v3.getVehicleType());

        assertTrue(v1 instanceof TwoWheeler);
        assertTrue(v2 instanceof Car);
        assertTrue(v3 instanceof ElectricCar);
    }
}

