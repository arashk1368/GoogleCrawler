package cloudservices.brokerage.crawler.googlecrawler;

import cloudservices.brokerage.crawler.crawlingcommons.model.DAO.DAOException;
import cloudservices.brokerage.crawler.crawlingcommons.model.DAO.WSDLDAO;
import cloudservices.brokerage.crawler.crawlingcommons.model.entities.WSDL;
import cloudservices.brokerage.crawler.crawlingcommons.model.enums.WSDLColType;
import cloudservices.brokerage.crawler.googlecrawler.core.ResultFinder;
import cloudservices.brokerage.crawler.googlecrawler.model.GoogleResult;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Arash Khodadadi http://www.arashkhodadadi.com/
 */
public class WSDLFinder extends ResultFinder {

    private final static Logger LOGGER = Logger.getLogger(ResultFinder.class.getName());
    private final static String TOKEN = ";;;";
    private final static int MAX_SAME_RESULTS = 5;
    private final static int MAX_EMPTY_RESULTS = 3;
    private final WSDLDAO wsdlDAO;
    private long savedResultsNum;
    private long modifiedResultsNum;

    public WSDLFinder(long politenessDelay, String userAgent, String googleUrl) {
        super(politenessDelay, userAgent, googleUrl, MAX_EMPTY_RESULTS, MAX_SAME_RESULTS);
        this.wsdlDAO = new WSDLDAO();
    }

    @Override
    public boolean useResult(GoogleResult result, String query, long start) {
        try {
            WSDL newWSDL = new WSDL(result.getUrl(), result.getTitle(), result.getDescription(), query);
            WSDL indb = wsdlDAO.find(newWSDL.getUrl());
            if (indb == null) {
                wsdlDAO.addWSDL(newWSDL);
                LOGGER.log(Level.INFO, "Result with url= {0} added successfully with Id= {1}", new Object[]{newWSDL.getUrl(), newWSDL.getId()});
                this.savedResultsNum++;
                return true;
            } else {
                boolean modified = false;
                if (!indb.getDescription().contains(newWSDL.getDescription())) {
                    String newDesc = indb.getDescription().concat(TOKEN).concat(newWSDL.getDescription());
                    if (WSDL.checkLength(newDesc.length(), WSDLColType.DESCRIPTION)) {
                        indb.setDescription(newDesc);
                        wsdlDAO.saveOrUpdate(indb);
                        LOGGER.log(Level.INFO, "Description for Result with url = {0} updated to {1}", new Object[]{indb.getUrl(), indb.getDescription()});
                        modified = true;
                    } else {
                        LOGGER.log(Level.INFO, "Description for Result with url = {0} can not be updated because it is too large!", indb.getUrl());
                    }
                }
                if (!indb.getTitle().contains(newWSDL.getTitle())) {
                    String newTitle = indb.getTitle().concat(TOKEN).concat(newWSDL.getTitle());
                    if (WSDL.checkLength(newTitle.length(), WSDLColType.TITLE)) {
                        indb.setTitle(newTitle);
                        wsdlDAO.saveOrUpdate(indb);
                        LOGGER.log(Level.INFO, "Title for Result with url = {0} updated to {1}", new Object[]{indb.getUrl(), indb.getTitle()});
                        modified = true;
                    } else {
                        LOGGER.log(Level.INFO, "Title for Result with url = {0} can not be updated because it is too large!", indb.getUrl());
                    }
                }
                if (!indb.getQuery().contains(newWSDL.getQuery())) {
                    String newQuery = indb.getQuery().concat(TOKEN).concat(newWSDL.getQuery());
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
                    LOGGER.log(Level.INFO, "Result with url ={0} already exists with the same properties or could not be updated", newWSDL.getUrl());
                    return false;
                }
            }
        } catch (DAOException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
            return false;
        }
    }

    @Override
    public boolean validateResult(GoogleResult googleResult) {
        return checkLength(googleResult);
    }

    /**
     * Get the value of savedResultsNum
     *
     * @return the value of savedResultsNum
     */
    public long getSavedResultsNum() {
        return savedResultsNum;
    }

    public long getModifiedResultsNum() {
        return modifiedResultsNum;
    }

    private boolean checkLength(GoogleResult googleResult) {
        return WSDL.checkLength(googleResult.getDescription().length(), WSDLColType.DESCRIPTION)
                && WSDL.checkLength(googleResult.getTitle().length(), WSDLColType.TITLE)
                && WSDL.checkLength(googleResult.getUrl().length(), WSDLColType.URL);
    }

}
