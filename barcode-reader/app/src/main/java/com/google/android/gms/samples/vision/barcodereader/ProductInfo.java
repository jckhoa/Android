package com.google.android.gms.samples.vision.barcodereader;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Pair;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.google.android.gms.vision.barcode.Barcode;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.ArrayList;


public class ProductInfo extends AppCompatActivity {
    private String title;
    private String url;
    private TextView productName;
    private ProgressDialog progressDialog;
    private WebView myWebview;
    private MyJavaScriptInterface jsInterface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_info);

        Barcode barcode = getIntent().getParcelableExtra("BARCODE");

        productName = (TextView) findViewById(R.id.productName);



        myWebview = findViewById(R.id.productWebview);
        myWebview.getSettings().setLoadWithOverviewMode(true);
        myWebview.getSettings().setBuiltInZoomControls(true);
        myWebview.getSettings().setJavaScriptEnabled(true);

        jsInterface = new MyJavaScriptInterface(this);
        myWebview.addJavascriptInterface(jsInterface, "HtmlViewer");
        //myWebview.addJavascriptInterface(new MyJavaScriptInterface(this), "HTMLOUT");

        myWebview.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                view.loadUrl("javascript:window.HtmlViewer.showHTML('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');");
                //myWebview.loadUrl("javascript:window.HTMLOUT.processHTML('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');");
            }
        });

        //myWebview.loadUrl(url);

        /*
        String barcodeDatabaseUrl = "https://barcodesdatabase.org/barcode/9421021461303";
        ArrayList<Pair<Character, Integer>> barcodeDatabaseInstruction = new ArrayList<Pair<Character, Integer>>();
        barcodeDatabaseInstruction.add(new Pair<Character, Integer>('l',1));
        WebScraper barcodeDatabaseScraper = new WebScraper("Product", 0, barcodeDatabaseInstruction);
        scrapeWeb(barcodeDatabaseUrl, barcodeDatabaseScraper);
        */
        String amazonUrl = "https://www.amazon.fr/s?k=4007371062459";
        ArrayList<Pair<Character, String>> amazonInstruction = new ArrayList<Pair<Character, String>>();
        //amazonInstruction.add(new Pair<Character, Integer>('l',0));
        amazonInstruction.add(new Pair<>('c',"span[data-component-type=s-search-results]"));
        amazonInstruction.add(new Pair<>('c',"div[class^=s-result-list]"));
        amazonInstruction.add(new Pair<>('c',"a[href*=4007371062459]"));
       // amazonInstruction.add(new Pair<>('c',"span"));
        WebScraper amazonScraper = new WebScraper(amazonInstruction);
        scrapeWeb(amazonUrl, amazonScraper);
        //new Content().execute();
    }

    public void scrapeWeb(String url, WebScraper scraper) {
        jsInterface.setWebScraper(scraper);
        myWebview.loadUrl(url);
    }

    public void scrapeWebSelect(String url, String searchText, int level) {

    }
    class MyJavaScriptInterface {
        private Context ctx;
        private WebScraper scraper;

        MyJavaScriptInterface(Context ctx) {
            this.ctx = ctx;
        }

        public void setWebScraper(WebScraper scraper) {
            this.scraper = scraper;
        }

        @JavascriptInterface
        @SuppressWarnings("unused")
        public void showHTML(final String html) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //new AlertDialog.Builder(ctx).setTitle("HTML").setMessage(html).setPositiveButton(android.R.string.ok, null).setCancelable(false).create().show();
                    scraper.scrape(html);
                    productName.setText(scraper.getResult());
                }
            });

        }

    }

    class WebScraper {
        ArrayList<Pair<Character, String>> instruction;
        String result;

        WebScraper(ArrayList<Pair<Character, String>> instruction) {
            this.instruction = instruction;
        }

        public String getResult() {
            return result;
        }

        public void scrape(String html) {

            Element elm = search(html);
            if (elm == null) {
                result = "non-trouvable";
            }
            else {
                //result = elm.text();
                result = elm.child(0).tagName();
            }
        }

        private Element search(String html){
            Element elm = Jsoup.parse(html).body();
            if (elm == null) return elm;

            for (Pair<Character, String> item: instruction) {
                if (item.first == 'd') {
                    Integer depth = Integer.valueOf(item.second);
                    // go down to children
                    for (Integer i = 0; i < depth; ++i) {
                        elm = elm.child(0);
                        if (elm == null) return elm;
                    }
                }
                else if (item.first == 'l'){
                    Integer siblings = Integer.valueOf(item.second);
                    for (Integer i = 0; i < siblings; i++ ) {
                        elm = elm.nextElementSibling();
                        if (elm == null) return elm;
                    }
                }
                else if (item.first == 'c') { //css
                    elm = elm.select(item.second).first();
                    if (elm == null) return elm;
                }
            }
            return elm;

        }
    }
/*
    private class Content extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(ProductInfo.this);
            progressDialog.setTitle("Connect for product information");
            progressDialog.setMessage("Obtaining product information...");
            progressDialog.setIndeterminate(false);
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                //Connect to the website
                Document document = Jsoup.connect(url).get();

                //Get the logo source of the website
                Element mProductName = document.getElementsContainingOwnText("Results").first();
                String mAuthorName = mProductName.text();
                //Get the title of the website
                title = document.title();

            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            productName.setText(title);
            progressDialog.dismiss();
        }
    } */
}
