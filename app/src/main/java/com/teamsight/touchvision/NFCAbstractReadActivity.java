package com.teamsight.touchvision;

import android.app.ActionBar;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.Settings;
import android.widget.Toast;

/**
 * Created by aldrichW on 15-11-20.
 */
public abstract class NFCAbstractReadActivity extends Activity {
    private NfcAdapter mNfcAdapter;
    private IntentFilter [] mNdefExchangeFilter;
    private PendingIntent mPendingNfcIntent;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        //Access the current application context
        mContext = getApplicationContext();
        //Let's just use the default NFC Adapter instance that android provides
        mNfcAdapter = NfcAdapter.getDefaultAdapter(mContext);
        //Pending intent allows third party-apps to transition to our app when a specific event or notification occurs.
        mPendingNfcIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP), 0);
        IntentFilter filter = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        mNdefExchangeFilter = new IntentFilter[]{filter};
    }

    @Override
    protected void onResume(){
        super.onResume();
        //No NfcAdapter instance exists, NFC may not be supported.
        if(mNfcAdapter == null) {
            Toast.makeText(mContext, "NFC is not supported", Toast.LENGTH_SHORT).show();
            return;
        }

        //The hardware device supports NFC, but is currently disabled in the system settings
        if(!mNfcAdapter.isEnabled()){
            Toast.makeText(mContext, "Please enable your NFC", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
            startActivity(intent);
        }

        //Cool, we have an initialized NFC adapter and it's enabled. Onwards
        mNfcAdapter.enableForegroundDispatch(this, mPendingNfcIntent, mNdefExchangeFilter, null);

    }

    @Override
    protected void onPause(){
        //Make sure we disable foreground dispatch to avoid an exception being thrown
        if(mNfcAdapter != null){
            mNfcAdapter.disableForegroundDispatch(this);
        }
        super.onPause();

    }

    @Override
    protected void onNewIntent(Intent intent){
        super.onNewIntent(intent);
        if(NfcAdapter.ACTION_TAG_DISCOVERED == intent.getAction()){
            NdefMessage[] messages = null;
            Parcelable[] rawMessages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);

            if (rawMessages != null) {
                messages = new NdefMessage[rawMessages.length];
                for (int i = 0; i < rawMessages.length; i ++) {
                    messages[i] = (NdefMessage)rawMessages[i];
                }
            }
            //The tag info should be in the first element of the messages array
            if (messages != null && messages[0] != null) {
                StringBuilder result = new StringBuilder();
                byte[] payload = messages[0].getRecords()[0].getPayload();
                //Start iterating at value 3 in order to skip the language metadata 'en'
                for (int i = 3; i < payload.length; i ++) {
                    result.append((char)payload[i]);
                }
                onTagRead(result.toString());
            }
        }
    }

    protected abstract void onTagRead (String tagMessage);

}
