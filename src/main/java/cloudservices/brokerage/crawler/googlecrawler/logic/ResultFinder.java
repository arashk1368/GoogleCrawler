/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cloudservices.brokerage.crawler.googlecrawler.logic;

import cloudservices.brokerage.crawler.crawlingcommons.model.DAO.DAOException;
import cloudservices.brokerage.crawler.crawlingcommons.model.DAO.WSDLDAO;
import cloudservices.brokerage.crawler.crawlingcommons.model.entities.WSDL;
import cloudservices.brokerage.crawler.crawlingcommons.model.enums.WSDLColType;
import cloudservices.brokerage.crawler.googlecrawler.model.GoogleResult;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Arash Khodadadi http://www.arashkhodadadi.com/
 */
public class ResultFinder {

    private final GoogleSearch googleSearch;
    private long politenessDelay;
    private long totalResultsNum;
    private long savedResultsNum;
    private long modifiedResultsNum;
    private final WSDLDAO wsdlDAO;
    private final static String TOKEN = ";;;";
    private final static Logger LOGGER = Logger.getLogger(ResultFinder.class.getName());
    private final static int MAX_EMPTY_RESULTS = 3;
    private int emptyCounter;

    public ResultFinder(long politenessDelay, String userAgent, String googleUrl) {
        this.politenessDelay = politenessDelay;
        this.googleSearch = new GoogleSearch(userAgent, googleUrl);
        this.wsdlDAO = new WSDLDAO();
    }

    public void start(String query, String fileType, String filter, long maxGoogleResults, long initialStart) throws UnsupportedEncodingException, IOException, DAOException {
        LOGGER.log(Level.INFO, "Result Finder started for query= {0} filetype= {1} filter= {2}",
                new Object[]{query, fileType, filter});
        List<GoogleResult> gResults;
        WSDL wsdl;
        long start = initialStart;
        this.emptyCounter = 0;
        while (this.totalResultsNum < maxGoogleResults) {
            gResults = this.googleSearch.getResults(query, start, fileType, filter);
            if (gResults.isEmpty()) {
                if (emptyCounter > MAX_EMPTY_RESULTS) {
                    LOGGER.log(Level.INFO, "No Results Found For {0} Times", MAX_EMPTY_RESULTS);
                    break;
                }
                start = initialStart + 10 * emptyCounter;
                emptyCounter++;
            } else {
                this.totalResultsNum += gResults.size();
                start = initialStart + this.totalResultsNum;
                int counter = 0;
                for (GoogleResult googleResult : gResults) {
                    try {
                        if (checkWSDL(googleResult)) {
                            wsdl = new WSDL(googleResult.getUrl(), googleResult.getTitle(), googleResult.getDescription(), query);
                            if (addOrUpdateWSDL(wsdl)) {
                                counter++;
                            }
                        }
                    } catch (Exception ex) {
                        LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
                    }
                }
                LOGGER.log(Level.INFO, "{0} Results Useful", counter);
            }
            try {
                long rand = Math.round(Math.random() * 10);
                Thread.sleep(this.politenessDelay * rand);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private boolean addOrUpdateWSDL(WSDL wsdl) throws DAOException {
        WSDL indb = wsdlDAO.find(wsdl.getUrl());
        if (indb == null) {
            wsdlDAO.addWSDL(wsdl);
            LOGGER.log(Level.INFO, "Result with url= {0} added successfully with Id= {1}", new Object[]{wsdl.getUrl(), wsdl.getId()});
            this.savedResultsNum++;
            return true;
        } else {
            boolean modified = false;
            if (!indb.getDescription().contains(wsdl.getDescription())) {
                String newDesc = indb.getDescription().concat(TOKEN).concat(wsdl.getDescription());
                if (WSDL.checkLength(newDesc.length(), WSDLColType.DESCRIPTION)) {
                    indb.setDescription(newDesc);
                    wsdlDAO.saveOrUpdate(indb);
                    LOGGER.log(Level.INFO, "Description for Result with url = {0} updated to {1}", new Object[]{indb.getUrl(), indb.getDescription()});
                    modified = true;
                } else {
                    LOGGER.log(Level.INFO, "Description for Result with url = {0} can not be updated because it is too large!", indb.getUrl());
                }
            }
            if (!indb.getTitle().contains(wsdl.getTitle())) {
                String newTitle = indb.getTitle().concat(TOKEN).concat(wsdl.getTitle());
                if (WSDL.checkLength(newTitle.length(), WSDLColType.TITLE)) {
                    indb.setTitle(newTitle);
                    wsdlDAO.saveOrUpdate(indb);
                    LOGGER.log(Level.INFO, "Title for Result with url = {0} updated to {1}", new Object[]{indb.getUrl(), indb.getTitle()});
                    modified = true;
                } else {
                    LOGGER.log(Level.INFO, "Title for Result with url = {0} can not be updated because it is too large!", indb.getUrl());
                }
            }
            if (!indb.getQuery().contains(wsdl.getQuery())) {
                String newQuery = indb.getQuery().concat(TOKEN).concat(wsdl.getQuery());
                if (WSDL.checkLength(newQuery.length(), WSDLColType.SEARCHED_QUERY)) {
                    indb.setQuery(newQuery);
                    wsdlDAO.saveOrUpdate(indb);
                    LOGGER.log(Level.INFO, "Query for Result with url = {0} updated to {1}", new Object[]{indb.getUrl(), indb.getQuery()});
                    modified = true;
                } else {
                    LOGGER.log(Level.INFO, "Query for Result with url = {0} can not be updated because it is too large!", indb.getUrl());
                }
            }
            if (modified) {
                this.modifiedResultsNum++;
                return true;
            } else {
                LOGGER.log(Level.INFO, "Result with url ={0} already exists with the same properties or could not be updated", wsdl.getUrl());
                return false;
            }
        }
    }

    private boolean checkWSDL(GoogleResult googleResult) throws DAOException {
        if (checkLength(googleResult)) {
            if (!googleResult.getUrl().contains("facebook")) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private boolean checkLength(GoogleResult googleResult) {
        if (WSDL.checkLength(googleResult.getDescription().length(), WSDLColType.DESCRIPTION)
                && WSDL.checkLength(googleResult.getTitle().length(), WSDLColType.TITLE)
                && WSDL.checkLength(googleResult.getUrl().length(), WSDLColType.URL)) {
            return true;
        }
        return false;
    }

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
     * Get the value of savedResultsNum
     *
     * @return the value of savedResultsNum
     */
    public long getSavedResultsNum() {
        return savedResultsNum;
    }

    /**
     * Get the value of totalResultsNum
     *
     * @return the value of totalResultsNum
     */
    public long getTotalResultsNum() {
        return totalResultsNum;
    }

    public long getModifiedResultsNum() {
        return modifiedResultsNum;
    }
}
