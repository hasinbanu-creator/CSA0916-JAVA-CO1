package com.smartparking.model;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Objects;

/**
 * Monthly access pass for university staff and students.
 *
 * Implements Task 1 / Task 12 requirement: Staff and students holding a valid monthly pass park free of charge.
 */
public class MonthlyPass implements ParkingPass {

    public static final String PASS_TYPE = "MONTHLY_PASS";

    private final String passId;
    private final String holderId;
    private final String userRole; // "STAFF" or "STUDENT"
    private final String vehicleRegNo;
    private final YearMonth validMonth;

    public MonthlyPass(String passId, String holderId, String userRole, String vehicleRegNo, YearMonth validMonth) {
        if (passId == null || passId.isBlank()) {
            throw new IllegalArgumentException("Pass ID cannot be null or blank.");
        }
        if (holderId == null || holderId.isBlank()) {
            throw new IllegalArgumentException("Holder ID cannot be null or blank.");
        }
        if (userRole == null || (!userRole.trim().equalsIgnoreCase("STAFF") && !userRole.trim().equalsIgnoreCase("STUDENT"))) {
            throw new IllegalArgumentException("User role must be STAFF or STUDENT: " + userRole);
        }
        if (vehicleRegNo == null || vehicleRegNo.isBlank()) {
            throw new IllegalArgumentException("Vehicle registration cannot be null or blank.");
        }
        if (validMonth == null) {
            throw new IllegalArgumentException("Valid month cannot be null.");
        }

        this.passId = passId.trim().toUpperCase();
        this.holderId = holderId.trim().toUpperCase();
        this.userRole = userRole.trim().toUpperCase();
        this.vehicleRegNo = vehicleRegNo.trim().toUpperCase();
        this.validMonth = validMonth;
    }

    @Override
    public String getPassId() {
        return passId;
    }

    @Override
    public String getHolderId() {
        return holderId;
    }

    public String getUserRole() {
        return userRole;
    }

    @Override
    public String getVehicleRegNo() {
        return vehicleRegNo;
    }

    @Override
    public String getPassType() {
        return PASS_TYPE;
    }

    public YearMonth getValidMonth() {
        return validMonth;
    }

    @Override
    public boolean isValidAt(LocalDateTime timestamp) {
        if (timestamp == null) {
            return false;
        }
        return YearMonth.from(timestamp).equals(validMonth);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof MonthlyPass other)) return false;
        return passId.equals(other.passId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(passId);
    }

    @Override
    public String toString() {
        return "MonthlyPass{" +
                "passId='" + passId + '\'' +
                ", holderId='" + holderId + '\'' +
                ", userRole='" + userRole + '\'' +
                ", vehicleRegNo='" + vehicleRegNo + '\'' +
                ", validMonth=" + validMonth +
                '}';
    }
}
