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
        myWebview.getSettings().setUserAgentString("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:67.0) Gecko/20100101 Firefox/67.0");

        jsInterface = new MyJavaScriptInterface(this);
        myWebview.addJavascriptInterface(jsInterface, "HtmlViewer");

        myWebview.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                view.loadUrl("javascript:window.HtmlViewer.showHTML('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');");
            }
        });

        //myWebview.loadUrl(url);



        HashMap<String, MyTable.MyTableData> data = myTable.getTableData();
        MyTable.MyTableData item = data.get(barcodeValue);
        if (item != null) {
            productName.setText(item.getValue());
            return;
        }


        //WebScraper bcdScraper = new WebScraper("https://barcodesdatabase.org/barcode/9421021461303");
        WebScraper bcdScraper = new WebScraper("https://barcodesdatabase.org/barcode/" + barcodeValue);
        bcdScraper.addScrapedItem("Owner", "table[class$=registration-table] tr:eq(1) td:eq(1)");
        bcdScraper.addScrapedItem("ProductName","table[class$=registration-table] tr:eq(2) td:eq(1)" );

        //WebScraper  fnacScraper = new WebScraper("https://www.fnac.com/SearchResult/ResultList.aspx?Search=4713883543514");
        WebScraper  fnacScraper = new WebScraper("https://www.fnac.com/SearchResult/ResultList.aspx?Search=" + barcodeValue);
        fnacScraper.addScrapedItem("ProductName", "div.Article-itemGroup p.Article-desc>a");

        //WebScraper rktScraper = new WebScraper("https://fr.shopping.rakuten.com/s/4713883543514");
        WebScraper rktScraper = new WebScraper("https://fr.shopping.rakuten.com/s/" + barcodeValue);
        rktScraper.addScrapedItem("ProductName", "div[class^=navItem] h2.productName");

        //WebScraper amzScraper = new WebScraper("https://www.amazon.fr/s?k=4713883543514");
        WebScraper amzScraper = new WebScraper("https://www.amazon.fr/s?k=" + barcodeValue);
        amzScraper.addScrapedItem("ProductName", "a[href*=keywords=4713883543514]>span");

        //WebScraper ebScraper = new WebScraper("https://www.ebay.fr/sch/i.html?_nkw=3365000015711");
        WebScraper ebScraper = new WebScraper("https://www.ebay.fr/sch/i.html?_nkw=" + barcodeValue);
        ebScraper.addScrapedItem("ProductName", "h3.lvtitle>a");

        //WebScraper mavScraper = new WebScraper("https://maison-a-vivre.com/recherche?search_query=3232870161711");
        WebScraper mavScraper = new WebScraper("https://maison-a-vivre.com/recherche?search_query=" + barcodeValue);
        mavScraper.addScrapedItem("Mark","div.content_price>div");
        mavScraper.addScrapedItem("ProductName","div.content_price>a");

        ArrayList<WebScraper> scrapers = new ArrayList<>();
        scrapers.add(mavScraper);
        scrapers.add(bcdScraper);
        scrapers.add(rktScraper);
        scrapers.add(amzScraper);
        scrapers.add(ebScraper);
        scrapers.add(fnacScraper);

        scrapeWeb(scrapers);

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

    public void scrapeWeb(ArrayList<WebScraper> scrapers) {

        progressDialog = new ProgressDialog(ProductInfo.this);
        progressDialog.setTitle("Chercher l'info du produit");
        progressDialog.setMessage("Recherche " + scrapers.get(0).getUrl());
        progressDialog.setIndeterminate(false);
        progressDialog.show();

        jsInterface.setWebScraper(scrapers);
        myWebview.loadUrl(scrapers.get(0).getUrl());
    }

    class MyJavaScriptInterface {
        private Context ctx;
        private ArrayList<WebScraper> scrapers;
        private int index = 0;

        MyJavaScriptInterface(Context ctx) {
            this.ctx = ctx;
        }

        public void setWebScraper(ArrayList<WebScraper> scrapers) {
            this.scrapers = scrapers;
            this.index = 0;
        }

        @JavascriptInterface
        @SuppressWarnings("unused")
        public void showHTML(final String html) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //new AlertDialog.Builder(ctx).setTitle("HTML").setMessage(html).setPositiveButton(android.R.string.ok, null).setCancelable(false).create().show();
                    if (index >= scrapers.size()) {
                        progressDialog.dismiss();
                        return;
                    }

                    WebScraper scraper = scrapers.get(index);
                    scraper.run(html);
                    String text = scraper.getText();

                    if (text != null && !text.isEmpty()) {
                        productName.setText(text);
                        progressDialog.dismiss();
                        index = 0;
                    } else {
                        ++index;
                        if (index < scrapers.size()) {
                            progressDialog.setMessage("Recherche " + scrapers.get(index).getUrl());
                            myWebview.loadUrl(scrapers.get(index).getUrl());
                        } else {
                            progressDialog.dismiss();
                            index = 0;
                        }
                    }
                }
            });

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
