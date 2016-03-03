package com.teamsight.touchvision;

import android.os.Bundle;
import android.app.Activity;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import com.teamsight.touchvision.sistelnetworks.vwand.VWand;

import java.util.Locale;

public class InitialActivity extends Activity {
    // vWand object for connect and communicate to vWand
    public static VWand vWand = null;   //vWand instance
    public static TextToSpeechService mT2Service;   //TextToSpeech Service
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    public static Vibrator vibe;    //Android haptic feedback

    //Let's do all of our connection initialization
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.initial);


        mT2Service = new TextToSpeechService(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {    //Initialization callback function gets called
                Log.d(LOG_TAG, "Text To Speech Service started!");
                if(TextToSpeech.SUCCESS == status){     //If TextToSpeech was started successfully
                    if(mT2Service.setVoice(Locale.US)){
                        Log.d(LOG_TAG, "Voice set successfully!");
                        Log.d(LOG_TAG, "Text To Speech service ready to take requests");
                    }
                }
            }
        });

        mT2Service.startService();

        vibe = (Vibrator) getSystemService( VIBRATOR_SERVICE ); // Android System haptic feedback
    }

    @Override
    public void onStart(){
        super.onStart();

        vWand = VWand.getInstance();

        assert(vWand != null);

        if(!vWand.isConnected()){
            //vWand is not connected yet, go to connection flow
            startVWandConnectActivity();
        }
    }

    public void startVWandConnectActivity(){


    }

}
