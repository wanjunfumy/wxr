package com.wanjf.mysecretapp.tools;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

/**
 * DES加密解密工作类
 * @author Administrator
 */
public class BaseDES {
	
	/**
	 * 加密
	 * @param dataSource
	 * @param password
	 * @return 
	 */
	public byte[] desCrypto(byte[] dataSource, String password) {
		return desCrypto(dataSource, password.getBytes());
	}
	
	/**
	 * 加密
	 * @param dataSource
	 * @param password
	 * @return 
	 */
	public byte[] desCrypto(byte[] dataSource, byte[] password) {
		if (dataSource == null) {
			return null;
		}
		SecureRandom secureRandom = null;
		DESKeySpec desKeySpec = null;
		try {
			secureRandom = SecureRandom.getInstance("SHA1PRNG");
			desKeySpec = new DESKeySpec(password);
			//创建一个密钥工厂
			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
			SecretKey secretKey = keyFactory.generateSecret(desKeySpec);
			//Cipher 才是加密的关键
			Cipher cipher = Cipher.getInstance("DES/ECB/NoPadding");
			cipher.init(Cipher.ENCRYPT_MODE, secretKey, secureRandom);
			return cipher.doFinal(dataSource);//真正的加密步骤
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 解密
	 * @param dataSource
	 * @param password
	 * @return 
	 */
	public byte[] decrypt(byte[] dataSource, byte[] password) {
		if (dataSource == null) {
			return null;
		}
		SecureRandom secureRandom = null;
		try {
			secureRandom = SecureRandom.getInstance("SHA1PRNG");
			DESKeySpec keySpec = new DESKeySpec(password);
			SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("DES");
			SecretKey secretKey = secretKeyFactory.generateSecret(keySpec);
			Cipher cipher = Cipher.getInstance("DES/ECB/NoPadding");
			cipher.init(Cipher.DECRYPT_MODE, secretKey, secureRandom);
			return cipher.doFinal(dataSource);
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * 2倍key长度的3DES加解密方法
	 * 
	 * @param content
	 *            需要加密解密的内容
	 * @param isEncryprt
	 *            true为加密，false为解密
	 * @return 加密解密后的byte数组
	 */
	public byte[] ThreeDES2XCrrypt(byte[] content,byte[] s, boolean isEncryprt) {
		try {
			byte[] l = new byte[8];
			byte[] r = new byte[8];
			System.arraycopy(s, 0, l, 0, 8);
			System.arraycopy(s, 8, r, 0, 8);

			if (isEncryprt) {// 加密 L加密->R解密->L加密
				byte[] bs1 = desCrypto(content, l);
				byte[] bs2 = decrypt(bs1, r);
				return desCrypto(bs2, l);
			} else {// 解密 L解密->R加密->L解密
				byte[] bs1 = decrypt(content, l);
				byte[] bs2 = desCrypto(bs1, r);
				return decrypt(bs2, l);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.gc();
		return null;
	}
	
	/**
	 * mac校验
	 * 
	 * @param data
	 *            通信报文，除了mac校验码位置的全部内容
	 * @param key
	 *            DES密钥
	 * @param iv
	 *            向量
	 * @return
	 */
	public byte[] proofMacData(byte[] data, byte[] key, byte[] iv) {
		if (key.length % 16 != 0) {
			return null;
		}
		if (data.length % 8 != 0) {
			return null;
		}
		if (iv.length % 8 != 0) {
			return null;
		}
		byte[] l = new byte[8];// DES密钥左8位
		byte[] r = new byte[8];// DES密钥右8位
		System.arraycopy(key, 0, l, 0, 8);
		System.arraycopy(key, 8, r, 0, 8);

		// MAC(L加密)->R解密->L加密
		byte[] dMACL = proofMac(data, iv, l);// 错误方法做的： [-19, 43, -33, 25, 96,
												// -36, -109, 83]
		byte[] rDecrypt = decrypt(dMACL, r);
		byte[] lDesCrypt = desCrypto(rDecrypt, l);// 真确的解析方式所得：[-47, -21, -103,
													// 100, 65, 80, 64, -57]，
													// 错误方法做的：[-42, -41, -38,
													// -111, -8, 119, -85, 93]
													// 原mac校验码 69, 33, -84, 32,
													// 18, 39, -127, 52
		System.gc();
		return lDesCrypt;
	}

	/**
	 * 
	 * @param data
	 * @param iv
	 * @return
	 */
	public byte[] proofMac(byte[] data, byte[] iv, byte[] lDecrypt) {
		if (iv.length % 8 != 0) {
			return null;
		}
		byte[] bTemp = new byte[8];
		bTemp = iv.clone();
		for (int i = 0; i < data.length; i += 8) {
			bTemp[0] = (byte) (data[i + 0] ^ bTemp[0]);
			bTemp[1] = (byte) (data[i + 1] ^ bTemp[1]);
			bTemp[2] = (byte) (data[i + 2] ^ bTemp[2]);
			bTemp[3] = (byte) (data[i + 3] ^ bTemp[3]);
			bTemp[4] = (byte) (data[i + 4] ^ bTemp[4]);
			bTemp[5] = (byte) (data[i + 5] ^ bTemp[5]);
			bTemp[6] = (byte) (data[i + 6] ^ bTemp[6]);
			bTemp[7] = (byte) (data[i + 7] ^ bTemp[7]);
			bTemp = desCrypto(bTemp, lDecrypt);
		}
		return bTemp;
	}
	
	/**
	 * 解密
	 * @param dataSource
	 * @param password
	 * @return 
	 */
	public byte[] decrypt(byte[] dataSource, String password) {
		return decrypt(dataSource, password.getBytes());
	}
}
