package core.communication.format.xml;

import com.ecc.emp.core.Context;
import com.ecc.emp.core.EMPException;
import com.ecc.emp.data.DataElement;
import com.ecc.emp.format.FormatElement;

import core.communication.format.FormatField;

/**
 * 
 * <b>����������</b><br>
 * XMLͷ����Ϣ��������ʽ������&lt;?xml version=&quot;1.0&quot; encoding=&quot;gb2312&quot; ?&gt;<br>
 * 
 * <b>����ʾ����</b><br>
 * &lt;xmlHead version="1.0" encoding="gb2312"/&gt;<br>
 * 
 * <b>����˵����</b><br>
 * <b>version</b> XML�汾<br>
 * <b>encoding</b> XML����<br>
 * 
 * @create date:2000-03-02 <br>
 * @author ZhongMingChang
 * @modifier LiJia 2006-10-24
 */
public class XMLHeadFormat extends FormatField
{

	private String version="1.0";

	private String encoding="GBK";

	public XMLHeadFormat() {
		super();
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public String getEncoding() {
		return encoding;
	}

	public String getVersion() {
		return version;
	}

	private String textIndent = "\t";
	public String getTextIndent() {
		return textIndent;
	}

	public void setTextIndent(String textIndent) {
		this.textIndent = textIndent;
	}
	public void updateTextIndent(String aTextIndent) {
		this.textIndent = aTextIndent+this.textIndent;
	}
	/**
	 * ��ʽ������Ԫ�أ�����XMLͷ����Ϣ�ַ������������������޹�
	 * 
	 * @param DataElement dataElement //������
	 * @return Object //��ʽ������ַ���
	 */
	public Object format(DataElement dataElement) {
		StringBuffer retStr = new StringBuffer();

		retStr.append(this.getTextIndent() + "<?xml version=\"").append(version);
		retStr.append("\" encoding=\"").append(encoding).append("\"?>");

		return retStr.toString();
	}

	public Object format(Context context) {
		StringBuffer retStr = new StringBuffer();

		retStr.append(this.getTextIndent() + "<?xml version=\"").append(version);
		retStr.append("\" encoding=\"").append(encoding).append("\"?>");

		return retStr.toString();
	}

	/**
	 * ��ʽ����ڣ���XMLWrapFormat���á��õ�XMLͷ����Ϣ������׷�ӵ��ַ���������
	 * 
	 * @param StringBuffer sb //�ַ���������
	 * @param DataElement dataElement //����ʽ����������
	 */
	public void format(StringBuffer sb, DataElement dataElement) {
		sb.append(format(dataElement));
	}

	/**
	 * ��ʽ����ڣ���XMLWrapFormat���á��õ�XMLͷ����Ϣ������׷�ӵ��ַ���������
	 * 
	 * @param StringBuffer sb //�ַ���������
	 * @param Context context //�߼������������
	 */
	public void format(StringBuffer sb, Context context) {
		sb.append(format((DataElement) null));
	}

	/**
	 * �������ַ����е���һ��XMLͷ����Ϣ���з���ʽ����ʵ����ֻ���س��ȣ���
	 * 
	 * @param String src //XML��ʽ���봮
	 * @param int offset //ƫ����
	 * @param DataElement dataElement //����������
	 * @return int //�Ѵ�����ִ�����
	 * @throws EMPException
	 */
	public void unformat(String src, DataElement dataElement) throws EMPException {
		
	}

	/**
	 * �������ַ����е���һ��XMLͷ����Ϣ���з���ʽ����ʵ����ֻ���س��ȣ���
	 * 
	 * @param String src //XML��ʽ���봮
	 * @param int offset //ƫ����
	 * @param Context context //�߼������������
	 * @return int //�Ѵ�����ִ�����
	 * @throws EMPException
	 */
	public void unformat(DataElement dataElement, String src, Context context) throws EMPException {
		
	}

	public String toString() {
		return toString(0);
	}

	public String toString(int tabCount) {
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < tabCount; i++)
			buf.append("\t");

		buf.append("<xmlHead version=\"").append(version);
		buf.append("\" encoding=\"").append(encoding).append("\"/>\n");

		for (int i = 0; i < this.getDecorators().size(); i++) {
			FormatElement fmt = (FormatElement) getDecorators().get(i);
			buf.append(fmt.toString(tabCount));
		}

		return buf.toString();
	}
}
