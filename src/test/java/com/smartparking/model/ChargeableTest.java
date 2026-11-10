package com.smartparking.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests verifying Task 9 Abstraction and the Chargeable interface:
 * 1. Interface implementation check (ElectricCar implements Chargeable).
 * 2. Initial battery capacity and charge state.
 * 3. Valid charging behavior and return of accepted energy.
 * 4. Charging to exact capacity.
 * 5. Charging beyond capacity (accepted energy capped at remaining space, safe billing verification).
 * 6. Validation rejecting negative, zero, NaN, and infinite energy inputs.
 * 7. Validation rejecting invalid battery capacities in constructor.
 * 8. Verification of default interface methods (remaining capacity, SoC percentage, estimated time with efficiency).
 * 9. Verification of static interface utility methods (cost calculation, energy/power validation).
 * 10. Preservation of Task 7 & 8 fee calculation behavior.
 */
class ChargeableTest {

    @Test
    @DisplayName("ElectricCar implements the Chargeable interface")
    void testElectricCarImplementsChargeable() {
        ElectricCar ev = new ElectricCar("TN03C3", 60.0);
        assertInstanceOf(Chargeable.class, ev, "ElectricCar must be an instance of Chargeable");
        assertTrue(Chargeable.class.isAssignableFrom(ElectricCar.class),
                "Chargeable interface must be assignable from ElectricCar");
        assertInstanceOf(Vehicle.class, ev, "ElectricCar must still be an instance of Vehicle");
    }

    @Test
    @DisplayName("Initial battery values are correctly set and accessible via Chargeable reference")
    void testInitialBatteryValues() {
        Chargeable evDefault = new ElectricCar("TN03C3", 80.0);
        assertEquals(80.0, evDefault.getBatteryCapacityKWh(), 0.001);
        assertEquals(40.0, evDefault.getCurrentChargeKWh(), 0.001, "Default initial charge is 50% of capacity");

        Chargeable evExplicit = new ElectricCar("TN03C3", 100.0, 25.0);
        assertEquals(100.0, evExplicit.getBatteryCapacityKWh(), 0.001);
        assertEquals(25.0, evExplicit.getCurrentChargeKWh(), 0.001);
    }

    @Test
    @DisplayName("Valid charging delivers specified energy to the battery and returns accepted amount")
    void testValidCharging() {
        Chargeable ev = new ElectricCar("TN03C3", 60.0, 20.0);
        double accepted1 = ev.charge(15.0);
        assertEquals(15.0, accepted1, 0.001, "When space is available, entire requested energy is accepted");
        assertEquals(35.0, ev.getCurrentChargeKWh(), 0.001);

        double accepted2 = ev.charge(10.5);
        assertEquals(10.5, accepted2, 0.001);
        assertEquals(45.5, ev.getCurrentChargeKWh(), 0.001);
    }

    @Test
    @DisplayName("Charging to exact capacity reaches 100% full charge")
    void testChargingToExactCapacity() {
        Chargeable ev = new ElectricCar("TN03C3", 50.0, 30.0);
        assertEquals(20.0, ev.getRemainingCapacityKWh(), 0.001);

        double accepted = ev.charge(20.0);
        assertEquals(20.0, accepted, 0.001);
        assertEquals(50.0, ev.getCurrentChargeKWh(), 0.001);
        assertEquals(0.0, ev.getRemainingCapacityKWh(), 0.001);
        assertEquals(100.0, ev.getBatteryPercentage(), 0.001);
    }

    @Test
    @DisplayName("Charging beyond capacity safely clamps charge to total capacity and bills only accepted energy")
    void testChargingBeyondCapacityAndBillingPolicy() {
        Chargeable ev = new ElectricCar("TN03C3", 60.0, 50.0);
        assertEquals(10.0, ev.getRemainingCapacityKWh(), 0.001);

        // Requested 30.0 kWh when only 10.0 kWh space is remaining
        double requestedEnergy = 30.0;
        double acceptedEnergy = ev.charge(requestedEnergy);

        // Invariant 1: Battery must be capped at total capacity (60.0 kWh)
        assertEquals(60.0, ev.getCurrentChargeKWh(), 0.001, "Charge must be clamped at total capacity");
        assertEquals(0.0, ev.getRemainingCapacityKWh(), 0.001);
        assertEquals(100.0, ev.getBatteryPercentage(), 0.001);

        // Invariant 2: Returned accepted energy must strictly equal available space (10.0 kWh)
        assertEquals(10.0, acceptedEnergy, 0.001, "Accepted energy must equal remaining capacity");

        // Invariant 3: Billing policy: customer is billed strictly on accepted energy, not requested energy
        double fairBilledCost = Chargeable.calculateChargingCost(acceptedEnergy, Chargeable.STANDARD_CHARGING_RATE_PER_KWH);
        assertEquals(120.0, fairBilledCost, 0.001, "Billing must be 10 kWh * Rs 12 = Rs 120, NOT 30 kWh * Rs 12 = Rs 360");
    }

    @Test
    @DisplayName("Negative, zero, NaN, and infinite energy inputs throw IllegalArgumentException")
    void testInvalidChargingEnergyInputs() {
        Chargeable ev = new ElectricCar("TN03C3", 60.0, 20.0);

        assertThrows(IllegalArgumentException.class, () -> ev.charge(-5.0), "Negative energy must be rejected");
        assertThrows(IllegalArgumentException.class, () -> ev.charge(0.0), "Zero energy must be rejected");
        assertThrows(IllegalArgumentException.class, () -> ev.charge(Double.NaN), "NaN energy must be rejected");
        assertThrows(IllegalArgumentException.class, () -> ev.charge(Double.POSITIVE_INFINITY), "Infinite energy must be rejected");
        assertThrows(IllegalArgumentException.class, () -> ev.charge(Double.NEGATIVE_INFINITY), "Negative infinite energy must be rejected");
    }

    @Test
    @DisplayName("Invalid battery capacity and initial level throw IllegalArgumentException")
    void testInvalidBatteryCapacityInConstructor() {
        assertThrows(IllegalArgumentException.class, () -> new ElectricCar("TN03C3", -50.0));
        assertThrows(IllegalArgumentException.class, () -> new ElectricCar("TN03C3", 0.0));
        assertThrows(IllegalArgumentException.class, () -> new ElectricCar("TN03C3", Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> new ElectricCar("TN03C3", Double.POSITIVE_INFINITY));

        // Invalid initial battery level
        assertThrows(IllegalArgumentException.class, () -> new ElectricCar("TN03C3", 50.0, -1.0));
        assertThrows(IllegalArgumentException.class, () -> new ElectricCar("TN03C3", 50.0, 60.0));
        assertThrows(IllegalArgumentException.class, () -> new ElectricCar("TN03C3", 50.0, Double.NaN));
    }

    @Test
    @DisplayName("Default interface methods calculate remaining capacity, percentage, and time with efficiency accurately")
    void testDefaultInterfaceMethods() {
        Chargeable ev = new ElectricCar("TN03C3", 80.0, 20.0);

        // getRemainingCapacityKWh()
        assertEquals(60.0, ev.getRemainingCapacityKWh(), 0.001);

        // getBatteryPercentage()
        assertEquals(25.0, ev.getBatteryPercentage(), 0.001);

        // estimateChargingTimeMinutes with ideal efficiency (100%): 60 kWh / 20 kW * 60 min = 180 min
        assertEquals(180.0, ev.estimateChargingTimeMinutes(20.0), 0.001);
        assertEquals(180.0, ev.estimateChargingTimeMinutes(20.0, 1.0), 0.001);

        // estimateChargingTimeMinutes with 90% realistic efficiency: 60 kWh / (20 kW * 0.9) * 60 min = 200 min
        assertEquals(200.0, ev.estimateChargingTimeMinutes(20.0, 0.90), 0.001);

        // estimateChargingTimeMinutes power validation
        assertThrows(IllegalArgumentException.class, () -> ev.estimateChargingTimeMinutes(0.0));
        assertThrows(IllegalArgumentException.class, () -> ev.estimateChargingTimeMinutes(-10.0));
        assertThrows(IllegalArgumentException.class, () -> ev.estimateChargingTimeMinutes(Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> ev.estimateChargingTimeMinutes(Double.POSITIVE_INFINITY));

        // estimateChargingTimeMinutes efficiency validation
        assertThrows(IllegalArgumentException.class, () -> ev.estimateChargingTimeMinutes(20.0, 0.0));
        assertThrows(IllegalArgumentException.class, () -> ev.estimateChargingTimeMinutes(20.0, -0.5));
        assertThrows(IllegalArgumentException.class, () -> ev.estimateChargingTimeMinutes(20.0, 1.05));
        assertThrows(IllegalArgumentException.class, () -> ev.estimateChargingTimeMinutes(20.0, Double.NaN));
    }

    @Test
    @DisplayName("Static interface methods compute charging costs and validate inputs correctly")
    void testStaticInterfaceMethods() {
        // calculateChargingCost
        double cost = Chargeable.calculateChargingCost(25.0, 12.0);
        assertEquals(300.0, cost, 0.001);

        // calculateChargingCost with zero rate (free charging station)
        assertEquals(0.0, Chargeable.calculateChargingCost(25.0, 0.0), 0.001);

        // calculateChargingCost validations
        assertThrows(IllegalArgumentException.class, () -> Chargeable.calculateChargingCost(-10.0, 12.0));
        assertThrows(IllegalArgumentException.class, () -> Chargeable.calculateChargingCost(25.0, -1.0));
        assertThrows(IllegalArgumentException.class, () -> Chargeable.calculateChargingCost(Double.NaN, 12.0));
        assertThrows(IllegalArgumentException.class, () -> Chargeable.calculateChargingCost(25.0, Double.NaN));

        // validateEnergy and validatePower static helpers
        Chargeable.validateEnergy(1.0);
        Chargeable.validatePower(1.0);
        assertThrows(IllegalArgumentException.class, () -> Chargeable.validateEnergy(0.0));
        assertThrows(IllegalArgumentException.class, () -> Chargeable.validatePower(0.0));
    }

    @Test
    @DisplayName("Existing ElectricCar fee calculation is preserved and separate from charging fees")
    void testElectricCarFeeCalculationUnchanged() {
        Vehicle evAsVehicle = new ElectricCar("TN03C3", 60.0, 20.0);

        // Task 7 rate: Rs 30.0 per started hour
        // 120 min = 2 started hours -> Rs 60.0
        assertEquals(60.0, evAsVehicle.calculateFee(120L), 0.001);

        // 130 min = 3 started hours -> Rs 90.0
        assertEquals(90.0, evAsVehicle.calculateFee(130L), 0.001);

        // 420 min = 7 started hours -> Rs 210.0 (matches Task 8 FeeDemo)
        assertEquals(210.0, evAsVehicle.calculateFee(420L), 0.001);
    }
}
