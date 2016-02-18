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

/**
 * This class contains utility methods. 
 * 
 *
 */
public class Util {

	/**
	 * Converts byte array to hexadecimal char array.
	 * @param array source
	 * @return
	 */
	public static char[] getHexValue(byte[] array) {
		char[] symbols = "0123456789ABCDEF".toCharArray();
		char[] hexValue = new char[array.length * 2];
		
		int len = array.length;
		
		for (int i = 0; i < len; i++) {
			// convert the byte to an int
			int current = array[i] & 0xff;
			// determine the Hex symbol for the last 4 bits
			hexValue[i * 2 + 1] = symbols[current & 0x0f];
			// determine the Hex symbol for the first 4 bits
			hexValue[i * 2] = symbols[current >> 4];
		}
		return hexValue;
	}
	
	/**
	 * Used to decode the URI.
	 * 
	 * @param Uri_code first byte of the record data in the URI RTD.
	 * @return protocol field of an URI.
	 */
	public static String getProtocolPrefix(byte Uri_code)
	{
		String protocol = "";
		
		if (Uri_code == (byte) 0x01) //01
			protocol = "http://www.";
		if (Uri_code == (byte) 0x02) //02
			protocol = "https://www.";
		if (Uri_code == (byte) 0x03)
			protocol = "http://";
		if (Uri_code == (byte) 0x04)
			protocol = "https://";
		if (Uri_code == (byte) 0x05)
			protocol = "tel:";
		if (Uri_code == (byte) 0x06)
			protocol = "mailto:";
		if (Uri_code == (byte) 0x07)
			protocol = "ftp://anonymous:anonymous@";
		if (Uri_code == (byte) 0x08)
			protocol = "ftp://ftp.";
		
		return protocol;
		
	}
	
	/**
	 * Used to encode the URI.
	 * 
	 * @param protocol field of an URI.
	 * @return Uri_code first byte of the record data in the URI RTD.
	 */
	
	public static byte[] getUriIdentifierCode(String protocol)
	{
		byte[] uri_code = new byte[1];
		
		if (protocol.equals("http://www."))
			uri_code[0] = (byte) 0x01;
		if (protocol.equals("https://www."))
			uri_code[0] = (byte) 0x02;
		if (protocol.equals("http://"))
			uri_code[0] = (byte) 0x03;
		if (protocol.equals("https://"))
			uri_code[0] = (byte) 0x04;
		if (protocol.equals("tel:"))
			uri_code[0] = (byte) 0x05;
		if (protocol.equals("mailto:"))
			uri_code[0] = (byte) 0x06;
		if (protocol.equals("ftp://anonymous:anonymous@"))
			uri_code[0] = (byte) 0x07;
		if (protocol.equals("ftp://ftp."))
			uri_code[0] = (byte) 0x08;
		
		return uri_code;
		
	}
	
	
	/**
	 * Converts Hexadecimal string to ASCII value.
	 * 
	 * @param hex the source in Hexadecimal format.
	 * @return string value in ASCII format.
	 */
	 public static String hexToASCIIString(String hex){
		 
		  StringBuilder sb = new StringBuilder();
		  StringBuilder temp = new StringBuilder();
	 
		  //49204c6f7665204a617661 split into two characters 49, 20, 4c...
		  for( int i=0; i<hex.length()-1; i+=2 ){
	 
		      //grab the hex in pairs
		      String output = hex.substring(i, (i + 2));
		      //convert hex to decimal
		      int decimal = Integer.parseInt(output, 16);
		      //convert the decimal to character
		      sb.append((char)decimal);
	 
		      temp.append(decimal);
		  }
	 
		  return sb.toString();
	  }
	 
	/**
	 * Format command into a valid PN532 information frame.
	 * 
	 * @param cmd the PN532 command to format.
	 * @return information frame with command.
	 * @see <a href="http://www.nxp.com/documents/user_manual/141520.pdf">PN532 User Manual</a>
	 */
	public static byte[] toInformationFrame(byte[] cmd)
	{
		//Command frame structure from document PN532 User Manual.
				
		
		//1 byte indicating the number of bytes in the data field
		//(TFI and PD0 to PDn) 
		int len = cmd.length;
		
		//1 Packet Length Checksum LCS byte that satisfies the relation:
		// Lower byte of[LEN + LCS] = 00h
		int LCS = 256 - len;
		
		int size = 0;
		
		for(int i = 0; i < len; i++)
		{
			//Convert char to int  
			size = size + cmd[i];
		}
			
		// 1 Data checksum DCS byte that satisfies the relation:
		// Lower byte of [TFI + PD0 + PD1 + ... + PDn + DCS] = 00h		
		int DCS = 256 - size;
		byte[] aux = {(byte) 0x00, (byte) 0xFF, (byte) len, (byte) LCS};
		byte[] aux1 = {(byte) DCS};
		byte[] result = new byte[aux.length + cmd.length + aux1.length];
		int offset = 0;
		System.arraycopy(aux, 0, result, offset, aux.length);
		offset = offset + aux.length;
		System.arraycopy(cmd, 0, result, offset, cmd.length);
		offset = offset + cmd.length;
		System.arraycopy(aux1, 0, result, offset, aux1.length);

		return result;
		
		
	}
	
	/** 
	 *  Converts n bytes to a integer value. 
	 *  
	 * @param source the byte array.
	 * @param size number of bytes to convert.
	 * @param position position to start the conversion.
	 * @return integer value
	 * @throws Exception
	 */
	public static int getIntegerValue(byte[] source, int size, int position) throws Exception
	{
		byte[] byteLC = new byte[size];
		
		if (size + position > source.length)
			throw new Exception("Excess of bytes to read");
		System.arraycopy(source, position, byteLC, 0, size);
		return Integer.parseInt(new String(Util.getHexValue(byteLC)), 16);
	}
	
	/**
	 * Converts ASCII string to Hexadecimal value.
	 * 
	 * @param ascii source in ASCII format.
	 * @return string value in Hexadecimal format.
	 */
	public static String asciiToHexString(String ascii)
	{
		StringBuilder hex = new StringBuilder();
		int len = ascii.length();
		for (int i=0; i < len; i++) {
			hex.append(Integer.toHexString(ascii.charAt(i)));
		}      
		return hex.toString();
	}
	
	/**
	 * Converts Hexadecimal String to byte array.
	 * 
	 * @param s source in Hexadecimal format.
	 * @return the byte array.
	 */
	public static byte[] hexStringToByteArray(String s) {
		int len = s.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
					+ Character.digit(s.charAt(i+1), 16));
		}
		return data;
	}
}
