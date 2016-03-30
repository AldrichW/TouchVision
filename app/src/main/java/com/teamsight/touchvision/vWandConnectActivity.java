package com.teamsight.touchvision;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
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

        Thread thread = new Thread() {
            @Override
            public void run() {
                mT2SService.speakText("Connecting to V-Wand. Please make sure V-Wand is powered on", TextToSpeechService.FLUSH_IF_BUSY);
                while(!mVWandService.isVWandConnected()){
                    mVWandService.connectToVWand();
                }

                Handler mainHandler = new Handler(getApplicationContext().getMainLooper());

                Runnable myRunnable = new Runnable() {
                    @Override
                    public void run() {
                        try{
                            showSuccessCheckmark();
                            mT2SService.speakText("V-Wand Connected.", TextToSpeechService.FLUSH_IF_BUSY);
                            sleep(3000); // sleep for 3 seconds
                            startMainInterfaceActivity();
                        }catch (InterruptedException e){
                            System.out.println(e.getMessage());
                            e.printStackTrace();
                            startMainInterfaceActivity();
                        }

                    }
                };
                mainHandler.post(myRunnable);
            }
        };

        thread.start();

        startBluetoothAnimation();  //When connecting to vWand show in progress by fading bluetooth image
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        mVWandService.disconnectVWand();
    }

    private void startBluetoothAnimation() {
        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setInterpolator(new DecelerateInterpolator()); //add this.
        fadeIn.setDuration(1000);
        fadeIn.setRepeatCount(Animation.INFINITE);

        Animation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setInterpolator(new AccelerateInterpolator()); //and this
        fadeOut.setStartOffset(1000);
        fadeOut.setDuration(1000);
        fadeOut.setRepeatCount(Animation.INFINITE);

        AnimationSet animation = new AnimationSet(false); //change to false
        animation.addAnimation(fadeIn);
        animation.addAnimation(fadeOut);
        animation.setRepeatMode(Animation.REVERSE);
        animation.setRepeatCount(Animation.INFINITE);
        bluetoothImage.setAnimation(animation);
    }

    private void showSuccessCheckmark(){
        bluetoothImage.setMaxWidth(150);
        bluetoothImage.setMaxHeight(150);
        bluetoothImage.setImageResource(R.mipmap.success_small);
    }


    private void startMainInterfaceActivity(){
        Intent newIntent = new Intent(this, MainActivity.class);

        startActivity(newIntent);
    }

}
