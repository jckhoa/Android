package com.google.android.gms.samples.vision.barcodereader;

import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.HashMap;

public class WebScraper {
    String name;
    HashMap<String, WebScraperItem> items;
    String url;

    public WebScraper(String name, String url) {
        this.name = name;
        this.url = url;
        this.items = new HashMap<>();
    }

    public void addScrapedItem(String id, String command) {
        items.put(id, new WebScraperItem(command));
    }

    public String getName() { return name; }
    public String getUrl() { return url;}

    public void run(String webContent) {
        Document doc = Jsoup.parse(webContent);
        for (HashMap.Entry<String, WebScraperItem> item: items.entrySet()) {
            Elements elms = doc.select(item.getValue().getCommand());
            if (elms != null && !elms.isEmpty()) {
                item.getValue().setElement(elms.first());
            }
        }
    }

    public String getText() {
        String result = "";
        for (HashMap.Entry<String, WebScraperItem> item: items.entrySet()) {
            WebScraperItem val = item.getValue();
            String itemText = item.getValue().getText();
            result += item.getValue().getText() + " ";
        }
        return result.trim();
    }

    public String getText(String id) {
        WebScraperItem item = items.get(id);
        if (item != null) {
            return item.getText();
        }
        else {
            return "";
        }
    }

    public Element getElement(String id) {
        WebScraperItem item = items.get(id);
        if (item != null) return item.getElement();
        else return null;
    }

    class WebScraperItem {
        String command;
        Element element;

        public WebScraperItem(String command) {
            this.command = command;
            this.element = null;
        }

        public void setElement(Element element) { this.element = element; }
        public String getCommand() { return command; }
        public Element getElement() { return element;}
        public String getText() {
            if (element != null) {
                return element.text();
            }
            else {
                return "";
            }
        }
    }

}
