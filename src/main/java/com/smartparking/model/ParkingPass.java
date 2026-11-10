package com.smartparking.model;

import java.time.LocalDateTime;

/**
 * Interface defining the contract for parking access passes in the Campus Smart-Parking System.
 *
 * Implements Task 12 Day-4 Design Challenge abstraction:
 * - Decouples user institutional entitlement (free parking privilege) from physical vehicle identity.
 * - Supports multiple pass variants: Staff/Student {@link MonthlyPass} and {@link VisitorOneDayPass}.
 */
public interface ParkingPass {

    /**
     * Returns the unique identifier of the pass (e.g. PASS-M-1001, PASS-V-2001).
     *
     * @return non-null, non-blank pass identifier
     */
    String getPassId();

    /**
     * Returns the identifier of the authorized pass holder (staff ID, student ID, or visitor name).
     *
     * @return non-null holder identifier
     */
    String getHolderId();

    /**
     * Returns the authorized vehicle registration number bound to this pass.
     *
     * @return normalized uppercase license plate
     */
    String getVehicleRegNo();

    /**
     * Returns the pass category identifier (e.g. "MONTHLY_PASS", "VISITOR_ONE_DAY_PASS").
     *
     * @return pass type string
     */
    String getPassType();

    /**
     * Verifies whether this pass is active and valid at the specified date and time.
     *
     * @param timestamp timestamp to evaluate
     * @return true if valid and active, false otherwise
     */
    boolean isValidAt(LocalDateTime timestamp);

    /**
     * Indicates whether base parking occupancy fees are waived for holders of this pass.
     * Default returns true; individual pass types can specialize fee waiver rules.
     *
     * Note: EV electricity consumption charges remain strictly separate and chargeable.
     *
     * @return true if base parking fee is waived (Rs 0.0), false otherwise
     */
    default boolean isParkingFeeWaived() {
        return true;
    }
}
