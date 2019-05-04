package common.util;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
/**
 * FTP加密算法
 * @author 汪华
 *
 */
public class DesCrypt {

  private static String Algorithm = "DES"; //定义 加密算法,可用 DES,DESede,Blowfish
  private static byte[] defaultKey = getDefaultKey(); //手工指定密钥
  static boolean debug = false;

  //生成密钥, 注意此步骤时间比较长
  public static byte[] getKey() throws Exception {
    KeyGenerator keygen = KeyGenerator.getInstance(Algorithm);
    SecretKey deskey = keygen.generateKey();
    if (debug) {
      System.out.println("生成密钥:" + byte2hex(deskey.getEncoded()));
    }
    return deskey.getEncoded();
  }

  //生成默认密钥
  public static byte[] getDefaultKey() {
    String defaultKey = "";
    for (int i = 1; i < 9; i++) {
      char j = (char) (i * i + 33 + i + (i * i) % 10 + (19 * i) % 9);
      defaultKey = defaultKey + String.valueOf(j);
    }
    return defaultKey.getBytes();
  }

  //指定密钥加密
  public static byte[] encode(byte[] input, byte[] key) throws Exception {
    SecretKey deskey = new javax.crypto.spec.SecretKeySpec(key, Algorithm);
    if (debug) {
      System.out.println("加密前的字符串:" + new String(input));
      System.out.println("加密前的二进串:" + byte2hex(input));
    }
    Cipher c1 = Cipher.getInstance(Algorithm);
    c1.init(Cipher.ENCRYPT_MODE, deskey);
    byte[] cipherByte = c1.doFinal(input);
    if (debug) {
      System.out.println("加密后的二进串:" + byte2hex(cipherByte));
    }
    return cipherByte;
  }

  //指定密钥解密
  public static byte[] decode(byte[] input, byte[] key) throws Exception {
    SecretKey deskey = new javax.crypto.spec.SecretKeySpec(key, Algorithm);
    if (debug) {
      System.out.println("解密前的二进串:" + byte2hex(input));
    }
    Cipher c1 = Cipher.getInstance(Algorithm);
    c1.init(Cipher.DECRYPT_MODE, deskey);
    byte[] clearByte = c1.doFinal(input);
    if (debug) {
      System.out.println("解密后的二进串:" + byte2hex(clearByte));
      System.out.println("解密后的字符串:" + (new String(clearByte)));
    }
    return clearByte;
  }

  //默认密钥加密
  public static String defaultEncode(String input) throws Exception {
    return hex2string(encode(input.getBytes(), defaultKey));
  }

  //默认密钥解密
  public static String defaultDecode(String input) throws Exception {
    return new String(decode(hex2byte(input), defaultKey));
  }

//hex  to  string
  public static String hex2string(byte[] b) {
    StringBuffer d = new StringBuffer(b.length * 2);
    for (int i = 0; i < b.length; i++) {
      char hi = Character.forDigit( (b[i] >> 4) & 0x0F, 16);
      char lo = Character.forDigit(b[i] & 0x0F, 16);
      d.append(Character.toUpperCase(hi));
      d.append(Character.toUpperCase(lo));
    }
    return d.toString();
  }

  //字节码转换成16进制字符串
  public static String byte2hex(byte[] b) {
    String hs = "";
    String stmp = "";
    for (int n = 0; n < b.length; n++) {
      stmp = (java.lang.Integer.toHexString(b[n] & 0XFF));
      if (stmp.length() == 1) {
        hs = hs + "0" + stmp;
      }
      else {
        hs = hs + stmp;
      }
      if (n < b.length - 1) {
        hs = hs + ":";
      }
    }
    return hs.toUpperCase();
  }

  public static byte[] hex2byte(byte[] b, int offset, int len) {
    byte[] d = new byte[len];
    for (int i = 0; i < len * 2; i++) {
      int shift = i % 2 == 1 ? 0 : 4;
      d[i >> 1] |= Character.digit( (char) b[offset + i], 16) << shift;
    }
    return d;
  }

//hex  to  byte
  public static byte[] hex2byte(String s) {
    return hex2byte(s.getBytes(), 0, s.length() >> 1);
  }

  public static void main(String[] args) throws Exception {
    debug = true;
    String mingSrc = "fbs";
    String mi = defaultEncode(mingSrc);
    String mingDes = defaultDecode(mi);

    System.out.println("加密前的明文：" + mingSrc);
    System.out.println("密文：" + mi);
    System.out.println("解密后的明文：" + mingDes);
  }

}
