package core.communication.format.paesb;

import java.math.BigDecimal;

import com.dc.eai.data.AtomData;
import com.dc.eai.data.CompositeData;
import com.dc.eai.data.Field;
import com.dc.eai.data.FieldAttr;
import com.ecc.emp.core.Context;
import com.ecc.emp.core.EMPConstance;
import com.ecc.emp.data.DataElement;
import com.ecc.emp.data.DataField;
import com.ecc.emp.data.KeyedCollection;
import com.ecc.emp.format.EMPFormatException;
import com.ecc.emp.log.EMPLog;
import common.util.SFUtil;

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
public class ESBFieldFloatFormat extends ESBFieldFormat{
		
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

		//SFLogger.debug(context, "prepare to unformat <ESBField id=\"" + this.getName() + "\" dataName=\"" + this.getDataName() + "\">");
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
					//SFLogger.debug(context, "unformat ESBFieldFormat find FIELD [" + aDataElementName + "] in Context");
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
				//SFLogger.debug(context, "unformat <ESBField id=\"" + this.getName() + "\" dataName=\"" + this.getDataName() + "\">" + " find Field [" + this.getName() + "] in parent CD");
			}
			else {
				//SFLogger.debug(context, "unformat <ESBField id=\"" + this.getName() + "\" dataName=\"" + this.getDataName() + "\">" + " No id, Using Parent CD");
			}
		}
		
		Field aField = ((Field)atomData);
		if (aField == null || aDataElement == null)
			return;
		
		FieldAttr aFieldAttr  = aField.getAttr();
		String sValue = null;
		BigDecimal aBigDecimal = null;
		if (this.getType().equals("string")) {
			sValue = aField.strValue();
		}
		else if (this.getType().equals("double")) {
			aBigDecimal = new BigDecimal(Double.toString(aField.doubleValue()));
			sValue = String.format("%." + aFieldAttr.getScale() +"f", aBigDecimal);
		}
		else if (this.getType().equals("float")) {
			aBigDecimal = new BigDecimal(Float.toString(aField.floatValue()));
			sValue = String.format("%." + aFieldAttr.getScale() +"f", aBigDecimal);
		}
		else if (this.getType().equals("int")) {
			sValue = Integer.toString(aField.intValue());
		}
		else {
			throw new EMPFormatException("暂时不支持的ESB格式定义:" + this);
		}
		
		try {
			this.unformat(sValue, (DataField)aDataElement);
		}
		catch (Exception e)  {
			EMPLog.log(EMPConstance.EMP_FORMAT,	EMPLog.ERROR, 0, "ESBField[" + this + "] unformat failed!", e);
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

		//SFLogger.debug(context, "prepare to unformat <ESBField id=\"" + this.getName() + "\" dataName=\"" + this.getDataName() + "\">");
		if (CompositeData.class.isAssignableFrom(atomData.getClass())) {
			if (this.getDataName() != null && dataElement != null) {
				try {
					String aDataElementName = SFUtil.getContextValueInAction(context, this.getDataName());
					if (aDataElementName != null && aDataElementName.length() > 0) {
						if (this.isAppend()) {
							//((KeyedCollection)dataElement).setAppend(this.isAppend());这个改变了context的属性，废掉
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
						//SFLogger.debug(context, "unformat ESBFieldFormat find FIELD [" + aDataElementName + "] in KCOLL");
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
				//SFLogger.debug(context, "unformat <ESBField id=\"" + this.getName() + "\" dataName=\"" + this.getDataName() + "\">" + " find Field [" + this.getName() + "] in parent CD");
			}
			else {
				//SFLogger.debug(context, "unformat <ESBField id=\"" + this.getName() + "\" dataName=\"" + this.getDataName() + "\">" + " No id, Using Parent CD");
			}
		}
		Field aField = ((Field)atomData);
		if (aField == null || aDataElement == null)
			return;

		FieldAttr aFieldAttr  = aField.getAttr();
		String sValue = null;
		BigDecimal aBigDecimal = null;
		if (this.getType().equals("string")) {
			sValue = aField.strValue();
		}
		else if (this.getType().equals("double")) {
			aBigDecimal = new BigDecimal(Double.toString(aField.doubleValue()));
			sValue = String.format("%." + aFieldAttr.getScale() +"f", aBigDecimal);
		}
		else if (this.getType().equals("float")) {
			aBigDecimal = new BigDecimal(Float.toString(aField.floatValue()));
			sValue = String.format("%." + aFieldAttr.getScale() +"f", aBigDecimal);
		}
		else if (this.getType().equals("int")) {
			sValue = Integer.toString(aField.intValue());
		}
		else {
			throw new EMPFormatException("暂时不支持的ESB格式定义:" + this);
		}
		
		try {
			this.unformat(sValue, (DataField)aDataElement);
		}
		catch (Exception e)  {
			EMPLog.log(EMPConstance.EMP_FORMAT,	EMPLog.ERROR, 0, "ESBField[" + this + "] unformat failed!", e);
			throw new EMPFormatException(e);
		}
	}
}
