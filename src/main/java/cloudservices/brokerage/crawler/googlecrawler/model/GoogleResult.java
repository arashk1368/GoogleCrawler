/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cloudservices.brokerage.crawler.googlecrawler.model;

import java.util.Objects;

/**
 *
 * @author Arash Khodadadi http://www.arashkhodadadi.com/
 */
public class GoogleResult {
    
    private String title;
    private String url;
    private String description;
    
    public GoogleResult(String title, String url, String description) {
        this.title = title;
        this.url = url;
        this.description = description;
    }
    
    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        if (other == this) {
            return true;
        }
        if (!(other instanceof GoogleResult)) {
            return false;
        }
        GoogleResult otherResult = (GoogleResult) other;
        return otherResult.description.equals(this.description)
                && otherResult.title.equals(this.title)
                && otherResult.url.equals(this.url);
    }
    
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 11 * hash + Objects.hashCode(this.title);
        hash = 11 * hash + Objects.hashCode(this.url);
        hash = 11 * hash + Objects.hashCode(this.description);
        return hash;
    }

    /**
     * Get the value of description
     *
     * @return the value of description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Set the value of description
     *
     * @param description new value of description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Get the value of url
     *
     * @return the value of url
     */
    public String getUrl() {
        return url;
    }

    /**
     * Set the value of url
     *
     * @param url new value of url
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * Get the value of title
     *
     * @return the value of title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Set the value of title
     *
     * @param title new value of title
     */
    public void setTitle(String title) {
        this.title = title;
    }
}
