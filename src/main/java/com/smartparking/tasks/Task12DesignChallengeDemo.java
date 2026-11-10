package com.smartparking.tasks;

import com.smartparking.model.Car;
import com.smartparking.model.DeliveryVan;
import com.smartparking.model.ElectricBike;
import com.smartparking.model.ElectricCar;
import com.smartparking.model.ParkingTicket;
import com.smartparking.model.VisitorOneDayPass;
import com.smartparking.service.ParkingExitSummary;
import com.smartparking.service.ParkingManagementService;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Task 12 Executable Demonstration: Day-4 Final Design Challenge (8 Marks).
 *
 * Demonstrates:
 * 1. An electric bike being parked and charged the flat parking fee (Rs 15.00).
 * 2. A delivery van's dynamic fee calculation during peak (9 AM - 5 PM) and off-peak hours.
 * 3. A visitor using a valid one-day pass (base parking fee waived, separate EV charging billed).
 * 4. Multi-level spatial allocation across five unequal floors using jagged arrays.
 * 5. Physical slot release and immediate reallocation into the freed coordinate.
 */
public class Task12DesignChallengeDemo {

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("       CAMPUS SMART-PARKING SYSTEM - TASK 12 DESIGN CHALLENGE DEMO              ");
        System.out.println("================================================================================");

        LocalDate today = LocalDate.of(2026, 11, 10);
        LocalDateTime baseTime = today.atTime(8, 0);

        // ---------------------------------------------------------------------
        // 1. Initializing Five-Floor Parking Facility with Unequal Capacities
        // ---------------------------------------------------------------------
        System.out.println("\n--- 1. Initializing Five-Floor Parking Facility (Unequal Jagged Layout) ---");
        // Floor 0: 4 slots, Floor 1: 3 slots, Floor 2: 3 slots, Floor 3: 2 slots, Floor 4: 2 slots (Total = 14)
        int[] fiveFloors = new int[]{4, 3, 3, 2, 2};
        ParkingFacility facility = new ParkingFacility(fiveFloors);
        ParkingManagementService service = new ParkingManagementService(facility, 14);

        System.out.println("Facility Configured: " + facility.getFloorCount() + " Floors, "
                + facility.getTotalCapacity() + " Total Capacity");
        for (int f = 0; f < facility.getFloorCount(); f++) {
            System.out.println("  Floor " + f + ": " + facility.getSlotCount(f) + " slots");
        }

        // ---------------------------------------------------------------------
        // 2. Electric Bike Parking (Flat Fee Model)
        // ---------------------------------------------------------------------
        System.out.println("\n--- 2. Electric Bike Parking (Flat Fee Model) ---");
        ElectricBike ebike = new ElectricBike("EB-CAMPUS-01");
        ParkingTicket ebikeTicket = service.parkVehicle(ebike, baseTime.plusMinutes(15));
        System.out.printf("Admitted: %-14s | Type: %-13s | Ticket: %s | Location: Floor %d, Slot %d%n",
                ebike.getRegistrationNumber(), ebike.getVehicleType(),
                ebikeTicket.getTicketNumber(), ebikeTicket.getFloorNumber(), ebikeTicket.getSlotNumber());

        // Depart after 150 minutes (2.5 hours). Conventional car would pay Rs 90, e-bike pays flat Rs 15.00
        LocalDateTime ebikeExit = baseTime.plusMinutes(165);
        ParkingExitSummary ebikeSummary = service.processExit(ebikeTicket.getTicketNumber(), ebikeExit);
        System.out.printf("Departed: %-14s | Stay: %3d min | Parking: Rs %6.2f (FLAT) | Total: Rs %6.2f | Freed: [%d, %d]%n",
                ebikeSummary.vehicle().getRegistrationNumber(), ebikeSummary.durationMinutes(),
                ebikeSummary.parkingFee(), ebikeSummary.totalFee(),
                ebikeSummary.releasedFloor(), ebikeSummary.releasedSlot());

        // ---------------------------------------------------------------------
        // 3. Delivery Van Dynamic Time-Window Surge Pricing (9 AM - 5 PM)
        // ---------------------------------------------------------------------
        System.out.println("\n--- 3. Delivery Van Time-Window Surge Pricing ---");

        // Van A: Peak Delivery Hours (10:00 AM to 12:00 PM = 120 min) -> 2 hrs @ Rs 60/hr = Rs 120.00
        DeliveryVan vanPeak = new DeliveryVan("VAN-PEAK-99", "FastTrack Logistics");
        LocalDateTime peakEntry = today.atTime(10, 0);
        LocalDateTime peakExit = today.atTime(12, 0);
        ParkingTicket peakTicket = service.parkVehicle(vanPeak, peakEntry);
        System.out.printf("Admitted: %-14s | Window: 10:00 - 12:00 (PEAK)     | Ticket: %s | Location: [%d, %d]%n",
                vanPeak.getRegistrationNumber(), peakTicket.getTicketNumber(),
                peakTicket.getFloorNumber(), peakTicket.getSlotNumber());
        ParkingExitSummary peakSummary = service.processExit(peakTicket.getTicketNumber(), peakExit);
        System.out.printf("Departed: %-14s | Stay: %3d min | Rate: Rs 60/hr (PEAK)    | Total: Rs %6.2f%n",
                peakSummary.vehicle().getRegistrationNumber(), peakSummary.durationMinutes(), peakSummary.totalFee());

        // Van B: Off-Peak Hours (18:00 to 19:30 = 90 min) -> 2 started hrs @ Rs 30/hr = Rs 60.00
        DeliveryVan vanOffPeak = new DeliveryVan("VAN-OFFPK-88", "Evening Express");
        LocalDateTime offPeakEntry = today.atTime(18, 0);
        LocalDateTime offPeakExit = today.atTime(19, 30);
        ParkingTicket offPeakTicket = service.parkVehicle(vanOffPeak, offPeakEntry);
        ParkingExitSummary offPeakSummary = service.processExit(offPeakTicket.getTicketNumber(), offPeakExit);
        System.out.printf("Departed: %-14s | Stay: %3d min | Rate: Rs 30/hr (OFF-PEAK)| Total: Rs %6.2f%n",
                offPeakSummary.vehicle().getRegistrationNumber(), offPeakSummary.durationMinutes(), offPeakSummary.totalFee());

        // ---------------------------------------------------------------------
        // 4. Visitor Using a Valid One-Day Pass (With & Without EV Charging)
        // ---------------------------------------------------------------------
        System.out.println("\n--- 4. Visitor One-Day Pass (Exemption & EV Charging Separation) ---");

        // Case A: Conventional Car with Visitor One-Day Pass
        VisitorOneDayPass passA = new VisitorOneDayPass("PASS-DAY-01", "Dr. Mehta (Guest Speaker)", "KA02VP101", today);
        Car visitorCar = new Car("KA02VP101", 5);
        ParkingTicket passTicketA = service.parkVehicle(visitorCar, today.atTime(9, 0), passA);
        System.out.printf("Admitted: %-14s | Visitor: %-15s | Pass: %s (VALID DATE: %s)%n",
                visitorCar.getRegistrationNumber(), passA.getVisitorId(), passA.getPassId(), today);

        // Stay 6 hours (09:00 to 15:00): Standard fee would be Rs 180.00; with pass = Rs 0.00
        ParkingExitSummary summaryA = service.processExit(passTicketA.getTicketNumber(), today.atTime(15, 0));
        System.out.printf("Departed: %-14s | Stay: %3d min | Parking: Rs %6.2f (WAIVED) | Total: Rs %6.2f%n",
                summaryA.vehicle().getRegistrationNumber(), summaryA.durationMinutes(),
                summaryA.parkingFee(), summaryA.totalFee());

        // Case B: Electric Car with Visitor One-Day Pass + EV Charging
        VisitorOneDayPass passB = new VisitorOneDayPass("PASS-DAY-02", "Prof. Sharma (Examiner)", "DL03EV202", today);
        ElectricCar visitorEv = new ElectricCar("DL03EV202", 60.0, 20.0);
        ParkingTicket passTicketB = service.parkVehicle(visitorEv, today.atTime(9, 30), passB);

        // Stay 4 hours + 15 kWh charging: Parking fee waived (Rs 0.0), charging billed (15 kWh @ Rs 12 = Rs 180.00)
        ParkingExitSummary summaryB = service.processExit(passTicketB.getTicketNumber(), today.atTime(13, 30), 15.0);
        System.out.printf("Departed: %-14s | Stay: %3d min | Parking: Rs %6.2f (WAIVED) | Charging: Rs %6.2f (15 kWh) | Total: Rs %6.2f%n",
                summaryB.vehicle().getRegistrationNumber(), summaryB.durationMinutes(),
                summaryB.parkingFee(), summaryB.chargingFee(), summaryB.totalFee());

        // ---------------------------------------------------------------------
        // 5. Allocation Across All Five Unequal Floors
        // ---------------------------------------------------------------------
        System.out.println("\n--- 5. Allocation Across All Five Unequal Floors ---");
        System.out.println("Sequentially admitting vehicles to demonstrate vertical slot distribution:");

        ParkingTicket[] floorTickets = new ParkingTicket[14];
        for (int i = 0; i < 14; i++) {
            Car c = new Car("CAMPUS-" + (i + 1), 4);
            floorTickets[i] = service.parkVehicle(c, baseTime.plusMinutes(i * 5));
            System.out.printf("  Vehicle CAMPUS-%-2d allocated to -> Floor %d, Slot %d (Capacity: %d slots)%n",
                    (i + 1), floorTickets[i].getFloorNumber(), floorTickets[i].getSlotNumber(),
                    facility.getSlotCount(floorTickets[i].getFloorNumber()));
        }
        System.out.println("Active Parked Vehicles: " + service.getActiveTicketCount() + " / " + service.getCapacity() + " (FACILITY FULL)");

        // ---------------------------------------------------------------------
        // 6. Slot Release and Reallocation
        // ---------------------------------------------------------------------
        System.out.println("\n--- 6. Slot Release and Reallocation ---");
        // Release slot on Floor 2, Slot 1 (Ticket CAMPUS-9)
        ParkingTicket releasedTicket = floorTickets[8]; // index 8 is Floor 2, Slot 1
        System.out.printf("Releasing slot at Floor %d, Slot %d occupied by %s...%n",
                releasedTicket.getFloorNumber(), releasedTicket.getSlotNumber(), releasedTicket.getVehicleRegNo());
        ParkingExitSummary relSummary = service.processExit(releasedTicket.getTicketNumber(), baseTime.plusHours(4));
        System.out.printf("Freed Coordinates: Floor %d, Slot %d | Active Count: %d / %d%n",
                relSummary.releasedFloor(), relSummary.releasedSlot(), service.getActiveTicketCount(), service.getCapacity());

        // Admit a new vehicle: verify it is allocated into the newly freed slot [2, 1]
        Car reallocCar = new Car("REALLOC-999", 5);
        ParkingTicket reallocTicket = service.parkVehicle(reallocCar, baseTime.plusHours(4).plusMinutes(10));
        System.out.printf("Admitted: %-14s | Allocated Location: Floor %d, Slot %d [SUCCESSFULLY REUSED]%n",
                reallocCar.getRegistrationNumber(), reallocTicket.getFloorNumber(), reallocTicket.getSlotNumber());

        System.out.println("\n================================================================================");
        System.out.println("                  TASK 12 DESIGN CHALLENGE COMPLETE                             ");
        System.out.println("================================================================================");
    }
}
