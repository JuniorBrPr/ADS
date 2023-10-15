package models;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.function.Function;

public class TrafficTracker {
    private final String TRAFFIC_FILE_EXTENSION = ".txt";
    private final String TRAFFIC_FILE_PATTERN = ".+\\" + TRAFFIC_FILE_EXTENSION;

    private final OrderedList<Car> cars; // the reference list of all known Cars registered by the RDW
    private final OrderedList<Violation> violations; // the accumulation of all offences by car and by city

    public TrafficTracker() {
        this.cars = new OrderedArrayList<>(Car::compareTo);
        this.violations = new OrderedArrayList<>(Violation::compareByLicensePlateAndCity);
    }

    /**
     * imports all registered cars from a resource file that has been provided by
     * the RDW
     * 
     * @param resourceName the name of the resource file
     */
    public void importCarsFromVault(String resourceName) {
        this.cars.clear();

        // load all cars from the text file
        int numberOfLines = importItemsFromFile(this.cars,
                createFileFromURL(Objects.requireNonNull(TrafficTracker.class.getResource(resourceName))),
                Car::fromLine);
        // sort the cars for efficient later retrieval
        this.cars.sort();

        System.out.printf("Imported %d cars from %d lines in %s.\n", this.cars.size(), numberOfLines, resourceName);
    }

    /**
     * imports and merges all raw detection data of all entry gates of all cities
     * from the hierarchical file structure of the vault
     * accumulates any offences against purple rules into this.violations
     * 
     * @param resourceName the name of the resource file
     */
    public void importDetectionsFromVault(String resourceName) {
        this.violations.clear();

        int totalNumberOfOffences = this.mergeDetectionsFromVaultRecursively(
                createFileFromURL(Objects.requireNonNull(TrafficTracker.class.getResource(resourceName))));

        System.out.printf("Found %d offences among detections imported from files in %s.\n",
                totalNumberOfOffences, resourceName);
    }

    /**
     * traverses the detections vault recursively and processes every data file that
     * it finds
     * 
     * @param file the file or folder to be processed
     */
    private int mergeDetectionsFromVaultRecursively(File file) {
        int totalNumberOfOffences = 0;

        if (file.isDirectory()) {
            // the file is a folder (a.k.a. directory)
            // retrieve a list of all files and sub folders in this directory
            File[] filesInDirectory = Objects.requireNonNullElse(file.listFiles(), new File[0]);

            // TODO recursively process all files and sub folders from the filesInDirectory
            // list.
            // also track the total number of offences found
            for (File subFile : filesInDirectory) {
                totalNumberOfOffences += this.mergeDetectionsFromVaultRecursively(subFile);
            }
        } else if (file.getName().matches(TRAFFIC_FILE_PATTERN)) {
            // the file is a regular file that matches the target pattern for raw detection
            // files
            // process the content of this file and merge the offences found into
            // this.violations
            totalNumberOfOffences += this.mergeDetectionsFromFile(file);
        }

        return totalNumberOfOffences;
    }

    /**
     * imports another batch detection data from the filePath text file
     * and merges the offences into the earlier imported and accumulated violations
     * 
     * @param file the file to be processed
     */
    private int mergeDetectionsFromFile(File file) {
        // Re-sort the accumulated violations for efficient searching and merging
        this.violations.sort();

        // Use a regular ArrayList to load the raw detection info from the file
        List<Detection> newDetections = new ArrayList<>();

        // Import all detections from the specified file into the newDetections list
        importItemsFromFile(newDetections, file, s -> Detection.fromLine(s, cars));
//        System.out.printf("Imported %d detections from %s.\n", newDetections.size(), file.getPath());

        int totalNumberOfOffences = 0; // Tracks the number of offences that emerge from the data in this file

        // Validate all detections against the purple criteria and
        // merge any resulting offences into this.violations, accumulating offences per
        // car and per city
        for (Detection newDetection : newDetections) {
            Violation violation = newDetection.validatePurple();
            if (violation != null) {
                // Check if the violation already exists in this.violations
                Optional<Violation> existingViolation = this.violations.stream()
                        .filter(v -> v.getCar().getLicensePlate().equals(violation.getCar().getLicensePlate())
                                && v.getCity().equals(violation.getCity()))
                        .findFirst();

                if (existingViolation.isPresent()) {
                    // If the violation already exists, update its offencesCount
                    existingViolation.get().setOffencesCount(existingViolation.get().getOffencesCount() + 1);
                } else {
                    // If the violation does not exist, add it to this.violations and to this.cars
                    this.violations.add(violation);
                }

                totalNumberOfOffences++;
            }
        }

        return totalNumberOfOffences;
    }

    /**
     * calculates the total revenue of fines from all violations,
     * Trucks pay €25 per offence, Coaches €35 per offence
     * 
     * @return the total amount of money recovered from all violations
     */
    public double calculateTotalFines() {
        return this.violations.stream()
                .mapToDouble(violation -> {
                    if (violation.getCar().getCarType() == Car.CarType.Truck) {
                        return 25.0 * violation.getOffencesCount();
                    } else if (violation.getCar().getCarType() == Car.CarType.Coach) {
                        return 35.0 * violation.getOffencesCount();
                    } else {
                        return 0.0; // Handle other car types if needed
                    }
                })
                .sum();
    }

    /**
     * Prepares a list of topNumber of violations that show the highest
     * offencesCount
     * when this.violations are aggregated by car across all cities.
     * 
     * @param topNumber the requested top number of violations in the result list
     * @return a list of topNum items that provides the top aggregated violations
     */
    public List<Violation> topViolationsByCar(int topNumber) {
        //TODO
        return null; // replace this reference
    }

    /**
     * Prepares a list of topNumber of violations that show the highest
     * offencesCount
     * when this.violations are aggregated by city across all cars.
     * 
     * @param topNumber the requested top number of violations in the result list
     * @return a list of topNum items that provides the top aggregated violations
     */
    public List<Violation> topViolationsByCity(int topNumber) {

        // TODO merge all violations from this.violations into a new OrderedArrayList
        // which orders and aggregates violations by Car
        // TODO sort the new list by decreasing offencesCount.
        // TODO use .subList to return only the topNumber of violations from the sorted
        // list
        // (You may want to prepare/reuse a local private method for all this
        return null; // replace this reference
    }

    /**
     * imports a collection of items from a text file which provides one line for
     * each item
     * 
     * @param items     the list to which imported items shall be added
     * @param file      the source text file
     * @param converter a function that can convert a text line into a new item
     *                  instance
     * @param <E>       the (generic) type of each item
     */
    public static <E> int importItemsFromFile(List<E> items, File file, Function<String, E> converter) {
        int numberOfLines = 0;

        Scanner scanner = createFileScanner(file);
        while (scanner.hasNext()) {
            String line = scanner.nextLine();
            numberOfLines++;

            E item = converter.apply(line);
            if(item != null) {
                items.add(item);
            }
        }

//         System.out.printf("Imported %d lines from %s.\n", numberOfLines, file.getPath());
        return numberOfLines;
    }

    /**
     * helper method to create a scanner on a file and handle the exception
     * 
     * @param file the file to be scanned
     * @return a scanner on the file
     */
    private static Scanner createFileScanner(File file) {
        try {
            return new Scanner(file);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("FileNotFound exception on path: " + file.getPath());
        }
    }

    private static File createFileFromURL(URL url) {
        try {
            return new File(url.toURI().getPath());
        } catch (URISyntaxException e) {
            throw new RuntimeException("URI syntax error found on URL: " + url.getPath());
        }
    }

    public OrderedList<Car> getCars() {
        return this.cars;
    }

    public OrderedList<Violation> getViolations() {
        return this.violations;
    }
}
