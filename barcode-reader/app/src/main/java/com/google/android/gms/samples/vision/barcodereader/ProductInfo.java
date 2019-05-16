package com.google.android.gms.samples.vision.barcodereader;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.google.android.gms.vision.barcode.Barcode;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;


public class ProductInfo extends AppCompatActivity {
    private String title;
    private String url;
    private TextView productName;
    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_info);

        Barcode barcode = getIntent().getParcelableExtra("BARCODE");

        productName = (TextView) findViewById(R.id.productName);
        //form url
        url = "https://barcodesdatabase.org/barcode/9421021461303";

        final WebView myWebview = findViewById(R.id.productWebview);
        myWebview.getSettings().setLoadWithOverviewMode(true);
        myWebview.getSettings().setBuiltInZoomControls(true);

        myWebview.addJavascriptInterface(new MyJavaScriptInterface(this), "HtmlViewer");
        //myWebview.addJavascriptInterface(new MyJavaScriptInterface(this), "HTMLOUT");

        myWebview.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                myWebview.loadUrl("javascript:window.HtmlViewer.showHTML('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');");
                //myWebview.loadUrl("javascript:window.HTMLOUT.processHTML('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');");
            }
        });

        myWebview.loadUrl(url);

        //new Content().execute();
    }


    class MyJavaScriptInterface {

        private Context ctx;

        MyJavaScriptInterface(Context ctx) {
            this.ctx = ctx;
        }

        @JavascriptInterface
        @SuppressWarnings("unused")
        public void showHTML(final String html) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // new AlertDialog.Builder(ctx).setTitle("HTML").setMessage(html).setPositiveButton(android.R.string.ok, null).setCancelable(false).create().show();
                    Document document = Jsoup.parse(html);
                    Element mProductName = document.getElementsContainingOwnText("Product").first().nextElementSibling();
                    productName.setText(mProductName.text());
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
