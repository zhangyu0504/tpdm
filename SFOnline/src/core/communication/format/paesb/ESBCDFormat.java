package core.communication.format.paesb;

import com.dc.eai.data.Array;
import com.dc.eai.data.AtomData;
import com.dc.eai.data.CompositeData;
import com.dc.eai.data.Field;
import com.ecc.emp.core.Context;
import com.ecc.emp.core.EMPConstance;
import com.ecc.emp.data.DataElement;
import com.ecc.emp.data.KeyedCollection;
import com.ecc.emp.format.EMPFormatException;
import com.ecc.emp.format.FormatElement;

import common.util.SFUtil;

import core.communication.format.KeyedFormat;
import core.log.SFLogger;

/**
 * 数据结构(kColl)类型数据的格式化处理类。
 * 
 * @author zhongmc
 * @version 2.1
 * @since 1.0 2006-12-4
 * @lastmodified 2008-7-2
 */
public class ESBCDFormat extends KeyedFormat{

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
	
	public ESBCDFormat() {
		super();
	}

	public ESBCDFormat(String name) {
		super(name);
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
	public CompositeData format(Context context) throws EMPFormatException {
		
		CompositeData aCompositeData = new CompositeData();
		DataElement aDataElement = null;
		if (this.getDataName() != null) {
			try {
				String aDataElementName = SFUtil.getContextValueInAction(context, this.getDataName());
				if (aDataElementName != null && aDataElementName.length() > 0)
					aDataElement = context.getDataElement(aDataElementName);
				else
					SFLogger.debug(context, "format ESBCDFormat, can't find dataElement [" + this.getDataName() + "] in context");
				
			}
			catch (Exception e) {
				throw new EMPFormatException(e);
			}
		}

		for (int i = 0; i < this.fmtElements.size(); i++) {
			FormatElement element = (FormatElement) fmtElements.get(i);
			if (ESBCDFormat.class.isAssignableFrom(element.getClass())) {
				ESBCDFormat aESBCDFormat = (ESBCDFormat)element;
				if (aESBCDFormat.getOpCtx()) {
					CompositeData tmpCD = aESBCDFormat.format(context);
					if (tmpCD != null){
						aCompositeData.addStruct(element.getName(), tmpCD);
					}else{
						aCompositeData.addStruct(element.getName(), new CompositeData());
					}
						
				}
				else {
					CompositeData tmpCD = aESBCDFormat.format(aDataElement, context);
					if (tmpCD != null){
						aCompositeData.addStruct(element.getName(), tmpCD);
					}else{
						aCompositeData.addStruct(element.getName(), new CompositeData());
					}	
				}
			} 
			else if (ESBArrayFormat.class.isAssignableFrom(element.getClass())) {
				ESBArrayFormat aESBArrayFormat = (ESBArrayFormat)element;
				if (aESBArrayFormat.getOpCtx()) {
					Array tmpArray = aESBArrayFormat.format(context);
					if (tmpArray != null){
						aCompositeData.addArray(element.getName(),tmpArray);
					}else{
						aCompositeData.addArray(element.getName(),new Array());
					}
						
				}
				else {
					Array tmpArray = aESBArrayFormat.format(aDataElement, context);
//					if (tmpArray != null)
//						aCompositeData.addArray(element.getName(), tmpArray);
					if (tmpArray != null){
						aCompositeData.addArray(element.getName(),tmpArray);
					}else{
						aCompositeData.addArray(element.getName(),new Array());
					}	
				}
			} 
			else if (ESBFieldFormat.class.isAssignableFrom(element.getClass())){
				ESBFieldFormat aESBFieldFormat = (ESBFieldFormat)element;
				if (aESBFieldFormat.getOpCtx()) {
					Field tmpField = aESBFieldFormat.format(context);
					if (tmpField != null)
						aCompositeData.addField(element.getName(), tmpField);
				}
				else {
					Field tmpField = aESBFieldFormat.format(aDataElement, context);
					if (tmpField != null)
						aCompositeData.addField(element.getName(), tmpField);
				}
			}
			else {
				
				
				SFLogger.error(context, "Format failed when format: " + element);
				throw new EMPFormatException("Invalid Format when format: " + this);
			}
		}
		
		if (aCompositeData.size() == 0 && !this.nullAppear)
			return null;
		else
			return aCompositeData;
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
	public CompositeData format(DataElement dataElement, Context context) throws EMPFormatException {
		
		CompositeData aCompositeData = new CompositeData();

		DataElement aDataElement = dataElement;
		if (this.getDataName() != null) {
			try {
				String aDataElementName = SFUtil.getContextValueInAction(context, this.getDataName());
				if (aDataElementName != null && aDataElementName.length() > 0)
					aDataElement = ((KeyedCollection)dataElement).getDataElement(aDataElementName);
				else
					SFLogger.debug(context, "format ESBCDFormat, can't find dataElement [" + this.getDataName() + "][" + aDataElement + "] in context");
			}
			catch (Exception e) {
				throw new EMPFormatException(e);
			}
		}

		for (int i = 0; i < this.fmtElements.size(); i++) {
			FormatElement element = (FormatElement) fmtElements.get(i);
			if (ESBCDFormat.class.isAssignableFrom(element.getClass())) {
				ESBCDFormat aESBCDFormat = (ESBCDFormat)element;
				if (aESBCDFormat.getOpCtx()) {
					CompositeData tmpCD = aESBCDFormat.format(context);
					if (tmpCD != null)
						aCompositeData.addStruct(element.getName(), tmpCD);
				}
				else {
					CompositeData tmpCD = aESBCDFormat.format(aDataElement, context);
					if (tmpCD != null)
						aCompositeData.addStruct(element.getName(), tmpCD);
				}
			} 
			else if (ESBArrayFormat.class.isAssignableFrom(element.getClass())) {
				ESBArrayFormat aESBArrayFormat = (ESBArrayFormat)element;
				if (aESBArrayFormat.getOpCtx()) {
					Array tmpArray = aESBArrayFormat.format(context);
					if (tmpArray != null)
						aCompositeData.addArray(element.getName(), tmpArray);
				}
				else {
					Array tmpArray = aESBArrayFormat.format(aDataElement, context);
					if (tmpArray != null)
						aCompositeData.addArray(element.getName(), tmpArray);
				}
			} 
			else if (ESBFieldFormat.class.isAssignableFrom(element.getClass())){
				ESBFieldFormat aESBFieldFormat = (ESBFieldFormat)element;
				if (aESBFieldFormat.getOpCtx()) {
					Field tmpField = aESBFieldFormat.format(context);
					if (tmpField != null)
						aCompositeData.addField(element.getName(), tmpField);
				}
				else {
					Field tmpField = aESBFieldFormat.format(aDataElement, context);
					if (tmpField != null)
						aCompositeData.addField(element.getName(), tmpField);
				}
			}
			else {
				SFLogger.error(context, "Format failed when format: " + element);
				throw new EMPFormatException("Invalid Format when format: " + this);
			}
		}

		if (aCompositeData.size() == 0 && !this.nullAppear)
			return null;
		else
			return aCompositeData;
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
	public void unFormat(Object src, Context context) throws Exception {
		
		DataElement aDataElement = null;
		if (src == null)
			return;
		
		//SFLogger.debug(context, "prepare to unformat <ESBCD id=\"" + this.getName() + "\" dataName=\"" + this.getDataName() + "\">");
		if (this.getDataName() != null) {
			try {
				String aDataElementName = SFUtil.getContextValueInAction(context, this.getDataName());
				if (aDataElementName != null && aDataElementName.length() > 0) {
					if (this.isAppend()) {
						//context.getDataElement().setAppend(this.isAppend());这个改变了context的属性，废掉
						if (!context.containsKey(aDataElementName)){
							DataElement theAppendDataElement = (DataElement) (this.getAppendClass() == null? KeyedCollection.class :Class.forName(this.getAppendClass())).newInstance();
							theAppendDataElement.setName(aDataElementName);
							theAppendDataElement.setAppend(this.isAppend());
							context.addDataElement(theAppendDataElement);
						}
					}
					aDataElement = context.getDataElement(aDataElementName, this.getAppendClass() == null? KeyedCollection.class :Class.forName(this.getAppendClass()));
				}
				if (aDataElement != null) {
					//SFLogger.debug(context, "unformat ESBCDFormat find KCOLL [" + aDataElementName + "] in context");
				}
				else {
					//SFLogger.debug(context, "unformat ESBCDFormat can't find KCOLL [" + aDataElementName + "] in context, , Continue with NULL");					
				}
			}
			catch (Exception e) {
				SFLogger.error(context, "unformat <ESBCD id=\"" + this.getName() + "\" dataName=\"" + this.getDataName() + "\"> occur error!");
				throw new EMPFormatException(e);
			}
		}
		if (this.getName() != null) {
			src = ((CompositeData)src).getStruct(this.getName());
			//SFLogger.debug(context, "unformat <ESBCD id=\"" + this.getName() + "\" dataName=\"" + this.getDataName() + "\">" + " find CD [" + this.getName() + "] in parent CD");
		
		
		}
		else {
			//SFLogger.debug(context, "unformat <ESBCD id=\"" + this.getName() + "\" dataName=\"" + this.getDataName() + "\">" + " No id, Using Parent CD");
		}

		for (int i = 0; i < this.fmtElements.size(); i++) {
			FormatElement element = (FormatElement)fmtElements.get(i);
			if (ESBCDFormat.class.isAssignableFrom(element.getClass())) {
				ESBCDFormat aESBCDFormat = (ESBCDFormat)element;
				
				if (aESBCDFormat.getOpCtx()) {
					aESBCDFormat.unFormat(src, context);
				}
				else {
					aESBCDFormat.unformat(aDataElement, (AtomData)src, context);
				}
			} 
			else if (ESBArrayFormat.class.isAssignableFrom(element.getClass())) {
				ESBArrayFormat aESBArrayFormat = (ESBArrayFormat)element;
				
				if (aESBArrayFormat.getOpCtx()) {
					aESBArrayFormat.unformat((AtomData)src, context);
				}
				else {
					aESBArrayFormat.unformat(aDataElement, (AtomData)src, context);
				}
			} 
			else if (ESBFieldFormat.class.isAssignableFrom(element.getClass())){
				ESBFieldFormat aESBFieldFormat = (ESBFieldFormat)element;
				if (aESBFieldFormat.getOpCtx()) {
					aESBFieldFormat.unformat((AtomData)src, context);
				}
				else {
					aESBFieldFormat.unformat(aDataElement, (AtomData)src, context);
				}
			}
			else {
				SFLogger.error(context, "UnFormat failed when unformat: " + element);
				throw new EMPFormatException("Invalid Format when unformat: " + this);
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
		
		if (atomData == null)
			return;
		
		//SFLogger.debug(context, "prepare to unformat <ESBCD id=\"" + this.getName() + "\" dataName=\"" + this.getDataName() + "\"> ON A KCOLL");
		if (this.getDataName() != null && dataElement != null) {
			try {
				String aDataElementName = SFUtil.getContextValueInAction(context, this.getDataName());
				if (aDataElementName != null && aDataElementName.length() > 0) {
					if (this.isAppend()) {
						//dataElement.setAppend(this.isAppend());不改变父亲的DataElement的属性
						if (!(((KeyedCollection)dataElement).containsKey(aDataElementName))){
							DataElement theAppendDataElement = (DataElement) (this.getAppendClass() == null? KeyedCollection.class :Class.forName(this.getAppendClass())).newInstance();
							theAppendDataElement.setName(aDataElementName);
							theAppendDataElement.setAppend(this.isAppend());
							((KeyedCollection)dataElement).addDataElement(theAppendDataElement);
						}
					}
					aDataElement = ((KeyedCollection)dataElement).getDataElement(aDataElementName, this.getAppendClass() == null? KeyedCollection.class :Class.forName(this.getAppendClass()));
				}
				if (aDataElement != null) {
					dataElement = aDataElement;
					//SFLogger.debug(context, "unformat ESBCDFormat find KCOLL [" + aDataElementName + "] in Parent KCOLL");
				}
				else {
					//SFLogger.debug(context, "unformat ESBCDFormat can't find KCOLL [" + aDataElementName + "] in Parent KCOLL, Continue with NULL");					
				}
			}
			catch (Exception e) {
				SFLogger.error(context, "unformat <ESBCD id=\"" + this.getName() + "\" dataName=\"" + this.getDataName() + "\"> occur error!");
				throw new EMPFormatException(e);
			}
		}
		if (this.getName() != null) {
			atomData = ((CompositeData)atomData).getStruct(this.getName());
			//SFLogger.debug(context, "unformat <ESBCD id=\"" + this.getName() + "\" dataName=\"" + this.getDataName() + "\">" + " find CD [" + this.getName() + "] in parent CD");
		}
		else {
			//SFLogger.debug(context, "unformat <ESBCD id=\"" + this.getName() + "\" dataName=\"" + this.getDataName() + "\">" + " No id, Using Parent CD");
		}

		for (int i = 0; i < this.fmtElements.size(); i++) {
			FormatElement element = (FormatElement) fmtElements.get(i);
			if (ESBCDFormat.class.isAssignableFrom(element.getClass())) {
				ESBCDFormat aESBCDFormat = (ESBCDFormat)element;
				
				if (aESBCDFormat.getOpCtx()) {
					aESBCDFormat.unFormat(atomData, context);
				}
				else {
					aESBCDFormat.unformat(dataElement, atomData, context);
				}
			} 
			else if (ESBArrayFormat.class.isAssignableFrom(element.getClass())) {
				ESBArrayFormat aESBArrayFormat = (ESBArrayFormat)element;
				
				if (aESBArrayFormat.getOpCtx()) {
					aESBArrayFormat.unformat(atomData, context);
				}
				else {
					aESBArrayFormat.unformat(dataElement, atomData, context);
				}
			} 
			else if (ESBFieldFormat.class.isAssignableFrom(element.getClass())){
				ESBFieldFormat aESBFieldFormat = (ESBFieldFormat)element;
				if (aESBFieldFormat.getOpCtx()) {
					aESBFieldFormat.unformat(atomData, context);
				}
				else {
					aESBFieldFormat.unformat(dataElement, atomData, context);
				}
			}
			else {
				SFLogger.error(context, "UnFormat failed when unformat: " + element);
				throw new EMPFormatException("Invalid Format when unformat: " + this);
			}
		}
	}
	
	public String toString(int tabCount) {
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < tabCount; i++)
			buf.append("\t");

		buf.append("<ESBCD dataName=\"");
		buf.append(getDataName());
		buf.append(" name=\"").append(this.getName());
		buf.append(" opCtx=\"").append(this.getOpCtx());
		buf.append(" append=\"").append(this.isAppend());
		buf.append("\"/>\n");
		
		for (int i = 0; i < this.fmtElements.size(); i++) {
			FormatElement fmt = (FormatElement) fmtElements.get(i);
			buf.append(fmt.toString(tabCount + 1));
			buf.append("\n");
		}

		for (int i = 0; i < tabCount; i++)
			buf.append("\t");
		buf.append("</ESBCD>");

		for (int i = 0; i < this.getDecorators().size(); i++) {
			FormatElement fmt = (FormatElement) getDecorators().get(i);
			buf.append(fmt.toString(tabCount));
			buf.append("\n");

		}
		return buf.toString();
	}
	
	
	
	
	
	public void addFormatToContext(Context context) throws Exception{

		DataElement aDataElement = null;
		
		//SFLogger.debug(context, "prepare to unformat <ESBCD id=\"" + this.getName() + "\" dataName=\"" + this.getDataName() + "\">");
		if (this.getDataName() != null) {
			try {
				String aDataElementName = SFUtil.getContextValueInAction(context, this.getDataName());
				if (aDataElementName != null && aDataElementName.length() > 0) {
					if (this.isAppend()) {
						//context.getDataElement().setAppend(this.isAppend());这个改变了context的属性，废掉
						if (!context.containsKey(aDataElementName)){
							DataElement theAppendDataElement = (DataElement) (this.getAppendClass() == null? KeyedCollection.class :Class.forName(this.getAppendClass())).newInstance();
							theAppendDataElement.setName(aDataElementName);
							theAppendDataElement.setAppend(this.isAppend());
							context.addDataElement(theAppendDataElement);
						}
					}
					aDataElement = context.getDataElement(aDataElementName, this.getAppendClass() == null? KeyedCollection.class :Class.forName(this.getAppendClass()));
				}
				if (aDataElement != null) {
					//SFLogger.debug(context, "unformat ESBCDFormat find KCOLL [" + aDataElementName + "] in context");
				}
				else {
					//SFLogger.debug(context, "unformat ESBCDFormat can't find KCOLL [" + aDataElementName + "] in context, , Continue with NULL");					
				}
			}
			catch (Exception e) {
				SFLogger.error(context, "unformat <ESBCD id=\"" + this.getName() + "\" dataName=\"" + this.getDataName() + "\"> occur error!");
				throw new EMPFormatException(e);
			}
		}

		for (int i = 0; i < this.fmtElements.size(); i++) {
			FormatElement element = (FormatElement)fmtElements.get(i);
			if (ESBCDFormat.class.isAssignableFrom(element.getClass())) {
				ESBCDFormat aESBCDFormat = (ESBCDFormat)element;
				
				if (aESBCDFormat.getOpCtx()) {
					aESBCDFormat.addFormatToContext(context);
				}
				else {
					aESBCDFormat.addFormatToContext(aDataElement, context);
				}
			} 
			else if (ESBArrayFormat.class.isAssignableFrom(element.getClass())) {
				ESBArrayFormat aESBArrayFormat = (ESBArrayFormat)element;
				
				if (aESBArrayFormat.getOpCtx()) {
					aESBArrayFormat.addFormatToContext(context);
				}
				else {
					aESBArrayFormat.addFormatToContext(aDataElement, context);
				}
			} 
			else if (ESBFieldFormat.class.isAssignableFrom(element.getClass())){
				ESBFieldFormat aESBFieldFormat = (ESBFieldFormat)element;
				if (aESBFieldFormat.getOpCtx()) {
					aESBFieldFormat.addFormatToContext(context);
				}
				else {
					aESBFieldFormat.addFormatToContext(aDataElement,context);
				}
			}
			else {
				SFLogger.error(context, "UnFormat failed when unformat: " + element);
				throw new EMPFormatException("Invalid Format when unformat: " + this);
			}
		}
	
		
		
		
		
//		CompositeData responseData = new CompositeData();
//		responseData = PackUtil.unpackXmlStr(src.trim());
//		for (int i=0;i<this.fmtElements.size();i++) {
//			ESBCDFormat element = (ESBCDFormat)this.fmtElements.get(i);
//			
//			if("BODY".equals(element.getName())){
//				
//				
//				
//				
//				ExpressCalculateService es= (ExpressCalculateService) context.getService((String) context.getDataValue(SFConst.CTX_SERVICE_EXPRESSCALC));
//				String kcoll = (String)es.execute(element.getDataName(), context);
//				
//				element.putBodyToContext(context,kcoll);   //element.getDataName()
//				
//			} 
//			
//			
//		}	
	
		
	}
	
	
	public void addFormatToContext(DataElement dataElement,Context context) throws Exception{
		DataElement aDataElement = null;
		
		if (this.getDataName() != null && dataElement != null) {
			try {
				String aDataElementName = SFUtil.getContextValueInAction(context, this.getDataName());
				if (aDataElementName != null && aDataElementName.length() > 0) {
					if (this.isAppend()) {
						//dataElement.setAppend(this.isAppend());不改变父亲的DataElement的属性
						if (!(((KeyedCollection)dataElement).containsKey(aDataElementName))){
							DataElement theAppendDataElement = (DataElement) (this.getAppendClass() == null? KeyedCollection.class :Class.forName(this.getAppendClass())).newInstance();
							theAppendDataElement.setName(aDataElementName);
							theAppendDataElement.setAppend(this.isAppend());
							((KeyedCollection)dataElement).addDataElement(theAppendDataElement);
						}
					}
					aDataElement = ((KeyedCollection)dataElement).getDataElement(aDataElementName, this.getAppendClass() == null? KeyedCollection.class :Class.forName(this.getAppendClass()));
				}
				if (aDataElement != null) {
					dataElement = aDataElement;
					//SFLogger.debug(context, "unformat ESBCDFormat find KCOLL [" + aDataElementName + "] in Parent KCOLL");
				}
				else {
					//SFLogger.debug(context, "unformat ESBCDFormat can't find KCOLL [" + aDataElementName + "] in Parent KCOLL, Continue with NULL");					
				}
			}
			catch (Exception e) {
				SFLogger.error(context, "unformat <ESBCD id=\"" + this.getName() + "\" dataName=\"" + this.getDataName() + "\"> occur error!");
				throw new EMPFormatException(e);
			}
		}

		for (int i = 0; i < this.fmtElements.size(); i++) {
			FormatElement element = (FormatElement) fmtElements.get(i);
			if (ESBCDFormat.class.isAssignableFrom(element.getClass())) {
				ESBCDFormat aESBCDFormat = (ESBCDFormat)element;
				
				if (aESBCDFormat.getOpCtx()) {
					aESBCDFormat.addFormatToContext(context);
				}
				else {
					aESBCDFormat.addFormatToContext(dataElement,context);
				}
			} 
			else if (ESBArrayFormat.class.isAssignableFrom(element.getClass())) {
				ESBArrayFormat aESBArrayFormat = (ESBArrayFormat)element;
				
//				if (aESBArrayFormat.getOpCtx()) {
//					aESBArrayFormat.addFormatToContext(context);
//				}
//				else {
//					aESBArrayFormat.addFormatToContext(dataElement,context);
//				}
			} 
			else if (ESBFieldFormat.class.isAssignableFrom(element.getClass())){
				ESBFieldFormat aESBFieldFormat = (ESBFieldFormat)element;
				if (aESBFieldFormat.getOpCtx()) {
					aESBFieldFormat.addFormatToContext(context);
				}
				else {
					aESBFieldFormat.addFormatToContext(dataElement,context);
				}
			}
			else {
				SFLogger.error(context, "UnFormat failed when unformat: " + element);
				throw new EMPFormatException("Invalid Format when unformat: " + this);
			}
		}
	}
}
