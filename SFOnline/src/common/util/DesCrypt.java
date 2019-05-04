package common.util;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
/**
 * FTP�����㷨
 * @author ����
 *
 */
public class DesCrypt {

  private static String Algorithm = "DES"; //���� �����㷨,���� DES,DESede,Blowfish
  private static byte[] defaultKey = getDefaultKey(); //�ֹ�ָ����Կ
  static boolean debug = false;

  //������Կ, ע��˲���ʱ��Ƚϳ�
  public static byte[] getKey() throws Exception {
    KeyGenerator keygen = KeyGenerator.getInstance(Algorithm);
    SecretKey deskey = keygen.generateKey();
    if (debug) {
      System.out.println("������Կ:" + byte2hex(deskey.getEncoded()));
    }
    return deskey.getEncoded();
  }

  //����Ĭ����Կ
  public static byte[] getDefaultKey() {
    String defaultKey = "";
    for (int i = 1; i < 9; i++) {
      char j = (char) (i * i + 33 + i + (i * i) % 10 + (19 * i) % 9);
      defaultKey = defaultKey + String.valueOf(j);
    }
    return defaultKey.getBytes();
  }

  //ָ����Կ����
  public static byte[] encode(byte[] input, byte[] key) throws Exception {
    SecretKey deskey = new javax.crypto.spec.SecretKeySpec(key, Algorithm);
    if (debug) {
      System.out.println("����ǰ���ַ���:" + new String(input));
      System.out.println("����ǰ�Ķ�����:" + byte2hex(input));
    }
    Cipher c1 = Cipher.getInstance(Algorithm);
    c1.init(Cipher.ENCRYPT_MODE, deskey);
    byte[] cipherByte = c1.doFinal(input);
    if (debug) {
      System.out.println("���ܺ�Ķ�����:" + byte2hex(cipherByte));
    }
    return cipherByte;
  }

  //ָ����Կ����
  public static byte[] decode(byte[] input, byte[] key) throws Exception {
    SecretKey deskey = new javax.crypto.spec.SecretKeySpec(key, Algorithm);
    if (debug) {
      System.out.println("����ǰ�Ķ�����:" + byte2hex(input));
    }
    Cipher c1 = Cipher.getInstance(Algorithm);
    c1.init(Cipher.DECRYPT_MODE, deskey);
    byte[] clearByte = c1.doFinal(input);
    if (debug) {
      System.out.println("���ܺ�Ķ�����:" + byte2hex(clearByte));
      System.out.println("���ܺ���ַ���:" + (new String(clearByte)));
    }
    return clearByte;
  }

  //Ĭ����Կ����
  public static String defaultEncode(String input) throws Exception {
    return hex2string(encode(input.getBytes(), defaultKey));
  }

  //Ĭ����Կ����
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

  //�ֽ���ת����16�����ַ���
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

    System.out.println("����ǰ�����ģ�" + mingSrc);
    System.out.println("���ģ�" + mi);
    System.out.println("���ܺ�����ģ�" + mingDes);
  }

}
