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

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.teamsight.touchvision.MainActivity;
//import com.teamsight.touchvision.sistelnetworks.activities.MainActivity;
import com.teamsight.touchvision.sistelnetworks.vwand.BDevicesArray;
import com.teamsight.touchvision.sistelnetworks.vwandtestingtool.R;

import java.util.Vector;

/**
 * This activity shows a list with bounded devices and will try to connect with 
 * the selected vWand.
 * 
 * 
 */
public class ConnectActivity extends ListActivity {

	private ProgressDialog dialog;
	private Handler mHandler = new Handler();
	
	// Debugging
	private static final String TAG = "ConnectActivity";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.bdevices);

		BDevicesArray devs = MainActivity.devices;
		Vector<String> devVec = MainActivity.devices.getDevices();

		setListAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, MainActivity.devices.getDevices()));

	}

	@Override
	protected void onListItemClick(ListView listView, View view, int position,
			long id) {
		super.onListItemClick(listView, view, position, id);
		
		final int pos = position;
		dialog = ProgressDialog.show(this, "", " Connecting ... to pos: " + pos, true, false);
		
		
		new Thread( new Runnable(){    		
			public void run(){
				try
				{

					BluetoothDevice device = MainActivity.devices.getDevice(pos);
					
					//Create vWand connection
					MainActivity.vWand.createConnection(device);

					
					Intent i = new Intent();
					setResult(RESULT_OK, i);
					
			

				}catch(Exception e)
				{
					mHandler.post(new Runnable() {
						public void run() {
							Toast tx;

							tx = Toast.makeText(getApplicationContext(),
									"Unable to connect", Toast.LENGTH_LONG);
							tx.show();
						}
					});
					
			
				}
				finally
				{
					//Close connecting ... dialog
					dialog.dismiss();
					
					//Finish activity and return to main activity.
					finish();
				}
			}
		}).start();
			
	}

}
