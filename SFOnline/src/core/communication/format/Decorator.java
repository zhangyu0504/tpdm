package core.communication.format;

/**
 * ��ʽ�����η����ࡣ
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
	 * Ϊ���������������ݡ�
	 * 
	 * @param src ����
	 * @return Object ���κ�ı���
	 */
	public Object addDecoration(Object src) {
		return null;
	}

	/**
	 * Ϊ�����Ƴ��������ݡ�
	 * 
	 * @param src ����
	 * @return Object �Ƴ����κ�ı���
	 */
	public Object removeDecoration(Object src) {
		return null;
	}
}
