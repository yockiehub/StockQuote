package com.android.yockie.stockquote;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Created by palme_000 on 02/13/16.
 */
public class StockInfoActivity extends Activity {
    //To be able to identify messages from my app
    //in the LogCat
    private static final String TAG = "STOCKQUOTE";

    TextView companyNameTextView;
    TextView yearLowTextView;
    TextView yearHighTextView;
    TextView daysLowTextView;
    TextView daysHighTextView;
    TextView lastTradePriceOnlyTextView;
    TextView changeTextView;
    TextView daysRangeTextView;

    static final String KEY_ITEM = "quote"; // parent node
    static final String KEY_NAME = "Name";
    static final String KEY_YEAR_LOW = "YearLow";
    static final String KEY_YEAR_HIGH = "YearHigh";
    static final String KEY_DAYS_LOW = "DaysLow";
    static final String KEY_DAYS_HIGH = "DaysHigh";
    static final String KEY_LAST_TRADE_PRICE = "LastTradePriceOnly";
    static final String KEY_CHANGE = "Change";
    static final String KEY_DAYS_RANGE = "DaysRange";

    // XML Data to Retrieve
    String name = "";
    String yearLow = "";
    String yearHigh = "";
    String daysLow = "";
    String daysHigh = "";
    String lastTradePriceOnly = "";
    String change = "";
    String daysRange = "";

    // Used to make the URL to call for XML data
    String yahooURLFirst = "http://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20yahoo.finance.quote%20where%20symbol%20in%20(%22";
    String yahooURLSecond = "%22)&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys";

    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_info);

        //This is how we pass information from an activity
        //to another
        Intent intent = getIntent();
        String stockSymbol = intent.getStringExtra(MainActivity.STOCK_SYMBOL);

        //Initialize textViews
        companyNameTextView = (TextView) findViewById(R.id.companyNameTextView);
        yearLowTextView = (TextView) findViewById(R.id.yearLowTextView);
        yearHighTextView = (TextView) findViewById(R.id.yearHighTextView);
        daysLowTextView = (TextView) findViewById(R.id.daysLowTextView);
        daysHighTextView = (TextView) findViewById(R.id.daysHighTextView);
        lastTradePriceOnlyTextView = (TextView) findViewById(R.id.lastTradePriceOnlyTextView);
        changeTextView = (TextView) findViewById(R.id.changeTextView);
        daysRangeTextView = (TextView) findViewById(R.id.daysRangeTextView);

        // Sends a message to the LogCat
        Log.d(TAG, "Before URL Creation " + stockSymbol);

        // Create the YQL query
        final String yqlURL = yahooURLFirst + stockSymbol + yahooURLSecond;

        new MyAsyncTask().execute(yqlURL);


    }

    private class MyAsyncTask extends AsyncTask<String, String, String> {
        /**
         * Override this method to perform a computation on a background thread. The
         * specified parameters are the parameters passed to {@link #execute}
         * by the caller of this task.
         * <p/>
         * This method can call {@link #publishProgress} to publish updates
         * on the UI thread.
         *
         * @param params The parameters of the task.
         * @return A result, defined by the subclass of this task.
         * @see #onPreExecute()
         * @see #onPostExecute
         * @see #publishProgress
         */
        @Override
        protected String doInBackground(String... params) {
            try{
                URL url = new URL(params[0]);

                URLConnection connection;
                connection = url.openConnection();

                HttpURLConnection httpConnection = (HttpURLConnection) connection;

                int responseCode = httpConnection.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK){
                    InputStream in = httpConnection.getInputStream();
                    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                    DocumentBuilder db = dbf.newDocumentBuilder();
                    Document dom = db.parse(in);
                    Element docEle = dom.getDocumentElement();
                    NodeList nl = docEle.getElementsByTagName("quote");

                    if (nl != null && nl.getLength() > 0){
                        for (int i = 0; i < nl.getLength(); i++){
                            StockInfo theStock = getStockInformation(docEle);

                            name = theStock.getName();
                            yearLow = theStock.getYearLow();
                            yearHigh = theStock.getYearHigh();
                            daysLow = theStock.getDaysLow();
                            daysHigh = theStock.getDaysHigh();
                            lastTradePriceOnly = theStock.getLastTradePriceOnly();
                            change = theStock.getChange();
                            daysRange = theStock.getDaysRange();


                        }
                    }
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            } catch (SAXException e) {
                e.printStackTrace();
            }

            return null;
        }

        protected void onPostExecute(String result){
            companyNameTextView.setText(name);
            yearLowTextView.setText("Year low: " + yearLow);
            yearHighTextView.setText("Year high: " + yearHigh);
            daysHighTextView.setText("Days high: " + daysHigh);
            daysLowTextView.setText("Days low: " + daysLow);
            lastTradePriceOnlyTextView.setText("Last price: " + lastTradePriceOnly);
            changeTextView.setText("Change: " + change);
            daysRangeTextView.setText("Daily price range: " + daysRange);
        }

        private StockInfo getStockInformation(Element entry){
            String stockName = getTextValue(entry, "Name");
            String stockYearLow = getTextValue(entry, "YearLow");
            String stockYearHigh = getTextValue(entry, "YearHigh");
            String stockDaysLow = getTextValue(entry, "DaysLow");
            String stockDaysHigh = getTextValue(entry, "DaysHigh");
            String stockLastTradePriceOnly = getTextValue(entry, "LastTradePriceOnly");
            String stockChange = getTextValue(entry, "Change");
            String stockDaysRange = getTextValue(entry, "DaysRange");

            StockInfo theStock = new StockInfo(stockDaysLow, stockDaysHigh, stockYearLow,
                    stockYearHigh, stockName, stockLastTradePriceOnly,
                    stockChange, stockDaysRange);

            return theStock;

        }

        private String getTextValue(Element entry, String tagName){
            String tagValueToReturn = null;

            NodeList nl = entry.getElementsByTagName(tagName);

            if(nl != null && nl.getLength() > 0){
                Element element = (Element) nl.item(0);
                tagValueToReturn = element.getFirstChild().getNodeValue();
            }
            return tagValueToReturn;
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.stock_info, menu);
        return true;
    }

}
