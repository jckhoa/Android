package com.google.android.gms.samples.vision.barcodereader;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashMap;


public class ProductInfo extends AppCompatActivity {
    private String title;
    private String url;
    private TextView productName;
    private TextView barcodeTextView;
    private ProgressDialog progressDialog;
    private WebView myWebview;
    private MyJavaScriptInterface jsInterface;
    private String barcodeValue;
    private MyTable myTable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_info);

        myTable = (MyTable) getIntent().getExtras().getSerializable("TABLE");

        Barcode barcode = getIntent().getParcelableExtra("BARCODE");
        barcodeValue = barcode.displayValue;
        barcodeTextView = findViewById(R.id.barcode);
        barcodeTextView.setText(barcode.displayValue);

        productName = findViewById(R.id.productName);



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
        ArrayList<WebScraperRequest> barcodeDatabaseInstruction = new ArrayList<>();
        barcodeDatabaseInstruction.add(new WebScraperRequest(ScraperCommand.TEXT,"Product", 0));
        barcodeDatabaseInstruction.add(new WebScraperRequest(ScraperCommand.SIBLING,"", 1));
        WebScraper barcodeDatabaseScraper = new WebScraper(barcodeDatabaseInstruction);
        scrapeWeb(barcodeDatabaseUrl, barcodeDatabaseScraper);
        */

        HashMap<String, MyTable.MyTableData> data = myTable.getTableData();
        MyTable.MyTableData item = data.get(barcodeValue);
        if (item != null) {
            productName.setText(item.getValue());
            return;
        }

        String amazonUrl = "https://www.amazon.fr/s?k=4007371062459";
        ArrayList<WebScraperRequest> amazonInstruction = new ArrayList<>();
        amazonInstruction.add(new WebScraperRequest(ScraperCommand.CSS, "span[data-component-type=s-search-results]", 0));
        amazonInstruction.add(new WebScraperRequest(ScraperCommand.CSS, "a[href*=4007371062459]", 2));
        amazonInstruction.add(new WebScraperRequest(ScraperCommand.CSS, "span", 0));
        WebScraper amazonScraper = new WebScraper(amazonInstruction);

        progressDialog = new ProgressDialog(ProductInfo.this);
        progressDialog.setTitle("Connect for product information");
        progressDialog.setMessage("Obtaining product information...");
        progressDialog.setIndeterminate(false);
        progressDialog.show();

        scrapeWeb(amazonUrl, amazonScraper);

        //new Content().execute();
    }

    public void onClick(View view) {
        Intent intent = new Intent(this, BarcodeCaptureActivity.class);
        switch (view.getId()) {
            case R.id.confirmProduct:
                intent.putExtra("BARCODE", barcodeValue);
                intent.putExtra("PRODUCT_NAME", productName.getText().toString());
                setResult(CommonStatusCodes.SUCCESS_CACHE, intent);
                break;
            case R.id.cancelProduct:
                setResult(CommonStatusCodes.CANCELED, intent);
                break;
            case R.id.rescanProduct:
                setResult(CommonStatusCodes.ERROR, intent);
                break;
        }
        finish();
    }
    public void scrapeWeb(String url, WebScraper scraper) {
        jsInterface.setWebScraper(scraper);
        myWebview.loadUrl(url);
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
                    progressDialog.dismiss();
                }
            });

        }

    }

    public enum ScraperCommand {
        CSS, TEXT, SIBLING
    }

    class WebScraperRequest {
        public ScraperCommand command;
        public String request;
        public int index;

        public WebScraperRequest(ScraperCommand command, String request, int index) {
            this.command = command;
            this.request = request;
            this.index = index;
        }
    }

    class WebScraper {
        private ArrayList<WebScraperRequest> requests;
        private String result;

        WebScraper(ArrayList<WebScraperRequest> requests) {
            this.requests = requests;
        }

        public String getResult() {
            return result;
        }

        public void scrape(String html) {
            Element elm = search(html);
            result = (elm == null ? "" : elm.text());
        }

        private Element search(String html){
            Element elm = Jsoup.parse(html).body();
            if (elm == null) return null;

            for (WebScraperRequest request: requests) {
                Elements elements = null;
                switch (request.command) {
                    case CSS:
                        elements = elm.select(request.request);
                        break;
                    case TEXT:
                        elements = elm.getElementsContainingOwnText(request.request);
                        break;
                    case SIBLING:
                        elements = elm.siblingElements();
                }
                if (elements.isEmpty()) return null;
                else {
                    elm = elements.eq(request.index).first();
                    if (elm == null) return null;
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
