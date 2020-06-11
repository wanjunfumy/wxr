package com.wanjf.mysecretapp.tools;


import android.annotation.SuppressLint;

import java.io.UnsupportedEncodingException;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class AESCrypto {

	// private static String AES_SECRET_KEY =
	private static final String AES_SECRET_KEY = "A8A72DS7D6264530F01BA49BC73EB87F";

	private static final String MAC_KEY_DES = "B0FB83E39A5EBFNCBE471362A58393FF";
	private static final String MAC_KEY = "D951DBE037C82320";

	private String KEY_ALGORITHM = "AES";
	private String CIPHER_ALGORITHM = "AES/ECB/PKCS5Padding";

	public AESCrypto() {

	}

	/**
	 * 
	 * @param dataPackage
	 *            加密后的数据
	 * @param length
	 *            加密前的数据长度
	 * @return
	 */
	public byte[] insertToPackage(byte[] dataPackage, int length) {
		byte[] ret = new byte[4 + dataPackage.length + 8];

		try {
			int a = 8 - (4 + dataPackage.length) % 8;
			byte[] tem = new byte[4 + dataPackage.length + a];// 将会被mac校验的部分
			byte[] _leng = intToByte(length);

			System.arraycopy(_leng, 0, ret, 0, _leng.length);// 将表示报文长度的byte拼接到整体报文中
			System.arraycopy(dataPackage, 0, ret, _leng.length, dataPackage.length);

			/**
			 * 将会被校验和生产mac的数组
			 */
			// System.arraycopy(ret, 0, tem, 0, tem.length);// 和服务端对不上，用下面的ss

			byte[] ss = new byte[8];

			System.arraycopy(_leng, 0, ss, 0, _leng.length);

			BaseDES baseDES = new BaseDES();
			byte[] macProof = baseDES.proofMacData(ss, stringToHex(MAC_KEY_DES), stringToHex(MAC_KEY));
			System.arraycopy(macProof, 0, ret, dataPackage.length + 4, macProof.length);// 整个密码加密完成，下一步应该是base64加密
			return ret;
		} catch (Exception e) {

		}
		return ret;
	}

	/**
	 * int to byte
	 * 
	 * @param i
	 * @return
	 */
	public byte[] intToByte(int i) {
		byte[] byt = new byte[4];
		for (int j = 0; j < 4; j++) {
			byt[j] = (byte) (i >> 8 * j & 0xFF);
		}
		return byt;
	}

	private int getInt(char c) {
		if (c >= '0' && c <= '9') {// 转化为10进制
			return c - '0';
		} else if (c >= 'A' && c <= 'Z') {
			return c - 'A' + 10;
		} else if (c >= 'a' && c <= 'z') {
			return c - 'a' + 10;
		}
		return 999;
	}

	/**
	 * 将string转换为byte
	 * 
	 * @param string
	 * @return
	 */
	public byte[] stringToHex(String string) {
		if (string.length() % 2 != 0) {// 不行
			return null;
		}
		byte[] bs = new byte[string.length() / 2];
		char[] cs = string.toCharArray();
		int height = 0;
		int low = 0;
		for (int i = 0; i < cs.length; i += 2) {//
			height = getInt(cs[i]);
			low = getInt(cs[i + 1]);

			if (low == 999 || height == 999) {
				return null;
			}

			int byteValue = height * 16 + low;
			bs[i / 2] = (byte) byteValue;
		}
		System.gc();
		return bs;
	}

	/**
	 * 将byte转换为string
	 * 
	 * @param bytes
	 * @return
	 */
	public String byteToString(byte[] bytes) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < bytes.length; i++) {
			int b = (bytes[i] & 0xFF);
			String dd = Integer.toHexString(b);
			if (dd.length() < 2) {
				sb.append("0");
			}
			sb.append(Integer.toHexString(b));
		}
		return sb.toString();
	}

	private byte[] getRealKey() {
		byte[] key = stringToHex(AES_SECRET_KEY);
		return key;
	}

	/**
	 * 
	 * @param data
	 *            需要加密的流
	 * @return
	 */
	public byte[] AES256ECBEncrypt(byte[] data) {
		try {
			Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM, "BC");
			SecretKeySpec keySpec = new SecretKeySpec(AES_SECRET_KEY.getBytes("UTF-8"), KEY_ALGORITHM);
			cipher.init(Cipher.ENCRYPT_MODE, keySpec);
			byte[] encoded = cipher.doFinal(data);
			return insertToPackage(encoded, data.length);
		} catch (Exception e) {

		}
		return data;
	}

	/**
	 * aes 256加密
	 * 
	 * @param data
	 *            如果返回
	 */
	public byte[] AES256ECBEncrypt(String data) {
		try {
			return AES256ECBEncrypt(data.getBytes("UTF-8"));
		} catch (Exception e) {

		}
		return data.getBytes();
	}

	/**
	 * aes 256解密
	 * @param data 需要解密的流
	 */
	public byte[] AES256ECBDecrypt(byte[] data) {
		try {
			if (data.length == 0) {
				return data;
			}
			@SuppressLint("GetInstance") Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM, "BC");
			SecretKeySpec key = new SecretKeySpec(AES_SECRET_KEY.getBytes(), KEY_ALGORITHM);
			cipher.init(Cipher.DECRYPT_MODE, key);
			byte[] dec = cipher.doFinal(data);
			return dec;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return data;
	}

	/**
	 * aes 256解密
	 */
	public byte[] AES256ECBDecrypt(String data) {
		try {
			return AES256ECBDecrypt(data.getBytes("UTF-8"));
		} catch (Exception e) {

		}
		return data.getBytes();
	}

	/**
	 * 解密
	 * 
	 * @param encryptString
	 * @return
	 */
	public String decrypt(String encryptString) {
		try {
			byte[] tem = stringToHex(encryptString);
			byte[] tl = new byte[tem.length - 4 - 8];// 暂时写死，一般不会变，变了再说
			System.arraycopy(tem, 4, tl, 0, tl.length);
			byte[] bs = AES256ECBDecrypt(tl);
			try {
				return new String(bs, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.fillInStackTrace();
		}
		return "";
	}

}
