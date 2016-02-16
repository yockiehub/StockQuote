package com.android.yockie.stockquote;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    public final static String STOCK_SYMBOL = "com.example.stockquote.STOCK";
    private SharedPreferences stockSymbolsEntered;
    private TableLayout stockTableScrollView;
    private EditText stockSymbolEditText;
    Button enterStockSymbolButton;
    Button deleteStocksButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Retrieve any save stocks entered by the user
        //MODE_PRIVATE means that this status will only be accessible from my app
        stockSymbolsEntered = getSharedPreferences("stockList", MODE_PRIVATE);

        //Initialize all the different components

        stockTableScrollView = (TableLayout) findViewById(R.id.stockTableScrollView);
        stockSymbolEditText = (EditText) findViewById(R.id.stockSymbolEditText);
        enterStockSymbolButton = (Button) findViewById(R.id.enterStockSymbolButton);
        deleteStocksButton = (Button) findViewById(R.id.deleteStocksButton);

        //Setting the click listeners

        enterStockSymbolButton.setOnClickListener(enterStockSymbolButtonListener);
        deleteStocksButton.setOnClickListener(deleteStocksButtonListener);

        //Update all stock entered

        updateSavedStockList(null);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    private void updateSavedStockList(String newStockSymbol){
        String[] stocks = stockSymbolsEntered.getAll().keySet().toArray(new String[0]);

        Arrays.sort(stocks, String.CASE_INSENSITIVE_ORDER);

        if (newStockSymbol != null){
                insertStockInScrollView(newStockSymbol, Arrays.binarySearch(stocks, newStockSymbol));
        } else {
            for (int i = 0; i < stocks.length; ++i){
                insertStockInScrollView(stocks[i],i);
            }
        }
    }

    private void saveStockSymbol(String newStock){
        String isTheStockNew = stockSymbolsEntered.getString(newStock, null);

        SharedPreferences.Editor preferencesEditor = stockSymbolsEntered.edit();

        preferencesEditor.putString(newStock, newStock);
        preferencesEditor.apply();

        if(isTheStockNew == null){
            updateSavedStockList(newStock);
        }

    }

    private void insertStockInScrollView(String stock, int arrayIndex){
        //This will insert the new created row into the scroll view
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View newStockRow = inflater.inflate(R.layout.stock_quote_row, null);
        TextView newStockTextView = (TextView) newStockRow.findViewById(R.id.StockSymbolTextView);
        newStockTextView.setText(stock);
        Button stockQuoteButton = (Button) newStockRow.findViewById(R.id.stockQuoteButton);
        stockQuoteButton.setOnClickListener(getStockActivityListener);
        Button quoteFromWebButton = (Button) newStockRow.findViewById(R.id.quoteFromWebButton);
        quoteFromWebButton.setOnClickListener(getStockFromWebsiteListenerListener);
        stockTableScrollView.addView(newStockRow, arrayIndex);

    }

    public View.OnClickListener enterStockSymbolButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(stockSymbolEditText.getText().length() > 0){
                saveStockSymbol(stockSymbolEditText.getText().toString());

                stockSymbolEditText.setText("");
                //Now we force the keyboard to close
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

                imm.hideSoftInputFromWindow(stockSymbolEditText.getWindowToken(), 0);
            }else{
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                builder.setTitle(R.string.invalid_stock_symbol);

                builder.setPositiveButton(R.string.ok, null);

                builder.setMessage(R.string.missing_stock_symbol);

                AlertDialog theAlertDialog = builder.create();
                theAlertDialog.show();
            }
        }
    };

    private void deleteAllStocks(){
        stockTableScrollView.removeAllViews();
    }

    public View.OnClickListener deleteStocksButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            deleteAllStocks();

            SharedPreferences.Editor preferencesEditor = stockSymbolsEntered.edit();
            preferencesEditor.clear();
            preferencesEditor.apply();
        }
    };

    public View.OnClickListener getStockActivityListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            TableRow tableRow = (TableRow) v.getParent();

            TextView stockTextView = (TextView) tableRow.findViewById(R.id.StockSymbolTextView);

            String stockSymbol = stockTextView.getText().toString();

            Intent intent = new Intent(MainActivity.this, StockInfoActivity.class);

            intent.putExtra(STOCK_SYMBOL, stockSymbol);

            startActivity(intent);
        }
    };

    public View.OnClickListener getStockFromWebsiteListenerListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            TableRow tableRow = (TableRow) v.getParent();

            TextView stockTextView = (TextView) tableRow.findViewById(R.id.StockSymbolTextView);

            String stockSymbol = stockTextView.getText().toString();

            String stockURL = getString(R.string.yahoo_stock_url) + stockSymbol;

            Intent getStockWebpage = new Intent(Intent.ACTION_VIEW, Uri.parse(stockURL));

            startActivity(getStockWebpage);
        }
    };


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
