package com.teamsight.touchvision;

import android.app.Activity;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.Locale;

public class MainActivity extends NFCAbstractReadActivity {
    private TextToSpeechService mT2Service;
    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    //JSON Output key constants
    private static final String PRODUCT_KEY = "product";
    private static final String PRODUCT_NAME_KEY = "name";
    private static final String PRICE_KEY = "price";
    private static final String QUANTITY_KEY = "quantity";
    private static final String TYPE_KEY = "type";
    private static final String NUTRITION_KEY = "nutrition";
    private Button productButton;
    private Button nutritionButton;

    private String productName;
    private double price;
    private int quantity;
    private String quantityUnit;
    private String priceString;
    private String quantityString;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        productButton = (Button) this.findViewById(R.id.product_button);
        nutritionButton = (Button) this.findViewById(R.id.nutrition_button);

        productButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //If there are product info queued from a previous tag read
                //Voice out the product info again
                sayProductInfo();
            }
        });

        nutritionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //If there is nutritional info queued from a previous tag read
                //Voice out the nutritional info.
                sayNutritionInfo();
            }
        });

        mT2Service = new TextToSpeechService(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                Log.d(LOG_TAG, "Text To Speech Service started!");
                if(TextToSpeech.SUCCESS == status){
                    if(mT2Service.setVoice(Locale.US)){
                        Log.d(LOG_TAG, "Voice set successfully!");
                        Log.d(LOG_TAG, "Text To Speech service ready to take requests");
                    }
                }
            }
        });
        mT2Service.startService();
    }

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

    @Override
    protected void onTagRead(final String tagMessage){
        // TODO Auto-generated method stub
        final TextView textView = (TextView) this.findViewById(R.id.tag_message_text);

        final TextView productIDView = (TextView) this.findViewById(R.id.product_id_text);



        productIDView.setText(tagMessage);

        new Thread(new Runnable() {
            public void run() {
                String message = tagMessage;
                HTTPBackendService bs = new HTTPBackendService();
                String postData = bs.createPOSTDataWithProductIdentifier(message);
                Log.d(LOG_TAG, postData);
                final String postOutput = bs.sendPOSTRequest(null, postData);
                try{
                    JSONObject jsonOut= new JSONObject(postOutput);
                    Log.d(LOG_TAG, "The JSON Object response from the POST Network query.");
                    Log.d(LOG_TAG, jsonOut.toString());
                    // Hard coding JSON key names. gross.
                    // Going to make a dedicated JSONParserService. Stay tuned
                    JSONObject productOut = jsonOut.getJSONObject(PRODUCT_KEY);
                    productName = productOut.getString(PRODUCT_NAME_KEY);
                    price = productOut.getDouble(PRICE_KEY);
                    priceString = price + " dollars";
                    quantity = productOut.getInt(QUANTITY_KEY);
                    quantityUnit = productOut.getString(TYPE_KEY);
                    quantityString = String.valueOf(quantity) + " " + quantityUnit;

                    JSONObject nutritionOut = jsonOut.getJSONObject(NUTRITION_KEY);



                    textView.post(new Runnable() {
                        @Override
                        public void run() {
                            //Using the T2Service, output product name, product name, and quantity
                            textView.setText(productName);
                            sayProductInfo();
                        }
                    });
                }
                catch(Exception e){

                }
            }
        }).start();
    }

    protected void sayProductInfo(){
        mT2Service.speakText(productName);
        mT2Service.speakText(priceString);
        mT2Service.speakText(quantityString);
    }

    protected void sayNutritionInfo(){

    }
}
