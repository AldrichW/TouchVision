package com.teamsight.touchvision;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.widget.ImageView;

public class vWandConnectActivity extends Activity {

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


        startMainInterfaceActivity();
    }


    private void startMainInterfaceActivity(){
        Intent newIntent = new Intent(this, MainActivity.class);

        startActivity(newIntent);

        finish();
    }

}
