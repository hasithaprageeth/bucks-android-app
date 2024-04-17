package uom.prageeth.hasitha.bucks;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Currency;


public class CurrencyFragment extends Fragment {

    private ArrayAdapter<String> adapter ;

    public CurrencyFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    public void onCreateOptionsMenu(Menu menu,MenuInflater inflater) {
        inflater.inflate(R.menu.main_currency_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_refresh) {
            updateCurrency();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateCurrency(){
        FetchCurrencyRatesTask currencyRatesTask = new FetchCurrencyRatesTask();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String base = prefs.getString(getString(R.string.pref_base_currency_key),
                getString(R.string.pref_USD_value));
        currencyRatesTask.execute(base);
    }

    @Override
    public void onStart(){
        super.onStart();
        updateCurrency();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        adapter = new  ArrayAdapter<String>(getActivity(),R.layout.currency_item,R.id.currency_item_textview,new ArrayList<String>());

        ListView listView = (ListView)rootView.findViewById(R.id.currency_listview);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                // Two methods to get the currency data items at a specific position in the Adapter or AdapterView
                String currency = adapter.getItem(position);
               //String currency = (String) adapterView.getItemAtPosition(position);

                Intent intent = new Intent(getActivity(), CurrencyDetailActivity.class)
                                    .putExtra(Intent.EXTRA_TEXT,currency);
                startActivity(intent);
                //Toast.makeText(getActivity(),currency, Toast.LENGTH_SHORT).show();
            }
        });
        return rootView;
    }

    public class FetchCurrencyRatesTask extends AsyncTask<String,Void,String[]> {

        private final String LOG_TAG = FetchCurrencyRatesTask.class.getSimpleName();
        private final int DEFAULT_DECIMALS = 4;
        private String[] currencyTypeArray = {"EUR","GBP","AUD","USD","JPY","CAD","CHF","CNY","SGD","LKR"};

        private String getReadableDateTimeString(long time){
            SimpleDateFormat dateTimeFormat = new SimpleDateFormat("EEE, dd MMM yyyy hh:mm");
            return dateTimeFormat.format(time);
        }

        private Double setDecimals(Double rate, int decimals){
          return (new BigDecimal(rate)).setScale(decimals,BigDecimal.ROUND_HALF_UP).doubleValue();
        }

        private String[] getCurrencyRatesFromJson(String currencyJsonString, String base)
                throws JSONException {

            final String GER_RATE = "rate";
            final String GER_NAME = "name";

            JSONObject currencyJson = new JSONObject(currencyJsonString);

            Time timeObj = new Time(Time.getCurrentTimezone());
            timeObj.setToNow();
            String time = getReadableDateTimeString(timeObj.toMillis(false));

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            int decimals = Integer.parseInt(prefs.getString(getString(R.string.pref_decimals_key), getString(R.string.pref_decimals_4_value)));

            String[] resultStrs = new String[currencyJson.length() -1];

            JSONObject baseJsonObject = currencyJson.getJSONObject(base);
            resultStrs[0] = time + " - " + base
                                 + " - " + baseJsonObject.getString(GER_NAME)
                                 + " - " + (Currency.getInstance(base)).getSymbol()
                                 + " " + baseJsonObject.getDouble(GER_RATE);

            int i = 1;
            for(String key :currencyTypeArray){

                if (key.equals(base)){
                    continue;
                }
                JSONObject keyJsonObject = currencyJson.getJSONObject(key);

                Double rate = (decimals == DEFAULT_DECIMALS)? keyJsonObject.getDouble(GER_RATE): setDecimals(keyJsonObject.getDouble(GER_RATE),decimals);
                String name = keyJsonObject.getString(GER_NAME);
                String symbol = (Currency.getInstance(key)).getSymbol();

                resultStrs[i++] =  time + " - " + name + " - " + symbol + " " + rate ;
            }
            return resultStrs;
        }

        @Override
        protected String[] doInBackground(String... params) {

            if (currencyTypeArray.length == 0) {
                return null;
            }

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Raw JSON response as a string
            String currencyJsonString = null;

            String includeCurrencyNames = "names";

            StringBuilder currencyType = new StringBuilder();

            for(String currency :currencyTypeArray){
                currencyType.append(currency+",");
            }

            try {
                // http://www.getexchangerates.com/api/
                final String CURRENCY_RATES_BASE_URL = "http://www.getexchangerates.com/api/latest.json?";
                final String CURRENCY_BASE_PARAM = "base";
                final String CURRENCY_NAMES_PARAM = "include";
                final String CURRENCY_TYPES_PARAM = "currencies";

                Uri builtUri = Uri.parse(CURRENCY_RATES_BASE_URL).buildUpon()
                        .appendQueryParameter(CURRENCY_BASE_PARAM, params[0])
                        .appendQueryParameter(CURRENCY_NAMES_PARAM, includeCurrencyNames)
                        .appendQueryParameter(CURRENCY_TYPES_PARAM, currencyType.toString())
                        .build();

                URL url = new URL(builtUri.toString());

                // Create the request to Get Exchange Rates, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();

                if (inputStream == null) {
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                StringBuffer buffer = new StringBuffer();
                String line;

                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    return null;
                }
                currencyJsonString = buffer.toString();

            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                return null;

            } finally{
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }
            try{
                return getCurrencyRatesFromJson(currencyJsonString, params[0]);
            }catch (JSONException e){
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String[] result) {
            if(result != null) {
                adapter.clear();
                for (String item: result){
                        adapter.add(item);
                }
            }
        }
    }
}