package maze_escape;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class PrimMazeEscapeMainTest {
    static String output;

    @BeforeAll
    static void beforeALl() {
        // Backup the original standard out
        PrintStream originalOut = System.out;
        // Create a stream to hold the output
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        // Change System.out to point out to our stream
        System.setOut(new PrintStream(outContent));

        // Run the main method
        PrimMazeEscapeMain.main(new String[]{});

        // Restore original System.out
        System.setOut(originalOut);

        // Get the output string
        output = outContent.toString();
    }

    @Test
    void mainShouldPrintWelcome() {
        assert output.contains("Welcome to the HvA Maze Escape");
    }

    @Test
    void mainShouldPrintMaze() {
        assert output.contains("Created 100x100 Randomized-Prim-Maze(20231113) with 250 walls removed");
    }

    @Test
    void depthFirstSearchShouldFindCorrectPath() {
        assert output.contains("Depth First Search: Weight=463.00 Length=162 visited=5167 (6666, 6563, 6663, " +
                "6665, 6765, 6766, 6767, 6768, 6769, 7069, ..., 788, 790, 692, 693, 493, 393, 293, 193, 194, 96)");
    }

    @Test
    void depthFirstSearchShouldFindCorrectReturnPath() {
        assert output.contains("Depth First Search return: Weight=1976.00 Length=709 visited=3197 (96, 97, 197, " +
                "194, 193, 293, 393, 395, 495, 493, ..., 6974, 6973, 6972, 7070, 7069, 6769, 6768, 6767, 6766, 6666)");
    }

    @Test
    void breadthFirstSearchShouldFindCorrectPath() {
        assert output.contains("Breadth First Search: Weight=226.00 Length=79 visited=5126 (6666, 6563, 6462, " +
                "6460, 6459, 6359, 6259, 6157, 6057, 5756, ..., 788, 790, 692, 693, 493, 393, 293, 193, 194, 96)");
    }

    @Test
    void breadthFirstSearchShouldFindCorrectReturnPath() {
        assert output.contains("Breadth First Search return: Weight=226.00 Length=79 visited=2940 (96, 194, 193, " +
                "293, 393, 493, 693, 692, 790, 788, ..., 5756, 6057, 6157, 6259, 6359, 6459, 6460, 6462, 6563, 6666)");
    }

    @Test
    void dijkstraShortestPathShouldFindCorrectWeightAndLength() {
        assert output.contains("Dijkstra Shortest Path: Weight=226.00 Length=79");
    }
}
