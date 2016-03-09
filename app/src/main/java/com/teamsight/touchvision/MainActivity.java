package com.teamsight.touchvision;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.teamsight.touchvision.sistelnetworks.activities.ReadActivity;
import com.teamsight.touchvision.sistelnetworks.vwand.BDevicesArray;
import com.teamsight.touchvision.sistelnetworks.vwand.VWand;

import org.json.JSONObject;

import java.util.Locale;


public class MainActivity extends NFCAbstractReadActivity {
    public static TextToSpeechService mT2Service = null;
    public static VWandService mVWandService = null;
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    public static Vibrator vibe;


    //JSON Output key constants
    private static final String PRODUCT_KEY = "product";
    private static final String PRODUCT_NAME_KEY = "name";
    private static final String PRICE_KEY = "price";
    private static final String QUANTITY_KEY = "quantity";
    private static final String TYPE_KEY = "type";
    private static final String NUTRITION_KEY = "nutrition";

    // Intent request codes
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_CONNECT = 2;
    private static final int REQUEST_READ = 4;


    private Button productButton;
    private Button nutritionButton;

    private String productName;
    private double price;
    private int quantity;
    private String quantityUnit;
    private String priceString;
    private String quantityString;
    private int calorieCount;
    private String calorieString;
    private static com.teamsight.touchvision.sistelnetworks.activities.MainActivity vWandMainActivity;


    // Array of Bluetooth detected devices
    public static BDevicesArray devices = new BDevicesArray();

    // vWand object for connect and communicate to vWand
    public static VWand vWand = null;

    //Tag contents from vWand reads
    public static String tagContent = null;
    public static String previousTagContent = null;


    private Intent mIntentService;
    private VWandStateReceiver mVWandStateReceiver;



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

        mVWandService = new VWandService(); //Create instance of VWand

        vibe = (Vibrator) getSystemService( VIBRATOR_SERVICE );

        // The filter's action is BROADCAST_ACTION
        IntentFilter statusIntentFilter = new IntentFilter(
                Constants.BROADCAST_ACTION);

        // Sets the filter's category to DEFAULT
        statusIntentFilter.addCategory(Intent.CATEGORY_DEFAULT);

        // Instantiates a new DownloadStateReceiver
        mVWandStateReceiver = new VWandStateReceiver();

        // Registers the DownloadStateReceiver and its intent filters
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mVWandStateReceiver,
                statusIntentFilter);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if(!BluetoothService.isBluetoothEnabled()){
            Intent enableBtIntent = BluetoothService.activateBluetoothAdapter();
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        while(!mVWandService.isVWandConnected()){
            mVWandService.connectToVWand();
        }

        mIntentService = new Intent(MainActivity.this, VWandReadIntentService.class);
        mIntentService.setData(null);
        this.startService(mIntentService);

    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        mVWandService.disconnectVWand();    //Kill off the VWand when the app is about to be destroyed.
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
        //TODO: BT service to monitor BT state to make sure that the vWand is still connected and is able to run
        //TODO: WIFI Monitoring in the HTTPBackendService.
        //TODO: May want to move the thread instantiation into the actual HTTPBackendService, that way the thread management is dealt with there
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
                    calorieString = nutritionOut.getString(NUTRITION_KEY); //At the moment the calorie info is being returned in the nutrition field

                    calorieString = parseCalories(calorieString);

                    textView.post(new Runnable() {
                        @Override
                        public void run() {

                            //First set the text on the screen, then use the T2S Service to read out all the info.
                            textView.setText(productName);
                            textView.setText(calorieString);

                            //Using the T2Service, output product name, product name, and quantity
                            sayProductInfo();
                            sayNutritionInfo();

                            mIntentService.setData(null);
                            MainActivity.this.startService(mIntentService);
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
        mT2Service.speakText(calorieString);
    }

    protected String parseCalories (String calString) {
        Integer calStartLoc = calorieString.indexOf(">") + 1;
        Integer calEndLoc = calorieString.indexOf("}");
        calString = calString.substring(calStartLoc, calEndLoc);

        calString += " calories";

        return calString;
    }


    /**
     * This function starts Read Activity.
     */
    public void startReadActivity() {
        Intent i = new Intent(this, ReadActivity.class);
        startActivityForResult(i, REQUEST_READ);
    }

    //This is a callback for the result of the activities that are called from here.
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_ENABLE_BT & resultCode == RESULT_OK) {
            assert(mVWandService != null);
            mVWandService.connectToVWand();

        } else if (requestCode == REQUEST_CONNECT & resultCode == RESULT_OK) {


            try
            {
                Toast.makeText(getApplicationContext(), "Successfully Connected to the vWand.", Toast.LENGTH_LONG).show(); //Sets View Layout properties
                startReadActivity();
            }catch(Exception e)
            {

            }

        } else if (requestCode == REQUEST_READ) {
            onTagRead(tagContent);
        }
    }

    private class VWandStateReceiver extends BroadcastReceiver {

        private VWandStateReceiver() {

            // prevents instantiation by other packages.
        }

        /**
         * This method is called by the system when a broadcast Intent is matched by this class'
         * intent filters
         *
         * @param context An Android context
         * @param intent  The incoming broadcast Intent
         */
        @Override
        public void onReceive(Context context, Intent intent) {

            /*
             * Gets the status from the Intent's extended data, and chooses the appropriate action
             */
            switch (intent.getIntExtra(Constants.EXTENDED_DATA_STATUS,
                    Constants.STATE_ACTION_COMPLETE)) {

                // Logs "started" state
                case Constants.STATE_ACTION_STARTED:
                    if (Constants.LOGD) {

                        Log.d(LOG_TAG, "State: STARTED");
                    }
                    break;
                // Logs "parsing the RSS feed" state
                case Constants.STATE_ACTION_PARSING:
                    if (Constants.LOGD) {

                        Log.d(LOG_TAG, "State: PARSING");
                    }
                    break;
                // Starts displaying data when the RSS download is complete
                case Constants.STATE_ACTION_COMPLETE:
                    // Logs the status
                    onTagRead(tagContent);
                    break;
                default:
                    break;
            }
        }

    }
}