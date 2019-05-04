package core.communication.format.paesb;

import java.math.BigDecimal;

import com.dc.eai.data.AtomData;
import com.dc.eai.data.CompositeData;
import com.dc.eai.data.Field;
import com.dc.eai.data.FieldAttr;
import com.dc.eai.data.FieldType;
import com.ecc.emp.core.Context;
import com.ecc.emp.data.DataElement;
import com.ecc.emp.data.DataField;
import com.ecc.emp.data.KeyedCollection;
import com.ecc.emp.format.EMPFormatException;
import com.ecc.emp.format.FormatElement;
import com.ecc.emp.log.EMPLog;
import common.util.SFUtil;

import core.communication.format.KeyedFormat;
import core.communication.format.string.FixedLenFormat;
import core.log.SFLogger;

/**
 * 
 * <b>功能描述：</b><br>
 * ESB Field格式<br>
 * 可自定义长度、精度、类型、空白填充字符和对齐方式<br>
 * 
 * <b>配置示例：</b><br>
 * &lt;fmtDef id="testID"&gt;<br>
 * &nbsp;&nbsp;&lt;record&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&lt;ESBFieldFormat dataName="myName" len="10" padChar="*" aligment="Left"&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;delim delimChar=";"/&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&lt;/ESBFieldFormat&gt;<br>
 * &nbsp;&nbsp;&lt;/record&gt;<br>
 * &lt;/fmtDef&gt;<br>
 * 
 * <b>参数说明：</b><br>
 * <b>dataName</b> EMP数据域名称<br>
 * <b>len</b> 字符串长度<br>
 * <b>padChar</b> 空白填充字符，默认为空格<br>
 * <b>aligment</b> 对其方式，取值为none（默认），left，right，center：
 * 格式化时none相当于left；反格式化时取值为none代表不去掉任何空白填充字符。
 * 
 * @author ZhongMingChang
 * @modifier LiJia 2006-12-11
 */
public class ESBFieldFormat extends FixedLenFormat{

	//ESB报文规范中的长度
	private int esbLen = 0;

	//ESB报文规范中的刻度
	private int scale = 0;

	//ESB报文规范中的报文类型,默认是字符串
	private String type = "string";
	
	//该字段是否直接从context中取值或直接赋值到context中的变量,ESBCDFormat中使用到了该参数
	private boolean opCtx = false;
	
	//如果改该字段的值为空是否需要增加到报文中的标志
	private boolean nullAppear = false;
	
	//非空检查
	private boolean nullCheck = false;
	
	//为了实现常量，本类没有从ConstantFormat继承，而是实现了ConstanctFormat的功能，所以新增了如下几个属性
	private boolean fixed = false;	

	private String format = "yyyy-MM-dd hh:mm:ss";
	
	private String value;
	
	
	private String label;
	
	

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public void setFixed(boolean fixed) {
		this.fixed = fixed;
	}
	   
	public boolean isFixed() {
		return fixed;
	}

	public void setFormat(String format) {
		this.format = format;
	}
	
	public String getFormat() {
		return format;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * @roseuid 44FD3F20031C
	 */
	public ESBFieldFormat() {
		super();
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}

	public void setEsbLen(int esbLen) {
		this.esbLen = esbLen;
	}

	public int getEsbLen() {
		return this.esbLen;
	}

	public void setScale(int scale) {
		this.scale = scale;
	}

	public int getScale() {
		return this.scale;
	}
	
	public void setOpCtx(boolean opCtx)
	{
		this.opCtx = opCtx;
	}
	
	public Boolean getOpCtx()
	{
		return this.opCtx;
	}

	public void setNullAppear(boolean nullAppear)
	{
		this.nullAppear = nullAppear;
	}
	
	public Boolean isNullAppear()
	{
		return this.nullAppear;
	}
	
	
	
	
	public boolean isNullCheck() {
		return nullCheck;
	}

	public void setNullCheck(boolean nullCheck) {
		this.nullCheck = nullCheck;
	}

	/**
	 * 打包报文的调用入口。
	 * <p>
	 * 应用系统调用它进行数据报文组包，
	 * EMP将根据报文的设定返回AtomData数据对象。
	 * 
	 * @param context 交易上下文
	 * @return AtomData 打包后的报文
	 * @throws EMPFormatException
	 */
	public Field format(Context context) throws EMPFormatException {
		StringBuffer output = new StringBuffer();
		
		if (this.getFormatElement() == null) {
			try {
				this.format(output, context);
			}
			catch (Exception e)  {
				SFLogger.error(context, "ESBField[" + this + "] format failed!"+e);
				throw new EMPFormatException(e);
			}
		}
		else {
			FormatElement formatElement = this.getFormatElement();
			//支持ESB报文体系中配置定长报文格式
			//目前因境内外币支付系统改造，暂时只支持com.sdb.common.format.KeyedFormat
			if (KeyedFormat.class.isAssignableFrom(formatElement.getClass())) {
				KeyedFormat tmpKeyedFormat = (KeyedFormat)formatElement;
				try {
					tmpKeyedFormat.format(output, context);
				}
				catch (Exception e)  {
					SFLogger.error(context, "ESBField[" + this + "] format failed!"+ e);
					throw new EMPFormatException(e);
				}
			}
		}

		if (output.length() == 0 && !nullAppear)
			return null;

		Field aField = new Field(new FieldAttr(FieldType.getFieldType(type), esbLen, scale));
		if (output != null && output.length() > 0) {
			if (type.equals("string")) {
				aField.setValue(output.toString());
			}
			else if (type.equals("double")) {
				aField.setValue(Double.valueOf(output.toString()));
			}
			else if (type.equals("float")) {
				aField.setValue(Float.valueOf(output.toString()));
			}
			else if (type.equals("int")) {
				aField.setValue(Integer.valueOf(output.toString()));
			}
			else {
				throw new EMPFormatException("暂时不支持的ESB格式定义:" + this);
			}
		}
		
		return aField;
	}
	
	/**
	 * 打包报文的调用入口。
	 * <p>
	 * 应用系统调用它进行数据报文组包，
	 * EMP将根据报文的设定返回AtomData数据对象。
	 * 
	 * @param context 交易上下文
	 * @param dataElement 数据源
	 * @return AtomData 打包后的报文
	 * @throws EMPFormatException
	 */
	public Field format(DataElement dataElement, Context context) throws EMPFormatException {
		StringBuffer output = new StringBuffer();
		if (this.getFormatElement() == null) {
			if (dataElement != null) {
				try {
					this.format(output, dataElement);
				}
				catch (Exception e)  {
					SFLogger.error(context, "ESBField[" + this + "] format failed!"+ e);
					throw new EMPFormatException(e);
				}
			}
		}
		else {
			FormatElement formatElement = this.getFormatElement();
			//支持ESB报文体系中配置定长报文格式
			//目前因境内外币支付系统改造，暂时只支持com.sdb.common.format.KeyedFormat
			if (KeyedFormat.class.isAssignableFrom(formatElement.getClass())) {
				KeyedFormat tmpKeyedFormat = (KeyedFormat)formatElement;
				try {
					tmpKeyedFormat.format(output, dataElement);
				}
				catch (Exception e)  {
					SFLogger.error(context, "ESBField[" + this + "] format failed!"+ e);
					throw new EMPFormatException(e);
				}
			}
		}

		if (output.length() == 0 && !nullAppear)
			return null;
		
		Field aField = new Field(new FieldAttr(FieldType.getFieldType(type), esbLen, scale));
		if (output != null && output.length() > 0) {
			if (type.equals("string")) {
				aField.setValue(output.toString());
			}
			else if (type.equals("double")) {
				aField.setValue(Double.valueOf(output.toString()));
			}
			else if (type.equals("float")) {
				aField.setValue(Float.valueOf(output.toString()));
			}
			else if (type.equals("int")) {
				aField.setValue(Integer.valueOf(output.toString()));
			}
			else {
				throw new EMPFormatException("暂时不支持的ESB格式定义:" + this);
			}
		}
		
		return aField;
	}
		
	/**
	 * 数据解包的调用入口。
	 * <p>
	 * 根据设定格式解包。
	 * 
	 * @param context 交易上下文
	 * @param atomData 待解报文
	 * @throws EMPFormatException
	 */
	public void unformat(AtomData atomData, Context context) throws EMPFormatException {
		DataElement aDataElement = null;
		if (atomData == null)
			return;

		EMPLog.log("PAESB", EMPLog.DEBUG, 0, "prepare to unformat <ESBField id=\"" + this.getName() + "\" dataName=\"" + this.getDataName() + "\">");
		if (this.getDataName() != null) {
			try {
				String aDataElementName = SFUtil.getContextValueInAction(context, this.getDataName());
				if (aDataElementName != null && aDataElementName.length() > 0) {
					if (this.isAppend()) {
						//context.getDataElement().setAppend(this.isAppend());这个改变了context的属性，废掉
						if (!context.containsKey(aDataElementName)){
							DataElement theAppendDataElement = (DataElement) (this.getAppendClass() == null? DataField.class :Class.forName(this.getAppendClass())).newInstance();
							theAppendDataElement.setName(aDataElementName);
							theAppendDataElement.setAppend(this.isAppend());
							context.addDataElement(theAppendDataElement);
						}
					}
					aDataElement = context.getDataElement(aDataElementName, this.getAppendClass() == null? DataField.class :Class.forName(this.getAppendClass()));
				}
				if (aDataElement != null) {
					EMPLog.log("PAESB", EMPLog.DEBUG, 0, "unformat ESBFieldFormat find FIELD [" + aDataElementName + "] in Context");
				}
				else {
					EMPLog.log("PAESB", EMPLog.WARNING, 0, "unformat ESBFieldFormat can't find FIELD [" + aDataElementName + "] in Context, , Continue with NULL");					
				}
			}
			catch (Exception e) {
				EMPLog.log("PAESB", EMPLog.ERROR, 0, "unformat <ESBField id=\"" + this.getName() + "\" dataName=\"" + this.getDataName() + "\"> occur error!");
				throw new EMPFormatException(e);
			}
		}
		
		if (CompositeData.class.isAssignableFrom(atomData.getClass())) {
			if (this.getName() != null) {
				atomData = ((CompositeData)atomData).getField(this.getName());
				EMPLog.log("PAESB", EMPLog.DEBUG, 0, "unformat <ESBField id=\"" + this.getName() + "\" dataName=\"" + this.getDataName() + "\">" + " find Field [" + this.getName() + "] in parent CD");
			}
			else {
				EMPLog.log("PAESB", EMPLog.DEBUG, 0, "unformat <ESBField id=\"" + this.getName() + "\" dataName=\"" + this.getDataName() + "\">" + " No id, Using Parent CD");
			}
		}
		
		Field aField = ((Field)atomData);
		if (aField == null || aDataElement == null)
			return;
		
		String sValue = null;
		BigDecimal aBigDecimal = null;
		if (type.equals("string")) {
			sValue = aField.strValue();
		}
		else if (type.equals("double")) {
			aBigDecimal = new BigDecimal(Double.toString(aField.doubleValue()));
			sValue = aBigDecimal.toPlainString();
		}
		else if (type.equals("float")) {
			aBigDecimal = new BigDecimal(Float.toString(aField.floatValue()));
			sValue = aBigDecimal.toPlainString();
		}
		else if (type.equals("int")) {
			sValue = Integer.toString(aField.intValue());
		}
		else {
			throw new EMPFormatException("暂时不支持的ESB格式定义:" + this);
		}
		
		try {
//			this.unformat(sValue, 0, context);
			this.unformat(sValue, (DataField)aDataElement);
		}
		catch (Exception e)  {
			SFLogger.error(context, "ESBField[" + this + "] unformat failed!"+ e);
			throw new EMPFormatException(e);
		}
	}
	
	/**
	 * 数据解包的调用入口。
	 * <p>
	 * 根据设定格式解包。
	 * 
	 * @param context 交易上下文
	 * @param dataElement 解包后存放对象
	 * @param atomData 待解报文
	 * @throws EMPFormatException
	 */
	public void unformat(DataElement dataElement, AtomData atomData, Context context) throws EMPFormatException {
		DataElement aDataElement = null;
		if (atomData == null)
			return;

		EMPLog.log("PAESB", EMPLog.DEBUG, 0, "prepare to unformat <ESBField id=\"" + this.getName() + "\" dataName=\"" + this.getDataName() + "\">");
		if (CompositeData.class.isAssignableFrom(atomData.getClass())) {
			if (this.getDataName() != null && dataElement != null) {
				try {
					String aDataElementName = SFUtil.getContextValueInAction(context, this.getDataName());
					if (aDataElementName != null && aDataElementName.length() > 0) {
						if (this.isAppend()) {
							//((KeyedCollection)dataElement).setAppend(this.isAppend());不改变父亲的DataElement的属性
							if (!(((KeyedCollection)dataElement).containsKey(aDataElementName))){
								DataElement theAppendDataElement = (DataElement) (this.getAppendClass() == null? DataField.class :Class.forName(this.getAppendClass())).newInstance();
								theAppendDataElement.setName(aDataElementName);
								theAppendDataElement.setAppend(this.isAppend());
								((KeyedCollection)dataElement).addDataElement(theAppendDataElement);
							}
						}
						aDataElement = ((KeyedCollection)dataElement).getDataElement(aDataElementName, this.getAppendClass() == null? DataField.class :Class.forName(this.getAppendClass()));
					}
					if (aDataElement != null) {
						EMPLog.log("PAESB", EMPLog.DEBUG, 0, "unformat ESBFieldFormat find FIELD [" + aDataElementName + "] in KCOLL");
					}
					else {
						EMPLog.log("PAESB", EMPLog.WARNING, 0, "unformat ESBFieldFormat can't find FIELD [" + aDataElementName + "] in KCOLL, , Continue with NULL");					
					}
				}
				catch (Exception e) {
					EMPLog.log("PAESB", EMPLog.ERROR, 0, "unformat <ESBField id=\"" + this.getName() + "\" dataName=\"" + this.getDataName() + "\"> occur error!");
					throw new EMPFormatException(e);
				}
			}

			if (this.getName() != null) {
				atomData = ((CompositeData)atomData).getField(this.getName());
				EMPLog.log("PAESB", EMPLog.DEBUG, 0, "unformat <ESBField id=\"" + this.getName() + "\" dataName=\"" + this.getDataName() + "\">" + " find Field [" + this.getName() + "] in parent CD");
			}
			else {
				EMPLog.log("PAESB", EMPLog.DEBUG, 0, "unformat <ESBField id=\"" + this.getName() + "\" dataName=\"" + this.getDataName() + "\">" + " No id, Using Parent CD");
			}
		}
		
		String sValue = null;
		Field aField = ((Field)atomData);
		if (aField == null || aDataElement == null){
			if(this.value!=null){
				sValue = this.value;
			}else{
				
				if(this.nullCheck && SFUtil.isEmpty(sValue)){
					throw new EMPFormatException("字段["+this.getLabel()+"]不允许为空");
				}
				return;
			}
			
		}else{
			BigDecimal aBigDecimal = null;
			String msgType=aField.getFieldType().getName();
			if (type.equals("string")) {
				//兼容实际报文类型与报文配置不一致（兼容同一接口不同渠道发送报文的类型不一至）
				if(msgType==null||type.equals(msgType)){
					sValue = aField.strValue();
				}else if("double".equals(msgType)){
					aBigDecimal = new BigDecimal(Double.toString(aField.doubleValue()));
					sValue = aBigDecimal.toPlainString();
				}else if("float".equals(msgType)){
					aBigDecimal = new BigDecimal(Float.toString(aField.floatValue()));
					sValue = aBigDecimal.toPlainString();
				}else if("int".equals(msgType)){
					sValue = Integer.toString(aField.intValue());
				}else{
					throw new EMPFormatException("暂时不支持的ESB格式定义:" + this);
				}
			}else if (type.equals("double")) {
				aBigDecimal = new BigDecimal(Double.toString(aField.doubleValue()));
				sValue = aBigDecimal.toPlainString();
			}else if (type.equals("float")) {
				aBigDecimal = new BigDecimal(Float.toString(aField.floatValue()));
				sValue = aBigDecimal.toPlainString();
			}else if (type.equals("int")) {
				sValue = Integer.toString(aField.intValue());
			}else {
				throw new EMPFormatException("暂时不支持的ESB格式定义:" + this);
			}
		}
		
		if(this.nullCheck && SFUtil.isEmpty(sValue)){
			throw new EMPFormatException("字段["+this.getLabel()+"]不允许为空");
		}
		
		try {
			this.unformat(sValue, (DataField)aDataElement);
		}
		catch (Exception e)  {
			SFLogger.error(context, "ESBField[" + this + "] unformat failed!"+ e);
			throw new EMPFormatException(e);
		}
	}

	public String toString(int tabCount) {
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < tabCount; i++)
			buf.append("\t");

		buf.append("<ESBField dataName=\"");
		buf.append(getDataName());
		buf.append("\" name=\"").append(this.getName());
		buf.append("\" opCtx=\"").append(this.getOpCtx());
		buf.append("\" len=\"").append(this.getLen()).append("\" padChar=\"").append(this.getPadChar());
		buf.append("\" aligment=\"").append(this.getAligment());
		buf.append("\" type=\"").append(type).append("\" esbLen=\"").append(esbLen).append("\" scale=\"").append(scale);
		buf.append("\"/>\n");

		for (int i = 0; i < this.getDecorators().size(); i++) {
			FormatElement fmt = (FormatElement) getDecorators().get(i);
			buf.append(fmt.toString(tabCount));
			buf.append("\n");
		}

		return buf.toString();
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
	/**
	 * 将数据域转为常量（时间）字符串
	 * 
	 * @param DataField
	 *            dataField //数据域
	 * @returns Object //格式化后的字符串
	 */
	public Object format(DataField dataField) throws EMPFormatException {
		if (fixed) {
			if (this.isConstant()) {
				String value = this.value;
				if (value == null)
					value = getDateTime();
	
				DataField newField = new DataField();
				newField.setValue(value);
				Object fixedLenValue;
				try {
					fixedLenValue = super.format(newField);
				} catch (EMPFormatException e) {
//					 SFLogger.error(context,
//								"ConstantFormat format failed! Fixed length format error!"+ e);
					 throw new EMPFormatException(
						"ConstantFormat format failed! Fixed length format error!", e);
				}
				return fixedLenValue;			
				
			} else {
				return super.format(dataField);
			}
		}
		else {
			if (this.isConstant()) {
				String tmpValue = this.value;
				if (tmpValue == null)
					tmpValue = getDateTime();
				
				return tmpValue;
			}
			else {
				if (dataField == null)
					return "";
				Object tmpValue = dataField.getValue();
				if (tmpValue == null)
					return "";
				else
					return tmpValue;
			}
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
	 * 字符串格式报文解包入口。
	 * 
	 * @param src 待解报文(字符串)
	 * @param offset 起始位置
	 * @param element 数据模型(kColl)
	 * @return 解包长度
	 * @throws Exception
	 */
	public int unformat(String src, int offset, DataElement element) throws Exception {
		String dataName = getDataName(src);
		DataField field = null;
		if (!isConstant()){
			field = (DataField) ((KeyedCollection) element).getDataElement(dataName, DataField.class);
			if (field == null) { 
				EMPLog.log("PAESB", EMPLog.ERROR, 0, "FormatField unformat failed! Can't find DataField [" + dataName + "]");
				throw new EMPFormatException("FormatField unformat failed! Can't find DataField [" + dataName + "]");
			}
		}
			
		this.unformat(src, field);
		
		return src.length();
	}
	
	
	public void putfieldNameToContext(Context context,String fieldDataName,String val)throws Exception{
		
		
		if(context.containsKey(fieldDataName)){
			context.setDataValue(fieldDataName,val);
		}else{
			String[] fieldDataArray = fieldDataName.split("\\.");
			if(fieldDataArray.length>1){
				
				if(context.containsKey(fieldDataArray[0])){
					KeyedCollection dColl = (KeyedCollection)context.getDataElement(fieldDataArray[0]);
					dColl.addDataField(fieldDataArray[1], val);
				}else{
					KeyedCollection dColl = new KeyedCollection(fieldDataArray[0]);
					dColl.addDataField(fieldDataArray[1], val);
					context.addDataElement(dColl);
				}
			}else{
				context.addDataField(fieldDataName,val);
			}
			
			
		}
	
		
	}
	
	public void addFormatToContext(Context context) throws Exception{
		DataElement aDataElement = null;

		if (this.getDataName() != null) {
			try {
				String aDataElementName = SFUtil.getContextValueInAction(context, this.getDataName());
				if (aDataElementName != null && aDataElementName.length() > 0) {
					if (this.isAppend()) {
						//context.getDataElement().setAppend(this.isAppend());这个改变了context的属性，废掉
						if (!context.containsKey(aDataElementName)){
							DataElement theAppendDataElement = (DataElement) (this.getAppendClass() == null? DataField.class :Class.forName(this.getAppendClass())).newInstance();
							theAppendDataElement.setName(aDataElementName);
							theAppendDataElement.setAppend(this.isAppend());
							context.addDataElement(theAppendDataElement);
						}
					}
					aDataElement = context.getDataElement(aDataElementName, this.getAppendClass() == null? DataField.class :Class.forName(this.getAppendClass()));
				}
				if (aDataElement != null) {
					EMPLog.log("PAESB", EMPLog.DEBUG, 0, "unformat ESBFieldFormat find FIELD [" + aDataElementName + "] in Context");
				}
				else {
					EMPLog.log("PAESB", EMPLog.WARNING, 0, "unformat ESBFieldFormat can't find FIELD [" + aDataElementName + "] in Context, , Continue with NULL");					
				}
			}
			catch (Exception e) {
				EMPLog.log("PAESB", EMPLog.ERROR, 0, "unformat <ESBField id=\"" + this.getName() + "\" dataName=\"" + this.getDataName() + "\"> occur error!");
				throw new EMPFormatException(e);
			}
		}
		
		
	}
	
	
	public void addFormatToContext(DataElement dataElement,Context context) throws Exception{
		DataElement aDataElement = null;


		if (this.getDataName() != null && dataElement != null) {
			try {
				String aDataElementName = SFUtil.getContextValueInAction(context, this.getDataName());
				if (aDataElementName != null && aDataElementName.length() > 0) {
					if (this.isAppend()) {
						//((KeyedCollection)dataElement).setAppend(this.isAppend());不改变父亲的DataElement的属性
						if (!(((KeyedCollection)dataElement).containsKey(aDataElementName))){
							DataElement theAppendDataElement = (DataElement) (this.getAppendClass() == null? DataField.class :Class.forName(this.getAppendClass())).newInstance();
							theAppendDataElement.setName(aDataElementName);
							theAppendDataElement.setAppend(this.isAppend());
							((KeyedCollection)dataElement).addDataElement(theAppendDataElement);
						}
					}
					aDataElement = ((KeyedCollection)dataElement).getDataElement(aDataElementName, this.getAppendClass() == null? DataField.class :Class.forName(this.getAppendClass()));
				}
				if (aDataElement != null) {
					EMPLog.log("PAESB", EMPLog.DEBUG, 0, "unformat ESBFieldFormat find FIELD [" + aDataElementName + "] in KCOLL");
				}
				else {
					EMPLog.log("PAESB", EMPLog.WARNING, 0, "unformat ESBFieldFormat can't find FIELD [" + aDataElementName + "] in KCOLL, , Continue with NULL");					
				}
			}
			catch (Exception e) {
				EMPLog.log("PAESB", EMPLog.ERROR, 0, "unformat <ESBField id=\"" + this.getName() + "\" dataName=\"" + this.getDataName() + "\"> occur error!");
				throw new EMPFormatException(e);
			}
		}

	
	}
	
	
	
	
}
