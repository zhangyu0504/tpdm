package core.communication.format;

/**
 * 格式化修饰符基类。
 * 
 * @author zhongmc
 * @version 2.1
 * @since 1.0 2006-12-4
 * @lastmodified 2008-7-2
 */
public class Decorator extends PBankFormatElement {

	public Decorator() {

	}

	/**
	 * 为报文增加修饰内容。
	 * 
	 * @param src 报文
	 * @return Object 修饰后的报文
	 */
	public Object addDecoration(Object src) {
		return null;
	}

	/**
	 * 为报文移除修饰内容。
	 * 
	 * @param src 报文
	 * @return Object 移除修饰后的报文
	 */
	public Object removeDecoration(Object src) {
		return null;
	}
}
