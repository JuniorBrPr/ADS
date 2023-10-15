package models;

import java.time.LocalDate;

/**
 * Represents a car with its license plate, emission category, car type, fuel type and date of admission.
 * Uniquely identified by its license plate.
 */
public class Car implements Comparable<Car> {

    private final String licensePlate;
    private int emissionCategory;
    private CarType carType;
    private FuelType fuelType;
    private LocalDate dateOfAdmission;

    public enum CarType {
        Unknown,
        Car,
        Van,
        Truck,
        Coach
    }

    public enum FuelType {
        Unknown,
        Gasoline,
        Lpg,
        Diesel,
        Electric
    }

    public Car(String licensePlate) {
        this.licensePlate = licensePlate;
        this.emissionCategory = 0;
        this.carType = CarType.Unknown;
        this.fuelType = FuelType.Unknown;
        this.dateOfAdmission = LocalDate.EPOCH;
    }

    public Car(String licensePlate, int emissionCategory,
               CarType carType, FuelType fuelType, LocalDate dateOfAdmission) {
        this(licensePlate);
        this.emissionCategory = emissionCategory;
        this.carType = carType;
        this.fuelType = fuelType;
        this.dateOfAdmission = dateOfAdmission;
    }

    /**
     * Creates a new Car instance from a text line
     * The text line should be in the format: licensePlate, emissionCategory, carType, fuelType, dateOfAdmission
     * The fields should be separated by commas
     * Throws an exception if the fields cannot be parsed
     *
     * @param textLine the text line to parse
     * @return a new Car instance with the provided information or null if the textLine is corrupt or incomplete
     */
    public static Car fromLine(String textLine) {
        Car newCar = null;

        String[] fields = textLine.split(",");
        if (fields.length >= 5) {
            try {
                newCar = new Car(
                        fields[0].trim(),
                        Integer.parseInt(fields[1].trim()),
                        CarType.valueOf(fields[2].trim()),
                        FuelType.valueOf(fields[3].trim()),
                        LocalDate.parse(fields[4].trim())
                );
            } catch (Exception e) {
                System.out.printf("Could not parse Car specification in text line '%s'\n", textLine);
                System.out.println(e.getMessage());
            }
        }

        return newCar;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public int getEmissionCategory() {
        return emissionCategory;
    }

    public CarType getCarType() {
        return carType;
    }

    public FuelType getFuelType() {
        return fuelType;
    }

    @Override
    public int compareTo(Car other) {
        return this.licensePlate.compareTo(other.licensePlate);
    }

    @Override
    public String toString() {
        return String.format("%s/%d/%s/%s",
                this.licensePlate, this.emissionCategory, this.carType, this.fuelType);
    }

    /**
     * Two cars are considered equal if they have the same license plate
     *
     * @param o the other car to compare with
     * @return true if the cars are equal, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Car car)) return false;
        // cars are uniquely defined by their license plate
        return licensePlate.equals(car.licensePlate);
    }


    /**
     * Two cars are considered equal if they have the same license plate
     *
     * @return the hash code of the license plate
     */
    @Override
    public int hashCode() {
        return licensePlate.hashCode();
    }
}
