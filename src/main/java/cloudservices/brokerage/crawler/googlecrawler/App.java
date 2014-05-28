package cloudservices.brokerage.crawler.googlecrawler;

import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class App 
{
     public void crawl2() throws IOException {
        String google = "http://www.google.com/search?q=";
        String search = "service";
        String charset = "UTF-8";
        String userAgent = "ExampleBot 1.0 (+http://example.com/bot)"; // Change this to your company's name and bot homepage!

        Elements links = Jsoup.connect(google + URLEncoder.encode(search, charset)).userAgent(userAgent).get().select("li.g>h3>a");

        for (Element link : links) {
            String title = link.text();
            String val = link.val();
            String url = link.absUrl("href"); // Google returns URLs in format "http://www.google.com/url?q=<url>&sa=U&ei=<someKey>".
            url = URLDecoder.decode(url.substring(url.indexOf('=') + 1, url.indexOf('&')), "UTF-8");

            if (!url.startsWith("http")) {
                continue; // Ads/news/etc.
            }

            System.out.println("Title: " + title);
            System.out.println("URL: " + url);
            System.out.println("VALUE: " + val);
        }
    }
    
    public static void main( String[] args )
    {
        System.out.println( "Hello World!" );
    }
}
