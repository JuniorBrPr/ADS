package nl.hva.ict.ads.elections.models;

import nl.hva.ict.ads.utils.PathUtils;
import nl.hva.ict.ads.utils.xml.XMLParser;

import javax.xml.stream.XMLStreamException;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Holds all election data per consituency
 * Provides calculation methods for overall election results
 */
public class Election {

    private String name;

    // all (unique) parties in this election, organised by Id
    // will be build from the XML
    protected Map<Integer, Party> parties;

    // all (unique) constituencies in this election, identified by Id
    protected Set<Constituency> constituencies;

    public Election(String name) {
        this.name = name;
        this.parties = new HashMap<>();
        this.constituencies = new HashSet<>();
    }

    /**
     * finds all (unique) parties registered for this election
     * @return all parties participating in at least one constituency, without duplicates
     */
    public Collection<Party> getParties() {
        return parties.values();
    }

    /**
     * finds the party with a given id
     * @param id
     * @return  the party with given id, or null if no such party exists.
     */
    public Party getParty(int id) {
        return getParties().stream()
                .filter(p -> p.getId() == id)
                .findFirst()
                .orElse(null);
    }

    public Set<? extends Constituency> getConstituencies() {
        return this.constituencies;
    }

    /**
     * finds all unique candidates across all parties across all constituencies
     * organised by increasing party-id
     * @return alle unique candidates organised by increasing party-id
     */
    public List<Candidate> getAllCandidates() {
        return getParties().stream()
                .map(Party::getCandidates)
                .flatMap(Collection::stream)
                .toList();
    }

    /**
     * Retrieve for the given party the number of Candidates that have been registered per Constituency
     * @param party
     * @return
     */
    public Map<Constituency,Integer> numberOfRegistrationsByConstituency(Party party) {
        Map<Constituency, Integer> registrations = new HashMap<>();
        for (Constituency constituency : constituencies) {
            int count = constituency.getCandidates(party).size();
            registrations.put(constituency, count);
        }
        return registrations;
    }

    /**
     * Finds all Candidates that have a duplicate name against another candidate in the election
     * (can be in the same party or in another party)
     * @return
     */
    public Set<Candidate> getCandidatesWithDuplicateNames() {
        Map<String, Integer> nameCounts = new HashMap<>();
        for (Party party : parties.values()) {
            for (Candidate candidate : party.getCandidates()) {
                nameCounts.put(candidate.getFullName(), nameCounts.getOrDefault(candidate.getFullName(), 0) + 1);
            }
        }
        return getAllCandidates().stream()
                .filter(candidate -> nameCounts.get(candidate.getFullName()) > 1)
                .collect(Collectors.toSet());
    }

    /**
     * Retrieve from all constituencies the combined sub set of all polling stations that are located within the area of the specified zip codes
     * i.e. firstZipCode <= pollingStation.zipCode <= lastZipCode
     * All valid zip codes adhere to the pattern 'nnnnXX' with 1000 <= nnnn <= 9999 and 'AA' <= XX <= 'ZZ'
     * @param firstZipCode
     * @param lastZipCode
     * @return      the sub set of polling stations within the specified zipCode range
     */
    public Collection<PollingStation> getPollingStationsByZipCodeRange(String firstZipCode, String lastZipCode) {
        return constituencies.stream()
                .flatMap(constituency -> constituency.getPollingStations().stream())
                .filter(pollingStation ->
                        pollingStation.getZipCode().compareTo(firstZipCode) >= 0 &&
                                pollingStation.getZipCode().compareTo(lastZipCode) <= 0)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves per party the total number of votes across all candidates, constituencies and polling stations
     * @return
     */
    public Map<Party, Integer> getVotesByParty() {
        Map<Party, Integer> votes = new HashMap<>();
    for (Party party : parties.values()) {
        int totalVotes = 0;
        for (Constituency constituency : constituencies) {
            totalVotes += constituency.getVotesByParty().getOrDefault(party, 0);
        }
        votes.put(party, totalVotes);
    }
    return votes;
    }

    /**
     * Retrieves per party the total number of votes across all candidates,
     * that were cast in one out of the given collection of polling stations.
     * This method is useful to prepare an election result for any sub-area of a Constituency.
     * Or to obtain statistics of special types of voting, e.g. by mail.
     * @param pollingStations the polling stations that cover the sub-area of interest
     * @return
     */
    public Map<Party, Integer> getVotesByPartyAcrossPollingStations(Collection<PollingStation> pollingStations) {
        Map<Party, Integer> votes = new HashMap<>();
        for (Party party : parties.values()) {
            int totalVotes = 0;
            for (Candidate candidate : party.getCandidates()) {
                for (PollingStation station : pollingStations) {
                    totalVotes += station.getVotes(candidate);
                }
            }
            votes.put(party, totalVotes);
        }

        return votes;
    }

    /**
     * Transforms and sorts decreasingly vote counts by party into votes percentages by party
     * The party with the highest vote count shall be ranked upfront
     * The votes percentage by party is calculated from  100.0 * partyVotes / totalVotes;
     * @return  the sorted list of (party,votesPercentage) pairs with the highest percentage upfront
     */
    public static List<Map.Entry<Party,Double>> sortedElectionResultsByPartyPercentage(int tops, Map<Party, Integer> votesCounts) {
        List<Map.Entry<Party, Double>> sortedResults = votesCounts.entrySet().stream()
                .sorted(Map.Entry.<Party, Integer>comparingByValue().reversed())
                .limit(tops)
                .map(entry -> Map.entry(entry.getKey(), (entry.getValue() * 100.0) / integersSum(votesCounts.values())))
                .collect(Collectors.toList());

        return sortedResults;
    }

    /**
     * Find the most representative Polling Station, which has got its votes distribution across all parties
     * the most alike the distribution of overall total votes.
     * A perfect match is found, if for each party the percentage of votes won at the polling station
     * is identical to the percentage of votes won by the party overall in the election.
     * The most representative Polling Station has the smallest deviation from that perfect match.
     *
     * There are different metrics possible to calculate a relative deviation between distributions.
     * You may use the helper method {@link #euclidianVotesDistributionDeviation(Map, Map)}
     * which calculates a relative least-squares deviation between two distributions.
     *
     * @return the most representative polling station.
     */
    public PollingStation findMostRepresentativePollingStation() {
        Map<Party, Integer> overallVotes = getVotesByParty();
        return constituencies.stream()
                .flatMap(constituency -> constituency.getPollingStations().stream())
                .min(Comparator.comparingDouble(station -> euclidianVotesDistributionDeviation(overallVotes, station.getVotesByParty())))
                .orElse(null);
    }

    /**
     * Calculates the Euclidian distance between the relative distribution across parties of two voteCounts.
     * If the two relative distributions across parties are identical, then the distance will be zero
     * If some parties have relatively more votes in one distribution than the other, the outcome will be positive.
     * The lower the outcome, the more alike are the relative distributions of the voteCounts.
     * ratign of votesCounts1 relative to votesCounts2.
     * see https://towardsdatascience.com/9-distance-measures-in-data-science-918109d069fa
     *
     * @param votesCounts1 one distribution of votes across parties.
     * @param votesCounts2 another distribution of votes across parties.
     * @return de relative distance between the two distributions.
     */
    private double euclidianVotesDistributionDeviation(Map<Party, Integer> votesCounts1, Map<Party, Integer> votesCounts2) {
        // calculate total number of votes in both distributions
        int totalNumberOfVotes1 = integersSum(votesCounts1.values());
        int totalNumberOfVotes2 = integersSum(votesCounts2.values());

        // we calculate the distance as the sum of squares of relative voteCount distribution differences per party
        // if we compare two voteCounts that have the same relative distribution across parties, the outcome will be zero

        return votesCounts1.entrySet().stream()
                .mapToDouble(e -> Math.pow(e.getValue()/(double)totalNumberOfVotes1 -
                        votesCounts2.getOrDefault(e.getKey(),0)/(double)totalNumberOfVotes2, 2))
                .sum();
    }

    /**
     * auxiliary method to calculate the total sum of a collection of integers
     * @param integers
     * @return
     */
    public static int integersSum(Collection<Integer> integers) {
        return integers.stream().reduce(Integer::sum).orElse(0);
    }

    public String prepareSummary(int partyId) {
        Party party = this.getParty(partyId);
        StringBuilder summary = new StringBuilder()
                .append("\nSummary of ").append(party).append(":\n")
                .append("\nTotal number of candidates = ").append(party.getCandidates().size()).append("\n")
                .append("\nCandidate").append(party.getCandidates()).append("\n")
                .append("\nTotal number of registrations = ").append(numberOfRegistrationsByConstituency(party).values().stream().mapToInt(Integer::intValue).sum()).append("\n")
                .append("\nNumber of registrations per constituency: ").append(numberOfRegistrationsByConstituency(party)).append("\n");

        return summary.toString();
    }

    public String prepareSummary() {
        StringBuilder summary = new StringBuilder()
                .append("\nElection summary of\n").append(this.name).append(":\n\n\n")
                .append(parties.size()).append(" Participating parties:\n").append("\n")
                .append(parties.values().stream().sorted(Comparator.comparing(Party::getId)).collect(Collectors.toList())).append("\n")
                .append("\nTotal number of constituencies = ").append(constituencies.size()).append("\n")
                .append("\nTotal number of polling stations = ").append(constituencies.stream().mapToInt(constituency -> constituency.getPollingStations().size()).sum()).append("\n")
                .append("\nTotal number of candidates in the election = ").append(getAllCandidates().size()).append("\n")
                .append("\nDifferent candidates with duplicate names across different parties are:\n\n");
                
                Set<Candidate> candidatesSet = getCandidatesWithDuplicateNames();
                List<Candidate> candidatesList = new ArrayList<>(candidatesSet);
                Collections.reverse(candidatesList);
                summary.append(candidatesList).append("\n\n");

                summary.append("\nOverall election results by party percentage:\n\n").append(sortedElectionResultsByPartyPercentage(parties.size(), getVotesByParty())).append("\n\n")
                .append("\nPolling stations in Amsterdam Wibautstraat area with zip codes 1091AA-1091ZZ:\n\n").append(getPollingStationsByZipCodeRange("1091AA", "1091ZZ")).append("\n")
                .append("\nTop 10 sorted election results within the Amsterdam Wibautstraat area with zipcodes between 1091AA and 1091ZZ ordered by decreasing party percentage:\n\n").append(sortedElectionResultsByPartyPercentage(10, getVotesByPartyAcrossPollingStations(getPollingStationsByZipCodeRange("1091AA", "1091ZZ")))).append("\n")
                .append("\nMost representative polling station is:\n\n").append(findMostRepresentativePollingStation()).append("\n\n")
                .append(sortedElectionResultsByPartyPercentage(parties.size(), findMostRepresentativePollingStation().getVotesByParty())).append("\n");

        return summary.toString();
    }

    /**
     * Reads all data of Parties, Candidates, Contingencies and PollingStations from available files in the given folder and its subfolders
     * This method can cope with any structure of sub folders, but does assume the file names to comply with the conventions
     * as found from downloading the files from https://data.overheid.nl/dataset/verkiezingsuitslag-tweede-kamer-2021
     * So, you can merge folders after unpacking the zip distributions of the data, but do not change file names.
     * @param folderName    the root folder with the data files of the election results
     * @return een Election met alle daarbij behorende gegevens.
     * @throws XMLStreamException bij fouten in een van de XML bestanden.
     * @throws IOException als er iets mis gaat bij het lezen van een van de bestanden.
     */
    public static Election importFromDataFolder(String folderName) throws XMLStreamException, IOException {
        System.out.println("Loading election data from " + folderName);
        Election election = new Election(folderName);
        int progress = 0;
        Map<Integer, Constituency> kieskringen = new HashMap<>();
        for (Path constituencyCandidatesFile : PathUtils.findFilesToScan(folderName, "Kandidatenlijsten_TK2021_")) {
            XMLParser parser = new XMLParser(new FileInputStream(constituencyCandidatesFile.toString()));
            Constituency constituency = Constituency.importFromXML(parser, election.parties);
            //election.constituenciesM.put(constituency.getId(), constituency);
            election.constituencies.add(constituency);
            showProgress(++progress);
        }
        System.out.println();
        progress = 0;
        for (Path votesPerPollingStationFile : PathUtils.findFilesToScan(folderName, "Telling_TK2021_gemeente")) {
            XMLParser parser = new XMLParser(new FileInputStream(votesPerPollingStationFile.toString()));
            election.importVotesFromXml(parser);
            showProgress(++progress);
        }
        System.out.println();
        return election;
    }

    protected static void showProgress(final int progress) {
        System.out.print('.');
        if (progress % 50 == 0) System.out.println();
    }

    /**
     * Auxiliary method for parsing the data from the EML files
     * This methode can be used as-is and does not require your investigation or extension.
     */
    public void importVotesFromXml(XMLParser parser) throws XMLStreamException {
        if (parser.findBeginTag(Constituency.CONSTITUENCY)) {

            int constituencyId = 0;
            if (parser.findBeginTag(Constituency.CONSTITUENCY_IDENTIFIER)) {
                constituencyId = parser.getIntegerAttributeValue(null, Constituency.ID, 0);
                parser.findAndAcceptEndTag(Constituency.CONSTITUENCY_IDENTIFIER);
            }

            //Constituency constituency = this.constituenciesM.get(constituencyId);
            final int finalConstituencyId = constituencyId;
            Constituency constituency = this.constituencies.stream()
                    .filter(c -> c.getId() == finalConstituencyId)
                    .findFirst()
                    .orElse(null);

            //parser.findBeginTag(PollingStation.POLLING_STATION_VOTES);
            while (parser.findBeginTag(PollingStation.POLLING_STATION_VOTES)) {
                PollingStation pollingStation = PollingStation.importFromXml(parser, constituency, this.parties);
                if (pollingStation != null) constituency.add(pollingStation);
            }

            parser.findAndAcceptEndTag(Constituency.CONSTITUENCY);
        }
    }
}
