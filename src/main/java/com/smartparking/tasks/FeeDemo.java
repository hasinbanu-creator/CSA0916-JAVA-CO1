package com.smartparking.tasks;

import com.smartparking.model.Car;
import com.smartparking.model.ElectricCar;
import com.smartparking.model.ParkingTariff;
import com.smartparking.model.TwoWheeler;
import com.smartparking.model.Vehicle;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

/**
 * Task 8 Demonstration: Compile-Time and Runtime Polymorphism in the Campus Smart-Parking System.
 *
 * Demonstrates:
 * 1. Compile-Time Polymorphism (Static Binding):
 *    - Overloaded {@code calculateFee} methods with different parameter signatures
 *      (e.g., duration in minutes, entry/exit LocalTime, whole hours, LocalDateTime).
 * 2. Runtime Polymorphism (Dynamic Binding / Dynamic Method Dispatch):
 *    - Parent-class references ({@link Vehicle}) in a {@code Vehicle[]} array holding
 *      concrete subtype objects ({@link TwoWheeler}, {@link Car}, {@link ElectricCar}).
 *    - Dynamic dispatch resolving {@code calculateFee()} to the target subclass at runtime.
 * 3. Safe Downcasting and Type Inspection:
 *    - Using pattern matching for {@code instanceof} to safely access subtype-specific members.
 * 4. Logical Identity Verification:
 *    - Evaluating cross-subclass equality based on license plate / registration number.
 *
 * Execution Output:
 * Prints the exact four lines required by the academic assignment specification:
 * <pre>{@code
 * TwoWheeler TN01A1 -> Rs 30.0
 * Car TN02B2 -> Rs 90.0
 * ElectricCar TN03C3 -> Rs 210.0
 * true
 * }</pre>
 */
public class FeeDemo {

    // =========================================================================
    // Overloaded Methods (Compile-Time Polymorphism / Static Binding)
    // =========================================================================

    /**
     * Overload 1: Computes fee from duration in whole minutes.
     *
     * @param vehicle         target vehicle (non-null)
     * @param durationMinutes parking stay duration in minutes (non-negative)
     * @return calculated parking fee in INR
     */
    public static double calculateFee(Vehicle vehicle, long durationMinutes) {
        Objects.requireNonNull(vehicle, "Vehicle cannot be null.");
        return vehicle.calculateFee(durationMinutes);
    }

    /**
     * Overload 2: Computes fee from entry and exit {@link LocalTime} timestamps.
     * Handles same-day stays and overnight stays across midnight.
     *
     * @param vehicle   target vehicle (non-null)
     * @param entryTime entry timestamp (non-null)
     * @param exitTime  exit timestamp (non-null)
     * @return calculated parking fee in INR
     */
    public static double calculateFee(Vehicle vehicle, LocalTime entryTime, LocalTime exitTime) {
        Objects.requireNonNull(vehicle, "Vehicle cannot be null.");
        Objects.requireNonNull(entryTime, "Entry time cannot be null.");
        Objects.requireNonNull(exitTime, "Exit time cannot be null.");

        long minutes = Duration.between(entryTime, exitTime).toMinutes();
        if (minutes < 0) {
            // Stay spans across midnight (e.g. 23:00 to 02:00 = +21 hours)
            minutes += 24 * 60;
        }
        return calculateFee(vehicle, minutes);
    }

    /**
     * Overload 3: Computes fee from whole hours duration.
     *
     * @param vehicle    target vehicle (non-null)
     * @param wholeHours duration in whole hours (must be non-negative)
     * @return calculated parking fee in INR
     */
    public static double calculateFee(Vehicle vehicle, int wholeHours) {
        Objects.requireNonNull(vehicle, "Vehicle cannot be null.");
        if (wholeHours < 0) {
            throw new IllegalArgumentException("Hours duration cannot be negative: " + wholeHours);
        }
        long minutes = (long) wholeHours * ParkingTariff.MINUTES_PER_HOUR;
        return calculateFee(vehicle, minutes);
    }

    /**
     * Overload 4: Computes fee from entry and exit {@link LocalDateTime} timestamps.
     *
     * @param vehicle       target vehicle (non-null)
     * @param entryDateTime entry date and time (non-null)
     * @param exitDateTime  exit date and time (non-null, must not precede entry)
     * @return calculated parking fee in INR
     */
    public static double calculateFee(Vehicle vehicle, LocalDateTime entryDateTime, LocalDateTime exitDateTime) {
        Objects.requireNonNull(vehicle, "Vehicle cannot be null.");
        Objects.requireNonNull(entryDateTime, "Entry date-time cannot be null.");
        Objects.requireNonNull(exitDateTime, "Exit date-time cannot be null.");

        long minutes = Duration.between(entryDateTime, exitDateTime).toMinutes();
        if (minutes < 0) {
            throw new IllegalArgumentException("Exit date-time cannot precede entry date-time.");
        }
        return calculateFee(vehicle, minutes);
    }

    // =========================================================================
    // Safe Downcasting and Subclass Inspection Helper
    // =========================================================================

    /**
     * Inspects subclass-specific features safely using pattern matching for {@code instanceof}.
     * Demonstrates safe downcasting without risking {@link ClassCastException}.
     *
     * @param vehicle vehicle reference to inspect
     * @return human-readable description of subtype-specific fields
     */
    public static String getSubclassSpecificFeature(Vehicle vehicle) {
        if (vehicle == null) {
            return "Null reference";
        }
        if (vehicle instanceof ElectricCar ec) {
            // Safely downcasted to ElectricCar: access battery capacity and charging status
            return "ElectricCar [Battery: " + ec.getBatteryCapacityKWh() + " kWh, Charging: " + ec.isCharging() + "]";
        } else if (vehicle instanceof Car c) {
            // Safely downcasted to Car: access seating capacity
            return "Car [Seats: " + c.getSeatingCapacity() + "]";
        } else if (vehicle instanceof TwoWheeler tw) {
            // Safely downcasted to TwoWheeler: access helmet storage
            return "TwoWheeler [Helmet Storage: " + tw.hasHelmetStorage() + "]";
        }
        return "Generic Vehicle [" + vehicle.getRegistrationNumber() + "]";
    }

    // =========================================================================
    // Main Application Entry Point
    // =========================================================================

    /**
     * Main method executing the polymorphic fee calculation demonstration.
     * Produces the exact 4-line output specified by the academic assignment PPT.
     *
     * @param args command-line arguments (ignored)
     */
    public static void main(String[] args) {
        // 1. Upcasting: Child-class objects stored in a parent-class Vehicle[] array
        Vehicle[] parked = {
            new TwoWheeler("TN01A1", false),
            new Car("TN02B2", 5),
            new ElectricCar("TN03C3", 60.0)
        };

        // 2. Durations selected to reflect realistic parking stays and match expected fee amounts
        //    under the established Task 7 tariff policies (ceiling rounding):
        //    - TwoWheeler: 130 min -> 3 started hours @ Rs 10.0/hr = Rs 30.0
        //    - Car:        130 min -> 3 started hours @ Rs 30.0/hr = Rs 90.0
        //    - ElectricCar: 420 min -> 7 started hours @ Rs 30.0/hr = Rs 210.0
        long[] durationsMinutes = { 130L, 130L, 420L };

        // 3. Dynamic method dispatch over the polymorphic array
        for (int i = 0; i < parked.length; i++) {
            Vehicle v = parked[i];

            // Safe downcasting demonstration using pattern matching for instanceof
            if (v instanceof ElectricCar ec) {
                if (ec.getBatteryCapacityKWh() <= 0) {
                    throw new IllegalStateException("EV battery capacity must be positive");
                }
            } else if (v instanceof Car c) {
                if (c.getSeatingCapacity() <= 0) {
                    throw new IllegalStateException("Car seating capacity must be positive");
                }
            } else if (v instanceof TwoWheeler tw) {
                // Verified safe access to two-wheeler specific state
                boolean hasStorage = tw.hasHelmetStorage();
            }

            // Dynamic method dispatch: JVM resolves calculateFee() based on actual runtime object
            double fee = v.calculateFee(durationsMinutes[i]);

            System.out.println(v.getClass().getSimpleName() + " " + v.getRegistrationNumber()
                    + " -> Rs " + fee);
        }

        // 4. Equality comparison: validates cross-subclass identity contract
        System.out.println(new Car("TN02B2", 5).equals(new ElectricCar("TN02B2", 60.0)));
    }
}

