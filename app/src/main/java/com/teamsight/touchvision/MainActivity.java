package com.teamsight.touchvision;

import android.app.Activity;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends NFCAbstractReadActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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

        new Thread(new Runnable() {
            public void run() {
                String message = tagMessage;
                HTTPBackendService bs = new HTTPBackendService();
                String postData = bs.createPOSTDataWithProductIdentifier(message);
                final String postOutput = bs.sendPOSTRequest(null, postData);
                textView.post(new Runnable() {
                    @Override
                    public void run() {
                        textView.setText(postOutput);
                    }
                });
            }
        }).start();



    }
}
