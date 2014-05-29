/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cloudservices.brokerage.crawler.googlecrawler.logic;

import cloudservices.brokerage.commons.utils.validators.WSDLValidator;
import cloudservices.brokerage.crawler.crawlingcommons.model.DAO.DAOException;
import cloudservices.brokerage.crawler.crawlingcommons.model.DAO.WSDLDAO;
import cloudservices.brokerage.crawler.crawlingcommons.model.entities.WSDL;
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
public class WSDLFinder {

    private GoogleSearch googleSearch;
    private long politenessDelay;
    private long totalResultsNum;
    private long savedResultsNum;
    private WSDLDAO wsdlDAO;
    private final static Logger LOGGER = Logger.getLogger(WSDLFinder.class.getName());

    public WSDLFinder(long politenessDelay, String userAgent, String googleUrl) {
        this.politenessDelay = politenessDelay;
        this.googleSearch = new GoogleSearch(userAgent, googleUrl);
        this.wsdlDAO = new WSDLDAO();
    }

    public void start(String query, long maxGoogleResults, long initialStart) throws UnsupportedEncodingException, IOException, DAOException {
        LOGGER.log(Level.INFO, "WSDL Finder started for query= {0}", query);
        List<GoogleResult> gResults;
        WSDL wsdl;
        long start = initialStart;
        while (this.totalResultsNum < maxGoogleResults) {
            gResults = this.googleSearch.getResults(query, start);
            this.totalResultsNum += gResults.size();
            start = initialStart + this.totalResultsNum;
            for (GoogleResult googleResult : gResults) {
                wsdl = new WSDL(googleResult.getUrl(), googleResult.getTitle(), googleResult.getDescription());
                if (checkWSDL(wsdl)) {
                    wsdlDAO.addWSDL(wsdl);
                    LOGGER.log(Level.INFO, "WSDL with url= {0} added successfully with Id= {1}", new Object[]{wsdl.getUrl(), wsdl.getId()});
                    this.savedResultsNum++;
                }
            }
            try {
                Thread.sleep(this.politenessDelay);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private boolean checkWSDL(WSDL wsdl) throws DAOException {
        if (wsdlDAO.URLExists(wsdl.getUrl())) {
            LOGGER.log(Level.INFO, "WSDL with url ={0} already exists", wsdl.getUrl());
            return false;
        }
        //TODO: validate wsdl and other logic here
        return true;
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
}
