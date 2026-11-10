package com.smartparking.tasks;

import com.smartparking.model.Car;
import com.smartparking.model.ElectricCar;
import com.smartparking.model.ParkingTicket;
import com.smartparking.model.TwoWheeler;
import com.smartparking.model.Vehicle;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests verifying Task 10: Packages, Access Modifiers, and Encapsulation.
 *
 * Validates:
 * 1. Public getters return expected values and preserve state encapsulation.
 * 2. Fail-fast validation guards prevent corrupted object states.
 * 3. Model instance fields are strictly declared private (verified via reflection).
 * 4. Autoboxing and unboxing mechanics and null unboxing hazards.
 * 5. Wrapper comparison semantics: Integer == identity vs .equals() value equality.
 * 6. EncapsulationDemo console execution and output fidelity.
 */
class EncapsulationDemoTest {

    @Test
    @DisplayName("Public getters return expected state values across model hierarchy")
    void testPublicGettersReturnExpectedValues() {
        ElectricCar ev = new ElectricCar("TN03CC3333", 60.0, 20.0);
        Car car = new Car("TN02BB2222", 5);
        TwoWheeler bike = new TwoWheeler("TN01AA1111", true);

        assertEquals("TN03CC3333", ev.getRegistrationNumber());
        assertEquals("ELECTRIC_CAR", ev.getVehicleType());
        assertEquals(60.0, ev.getBatteryCapacityKWh(), 0.001);
        assertEquals(20.0, ev.getCurrentChargeKWh(), 0.001);
        assertEquals(20.0, ev.getCurrentBatteryLevelKWh(), 0.001);

        assertEquals("TN02BB2222", car.getRegistrationNumber());
        assertEquals("CAR", car.getVehicleType());
        assertEquals(5, car.getSeatingCapacity());

        assertEquals("TN01AA1111", bike.getRegistrationNumber());
        assertEquals("TWO_WHEELER", bike.getVehicleType());
        assertTrue(bike.hasHelmetStorage());
    }

    @Test
    @DisplayName("Validation guards reject invalid data and prevent corrupted object states")
    void testValidationRejectsInvalidValues() {
        ElectricCar ev = new ElectricCar("TN03CC3333", 60.0, 20.0);

        // A. Reject negative duration
        assertThrows(IllegalArgumentException.class, () -> ev.calculateFee(-1));
        assertThrows(IllegalArgumentException.class, () -> ev.calculateFee(-500));

        // B. Reject invalid charging inputs
        assertThrows(IllegalArgumentException.class, () -> ev.charge(-5.0));
        assertThrows(IllegalArgumentException.class, () -> ev.charge(0.0));
        assertThrows(IllegalArgumentException.class, () -> ev.charge(Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> ev.charge(Double.POSITIVE_INFINITY));

        // C. Reject invalid vehicle construction parameters
        assertThrows(IllegalArgumentException.class, () -> new Car("TN02B2", 0), "Seating capacity must be > 0");
        assertThrows(IllegalArgumentException.class, () -> new Car("TN02B2", -3), "Negative seating capacity must be rejected");
        assertThrows(IllegalArgumentException.class, () -> new Car(null, 5), "Null registration number must be rejected");
        assertThrows(IllegalArgumentException.class, () -> new Car("   ", 5), "Blank registration number must be rejected");
        assertThrows(IllegalArgumentException.class, () -> new ElectricCar("TN03C3", -50.0), "Negative battery capacity must be rejected");
    }

    @Test
    @DisplayName("Instance fields in model classes are strictly private (Data Hiding)")
    void testModelFieldsAreStrictlyPrivate() {
        Class<?>[] modelClasses = {
            Vehicle.class,
            Car.class,
            TwoWheeler.class,
            ElectricCar.class,
            ParkingTicket.class
        };

        for (Class<?> clazz : modelClasses) {
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                // Ignore synthetic fields (e.g. coverage tools or compiler artifacts)
                if (field.isSynthetic()) {
                    continue;
                }
                int modifiers = field.getModifiers();
                // Static final constants can be public/protected (e.g. rate constants), but instance fields MUST be private
                if (!Modifier.isStatic(modifiers)) {
                    assertTrue(Modifier.isPrivate(modifiers),
                            "Field '" + field.getName() + "' in " + clazz.getSimpleName()
                                    + " must be declared private to enforce encapsulation.");
                }
            }
        }
    }

    @Test
    @DisplayName("Autoboxing and unboxing behave correctly and throw NullPointerException on null unbox")
    void testAutoboxingAndUnboxingBehavior() {
        // Primitive to wrapper (Autoboxing)
        int primitiveMinutes = 60;
        Integer boxedMinutes = primitiveMinutes; // Autoboxed via Integer.valueOf(int)
        assertNotNull(boxedMinutes);
        assertEquals(60, boxedMinutes.intValue());

        // Wrapper to primitive (Unboxing)
        int unboxedMinutes = boxedMinutes; // Unboxed via boxedMinutes.intValue()
        assertEquals(60, unboxedMinutes);

        // Null unboxing hazard
        Integer nullWrapper = null;
        assertThrows(NullPointerException.class, () -> {
            int failedUnbox = nullWrapper; // Throws NPE at runtime
        }, "Unboxing a null wrapper must throw NullPointerException");

        // Method invocation conversion: Integer unboxed to int, then widened to long
        ElectricCar ev = new ElectricCar("TN03CC3333", 60.0, 20.0);
        Integer boxedDuration = Integer.valueOf(120);
        // calculateFee accepts primitive long:
        // boxedDuration (Integer) is unboxed to int (via .intValue()),
        // then widened to long, and the fee is returned as primitive double.
        double fee = ev.calculateFee(boxedDuration);
        assertEquals(60.0, fee, 0.001);
    }

    @Test
    @DisplayName("Integer == compares object references while .equals() compares numeric values")
    void testIntegerEqualsVersusIdentityContract() {
        // Values within the default Integer Cache (-128 to 127)
        Integer low1 = Integer.valueOf(100);
        Integer low2 = Integer.valueOf(100);
        assertTrue(low1 == low2, "Integer values within [-128, 127] must point to the same cached instance");
        assertTrue(low1.equals(low2), "Integer.equals() must evaluate to true for identical numeric values");

        // Values outside the default Integer Cache (> 127)
        Integer high1 = Integer.valueOf(200);
        Integer high2 = Integer.valueOf(200);
        assertFalse(high1 == high2, "Integer values outside [-128, 127] must be distinct heap objects");
        assertTrue(high1.equals(high2), "Integer.equals() must evaluate to true regardless of memory address");

        // Lower boundary of cache (-128 cached, -129 not cached)
        Integer minCached1 = Integer.valueOf(-128);
        Integer minCached2 = Integer.valueOf(-128);
        assertTrue(minCached1 == minCached2, "-128 is within the default Integer Cache");

        Integer belowCache1 = Integer.valueOf(-129);
        Integer belowCache2 = Integer.valueOf(-129);
        assertFalse(belowCache1 == belowCache2, "-129 is outside the default Integer Cache");
        assertTrue(belowCache1.equals(belowCache2), "-129 values are numerically equal");
    }

    @Test
    @DisplayName("EncapsulationDemo.main() executes cleanly and outputs all required demonstration sections")
    void testEncapsulationDemoMainExecution() {
        PrintStream originalOut = System.out;
        ByteArrayOutputStream captured = new ByteArrayOutputStream();

        try {
            System.setOut(new PrintStream(captured, true, StandardCharsets.UTF_8));
            assertDoesNotThrow(() -> EncapsulationDemo.main(new String[0]),
                    "EncapsulationDemo.main() must execute without throwing an exception");
        } finally {
            System.setOut(originalOut);
        }

        String output = captured.toString(StandardCharsets.UTF_8);

        // Verify key sections are present
        assertTrue(output.contains("1. Reading Encapsulated State via Public Getters"));
        assertTrue(output.contains("Vehicle Registration : TN03CC3333"));
        assertTrue(output.contains("2. Controlled State Mutation via Public Business Methods"));
        assertTrue(output.contains("Accepted Energy      : 25.0 kWh"));
        assertTrue(output.contains("3. Validation Guards Protecting Object Invariants"));
        assertTrue(output.contains("[REJECTED] Negative stay duration"));
        assertTrue(output.contains("[REJECTED] Non-positive energy"));
        assertTrue(output.contains("[REJECTED] Invalid seat count"));
        assertTrue(output.contains("[REJECTED] Blank registration number"));
        assertTrue(output.contains("4. Autoboxing and Unboxing in Parking Operations"));
        assertTrue(output.contains("Integer duration unboxed to int, widened to long, returning double fee: Rs 60.0"));
        assertTrue(output.contains("5. Wrapper Class Comparison: Integer '==' vs. '.equals()'"));
        assertTrue(output.contains("slotA == slotB      : true"));
        assertTrue(output.contains("slotC == slotD      : false"));
        assertTrue(output.contains("slotC.equals(slotD) : true"));
    }
}

