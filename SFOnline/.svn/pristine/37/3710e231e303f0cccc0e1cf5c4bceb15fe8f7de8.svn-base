package core.communication.format.string;

import com.ecc.emp.core.EMPConstance;
import com.ecc.emp.data.DataField;
import com.ecc.emp.format.EMPFormatException;
import com.ecc.emp.format.FormatElement;
import com.ecc.emp.log.EMPLog;

/**
 * 
 * <b>功能描述：</b><br>
 * 常量字符串格式，用来按格式生成当前日期<br>
 * 
 * <b>配置示例：</b><br>
 * &lt;fmtDef id="testID"&gt;<br>
 * &nbsp;&nbsp;&lt;record&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&lt;ConstantFormat format="MM-dd-yy"/&gt;<br>
 * &nbsp;&nbsp;&lt;/record&gt;<br>
 * &lt;/fmtDef&gt;<br>
 * 
 * <b>参数说明：</b><br>
 * <b>dataName</b> EMP数据域名称<br>
 * <b>format</b> 日期时间的格式<br>
 * <b>fixed</b> 是否固定长度<br>
 * 若设置fixed=true，则FixedLenFormat的参数都可以使用。参见FixedLenFormat
 * dataName只在unformat且需要将日期回填数据域时需要，同时还需设置参数constant="false"<br>
 * 
 * @author ZhongMingChang
 * @modifier LiJia 2006-12-11
 */
public class ConstantFormat extends FixedLenFormat {

	private String format = "yyyy-MM-dd hh:mm:ss";
	
	private boolean fixed = false;
	
	private String value;
	
	public void setFormat(String format) {
		this.format = format;
	}

	public void setConstant(boolean constant) {
		super.setConstant(constant);
	}
	
	public void setFixed(boolean fixed) {
		this.fixed = fixed;
	}
	   
	public boolean isFixed() {
		return fixed;
	}

	public String getFormat() {
		return format;
	}

	public String getValue() {
		return value;
	}

	public ConstantFormat() {
		super();
		super.setConstant(true);
	}
	
	/**
	 * 将数据域转为常量（时间）字符串
	 * 
	 * @param DataField
	 *            dataField //数据域
	 * @returns Object //格式化后的字符串
	 */
	public Object format(DataField dataField) throws EMPFormatException {

		String value = this.value;
		if (value == null)
			value = getDateTime();
		
		if (fixed) {
			DataField newField = new DataField();
			newField.setValue(value);
			Object fixedLenValue;
			try {
				fixedLenValue = super.format(newField);
			} catch (EMPFormatException e) {
				 EMPLog.log(EMPConstance.EMP_FORMAT,	EMPLog.ERROR, 0,
							"ConstantFormat format failed! Fixed length format error!", e);
				 throw new EMPFormatException(
					"ConstantFormat format failed! Fixed length format error!", e);
			}
			return fixedLenValue;			
			
		} else {
			return value;
		}
	}

	/**
	 * 对输入字符串中要处理的字串进行反格式化并将数据填回EMP数据域。
	 * 
	 * @param Object src //输入串
	 * @param DataField dataField //数据域
	 */
	public void unformat(Object src, DataField dataField) throws EMPFormatException {
		if (dataField != null) {
			if (fixed) {
				super.unformat(src, dataField);
			} 
			else {
				try {
					if (src.getClass().isArray()) {
						if (this.isBin()) {
							dataField.setValue(src);
						}
						else {
							dataField.setValue(new String((byte[])src, this.getEncoding()));
						}
					}
					else {
						if (this.isBin()) {
							dataField.setValue(((String)src).getBytes(this.getEncoding()));
						}
						else {
							dataField.setValue(src);
						}
					}
				}
				catch (Exception e) {
					throw new EMPFormatException("ConstantFormat unformat failed: invalid charset code!", e);
				}
			}
		}
	}
	
	/**
	 * 根据format的设置返回格式化的日期或时间
	 * 
	 * @return String //格式化的日期或时间
	 */
	public String getDateTime() {
		java.util.Calendar calendar = java.util.Calendar.getInstance();
		java.util.Date date = calendar.getTime();

		if (format.equals("TimeMillis")) {
			return String.valueOf(System.currentTimeMillis());
		} 
		else {
			java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat(format);
			return formatter.format(date);
		}
	}

	public String toString() {
		return toString(0);
	}

	public String toString(int tabCount) {
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < tabCount; i++)
			buf.append("\t");

		buf.append("<ConstantFormat format=\"");
		buf.append(format);
		buf.append("\"/>\n");

		for (int i = 0; i < this.getDecorators().size(); i++) {
			FormatElement fmt = (FormatElement) getDecorators().get(i);
			buf.append(fmt.toString(tabCount));
			buf.append("\n");
		}

		return buf.toString();
	}

	public void setValue(String value) {
		this.value = value;
	}

}
