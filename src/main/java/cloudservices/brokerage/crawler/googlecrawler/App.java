package cloudservices.brokerage.crawler.googlecrawler;

import cloudservices.brokerage.commons.utils.logging.LoggerSetup;
import cloudservices.brokerage.crawler.crawlingcommons.model.DAO.BaseDAO;
import cloudservices.brokerage.crawler.crawlingcommons.model.DAO.DAOException;
import cloudservices.brokerage.crawler.googlecrawler.logic.WSDLFinder;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.hibernate.HibernateException;
import org.hibernate.cfg.Configuration;
import org.jsoup.HttpStatusException;

public class App {

    private final static Logger LOGGER = Logger.getLogger(App.class.getName());
    private final static String GOOGLE_URL = "http://www.google.com/";
    private final static String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) "
            + "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/35.0.1916.114 Safari/537.36";
//    private final static String USER_AGENT = "ExampleBot 1.0 (+http://example.com/bot)";
    private final static long POLITENESS_DELAY = 60000; //ms
    private final static String QUERY = "SOAP Service";
    private final static String FILE_TYPE = "wsdl";
    private final static String GOOGLE_FILTER = "0";
    private final static long MAX_GOOGLE_RESULTS = 1000; //It is better %10=0
    private final static long INITIAL_START = 0;

    public static void main(String[] args) throws InterruptedException {
        try {
            LoggerSetup.setup("log.txt", "log.html", Level.INFO);
        } catch (IOException e) {
            throw new RuntimeException("Problems with creating the log files");
        }

        WSDLFinder finder = new WSDLFinder(POLITENESS_DELAY, USER_AGENT, GOOGLE_URL);
        long startTime = System.currentTimeMillis();
        LOGGER.log(Level.SEVERE, "Searching Start");
        LOGGER.log(Level.SEVERE, "Google URL= " + GOOGLE_URL);
        LOGGER.log(Level.SEVERE, "Query= " + QUERY);
        LOGGER.log(Level.SEVERE, "File Type= " + FILE_TYPE);
        LOGGER.log(Level.SEVERE, "Google Filtering= {0}", GOOGLE_FILTER);
        LOGGER.log(Level.SEVERE, "Query= " + QUERY);
        LOGGER.log(Level.SEVERE, "User Agent= " + USER_AGENT);
        LOGGER.log(Level.SEVERE, "Politeness Delay= {0}", POLITENESS_DELAY);
        LOGGER.log(Level.SEVERE, "Max Google Results= {0}", MAX_GOOGLE_RESULTS);
        LOGGER.log(Level.SEVERE, "Initial Start= {0}", INITIAL_START);

        try {
            Configuration configuration = new Configuration();
            configuration.configure("hibernate.cfg.xml");
            BaseDAO.openSession(configuration);

            finder.start(QUERY, FILE_TYPE, GOOGLE_FILTER, MAX_GOOGLE_RESULTS, INITIAL_START);
        } catch (HttpStatusException ex) {
            LOGGER.log(Level.SEVERE, "REJECTED BY GOOGLE", ex);
            long rand = Math.round(Math.random() * 100);
            Thread.sleep(POLITENESS_DELAY * rand);
        } catch (HibernateException | DAOException | IOException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            BaseDAO.closeSession();
            long endTime = System.currentTimeMillis();
            long totalTime = endTime - startTime;
            LOGGER.log(Level.SEVERE, "Searching End in {0}ms", totalTime);
            LOGGER.log(Level.SEVERE, "Total Google Results Found: {0}", finder.getTotalResultsNum());
            LOGGER.log(Level.SEVERE, "Total WSDL Saved: {0}", finder.getSavedResultsNum());
            LOGGER.log(Level.SEVERE, "Total WSDL Updated: {0}", finder.getModifiedResultsNum());
        }
    }
}
