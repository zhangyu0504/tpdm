package common.util;

import java.security.SecureRandom;
import java.util.Vector;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

/**
 * 
 * MacUtil<br>
 * Copyright (c) 2012, 2018 pingan Corporation <br>
 *
 * @author yudongming052<br>
 * @version 1.0.0<br>
 * @since 2013-10-21<br>
 *
 * MacUtil是一个用于软加密的MAC工具类。
 * MAC算法：“ 串"00000000"与数据进行8字节异或, 异或的结果进行DES运算作为 下次异或的初值, 若数据长度不为8的整数倍,
 * 			则添空字符0x00”
 *
 */
public class MacUtil {

	/**
	 * 把字节数组转换为16进制的形式的字符串
	 * 
	 * @param byte[] b 待转换字节数组
	 * @return String 字符串
	 */
	private static String bytes2HexString(byte[] b) {
		String ret = "";
		for (int i = 0; i < b.length; i++) {
			String hex = Integer.toHexString(b[i] & 0xFF);
			if (hex.length() == 1) {
				hex = '0' + hex;
			}
			ret += hex.toUpperCase();
		}
		return ret;
	}

    /**
     * 十六进制字符串转化为字节数组
     * @param String 字符串
     * @return byte[] 字节数组
     */
	private static byte[] hex2Byte(String str) { 
    	if(str == null){
    		return null;
    	}
    	str = str.trim();
        int len = str.length(); 
        if(len == 0 || len % 2 == 1){
        	return null;
        }
        byte[] b = new byte[len/2];
        for (int   i   =   0;   i   <   len;   i+=2) {
        	b[i/2] = Integer.decode("0x"+str.substring(i,i+2)).byteValue();
        }
        return b;
    }	
    /**
     * 生成16位MAC数据串
     * @param String key 密钥
     * @param String data 待加密数据
     * @return String 16位MAC字符串
     * @throws Exception
     */
    public static String generateMac16(String key,String data) throws Exception{
    	if(key == null || "".equals(key))
    		return null;
    	if(data == null || "".equals(data)) 
    		return null;
    	
    	byte[] keyBytes = hex2Byte(key);
    	byte[] macData = data.getBytes();
    	return bytes2HexString(countMACx9_9(keyBytes, macData, 0, -1));
    }
    /**
     * 生成8位MAC数据串
     * @param String key MAC密钥
     * @param String data 待加密数据报文
     * @return String 8位MAC字符串
     * @throws Exception
     */
    public static String generateMac8(String key,String data) throws Exception{
    	if(key == null || "".equals(key))
    		return null;
    	if(data == null || "".equals(data)) 
    		return null;
    	
    	byte[] keyBytes = hex2Byte(key);
    	byte[] macData = data.getBytes();
    	return bytes2HexString(countMACx9_9(keyBytes, macData, 0, -1)).substring(0,8);
    }
    /**
     * 数据报文进行MAC校验
     * @param String key MAC密钥
     * @param String data 待校验的数据报文
     * @return TRUE-校验通过 FALSE-校验失败
     * @throws Exception
     */
    public static boolean checkMac(String key,String data) throws Exception{
    	if(data == null || "".equals(data) || data.getBytes().length < 8)
    		return false;
    	//获得数据报文中的MAC
    	String vMac = subString(data,data.getBytes().length-8,8);
    	
    	//待加密码的数据报文
    	String vData = subString(data,0,data.getBytes().length-8);
    	
    	//加密报文
    	String  mac = generateMac8(key,vData);
    	
    	if(vMac.equals(mac)) {
    		return true;
    	}
    	return false;
    }
    
    /**
	 * 按字节截取字符串的字符
	 * 
	 * @param str
	 *            被处理字符串
	 * @param startIdx
	 *            截取位置
	 * @param toCount
	 *            截取长度
	 * @return String
	 */
	private static String subString(String str, int startIdx, int toCount) {
		try {
			if (str == null){
				return str;
			}
			if((startIdx+toCount)>str.getBytes().length){
				return str;
			}
			byte[] bStr = str.getBytes();
			byte[] rStr = new byte[toCount];
			for (int i = startIdx, j = 0; i < startIdx + toCount; i++) {
				rStr[j++] = bStr[i];
			}

			return new String(rStr);
		} catch (Exception ex) {
			return str;
		}
	}
	/**
	 * 采用x9.9算法计算MAC (Count MAC by ANSI-x9.9).
	 * 
	 * 算法： 串"00000000"与数据进行8字节异或, 异或的结果进行DES运算作为 下次异或的初值, 若数据长度不为8的整数倍,
	 * 则添空字符0x00.
	 * 
	 * @param tKey
	 *            密钥数据
	 * @param tBuffer
	 *            待计算的缓冲区
	 * @param iOffset
	 *            数据的偏移量(0,1,...，即起始位置)
	 * @param iLength
	 *            数据的长度(<0 - 默认值，即整个长度)
	 * @return MAC值(null -- 无效)
	 * 
	 */
	private static byte[] countMACx9_9(byte[] tKey, byte[] tBuffer,
			int iOffset, int iLength) throws Exception {
		byte[] tResult = null;
		Vector vctBlk = new Vector();
		byte[] tTmp, tBlk, tXor, tDes,tSrc;
		int iNum, iLen, iPos, iN, i, j, prt1;

		if (tKey == null || tBuffer == null)
			return tResult;

		if (iOffset < 0)
			iOffset = 0;

		if (iLength < 0)
			iLength = tBuffer.length - iOffset;

		/* 拆分数据（8字节块/Block） */
		iLen = 0;
		iPos = iOffset;
		while (iLen < iLength && iPos < tBuffer.length) {
			tBlk = new byte[8];
			// 初始化8字节串为0x00
			reset0x00(tBlk);
			// 以8字节/块进行数据拆分，并存入tBlk
			for (i = 0; i < tBlk.length && iLen < iLength
					&& iPos < tBuffer.length; i++) {
				tBlk[i] = tBuffer[iPos++];
				iLen++;
			}
			// 将拆分后的字节块追加到集合中
			vctBlk.addElement(tBlk); // store (back)
		}

		/* 循环计算（XOR + DES） 异或的结果进行DES运算后作为下次异或的初值, 若数据长度不为8的整数倍, 则添空字符0x00. */

		// 初始化异或初值
		tDes = new byte[8]; // 初始数据
		reset0x00(tDes);
		iNum = vctBlk.size();
		// 循环处理字节块
		for (iN = 0; iN < iNum; iN++) {
			tBlk = (byte[]) vctBlk.elementAt(iN);
			if (tBlk == null)
				continue;

			tXor = new byte[Math.min(tDes.length, tBlk.length)];

			// 按位异或
			for (i = 0; i < tXor.length; i++)
				tXor[i] = (byte) (tDes[i] ^ tBlk[i]);// 异或(Xor)

			// 对异或后的结果进行DES运算
			tTmp = encryptDES(tKey, tXor); // DES加密
			
			//清空初值
			reset0x00(tDes);
			
			//DES运算后的值赋值为下次异或的初值
			for (i = 0; i < Math.min(tDes.length, tTmp.length); i++)
				tDes[i] = tTmp[i]; // copy / transfer
		}

		vctBlk.removeAllElements(); // clear

		tResult = tDes;
		return tResult;
	}

	/**
	 * 清空数组内容为0x00
	 * @param b 待清空byte数组
	 * @return 清空后的byte数组
	 */
	private static byte[] reset0x00(byte[] b){
		int i;
		for (i = 0; i < b.length; i++)
			b[i] = (byte) 0;
		return b;
	}
	/**
	 * DES加密
	 * 
	 * @param key
	 *            密钥数据
	 * @param src
	 *            待加密的缓冲区
	 * @return 加密后的缓冲区
	 * @throws Exception
	 */
	private static byte[] encryptDES(byte[] key, byte[] src) throws Exception {
//		System.out.println("Key长度:["+key.length+"],数据长度:["+src.length+"]");
		// DES算法要求有一个可信任的随机数源
		SecureRandom sr = new SecureRandom();
		// 从原始密匙数据创建DESKeySpec对象
		DESKeySpec dks = new DESKeySpec(key);
		// 创建一个密匙工厂，然后用它把DESKeySpec转换成 一个SecretKey对象
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
		SecretKey securekey = keyFactory.generateSecret(dks);
		// Cipher对象实际完成加密操作
		Cipher cipher = Cipher.getInstance("DES/ECB/NoPadding");
		// 用密匙初始化Cipher对象
		cipher.init(Cipher.ENCRYPT_MODE, securekey, sr);
		// 现在，获取数据并加密 正式执行加密操作
		return cipher.doFinal(src);
	}

	/**
	 * 解密 
	 * @param src 数据源
	 * @param key 密钥，长度必须是8的倍数  
	 * @return 返回解密后的原始数据 
	 * @throws  Exception
	 */
	private static byte[] decrypt(byte[] src, byte[] key) throws Exception {
		// DES算法要求有一个可信任的随机数源
		SecureRandom sr = new SecureRandom();
		// 从原始密匙数据创建一个DESKeySpec对象
		DESKeySpec dks = new DESKeySpec(key);
		// 创建一个密匙工厂，然后用它把DESKeySpec对象转换成一个SecretKey对象
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
		SecretKey securekey = keyFactory.generateSecret(dks);
		// Cipher对象实际完成解密操作
		Cipher cipher = Cipher.getInstance("DES");
		// 用密匙初始化Cipher对象
		cipher.init(Cipher.DECRYPT_MODE, securekey, sr);
		// 现在，获取数据并解密 正式执行解密操作
		return cipher.doFinal(src);
	}
}
