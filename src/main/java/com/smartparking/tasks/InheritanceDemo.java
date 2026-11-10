package com.smartparking.tasks;

import com.smartparking.model.Car;
import com.smartparking.model.ElectricCar;
import com.smartparking.model.TwoWheeler;
import com.smartparking.model.Vehicle;

/**
 * Task 6: Inheritance Hierarchy and Constructor Execution Demonstration.
 *
 * Demonstrates:
 * 1. Hierarchical inheritance where TwoWheeler, Car, and ElectricCar extend abstract Vehicle.
 * 2. Inherited common fields (registrationNumber, vehicleType) and methods (getVehicleInfo()).
 * 3. Subclass-specific state and specialized methods.
 * 4. Step-by-step constructor execution trace for ElectricCar instantiation.
 */
public class InheritanceDemo {

    /**
     * Helper to trace constructor execution steps when instantiating an ElectricCar.
     */
    public static class TracedElectricCar extends ElectricCar {
        public TracedElectricCar(String regNo, double batteryCapacity) {
            super(traceStep("2. Subclass constructor invokes super(regNo, \"ELECTRIC_CAR\")", regNo), batteryCapacity);
            System.out.println("   [Trace Step 4] Returning to ElectricCar constructor body:");
            System.out.println("      - Subclass field 'batteryCapacityKWh' initialized to: " + getBatteryCapacityKWh());
            System.out.println("      - Subclass field 'currentBatteryLevelKWh' initialized to: " + getCurrentBatteryLevelKWh());
            System.out.println("      - Subclass field 'isCharging' initialized to: " + isCharging());
            System.out.println("      - Object fully constructed on heap.");
        }

        private static String traceStep(String stepDescription, String value) {
            System.out.println("   [Trace Step 1] " + stepDescription);
            System.out.println("   [Trace Step 2] Calling Vehicle superclass constructor: Vehicle(\"" + value + "\", \"ELECTRIC_CAR\")");
            System.out.println("      - java.lang.Object constructor finishes");
            System.out.println("      - Vehicle fields 'registrationNumber' and 'vehicleType' assigned");
            System.out.println("   [Trace Step 3] Vehicle constructor completes execution");
            return value;
        }
    }

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("               Task 6: Inheritance Hierarchy Demonstration                     ");
        System.out.println("================================================================================");

        // 1. Instantiating Subclasses in the Hierarchical Inheritance Tree
        System.out.println("\n--- 1. Creating Subclass Instances ---");

        TwoWheeler bike = new TwoWheeler("TN09AB1234", true);
        Car sedan = new Car("TN10CD5678", 5);
        ElectricCar ev = new ElectricCar("TN11EF9012", 60.0, 30.0);

        // 2. Demonstrating Inherited vs. Subclass-Specific Fields
        System.out.println("\n--- 2. Inherited Fields and Subclass-Specific Fields ---");

        System.out.println("[TwoWheeler]");
        System.out.println("  Inherited Registration No : " + bike.getRegistrationNumber());
        System.out.println("  Inherited Vehicle Type    : " + bike.getVehicleType());
        System.out.println("  Subclass Helmet Storage   : " + bike.hasHelmetStorage());
        System.out.println("  Superclass Info Method    : " + bike.getVehicleInfo());
        System.out.println("  Specialized Detailed Info : " + bike.getDetailedInfo());

        System.out.println("\n[Car]");
        System.out.println("  Inherited Registration No : " + sedan.getRegistrationNumber());
        System.out.println("  Inherited Vehicle Type    : " + sedan.getVehicleType());
        System.out.println("  Subclass Seating Capacity : " + sedan.getSeatingCapacity());
        System.out.println("  Superclass Info Method    : " + sedan.getVehicleInfo());
        System.out.println("  Specialized Detailed Info : " + sedan.getDetailedInfo());

        System.out.println("\n[ElectricCar]");
        System.out.println("  Inherited Registration No : " + ev.getRegistrationNumber());
        System.out.println("  Inherited Vehicle Type    : " + ev.getVehicleType());
        System.out.println("  Subclass Battery Capacity : " + ev.getBatteryCapacityKWh() + " kWh");
        System.out.println("  Subclass Current Charge   : " + ev.getCurrentBatteryLevelKWh() + " kWh");
        System.out.println("  Subclass Charging Status  : " + ev.isCharging());
        System.out.println("  Superclass Info Method    : " + ev.getVehicleInfo());
        System.out.println("  Specialized Detailed Info : " + ev.getDetailedInfo());

        // Demonstrating EV charging behavior
        System.out.println("\n--- 3. Subclass Specific Behaviour (EV Charging) ---");
        System.out.println("Connecting EV to charging slot...");
        ev.startCharging();
        ev.charge(15.0);
        System.out.println("After charging +15 kWh: " + ev.getDetailedInfo());
        ev.stopCharging();
        System.out.println("Disconnecting EV: " + ev.getDetailedInfo());

        // 3. Step-by-Step Constructor Execution Trace for ElectricCar
        System.out.println("\n--- 4. Constructor Execution Order Trace for new ElectricCar(...) ---");
        System.out.println("Executing: new TracedElectricCar(\"TN99EV8888\", 75.0);");
        TracedElectricCar tracedEv = new TracedElectricCar("TN99EV8888", 75.0);
        System.out.println("\nFinal Initialized Object State:");
        System.out.println("  " + tracedEv);
        System.out.println("================================================================================");
    }
}

