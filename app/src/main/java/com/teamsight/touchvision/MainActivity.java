package com.teamsight.touchvision;

import android.app.Activity;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.Locale;

public class MainActivity extends NFCAbstractReadActivity {
    private TextToSpeechService mT2Service;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mT2Service = new TextToSpeechService(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                System.out.println("Text To Speech Service started!");
                if(TextToSpeech.SUCCESS == status){
                    if(mT2Service.setVoice(Locale.US)){
                        System.out.println("Voice set successfully!");
                    }
                }
            }
        });
        mT2Service.startService();
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
                try{
                    JSONObject jsonOut= new JSONObject(postOutput);
                    // Hard coding JSON key names. gross.
                    // Going to make a dedicated JSONParserService. Stay tuned
                    final String productName = jsonOut.getString("product_name");
                    textView.post(new Runnable() {
                        @Override
                        public void run() {
                            textView.setText(productName);
                            mT2Service.speakText(productName);
                        }
                    });
                }
                catch(Exception e){

                }
            }
        }).start();



    }
}
