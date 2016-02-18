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
 * This class represents NFC Tag object.
 * 
 *
 */
public class Tag {
	
	private String _type = "";
	private byte[] _uid = null;
	
	public static String MIFARE_DESFIRE = "Mifare DESFire";
	public static String MIFARE_ULTRALIGHT = "Mifare Ultralight";
	public static String MIFARE_CLASSIC_1k = "Mifare Classic 1k";
	
	/**
	 * Tags consists of two fields.
	 * 
	 * @param type Tag technology. 
	 * @param uid Tag Identifier.
	 */
	public Tag(String type, byte[] uid)
	{
		_type = type;
		_uid = new byte[uid.length];
		System.arraycopy(uid, 0, _uid, 0, uid.length);
		
	}

	public String get_type() {
		return _type;
	}

	public void set_type(String _type) {
		this._type = _type;
	}

	public byte[] get_uid() {
		return _uid;
	}

	public void set_uid(byte[] _uid) {
		this._uid = _uid;
	}

}
