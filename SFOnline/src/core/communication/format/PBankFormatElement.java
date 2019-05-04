package core.communication.format;

import java.io.ByteArrayOutputStream;

import com.dc.eai.data.AtomData;
import com.ecc.emp.core.Context;
import com.ecc.emp.data.DataElement;
import com.ecc.emp.format.EMPFormatException;
import com.ecc.emp.format.FormatElement;

/**
 * ��ʽ��Ԫ�ػ��࣬�Լ���ʽ������������ڡ�
 *
 * @author zhongmc
 * @version 2.1
 * @since 1.0 2007-11-9
 * @lastmodified 2008-7-2
 */
public class PBankFormatElement extends FormatElement{
	/**
	 * PBank�ع�,���ֶ���˼�޸�Ϊformat��unformat�Ľ���Ƿ��Ƕ����Ƹ�ʽ
	 */
	//private boolean isBin = false;

	/**
	 * �Ƿ��Զ����Ʒ�ʽ�����ֶ�
	 */
	private boolean isBinType = true;

	/**
	 * ���뷽ʽ
	 */
	private String encoding = "GB18030";

	/**
	 * ��ʽ����
	 */
	private String desc = null;
	
	/**
	 * ����ʽ��ʱKCOLL,ICOLL,FIELD�Ƿ�׷��ģʽ
	 */
	private boolean append = true;//false
	
	/**
	 * ����ʽ��ʱKCOLL,ICOLL,FIELD׷������
	 */
	private String appendClass = null;
	
	/**
	 * ���ĸ�ʽ����
	 */
	private String formatType = "tcpip";
	
	/**
	 * Ϊ֧��ESB���Ļ�̶���ʽ����DataName���Զ�����ʽ
	 */
	private boolean isExpression = false;

	public PBankFormatElement() {
		super();
	}

	public PBankFormatElement(String name) {
		super(name);
	}

	/**
	 * ������ĵĵ�����ڡ�
	 * <p>
	 * Ӧ��ϵͳ�������������ݱ��������
	 * EMP�����ݱ��ĵ��趨���ض����Ƹ�ʽbyte[]�����ݻ����ַ������ݡ�
	 * 
	 * @param context ����������
	 * @return Object �����ı���
	 * @throws EMPFormatException
	 */
	public Object format(Context context) throws EMPFormatException {
		try {
			if (formatType.equals("tcpip")) {
				if (this.isBin()) {
					ByteArrayOutputStream bo = new ByteArrayOutputStream();
					this.getFormatElement().format(bo, context);
					return bo.toByteArray();
				} else {
					StringBuffer buf = new StringBuffer();
					this.getFormatElement().format(buf, context);
					return buf.toString();
				}
			}
			else if (formatType.equals("paesb") || formatType.equals("xml")) {
				return this.getFormatElement().format(context);
			}
		} catch (EMPFormatException fe) {
			throw fe;
		} catch (Exception e) {
			throw new EMPFormatException(e);
		}

		return null;
	}

	/**
	 * ESB��ʽ���ĵĴ����ڡ�
	 * 
	 * @param dataElement ����ģ��(kColl)
	 * @param context �����Ķ���
	 * @return Object ESB��ʽ����
	 * @throws Exception
	 */
	public Object format(DataElement dataElement, Context context) throws Exception {
		return null;
	}

	/**
	 * ���ݽ���ĵ�����ڡ�
	 * <p>
	 * �����趨�Ƿ�Ϊ�����Ƹ�ʽ���ֱ���ö����ƽ�����ַ���ʽ�����
	 * 
	 * @param src ���ⱨ��
	 * @param context ����������
	 * @throws Exception
	 */
	public void unFormat(Object src, Context context) throws Exception {
		if (formatType.equals("tcpip")) {
			if (this.isBin())
				this.getFormatElement().unformat((byte[]) src, 0, context);
			else
				this.getFormatElement().unformat((String) src, 0, context);
		}
		else if (formatType.equals("paesb") || formatType.equals("xml")) {
			this.getFormatElement().unFormat(src, context);
		}

	}

	/**
	 * ESB��ʽ���ĵĽ����ڡ�
	 * 
	 * @param dataElement ����ģ��(kColl)
	 * @param atomData ESB���ݶ���
	 * @param context �����Ķ���
	 * @throws Exception
	 */
	public void unformat(DataElement dataElement, AtomData atomData, Context context) throws Exception {
		return;
	}
	
	/**
	 * ��ø�����ַ������֡�
	 * 
	 * @param ������
	 * @return ������ַ�������
	 */
	public String toString(int tabCount) {
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < tabCount; i++)
			buf.append("\t");

		buf.append("<fmtDef id=\"");
		buf.append(getName());

		buf.append("\" isBin=\"");
		buf.append(String.valueOf(this.isBin()));
		buf.append("\" isBinType=\"");
		buf.append(String.valueOf(isBinType));
		buf.append("\" encoding=\"");
		buf.append(String.valueOf(encoding));
		buf.append("\">\n");

		buf.append(this.getFormatElement().toString(tabCount + 1));

		for (int i = 0; i < tabCount; i++)
			buf.append("\t");
		buf.append("\n</fmtDef>");
		return buf.toString();
	}

	public boolean isBinType() {
		return isBinType;
	}

	public void setIsBinType(boolean isBinType) {
		this.isBinType = isBinType;
	}

	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}
	
	public String getDesc() {
		return desc;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}
	
	/**
	 * �жϷ���ʽ��ʱ��DataField,ICOLL,KCOLL�Ƿ�׷��ģʽ��
	 * 
	 * @return �Ƿ�׷��ģʽ
	 */
	public boolean isAppend() {
		return append;
	}

	/**
	 * ���÷���ʽ��ʱ��DataField,ICOLL,KCOLL�Ƿ�׷��ģʽ��
	 * 
	 * @param append �Ƿ�׷��ģʽ
	 */
	public void setAppend(boolean append) {
		this.append = append;
	}

	public String getFormatType() {
		return formatType;
	}

	public void setFormatType(String formatType) {
		this.formatType = formatType;
	}
	/**
	 * ���÷���ʽ��ʱ��DataField,ICOLL,KCOLL׷����������
	 * 
	 * @param append ׷���������
	 */
	public String getAppendClass() {
		return appendClass;
	}

	public boolean isExpression() {
		return isExpression;
	}

	public void setIsExpression(boolean isExpression) {
		this.isExpression = isExpression;
	}

	public void setAppendClass(String appendClass) {
		if ("KeyedCollection".equals(appendClass)){
			appendClass = "com.ecc.emp.data.KeyedCollection";
		}
		else if ("IndexedCollection".equals(appendClass)){
			appendClass = "com.ecc.emp.data.IndexedCollection";
		}
		else if ("DataField".equals(appendClass)){
			appendClass = "com.ecc.emp.data.DataField";
		}
		this.appendClass = appendClass;
	}
}
