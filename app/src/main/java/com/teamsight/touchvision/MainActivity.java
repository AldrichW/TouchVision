package com.teamsight.touchvision;

import android.app.Activity;
import android.nfc.NfcAdapter;
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

import java.util.Set;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.teamsight.touchvision.sistelnetworks.activities.*;
import com.teamsight.touchvision.sistelnetworks.activities.ConnectActivity;
import com.teamsight.touchvision.sistelnetworks.vwand.BDevicesArray;
import com.teamsight.touchvision.sistelnetworks.vwand.VWand;


public class MainActivity extends NFCAbstractReadActivity {
    public static TextToSpeechService mT2Service;
    private static final String LOG_TAG = MainActivity.class.getSimpleName();

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


    // Local Bluetooth Adapter
    private	BluetoothAdapter mBluetoothAdapter = null;
    private NfcAdapter mNFCAdapter = null;



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

        /*        vWandMainActivity = new com.teamsight.touchvision.sistelnetworks.activities.MainActivity();
        vWandMainActivity.activateBluetooth();*/

        vWand = VWand.getInstance();

        activateBluetooth();

        Toast tx;

        tx = Toast.makeText(getApplicationContext(),
                "Bluetooth active", Toast.LENGTH_LONG);
        tx.show();
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
     * This function refill the array of bonded devices. If Bluetooth is off then it is activated.*/

    public void activateBluetooth() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        /*mNFCAdapter = NfcAdapter.getDefaultAdapter();
        if(mNFCAdapter != null) {
            if(!mNFCAdapter.isEnabled()){
                Intent enableNFCIntent = new Intent(NfcAdapter.)
            }
        }*/

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

            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter
                    .getBondedDevices();
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
        if (requestCode == REQUEST_CONNECT & resultCode == RESULT_OK) {


            try
            {
                Toast.makeText(getApplicationContext(), "Successfully Connected to the vWand.", Toast.LENGTH_LONG).show(); //Sets View Layout properties
                startReadActivity();
            }catch(Exception e)
            {

            }

        }
    }


}