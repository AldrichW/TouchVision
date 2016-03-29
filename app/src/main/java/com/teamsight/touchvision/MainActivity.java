package com.teamsight.touchvision;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.teamsight.touchvision.sistelnetworks.activities.ReadActivity;
import com.capstone.knockknock.KnockDetector;

import org.json.JSONObject;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Document;

import java.util.Locale;
import com.teamsight.touchvision.sistelnetworks.vwand.BDevicesArray;
import com.teamsight.touchvision.sistelnetworks.vwand.VWand;


public class MainActivity extends NFCAbstractReadActivity {
    public static TextToSpeechService mT2Service = null;
    public static VWandService mVWandService = null;
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private KnockDetector mKnockDetector = null;
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

    // Default values so we can test without a tag read
    private String productName    = "no product name available";
    private double price;
    private int quantity;
    private String quantityUnit;
    private String priceString    = "no price available";
    private String quantityString = "no quantity available";
    private String calorieString  = "no calorie count available";

    private static com.teamsight.touchvision.sistelnetworks.activities.MainActivity vWandMainActivity;


    // Array of Bluetooth detected devices
    public static BDevicesArray devices = new BDevicesArray();

    // vWand object for connecting to and communicating with vWand
    public static VWand vWand = null;

    //Tag contents from vWand reads
    public static String tagContent = null;
    public static String previousTagContent = null;

    //ToneGenerator to give us a beep on successful reads
    final ToneGenerator tg = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);


    private Intent mIntentService;
    private VWandStateReceiver mVWandStateReceiver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        productButton = (Button) this.findViewById(R.id.product_button);
        nutritionButton = (Button) this.findViewById(R.id.nutrition_button);

        productButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                //If there are product info queued from a previous tag read
                //Voice out the product info again
                sayProductInfo();
                return true;
            }
        });
        nutritionButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                //If there is nutritional info queued from a previous tag read
                //Voice out the nutritional info.
                sayNutritionInfo();
                return true;
            }
        });

        mT2Service = new TextToSpeechService(getApplicationContext(),
            new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                Log.d(LOG_TAG, "Text To Speech Service started!");
                if (TextToSpeech.SUCCESS == status) {
                    if (mT2Service.setVoice(Locale.US)) {
                        Log.d(LOG_TAG, "Voice set successfully!");
                        Log.d(LOG_TAG, "Text To Speech service ready to take requests");
                    }

                    mT2Service.setProgressListener(new UtteranceProgressListener() {

                        @Override
                        public void onStart(String utteranceId) {
                            Log.d("ProgressListener", "Speech Utterance Progress Started");
                        }

                        @Override
                        public void onDone(String utteranceId) {
                            Log.d("ProgressListener", "Speech Utterance Progress Ended: " + utteranceId);

                            if (utteranceId.equals(TextToSpeechService.ID_KNOCK_DETECTOR_RESUME)) {
                                if(mT2Service.isLastDone()){
                                    mKnockDetector.resume();
                                }
                            }
                        }

                        @Override
                        public void onError(String utteranceId) {
                            Log.d("ProgressListener", "Speech Utterance Error");
                        }
                    });
                }
            }
        });

        //Create our knock detector instance
        mKnockDetector = new KnockDetector((SensorManager) this.getSystemService(Context.SENSOR_SERVICE)){
            @Override
            protected void knockDetected(int knockCount) {
                switch (knockCount){
                    case 0:
                        Log.d("knockDetected", "0 knocks");
                        mT2Service.speakText("Zero knocks detected.", TextToSpeechService.DROP_IF_BUSY);
                        break;
                    case 1:
                        Log.d("knockDetected", "1 knocks");
                        mT2Service.speakText(mOutputOneKnock, TextToSpeechService.DROP_IF_BUSY);
                        break;
                    case 2:
                        Log.d("knockDetected", "2 knocks");
                        mT2Service.speakText(mOutputTwoKnock, TextToSpeechService.DROP_IF_BUSY);
                        break;
                    case 3:
                        Log.d("knockDetected", "3 knocks");
                        mT2Service.speakText(mOutputThreeKnock, TextToSpeechService.DROP_IF_BUSY);
                        break;
                    default:
                        break;
                }
                mKnockDetector.pause();
            }
        };

        mKnockDetector.init();

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
        final TextView textView      = (TextView) this.findViewById(R.id.tag_message_text);
        final TextView productIDView = (TextView) this.findViewById(R.id.product_id_text);

        productIDView.setText(tagMessage);
        new Thread(new Runnable() {
            public void run() {
                if(tagMessage.substring(0, 4).equals("ttc_")) {
                    onTtcTagRead(tagMessage.substring(4), textView);
                }
                else if(tagMessage.substring(0, 7).equals("poster_")) {
                    onPosterRead(tagMessage, textView);
                }
                else {
                    onProductTagRead(tagMessage, textView);
                }
            }
        }).start();
    }


    protected void sayProductInfo() {
        mKnockDetector.pause();
        mKnockDetector.registerStrings(priceString, quantityString, calorieString);

        final String message = "The product is " + productName +
                ". Knock once for the price, twice for the quantity, and three times for the calories.";
        mT2Service.speakText(message, TextToSpeechService.FLUSH_IF_BUSY,
                                      TextToSpeechService.ID_KNOCK_DETECTOR_RESUME);
    }


    protected void sayNutritionInfo() {
        mKnockDetector.pause();
        mKnockDetector.registerStrings(calorieString, null, null);

        final String message = "Knock once for calorie info.";
        mT2Service.speakText(message, TextToSpeechService.FLUSH_IF_BUSY,
                                      TextToSpeechService.ID_KNOCK_DETECTOR_RESUME);
    }


    protected void onPosterRead(final String message, final TextView textView) {
        HTTPBackendService bs = new HTTPBackendService();
        String postData = bs.createPOSTDataWithProductIdentifier(message);
        Log.d(LOG_TAG, postData);
        final String postOutput = bs.sendPOSTRequest(null, postData);
        try{
            JSONObject jsonOut = new JSONObject(postOutput);
            Log.d(LOG_TAG, "The JSON Object response from the POST Network query.");
            Log.d(LOG_TAG, jsonOut.toString());

            JSONObject productOut = jsonOut.getJSONObject(PRODUCT_KEY);
            productName = productOut.getString(PRODUCT_NAME_KEY);

            textView.post(new Runnable() {
                @Override
                public void run() {
                    textView.setText(productName);
                    mT2Service.speakText(productName, TextToSpeechService.FLUSH_IF_BUSY);
                }
            });
        }
        catch(Exception e) {
            Log.d("ERROR", e.getMessage());
        }
    }


    protected void onProductTagRead(final String message, final TextView textView) {
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

            //At the moment the calorie info is being returned in the nutrition field
            JSONObject nutritionOut = jsonOut.getJSONObject(NUTRITION_KEY);
            calorieString = nutritionOut.getString(NUTRITION_KEY); 
            calorieString = parseCalories(calorieString);

            textView.post(new Runnable() {
                @Override
                public void run() {
                    textView.setText(productName);
                    textView.setText(calorieString);

                    sayProductInfo();
                }
            });
        }
        catch(Exception e) {
            Log.d("ERROR", e.getMessage());
        }
    }


    // 1. Some examples of (stopId, routeTag) are:
    //    (0127, 511) - Bathurst at Dundas
    //    (3079, 501) - Queen E at Yonge
    //    (3088, 501) - Queen E at Spadina
    //    (3081, 301) - Queen W at Bathurst, 24 hours
    //    You can get streetcar numbers here https://www.ttc.ca/Routes/Streetcars.jsp
    //    and get the stops for a streetcar here, replacing NUMBER
    //    http://webservices.nextbus.com/service/publicXMLFeed?command=routeConfig&a=ttc&r=NUMBER
    // 2. The format expected for the NFC tag would be ttc_stopID_routeTag.  The prefix
    //    is parsed in onTagRead and passed in here as stopId_routeTag.
    //    Example: onTagRead("ttc_3081_301")

    protected void onTtcTagRead(final String tagData,final TextView textView) {
        final int underscoreIndex = tagData.indexOf("_");

        final String stopId   = tagData.substring(0, underscoreIndex);
        final String routeTag = tagData.substring(underscoreIndex + 1);

        HTTPBackendService bs = new HTTPBackendService();
        Document document = bs.sendGETRequest(null, stopId, routeTag);

        Element predsElement = (Element) document.getElementsByTagName("predictions").item(0);

        final String stopTitle = predsElement.getAttribute("stopTitle");

        textView.post(new Runnable() {
            @Override
            public void run() {
                textView.setText(stopTitle);
            }
        });

        Log.d("parseTtcData", stopTitle);
        mT2Service.speakText("The stop is " + stopTitle, TextToSpeechService.FLUSH_IF_BUSY);

        NodeList directionList = predsElement.getElementsByTagName("direction");

        for(int i=0; i < directionList.getLength(); i++)
        {
            Element direction = (Element) directionList.item(i);
            final String routeDirection = direction.getAttribute("title");
            Log.d("parseTtcData", routeDirection);

            Element closestPred = (Element) direction.getElementsByTagName("prediction").item(0);
            final String minutesUntilArrival = closestPred.getAttribute("minutes");
            Log.d("parseTtcData", minutesUntilArrival);

            int _secondsUntilArrival = Integer.parseInt(closestPred.getAttribute("seconds"));
            _secondsUntilArrival %= 60; // Seconds per minute
            final String secondsUntilArrival = Integer.toString(_secondsUntilArrival);
            Log.d("parseTtcData", secondsUntilArrival);

            final String timeString = minutesUntilArrival + " minutes " + secondsUntilArrival + " seconds";
            mT2Service.speakText("The arrival time for " + routeDirection + " is " + timeString, TextToSpeechService.FLUSH_IF_BUSY);
        }
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

            //Sets vibration pattern for a successful read; vibrate for 300ms, stop for 150, then repeat
            long[] vibePattern = {0, 300, 150, 300, 150};
            vibe.vibrate(vibePattern, 2);

            //Plays a beep sound to notify user of successful read.
            tg.startTone(ToneGenerator.TONE_PROP_BEEP);
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
                    assert(vibe != null);

                    //Sets vibration pattern for a successful read; vibrate for 300ms, stop for 150, then repeat
                    long[] vibePattern = {0, 300, 150, 300, 150};
                    vibe.vibrate(vibePattern, 2);

                    //Plays a beep sound to notify user of successful read.
                    tg.startTone(ToneGenerator.TONE_PROP_BEEP);
                    onTagRead(tagContent);
                    break;
                default:
                    break;
            }
        }

    }
}
