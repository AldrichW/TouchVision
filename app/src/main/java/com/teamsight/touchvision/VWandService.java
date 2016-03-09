package com.teamsight.touchvision;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.widget.Button;
import android.widget.Toast;

import com.teamsight.touchvision.sistelnetworks.vwand.VWand;

import java.io.IOException;
import java.util.Vector;
import java.util.concurrent.RunnableFuture;

/**
 * Created by aldrichW on 16-03-08.
 */
public class VWandService {

    // vWand object for connect and communicate to vWand
    public static VWand vWand = null;


    public VWandService(){
        //Constructor for the new VWand instance
        vWand = VWand.getInstance();
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
            if(vWand.isConnected()){
                //vWand instance is already connected
                MainActivity.vibe.vibrate(500);
                MainActivity.mT2Service.speakText("V-Wand is Connected");
                return true;
            }
            MainActivity.mT2Service.speakText("Connecting to: V-Wand" + BluetoothService.devices.getDevice(pos).getName().substring(5));

            attemptConnection(pos);
        }
        if(vWand.isConnected()){
            //vWand instance is already connected
            MainActivity.vibe.vibrate(500);
            MainActivity.mT2Service.speakText("V-Wand is Connected");
            return true;
        }
        MainActivity.mT2Service.speakText("V-Wand Failed to connect");
        return false;

    }

    private void attemptConnection(final Integer position){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try
                {
                    BluetoothDevice device = BluetoothService.devices.getDevice(position);
                    //Create vWand connection
                    vWand.createConnection(device);

                }catch(IOException e)
                {
                    System.out.println("Failed connected attempt");
                }
                finally
                {
                    //Don't need to do anything here for now
                }
            }
        }).start();
    }
}
