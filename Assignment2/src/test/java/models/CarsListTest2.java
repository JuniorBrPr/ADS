package models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class CarsListTest2 {
    Car scoda, audi, bmw, mercedes, icova, volvo1, volvo2, daf1, daf2, daf3, kamaz;

    OrderedArrayList<Car> cars;
    List<Car> initialCars;

    @BeforeEach
    public void setup() {
        Locale.setDefault(Locale.ENGLISH);
        scoda = new Car("1-AAA-02", 6, Car.CarType.Car, Car.FuelType.Gasoline, LocalDate.of(2014,1,31));
        audi = new Car("AA-11-BB", 4, Car.CarType.Car, Car.FuelType.Diesel, LocalDate.of(1998,1,31));
        mercedes = new Car("VV-11-BB", 4, Car.CarType.Van, Car.FuelType.Diesel, LocalDate.of(1998,1,31));
        bmw = new Car("A-123-BB", 4, Car.CarType.Car, Car.FuelType.Gasoline, LocalDate.of(2019,1,31));
        icova = new Car("1-TTT-99", 5, Car.CarType.Truck, Car.FuelType.Lpg, LocalDate.of(2011,1,31));
        volvo1 = new Car("1-TTT-01", 5, Car.CarType.Truck, Car.FuelType.Diesel, LocalDate.of(2009,1,31));
        volvo2 = new Car("1-TTT-02", 6, Car.CarType.Truck, Car.FuelType.Diesel, LocalDate.of(2011,1,31));
        daf1 = new Car("1-CCC-01", 5, Car.CarType.Coach, Car.FuelType.Diesel, LocalDate.of(2009,1,31));
        daf2 = new Car("1-CCC-02", 6, Car.CarType.Coach, Car.FuelType.Diesel, LocalDate.of(2011,1,31));
        daf3 = new Car("1-CCC-03", 5, Car.CarType.Coach, Car.FuelType.Lpg, LocalDate.of(2006,1,31));
        kamaz = new Car("1-AAAA-0000");

        // Using your comparator for cars here as you have specified it in the TrafficTracker
        TrafficTracker trafficTracker = new TrafficTracker();
        assertNotNull(trafficTracker.getCars().getSortOrder(),
                "Cannot run these tests, until your TrafficTracker constructor sets up a cars OrdererdList with an adequate sort order");
        cars = new OrderedArrayList<>(trafficTracker.getCars().getSortOrder());
        initialCars = List.of(scoda,audi,mercedes,bmw,icova,volvo1,daf1,kamaz);
        cars.addAll(initialCars);
    }

    @Test
    public void recursiveBinarySearchTest() {
        // Sort the list of cars by licensePlate
        cars.sort(Car::compareTo);

        // Check if the recursiveBinarySearch method returns the correct index according to the licensePlate order
        assertEquals(cars.indexOfByRecursiveBinarySearch(scoda), 0);
        assertEquals(cars.indexOfByRecursiveBinarySearch(kamaz), 1);
        assertEquals(cars.indexOfByRecursiveBinarySearch(daf1), 2);
        assertEquals(cars.indexOfByRecursiveBinarySearch(volvo1), 3);
        assertEquals(cars.indexOfByRecursiveBinarySearch(icova), 4);
        assertEquals(cars.indexOfByRecursiveBinarySearch(bmw), 5);
        assertEquals(cars.indexOfByRecursiveBinarySearch(audi), 6);
        assertEquals(cars.indexOfByRecursiveBinarySearch(mercedes), 7);

        // Check if the recursiveBinarySearch method returns -1 if the item is not in the list
        assertEquals(cars.indexOfByRecursiveBinarySearch(volvo2), -1);
        assertEquals(cars.indexOfByRecursiveBinarySearch(daf2), -1);
        assertEquals(cars.indexOfByRecursiveBinarySearch(daf3), -1);
    }

    @Test
    public void iterativeBinarySearchTest() {
        // Sort the list of cars by licensePlate
        cars.sort(Car::compareTo);

        // Check if the iterativeBinarySearch method returns the correct index
        assertEquals(cars.indexOfByIterativeBinarySearch(scoda), 0);
        assertEquals(cars.indexOfByIterativeBinarySearch(kamaz), 1);
        assertEquals(cars.indexOfByIterativeBinarySearch(daf1), 2);
        assertEquals(cars.indexOfByIterativeBinarySearch(volvo1), 3);
        assertEquals(cars.indexOfByIterativeBinarySearch(icova), 4);
        assertEquals(cars.indexOfByIterativeBinarySearch(bmw), 5);
        assertEquals(cars.indexOfByIterativeBinarySearch(audi), 6);
        assertEquals(cars.indexOfByIterativeBinarySearch(mercedes), 7);

        // Check if the iterativeBinarySearch method returns -1 if the item is not in the list
        assertEquals(cars.indexOfByIterativeBinarySearch(volvo2), -1);
        assertEquals(cars.indexOfByIterativeBinarySearch(daf2), -1);
        assertEquals(cars.indexOfByIterativeBinarySearch(daf3), -1);
    }

    @Test
    public void binarySearchVariantsShouldReturnSameIndexNewItemAfterSort(){
        cars.add(daf2);
        cars.add(daf3);
        cars.add(volvo2);

        // Sort the list of cars by licensePlate
        cars.sort(Car::compareTo);

        // Check if indexOfByRecursiveBinarySearch and indexOfByIterativeBinarySearch return the same index
        assertEquals(cars.indexOfByRecursiveBinarySearch(daf2), cars.indexOfByIterativeBinarySearch(daf2));
        assertEquals(cars.indexOfByRecursiveBinarySearch(daf3), cars.indexOfByIterativeBinarySearch(daf3));
        assertEquals(cars.indexOfByRecursiveBinarySearch(volvo2), cars.indexOfByIterativeBinarySearch(volvo2));
    }
}
