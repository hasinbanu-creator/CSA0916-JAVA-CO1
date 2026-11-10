package com.smartparking.model;

/**
 * Represents a two-wheeled vehicle (motorcycle, scooter, moped).
 *
 * Demonstrates Task 7 concepts:
 * - Overriding calculateFee(long) to implement specialized two-wheeler tariff
 * - Use of the @Override annotation for compiler verification
 * - Overriding toString() to augment base vehicle description with helmet storage details
 */
public class TwoWheeler extends Vehicle {
    private final boolean hasHelmetStorage;

    /**
     * Parameterized constructor for TwoWheeler.
     * Invokes the superclass constructor with the explicit category "TWO_WHEELER".
     *
     * @param registrationNumber unique license plate number
     * @param hasHelmetStorage   whether the vehicle is equipped with secure helmet storage
     */
    public TwoWheeler(String registrationNumber, boolean hasHelmetStorage) {
        super(registrationNumber, "TWO_WHEELER");
        this.hasHelmetStorage = hasHelmetStorage;
    }

    /**
     * Returns whether the two-wheeler has helmet storage.
     *
     * @return true if helmet storage is available, false otherwise
     */
    public boolean hasHelmetStorage() {
        return hasHelmetStorage;
    }

    /**
     * Calculates the parking fee for a two-wheeler stay.
     * Charges Rs 10.0 per started hour.
     *
     * @param durationMinutes parking stay duration in minutes (must be non-negative)
     * @return calculated parking fee in INR
     */
    @Override
    public double calculateFee(long durationMinutes) {
        long billableHours = ParkingTariff.calculateStartedHours(durationMinutes);
        return billableHours * ParkingTariff.TWO_WHEELER_RATE_PER_HOUR;
    }

    /**
     * Specialized display method demonstrating invocation of inherited superclass behaviour.
     *
     * @return complete two-wheeler details
     */
    public String getDetailedInfo() {
        return super.getVehicleInfo() + ", Helmet Storage: " + (hasHelmetStorage ? "Yes" : "No");
    }

    @Override
    public String toString() {
        return "TwoWheeler[" + super.toString() + ", helmetStorage=" + hasHelmetStorage + "]";
    }
}
