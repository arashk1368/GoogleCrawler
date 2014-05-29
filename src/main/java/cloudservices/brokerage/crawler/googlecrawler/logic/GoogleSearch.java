/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cloudservices.brokerage.crawler.googlecrawler.logic;

import cloudservices.brokerage.crawler.googlecrawler.model.GoogleResult;
import cloudservices.brokerage.crawler.googlecrawler.utils.DocumentLoader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author Arash Khodadadi http://www.arashkhodadadi.com/
 */
public class GoogleSearch {

    private String userAgent;
    private String googleUrl;
    private final static String CHARSET = "UTF-8";
    private final static Logger LOGGER = Logger.getLogger(GoogleSearch.class.getName());

    public GoogleSearch(String userAgent, String googleUrl) {
        this.userAgent = userAgent;
        this.googleUrl = googleUrl;
    }

    public List<GoogleResult> getResults(String query, long start) throws UnsupportedEncodingException, IOException {
        List<GoogleResult> googleResults = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        sb.append(googleUrl);
        sb.append("search?q=");
        sb.append(URLEncoder.encode(query, CHARSET));
        sb.append("&start=");
        sb.append(start);

        LOGGER.log(Level.INFO, "Getting results in {0}", sb.toString());

        Document doc = DocumentLoader.getDocument(sb.toString(), this.userAgent);
        Elements results = doc.select("li.g");

        LOGGER.log(Level.INFO, "Found {0} results from parsing document", results.size());

        GoogleResult gResult;
        Elements linkElements;
        Elements descElements;
        Element linkElement;
        Element descElement;

        for (Element result : results) {
            linkElements = result.select("h3>a");
            descElements = result.select("span.st");
            if (!linkElements.isEmpty()) {
                linkElement = linkElements.get(0);
                String description = "";
                String title = linkElement.text();
                String url = linkElement.absUrl("href"); // Google returns URLs in format "http://www.google.com/url?q=<url>&sa=U&ei=<someKey>".
                url = URLDecoder.decode(url.substring(url.indexOf('=') + 1, url.indexOf('&')), "UTF-8");

                if (!url.startsWith("http")) {
                    LOGGER.log(Level.INFO,
                            "The result with title= {0} and url= {1} is not a valid result", new Object[]{title, url});
                    continue; // Ads/news/etc.
                }
                if (!descElements.isEmpty()) {
                    descElement = descElements.get(0);
                    description = descElement.text();
                } else {
                    LOGGER.log(Level.INFO,
                            "The result with title= {0} and url= {1} does not have a description", new Object[]{title, url});
                }

                gResult = new GoogleResult(title, url, description);
                googleResults.add(gResult);
            }
        }
        LOGGER.log(Level.INFO, "Found {0} results", googleResults.size());
        return googleResults;
    }

    /**
     * Get the value of userAgent
     *
     * @return the value of userAgent
     */
    public String getUserAgent() {
        return userAgent;
    }

    /**
     * Set the value of userAgent
     *
     * @param userAgent new value of userAgent
     */
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    /**
     * Get the value of googleUrl
     *
     * @return the value of googleUrl
     */
    public String getGoogleUrl() {
        return googleUrl;
    }

    /**
     * Set the value of googleUrl
     *
     * @param googleUrl new value of googleUrl
     */
    public void setGoogleUrl(String googleUrl) {
        this.googleUrl = googleUrl;
    }
}
