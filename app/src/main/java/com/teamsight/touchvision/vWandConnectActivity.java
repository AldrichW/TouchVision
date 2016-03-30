package com.teamsight.touchvision;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.widget.ImageView;

public class vWandConnectActivity extends Activity {

    public static VWandService mVWandService = null;

    private TextToSpeechService mT2SService = null;

    private ImageView bluetoothImage;
    private ImageView wifiImage;
    private ImageView vWandImage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_v_wand_connect);

        bluetoothImage = (ImageView) findViewById(R.id.bluetooth_image);
        wifiImage = (ImageView) findViewById(R.id.wifi_image);
        vWandImage = (ImageView) findViewById(R.id.vwand_image);

        mT2SService = InitialActivity.mT2Service;
        mVWandService = new VWandService(); //Create instance of VWand
    }

    @Override
    protected void onStart(){
        super.onStart();
        assert(mT2SService != null && mVWandService != null);

        mT2SService.speakText("V-Wand is Connecting.", TextToSpeechService.FLUSH_IF_BUSY);

        Thread thread = new Thread() {
            @Override
            public void run() {
                while(!mVWandService.isVWandConnected()){
                    mVWandService.connectToVWand();
                }

                Handler mainHandler = new Handler(getApplicationContext().getMainLooper());

                Runnable myRunnable = new Runnable() {
                    @Override
                    public void run() {
                        startMainInterfaceActivity();
                    }
                };
                mainHandler.post(myRunnable);
            }
        };

        thread.start();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        mVWandService.disconnectVWand();
    }


    private void startMainInterfaceActivity(){
        Intent newIntent = new Intent(this, MainActivity.class);

        startActivity(newIntent);
    }

}
