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
     * Helper subclass used exclusively to demonstrate constructor execution order.
     * Inherits from ElectricCar to trace the completion of superclass constructors
     * and the execution of its own subclass constructor body.
     */
    public static class TracedElectricCar extends ElectricCar {
        public TracedElectricCar(String regNo, double batteryCapacity) {
            super(regNo, batteryCapacity);
            System.out.println("   [Step 5] TracedElectricCar constructor body executes:");
            System.out.println("      - super(regNo, batteryCapacity) has completed.");
            System.out.println("      - Inherited Vehicle state: regNo=" + getRegistrationNumber() + ", type=" + getVehicleType());
            System.out.println("      - Inherited ElectricCar state: battery=" + getBatteryCapacityKWh() + " kWh, level=" + getCurrentBatteryLevelKWh() + " kWh, charging=" + isCharging());
            System.out.println("      - TracedElectricCar constructor body finishes.");
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

        // 4. Step-by-Step Constructor Execution Trace for ElectricCar
        System.out.println("\n--- 4. Constructor Execution Order Trace for new ElectricCar(...) ---");
        System.out.println("Chronological order enforced by Java language specification:");
        System.out.println("   [Step 1] Memory Allocation & Default Field Initialization:");
        System.out.println("            Heap memory allocated. All fields initialized to binary defaults");
        System.out.println("            (registrationNumber=null, vehicleType=null, batteryCapacityKWh=0.0, etc.).");
        System.out.println("   [Step 2] java.lang.Object Constructor:");
        System.out.println("            Vehicle constructor delegates to super() (Object constructor), which finishes.");
        System.out.println("   [Step 3] Vehicle Instance Field Initialization & Constructor Body:");
        System.out.println("            Instance initializers run, parameters are validated, and");
        System.out.println("            registrationNumber=\"TN99EV8888\", vehicleType=\"ELECTRIC_CAR\" are assigned.");
        System.out.println("   [Step 4] ElectricCar Instance Field Initialization & Constructor Body:");
        System.out.println("            Subclass instance initializers run, battery parameters are validated,");
        System.out.println("            batteryCapacityKWh=75.0, currentBatteryLevelKWh=37.5, isCharging=false are assigned.");
        System.out.println("\nExecuting: new TracedElectricCar(\"TN99EV8888\", 75.0);");
        TracedElectricCar tracedEv = new TracedElectricCar("TN99EV8888", 75.0);
        System.out.println("\nFinal Initialized Object State:");
        System.out.println("  " + tracedEv.getDetailedInfo());
        System.out.println("================================================================================");
    }
}
