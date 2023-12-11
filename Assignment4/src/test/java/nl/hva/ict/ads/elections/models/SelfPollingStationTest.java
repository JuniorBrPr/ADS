package nl.hva.ict.ads.elections.models;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SelfPollingStationTest {
    private final int VOTES11 = 10;
    private final int VOTES12 = 20;
    private final int VOTES21 = 40;

    private Party party1, party2;
    private Candidate candidate11, candidate12, candidate21, candidate21a;
    private PollingStation hva;

    @BeforeEach
    void setup() {
        this.party1 = new Party(1,"Party-1");
        this.party2 = new Party(2,"Party-2");
        this.candidate11 = new Candidate("A.", null, "Candidate", this.party1);
        this.candidate12 = new Candidate("B.", null, "Candidate", this.party1);
        this.candidate21 = new Candidate("A.", null, "Candidate", this.party2);
        this.candidate21a = new Candidate("A.", null, "Candidate", this.party2);;
        this.hva = new PollingStation("hva", "1091GH", "hva");
        hva.addVotes(candidate11, VOTES11);
        hva.addVotes(candidate12, VOTES12);
        hva.addVotes(candidate21, VOTES21);
    }

    @Test
    public void testAddAndGetVotes() {
        // Check that votes are correctly added and retrieved
        assertEquals(VOTES11, hva.getVotes(candidate11));
        assertEquals(VOTES12, hva.getVotes(candidate12));
        assertEquals(VOTES21, hva.getVotes(candidate21));

        // Add more votes and check again
        hva.addVotes(candidate11, VOTES11);
        assertEquals(VOTES11 * 2, hva.getVotes(candidate11));
    }

    @Test
    public void testGetVotesByParty() {
        // Check that votes are correctly counted by party
        Map<Party, Integer> votesByParty = hva.getVotesByParty();
        assertEquals(VOTES11 + VOTES12, votesByParty.get(party1));
        assertEquals(VOTES21, votesByParty.get(party2));
    }
}
