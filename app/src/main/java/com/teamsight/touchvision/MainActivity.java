package com.teamsight.touchvision;

import android.content.Context;
import android.hardware.SensorManager;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.widget.Toast;

import com.capstone.knockknock.KnockDetector;

import org.json.JSONObject;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Document;

import java.util.Locale;
import java.util.Set;

import com.teamsight.touchvision.sistelnetworks.activities.ConnectActivity;
import com.teamsight.touchvision.sistelnetworks.vwand.BDevicesArray;
import com.teamsight.touchvision.sistelnetworks.vwand.VWand;



public class MainActivity extends NFCAbstractReadActivity {
    public static TextToSpeechService mT2Service;
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private KnockDetector mKnockDetector = null;
    public static Vibrator vibe;


    private static final String ID_KNOCK_DETECTOR_RESUME = "id_knockDetectorResume";

    //JSON Output key constants
    private static final String PRODUCT_KEY = "product";
    private static final String PRODUCT_NAME_KEY = "name";
    private static final String PRICE_KEY = "price";
    private static final String QUANTITY_KEY = "quantity";
    private static final String TYPE_KEY = "type";
    private static final String NUTRITION_KEY = "nutrition";
    private static final String CALORIE_KEY = "calories";

    // Intent request codes
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_CONNECT = 2;
    private static final int REQUEST_READ = 4;
    private static final int REQUEST_WRITE = 5;

    private Button productButton;
    private Button nutritionButton;

    // Default values so we can test without a tag read
    private String productName    = "no product name available";
    private double price;
    private int quantity;
    private String quantityUnit;
    private String priceString    = "no price available";
    private String quantityString = "no quantity available";
    private int calorieCount;
    private String calorieString  = "no calorie count available";

    private static com.teamsight.touchvision.sistelnetworks.activities.MainActivity vWandMainActivity;


    // Array of Bluetooth detected devices
    public static BDevicesArray devices = new BDevicesArray();

    // vWand object for connect and communicate to vWand
    public static VWand vWand = null;

    //Tag contents from vWand reads
    public static String tagContent = null;
    public static String previousTagContent = null;



    // Local Bluetooth Adapter
    private	BluetoothAdapter mBluetoothAdapter = null;
    private NfcAdapter mNFCAdapter = null;


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

                            if (utteranceId.equals(ID_KNOCK_DETECTOR_RESUME)) {
                                mKnockDetector.resume();
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

        mKnockDetector = new KnockDetector((SensorManager) this.getSystemService(Context.SENSOR_SERVICE)){
            @Override
            protected void knockDetected(int knockCount) {
                switch (knockCount){
                    case 0:
                        Log.d("knockDetected", "0 knocks");
                        mT2Service.speakText("Zero knocks detected.", true);
                        break;
                    case 1:
                        Log.d("knockDetected", "1 knocks");
                        mT2Service.speakText(mOutputOneKnock, true);
                        break;
                    case 2:
                        Log.d("knockDetected", "2 knocks");
                        mT2Service.speakText(mOutputTwoKnock, true);
                        break;
                    case 3:
                        Log.d("knockDetected", "3 knocks");
                        mT2Service.speakText(mOutputThreeKnock, true);
                        break;
                    default:
                        break;
                }

                mKnockDetector.pause();
            }
        };

        mKnockDetector.init();

       vibe = (Vibrator) getSystemService( VIBRATOR_SERVICE );
    }

    protected void onStart() {
        super.onStart();


        vWand = VWand.getInstance();

        if(!vWand.isConnected()){

            Toast tx;

            tx = Toast.makeText(getApplicationContext(),
                    "Bluetooth active", Toast.LENGTH_LONG);
            tx.show();

            activateBluetooth();
        }

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
        //TODO: BT service to monitor BT state to make sure that the vWand is still connected and is able to run
        //TODO: WIFI Monitoring in the HTTPBackendService.
        //TODO: May want to move the thread instantiation into the actual HTTPBackendService, that way the thread management is dealt with there
        new Thread(new Runnable() {
            public void run() {
                if(tagMessage.substring(0, 4).equals("ttc_")) {
                    onTtcTagRead(tagMessage.substring(4));
                }
                else {
                    onProductTagRead(tagMessage, textView);
                }
            }
        }).start();
    }


    protected void sayProductInfo() {
        // Service will be paused after knockDetected
        mKnockDetector.registerStrings(productName, priceString, quantityString);

        final String message = "Knock once for product name, twice for price and three times for quantity.";
        mT2Service.speakText(message, false, ID_KNOCK_DETECTOR_RESUME);
    }


    protected void sayNutritionInfo() {
        // Service will be paused after knockDetected
        mKnockDetector.registerStrings(calorieString, null, null);

        mT2Service.speakText("Knock once for calorie info.", false, ID_KNOCK_DETECTOR_RESUME);
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
                    sayNutritionInfo();
                }
            });
        }
        catch(Exception e) {

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

    protected void onTtcTagRead(final String tagData) {
        final int underscoreIndex = tagData.indexOf("_");

        final String stopId   = tagData.substring(0, underscoreIndex);
        final String routeTag = tagData.substring(underscoreIndex + 1);

        HTTPBackendService bs = new HTTPBackendService();
        Document document = bs.sendGETRequest(null, stopId, routeTag);

        Element predsElement = (Element) document.getElementsByTagName("predictions").item(0);

        final String stopTitle = predsElement.getAttribute("stopTitle");
        Log.d("parseTtcData", stopTitle);
        mT2Service.speakText("The stop is " + stopTitle, true);

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
            mT2Service.speakText("The arrival time for " + routeDirection + " is " + timeString, true);
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
     * This function refill the array of bonded devices. If Bluetooth is off then it is activated.*/

    public void activateBluetooth() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBluetoothAdapter != null) {
            // Device does not support Bluetooth
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(
                        BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
            else
            // Bluetooth adapter was on!
            {

                Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
                // If there are paired devices
                if (pairedDevices.size() > 0) {
                    devices.clearDevices();
                    // Loop through paired devices
                    for (BluetoothDevice device : pairedDevices) {
                        // Add the name and address to an array adapter to show in a
                        // ListView
                        devices.saveDevice(device);
                        // mArrayAdapter.add(device.getName() + "\n" +
                        // device.getAddress());

                    }
                    startConnectActivity();
                }
            }
        }
    }

    /*
     * This function starts Connect Activity*/

    public void startConnectActivity() {
        Intent i;
        i = new Intent(this,  ConnectActivity.class);
        startActivityForResult(i, REQUEST_CONNECT);
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

            mBluetoothAdapter = BluetoothAdapter
                    .getDefaultAdapter();

            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
            // If there are paired devices
            if (pairedDevices.size() > 0) {
                devices.clearDevices();
                // Loop through paired devices
                for (BluetoothDevice device : pairedDevices) {
                    // Add the name and address to an array adapter to show in a
                    // ListView
                    devices.saveDevice(device);


                }
                startConnectActivity();
            }

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


}
