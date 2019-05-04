package core.communication.format.xml;

import com.ecc.emp.core.Context;
import com.ecc.emp.core.EMPException;
import com.ecc.emp.data.DataElement;
import com.ecc.emp.format.FormatElement;

import core.communication.format.FormatField;

/**
 * 
 * <b>功能描述：</b><br>
 * XML头部信息解析，格式类似于&lt;?xml version=&quot;1.0&quot; encoding=&quot;gb2312&quot; ?&gt;<br>
 * 
 * <b>配置示例：</b><br>
 * &lt;xmlHead version="1.0" encoding="gb2312"/&gt;<br>
 * 
 * <b>参数说明：</b><br>
 * <b>version</b> XML版本<br>
 * <b>encoding</b> XML编码<br>
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
	 * 格式化数据元素，返回XML头部信息字符串，和数据域内容无关
	 * 
	 * @param DataElement dataElement //数据域
	 * @return Object //格式化后的字符串
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
	 * 格式化入口，供XMLWrapFormat调用。得到XML头部信息并将其追加到字符串缓冲区
	 * 
	 * @param StringBuffer sb //字符串缓冲区
	 * @param DataElement dataElement //欲格式化的数据域
	 */
	public void format(StringBuffer sb, DataElement dataElement) {
		sb.append(format(dataElement));
	}

	/**
	 * 格式化入口，供XMLWrapFormat调用。得到XML头部信息并将其追加到字符串缓冲区
	 * 
	 * @param StringBuffer sb //字符串缓冲区
	 * @param Context context //逻辑处理的上下文
	 */
	public void format(StringBuffer sb, Context context) {
		sb.append(format((DataElement) null));
	}

	/**
	 * 对输入字符串中的下一个XML头部信息进行反格式化（实际上只返回长度）。
	 * 
	 * @param String src //XML格式输入串
	 * @param int offset //偏移量
	 * @param DataElement dataElement //接收数据域
	 * @return int //已处理的字串长度
	 * @throws EMPException
	 */
	public void unformat(String src, DataElement dataElement) throws EMPException {
		
	}

	/**
	 * 对输入字符串中的下一个XML头部信息进行反格式化（实际上只返回长度）。
	 * 
	 * @param String src //XML格式输入串
	 * @param int offset //偏移量
	 * @param Context context //逻辑处理的上下文
	 * @return int //已处理的字串长度
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
