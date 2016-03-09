/*
Copyright (c) 2013, Sistelnetworks 

Permission is hereby granted, free of charge, to any
person obtaining a copy of this software and associated
documentation files (the "Software"), to deal in the
Software without restriction, including without limitation
the rights to use, copy, modify, merge, publish,
distribute, sublicense, and/or sell copies of the
Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice
shall be included in all copies or substantial portions of
the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY
KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS
OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR
OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package com.teamsight.touchvision.sistelnetworks.activities;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.teamsight.touchvision.sistelnetworks.activities.*;
import com.teamsight.touchvision.sistelnetworks.vwand.BDevicesArray;
import com.teamsight.touchvision.sistelnetworks.vwand.VWand;
import com.teamsight.touchvision.sistelnetworks.vwandtestingtool.R;

import java.util.Set;

/**
 * This is the main Activity that displays the buttons and start the rest of activities (Connect, Read and Write).
 *
 */
public class MainActivity extends Activity {

	// Intent request codes
	private static final int REQUEST_ENABLE_BT = 1;
	private static final int REQUEST_CONNECT = 2;
	private static final int REQUEST_READ = 4;
	private static final int REQUEST_WRITE = 5;

	 // Layout Views
	private Button btnConnect = null;
	private Button btnRead = null;
	private Button btnWrite = null;
	
	
	// Array of Bluetooth detected devices 
	public static BDevicesArray devices = new BDevicesArray();
	
	// vWand object for connect and communicate to vWand 
	public static VWand vWand = null;
	
	
	// Local Bluetooth Adapter
	private	BluetoothAdapter mBluetoothAdapter = null;
	
	// Debugging
	private static final String TAG = "MainActivity";
	
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		// Get instance from vWand
		vWand = VWand.getInstance();


		btnConnect = (Button) findViewById(R.id.btnConnect);
		btnConnect.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				if (btnConnect.getText().equals("Connect")) {
					
					// Launch activation Bluetooth
					activateBluetooth();

				} else if (btnConnect.getText().equals("Disconnect")) {

					close();
				}

			}
		});

		btnRead = (Button) findViewById(R.id.btnRead);
		btnRead.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				
				startReadActivity();
			}
		});
		
		btnWrite = (Button) findViewById(R.id.btnWrite);
		btnWrite.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				
				startWriteActivity();
			}
		});
		

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	
	/**
	 * This function refill the array of bonded devices. If Bluetooth is off then it is activated.
	 */
	public void activateBluetooth() {
		mBluetoothAdapter = BluetoothAdapter
				.getDefaultAdapter();

		if (mBluetoothAdapter != null) {
			// Device does not support Bluetooth
			if (!mBluetoothAdapter.isEnabled()) {
				Intent enableBtIntent = new Intent(
						BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
			}
			else
            // Bluetooth adapter was on!
			{

				Set<BluetoothDevice> pairedDevices = mBluetoothAdapter
						.getBondedDevices();
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
					startConnectActivity();
				}
			}
		}
	}
	
	/**
	 * This function starts Write Activity.
	 */
	public void startWriteActivity() {
		Intent i = new Intent(this, WriteActivity.class);
		startActivityForResult(i, REQUEST_WRITE);
	}
	
	/**
	 * This function starts Read Activity.
	 */
	public void startReadActivity() {
		Intent i = new Intent(this, ReadActivity.class);
		startActivityForResult(i, REQUEST_READ);
	}
	
	/**
	 * This function starts Connect Activity
	 */
	public void startConnectActivity() {
		Intent i = new Intent(this, ConnectActivity.class);
		startActivityForResult(i, REQUEST_CONNECT);

	}

	/** 
	 * This function close vWand connection and reset the fields from view.
	 */
	public void close() {
		try {
			//Disconnect vWand
			vWand.disconnect();
			
			//Sets View Layout properties
			btnConnect.setText("Connect");
			btnRead.setEnabled(false);
			btnWrite.setEnabled(false);
			
		} catch (Exception e) {
			Log.e(TAG, "Failed to close client socket");
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		if (requestCode == REQUEST_ENABLE_BT & resultCode == RESULT_OK) {
			
			mBluetoothAdapter = BluetoothAdapter
					.getDefaultAdapter();
			
			Set<BluetoothDevice> pairedDevices = mBluetoothAdapter
					.getBondedDevices();
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
				startConnectActivity();
			}
			
		}
		if (requestCode == REQUEST_CONNECT & resultCode == RESULT_OK) {
			
				
				try
				{
					//Sets View Layout properties
					btnConnect.setText("Disconnect");
					btnRead.setEnabled(true);
					btnWrite.setEnabled(true);
				}catch(Exception e)
				{
					Log.e(TAG, "Failed to start vWand");
				}

		}
	}

}
