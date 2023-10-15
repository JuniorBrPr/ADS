package models;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.function.Function;

/**
 * Represents a traffic tracker that can import and merge data from the RDW
 * and calculate statistics about the traffic in the Netherlands.
 */
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
     * Imports all registered cars from a resource file that has been provided by the RDW. Sorts the cars for efficient
     * later retrieval.
     *
     * @param resourceName The name of the resource file.
     */
    public void importCarsFromVault(String resourceName) {
        this.cars.clear();
        int numberOfLines = importItemsFromFile(
                this.cars,
                createFileFromURL(Objects.requireNonNull(TrafficTracker.class.getResource(resourceName))),
                Car::fromLine
        );
        this.cars.sort();

        System.out.printf("Imported %d cars from %d lines in %s.\n", this.cars.size(), numberOfLines, resourceName);
    }

    /**
     * Imports and merges all raw detection data of all entry gates of all cities from the hierarchical file structure
     * of the vault accumulates any offences against purple rules into this.violations. Sorts the violations for
     * efficient later retrieval.
     *
     * @param resourceName The name of the resource file.
     */
    public void importDetectionsFromVault(String resourceName) {
        this.violations.clear();

        int totalNumberOfOffences = this.mergeDetectionsFromVaultRecursively(
                createFileFromURL(Objects.requireNonNull(TrafficTracker.class.getResource(resourceName))));
        this.violations.sort();

        System.out.printf("Found %d offences among detections imported from files in %s.\n",
                totalNumberOfOffences, resourceName);
    }

    /**
     * Traverses the detections vault recursively and processes every data file that it finds.
     * The method is called recursively for every subfolder that is found.
     *
     * @param file The file or folder to be processed.
     */
    private int mergeDetectionsFromVaultRecursively(File file) {
        int totalNumberOfOffences = 0;

        if (file.isDirectory()) {
            File[] filesInDirectory = Objects.requireNonNullElse(file.listFiles(), new File[0]);

            for (File subFile : filesInDirectory) {
                totalNumberOfOffences += this.mergeDetectionsFromVaultRecursively(subFile);
            }
        } else if (file.getName().matches(TRAFFIC_FILE_PATTERN)) {
            totalNumberOfOffences += this.mergeDetectionsFromFile(file);
        }

        return totalNumberOfOffences;
    }

    /**
     * Imports another batch detection data from the filePath text file and merges the offences into the earlier
     * imported and accumulated violations. Sorts the violations for efficient later retrieval.
     *
     * @param file The file to be processed
     */
    private int mergeDetectionsFromFile(File file) {
        this.violations.sort();

        // Use a regular ArrayList to load the raw detection info from the file
        List<Detection> newDetections = new ArrayList<>();

        // Import all detections from the specified file into the newDetections list
        importItemsFromFile(newDetections, file, s -> Detection.fromLine(s, cars));

        System.out.printf("Imported %d detections from ...%s.\n",
                newDetections.size(), file.getPath().split("classes")[1].replace("\\", "/"));
        return getTotalNumberOfOffences(newDetections);
    }

    /**
     * Validate all detections against the purple criteria and merge any resulting offences into this.violations,
     * accumulating offences per car and per city. If a violation already exists in this.violations, its offencesCount
     * is updated, otherwise it is added to this.violations.
     *
     * @param newDetections The list of new detections to be validated and merged.
     * @return The total number of offences that emerge from the data in this file.
     */
    private int getTotalNumberOfOffences(List<Detection> newDetections) {
        int totalNumberOfOffences = 0;
        for (Detection newDetection : newDetections) {
            Violation violation = newDetection.validatePurple();
            if (violation != null) {
                Optional<Violation> existingViolation = this.violations.stream()
                        .filter(v -> v.getCar().getLicensePlate().equals(violation.getCar().getLicensePlate())
                                && v.getCity().equals(violation.getCity()))
                        .findFirst();

                if (existingViolation.isPresent()) {
                    existingViolation.get().setOffencesCount(existingViolation.get().getOffencesCount() + 1);
                } else {
                    this.violations.add(violation);
                }

                totalNumberOfOffences++;
            }
        }
        return totalNumberOfOffences;
    }

    /**
     * Calculates the total revenue of fines from all violations, Trucks pay €25 per offence, Coaches €35 per offence.
     *
     * @return The total amount of money recovered from all violations.
     */
    public double calculateTotalFines() {
        return this.violations.stream()
                .mapToDouble(violation -> {
                    if (violation.getCar().getCarType() == Car.CarType.Truck) {
                        return 25.0 * violation.getOffencesCount();
                    } else if (violation.getCar().getCarType() == Car.CarType.Coach) {
                        return 35.0 * violation.getOffencesCount();
                    } else {
                        return 0.0;
                    }
                })
                .sum();
    }

    /**
     * Prepares a list of topNumber of violations that show the highest offencesCount when this.violations are
     * aggregated by car across all cities. If a violation already exists in violationsByCar, its offencesCount is
     * updated, otherwise it is added to violationsByCar. Order the violations by descending offencesCount.
     *
     * @param topNumber The requested top number of violations in the result list.
     * @return A list of topNum items that provides the top aggregated violations.
     */
    public List<Violation> topViolationsByCar(int topNumber) {
        OrderedList<Violation> violationsByCar = new OrderedArrayList<>(Violation::compareByLicensePlateAndCity);
        for (Violation violation : this.violations) {
            Optional<Violation> existingViolation = violationsByCar.stream()
                    .filter(v -> v.getCar().getLicensePlate().equals(violation.getCar().getLicensePlate()))
                    .findFirst();

            if (existingViolation.isPresent()) {
                violationsByCar.set(violationsByCar.indexOf(existingViolation.get()),
                        existingViolation.get().combineOffencesCounts(violation));
            } else {
                violationsByCar.add(violation);
            }
        }
        violationsByCar.sort(Violation::compareByOffencesCount);
        return violationsByCar.subList(0, Math.min(topNumber, violationsByCar.size()));
    }

    /**
     * Prepares a list of topNumber of violations that show the highest offencesCount when this.violations are
     * aggregated by city across all cars. If a violation already exists in violationsByCity, its offencesCount is
     * updated, otherwise it is added to violationsByCity. Order the violations by descending offencesCount.
     *
     * @param topNumber The requested top number of violations in the result list.
     * @return A list of topNum items that provides the top aggregated violations.
     */
    public List<Violation> topViolationsByCity(int topNumber) {
        OrderedList<Violation> violationsByCity = new OrderedArrayList<>(Violation::compareByLicensePlateAndCity);
        for (Violation violation : this.violations) {
            Optional<Violation> existingViolation = violationsByCity.stream()
                    .filter(v -> v.getCity().equals(violation.getCity()))
                    .findFirst();

            if (existingViolation.isPresent()) {
                violationsByCity.set(violationsByCity.indexOf(existingViolation.get()),
                        existingViolation.get().combineOffencesCounts(violation));
            } else {
                violationsByCity.add(violation);
            }
        }
        violationsByCity.sort(Violation::compareByOffencesCount);
        return violationsByCity.subList(0, Math.min(topNumber, violationsByCity.size()));
    }

    /**
     * Imports a collection of items from a text file which provides one line for each item.
     *
     * @param items     The list to which imported items shall be added.
     * @param file      The source text file.
     * @param converter A function that can convert a text line into a new item
     *                  instance.
     * @param <E>       The (generic) type of each item.
     */
    public static <E> int importItemsFromFile(List<E> items, File file, Function<String, E> converter) {
        int numberOfLines = 0;

        Scanner scanner = createFileScanner(file);
        while (scanner.hasNext()) {
            String line = scanner.nextLine();
            numberOfLines++;

            E item = converter.apply(line);
            if (item != null) {
                items.add(item);
            }
        }

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

    /**
     * helper method to create a file from a URL and handle the exception
     *
     * @param url The URL to be converted to a file.
     * @return A file from the URL.
     */
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
