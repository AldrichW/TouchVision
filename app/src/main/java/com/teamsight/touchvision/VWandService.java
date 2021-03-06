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
            InitialActivity.vibe.vibrate(750);
            return true;
        }

        //Get all vwand devices and store in a vector
        Vector<Integer> vWandDevices = BluetoothService.getVWandDevices();

        if(null == vWandDevices){
            return false;
        }

        for (final Integer pos : vWandDevices) {
            if(attemptConnection(pos)){
                //This means attemptConnection returned true, so the connection was successful, notify user via haptic feedback and TTS
                InitialActivity.vibe.vibrate(750);
                return true;
            }
        }

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
}
