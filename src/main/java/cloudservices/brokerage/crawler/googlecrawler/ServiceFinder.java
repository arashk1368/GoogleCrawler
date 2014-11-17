/*
 * Copyright 2014 Arash khodadadi.
 * <http://www.arashkhodadadi.com/>
 */
package cloudservices.brokerage.crawler.googlecrawler;

import cloudservices.brokerage.crawler.crawlingcommons.model.DAO.DAOException;
import cloudservices.brokerage.crawler.crawlingcommons.model.DAO.v2.RawCrawledServiceDAO;
import cloudservices.brokerage.crawler.crawlingcommons.model.entities.v2.RawCrawledService;
import cloudservices.brokerage.crawler.crawlingcommons.model.enums.v2.RawCrawledServiceColType;
import cloudservices.brokerage.crawler.crawlingcommons.model.enums.v2.RawCrawledServiceType;
import cloudservices.brokerage.crawler.googlecrawler.core.ResultFinder;
import cloudservices.brokerage.crawler.googlecrawler.model.GoogleResult;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Arash Khodadadi <http://www.arashkhodadadi.com/>
 */
public class ServiceFinder extends ResultFinder {

    private final static Logger LOGGER = Logger.getLogger(ResultFinder.class.getName());
    private final static String TOKEN = ";;;";
    private final static int MAX_SAME_RESULTS = 5;
    private final static int MAX_EMPTY_RESULTS = 3;
    private final RawCrawledServiceDAO crawledServiceDAO;
    private final RawCrawledServiceType rawCrawledServiceType;
    private long savedResultsNum;
    private long modifiedResultsNum;

    public ServiceFinder(long politenessDelay, String userAgent, String googleUrl, RawCrawledServiceType rawCrawledServiceType) {
        super(politenessDelay, userAgent, googleUrl, MAX_EMPTY_RESULTS, MAX_SAME_RESULTS);
        this.crawledServiceDAO = new RawCrawledServiceDAO();
        this.rawCrawledServiceType = rawCrawledServiceType;
    }

    @Override
    public boolean useResult(GoogleResult result, String query, long start) {
        try {
            RawCrawledService newRCS = new RawCrawledService(result.getTitle(), result.getUrl(), result.getDescription(),
                    query, "Google Crawler", String.valueOf(start), this.rawCrawledServiceType, true);
            return addOrUpdateCrawledService(newRCS);
        } catch (DAOException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
            return false;
        }
    }

    @Override
    public boolean validateResult(GoogleResult googleResult) {
        return RawCrawledService.checkLength(googleResult.getDescription().length(), RawCrawledServiceColType.DESCRIPTION)
                && RawCrawledService.checkLength(googleResult.getTitle().length(), RawCrawledServiceColType.TITLE)
                && RawCrawledService.checkLength(googleResult.getUrl().length(), RawCrawledServiceColType.URL);
    }

    public long getSavedResultsNum() {
        return savedResultsNum;
    }

    public long getModifiedResultsNum() {
        return modifiedResultsNum;
    }

    private boolean addOrUpdateCrawledService(RawCrawledService rcs) throws DAOException {
        RawCrawledService inDB = crawledServiceDAO.findByUrl(rcs.getUrl());

        if (inDB == null) {
            LOGGER.log(Level.FINE, "There is no raw crawled service in DB, Saving a new one");
            crawledServiceDAO.addCrawledService(rcs);
            this.savedResultsNum++;
            LOGGER.log(Level.INFO, "Raw Crawled Service with URL: {0} saved successfully with ID: {1}", new Object[]{rcs.getUrl(), rcs.getId()});
            return true;
        } else {
            LOGGER.log(Level.FINE, "Found the same url with ID = {0} in DB, Trying to update", inDB.getId());
            boolean isModified = false;
            if (inDB.getTitle().compareTo(rcs.getTitle()) != 0) {
                LOGGER.log(Level.FINER, "Titles are different;new one: {0} , indb: {1}", new Object[]{rcs.getTitle(), inDB.getTitle()});
                String[] titles = rcs.getTitle().split(TOKEN);
                for (String title : titles) {
                    if (!inDB.getTitle().contains(title)) {
                        String newString = inDB.getTitle().concat(TOKEN).concat(title);
                        LOGGER.log(Level.FINER, "Adding Title: {0}", title);
                        if (RawCrawledService.checkLength(newString.length(), RawCrawledServiceColType.TITLE)) {
                            inDB.setTitle(newString);
                        } else {
                            LOGGER.log(Level.WARNING, "Title can not be updated because it is too large!");
                        }
                    }
                }
                isModified = true;
            }

            if (inDB.getDescription().compareTo(rcs.getDescription()) != 0) {
                LOGGER.log(Level.FINER, "Descriptions are different;new one: {0} , indb: {1}", new Object[]{rcs.getDescription(), inDB.getDescription()});
                String[] descriptions = rcs.getDescription().split(TOKEN);
                for (String str : descriptions) {
                    if (!inDB.getDescription().contains(str)) {
                        String newString = inDB.getDescription().concat(TOKEN).concat(str);
                        LOGGER.log(Level.FINER, "Adding Description: {0}", str);
                        if (RawCrawledService.checkLength(newString.length(), RawCrawledServiceColType.DESCRIPTION)) {
                            inDB.setDescription(newString);
                        } else {
                            LOGGER.log(Level.WARNING, "Description can not be updated because it is too large!");
                        }
                    }
                }
                isModified = true;
            }

            if (inDB.getSource().compareTo(rcs.getSource()) != 0) {
                LOGGER.log(Level.FINER, "Sources are different;new one: {0} , indb: {1}", new Object[]{rcs.getSource(), inDB.getSource()});
                String[] newOnes = rcs.getSource().split(TOKEN);
                for (String str : newOnes) {
                    if (!inDB.getSource().contains(str)) {
                        String newString = inDB.getSource().concat(TOKEN).concat(str);
                        LOGGER.log(Level.FINER, "Adding Source: {0}", str);
                        if (RawCrawledService.checkLength(newString.length(), RawCrawledServiceColType.SOURCE)) {
                            inDB.setSource(newString);
                        } else {
                            LOGGER.log(Level.WARNING, "Source can not be updated because it is too large!");
                        }
                    }
                }
                isModified = true;
            }

            if (inDB.getQuery().compareTo(rcs.getQuery()) != 0) {
                LOGGER.log(Level.FINER, "Queries are different;new one: {0} , indb: {1}", new Object[]{rcs.getQuery(), inDB.getQuery()});
                String[] newOnes = rcs.getQuery().split(TOKEN);
                for (String str : newOnes) {
                    if (!inDB.getQuery().contains(str)) {
                        String newString = inDB.getQuery().concat(TOKEN).concat(str);
                        LOGGER.log(Level.FINER, "Adding Query: {0}", str);
                        if (RawCrawledService.checkLength(newString.length(), RawCrawledServiceColType.SEARCHED_QUERY)) {
                            inDB.setQuery(newString);
                        } else {
                            LOGGER.log(Level.WARNING, "Query can not be updated because it is too large!");
                        }
                    }
                }
                isModified = true;
            }
            if (isModified) {
                inDB.setUpdated(true);
                crawledServiceDAO.saveOrUpdate(inDB);
                this.modifiedResultsNum++;
                LOGGER.log(Level.INFO, "Raw Crawled Service with ID: {0} updated successfully", inDB.getId());
                return true;
            } else {
                LOGGER.log(Level.INFO, "Result with url ={0} already exists with the same properties", inDB.getUrl());
                return false;
            }
        }
    }

}
