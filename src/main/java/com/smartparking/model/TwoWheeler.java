package com.smartparking.model;

/**
 * Represents a two-wheeled vehicle (motorcycle, scooter, moped).
 *
 * Demonstrates:
 * - Direct inheritance from the abstract Vehicle superclass (Hierarchical inheritance)
 * - Constructor chaining to the superclass via super(...)
 * - Addition of specialized subclass state (hasHelmetStorage)
 * - Reuse of inherited behaviour via super.getVehicleInfo()
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
