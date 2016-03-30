package com.teamsight.touchvision;

import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.os.Handler;
import android.util.Log;

import com.teamsight.touchvision.sistelnetworks.vwand.BDevicesArray;
import com.teamsight.touchvision.sistelnetworks.vwand.Tag;
import com.teamsight.touchvision.sistelnetworks.vwand.Util;
import com.teamsight.touchvision.sistelnetworks.vwand.VWand;

import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * Created by aldrichW on 16-03-09.
 */
public class VWandReadIntentService extends IntentService {
    // Used to write to the system log from this class.
    public static final String LOG_TAG = "VWand Read Service";

    private BroadcastNotifier mBroadcastNotifier = null;

    private Tag tag = null;
    private String content = "";
    //VWand instance

    public static VWand vWand = null;

    /**
     * An IntentService must always have a constructor that calls the super constructor. The
     * string supplied to the super constructor is used to give a name to the IntentService's
     * background thread.
     */
    public VWandReadIntentService() {
        super("VWandReadIntentService");
        vWand = VWand.getInstance();
    }
    @Override
    protected void onHandleIntent(Intent workIntent) {
        // Gets data from the incoming Intent

        // Do work here, based on the contents of dataString
        if(null == mBroadcastNotifier){
            mBroadcastNotifier = new BroadcastNotifier(this);
        }

        try
        {
            //Wake-up device
            vWand.startvWand();

            mBroadcastNotifier.broadcastIntentWithState(Constants.STATE_ACTION_STARTED);

            tag = vWand.startDetectCard();

            if (tag != null)
            {
                //vWand read function
                NdefMessage message = vWand.readType2Tag();

                mBroadcastNotifier.broadcastIntentWithState(Constants.STATE_ACTION_PARSING);

                if (message != null) {
                    if (message.getRecords().length > 0) {

                        byte[] payload = message.getRecords()[0].getPayload();

                        content = new String(payload, Charset.forName("US-ASCII"));
                        //TODO:
                        //This is a shitty h4xx0r way of doing it, need to build a smarter parser here
                        content = content.substring(3);
                        MainActivity.tagContent = content;
                    }
                }

                mBroadcastNotifier.broadcastIntentWithState(Constants.STATE_ACTION_COMPLETE);
            }

        }catch(Exception e)
        {
            e.printStackTrace();
            Log.e(LOG_TAG, "Failed to read tag");
            onHandleIntent(workIntent);
        }
        }
}
