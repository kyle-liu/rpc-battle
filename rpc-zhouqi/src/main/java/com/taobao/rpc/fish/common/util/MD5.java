package com.taobao.rpc.fish.common.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import com.taobao.gecko.core.buffer.IoBuffer;

public class MD5 {

	public final static char hexDigits[] = { '0', '1', '2', '3', '4',
            '5', '6', '7', '8', '9',
            'A', 'B', 'C', 'D', 'E', 'F' };
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		String strs[]={"","abc","abcd","abc","12432asdgasdlgkj"};
		for(String str:strs){
			//byte result[]=getDigest(str);
			System.out.println(getHexDigest(str));
		}
	}
	public static byte[] getDigest(String str){
		if(str==null)return null;
		try {
			MessageDigest digest=MessageDigest.getInstance("MD5");
			digest.update(str.getBytes("utf-8"));
			return digest.digest();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	public static String toHex(byte data[]){
		if(data==null)return null;
		if(data.length==0)return "";
		 int j = data.length;
         char str[] = new char[j * 2];
         int k = 0;
         for (int i = 0; i < j; i++) {
             byte byte0 = data[i];
             str[k++] = hexDigits[byte0 >>> 4 & 0xf];
             str[k++] = hexDigits[byte0 & 0xf];
         }
         return new String(str);
	}
	public static String toHex(IoBuffer buffer){
		if(buffer==null)return null;
		if(buffer.limit()<16)throw new RuntimeException("buffer不中不够16个指纹");
		 int j = 16;
         char str[] = new char[j * 2];
         int k = 0;
         for (int i = 0; i < j; i++) {
             byte byte0 = buffer.get();
             str[k++] = hexDigits[byte0 >>> 4 & 0xf];
             str[k++] = hexDigits[byte0 & 0xf];
         }
         return new String(str);
	}
	
	public static String getHexDigest(String str){
		return toHex(getDigest(str));
	}
}
