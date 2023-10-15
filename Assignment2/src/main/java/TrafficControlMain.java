import models.Car;
import models.TrafficTracker;
import models.Violation;

import java.util.List;
import java.util.Locale;

public class TrafficControlMain {
    private final static String VAULT_NAME = "/2023-09";
//    private final static String VAULT_NAME = "/test1";

    public static void main(String[] args) {
        Locale.setDefault(Locale.ENGLISH);
        System.out.println("Welcome to the HvA Traffic Control processor\n");

        TrafficTracker trafficTracker = new TrafficTracker();

        // import all known cars from the data vault
        trafficTracker.importCarsFromVault(VAULT_NAME + "/cars.txt");
        System.out.print("Imported cars:\n[");
        List<Car> cars = trafficTracker.getCars().subList(0, Integer.min(10, trafficTracker.getCars().size()));
        int count = 0;
        for (int i = 0; i < cars.size(); i++) {
            count++;
            System.out.printf("%s%s", cars.get(i), i < cars.size() - 1 ? ", " : "]...\n");
            if (count == 2) {
                System.out.println();
                count = 0;
            }
        }
        count = 0;

        // import and process all detections at the city entry points of environmental zones from the data vault
        trafficTracker.importDetectionsFromVault(VAULT_NAME + "/detections");
        System.out.println("Aggregated offending detections:");
        List<Violation> violations = trafficTracker.getViolations()
                .subList(0, Integer.min(10, trafficTracker.getViolations().size()));
        for (int i = 0; i < violations.size(); i++) {
            count++;
            System.out.printf("%s%s", violations.get(i), i < violations.size() - 1 ? ", " : "]...\n\n");
            if (count == 4) {
                System.out.println();
                count = 0;
            }
        }

        // calculate potential revenues from multiple fine schemes for violations
        System.out.printf(
                "Total fines à €25 per offence for trucks and €35 per offence for coaches would amount to: \n€%,.2f\n",
                trafficTracker.calculateTotalFines());

        // report top-5 violations from different aggregation criteria
        System.out.printf("Top 5 cars with largest total number of offences are:\n%s\n",
                trafficTracker.topViolationsByCar(5));
        System.out.printf("Top 5 cities with largest total number of offences are:\n%s\n",
                trafficTracker.topViolationsByCity(5));
    }
}
