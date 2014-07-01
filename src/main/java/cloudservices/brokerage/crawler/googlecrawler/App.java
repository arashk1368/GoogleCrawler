package cloudservices.brokerage.crawler.googlecrawler;

import cloudservices.brokerage.commons.utils.logging.LoggerSetup;
import cloudservices.brokerage.crawler.crawlingcommons.model.DAO.BaseDAO;
import cloudservices.brokerage.crawler.crawlingcommons.model.DAO.DAOException;
import cloudservices.brokerage.crawler.googlecrawler.logic.WSDLFinder;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
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
    private static List<String> queries;
//    private final static String QUERY = "twitter service";
    private final static String FILE_TYPE = "wsdl";
    private final static String GOOGLE_FILTER = "0";
    private final static long MAX_GOOGLE_RESULTS = 900; //It is better %10=0
    private final static long INITIAL_START = 0;

    public static void main(String[] args) throws InterruptedException {
        queries = new ArrayList<>();
        queries.add("usage");
        queries.add("Description Language");
        queries.add("Web Services Description Language");
        queries.add("contract");
        queries.add("global");
        queries.add("service contract");
        queries.add("registry");
        queries.add("service repository");
        queries.add("service registry");
        queries.add("service provider");
        queries.add("public");
        queries.add("SOAP");
        queries.add("soap based service");
        queries.add("interface");
        queries.add("service");
        queries.add("web service");
        queries.add("Web Services");
        queries.add("Services");
        queries.add("Amazon");
        queries.add("Twitter");
        queries.add("apache");
        queries.add("google");
        queries.add("sun");
        queries.add("youtube");
        queries.add("linkedin");
        queries.add("social media service");
        queries.add("facebook");
        queries.add("microsoft");
        queries.add("wikipedia");
        queries.add("connection service");
        queries.add("sms service");
        queries.add("gps service");
        queries.add("location service");
        queries.add("communication service");
        queries.add("education service");
        queries.add("weather service");
        queries.add("company service");
        queries.add("product service");
        queries.add("exchange service");
        queries.add("enterprise service");
        queries.add("status service");
        queries.add("converter service");
        queries.add("conversion service");
        queries.add("repair service");
        queries.add("price service");
        queries.add("stock service");
        queries.add("information service");
        queries.add("ecommerce");
        queries.add("customer service");
        queries.add("email service");
        queries.add("shop service");
        queries.add("access");
        queries.add("public access");
        queries.add("calculation service");
        queries.add("java");
        queries.add(".net");
        queries.add("");
        for (String query : queries) {

            try {
                StringBuilder sb = new StringBuilder();
                sb.append(GOOGLE_URL);
                sb.append("search?q=");
                sb.append(query);
                sb.append("+filetype:");
                sb.append(FILE_TYPE);
                sb.append("&filter=");
                sb.append(GOOGLE_FILTER);
                sb.append("&start=");
                sb.append(INITIAL_START);
                String filename = "log-" + URLEncoder.encode(sb.toString(), "UTF-8");
                LoggerSetup.setup(filename + ".txt", filename + ".html", Level.INFO);
            } catch (IOException e) {
                throw new RuntimeException("Problems with creating the log files");
            }

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
                Configuration configuration = new Configuration();
                configuration.configure("hibernate.cfg.xml");
                BaseDAO.openSession(configuration);

                finder.start(query, FILE_TYPE, GOOGLE_FILTER, MAX_GOOGLE_RESULTS, INITIAL_START);
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
            long endTime = System.currentTimeMillis();
            long totalTime = endTime - startTime;
            LOGGER.log(Level.SEVERE, "Searching End in {0}ms", totalTime);
            LOGGER.log(Level.SEVERE, "Total Google Results Found: {0}", finder.getTotalResultsNum());
            LOGGER.log(Level.SEVERE, "Total WSDL Saved: {0}", finder.getSavedResultsNum());
            LOGGER.log(Level.SEVERE, "Total WSDL Updated: {0}", finder.getModifiedResultsNum());
        }
    }
}
