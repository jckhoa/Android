package com.example.webscraper;

import android.content.Context;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    WebView priceList;
    WebView webview;
    EditText editText;

    MyJsInterface myJsInterface;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        priceList = findViewById(R.id.priceList);
        webview = findViewById(R.id.webPage);
        editText = findViewById(R.id.editText);

        priceList.getSettings().setUseWideViewPort(true);
        priceList.getSettings().setLoadWithOverviewMode(true);
        priceList.getSettings().setJavaScriptEnabled(true);

        webview.getSettings().setJavaScriptEnabled(true);
        webview.getSettings().setUserAgentString("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:67.0) Gecko/20100101 Firefox/67.0");

        myJsInterface = new MyJsInterface(this);
        webview.addJavascriptInterface(myJsInterface, "Js");

        webview.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageFinished(WebView view, String url) {
                    view.loadUrl("javascript:window.Js.showHTML('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');");
                }
            }
        );

        /*
        WebScraper bcdScraper = new WebScraper("https://barcodesdatabase.org/barcode/9421021461303");
        bcdScraper.addScrapedItem("Owner", "table[class$=registration-table] tr:eq(1) td:eq(1)");
        bcdScraper.addScrapedItem("ProductName","table[class$=registration-table] tr:eq(2) td:eq(1)" );

        WebScraper  fnacScraper = new WebScraper("https://www.fnac.com/SearchResult/ResultList.aspx?Search=4713883543514");
        fnacScraper.addScrapedItem("ProductName", "div.Article-itemGroup p.Article-desc>a");

        WebScraper rktScraper = new WebScraper("https://fr.shopping.rakuten.com/s/4713883543514");
        rktScraper.addScrapedItem("ProductName", "div[class^=navItem] h2.productName");

        WebScraper amzScraper = new WebScraper("https://www.amazon.fr/s?k=4713883543514");
        amzScraper.addScrapedItem("ProductName", "a[href*=keywords=4713883543514]>span");

        WebScraper ebScraper = new WebScraper("https://www.ebay.fr/sch/i.html?_nkw=3365000015711");
        ebScraper.addScrapedItem("ProductName", "h3.lvtitle>a");

        WebScraper mavScraper = new WebScraper("https://maison-a-vivre.com/recherche?search_query=3232870161711");
        mavScraper.addScrapedItem("Mark","div.content_price>div");
        mavScraper.addScrapedItem("ProductName","div.content_price>a");

        myJsInterface.setScraper(bcdScraper);
        webview.loadUrl(bcdScraper.getUrl());
        */

        WebScraper bcdScraper = new WebScraper("https://barcodesdatabase.org/barcode/9421021461303");
        bcdScraper.addScrapedItem("tableItem", "table[class$=registration-table] >tbody");

        WebScraper  fnacScraper = new WebScraper("https://www.fnac.com/SearchResult/ResultList.aspx?Search=4713883543514");
        fnacScraper.addScrapedItem("tableItem", "div.Article-itemGroup");

        myJsInterface.addScraper(bcdScraper);
        myJsInterface.addScraper(fnacScraper);
        myJsInterface.setIndex(0);
        webview.loadUrl(bcdScraper.getUrl());
    }


    class MyJsInterface {

        private final Handler uiHandler = new Handler();
        Context ctx;
        ArrayList<WebScraper> scrapers = new ArrayList<>();
        int index = 0;
        Elements elements = new Elements();

        MyJsInterface(Context ctx) { this.ctx = ctx; }

        public void addScraper(WebScraper scraper) { scrapers.add(scraper); }
        public void setIndex(int index) { this.index = index; }
        public Elements getElements() { return elements; }

        @JavascriptInterface
        public void showHTML(String html) {
            final String htmlContent = html;

            uiHandler.post(
                new Runnable() {
                    @Override
                    public void run() {
                        if (index >= scrapers.size()) return;
                        /*
                        WebScraper scraper = scrapers.get(0);
                        scrapers.get(0).run(htmlContent);
                        String result = scraper.getText();

                        if (!result.isEmpty()) editText.setText(result);
                        else editText.setText("Not found");
                        editText.setText(scraper.getText("Owner"));
                        */

                        WebScraper scraper = scrapers.get(index);
                        Log.d("Scraping", scraper.getUrl());
                        scraper.run(htmlContent);
                        Element elm = scraper.getElement("tableItem");
                        if (elm != null) {
                            elements.add(elm);
                            Log.d("Element", "successful");
                        }
                        else {
                            Log.d("Element", "unsuccessful");
                        }
                        ++index;
                        if (index < scrapers.size()) {
                            webview.loadUrl(scrapers.get(index).getUrl());
                        }
                        else {
                            createTable();
                            index = 0;
                        }
                    }
                }
            );
        }

        public void createTable() {
            Log.d("table", "Create table");
            Element table = new Element("table");
            for (Element element: elements) {
                Element tr = new Element("tr");
                Element td = new Element("td");
                element.appendTo(td);
                td.appendTo(tr);
                tr.appendTo(table);
                Log.d("element", element.className());
            }
            Element body = new Element("body");
            Element html = new Element("html");
            table.appendTo(body);
            body.appendTo(html);
            Log.d("table content", html.toString());
            priceList.loadDataWithBaseURL("", html.toString(), "text/html", "UTF-8", "");
        }
    }

}
