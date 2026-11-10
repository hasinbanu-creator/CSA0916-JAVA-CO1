package com.smartparking.tasks;

import com.smartparking.model.Car;
import com.smartparking.model.ElectricCar;
import com.smartparking.model.ParkingTariff;
import com.smartparking.model.TwoWheeler;
import com.smartparking.model.Vehicle;

import java.util.HashSet;
import java.util.Set;

/**
 * Task 7: Method Overriding and Specialisation Demonstration.
 *
 * Demonstrates:
 * 1. Runtime polymorphic invocation of overridden calculateFee() via Vehicle references.
 * 2. Overridden toString() representations for each subclass.
 * 3. Overridden equals() and hashCode() maintaining logical identity by registration number.
 * 4. Deduplication of equal Vehicle instances within a HashSet.
 * 5. Practical application of the final keyword across variables, methods, and classes.
 */
public class OverrideDemo {

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("           Task 7: Method Overriding and Specialisation Demonstration           ");
        System.out.println("================================================================================");

        // Tariff policy reminder
        System.out.println("\n--- 0. Established Baseline Tariff Policies ---");
        System.out.println("   Two-Wheeler Rate : Rs " + ParkingTariff.TWO_WHEELER_RATE_PER_HOUR + " / started hour");
        System.out.println("   Car Rate         : Rs " + ParkingTariff.CAR_RATE_PER_HOUR + " / started hour");
        System.out.println("   Electric Car Rate: Rs " + ParkingTariff.ELECTRIC_CAR_RATE_PER_HOUR + " / started hour");
        System.out.println("   Billing Rule     : Every started hour is charged in full; 0 min costs Rs 0.0.");

        // 1. Heterogeneous Array and Runtime Polymorphism via Overridden calculateFee()
        System.out.println("\n--- 1. Polymorphic Fee Calculation via Vehicle[] Array ---");
        Vehicle[] fleet = new Vehicle[] {
            new TwoWheeler("TN01AA1111", true),
            new Car("TN02BB2222", 5),
            new ElectricCar("TN03CC3333", 60.0, 30.0)
        };

        long testStayDuration = 130; // 2 hours 10 minutes -> 3 started hours
        System.out.println("Stay Duration: " + testStayDuration + " minutes (Ceiling = " +
                ParkingTariff.calculateStartedHours(testStayDuration) + " billable hours)\n");

        for (Vehicle v : fleet) {
            // Dynamic method dispatch invokes the specialized subclass implementation of calculateFee()
            double fee = v.calculateFee(testStayDuration);
            System.out.println("Vehicle        : " + v.toString());
            System.out.println("Declared Type  : Vehicle");
            System.out.println("Actual Type    : " + v.getClass().getSimpleName());
            System.out.println("Calculated Fee : Rs " + fee);
            System.out.println("--------------------------------------------------------------------------------");
        }

        // 2. Object Equality and Hash Code Demonstration
        System.out.println("\n--- 2. Object-Class Overrides: equals() and hashCode() ---");
        Vehicle vOriginalCar = new Car("TN09XY9999", 4);
        Vehicle vDuplicateCar = new Car("TN09XY9999", 5); // Different seating, same regNo
        Vehicle vCrossTypeEv = new ElectricCar("TN09XY9999", 70.0); // Different vehicle subclass, same regNo
        Vehicle vDifferentVehicle = new Car("TN10ZZ1234", 4);

        System.out.println("Car 1           : " + vOriginalCar);
        System.out.println("Car 2 (Same Reg): " + vDuplicateCar);
        System.out.println("EV (Same Reg)   : " + vCrossTypeEv);
        System.out.println("Car 3 (Diff Reg): " + vDifferentVehicle);

        System.out.println("\nEquality Checks (Identity based on Registration Number):");
        System.out.println("Car 1 equals Car 1 (Reflexive)           : " + vOriginalCar.equals(vOriginalCar));
        System.out.println("Car 1 equals Car 2 (Same reg, diff seats): " + vOriginalCar.equals(vDuplicateCar));
        System.out.println("Car 1 equals EV (Same reg, diff subclass): " + vOriginalCar.equals(vCrossTypeEv));
        System.out.println("Car 1 equals Car 3 (Different reg)       : " + vOriginalCar.equals(vDifferentVehicle));
        System.out.println("Car 1 equals null                        : " + vOriginalCar.equals(null));
        System.out.println("Car 1 equals \"string\"                    : " + vOriginalCar.equals("TN09XY9999"));

        System.out.println("\nHash Code Verification (Equal objects must have equal hash codes):");
        System.out.println("Car 1 hashCode : " + vOriginalCar.hashCode());
        System.out.println("Car 2 hashCode : " + vDuplicateCar.hashCode());
        System.out.println("EV hashCode    : " + vCrossTypeEv.hashCode());
        System.out.println("Car 3 hashCode : " + vDifferentVehicle.hashCode());
        System.out.println("Contract Check : (Car1 == Car2) -> " + (vOriginalCar.hashCode() == vDuplicateCar.hashCode()));

        // 3. HashSet Deduplication Demonstration
        System.out.println("\n--- 3. HashSet Duplicate Elimination via equals() and hashCode() ---");
        Set<Vehicle> uniqueVehicles = new HashSet<>();
        uniqueVehicles.add(vOriginalCar);
        uniqueVehicles.add(vDuplicateCar); // duplicate regNo
        uniqueVehicles.add(vCrossTypeEv);  // duplicate regNo
        uniqueVehicles.add(vDifferentVehicle);

        System.out.println("Total elements attempted to insert : 4");
        System.out.println("Distinct elements retained in Set  : " + uniqueVehicles.size());
        for (Vehicle v : uniqueVehicles) {
            System.out.println("  In Set: " + v);
        }

        // 4. Demonstrating the Three Uses of the final Keyword
        System.out.println("\n--- 4. Demonstrating the Three Uses of the 'final' Keyword ---");

        // A. Final Variable: value cannot be reassigned after initialization
        final double flatDiscount = 5.0;
        System.out.println("A. Final Variable  : flatDiscount = " + flatDiscount + " (cannot be reassigned)");

        // B. Final Method: subclasses inherit getRegistrationNumber() but cannot override it
        System.out.println("B. Final Method    : vOriginalCar.getRegistrationNumber() = " +
                vOriginalCar.getRegistrationNumber() + " (protected from subclass tampering)");

        // C. Final Class: ParkingTariff is declared final and cannot be subclassed
        System.out.println("C. Final Class     : ParkingTariff cannot be extended; private constructor prevents 'new'");

        System.out.println("================================================================================");
    }
}

