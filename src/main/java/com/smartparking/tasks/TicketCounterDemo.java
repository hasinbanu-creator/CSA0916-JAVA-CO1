package com.smartparking.tasks;

import com.smartparking.model.ParkingTicket;
import java.time.LocalDateTime;

/**
 * Task 5: Static Members and Object Life Cycle Demonstration.
 *
 * Demonstrates:
 * 1. A static ticket counter shared across multiple facility/gate instances within
 *    the lifetime of the same JVM and ClassLoader.
 * 2. Static methods and static initialization blocks with justified purpose.
 * 3. The alternative design comparing static counters with facility-instance counters.
 * 4. Java pass-by-value semantics for object references (mutation vs. reassignment).
 * 5. The complete object lifecycle of a ParkingTicket from heap allocation,
 *    through active session and departure completion, to dereferencing and GC eligibility.
 */
public class TicketCounterDemo {

    // Class-level shared state: belongs to the class and is shared among instances
    // loaded by the same ClassLoader. Exact physical storage is JVM-implementation-dependent.
    private static int sharedTicketCounter;
    private static final String DEFAULT_PREFIX;

    /*
     * Static Initialization Block.
     * Executes once when the class is loaded into memory by the JVM ClassLoader.
     * Used here to establish a consistent ticketing prefix and initial sequence offset.
     */
    static {
        DEFAULT_PREFIX = "TKT-";
        sharedTicketCounter = 1000;
    }

    /**
     * Static utility method to generate the next unique sequential ticket ID.
     * Increments the shared class counter and formats the identifier.
     * Guarantees sequence coordination within the current JVM process.
     *
     * @return uniquely generated ticket identifier string
     */
    public static synchronized String generateNextTicketId() {
        sharedTicketCounter++;
        return DEFAULT_PREFIX + sharedTicketCounter;
    }

    /**
     * Returns the current value of the shared ticket counter.
     *
     * @return current shared counter value
     */
    public static synchronized int getSharedTicketCounter() {
        return sharedTicketCounter;
    }

    /**
     * Package-private helper to reset the shared counter strictly for unit test isolation.
     * Not exposed in the public production API to prevent unauthorized counter resets
     * and sequence collisions in production code.
     *
     * @param baseline starting sequence value
     */
    static synchronized void resetSharedCounterForTesting(int baseline) {
        sharedTicketCounter = baseline;
    }

    /**
     * Demonstrates passing an object reference to a method.
     * Java passes the reference *by value* (a copy of the memory address).
     * Modifying the object's internal state through this reference mutates the
     * single heap object, so changes are directly visible to the caller.
     *
     * @param ticket        reference to the active ParkingTicket
     * @param departureTime exit timestamp to record
     */
    public static void completeParkingSession(ParkingTicket ticket, LocalDateTime departureTime) {
        if (ticket == null) {
            throw new IllegalArgumentException("Ticket reference cannot be null.");
        }
        ticket.closeTicket(departureTime);
    }

    /**
     * Demonstrates the boundary of pass-by-value semantics.
     * Reassigning the local parameter reference inside the method only changes
     * the local stack variable; the caller's reference variable remains unchanged.
     *
     * @param ticket      local parameter reference to be reassigned
     * @param replacement new object reference
     */
    public static void attemptReferenceReassignment(ParkingTicket ticket, ParkingTicket replacement) {
        // This reassignment only affects the local parameter variable on the stack frame.
        // It has ZERO effect on the reference held by the caller.
        ticket = replacement;
    }

    /**
     * Demonstrates returning an object reference from a method.
     * The method returns a reference value pointing to an archival snapshot on the heap.
     *
     * @param source active or completed ticket to duplicate
     * @return reference to the newly instantiated archival snapshot
     */
    public static ParkingTicket issueArchivalCopy(ParkingTicket source) {
        if (source == null) {
            throw new IllegalArgumentException("Source ticket cannot be null.");
        }
        return new ParkingTicket(source);
    }

    /**
     * Minimal representation of a facility entry gate to demonstrate multi-instance
     * interactions with static vs. instance counters.
     */
    public static class FacilityGate {
        private final String gateName;
        private int instanceTicketCounter; // Alternative design: instance-level counter

        public FacilityGate(String gateName) {
            if (gateName == null || gateName.isBlank()) {
                throw new IllegalArgumentException("Gate name cannot be blank.");
            }
            this.gateName = gateName.trim();
            this.instanceTicketCounter = 0;
        }

        public String getGateName() {
            return gateName;
        }

        public int getInstanceTicketCounter() {
            return instanceTicketCounter;
        }

        /**
         * Primary Design: Issues a ticket using the shared static counter.
         * Guarantees unique ticket IDs across all gates in the current JVM.
         */
        public ParkingTicket issueTicketShared(String regNo, String vehicleType, int floor, int slot) {
            String ticketId = TicketCounterDemo.generateNextTicketId();
            return new ParkingTicket(ticketId, regNo, vehicleType, floor, slot);
        }

        /**
         * Alternative Design: Issues a ticket using an instance counter local to this facility.
         * Generates independent sequences per gate.
         */
        public ParkingTicket issueTicketInstance(String regNo, String vehicleType, int floor, int slot) {
            this.instanceTicketCounter++;
            String ticketId = this.gateName + "-" + this.instanceTicketCounter;
            return new ParkingTicket(ticketId, regNo, vehicleType, floor, slot);
        }
    }

    /**
     * Execution driver demonstrating all Task 5 concepts.
     */
    public static void main(String[] args) {
        System.out.println("=== Task 5: Static Members and Object Life Cycle Demo ===");

        // 1. Static Counter Shared Across Multiple Facilities (North & South Gates)
        System.out.println("\n--- 1. Shared Static Ticket Counter ---");
        FacilityGate northGate = new FacilityGate("North-Gate");
        FacilityGate southGate = new FacilityGate("South-Gate");

        ParkingTicket t1 = northGate.issueTicketShared("TN01AA1111", "CAR", 0, 1);
        System.out.println("Ticket 1 (North Gate): ID = " + t1.getTicketNumber() + ", Counter = " + getSharedTicketCounter());

        ParkingTicket t2 = southGate.issueTicketShared("TN02BB2222", "TWO_WHEELER", 0, 2);
        System.out.println("Ticket 2 (South Gate): ID = " + t2.getTicketNumber() + ", Counter = " + getSharedTicketCounter());

        ParkingTicket t3 = northGate.issueTicketShared("TN03CC3333", "CAR", 1, 0);
        System.out.println("Ticket 3 (North Gate): ID = " + t3.getTicketNumber() + ", Counter = " + getSharedTicketCounter());
        System.out.println("Notice: Both distinct gate instances share the single static counter sequence in this JVM.");

        // 2. Alternative Concept: Facility-Instance Counter
        System.out.println("\n--- 2. Alternative: Facility-Instance Counter ---");
        ParkingTicket altNorth1 = northGate.issueTicketInstance("TN01AA1111", "CAR", 0, 1);
        ParkingTicket altSouth1 = southGate.issueTicketInstance("TN02BB2222", "TWO_WHEELER", 0, 2);
        System.out.println("Alt Ticket North Gate: ID = " + altNorth1.getTicketNumber() + " (Gate Counter = " + northGate.getInstanceTicketCounter() + ")");
        System.out.println("Alt Ticket South Gate: ID = " + altSouth1.getTicketNumber() + " (Gate Counter = " + southGate.getInstanceTicketCounter() + ")");

        // 3. Passing Object References: In-Place Mutation
        System.out.println("\n--- 3. Passing Object References: In-Place State Mutation ---");
        System.out.println("Before departure: active = " + t1.isActive() + ", exitTime = " + t1.getExitTime());
        LocalDateTime exitTime = LocalDateTime.now().plusHours(2);
        completeParkingSession(t1, exitTime);
        System.out.println("After completeParkingSession(t1): active = " + t1.isActive() + ", exitTime = " + t1.getExitTime());

        // 4. Passing Object References: Reassignment Immunity
        System.out.println("\n--- 4. Passing Object References: Reassignment Immunity ---");
        ParkingTicket replacementTicket = new ParkingTicket("TKT-REPLACE", "TN99ZZ9999", "CAR", 2, 4);
        ParkingTicket originalReference = t1;
        attemptReferenceReassignment(t1, replacementTicket);
        System.out.println("Original ticket ID after attemptReferenceReassignment: " + t1.getTicketNumber());
        System.out.println("Reference intact (t1 == originalReference): " + (t1 == originalReference));

        // 5. Returning Object References
        System.out.println("\n--- 5. Returning Object References ---");
        ParkingTicket archival = issueArchivalCopy(t1);
        System.out.println("Archival ticket copy created: ID = " + archival.getTicketNumber() + ", Active = " + archival.isActive());
        System.out.println("Distinct heap objects (archival != t1): " + (archival != t1));

        // 6. Object Life Cycle & Garbage Collection Eligibility
        System.out.println("\n--- 6. Object Life Cycle & Garbage Collection Eligibility ---");
        System.out.println("Phase 1: Object allocated on heap with 'new' -> strongly reachable via variable 't3'.");
        System.out.println("Phase 2: Vehicle departs -> ticket marked completed (isActive=false), but object REMAINS referenced by 't3'. It is NOT eligible for GC.");
        System.out.println("Phase 3: Reference cleared (t3 = null) -> object has no GC roots and becomes ELIGIBLE for GC.");
        t3 = null; // Dereferencing
        System.out.println("Variable 't3' set to null. The heap object is now unreferenced and eligible for GC when JVM GC runs.");
    }
}
