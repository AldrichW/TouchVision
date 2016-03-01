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

import java.nio.charset.Charset;
import java.util.Arrays;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import com.teamsight.touchvision.sistelnetworks.vwand.BDevicesArray;
import com.teamsight.touchvision.sistelnetworks.vwand.Tag;
import com.teamsight.touchvision.sistelnetworks.vwand.Util;
import com.teamsight.touchvision.sistelnetworks.vwandtestingtool.R;


/**
 * This activity shows how to get UID and Type from detected tag   
 * and how to read tag content (only for NFC Forum Tag type 2).
 *
 */
public class ReadActivity extends Activity {

	
	// Debugging
	private static final String TAG = "ReadActivity";
	
	private Tag tag = null;
	private TextView tvUID = null;
	private TextView tvType = null;
	private TextView tvContent = null;
	private String content = "";
	public static BDevicesArray devices = new BDevicesArray();
	private Handler mHandler = new Handler();
	
	private boolean reading = true;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		read();
		
	}
	
	/**
	 * 
	 * Starts thread to get UID and type from detected card and read any NFC Forum Type 2 Tag (NXP Mifare Ultralight, NXP Mifare Ultralight C, NXP NTAG203).
	 * </br></br>
	 * When read process is finished, the android.nfc.NdefMessage is returned, then a new android intent is created with the result (only if NDEF Record contains valid URI). 
	 *  
	 * 
	 */
	public void read() {

		new Thread(new Runnable() {
			public void run() {
				
				do{	
					try
					{
						//Wake-up device
						com.teamsight.touchvision.MainActivity.vWand.startvWand();


						tag = com.teamsight.touchvision.MainActivity.vWand.startDetectCard();

						if (tag != null)
						{
							/*mHandler.post(new Runnable() {
								public void run() {

									*//*This stuff can all be removed probably.
									//Set tag identifier in text view.
									//tvUID.setText(new String(Util.getHexValue(tag.get_uid())));
									com.teamsight.touchvision.MainActivity.mT2Service.speakText("Tag is: " + tag.toString());
									com.teamsight.touchvision.MainActivity.mT2Service.speakText("Tag UID is: " + new String(Util.getHexValue(tag.get_uid())));
									com.teamsight.touchvision.MainActivity.mT2Service.speakText("Tag type is: " + tag.get_type());*//*

								}
							});*/

							//vWand read function
							NdefMessage message = com.teamsight.touchvision.MainActivity.vWand.readType2Tag();

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

										}
										
										
									}
									content = new String(payload, Charset.forName("US-ASCII"));
									com.teamsight.touchvision.MainActivity.tagContent = content;
									
									mHandler.post(new Runnable() {
										public void run() {

											/*tvContent.setText(content);*/
											//This is to make sure that if we're re-reading the same tag we don't keep repeating the content.
											if (!content.equals(com.teamsight.touchvision.MainActivity.previousTagContent)) {

												//Only read out stuff if the tag is different, and save the different value read.
												com.teamsight.touchvision.MainActivity.mT2Service.speakText("Tag message content is: " + content);
												com.teamsight.touchvision.MainActivity.previousTagContent = content;
											} else {
												//onDestroy();
												com.teamsight.touchvision.MainActivity.mT2Service.speakText("Should stop reading now");
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
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		try {
			reading = false;
			
			//On destroy stop detecting card
			com.teamsight.touchvision.MainActivity.vWand.stopDetectCard();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}
}
