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
package com.teamsight.touchvision.sistelnetworks.vwand;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.UUID;

import com.teamsight.touchvision.sistelnetworks.vwand.Util;
import com.teamsight.touchvision.sistelnetworks.vwand.VWandException;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.nfc.NdefMessage;
import android.util.Log;

/**
 * This class represents vWand object. </br>
 * 
 * Basic functions are:
 *  <p><ul>
 * <li>Connect and disconnect vWand.
 * <li>Get UID and Type tag.
 * <li>Read and Write NFC Forum Tags (Type 2).
 * <li>Send commands (supported by PN532) to vWand.
 * <li>Receive information frame from vWand.
 * </ul></p>
 * @see <a href="http://www.nfc-forum.org/specs/spec_list/">PN532 User Manual</a>
 */

public class VWand {

	private InputStream mmInStream = null;
	private OutputStream mmOutStream = null;
	private BluetoothSocket mmSocket = null;
	
	private boolean stopReading = false;
	
	private boolean detectionFlag = true;
	
	//Debugging
	private static final String TAG = "VWand";
	
	//Instance of VWand object
	private static VWand INSTANCE = new VWand();
	
	//This class implements singleton pattern.
	private VWand() {}
	
	/**
	 * Gets instance of VWand object.
	 * @return vWand object.
	 */
	public static VWand getInstance()
	{
		return INSTANCE;
	}
	
	/**
	 * Connects to vWand Bluetooth device. 
	 * 
	 * @param device vWand already paired to the local adapter.
	 * @throws Exception if any error occurred.
	 */
	public void createConnection(BluetoothDevice device) throws IOException
	{
			BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();

			adapter.cancelDiscovery();
			
			mmSocket = device
					.createRfcommSocketToServiceRecord(UUID
							.fromString("00001101-0000-1000-8000-00805F9B34FB"));
			if (mmSocket.isConnected())
				mmSocket.close();
			mmSocket.connect();
			
			
			mmInStream = mmSocket.getInputStream();
			mmOutStream = mmSocket.getOutputStream();
	}

	/**
	 * Gets the socket connection status.
	 * 
	 * @return true if socket is connected.
	 */
	public boolean isConnected()
	{
		if (this.mmSocket != null)
			return this.mmSocket.isConnected();
		else
			return false;
	}
	
	
	/**
	 * This command wake-up device and switch SAM (Security Access Module) mode to Normal mode.
	 *
	 * @throws Exception if communication has not been successful.
	 * @see <a href="http://www.nfc-forum.org/specs/spec_list/">PN532 User Manual</a>
	 */
	public void startvWand() throws Exception
	{
		// Command to Wake-up device (55h,55h) and SAM Configuration	
		byte[] cmd = {(byte) 0x55, (byte) 0x55, //Command to Wake-up
				      (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, 
				      (byte) 0x00, (byte) 0x00, (byte) 0x00, 
				      (byte) 0x00,(byte) 0xFF, 
				      (byte) 0x03, 
				      (byte) 0xFD, 
				      (byte) 0xD4, //TFI (D4) frame from the host controller to PN532
				      (byte) 0x14, //Code command (SAMConfiguration)
				      (byte) 0x01, // Normal Mode  
				      (byte) 0x17 //TimeOut
				      };
	
		write(cmd);
		recv_ack();
		recv_frame();
	}
	
	/**
	 * Sends command packet to vWand and receives response from it.
	 * 
	 * @param command supported by PN532 module.
	 * @return response OutPut frame composed by D5h (1 byte) + Command Code (1 byte) + OutParam (n bytes).
	 * @throws Exception if communication has not been successful.
	 * @see <a href="http://www.nxp.com/documents/user_manual/141520.pdf">PN532 User Manual</a>
	 */
	public byte[] send(byte[] command) throws Exception
	{
		
		write(Util.toInformationFrame(command));
		recv_ack();
		byte[] frame = recv_frame();
		
				
		//From Error table code list (PN532 User Manual)
		
		if (frame != null)
			if (frame.length == 3)
			{
				if (frame[2] == (byte) 0x01)
				{
					throw new VWandException("Time Out, the target has not answered");
				}
				if (frame[2] == (byte) 0x02)
				{
					throw new VWandException("A CRC error has been detected by the CIU");
				}
				if (frame[2] == (byte) 0x03)
				{
					throw new VWandException("A Parity error has been detected by the CIU");
				}
				if (frame[2] == (byte) 0x04)
				{
					throw new VWandException("During an anti-collision/select operation (ISO/IEC14443-3"+
							"Type A and ISO/IEC18092 106 kbps passive mode), an erroneous Bit Count has been detected");
				}
				if (frame[2] == (byte) 0x05)
				{
					throw new VWandException("Framing error during Mifare operation");
				}
				if (frame[2] == (byte) 0x06)
				{
					throw new VWandException("An abnormal bit-collision has been detected during bit wise anti-collision at 106 kbps");
				}
				if (frame[2] == (byte) 0x07)
				{
					throw new VWandException("Communication buffer size insufficient");
				}
				if (frame[2] == (byte) 0x09)
				{
					throw new VWandException("RF Buffer overflow has been detected by the CIU (bit BufferOvfl of the register CIU_Error)");
				}
				if (frame[2] == (byte) 0x0A)
				{
					throw new VWandException("In active communication mode, the RF field has not been switched on in time by the counterpart (as defined in NFCIP-1 standard)");
				}
				if (frame[2] == (byte) 0x0B)
				{
					throw new VWandException("RF Protocol error");
				}
				if (frame[2] == (byte) 0x0D)
				{
					throw new VWandException("Temperature error: the internal temperature sensor has detected overheating, and th erefore has automatically switched off the antenna drivers");
				}
				if (frame[2] == (byte) 0x0E)
				{
					throw new VWandException("Internal buffer overflow");
				}
				if (frame[2] == (byte) 0x10)
				{
					throw new VWandException("Invalid parameter");
				}
				if (frame[2] == (byte) 0x12)
				{
					throw new VWandException("DEP Protocol: The PN532 configured in target mode does not support the command received from the initiator");
				}
				if (frame[2] == (byte) 0x13)
				{
					throw new VWandException("DEP Protocol, Mifare or ISO/IEC14443-4: The data format does not match to the specification.");
				}
				if (frame[2] == (byte) 0x14)
				{
					throw new VWandException("Mifare: Authentication error");
				}
				if (frame[2] == (byte) 0x23)
				{
					throw new VWandException("ISO/IEC14443-3: UID Check byte is wrong");
				}
				if (frame[2] == (byte) 0x25)
				{
					throw new VWandException("Target is not in a correct state to perform this operation (not in DEP protocol, nor in PICC emulation)");
				}
				if (frame[2] == (byte) 0x26)
				{
					throw new VWandException("Operation not allowed in this configuration (host controller interface)");
				}
				if (frame[2] == (byte) 0x27)
				{
					throw new VWandException("This command is not acceptable due to the current context of the PN532");
				}
				if (frame[2] == (byte) 0x29)
				{
					throw new VWandException("The PN532 configured as target has been released by its initiator");
				}
				if (frame[2] == (byte) 0x2A)
				{
					throw new VWandException("PN532 and ISO/IEC14443-3B only: the ID of the card does not match, meaning that the expected card has been exchanged with another one");
				}
				if (frame[2] == (byte) 0x2B)
				{
					throw new VWandException("PN532 and ISO/IEC14443-3B only: the card previously activated has disappeared");
				}
				if (frame[2] == (byte) 0x2C)
				{
					throw new VWandException("Mismatch between the NFCID3 initiator and the NFCID3 target in DEP 212/424 kbps passive");
				}
				if (frame[2] == (byte) 0x2D)
				{
					throw new VWandException("An over-current event has been detected");
				}
				if (frame[2] == (byte) 0x2E)
				{
					throw new VWandException("NAD missing in DEP frame");
				}	

			}
		
		return frame;
		
	}
	
	/**
	 * Sends PN532 Power Down command to vWand.
	 * </br></br>
	 * This command put vWand in Power Down mode in order to save power consumption.</br>
	 * When this command is used, startvWand() method must be used in order to start vWand once again.
	 * 
	 * @return result raw data with response from vWand.
	 * @throws Exception if communication has not been successful.
	 */
	public byte[] powerDown() throws Exception
	{
		byte[] cmd = {(byte) 0xD4, // Send from host to PN532
					   (byte) 0x16, // Command Code (PowerDown)
					   (byte) 0x10 //WakeUpEnable
					   };
		
		//Send power down
		write(Util.toInformationFrame(cmd));
		recv_ack();
		Thread.sleep(2);
		return recv_frame();
	}
	

	/**
	 * Writes NDEF message to detected tag (only for NFC Forum Type 2).
	 * </br></br>
	 * This process is defined at Type 2 Tag Operation Specification document.
	 * </br></br>
	 * Compatible Tags are: NXP Mifare Ultralight, NXP Mifare Ultralight C, NXP NTAG203
	 * 
	 * @param message android.nfc.NdefMessage to write.
	 * @throws Exception if communication has not been successful.
	 * @throws VWandException if communication has not been successful. The possible errors are defined at PN532 User Manual (7.1 Error handling)
	 * 
	 */
	public void writeType2Tag(NdefMessage message) throws Exception, VWandException
	{
		
		//Valid states from Tag
		boolean INITIALIZED_STATE = false;
		boolean READ_WRITE_STATE = false;
		boolean READ_ONLY_STATE = false;
		
		boolean INVALID_STATE = false;
		
		//This variable indicates the read and write access capability
		byte[] access = new byte[1];
		
		//Indicates position inside read blocks (4 blocks)
		int position;
		
		//Indicates number of block
		int nBlock = 3;
		
		//Indicates size from NDEF Message TLV
		int ndefTLVSize = 0;
		
		//To save the tag memory read.
		byte[] memoryTag = new byte[16];
		
		//Indicates the number block where size byte is.
		int nBlockSize = 0;
		
		//Indicates the position of size inside block.
		int positionSize = 0;
		

		byte[] cmdREAD = new byte[5];
		
		byte[] headerREADCMD = {
				(byte) 0xD4, //Send host to PN532
				(byte) 0x40, // Command Code (InDataExchange)
				(byte) 0x01, // One target
				(byte) 0x30, // READ Command code
				
		};
		
		byte[] cmdWRITE = new byte[9];
		
		byte[] headerWRITECDM = {
				(byte) 0xD4, //Send host to PN532
				(byte) 0x40, // Command Code (InDataExchange)
				(byte) 0x01, // One target
				(byte) 0xA2 // WRITE Command code
				};
		
		//03h Indicating that it contains the NDEF TLV Message 
		byte[] T = {(byte) 0x03}; 
		byte[] terminatorT = {(byte) 0xFE};
		byte[] ndefContent = new byte[message.toByteArray().length + 1];
		int L = message.toByteArray().length; //NDEF Message
		System.arraycopy(message.toByteArray(), 0, ndefContent, 0, message.toByteArray().length);
		System.arraycopy(terminatorT, 0, ndefContent, message.toByteArray().length, 1);
		
		TLVBlock ndefTLVBlock = new TLVBlock(T, L, ndefContent);
		

		//Detecting card ...
		Tag ultralightTag = startDetectCard();

		
		if (ultralightTag != null)	
		{


			//---------------------------------------------------------------//
			//------------------- NDEF Detection procedure-------------------//
			//---------------------------------------------------------------//

			byte[] block = Util.hexStringToByteArray(String.format("%02x", nBlock));// Read from the block 3

			System.arraycopy(headerREADCMD, 0, cmdREAD, 0, headerREADCMD.length);
			System.arraycopy(block, 0, cmdREAD, headerREADCMD.length, block.length);

			//1. Reads 16 bytes from block 3.
			byte[] result = send(cmdREAD);

			//Discarding the first three bytes
			//0xD5 0x41 0x00
			//The following four bytes are CC
			if (result[3] == (byte) 0xE1) //Magic number, indicate that NFC forum defined data is stored in data area.
			{
				// result[4] indicates support version
				// result[5] indicates memory size
				System.arraycopy(result, 6, access, 0, 1); // indicates read and write access	

			}
			else
				throw new VWandException("Not found E1h (magic number)");


			do
			{
				nBlock++;
				//2. Reading data area in order to search NDEF Message TLV.
				block = Util.hexStringToByteArray(String.format("%02x", nBlock));// Start from the block 4

				System.arraycopy(headerREADCMD, 0, cmdREAD, 0, headerREADCMD.length);
				System.arraycopy(block, 0, cmdREAD, headerREADCMD.length, block.length);

				result = send(cmdREAD);


				//Discarding the first three bytes
				//0xD5 0x41 0x00
				System.arraycopy(result, 3, memoryTag, 0, 16);
				//Searching the 03h value which indicate the first NDEF Message TLV
				for(position = 0; position < memoryTag.length; position++)
				{ 
					if (memoryTag[position++] != (byte) 0x03)
					{
						position = position + memoryTag[position];//Size
					}
					else
					{
						INVALID_STATE = false;
						if (memoryTag[position] == (byte) 0xFF) //Three consecutive bytes format size format.
						{
							ndefTLVSize = Util.getIntegerValue(memoryTag, 2, position + 1);
						}
						else
						{
							ndefTLVSize = memoryTag[position];
						}
						positionSize = position;
						nBlockSize = nBlock;
						break;
					}
				}


				if (ndefTLVSize != 0) 
				{

					if (access[0] == (byte) 0x00)
						READ_WRITE_STATE = true;
					else if (access[0] == (byte) 0x0F)
						READ_ONLY_STATE = true;



				}
				else //If is equal to zero, no NDEF message is detected.
				{
					if (access[0] == (byte) 0x00)
						INITIALIZED_STATE = true;
				}

			}while(INVALID_STATE);


			//---------------------------------------------------------------//
			//-------------------- NDEF Write procedure ----------------------//
			//---------------------------------------------------------------//


			if (READ_WRITE_STATE || INITIALIZED_STATE)
			{
				//a. Set to 00h the field length.
				block = Util.hexStringToByteArray(String.format("%02x", nBlock));
				byte[] zero = {(byte) 0x00};

				System.arraycopy(zero, 0, memoryTag, position , 1);

				int nBlockAux = nBlock;
				for(int i = 0; i < 16; i = i + 4)
				{

					block = Util.hexStringToByteArray(String.format("%02x", nBlockAux));

					System.arraycopy(headerWRITECDM, 0, cmdWRITE, 0, headerWRITECDM.length);
					System.arraycopy(block, 0, cmdWRITE, headerWRITECDM.length, block.length);
					System.arraycopy(memoryTag, i, cmdWRITE, headerWRITECDM.length + block.length, 4);

					send(cmdWRITE);

					nBlockAux++;
				}


				if (ndefTLVBlock.get_V().length < 255)
				{ 


					int offSetInV = 0;

					//b. Writing NDEF Message in the memory:

					if (ndefTLVBlock.get_V().length > (16 - (position + 1)))
					{
						System.arraycopy(ndefTLVBlock.get_V(), offSetInV, memoryTag, position + 1 , 16 - (position + 1));
						offSetInV = 16 - (position + 1);
					}
					else
					{
						System.arraycopy(ndefTLVBlock.get_V(), offSetInV, memoryTag, position + 1 , ndefTLVBlock.get_V().length);
						offSetInV = ndefTLVBlock.get_V().length;

					}


					for(int i = 0; i < 16; i = i + 4)
					{
						block = Util.hexStringToByteArray(String.format("%02x", nBlock));

						System.arraycopy(headerWRITECDM, 0, cmdWRITE, 0, headerWRITECDM.length);
						System.arraycopy(block, 0, cmdWRITE, headerWRITECDM.length, block.length);
						System.arraycopy(memoryTag, i, cmdWRITE, headerWRITECDM.length + block.length, 4);
						send(cmdWRITE);

						nBlock++;
					}


					while(offSetInV < ndefTLVBlock.get_V().length)
					{
						block = Util.hexStringToByteArray(String.format("%02x", nBlock));

						System.arraycopy(headerREADCMD, 0, cmdREAD, 0, headerREADCMD.length);
						System.arraycopy(block, 0, cmdREAD, headerREADCMD.length, block.length);

						result = send(cmdREAD);

						//Discarding the first three bytes
						//0xD5 0x41 0x00
						System.arraycopy(result, 3, memoryTag, 0, 16);


						if ((ndefTLVBlock.get_V().length - offSetInV) > 16 )
						{
							System.arraycopy(ndefTLVBlock.get_V(), offSetInV, memoryTag, 0, 16);
							offSetInV = offSetInV + 16;
						}
						else
						{
							System.arraycopy(ndefTLVBlock.get_V(), offSetInV, memoryTag, 0, (ndefTLVBlock.get_V().length - offSetInV));
							offSetInV = offSetInV + (ndefTLVBlock.get_V().length - offSetInV);
						}

						for(int j = 0; j < 16; j = j + 4)
						{
							block = Util.hexStringToByteArray(String.format("%02x", nBlock));

							System.arraycopy(headerWRITECDM, 0, cmdWRITE, 0, headerWRITECDM.length);
							System.arraycopy(block, 0, cmdWRITE, headerWRITECDM.length, block.length);
							System.arraycopy(memoryTag, j, cmdWRITE, headerWRITECDM.length + block.length, 4);
							send(cmdWRITE);

							nBlock++;
						}


					}

					//c. Update the length

					block = Util.hexStringToByteArray(String.format("%02x", nBlockSize));

					System.arraycopy(headerREADCMD, 0, cmdREAD, 0, headerREADCMD.length);
					System.arraycopy(block, 0, cmdREAD, headerREADCMD.length, block.length);

					result = send(cmdREAD);

					//Discarding the first three bytes
					//0xD5 0x41 0x00
					System.arraycopy(result, 3, memoryTag, 0, 16);

					System.arraycopy(ndefTLVBlock.get_L(), 0, memoryTag, positionSize, 1);

					for(int j = 0; j < 16; j = j + 4)
					{
						block = Util.hexStringToByteArray(String.format("%02x", nBlockSize));

						System.arraycopy(headerWRITECDM, 0, cmdWRITE, 0, headerWRITECDM.length);
						System.arraycopy(block, 0, cmdWRITE, headerWRITECDM.length, block.length);
						System.arraycopy(memoryTag, j, cmdWRITE, headerWRITECDM.length + block.length, 4);
						send(cmdWRITE);

						nBlockSize++;
					}

				}//if size is bigger than 255 bytes (size is three byte format)
				else
				{
					int offSetInV = 0;

					//b. Writing NDEF Message in the memory:

					//Position indicates the position of size
					//position + 3 (FFh + size)  
					if (ndefTLVBlock.get_V().length > (16 - (position + 3)))
					{
						System.arraycopy(ndefTLVBlock.get_V(), offSetInV, memoryTag, position + 3 , 16 - (position + 3));
						offSetInV = 16 - (position + 3);
					}
					else
					{
						System.arraycopy(ndefTLVBlock.get_V(), offSetInV, memoryTag, position + 3 , ndefTLVBlock.get_V().length);
						offSetInV = ndefTLVBlock.get_V().length;

					}


					for(int i = 0; i < 16; i = i + 4)
					{
						block = Util.hexStringToByteArray(String.format("%02x", nBlock));

						System.arraycopy(headerWRITECDM, 0, cmdWRITE, 0, headerWRITECDM.length);
						System.arraycopy(block, 0, cmdWRITE, headerWRITECDM.length, block.length);
						System.arraycopy(memoryTag, i, cmdWRITE, headerWRITECDM.length + block.length, 4);
						send(cmdWRITE);

						nBlock++;
					}



					while(offSetInV < ndefTLVBlock.get_V().length)
					{
						block = Util.hexStringToByteArray(String.format("%02x", nBlock));

						System.arraycopy(headerREADCMD, 0, cmdREAD, 0, headerREADCMD.length);
						System.arraycopy(block, 0, cmdREAD, headerREADCMD.length, block.length);

						result = send(cmdREAD);

						//Discarding the first three bytes
						//0xD5 0x41 0x00
						System.arraycopy(result, 3, memoryTag, 0, 16);


						if ((ndefTLVBlock.get_V().length - offSetInV) > 16 )
						{
							System.arraycopy(ndefTLVBlock.get_V(), offSetInV, memoryTag, 0, 16);
							offSetInV = offSetInV + 16;
						}
						else
						{
							System.arraycopy(ndefTLVBlock.get_V(), offSetInV, memoryTag, 0, (ndefTLVBlock.get_V().length - offSetInV));
							offSetInV = offSetInV + (ndefTLVBlock.get_V().length - offSetInV);
						}

						for(int j = 0; j < 16; j = j + 4)
						{
							block = Util.hexStringToByteArray(String.format("%02x", nBlock));

							System.arraycopy(headerWRITECDM, 0, cmdWRITE, 0, headerWRITECDM.length);
							System.arraycopy(block, 0, cmdWRITE, headerWRITECDM.length, block.length);
							System.arraycopy(memoryTag, j, cmdWRITE, headerWRITECDM.length + block.length, 4);
							send(cmdWRITE);

							nBlock++;
						}


					}

					//c. Update the length


					block = Util.hexStringToByteArray(String.format("%02x", nBlockSize));

					System.arraycopy(headerREADCMD, 0, cmdREAD, 0, headerREADCMD.length);
					System.arraycopy(block, 0, cmdREAD, headerREADCMD.length, block.length);

					result = send(cmdREAD);

					//Discarding the first three bytes
					//0xD5 0x41 0x00
					System.arraycopy(result, 3, memoryTag, 0, 16);

					System.arraycopy(ndefTLVBlock.get_L(), 0, memoryTag, positionSize + 1, 2);

					for(int j = 0; j < 16; j = j + 4)
					{
						block = Util.hexStringToByteArray(String.format("%02x", nBlockSize));

						System.arraycopy(headerWRITECDM, 0, cmdWRITE, 0, headerWRITECDM.length);
						System.arraycopy(block, 0, cmdWRITE, headerWRITECDM.length, block.length);
						System.arraycopy(memoryTag, j, cmdWRITE, headerWRITECDM.length + block.length, 4);
						send(cmdWRITE);

						nBlockSize++;
					}
				}
			}

		}
		
	}
	
	
	
	/**
	 * Reads NDEF message from tag (only for NFC Forum Type 2).
	 * </br></br>
	 * This process is defined at Type 2 Tag Operation Specification document.
	 * </br></br>
	 * Compatible Tags are: NXP Mifare Ultralight, NXP Mifare Ultralight C, NXP NTAG203
	 * 
	 * @return android.nfc.NdefMessage read from Tag.
	 * @throws Exception if communication has not been successful.
	 * @throws VWandException if communication has not been successful. Errors are defined at PN532 User Manual (7.1 Error handling)
	 */
	public NdefMessage readType2Tag() throws Exception, VWandException
	{
		
		boolean INVALID_STATE = true;
		boolean INITIALIZED_STATE = false;
		boolean READ_WRITE_STATE = false;
		boolean READ_ONLY_STATE = false;
		
		byte[] access = new byte[1];
		
		int position;
		int ndefTLVSize = 0;
		int nBlock = 3;
		
		NdefMessage ndefResult = null;
		byte[] ndefPayload = new byte[1];
		
		//To save the tag memory read.
		byte[] memoryTag = new byte[16];
		
		Tag ultralightTag = startDetectCard();
		
		if (ultralightTag != null)
		{

			//---------------------------------------------------------------//
			//------------------- NDEF Detection procedure-------------------//
			//---------------------------------------------------------------//

			//1. Reads 16 bytes from block 3.
			byte[] cmd = new byte[5];
			byte[] headerREADCMD = {(byte) 0xD4, //Send host to PN532
					(byte) 0x40, // Command Code (InDataExchange)
					(byte) 0x01, // One target
					(byte) 0x30, // READ Command code

			};

			byte[] block = Util.hexStringToByteArray(String.format("%02x", nBlock));// Read from the block 3

			System.arraycopy(headerREADCMD, 0, cmd, 0, headerREADCMD.length);
			System.arraycopy(block, 0, cmd, headerREADCMD.length, block.length);


			byte[] result = send(cmd);

			//Discarding the first three bytes
			//0xD5 0x41 0x00
			//The following four bytes are CC
			if (result[3] == (byte) 0xE1) //Magic number, indicate that NFC forum defined data is stored in data area.
			{
				// result[4] indicates support version
				// result[5] indicates memory size
				System.arraycopy(result, 6, access, 0, 1); // indicates read and write access	

			}
			else
				throw new VWandException("Not found E1h (magic number)");


			do
			{
				nBlock++;
				//2. Reading data area in order to search NDEF Message TLV.
				block = Util.hexStringToByteArray(String.format("%02x", nBlock));// Start from the block 4

				System.arraycopy(headerREADCMD, 0, cmd, 0, headerREADCMD.length);
				System.arraycopy(block, 0, cmd, headerREADCMD.length, block.length);

				result = send(cmd);


				//Discarding the first three bytes
				//0xD5 0x41 0x00
				System.arraycopy(result, 3, memoryTag, 0, 16);
				//Searching the 03h value which indicate the first NDEF Message TLV
				for(position = 0; position < memoryTag.length;position++)
				{ 
					if (memoryTag[position++] != (byte) 0x03)
					{
						position = position + memoryTag[position];//Size
					}
					else
					{
						INVALID_STATE = false;
						if (memoryTag[position] == (byte) 0xFF) //Three consecutive bytes format.
						{
							ndefTLVSize = Util.getIntegerValue(memoryTag, 2, position + 1);
							position = position + 3;
						}
						else
						{
							ndefTLVSize = memoryTag[position];
							position++;
						}
						break;
					}
				}


				if (ndefTLVSize != 0) 
				{

					if (access[0] == (byte) 0x00)
						READ_WRITE_STATE = true;
					else if (access[0] == (byte) 0x0F)
						READ_ONLY_STATE = true;

					ndefPayload = new byte[ndefTLVSize];

				}
				else //If is equal to zero, no NDEF message is detected.
				{
					if (access[0] == (byte) 0x00)
						INITIALIZED_STATE = true;
				}

			}while(INVALID_STATE);


			//---------------------------------------------------------------//
			//-------------------- NDEF Read procedure ----------------------//
			//---------------------------------------------------------------//

			if (READ_WRITE_STATE || READ_ONLY_STATE)
			{
				//Less than one sector (1024 bytes).
				if (ndefTLVSize < 1024)
				{
					int read = (16 - position); //16 bytes read in last command minus the position of V (NDEF size). 

					
					if (read < ndefTLVSize)
						System.arraycopy(memoryTag, position, ndefPayload, 0, read);
					else
						System.arraycopy(memoryTag, position, ndefPayload, 0, ndefTLVSize);
					
					
					int iBlock = 8; 
					
					while(read < ndefTLVSize)
					{
						block = Util.hexStringToByteArray(String.format("%02x", iBlock));// Reading from the block 8

						System.arraycopy(headerREADCMD, 0, cmd, 0, headerREADCMD.length);
						System.arraycopy(block, 0, cmd, headerREADCMD.length, block.length);

						result = send(cmd);
						//Discarding the first three bytes
						//0xD5 0x41 0x00
						System.arraycopy(result, 3, memoryTag, 0, 16);

						if (ndefTLVSize - read < 16)
						{
							System.arraycopy(memoryTag, 0, ndefPayload, read, ndefTLVSize - read);
							read = ndefTLVSize;
						}
						else
						{
							System.arraycopy(memoryTag, 0, ndefPayload, read, 16);
							read = read + 16;
						}

						iBlock = iBlock + 4;


						//System.arraycopy(result, 4, ndefPayload, 0, 16);
					}

					
					ndefResult = new NdefMessage(ndefPayload);

				}else //TODO If the data to be read exceeds one or more sectors (256 contiguous blocks, 1024 bytes).
				{

				}
			}

		}

		return ndefResult;
		
	}
	
	
	
	/**
	 * Starts card detection process. 
	 *  
	 * @return Tag formed by UID and Type.
	 * @throws Exception if communication has not been successful.
	 */
	public Tag startDetectCard() throws Exception {

		
		//Command to start detect card 
		byte[] cmd = {
				(byte) 0xD4, // Send from host to PN532
				(byte) 0x4A, // Command Code (InListPassiveTarget)
				(byte) 0x01, // Number max of tags (1)
				(byte) 0x00 //BrTy 106 kbps type A (ISO/IEC14443 Type A),
				};
		Tag tag = null;
		String type = "";

		byte[] result = null;
		
		int size = 0;

		blockingCall();
		
		result = send(cmd);
		
		if (result != null)
			if (result[0] == (byte) 0xD5 && result[1] == (byte) 0x4B) //The response is successful
			{
				size = result[7]; //Extract size 

				byte[] uidByte = new byte[size];
				//Discard the first 8 bytes form the result.
				System.arraycopy(result, 8, uidByte, 0, size);

				if (result[4] == (byte) 0x00 & result[5] == (byte) 0x04)
					type = "Mifare Classic 1k";
				if (result[4] == (byte) 0x00 & result[5] == (byte) 0x44)
				{
					byte[] readType2 = {(byte) 0xD4, //Send host to PN532
							(byte) 0x40, // Command Code (InDataExchange)
							(byte) 0x01, // One target
							(byte) 0x30, // READ Command code
							(byte) 0x16
					};

					try{
						send(readType2);
						type = "Mifare Ultralight C";
					}
					catch(VWandException e)
					{
						if (e.getMessage().equals("DEP Protocol, Mifare or ISO/IEC14443-4: The data format does not match to the specification."))
							type = "Mifare Ultralight";
						send(cmd);
					}
				}
				if (result[4] == (byte) 0x04 & result[5] == (byte) 0x44)
					type = "Mifare DESFire";

				tag = new Tag(type, uidByte);
			}	

		return tag;

	}
	/**
	 * Receives ACK frame from vWand.
	 * 
	 * @return response OutPut frame composed by ACK.
	 * @throws Exception if communication has not been successful.
	 */
	public byte[] recv_ack() throws Exception {
		byte[] bufferACK = new byte[1]; // buffer for storing the stream
		byte[] receivedACK = new byte[6];

		int i = 0;
		boolean preamble = true;

		// Reading ACK
		do {
            //read a single byte from the mmInStream and store it in bufferACK
			mmInStream.read(bufferACK, 0, 1);


			if (i == 1)
            //if this is the second time we read
				if (bufferACK[0] == (byte) 0xFF)
                    //if the byte we read is all 1s there set preamble to false
					preamble = false;

            //copy the byte we just read from bufferACK to receivedACK
			System.arraycopy(bufferACK, 0, receivedACK, i, 1);
			i++;
			

		} while ((preamble & i < 6) || (!preamble & i < 4));
		
		if (preamble)
		{//if preamble stayed true for the entirety of the message
			byte[] ack = {(byte) 0x00, (byte) 0x00, (byte) 0xFF, (byte) 0x00, (byte) 0xFF, (byte) 0x00};

            //if the received ACK doesn't match what we're expecting, something is wrong
			if (!Arrays.equals(receivedACK,ack))
				throw new Exception("Invalid ack frame "+ new String(Util.getHexValue(receivedACK)));
			
			
		}
		else 
		{//if preamble became false (second byte we read was all 1s)
			byte[] ack = {(byte) 0x00, (byte) 0xFF, (byte) 0x00, (byte) 0xFF};

            //We are trying the same as before, but with a different expected ACK.
			if (!Arrays.equals(receivedACK, ack))
				throw new Exception("Invalid ack frame " + new String(Util.getHexValue(receivedACK)));
		}
		
		return receivedACK;
	}

	/**
	 * Receives information frame from vWand.
	 * 
	 * @return response OutPut frame composed by D5h (1 byte) + Command Code (1 byte) + OutParam (n bytes).
	 * @throws Exception If any error occur on reading process or if user stop the process.
	 */
	public synchronized byte[] recv_frame() throws Exception {

		byte[] bufferTagAux = new byte[1];

		boolean error = true;
		//String result = "";
		byte[] value = new byte[18];
		byte[] result = new byte[18];
		int i = 0;
		boolean recv = true;

		i = 0;
		int size = 0;

		boolean preamble = true;
		boolean normal = true; // If false then the frame is considered
								// extended.

		mmInStream.read(bufferTagAux, 0, 1); // Read first byte 0x00
		value[i++] = bufferTagAux[0];
		
		mmInStream.read(bufferTagAux, 0, 1); // Read second byte, 0x00 if exists preamble, 0xFF if not exists preamble
		value[i++] = bufferTagAux[0];
		
		if (bufferTagAux[0] == (byte) 0xFF) {
			preamble = false;
		}
		if (preamble) {
			mmInStream.read(bufferTagAux, 0, 1); // Read tag, 0xFF
			value[i++] = bufferTagAux[0];
			
			mmInStream.read(bufferTagAux, 0, 1); // Read tag, LEN or 0XFF
			value[i++] = bufferTagAux[0];
			
			if (bufferTagAux[0] == (byte) 0xFF) // In order to check if frame is extended
			{//if its 0xFF the frame is extended
				mmInStream.read(bufferTagAux, 0, 1); // Read tag
				value[i++] = bufferTagAux[0];
				
				if (bufferTagAux[0] == (byte) 0xFF)
					normal = false;
			}

		} else { //Without preamble
			mmInStream.read(bufferTagAux, 0, 1); // Read tag
			value[i++] = bufferTagAux[0];
			
			if (bufferTagAux[0] == (byte) 0xFF) {
				mmInStream.read(bufferTagAux, 0, 1); // Read tag I think this is actually LEN
				value[i++] = bufferTagAux[0];
				
				if (bufferTagAux[0] == 0xFF)
					normal = false;
			}
		}
		
		if (stopReading)
		{
			mmInStream.read(bufferTagAux, 0, 1); // Read 0xFF from ACK received from stop command
			
		
			 
			stopReading = false;
			return null;
			//throw new Exception("The reading process has been stopped.");
		}
		
		if (normal) {
			do {
				mmInStream.read(bufferTagAux, 0, 1); // Read tag
				value[i++] = bufferTagAux[0];
				
				if (bufferTagAux[0] == (byte) 0xD5) {

					if ((value[i - 5] == (byte) 0x00) & (value[i - 4] == (byte) 0xFF)) {
						error = false;
						recv = false;
						// Extract the size
						byte[] byteLen = new byte[1];
						System.arraycopy(value, i-3, byteLen, 0, 1);//copy 1 byte from value[i-3] to byteLen
						size = Integer.parseInt(new String(Util.getHexValue(byteLen)), 16);
						//size = value[i - 3];
						
					}
				}
			} while (recv);
		} else // Extended
		{
			do {
				mmInStream.read(bufferTagAux, 0, 1); // Read tag
				value[i++] = bufferTagAux[0];
				
				
				if (bufferTagAux[0] == (byte) 0xD5) {
					
					if ((value[i - 5] == (byte) 0xFF)
							& (value[i - 6] == (byte) 0xFF)) {
						error = false;
						recv = false;
						// Extract the size, in this case the size are two bytes
						//size = value[i - 4] + value[i - 3]; 
						size = Util.getIntegerValue(value, 2, i-4);

					}
				}
			} while (recv);
		}

		if (!error) {
			
			int k = 0;
			result = new byte[size];
			result[k++] = bufferTagAux[0]; //0XD5
			while (k < size) {
				mmInStream.read(bufferTagAux, 0, 1); // Read tag
				result[k] = bufferTagAux[0];
				k++;
				
			}
		
			// Read DCS and Postamble

			if (preamble) {
				mmInStream.read(bufferTagAux, 0, 1); // Read checksum
				

				mmInStream.read(bufferTagAux, 0, 1); // Read postamble
				

			}

			else {
				mmInStream.read(bufferTagAux, 0, 1); // Read checksum
				
				
			}

		} else {
			Log.e(TAG, "Error reading tag");
		}
		return result;
	}
	
	
	/**
	 * Stops card detection process.
	 * 
	 * @throws Exception if communication has not been successful.
	 */
	public void stopDetectCard() throws Exception
	{
		
		stopReading = true;
		detectionFlag = false;
		
		try
		{
			byte[] cmd = {(byte) 0x55, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00};
			write(Util.toInformationFrame(cmd));

			byte[] cmd1 = {(byte) 0xD4, (byte) 0x02};
			//Send power down
			write(Util.toInformationFrame(cmd1));

			byte[] frame = recv_frame(); //The ack has been received in recv_frame
			
			//If ACK is received in recv_frame (frame == null), try to receive frame once again.
			if (frame == null)
				frame =recv_frame();
			

			//Discarding available bytes
			if(mmInStream.available() > 0) {
				System.out.println("Availables: " + mmInStream.available());
				byte[] discarded = new byte[mmInStream.available()];
				final int bytesRead = mmInStream.read(discarded);

			}
			
		}catch(Exception e)
		{
			throw e;
		}
		finally
		{
			unblock();	
		}
		
		
	}
	
	/**
	 * Call this from block method.
	 * 
	 */
	private synchronized void blockingCall()
	{
		
		while (!detectionFlag) // Loop so this method doesn't return before the flag is true.
		{
			
			try
			{
				wait();
			}
			catch (InterruptedException ex)
			{
				// Ignore and loop again until the flag is true.
			}
			
		}
		
	}


	/**
	 * Call this from to unblock the method that is blocked.
	 * 
	 */
	private synchronized void unblock()
	{
		detectionFlag = true;
		notifyAll();
	}
	
	/**
	 * Sends information frame with command packet to vWand.
	 * 
	 * @param frame complete frame supported by PN532 module.
	 * @throws Exception if communication has not been successful.
	 * @see <a href="http://www.nxp.com/documents/user_manual/141520.pdf">PN532 User Manual</a>
	 * 
	 */
	public void write(byte[] frame) throws Exception {

		//byte[] bts = new BigInteger(cmd, 16).toByteArray();
		//Discarding available byts 
		
		try {
			mmOutStream.write(frame);
		} catch (IOException e) {

			Log.e(TAG, e.getMessage());
			
		}
	}
	
	/**
	 * Close Input and Output stream and disconnects vWand. 
	 */
	public void disconnect() {
			 if (mmInStream != null) {
	                try {mmInStream.close();} catch (Exception e) {}
	                mmInStream = null;
	        }

	        if (mmOutStream != null) {
	                try {mmOutStream.close();} catch (Exception e) {}
	                mmOutStream = null;
	        }
	        
	        
	        if (mmSocket != null) {
                try {mmSocket.close();} catch (Exception e) {}
                mmSocket = null;
	        }

	}
}
