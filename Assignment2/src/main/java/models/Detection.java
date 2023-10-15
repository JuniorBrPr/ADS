package models;

import java.time.LocalDateTime;
import java.util.List;

import static models.Car.CarType;
import static models.Car.FuelType;

/**
 * Represents a detection of a car that has entered an environmentally restricted zone
 * at a certain date and time.
 * A detection is associated with a car , a city and a date and time.
 */
public class Detection {
    private final Car car;
    private final String city;
    private final LocalDateTime dateTime;

    public Detection(Car car, String city, LocalDateTime dateTime) {
        this.car = car;
        this.city = city;
        this.dateTime = dateTime;
    }

    /**
     * Parses detection information from a line of text about a car that has entered an environmentally controlled zone
     * of a specified city.
     * The format of the text line is: licensePlate, city, dateTimeThe licensePlate shall be
     * matched with a car from the provided list. If no matching car can be found, a new Car shall be instantiated with
     * the given licensePlate and added to the list(besides the license plate number there will be no other information
     * available about this car).
     *
     * @param textLine The text line to parse.
     * @param cars     A list of known cars, ordered and searchable by licensePlate (i.e. the indexOf method of the list
     *                 shall only consider the licensePlate when comparing cars).
     * @return A new Detection instance with the provided information or null if the textLine is corrupt or incomplete
     */
    public static Detection fromLine(String textLine, List<Car> cars) {
        Detection newDetection = null;
        String[] parts = textLine.split(",");

        // Check if the textLine contains all the required information
        if (parts.length == 3) {
            String licensePlate = parts[0].trim();
            String city = parts[1].trim();
            LocalDateTime dateTime = LocalDateTime.parse(parts[2].trim());

            // Search for the car in the list using licensePlate
            int carIndex = -1;
            for (Car car : cars) {
                if (car.getLicensePlate().equals(licensePlate)) {
                    carIndex = cars.indexOf(car);
                    break;
                }
            }

            // If the car is not found, create a new Car and add it to the list
            if (carIndex == -1) {
                Car newCar = new Car(licensePlate);
                cars.add(newCar);
                carIndex = cars.indexOf(newCar);
            }

            newDetection = new Detection(cars.get(carIndex), city, dateTime);
        }

        return newDetection;
    }

    /**
     * Validates a detection against the purple conditions for entering an
     * environmentally restricted zone
     * I.e.:
     * Diesel trucks and diesel coaches with an emission category of below 6 may not
     * enter a purple zone
     *
     * @return a Violation instance if the detection saw an offence against the
     * purple zone rule/
     * null if no offence was found.
     */
    public Violation validatePurple() {
        CarType carType = car.getCarType();
        FuelType fuelType = car.getFuelType();
        int emissionCategory = car.getEmissionCategory();

        if ((carType == CarType.Truck || carType == CarType.Coach) &&
                fuelType == FuelType.Diesel &&
                emissionCategory < 6) {
            return new Violation(car, city);
        }

        return null;
    }

    public Car getCar() {
        return car;
    }

    public String getCity() {
        return city;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    @Override
    public String toString() {
        return car.getLicensePlate() + "/" + city + "/" + dateTime;
    }
}
