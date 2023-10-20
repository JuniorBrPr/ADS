package spotifycharts;

import java.util.EnumMap;

public class Song {

    public enum Language {
        EN, // English
        NL, // Dutch
        DE, // German
        FR, // French
        SP, // Spanish
        IT, // Italian
    }

    public enum Country {
        UK, // United Kingdom
        NL, // Netherlands
        DE, // Germany
        BE, // Belgium
        FR, // France
        SP, // Spain
        IT  // Italy
    }

    private final String artist;
    private final String title;
    private final Language language;

    // TODO add instance variable(s) to track the streams counts per country
    //  choose a data structure that you deem to be most appropriate for this application.
    private final EnumMap <Country, Integer> streamsCountPerCountry;

    /**
     * Constructs a new instance of Song based on given attribute values
     */
    public Song(String artist, String title, Language language) {
        this.artist = artist;
        this.title = title;
        this.language = language;

        // TODO initialise streams counts per country as appropriate.
        this.streamsCountPerCountry = new EnumMap<>(Country.class);
    }

    /**
     * Sets the given streams count for the given country on this song
     * @param country
     * @param streamsCount
     */
    public void setStreamsCountOfCountry(Country country, int streamsCount) {
        streamsCountPerCountry.put(country, streamsCount);
    }

    /**
     * retrieves the streams count of a given country from this song
     * @param country
     * @return
     */
    public int getStreamsCountOfCountry(Country country) {
        return streamsCountPerCountry.getOrDefault(country, 0);
    }
    /**
     * Calculates/retrieves the total of all streams counts across all countries from this song
     * @return
     */
    public int getStreamsCountTotal() {
        int totalStreams = 0;
        for (int streamsCount : streamsCountPerCountry.values()) {
            totalStreams += streamsCount;
        }

        return totalStreams;
    }

    /**
     * compares this song with the other song
     * ordening songs with the highest total number of streams upfront
     * @param other     the other song to compare against
     * @return  negative number, zero or positive number according to Comparator convention
     */
    public int compareByHighestStreamsCountTotal(Song other) {
        return Integer.compare(other.getStreamsCountTotal(), this.getStreamsCountTotal());
    }

    /**
     * compares this song with the other song
     * ordening all Dutch songs upfront and then by decreasing total number of streams
     * @param other     the other song to compare against
     * @return  negative number, zero or positive number according to Comparator conventions
     */
    public int compareForDutchNationalChart(Song other) {
        if (this.language == Language.NL && other.language != Language.NL) {
            return -1;
        } else if (this.language != Language.NL && other.language == Language.NL) {
            return 1;
        } else {
            return Integer.compare(other.getStreamsCountTotal(), this.getStreamsCountTotal());
        }
    }

    public String getArtist() {
        return artist;
    }

    public String getTitle() {
        return title;
    }

    public Language getLanguage() {
        return language;
    }

    @Override
    public String toString() {
        return artist + "/" + title + "{" + language + "}(" + getStreamsCountTotal() + ")";
    }
}
