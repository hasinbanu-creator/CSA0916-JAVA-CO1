package com.smartparking.tasks;

import com.smartparking.model.Car;
import com.smartparking.model.ElectricCar;
import com.smartparking.model.ParkingTicket;
import com.smartparking.model.TwoWheeler;
import com.smartparking.model.Vehicle;
import com.smartparking.service.ParkingExitSummary;
import com.smartparking.service.ParkingManagementService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Task 11 Integration Demonstration: End-to-End Campus Smart-Parking System.
 *
 * Demonstrates:
 * 1. Facility initialization and multi-category vehicle admission (TwoWheeler, Car, ElectricCar).
 * 2. Slot allocation and ticket issuance using array-based active ticket storage.
 * 3. Boundary cases: last available slot, facility full rejection, and slot reuse after departure.
 * 4. Invalid cases: duplicate active license plate rejection and unknown ticket rejection.
 * 5. Departures with polymorphic fee calculation and separate EV charging billing.
 * 6. Daily collection reconciliation comparing programmatic revenue with manual hand calculation.
 * 7. Empirical lookup performance comparison between ParkingTicket[], ArrayList<ParkingTicket>, and HashMap.
 */
public class ParkingSystemIntegrationDemo {

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("       CAMPUS SMART-PARKING SYSTEM - TASK 11 INTEGRATED SYSTEM DEMO            ");
        System.out.println("================================================================================");

        // ---------------------------------------------------------------------
        // 1. System Initialization
        // ---------------------------------------------------------------------
        System.out.println("\n--- 1. Initializing Multi-Level Parking Facility ---");
        // Multi-level facility: Floor 0 has 2 slots, Floor 1 has 2 slots (total capacity = 4)
        int[] slotsPerFloor = new int[]{2, 2};
        ParkingManagementService service = new ParkingManagementService(slotsPerFloor);
        System.out.println("Facility Initialized: 2 Floors, 4 Total Slots (Slots per floor: [2, 2])");
        System.out.println("Active Ticket Storage: Array-based ParkingTicket[4]");

        // ---------------------------------------------------------------------
        // 2. Vehicle Admissions (Normal Operations & Last Free Slot)
        // ---------------------------------------------------------------------
        System.out.println("\n--- 2. Vehicle Entry & Slot Allocation ---");

        TwoWheeler bike = new TwoWheeler("KA01AB1001", true);
        LocalDateTime timeBikeIn = LocalDateTime.of(2026, 11, 10, 8, 30);
        ParkingTicket ticketBike = service.parkVehicle(bike, timeBikeIn);
        System.out.printf("Admitted: %-12s | Category: %-12s | Ticket: %-8s | Location: Floor %d, Slot %d\n",
                bike.getRegistrationNumber(), bike.getVehicleType(), ticketBike.getTicketNumber(),
                ticketBike.getFloorNumber(), ticketBike.getSlotNumber());

        Car car1 = new Car("TN09CD2002", 5);
        LocalDateTime timeCar1In = LocalDateTime.of(2026, 11, 10, 9, 0);
        ParkingTicket ticketCar1 = service.parkVehicle(car1, timeCar1In);
        System.out.printf("Admitted: %-12s | Category: %-12s | Ticket: %-8s | Location: Floor %d, Slot %d\n",
                car1.getRegistrationNumber(), car1.getVehicleType(), ticketCar1.getTicketNumber(),
                ticketCar1.getFloorNumber(), ticketCar1.getSlotNumber());

        ElectricCar ev = new ElectricCar("DL01EV3003", 50.0, 20.0);
        LocalDateTime timeEvIn = LocalDateTime.of(2026, 11, 10, 10, 0);
        ParkingTicket ticketEv = service.parkVehicle(ev, timeEvIn);
        System.out.printf("Admitted: %-12s | Category: %-12s | Ticket: %-8s | Location: Floor %d, Slot %d\n",
                ev.getRegistrationNumber(), ev.getVehicleType(), ticketEv.getTicketNumber(),
                ticketEv.getFloorNumber(), ticketEv.getSlotNumber());

        Car car2 = new Car("MH02EF4004", 4);
        LocalDateTime timeCar2In = LocalDateTime.of(2026, 11, 10, 10, 15);
        ParkingTicket ticketCar2 = service.parkVehicle(car2, timeCar2In);
        System.out.printf("Admitted: %-12s | Category: %-12s | Ticket: %-8s | Location: Floor %d, Slot %d [LAST FREE SLOT]\n",
                car2.getRegistrationNumber(), car2.getVehicleType(), ticketCar2.getTicketNumber(),
                ticketCar2.getFloorNumber(), ticketCar2.getSlotNumber());

        System.out.println("Active Parked Vehicles: " + service.getActiveTicketCount() + " / " + service.getCapacity());

        // ---------------------------------------------------------------------
        // 3. Boundary & Invalid Case Demonstrations
        // ---------------------------------------------------------------------
        System.out.println("\n--- 3. Boundary & Invalid Case Demonstrations ---");

        // Case A: Facility full rejection
        Car overflowCar = new Car("AP03GH5005", 5);
        LocalDateTime timeOverflow = LocalDateTime.of(2026, 11, 10, 10, 20);
        try {
            service.parkVehicle(overflowCar, timeOverflow);
        } catch (IllegalStateException e) {
            System.out.println("[REJECTED - Facility Full]: " + e.getMessage());
        }

        // Case B: Duplicate active registration rejection
        try {
            service.parkVehicle(new Car("TN09CD2002", 5), timeOverflow);
        } catch (IllegalStateException e) {
            System.out.println("[REJECTED - Duplicate Reg ]: " + e.getMessage());
        }

        // Case C: Invalid / unknown ticket exit
        try {
            service.processExit("TKT-9999", LocalDateTime.of(2026, 11, 10, 11, 0));
        } catch (IllegalArgumentException e) {
            System.out.println("[REJECTED - Unknown Ticket]: " + e.getMessage());
        }

        // ---------------------------------------------------------------------
        // 4. Vehicle Departures & Fee Calculations
        // ---------------------------------------------------------------------
        System.out.println("\n--- 4. Vehicle Departures, Fee Calculation & Slot Release ---");

        // Exit 1: TwoWheeler exits at 09:45 (Duration: 75 min -> 2 billable hours @ Rs 10 = Rs 20.0)
        LocalDateTime timeBikeOut = LocalDateTime.of(2026, 11, 10, 9, 45);
        ParkingExitSummary summaryBike = service.processExit(ticketBike.getTicketNumber(), timeBikeOut);
        System.out.printf("Departed: %-12s | Duration: %3d min | Parking: Rs %6.2f | Charging: Rs %5.2f | Total: Rs %6.2f | Released: [%d, %d]\n",
                summaryBike.vehicle().getRegistrationNumber(), summaryBike.durationMinutes(),
                summaryBike.parkingFee(), summaryBike.chargingFee(), summaryBike.totalFee(),
                summaryBike.releasedFloor(), summaryBike.releasedSlot());

        // Demonstrate Slot Re-use: overflowCar can now enter and take the newly freed slot [0, 0]!
        LocalDateTime timeOverflowIn = LocalDateTime.of(2026, 11, 10, 10, 25);
        ParkingTicket ticketOverflow = service.parkVehicle(overflowCar, timeOverflowIn);
        System.out.printf("Re-admitted into freed slot: %-12s | Ticket: %-8s | Allocated Location: Floor %d, Slot %d\n",
                overflowCar.getRegistrationNumber(), ticketOverflow.getTicketNumber(),
                ticketOverflow.getFloorNumber(), ticketOverflow.getSlotNumber());

        // Exit 2: Car 1 exits at 11:00 (Duration: 120 min -> exact 2-hour slab boundary @ Rs 30 = Rs 60.0)
        LocalDateTime timeCar1Out = LocalDateTime.of(2026, 11, 10, 11, 0);
        ParkingExitSummary summaryCar1 = service.processExit(ticketCar1.getTicketNumber(), timeCar1Out);
        System.out.printf("Departed: %-12s | Duration: %3d min | Parking: Rs %6.2f | Charging: Rs %5.2f | Total: Rs %6.2f | Released: [%d, %d]\n",
                summaryCar1.vehicle().getRegistrationNumber(), summaryCar1.durationMinutes(),
                summaryCar1.parkingFee(), summaryCar1.chargingFee(), summaryCar1.totalFee(),
                summaryCar1.releasedFloor(), summaryCar1.releasedSlot());

        // Exit 3: Electric Car exits at 12:30 with 15.0 kWh charging (Duration: 150 min -> 3 billable hrs @ Rs 30 = Rs 90.0, Charging: 15 kWh @ Rs 12 = Rs 180.0)
        LocalDateTime timeEvOut = LocalDateTime.of(2026, 11, 10, 12, 30);
        ParkingExitSummary summaryEv = service.processExit(ticketEv.getTicketNumber(), timeEvOut, 15.0);
        System.out.printf("Departed: %-12s | Duration: %3d min | Parking: Rs %6.2f | Charging: Rs %5.2f | Total: Rs %6.2f | Released: [%d, %d]\n",
                summaryEv.vehicle().getRegistrationNumber(), summaryEv.durationMinutes(),
                summaryEv.parkingFee(), summaryEv.chargingFee(), summaryEv.totalFee(),
                summaryEv.releasedFloor(), summaryEv.releasedSlot());

        // Exit 4: Car 2 exits at 10:45 (Duration: 30 min -> 1 billable hour @ Rs 30 = Rs 30.0)
        LocalDateTime timeCar2Out = LocalDateTime.of(2026, 11, 10, 10, 45);
        ParkingExitSummary summaryCar2 = service.processExit(ticketCar2.getTicketNumber(), timeCar2Out);
        System.out.printf("Departed: %-12s | Duration: %3d min | Parking: Rs %6.2f | Charging: Rs %5.2f | Total: Rs %6.2f | Released: [%d, %d]\n",
                summaryCar2.vehicle().getRegistrationNumber(), summaryCar2.durationMinutes(),
                summaryCar2.parkingFee(), summaryCar2.chargingFee(), summaryCar2.totalFee(),
                summaryCar2.releasedFloor(), summaryCar2.releasedSlot());

        // Exit 5: Overflow Car exits at 10:50 (Duration: 25 min -> 1 billable hour @ Rs 30 = Rs 30.0)
        LocalDateTime timeOverflowOut = LocalDateTime.of(2026, 11, 10, 10, 50);
        ParkingExitSummary summaryOverflow = service.processExit(ticketOverflow.getTicketNumber(), timeOverflowOut);
        System.out.printf("Departed: %-12s | Duration: %3d min | Parking: Rs %6.2f | Charging: Rs %5.2f | Total: Rs %6.2f | Released: [%d, %d]\n",
                summaryOverflow.vehicle().getRegistrationNumber(), summaryOverflow.durationMinutes(),
                summaryOverflow.parkingFee(), summaryOverflow.chargingFee(), summaryOverflow.totalFee(),
                summaryOverflow.releasedFloor(), summaryOverflow.releasedSlot());

        // ---------------------------------------------------------------------
        // 5. Daily Collection Reconciliation
        // ---------------------------------------------------------------------
        System.out.println("\n--- 5. Daily Collection Reconciliation ---");
        double handCalcParking = 20.0 + 60.0 + 90.0 + 30.0 + 30.0; // 230.0
        double handCalcCharging = 180.0;                             // 180.0
        double handCalcTotal = handCalcParking + handCalcCharging;   // 410.0

        double progParking = service.getDailyParkingCollection();
        double progCharging = service.getDailyChargingCollection();
        double progTotal = service.getTotalDailyCollection();

        System.out.printf("Hand-Calculated Revenue : Parking = Rs %6.2f, Charging = Rs %6.2f, Total = Rs %6.2f\n",
                handCalcParking, handCalcCharging, handCalcTotal);
        System.out.printf("Program Recorded Revenue: Parking = Rs %6.2f, Charging = Rs %6.2f, Total = Rs %6.2f\n",
                progParking, progCharging, progTotal);
        System.out.println("Financial Audit Match   : " + (handCalcTotal == progTotal ? "PASSED (100% RECONCILED)" : "FAILED"));

        // ---------------------------------------------------------------------
        // 6. Illustrative Lookup Microbenchmark (Array vs ArrayList vs HashMap)
        // ---------------------------------------------------------------------
        System.out.println("\n--- 6. Illustrative Lookup Microbenchmark (N = 1000 active tickets) ---");
        runLookupBenchmark(1000);

        System.out.println("\n================================================================================");
        System.out.println("                  TASK 11 INTEGRATION DEMO COMPLETE                             ");
        System.out.println("================================================================================");
    }

    /**
     * Empirical lookup benchmark comparing sequential array scan, ArrayList sequential scan,
     * and HashMap direct key lookup.
     *
     * @param datasetSize number of synthetic tickets populated in each structure
     */
    private static void runLookupBenchmark(int datasetSize) {
        // 1. Prepare identical datasets across the three structures
        ParkingTicket[] arrayStorage = new ParkingTicket[datasetSize];
        List<ParkingTicket> listStorage = new ArrayList<>(datasetSize);
        Map<String, ParkingTicket> mapStorage = new HashMap<>(datasetSize);

        String targetReg = "REG-" + (datasetSize * 85 / 100); // Target located at 85% depth (average-case)

        for (int i = 0; i < datasetSize; i++) {
            String reg = "REG-" + i;
            String ticketId = "TKT-" + (1000 + i);
            ParkingTicket ticket = new ParkingTicket(ticketId, reg, "CAR", 0, i, LocalDateTime.now());
            arrayStorage[i] = ticket;
            listStorage.add(ticket);
            mapStorage.put(reg, ticket);
        }

        int iterations = 20_000;
        int warmup = 5_000;

        // Warm-up HotSpot JIT compiler
        for (int w = 0; w < warmup; w++) {
            searchArray(arrayStorage, targetReg);
            searchList(listStorage, targetReg);
            mapStorage.get(targetReg);
        }

        // Measure Array Sequential Lookup
        long startArray = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            searchArray(arrayStorage, targetReg);
        }
        long timeArrayNs = (System.nanoTime() - startArray) / iterations;

        // Measure ArrayList Sequential Lookup
        long startList = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            searchList(listStorage, targetReg);
        }
        long timeListNs = (System.nanoTime() - startList) / iterations;

        // Measure HashMap Lookup
        long startMap = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            mapStorage.get(targetReg);
        }
        long timeMapNs = (System.nanoTime() - startMap) / iterations;

        System.out.printf("Dataset Size    : %d active tickets (identical instances stored across all 3 structures)\n", datasetSize);
        System.out.printf("Lookup Target   : License plate '%s' (equivalent search key at ~85%% array depth)\n", targetReg);
        System.out.printf("Warm-up Runs    : %d iterations (HotSpot C2 JIT warm-up)\n", warmup);
        System.out.printf("Measurement     : %d timed iterations\n", iterations);
        System.out.println("--------------------------------------------------------------------------------");
        System.out.printf("1. ParkingTicket[] (Linear Scan) : ~%5d ns / lookup | Theoretical: O(n) [%d comparisons]\n",
                timeArrayNs, datasetSize * 85 / 100);
        System.out.printf("2. ArrayList (Linear Scan)       : ~%5d ns / lookup | Theoretical: O(n) [%d comparisons]\n",
                timeListNs, datasetSize * 85 / 100);
        System.out.printf("3. HashMap (Hash Key Lookup)     : ~%5d ns / lookup | Theoretical: O(1) [1 bucket lookup]\n",
                timeMapNs);
        System.out.println("--------------------------------------------------------------------------------");
        System.out.println("Note: This illustrative microbenchmark demonstrates asymptotic O(n) vs O(1) trends.");
        System.out.println("Actual wall-clock speedup depends on JVM heap sizing, Compressed OOPs, and CPU caches.");
        System.out.println("For small lots (N <= 100), contiguous array cache locality remains highly competitive.");
    }

    private static ParkingTicket searchArray(ParkingTicket[] array, String regNo) {
        for (ParkingTicket ticket : array) {
            if (ticket != null && ticket.getVehicleRegNo().equals(regNo)) {
                return ticket;
            }
        }
        return null;
    }

    private static ParkingTicket searchList(List<ParkingTicket> list, String regNo) {
        for (ParkingTicket ticket : list) {
            if (ticket.getVehicleRegNo().equals(regNo)) {
                return ticket;
            }
        }
        return null;
    }
}
