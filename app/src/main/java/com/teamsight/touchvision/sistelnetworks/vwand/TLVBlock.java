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
 * This class represents TLV Blocks. 
 * 
 *@see <a href="http://www.nfc-forum.org/specs/spec_list/">http://www.nfc-forum.org/specs/spec_list/</a>
 */
public class TLVBlock {

	//The tag T identifies the type of the TLV.
	private byte[] _T = null;
	private int _T_size = 0;
	// The length field provides the size in bytes of the value field.
	private byte[] _L = null;
	private int _L_size = 0;
	// The value field consists of N consecutive bytes.
	private byte[] _V = null;
	private int _V_size = 0;
	
	public byte[] get_T() {
		return _T;
	}


	public void set_T(byte[] _T) {
		this._T = _T;
	}


	public int get_T_size() {
		return _T_size;
	}


	public void set_T_size(int _T_size) {
		this._T_size = _T_size;
	}


	public byte[] get_L() {
		return _L;
	}


	public void set_L(byte[] _L) {
		this._L = _L;
	}


	public int get_L_size() {
		return _L_size;
	}


	public void set_L_size(int _L_size) {
		this._L_size = _L_size;
	}


	public byte[] get_V() {
		return _V;
	}


	public void set_V(byte[] _V) {
		this._V = _V;
	}


	public int get_V_size() {
		return _V_size;
	}


	public void set_V_size(int _V_size) {
		this._V_size = _V_size;
	}


	/**
	 * TLV Block consists of one to three fields.
	 * 
	 * @param T identifies the type of the TLV block.
	 * @param L provides the size in bytes of the value field.
	 * @param V indicates the value field.
	 */
	public TLVBlock(byte[] T, int L, byte[] V)
	{
		if (T != null)
		{
			_T_size = T.length;
			_T = new byte[_T_size];
			System.arraycopy(T, 0, _T, 0, _T_size);
		}
		
		
		if (L >= 0)
		{
			if (L < 255)
			{
				String sLC = String.format("%02x", L);
				_L = Util.hexStringToByteArray(sLC); //Size to write
			}
			else
			{
				String sLC = String.format("%04x", L);
				byte[] FF = {(byte) 0xFF};
				_L = new byte[3];
				byte[] Laux = Util.hexStringToByteArray(sLC); //Size to write
				System.arraycopy(FF, 0, L, 0, 1);
				System.arraycopy(Laux, 0, L, FF.length, 2);
			}
			_L_size = _L.length;
		}
		if (V != null)
		{
			_V_size = V.length;
			_V = new byte[V.length];
			System.arraycopy(V, 0, _V, 0, _V_size);
		}
	}

	/**
	 * Gets byte array formed byte T, L and V.
	 * 
	 * @return byte array.
	 */
	public byte[] getByteArrayValue()
	{
		
		byte[] result = new byte[_T_size + _L_size + _V_size];
		
		if (_T != null)
			System.arraycopy(_T, 0, result, 0, _T_size);
		if (_L != null)
			System.arraycopy(_L, 0, result, _T_size, _L_size);
		if (_V != null)
			System.arraycopy(_V, 0, result, _T_size + _L_size, _V_size);
		
		return result;
	}
}
