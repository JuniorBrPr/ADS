package models;

/**
 * Represents a violation of a car that has entered an environmentally restricted zone
 * at a certain date and time.
 * A violation is associated with a car , a city and a number of offences.
 */
public class Violation {
    private final Car car;
    private final String city;
    private int offencesCount;

    public Violation(Car car, String city) {
        this.car = car;
        this.city = city;
        this.offencesCount = 1;
    }

    /**
     * Compares two violations by license plate and city.
     *
     * @param v1 The first violation.
     * @param v2 The second violation.
     * @return 0 if the license plates and cities are equal, a negative integer if v1 is less than v2, a positive
     * integer if v1 is greater than v2.
     */
    public static int compareByLicensePlateAndCity(Violation v1, Violation v2) {
        int result = v1.car.getLicensePlate().compareTo(v2.car.getLicensePlate());
        if (result == 0) {
            return v1.city.compareTo(v2.city);
        } else {
            return result;
        }
    }

    /**
     * Compares two violations by offences count.
     *
     * @param v1 The first violation.
     * @param v2 The second violation.
     * @return 0 if the offences counts are equal, a negative integer if v2 is more than v1, a positive integer if v1 is
     * more than v2.
     */
    public static int compareByOffencesCount(Violation v1, Violation v2) {
        return v2.offencesCount - v1.offencesCount;
    }

    /**
     * Aggregates this violation with the other violation by adding their counts and nullifying identifying attributes
     * car and/or city that do not match identifying attributes that match are retained in the result.
     * This method can be used for aggregating violations applying different grouping criteria.
     *
     * @param other The other violation to aggregate with this one.
     * @return A new violation with the accumulated offencesCount and matching identifying attributes.
     */
    public Violation combineOffencesCounts(Violation other) {
        Violation combinedViolation = new Violation(
                //nullify the car and/or city if they do not match
                this.car != null && this.car.equals(other.car) ? this.car : null,
                this.city != null && this.city.equals(other.city) ? this.city : null);

        // add the offences counts of both original violations
        combinedViolation.setOffencesCount(this.offencesCount + other.offencesCount);

        return combinedViolation;
    }

    public Car getCar() {
        return car;
    }

    public String getCity() {
        return city;
    }

    public int getOffencesCount() {
        return offencesCount;
    }

    public void setOffencesCount(int offencesCount) {
        this.offencesCount = offencesCount;
    }

    @Override
    public String toString() {
        return String.format("%s/%s/%d",
                car == null ? "null" : car.getLicensePlate(),
                city == null ? "null" : city,
                offencesCount);
    }
}
