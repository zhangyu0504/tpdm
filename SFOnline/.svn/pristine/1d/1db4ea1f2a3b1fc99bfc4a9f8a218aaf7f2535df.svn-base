package core.communication.format.paesb;

import com.dc.eai.data.Array;
import com.dc.eai.data.AtomData;
import com.dc.eai.data.CompositeData;
import com.dc.eai.data.Field;
import com.ecc.emp.core.Context;
import com.ecc.emp.data.DataElement;
import com.ecc.emp.data.IndexedCollection;
import com.ecc.emp.data.KeyedCollection;
import com.ecc.emp.format.EMPFormatException;
import com.ecc.emp.format.FormatElement;

import common.util.SFUtil;

import core.communication.format.IndexedFormat;
import core.log.SFLogger;

/**
 * 数据结构(kColl)类型数据的格式化处理类。
 * 
 * @author zhongmc
 * @version 2.1
 * @since 1.0 2006-12-4
 * @lastmodified 2008-7-2
 */
public class ESBArrayFormat extends IndexedFormat{

	private boolean opCtx = false;

	//如果改该字段的值为空是否需要增加到报文中的标志
	private boolean nullAppear = false;
	
	public void setNullAppear(boolean nullAppear)
	{
		this.nullAppear = nullAppear;
	}
	
	public Boolean isNullAppear()
	{
		return this.nullAppear;
	}
	
	//ICOLL反向循环组报文标志
	private boolean reverseFlag = false;
	
	public void setReverseFlag(boolean reverseFlag)
	{
		this.reverseFlag = reverseFlag;
	}
	
	public Boolean getReverseFlag()
	{
		return this.reverseFlag;
	}

	public ESBArrayFormat() {
		super();
	}

	public void setOpCtx(boolean opCtx)
	{
		this.opCtx = opCtx;
	}
	
	public Boolean getOpCtx()
	{
		return this.opCtx;
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
	public Array format(Context context) throws EMPFormatException {
		
		Array aArray = new Array();
		DataElement aDataElement = null;
		if (this.getDataName() != null) {
			try {
				String aDataElementName = SFUtil.getContextValueInAction(context, this.getDataName());
				if (aDataElementName != null && aDataElementName.length() > 0)
					aDataElement = context.getDataElement(aDataElementName);
				else
					SFLogger.debug(context, "format ESBArray, can't find dataElement [" + this.getDataName() + "][" + aDataElement + "] in context");
			

			
			}
			catch (Exception e) {
				throw new EMPFormatException(e);
			}
		}

		IndexedCollection iColl = null;
		KeyedCollection kColl = null;
		if (aDataElement != null) {
			if (IndexedCollection.class.isAssignableFrom(aDataElement.getClass())) {
				iColl = (IndexedCollection) aDataElement;
			}
			else if (KeyedCollection.class.isAssignableFrom(aDataElement.getClass())) {
				kColl = (KeyedCollection) aDataElement;
			}
		}

		if (iColl != null)
		{
			FormatElement formatElement = this.getFormatElement();
			for (int i = 0; i < iColl.size(); i++) {
				DataElement element = null ;
				if (reverseFlag) {
					element = iColl.getElementAt(iColl.size() - i - 1);
				} else {
					element = iColl.getElementAt(i);
				}
				
				if (ESBFieldFormat.class.isAssignableFrom(formatElement.getClass())) {
					ESBFieldFormat aESBFieldFormat = (ESBFieldFormat)formatElement;
					if (aESBFieldFormat.getOpCtx()) {
						Field tmpField = aESBFieldFormat.format(context);
						if (tmpField != null)
							aArray.addField(aESBFieldFormat.format(context));
					}
					else {
						Field tmpField = aESBFieldFormat.format(element, context);
						if (tmpField != null)
							aArray.addField(tmpField);
					}
				}
				else if (ESBCDFormat.class.isAssignableFrom(formatElement.getClass())) {
					ESBCDFormat aESBCDFormat = (ESBCDFormat)formatElement;
					if (aESBCDFormat.getOpCtx()) {
						CompositeData tmpCD = aESBCDFormat.format(context);
						if (tmpCD != null)
							aArray.addStruct(tmpCD);
					}
					else {
						CompositeData tmpCD = aESBCDFormat.format(element, context);
						if (tmpCD != null)
							aArray.addStruct(tmpCD);
					}
				}
			}
		}
		
		if (kColl != null)
		{
			FormatElement formatElement = this.getFormatElement();
			DataElement element = kColl;
			if (ESBFieldFormat.class.isAssignableFrom(formatElement.getClass())) {
				ESBFieldFormat aESBFieldFormat = (ESBFieldFormat)formatElement;
				if (aESBFieldFormat.getOpCtx()) {
					Field tmpField = aESBFieldFormat.format(context);
					if (tmpField != null)
						aArray.addField(aESBFieldFormat.format(context));
				}
				else {
					Field tmpField = aESBFieldFormat.format(element, context);
					if (tmpField != null)
						aArray.addField(tmpField);
				}
			}
			else if (ESBCDFormat.class.isAssignableFrom(formatElement.getClass())) {
				ESBCDFormat aESBCDFormat = (ESBCDFormat)formatElement;
				if (aESBCDFormat.getOpCtx()) {
					CompositeData tmpCD = aESBCDFormat.format(context);
					if (tmpCD != null)
						aArray.addStruct(tmpCD);
				}
				else {
					CompositeData tmpCD = aESBCDFormat.format(element, context);
					if (tmpCD != null)
						aArray.addStruct(tmpCD);
				}
			}
		}
		
		if (aArray.size() == 0 && !this.nullAppear)
			return null;
		else
			return aArray;
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
	public Array format(DataElement dataElement, Context context) throws EMPFormatException {
		IndexedCollection iColl = null;
		Array aArray = new Array();

		KeyedCollection kColl = (KeyedCollection) dataElement;
		if (kColl != null) {
			try {
				iColl = (IndexedCollection) kColl.getDataElement(getDataName());
			}
			catch (Exception e) {
				throw new EMPFormatException(e);
			}
		}

		if (iColl != null)
		{
			FormatElement formatElement = this.getFormatElement();
			for (int i = 0; i < iColl.size(); i++) {
				DataElement element = null ;
				if (reverseFlag) {
					element = iColl.getElementAt(iColl.size() - i - 1);
				} else {
					element = iColl.getElementAt(i);
				}
				
				if (ESBFieldFormat.class.isAssignableFrom(formatElement.getClass())) {
					ESBFieldFormat aESBFieldFormat = (ESBFieldFormat)formatElement;
					if (aESBFieldFormat.getOpCtx()) {
						Field tmpField = aESBFieldFormat.format(context);
						if (tmpField != null)
							aArray.addField(tmpField);
					}
					else {
						Field tmpField = aESBFieldFormat.format(element, context);
						if (tmpField != null)
							aArray.addField(tmpField);
					}
				}
				else if (ESBCDFormat.class.isAssignableFrom(formatElement.getClass())) {
					ESBCDFormat aESBCDFormat = (ESBCDFormat)formatElement;
					if (aESBCDFormat.getOpCtx()) {
						aArray.addStruct(aESBCDFormat.format(context));
					}
					else {
						aArray.addStruct(aESBCDFormat.format(element, context));
					}
				} 
			}
		}

		if (aArray.size() == 0 && !this.nullAppear)
			return null;
		else
			return aArray;
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
	public void unformat(AtomData atomData, Context context) throws Exception {
		DataElement aDataElement = null;
		if (atomData == null)
			return;
		
		//SFLogger.debug(context, "prepare to unformat <ESBArray id=\"" + this.getName() + "\" dataName=\"" + this.getDataName() + "\">");
		if (this.getDataName() != null) {
			try {
				String aDataElementName = (String)SFUtil.getContextValueInAction(context, this.getDataName());
				if (aDataElementName != null && aDataElementName.length() > 0) {
					if (this.isAppend()) {
						//context.getDataElement().setAppend(this.isAppend());这个改变了context的属性，废掉
						if (!context.containsKey(aDataElementName)){
							DataElement theAppendDataElement = (DataElement) (this.getAppendClass() == null? IndexedCollection.class :Class.forName(this.getAppendClass())).newInstance();
							theAppendDataElement.setName(aDataElementName);
							theAppendDataElement.setAppend(this.isAppend());
							context.addDataElement(theAppendDataElement);
						}
					}
					aDataElement = context.getDataElement(aDataElementName, this.getAppendClass() == null? IndexedCollection.class :Class.forName(this.getAppendClass()));
				}
				if (aDataElement != null) {
					//SFLogger.debug(context, "unformat ESBArrayFormat find ICOLL [" + aDataElementName + "] in context");
				}
				else {
					//SFLogger.debug(context, "unformat ESBArrayFormat can't find ICOLL [" + aDataElementName + "] in context, , Continue with NULL");					
				}
			}
			catch (Exception e) {
				SFLogger.error(context, "unformat <ESBArray id=\"" + this.getName() + "\" dataName=\"" + this.getDataName() + "\"> occur error!");
				throw new EMPFormatException(e);
			}
		}
		if (this.getName() != null) {
			atomData = ((CompositeData)atomData).getArray(this.getName());
			//SFLogger.debug(context, "unformat <ESBArray id=\"" + this.getName() + "\" dataName=\"" + this.getDataName() + "\">" + " find Array [" + this.getName() + "] in parent CD");
		}
		else {
			//SFLogger.debug(context, "unformat <ESBArray id=\"" + this.getName() + "\" dataName=\"" + this.getDataName() + "\">" + " No id, Using Parent CD");
		}
		FormatElement formatElement = this.getFormatElement();

		IndexedCollection iColl = (IndexedCollection) aDataElement;
		DataElement aRecordTemplate = null;
		if (iColl != null && !this.isClear()) {
			iColl.clear();
			aRecordTemplate = iColl.getDataElement();
		}

		for (int i = 0; i < ((Array)atomData).size(); i++) {
			AtomData aAtomData = ((Array)atomData).getObject(i);
			
			if (ESBFieldFormat.class.isAssignableFrom(formatElement.getClass())) {
				ESBFieldFormat aESBFieldFormat = (ESBFieldFormat)formatElement;
				if (aESBFieldFormat.getOpCtx()) {
					aESBFieldFormat.unformat(aAtomData, context);
				}
				else {
					DataElement aRecord = null;
					if (aRecordTemplate != null) {
						aRecord = (DataElement)aRecordTemplate.clone();
					}
					aESBFieldFormat.unformat(aRecord, aAtomData, context);
					if (aRecord != null)
						iColl.addDataElement(aRecord);
				}
			}
			else if (ESBCDFormat.class.isAssignableFrom(formatElement.getClass())) {
				ESBCDFormat aESBCDFormat = (ESBCDFormat)formatElement;
				if (aESBCDFormat.getOpCtx()) {
					aESBCDFormat.unFormat(aAtomData, context);
				}
				else {
					DataElement aRecord = null;
					if (aRecordTemplate != null)
						aRecord = (DataElement)aRecordTemplate.clone();
					aESBCDFormat.unformat(aRecord, aAtomData, context);
					if (aRecord != null)
						iColl.addDataElement(aRecord);
				}
			} 
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
	public void unformat(DataElement dataElement, AtomData atomData, Context context) throws Exception {
		DataElement aDataElement = null;
		if (atomData == null || dataElement == null)
			return;
		
		//SFLogger.debug(context, "prepare to unformat <ESBArray id=\"" + this.getName() + "\" dataName=\"" + this.getDataName() + "\">");
		if (this.getDataName() != null) {
			try {
				String aDataElementName = (String)SFUtil.getContextValueInAction(context, this.getDataName());
				if (aDataElementName != null && aDataElementName.length() > 0) {
					if (this.isAppend()) {
						//dataElement.setAppend(this.isAppend());不改变父亲的DataElement的属性
						if (!(((KeyedCollection)dataElement).containsKey(aDataElementName))){
							DataElement theAppendDataElement = (DataElement) (this.getAppendClass() == null? IndexedCollection.class :Class.forName(this.getAppendClass())).newInstance();
							theAppendDataElement.setName(aDataElementName);
							theAppendDataElement.setAppend(this.isAppend());
							((KeyedCollection)dataElement).addDataElement(theAppendDataElement);
						}
					}
					aDataElement = ((KeyedCollection)dataElement).getDataElement(aDataElementName, this.getAppendClass() == null? IndexedCollection.class :Class.forName(this.getAppendClass()));
				}
				if (aDataElement != null) {
					dataElement = aDataElement;
					//SFLogger.debug(context, "unformat ESBArrayFormat find ICOLL [" + aDataElementName + "] in Parent KCOLL");
				}
				else {
					//SFLogger.debug(context, "unformat ESBArrayFormat can't find ICOLL [" + aDataElementName + "] in Parent KCOLL, , Continue with NULL");					
				}
			}
			catch (Exception e) {
				SFLogger.error(context, "unformat <ESBArray id=\"" + this.getName() + "\" dataName=\"" + this.getDataName() + "\"> occur error!");
				throw new EMPFormatException(e);
			}
		}
		if (this.getName() != null) {
			atomData = ((CompositeData)atomData).getArray(this.getName());
			//SFLogger.debug(context, "unformat <ESBArray id=\"" + this.getName() + "\" dataName=\"" + this.getDataName() + "\">" + " find ARRAY [" + this.getName() + "] in parent CD");
		}
		else {
			//SFLogger.debug(context, "unformat <ESBArray id=\"" + this.getName() + "\" dataName=\"" + this.getDataName() + "\">" + " No id, Using Parent CD");
		}
		FormatElement formatElement = this.getFormatElement();

		IndexedCollection iColl = (IndexedCollection) dataElement;
		if (!this.isAppend())
			iColl.clear();

		DataElement aRecordTemplate = iColl.getDataElement();

		for (int i = 0; i < ((Array)atomData).size(); i++) {
			AtomData aAtomData = ((Array)atomData).getObject(i);

			if (ESBFieldFormat.class.isAssignableFrom(formatElement.getClass())) {
				ESBFieldFormat aESBFieldFormat = (ESBFieldFormat)formatElement;
				if (aESBFieldFormat.getOpCtx()) {
					aESBFieldFormat.unformat(aAtomData, context);
				}
				else {
					DataElement aRecord = (DataElement)aRecordTemplate.clone();
					aESBFieldFormat.unformat(aRecord, aAtomData, context);
					iColl.addDataElement(aRecord);
				}
			}
			else if (ESBCDFormat.class.isAssignableFrom(formatElement.getClass())) {
				ESBCDFormat aESBCDFormat = (ESBCDFormat)formatElement;
				if (aESBCDFormat.getOpCtx()) {
					aESBCDFormat.unFormat(aAtomData, context);
				}
				else {
					DataElement aRecord = (DataElement)aRecordTemplate.clone();
					aESBCDFormat.unformat(aRecord, aAtomData, context);
					iColl.addDataElement(aRecord);
				}
			} 
		}
	}

		
		/**
	 * 获得该类的字符串表现。
	 * 
	 * @param tabCount 缩进量
	 * @return 该类的字符串表现
	 */
	public String toString(int tabCount) {
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < tabCount; i++)
			buf.append("\t");

		buf.append("<ESBArray dataName=\"");
		buf.append(getDataName());
		buf.append(" name=\"").append(this.getName());
		buf.append("\" append=\"");
		buf.append(String.valueOf(this.isAppend()));
		buf.append("\" opCtx=\"");
		buf.append(String.valueOf(this.getOpCtx()));
		buf.append("\">\n");

		buf.append(getFormatElement().toString(tabCount + 1));

		buf.append("\n");
		for (int i = 0; i < tabCount; i++)
			buf.append("\t");
		buf.append("</ESBArray>\n");

		for (int i = 0; i < this.getDecorators().size(); i++) {
			FormatElement fmt = (FormatElement) getDecorators().get(i);
			buf.append(fmt.toString(tabCount));
			buf.append("\n");
		}

		return buf.toString();
	}
	
	
	
	
	public void addFormatToContext(Context context) throws Exception {
		DataElement aDataElement = null;
		if (this.getDataName() != null) {
			try {
				String aDataElementName = (String)SFUtil.getContextValueInAction(context, this.getDataName());
				if (aDataElementName != null && aDataElementName.length() > 0) {
					if (this.isAppend()) {
						//context.getDataElement().setAppend(this.isAppend());这个改变了context的属性，废掉
						if (!context.containsKey(aDataElementName)){
							DataElement theAppendDataElement = (DataElement) (this.getAppendClass() == null? IndexedCollection.class :Class.forName(this.getAppendClass())).newInstance();
							theAppendDataElement.setName(aDataElementName);
							theAppendDataElement.setAppend(this.isAppend());
							context.addDataElement(theAppendDataElement);
						}
					}
					aDataElement = context.getDataElement(aDataElementName, this.getAppendClass() == null? IndexedCollection.class :Class.forName(this.getAppendClass()));
				}
				if (aDataElement != null) {
					//SFLogger.debug(context, "unformat ESBArrayFormat find ICOLL [" + aDataElementName + "] in context");
				}
				else {
					//SFLogger.debug(context, "unformat ESBArrayFormat can't find ICOLL [" + aDataElementName + "] in context, , Continue with NULL");					
				}
			}
			catch (Exception e) {
				SFLogger.error(context, "unformat <ESBArray id=\"" + this.getName() + "\" dataName=\"" + this.getDataName() + "\"> occur error!");
				throw new EMPFormatException(e);
			}
		}
		FormatElement formatElement = this.getFormatElement();

		IndexedCollection iColl = (IndexedCollection) aDataElement;
		DataElement aRecordTemplate = null;
		if (iColl != null && !this.isClear()) {
			iColl.clear();
			aRecordTemplate = iColl.getDataElement();
		}


		
		if (ESBFieldFormat.class.isAssignableFrom(formatElement.getClass())) {
			ESBFieldFormat aESBFieldFormat = (ESBFieldFormat)formatElement;
			if (aESBFieldFormat.getOpCtx()) {
				aESBFieldFormat.addFormatToContext(context);
			}
			else {
				DataElement aRecord = null;
				if (aRecordTemplate != null) {
					aRecord = (DataElement)aRecordTemplate.clone();
				}
				aESBFieldFormat.addFormatToContext(aRecord, context);
				if (aRecord != null)
					iColl.addDataElement(aRecord);
			}
		}
		else if (ESBCDFormat.class.isAssignableFrom(formatElement.getClass())) {
			ESBCDFormat aESBCDFormat = (ESBCDFormat)formatElement;
			if (aESBCDFormat.getOpCtx()) {
				aESBCDFormat.addFormatToContext( context);
			}
			else {
				DataElement aRecord = null;
				if (aRecordTemplate != null)
					aRecord = (DataElement)aRecordTemplate.clone();
				aESBCDFormat.addFormatToContext(aRecord, context);
				if (aRecord != null)
					iColl.addDataElement(aRecord);
			}
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
	public void addFormatToContext(DataElement dataElement, Context context) throws Exception {
		DataElement aDataElement = null;
		if (this.getDataName() != null) {
			try {
				String aDataElementName = (String)SFUtil.getContextValueInAction(context, this.getDataName());
				if (aDataElementName != null && aDataElementName.length() > 0) {
					if (this.isAppend()) {
						//dataElement.setAppend(this.isAppend());不改变父亲的DataElement的属性
						if (!(((KeyedCollection)dataElement).containsKey(aDataElementName))){
							DataElement theAppendDataElement = (DataElement) (this.getAppendClass() == null? IndexedCollection.class :Class.forName(this.getAppendClass())).newInstance();
							theAppendDataElement.setName(aDataElementName);
							theAppendDataElement.setAppend(this.isAppend());
							((KeyedCollection)dataElement).addDataElement(theAppendDataElement);
						}
					}
					aDataElement = ((KeyedCollection)dataElement).getDataElement(aDataElementName, this.getAppendClass() == null? IndexedCollection.class :Class.forName(this.getAppendClass()));
				}
				if (aDataElement != null) {
					dataElement = aDataElement;
					//SFLogger.debug(context, "unformat ESBArrayFormat find ICOLL [" + aDataElementName + "] in Parent KCOLL");
				}
				else {
					//SFLogger.debug(context, "unformat ESBArrayFormat can't find ICOLL [" + aDataElementName + "] in Parent KCOLL, , Continue with NULL");					
				}
			}
			catch (Exception e) {
				SFLogger.error(context, "unformat <ESBArray id=\"" + this.getName() + "\" dataName=\"" + this.getDataName() + "\"> occur error!");
				throw new EMPFormatException(e);
			}
		}
		FormatElement formatElement = this.getFormatElement();

		IndexedCollection iColl = (IndexedCollection) dataElement;
		if (!this.isAppend())
			iColl.clear();

		DataElement aRecordTemplate = iColl.getDataElement();



		if (ESBFieldFormat.class.isAssignableFrom(formatElement.getClass())) {
			ESBFieldFormat aESBFieldFormat = (ESBFieldFormat)formatElement;
			if (aESBFieldFormat.getOpCtx()) {
				aESBFieldFormat.addFormatToContext( context);
			}
			else {
				DataElement aRecord = (DataElement)aRecordTemplate.clone();
				aESBFieldFormat.addFormatToContext(aRecord, context);
				iColl.addDataElement(aRecord);
			}
		}
		else if (ESBCDFormat.class.isAssignableFrom(formatElement.getClass())) {
			ESBCDFormat aESBCDFormat = (ESBCDFormat)formatElement;
			if (aESBCDFormat.getOpCtx()) {
				aESBCDFormat.addFormatToContext(context);
			}
			else {
				DataElement aRecord = (DataElement)aRecordTemplate.clone();
				aESBCDFormat.addFormatToContext(aRecord,context);
				iColl.addDataElement(aRecord);
			}
		} 
	
	}
}
