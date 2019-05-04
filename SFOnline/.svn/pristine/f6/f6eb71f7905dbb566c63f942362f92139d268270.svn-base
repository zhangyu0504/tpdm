/*
 * Created on 2003-4-26
 */
package core.communication.util;

/**
 * 字符串工具类
 * @corporation FinWare Technology
 * @author international settlement team
 * @version 1.0
 * @createDate 2003-4-26
 * @createTime 14:29:09
 */




public final class StringUtil {
	/**
	 * 对字符串进行格式验证，如果字符串全部是数字，并且可以转为int类型，返回true,其他返回false.
	 * 
	 * @return
	 */
	public static boolean isDigital(String str) {
		boolean isDigit = false;
		if (verifyString(str) && str.matches("\\d{1,10}")) {
			isDigit = true;
		}
		return isDigit;
	}
	/**
	 * 对字符串进行非空验证，如果字符串为null或者""返回false.
	 * 
	 * @return
	 */
	public static boolean verifyString(String str) {
		return str != null && str.trim().length() > 0;
	}
	
}
