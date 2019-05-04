package core.communication.format;

import java.io.ByteArrayOutputStream;

import com.dc.eai.data.AtomData;
import com.ecc.emp.core.Context;
import com.ecc.emp.data.DataElement;
import com.ecc.emp.format.EMPFormatException;
import com.ecc.emp.format.FormatElement;

/**
 * 格式化元素基类，以及格式化处理器的入口。
 *
 * @author zhongmc
 * @version 2.1
 * @since 1.0 2007-11-9
 * @lastmodified 2008-7-2
 */
public class PBankFormatElement extends FormatElement{
	/**
	 * PBank重构,该字段意思修改为format和unformat的结果是否是二进制格式
	 */
	//private boolean isBin = false;

	/**
	 * 是否以二进制方式处理字段
	 */
	private boolean isBinType = true;

	/**
	 * 编码方式
	 */
	private String encoding = "GB18030";

	/**
	 * 格式描述
	 */
	private String desc = null;
	
	/**
	 * 反格式化时KCOLL,ICOLL,FIELD是否追加模式
	 */
	private boolean append = true;//false
	
	/**
	 * 反格式化时KCOLL,ICOLL,FIELD追加类名
	 */
	private String appendClass = null;
	
	/**
	 * 报文格式类型
	 */
	private String formatType = "tcpip";
	
	/**
	 * 为支持ESB报文或固定格式报文DataName可以定义表达式
	 */
	private boolean isExpression = false;

	public PBankFormatElement() {
		super();
	}

	public PBankFormatElement(String name) {
		super(name);
	}

	/**
	 * 打包报文的调用入口。
	 * <p>
	 * 应用系统调用它进行数据报文组包，
	 * EMP将根据报文的设定返回二进制格式byte[]的数据或者字符串数据。
	 * 
	 * @param context 交易上下文
	 * @return Object 打包后的报文
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
	 * ESB格式报文的打包入口。
	 * 
	 * @param dataElement 数据模型(kColl)
	 * @param context 上下文对象
	 * @return Object ESB格式对象
	 * @throws Exception
	 */
	public Object format(DataElement dataElement, Context context) throws Exception {
		return null;
	}

	/**
	 * 数据解包的调用入口。
	 * <p>
	 * 根据设定是否为二进制格式，分别采用二进制解包或字符格式解包。
	 * 
	 * @param src 待解报文
	 * @param context 交易上下文
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
	 * ESB格式报文的解包入口。
	 * 
	 * @param dataElement 数据模型(kColl)
	 * @param atomData ESB数据对象
	 * @param context 上下文对象
	 * @throws Exception
	 */
	public void unformat(DataElement dataElement, AtomData atomData, Context context) throws Exception {
		return;
	}
	
	/**
	 * 获得该类的字符串表现。
	 * 
	 * @param 缩进量
	 * @return 该类的字符串表现
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
	 * 判断反格式化时对DataField,ICOLL,KCOLL是否追加模式。
	 * 
	 * @return 是否追加模式
	 */
	public boolean isAppend() {
		return append;
	}

	/**
	 * 设置反格式化时对DataField,ICOLL,KCOLL是否追加模式。
	 * 
	 * @param append 是否追加模式
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
	 * 设置反格式化时对DataField,ICOLL,KCOLL追加类类名。
	 * 
	 * @param append 追加类的类名
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
