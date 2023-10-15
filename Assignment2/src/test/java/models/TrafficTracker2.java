package models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

public class TrafficTracker2 {
    private final static String VAULT_NAME = "/2023-09";

    TrafficTracker trafficTracker;

    @BeforeEach
    public void setup() {
        Locale.setDefault(Locale.ENGLISH);
        trafficTracker = new TrafficTracker();

        trafficTracker.importCarsFromVault(VAULT_NAME + "/cars.txt");

        trafficTracker.importDetectionsFromVault(VAULT_NAME + "/detections");
    }

    @Test
    public void calculateTotalFinesTest() {
        assertEquals(trafficTracker.calculateTotalFines(), 186090);
    }

    @Test
    public void topViolationsByCarTest() {
        List<Violation> violations = trafficTracker.topViolationsByCar(5);

        // Check if the list size equals the expected size
        assertEquals(violations.size(), 5);

        // Check if the items are in descending order of offencesCount
        for (int i = 0; i < violations.size() - 1; i++) {
            assertTrue(violations.get(i).getOffencesCount() >= violations.get(i + 1).getOffencesCount());
        }
    }

    @Test
    public void topViolationsByCityTest() {
        List<Violation> violations = trafficTracker.topViolationsByCity(5);

        // Check if the list size equals the expected size
        assertEquals(violations.size(), 5);

        // Check if the items are in descending order of offencesCount
        for (int i = 0; i < violations.size() - 1; i++) {
            assertTrue(violations.get(i).getOffencesCount() >= violations.get(i + 1).getOffencesCount());
        }

        // Check if the cities have been removed
        for (Violation violation : violations) {
            assertNull(violation.getCity());
        }
    }
}
