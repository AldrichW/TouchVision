package com.teamsight.touchvision;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.os.Handler;
import android.util.Log;

import com.teamsight.touchvision.sistelnetworks.vwand.BDevicesArray;
import com.teamsight.touchvision.sistelnetworks.vwand.Tag;
import com.teamsight.touchvision.sistelnetworks.vwand.Util;
import com.teamsight.touchvision.sistelnetworks.vwand.VWand;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Vector;

/**
 * Created by aldrichW on 16-03-08.
 */
public class VWandService {

    // Debugging
    private static final String TAG = "ReadActivity";


    private Tag tag = null;
    private String content = "";
    public static BDevicesArray devices = new BDevicesArray();
    private Handler mHandler = new Handler();

    private boolean reading = true;

    // vWand object for connect and communicate to vWand
    public static VWand vWand = null;


    public VWandService(){
        //Constructor for the new VWand instance
        vWand = VWand.getInstance();
    }

    public boolean isVWandConnected(){
        return vWand.isConnected();
    }

    public boolean connectToVWand(){
        assert(vWand != null);

        if(vWand.isConnected()){
            //vWand instance is already connected
            MainActivity.vibe.vibrate(500);
            MainActivity.mT2Service.speakText("V-Wand is Connected");
            return true;
        }

        //Get all vwand devices and store in a vector
        Vector<Integer> vWandDevices = BluetoothService.getVWandDevices();

        if(null == vWandDevices){
            return false;
        }

        for (final Integer pos : vWandDevices) {
            MainActivity.mT2Service.speakText("Connecting to: V-Wand" + BluetoothService.devices.getDevice(pos).getName().substring(5));

            if(attemptConnection(pos)){
                MainActivity.vibe.vibrate(500);
                MainActivity.mT2Service.speakText("V-Wand is Connected");
                return true;
            }
        }

        MainActivity.mT2Service.speakText("V-Wand Failed to connect");
        return false;

    }

    public void disconnectVWand(){
        if(vWand.isConnected()) {
            vWand.disconnect();
        }
    }

    private boolean attemptConnection(final Integer position){
        try
        {
            BluetoothDevice device = BluetoothService.devices.getDevice(position);
            //Create vWand connection
            vWand.createConnection(device);

        }catch(IOException e)
        {
            System.out.println("Failed connected attempt");
            return false;
        }
        finally
        {
            if(vWand.isConnected()){
                return true;
            }

            return false;
        }
    }

    public boolean startReading(){
        if(null == vWand || !vWand.isConnected()){
            return false;
        }

        //VWand is connected and active
        //Start Reading background service

        new Thread(new Runnable() {
            public void run() {

                do{
                    try
                    {
                        //Wake-up device
                        vWand.startvWand();
                        tag = vWand.startDetectCard();

                        if (tag != null)
                        {
                            //vWand read function
                            NdefMessage message = vWand.readType2Tag();
                            // If the message is not empty
                            if (message != null)
                            {
                                if (message.getRecords().length > 0)
                                {

                                    byte[] payload = message.getRecords()[0].getPayload();
                                    byte[] uri = {(byte) 0x55};

                                    if (Arrays.equals(message.getRecords()[0].getType(), uri))
                                    {
                                        byte identifierCode = payload[0]; //The first byte is the URI identifier Code.

                                        String prefix = Util.getProtocolPrefix(identifierCode);
                                        String url = prefix +
                                                new String(payload, 1, payload.length -1, Charset.forName("US-ASCII"));
                                        //Create intent (only if NdefRecord is URI type.)
                                        try
                                        {
                                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
											/*startActivity(intent);*/
                                        }catch(Exception ex)
                                        {
                                            Log.e(TAG, "Exception thrown for starting intent");
                                        }
                                    }
                                    content = new String(payload, Charset.forName("US-ASCII"));
                                    content = content.substring(3);
                                    MainActivity.tagContent = content;//This is a shitty h4xx0r way of doing it, need to build a smarter parser here

                                    mHandler.post(new Runnable() {
                                        public void run() {

                                            //This is to make sure that if we're re-reading the same tag we don't keep repeating the content.
                                            if (!content.equals(com.teamsight.touchvision.MainActivity.previousTagContent)) {
                                                //This was for debug!
                                                MainActivity.previousTagContent = content;
                                            } else {
                                                //com.teamsight.touchvision.MainActivity.mT2Service.speakText("Should stop reading now");
                                                reading = false;
                                            }
                                        }
                                    });
                                }
                            }
                            Thread.sleep(1 * 1000); //Waiting to read again.
                        }

                    }catch(Exception e)
                    {
                        e.printStackTrace();
                        Log.e(TAG, "Failed to read tag");
                    }
                }while(reading);

            }
        }).start();

        return false;
    }
}
