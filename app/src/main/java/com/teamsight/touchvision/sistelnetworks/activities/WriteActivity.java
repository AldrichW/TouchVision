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
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.teamsight.touchvision.sistelnetworks.vwand.Util;
import com.teamsight.touchvision.sistelnetworks.vwandtestingtool.R;


/**
 * This activity shows how to write a tag (only for NFC Forum Tag type 2).
 *
 */
public class WriteActivity extends Activity implements OnItemSelectedListener {



	private String itemSelected = "";
	private Button btnWrite = null;
	private EditText edtContent = null;
	private Handler mHandler = new Handler();
	
	private static final String TAG = "WriteActivity";
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.write);
		
		Spinner spinner = (Spinner) findViewById(R.id.spiWrite);
		// Create an ArrayAdapter using the string array and a default spinner layout
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
		        R.array.URI_Identifier_Code, android.R.layout.simple_spinner_item);
		// Specify the layout to use when the list of choices appears
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		spinner.setAdapter(adapter);
		
		spinner.setOnItemSelectedListener(this);
		
		
		btnWrite = (Button) findViewById(R.id.btnWrite);
		btnWrite.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				
				write();
				btnWrite.setEnabled(false);
			}
		});
		
		edtContent = (EditText) findViewById(R.id.etContentWrite);
		
		
	}
	
	/**
	 * Starts thread to write android.nfc.NdefMessage into detected NFC Forum Type 2 tag.
	 * 
	 * 
	 */
	public void write()
	{

		new Thread(new Runnable() {
			public void run() {
				try
				{	
					//Wake-up device
					MainActivity.vWand.startvWand();
					
					//-------- Complete NDEF RECORD -----------------
					
					//The URI Record Type ("U")
                    byte[] type = Util.hexStringToByteArray("55");
					
					//Dummy identifier
					byte[] id = Util.hexStringToByteArray("30"); 
					
					//Selected URI prefix is converted in prefix code.
					byte[] prefix = Util.getUriIdentifierCode(itemSelected);
					
					//Convert content in ASCII format to Hexadecimal string
				    String uri = Util.asciiToHexString(edtContent.getText().toString()); 
					
					byte[] content = Util.hexStringToByteArray(uri);
					
					//Payload formed by prefix and content uri.
					byte[] payload = new byte[1 + content.length];
					
					System.arraycopy(prefix, 0, payload, 0, 1);
					System.arraycopy(content, 0, payload, 1, content.length);
					
					//Creates NDEF Record with well-known type name format, URI type, id and payload.
					NdefRecord nr = new NdefRecord(NdefRecord.TNF_WELL_KNOWN, type, id, payload);

					//Creates a new NdefMessage with a record 
					NdefRecord[] records = {nr};
					NdefMessage ndefMessage = new NdefMessage(records);
					
					
					//vWand write function
					MainActivity.vWand.writeType2Tag(ndefMessage);
					
					
					mHandler.post(new Runnable() {
						public void run() {
							Toast tx;

							tx = Toast.makeText(getApplicationContext(),
									"The tag has been successful written.", Toast.LENGTH_LONG);
							tx.show();
						}
					});
					
					//Send power down command to save power consumption
					MainActivity.vWand.powerDown();
				}
				catch(Exception e)
				{
					if (e.getMessage() != null)
						Log.e(TAG, e.getMessage());
					
					mHandler.post(new Runnable() {
						public void run() {
							Toast tx;

							tx = Toast.makeText(getApplicationContext(),
									"Failed to write tag.", Toast.LENGTH_LONG);
							tx.show();
						}
					});

				}

				
				
				mHandler.post(new Runnable() {
					public void run() {

						btnWrite.setEnabled(true);

					}
				});


			}
		}).start();
	}
	

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int pos,
			long id) {
		itemSelected = (String) parent.getItemAtPosition(pos);
		
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub
		
	}
	
	
	
	}
