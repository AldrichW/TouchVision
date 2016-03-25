package com.teamsight.touchvision;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;

import com.teamsight.touchvision.sistelnetworks.vwand.BDevicesArray;

import java.util.Set;
import java.util.Vector;

/**
 * Created by aldrichW on 16-03-08.
 */
public class BluetoothService {

    private static BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    public static BDevicesArray devices = new BDevicesArray();

    public static boolean isBluetoothEnabled(){
        return mBluetoothAdapter.isEnabled();
    }

    public static Intent activateBluetoothAdapter(){
        if (mBluetoothAdapter != null) {
            // Device does not support Bluetooth
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(
                        BluetoothAdapter.ACTION_REQUEST_ENABLE);
                return enableBtIntent;
            }
        }
        //Bluetooth adapter is either already enabled or the device doesn't support bluetooth
        return null;
    }


    public static Vector<Integer> getVWandDevices(){

        if(!isBluetoothEnabled()){
            return null;    //Bluetooth is not enabled.
        }

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        // If there are paired devices
        if (pairedDevices.size() > 0) {
            devices.clearDevices();
            // Loop through paired devices
            for (BluetoothDevice device : pairedDevices) {
                // Add the name and address to an array adapter to show in a
                // ListView
                devices.saveDevice(device);
                // mArrayAdapter.add(device.getName() + "\n" +
                // device.getAddress());

            }
        }

        return devices.getvWands();
    }


}
