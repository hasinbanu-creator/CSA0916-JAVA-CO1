package com.smartparking.tasks;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests verifying Task 9 AbstractionDemo behavior and console output.
 */
class AbstractionDemoTest {

    @Test
    @DisplayName("AbstractionDemo.main() executes without exceptions and prints required sections")
    void testAbstractionDemoExecutionAndOutput() {
        PrintStream originalOut = System.out;
        ByteArrayOutputStream captured = new ByteArrayOutputStream();

        try {
            System.setOut(new PrintStream(captured, true, StandardCharsets.UTF_8));
            assertDoesNotThrow(() -> AbstractionDemo.main(new String[0]),
                    "AbstractionDemo.main() must execute without throwing any exception");
        } finally {
            System.setOut(originalOut);
        }

        String output = captured.toString(StandardCharsets.UTF_8);

        // Verify key demonstration sections and calculations
        assertTrue(output.contains("1. Abstract Vehicle References"), "Must demonstrate abstract Vehicle references");
        assertTrue(output.contains("Registration No: TN02B2, Type: CAR"));
        assertTrue(output.contains("Registration No: TN03C3, Type: ELECTRIC_CAR"));
        assertTrue(output.contains("EV Parking Fee (120 min): Rs 60.0"), "Must compute parking fee via abstract reference");

        assertTrue(output.contains("2. Chargeable Interface Polymorphism"), "Must demonstrate Chargeable interface reference");
        assertTrue(output.contains("Battery Capacity : 60.0 kWh"));
        assertTrue(output.contains("Initial Charge   : 20.0 kWh"));
        assertTrue(output.contains("State of Charge  : 33.3%"));
        assertTrue(output.contains("Remaining Space  : 40.0 kWh"));

        assertTrue(output.contains("3. Static Utility & Charging Execution"), "Must demonstrate static interface utility method");
        assertTrue(output.contains("Energy Rate      : Rs 12.0 / kWh"));
        assertTrue(output.contains("Delivering Energy: 25.0 kWh (Cost: Rs 300.0)"));

        assertTrue(output.contains("4. Post-Charging Verification"), "Must show post-charging state and default methods");
        assertTrue(output.contains("Updated Charge   : 45.0 / 60.0 kWh"));
        assertTrue(output.contains("Updated SoC      : 75.0%"));
        assertTrue(output.contains("Remaining Space  : 15.0 kWh"));
        assertTrue(output.contains("Est. Time to Full (22 kW): 40.9 mins"));

        assertTrue(output.contains("5. Billing Policy When Energy Exceeds Capacity"), "Must demonstrate fair billing on overflow");
        assertTrue(output.contains("Requested Energy : 30.0 kWh (Remaining Space: 15.0 kWh)"));
        assertTrue(output.contains("Accepted Energy  : 15.0 kWh (Battery safely capped at 100%)"));
        assertTrue(output.contains("Billed Amount    : Rs 180.0"));
        assertTrue(output.contains("Final Charge     : 60.0 / 60.0 kWh"));
    }
}

