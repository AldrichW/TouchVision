package com.teamsight.touchvision;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.os.PowerManager;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

import com.teamsight.touchvision.sistelnetworks.vwand.VWand;

import java.util.Locale;

public class InitialActivity extends Activity {
    // Intent request codes
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_CONNECT = 2;
    private static final int REQUEST_READ = 4;

    public static TextToSpeechService mT2Service;   //TextToSpeech Service
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    public static Vibrator vibe;    //Android haptic feedback

    private static PowerManager.WakeLock wakeLock;

    //Let's do all of our connection initialization
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.initial);

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);

        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "No Sleep");
        wakeLock.acquire();

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
                                }

                                @Override
                                public void onError(String utteranceId) {
                                    Log.d("ProgressListener", "Speech Utterance Error");
                                }
                            });

                            if(!BluetoothService.isBluetoothEnabled()){
                                Intent enableBtIntent = BluetoothService.activateBluetoothAdapter();
                                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                            }
                            else {
                                Intent i = new Intent(getApplicationContext(), vWandConnectActivity.class);

                                //Do we need to pass in any special variables

                                startActivity(i);
                            }
                        }
                        else{
                            mT2Service.startService();
                        }
                    }
                });

        mT2Service.startService();

        vibe = (Vibrator) getSystemService( VIBRATOR_SERVICE ); // Android System haptic feedback
    }

    @Override
    public void onStart(){
        super.onStart();

        if(!BluetoothService.isBluetoothEnabled()){
            Intent enableBtIntent = BluetoothService.activateBluetoothAdapter();
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    @Override
    protected  void onDestroy(){
        super.onDestroy();
        wakeLock.release();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_ENABLE_BT & resultCode == RESULT_OK) {
            Intent i = new Intent(getApplicationContext(), vWandConnectActivity.class);

            //Do we need to pass in any special variables
            startActivity(i);
        }
    }

    public void startVWandConnectActivity(){
        //Create a new Intent and transition to the vWand connection

        Intent i = new Intent(this, vWandConnectActivity.class);

        //Do we need to pass in any special variables

        startActivity(i);
    }

}
