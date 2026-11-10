package com.smartparking.tasks;

import com.smartparking.model.Car;
import com.smartparking.model.Chargeable;
import com.smartparking.model.ElectricCar;
import com.smartparking.model.Vehicle;

/**
 * Task 9 Demonstration: Abstraction in the Campus Smart-Parking System.
 *
 * Demonstrates:
 * 1. Abstract class {@link Vehicle} references holding concrete vehicle objects.
 * 2. Interface {@link Chargeable} references holding an {@link ElectricCar} instance.
 * 3. Calling abstract interface methods through the {@link Chargeable} interface reference.
 * 4. Invoking default interface methods (remaining capacity, battery percentage, estimated charging time).
 * 5. Invoking static utility methods on the interface (charging cost calculation).
 * 6. Deterministic, clear console output exhibiting state transitions during charging.
 * 7. Verification of the billing policy when requested energy exceeds remaining battery capacity.
 */
public class AbstractionDemo {

    public static void main(String[] args) {
        // 1. Abstract Vehicle references holding concrete vehicle objects
        Vehicle regularCar = new Car("TN02B2", 5);
        Vehicle evAsVehicle = new ElectricCar("TN03C3", 60.0, 20.0); // 20.0 kWh initial charge

        System.out.println("--- 1. Abstract Vehicle References ---");
        System.out.println("Car: " + regularCar.getVehicleInfo());
        System.out.println("EV:  " + evAsVehicle.getVehicleInfo());
        System.out.println("EV Parking Fee (120 min): Rs " + evAsVehicle.calculateFee(120));

        // 2. Chargeable interface reference holding an ElectricCar object
        System.out.println("\n--- 2. Chargeable Interface Polymorphism ---");
        Chargeable evAsChargeable = (ElectricCar) evAsVehicle;

        // 3. Invoking abstract interface methods via interface reference
        System.out.println("Battery Capacity : " + evAsChargeable.getBatteryCapacityKWh() + " kWh");
        System.out.println("Initial Charge   : " + evAsChargeable.getCurrentChargeKWh() + " kWh");

        // 4. Invoking default interface methods
        System.out.printf("State of Charge  : %.1f%%%n", evAsChargeable.getBatteryPercentage());
        System.out.println("Remaining Space  : " + evAsChargeable.getRemainingCapacityKWh() + " kWh");

        // 5. Invoking static interface utility method and performing normal charging
        double energyToDeliver = 25.0; // kWh
        double actualAccepted = evAsChargeable.charge(energyToDeliver);
        double chargingCost = Chargeable.calculateChargingCost(actualAccepted, Chargeable.STANDARD_CHARGING_RATE_PER_KWH);

        System.out.println("\n--- 3. Static Utility & Charging Execution ---");
        System.out.println("Energy Rate      : Rs " + Chargeable.STANDARD_CHARGING_RATE_PER_KWH + " / kWh");
        System.out.println("Delivering Energy: " + energyToDeliver + " kWh (Cost: Rs " + chargingCost + ")");

        // 6. Output state after successful charging using default methods
        System.out.println("\n--- 4. Post-Charging Verification ---");
        System.out.println("Updated Charge   : " + evAsChargeable.getCurrentChargeKWh() + " / " + evAsChargeable.getBatteryCapacityKWh() + " kWh");
        System.out.printf("Updated SoC      : %.1f%%%n", evAsChargeable.getBatteryPercentage());
        System.out.println("Remaining Space  : " + evAsChargeable.getRemainingCapacityKWh() + " kWh");
        System.out.printf("Est. Time to Full (22 kW): %.1f mins%n", evAsChargeable.estimateChargingTimeMinutes(22.0));

        // 7. Demonstrating billing policy when requested energy exceeds remaining capacity
        System.out.println("\n--- 5. Billing Policy When Energy Exceeds Capacity ---");
        double excessEnergyRequested = 30.0; // kWh requested when only 15.0 kWh remaining
        double excessAccepted = evAsChargeable.charge(excessEnergyRequested);
        double excessBilledCost = Chargeable.calculateChargingCost(excessAccepted, Chargeable.STANDARD_CHARGING_RATE_PER_KWH);
        System.out.println("Requested Energy : " + excessEnergyRequested + " kWh (Remaining Space: 15.0 kWh)");
        System.out.println("Accepted Energy  : " + excessAccepted + " kWh (Battery safely capped at 100%)");
        System.out.println("Billed Amount    : Rs " + excessBilledCost + " (Billed on " + excessAccepted + " kWh accepted, NOT requested 30.0 kWh)");
        System.out.println("Final Charge     : " + evAsChargeable.getCurrentChargeKWh() + " / " + evAsChargeable.getBatteryCapacityKWh() + " kWh");
    }
}
