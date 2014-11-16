package cloudservices.brokerage.crawler.googlecrawler;

import cloudservices.brokerage.commons.utils.file_utils.DirectoryUtil;
import cloudservices.brokerage.commons.utils.logging.LoggerSetup;
import cloudservices.brokerage.crawler.crawlingcommons.model.DAO.BaseDAO;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.hibernate.HibernateException;
import org.hibernate.cfg.Configuration;
import org.jsoup.HttpStatusException;

public class Search {

    private final static Logger LOGGER = Logger.getLogger(Search.class.getName());
    private final static String GOOGLE_URL = "http://www.google.com/";
    private final static String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) "
            + "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/35.0.1916.114 Safari/537.36";
    private final static long POLITENESS_DELAY = 60000; //ms
    private final static String FILE_TYPE = "";
    private final static String GOOGLE_FILTER = "";
    private final static long MAX_GOOGLE_RESULTS = 900; //It is better %10=0, max for google 1000
    private final static long INITIAL_START = 0;

    public static void main(String[] args) throws InterruptedException {
//        createNewDB();
        List<String> queries = createQueries();
        if (createLogFile()) {
            long totalStartTime = System.currentTimeMillis();
            long totalFound = 0;
            long totalSaved = 0;
            long totalModified = 0;
            try {
                Configuration configuration = new Configuration();
                configuration.configure("hibernate.cfg.xml");
                BaseDAO.openSession(configuration);

                for (String query : queries) {
                    WSDLFinder finder = new WSDLFinder(POLITENESS_DELAY, USER_AGENT, GOOGLE_URL);
                    long startTime = System.currentTimeMillis();
                    LOGGER.log(Level.SEVERE, "Searching Start");
                    LOGGER.log(Level.SEVERE, "Google URL= " + GOOGLE_URL);
                    LOGGER.log(Level.SEVERE, "Query= {0}", query);
                    LOGGER.log(Level.SEVERE, "File Type= " + FILE_TYPE);
                    LOGGER.log(Level.SEVERE, "Google Filtering= {0}", GOOGLE_FILTER);
                    LOGGER.log(Level.SEVERE, "Query= {0}", query);
                    LOGGER.log(Level.SEVERE, "User Agent= " + USER_AGENT);
                    LOGGER.log(Level.SEVERE, "Politeness Delay= {0}", POLITENESS_DELAY);
                    LOGGER.log(Level.SEVERE, "Max Google Results= {0}", MAX_GOOGLE_RESULTS);
                    LOGGER.log(Level.SEVERE, "Initial Start= {0}", INITIAL_START);

                    try {
                        finder.start(query, FILE_TYPE, GOOGLE_FILTER, MAX_GOOGLE_RESULTS, INITIAL_START);
                    } catch (HttpStatusException | SocketTimeoutException | UnknownHostException ex) {
                        LOGGER.log(Level.SEVERE, "REJECTED BY GOOGLE OR INTERNET DISCONNECTED", ex);
                        long rand = Math.round(Math.random() * 100);
                        LOGGER.log(Level.SEVERE, "Waiting for {0}", (POLITENESS_DELAY * rand));
                        Thread.sleep(POLITENESS_DELAY * rand);
                    }

                    long endTime = System.currentTimeMillis();
                    long totalTime = endTime - startTime;
                    LOGGER.log(Level.SEVERE, "Searching End in {0}ms", totalTime);
                    LOGGER.log(Level.SEVERE, "Google Results Found: {0}", finder.getTotalResultsNum());
                    LOGGER.log(Level.SEVERE, "Results Saved: {0}", finder.getSavedResultsNum());
                    LOGGER.log(Level.SEVERE, "Results Updated: {0}", finder.getModifiedResultsNum());

                    totalFound += finder.getTotalResultsNum();
                    totalModified += finder.getModifiedResultsNum();
                    totalSaved += finder.getSavedResultsNum();

                }
            } catch (HibernateException | IOException ex) {
                LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
            } finally {
                BaseDAO.closeSession();
                long endTime = System.currentTimeMillis();
                long totalTime = endTime - totalStartTime;
                LOGGER.log(Level.SEVERE, "Searching End for All in {0}ms", totalTime);
                LOGGER.log(Level.SEVERE, "Total Google Results Found: {0}", totalFound);
                LOGGER.log(Level.SEVERE, "Total Results Saved: {0}", totalSaved);
                LOGGER.log(Level.SEVERE, "Total Results Updated: {0}", totalModified);
            }
        } else {
            throw new RuntimeException("Problems with creating the log file");
        }
    }

    private static void createNewDB() {
        try {
            Configuration configuration = new Configuration();
            configuration.configure("hibernate.cfg.xml");
            BaseDAO.openSession(configuration);

        } finally {
            BaseDAO.closeSession();
        }
    }

    private static List<String> createQueries() {
        List<String> queries = new ArrayList<>();
        queries.add("");
        return queries;
    }

    private static boolean createLogFile() {
        try {
            StringBuilder sb = new StringBuilder();
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH-mm");
            Calendar cal = Calendar.getInstance();
            sb.append(dateFormat.format(cal.getTime()));
            String filename = sb.toString();
            DirectoryUtil.createDir("logs");
            LoggerSetup.setup("logs/" + filename + ".txt", "logs/" + filename + ".html", Level.FINER);
            return true;
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
            return false;
        }
    }
}
