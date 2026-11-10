package com.smartparking.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

/**
 * Visitor One-Day Pass granting authorized visitors parking fee exemption for a designated day.
 *
 * Implements Task 12 Day-4 Design Challenge Requirement C:
 * - Bound to visitor identification and vehicle license plate.
 * - Validity period: valid for the calendar date (00:00:00 to 23:59:59.999999999) or precise window.
 * - Validated during vehicle entry and vehicle departure.
 * - Enforces fee separation: Base parking fee is waived (Rs 0.0), while EV charging remains chargeable.
 */
public class VisitorOneDayPass implements ParkingPass {

    public static final String PASS_TYPE = "VISITOR_ONE_DAY_PASS";

    private final String passId;
    private final String visitorId;
    private final String vehicleRegNo;
    private final LocalDateTime validFrom;
    private final LocalDateTime validTo;

    /**
     * Calendar-day constructor. The pass is valid for the entire calendar date from 00:00:00
     * until 23:59:59.999999999.
     *
     * @param passId       unique pass identifier (non-null, non-blank)
     * @param visitorId    visitor name or identification (non-null, non-blank)
     * @param vehicleRegNo authorized vehicle registration number (non-null, non-blank)
     * @param validDate    calendar date on which the pass is active (non-null)
     */
    public VisitorOneDayPass(String passId, String visitorId, String vehicleRegNo, LocalDate validDate) {
        if (passId == null || passId.isBlank()) {
            throw new IllegalArgumentException("Pass ID cannot be null or blank.");
        }
        if (visitorId == null || visitorId.isBlank()) {
            throw new IllegalArgumentException("Visitor ID cannot be null or blank.");
        }
        if (vehicleRegNo == null || vehicleRegNo.isBlank()) {
            throw new IllegalArgumentException("Vehicle registration cannot be null or blank.");
        }
        if (validDate == null) {
            throw new IllegalArgumentException("Valid date cannot be null.");
        }

        this.passId = passId.trim().toUpperCase();
        this.visitorId = visitorId.trim().toUpperCase();
        this.vehicleRegNo = vehicleRegNo.trim().toUpperCase();
        this.validFrom = validDate.atStartOfDay();
        this.validTo = validDate.atTime(LocalTime.MAX);
    }

    /**
     * Precise window constructor for custom 24-hour validity periods.
     *
     * @param passId       unique pass identifier
     * @param visitorId    visitor identifier
     * @param vehicleRegNo vehicle registration
     * @param validFrom    window start timestamp
     * @param validTo      window end timestamp (must be strictly after validFrom)
     */
    public VisitorOneDayPass(String passId, String visitorId, String vehicleRegNo,
                             LocalDateTime validFrom, LocalDateTime validTo) {
        if (passId == null || passId.isBlank()) {
            throw new IllegalArgumentException("Pass ID cannot be null or blank.");
        }
        if (visitorId == null || visitorId.isBlank()) {
            throw new IllegalArgumentException("Visitor ID cannot be null or blank.");
        }
        if (vehicleRegNo == null || vehicleRegNo.isBlank()) {
            throw new IllegalArgumentException("Vehicle registration cannot be null or blank.");
        }
        if (validFrom == null || validTo == null) {
            throw new IllegalArgumentException("Validity timestamps cannot be null.");
        }
        if (!validTo.isAfter(validFrom)) {
            throw new IllegalArgumentException("validTo (" + validTo + ") must be strictly after validFrom (" + validFrom + ").");
        }

        this.passId = passId.trim().toUpperCase();
        this.visitorId = visitorId.trim().toUpperCase();
        this.vehicleRegNo = vehicleRegNo.trim().toUpperCase();
        this.validFrom = validFrom;
        this.validTo = validTo;
    }

    @Override
    public String getPassId() {
        return passId;
    }

    @Override
    public String getHolderId() {
        return visitorId;
    }

    public String getVisitorId() {
        return visitorId;
    }

    @Override
    public String getVehicleRegNo() {
        return vehicleRegNo;
    }

    @Override
    public String getPassType() {
        return PASS_TYPE;
    }

    public LocalDateTime getValidFrom() {
        return validFrom;
    }

    public LocalDateTime getValidTo() {
        return validTo;
    }

    @Override
    public boolean isValidAt(LocalDateTime timestamp) {
        if (timestamp == null) {
            return false;
        }
        return !timestamp.isBefore(validFrom) && !timestamp.isAfter(validTo);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof VisitorOneDayPass other)) return false;
        return passId.equals(other.passId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(passId);
    }

    @Override
    public String toString() {
        return "VisitorOneDayPass{" +
                "passId='" + passId + '\'' +
                ", visitorId='" + visitorId + '\'' +
                ", vehicleRegNo='" + vehicleRegNo + '\'' +
                ", validFrom=" + validFrom +
                ", validTo=" + validTo +
                '}';
    }
}
