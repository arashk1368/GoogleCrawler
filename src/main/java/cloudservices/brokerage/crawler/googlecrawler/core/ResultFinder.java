package cloudservices.brokerage.crawler.googlecrawler.core;

import cloudservices.brokerage.crawler.googlecrawler.model.GoogleResult;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Arash Khodadadi http://www.arashkhodadadi.com/
 */
public abstract class ResultFinder {

    private final static Logger LOGGER = Logger.getLogger(ResultFinder.class.getName());
    private final GoogleSearch googleSearch;
    private final int maxEmptyResults;
    private final int maxSameResults;
    private long politenessDelay;
    private long totalResultsNum;
    private int emptyCounter;
    private int sameCounter;
    private List<GoogleResult> previousResults;

    public ResultFinder(long politenessDelay, String userAgent, String googleUrl, int maxEmptyResults, int maxSameResults) {
        this.politenessDelay = politenessDelay;
        this.googleSearch = new GoogleSearch(userAgent, googleUrl);
        this.maxEmptyResults = maxEmptyResults;
        this.maxSameResults = maxSameResults;
    }

    public void start(String query, String fileType, String filter, long maxGoogleResults, long initialStart) throws UnsupportedEncodingException, IOException {
        LOGGER.log(Level.INFO, "Result Finder started for query= {0} filetype= {1} filter= {2}",
                new Object[]{query, fileType, filter});
        List<GoogleResult> gResults;
        long start = initialStart;
        this.emptyCounter = 0;
        this.sameCounter = 0;
        this.previousResults = new ArrayList<>(); // for the first time check
        while (this.totalResultsNum < maxGoogleResults) {
            gResults = this.googleSearch.getResults(query, start, fileType, filter);
            if (gResults.isEmpty()) {
                if (emptyCounter > maxEmptyResults) {
                    LOGGER.log(Level.INFO, "No Results Found For {0} Times", maxEmptyResults);
                    break;
                }
                start = initialStart + 10 * emptyCounter;
                emptyCounter++;
            } else if (previousResults.containsAll(gResults)) {
                if (sameCounter > maxSameResults) {
                    LOGGER.log(Level.INFO, "Same Results Found For {0} Times", maxSameResults);
                    break;
                }
                start += gResults.size();
                sameCounter++;
            } else {
                this.totalResultsNum += gResults.size();
                start = initialStart + this.totalResultsNum;
                int counter = 0;
                for (GoogleResult googleResult : gResults) {
                    try {
                        if (validateResult(googleResult)) {
                            if (useResult(googleResult, query, start)) {
                                counter++;
                            }
                        } else {
                        }
                    } catch (Exception ex) {
                        LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
                    }
                }
                LOGGER.log(Level.INFO, "{0} Results Useful", counter);
                this.previousResults = gResults;
                this.emptyCounter = 0;
                this.sameCounter = 0;
            }
            try {
                long rand = Math.round(Math.random() * 10);
                Thread.sleep(this.politenessDelay * rand);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Implement this method to use the found result and return the result of
     * your usage.
     *
     * @param result The result found.
     * @param query The query which the result found from.
     * @param start The starting point in Google search which includes this
     * result.
     * @return The result of usage which determines if the result was useful or
     * not.
     */
    public abstract boolean useResult(GoogleResult result, String query, long start);

    /**
     * Implement this method to validate the found result.
     *
     * @param googleResult The result found to be validated.
     * @return The result of the validation.
     */
    public abstract boolean validateResult(GoogleResult googleResult);

    /**
     * Get the value of politenessDelay
     *
     * @return the value of politenessDelay
     */
    public long getPolitenessDelay() {
        return politenessDelay;
    }

    /**
     * Set the value of politenessDelay
     *
     * @param politenessDelay new value of politenessDelay
     */
    public void setPolitenessDelay(long politenessDelay) {
        this.politenessDelay = politenessDelay;
    }

    /**
     * Get the value of totalResultsNum
     *
     * @return the value of totalResultsNum
     */
    public long getTotalResultsNum() {
        return totalResultsNum;
    }

}
