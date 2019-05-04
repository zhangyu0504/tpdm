package core.communication.format.xml;

import java.io.ByteArrayInputStream;

import module.bean.SecCompData;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ecc.emp.component.xml.XMLDocumentLoader;
import com.ecc.emp.core.Context;
import com.ecc.emp.data.DataElement;
import com.ecc.emp.data.DataField;
import com.ecc.emp.data.KeyedCollection;
import com.ecc.emp.format.EMPFormatException;
import com.ecc.emp.format.FormatElement;
import common.util.SFConst;
import common.util.SFUtil;

import core.communication.format.FormatField;
import core.communication.format.KeyedFormat;
import core.log.SFLogger;
import core.service.PBankExpressCalculate;

/**
 * 数据结构(kColl)类型数据的格式化处理类。
 * 
 * @author zhongmc
 * @version 2.1
 * @since 1.0 2006-12-4
 * @lastmodified 2008-7-2
 */
public class XMLWrapFormat extends KeyedFormat{

	private boolean opCtx = false;
	
	//如果改该字段的值为空是否需要增加到报文中的标志
	private boolean nullAppear = true;

	//如果改该字段的值为空是否改属性需要增加到报文中的标志
	private boolean nullAttrAppear = false;

	//如果改该字段的值为空是否需要显示FULL TAG
	private boolean isFullTag = true;
	
	public void setNullAppear(boolean nullAppear)
	{
		this.nullAppear = nullAppear;
	}
	
	public Boolean isNullAppear()
	{
		return this.nullAppear;
	}
	
	public XMLWrapFormat() {
		super();
	}

	public XMLWrapFormat(String name) {
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
	private String[] propName;
	
	public String[] getPropName() {
		return propName;
	}

	public void setPropName(String propName) {
		this.propName = propName.split("|");
	}

	private String[] propDataName;
	
	public String[] getPropDataName() {
		return propDataName;
	}

	public void setPropDataName(String propDataName) {
		this.propDataName = propDataName.split("|");;
	}
	
	private String kCollName;
	
	public String getKCollName() {
		return kCollName;
	}

	public void setKCollName(String kCollName) {
		this.kCollName = kCollName;
	}

	private boolean isICMember = true;

	public boolean isICMember() {
		return isICMember;
	}

	public void setIsICMember(boolean isICMember) {
		this.isICMember = isICMember;
	}

	public boolean isNullAttrAppear() {
		return nullAttrAppear;
	}

	public void setNullAttrAppear(boolean nullAttrAppear) {
		this.nullAttrAppear = nullAttrAppear;
	}

	public boolean isFullTag() {
		return isFullTag;
	}

	public void setFullTag(boolean isFullTag) {
		this.isFullTag = isFullTag;
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
		for (int i = 0; i < this.fmtElements.size(); i++) {
			FormatElement element = (FormatElement)fmtElements.get(i);
			if (XMLWrapFormat.class.isAssignableFrom(element.getClass())) {
				XMLWrapFormat aXMLKCFormat = (XMLWrapFormat)element;
				aXMLKCFormat.updateTextIndent(aTextIndent);
			} 
			else if (XMLArrayFormat.class.isAssignableFrom(element.getClass())) {
				XMLArrayFormat aXMLArrayFormat = (XMLArrayFormat)element;
				aXMLArrayFormat.updateTextIndent(aTextIndent);
			} 
			else if (XMLFieldFormat.class.isAssignableFrom(element.getClass())){
				XMLFieldFormat aXMLFieldFormat = (XMLFieldFormat)element;
				aXMLFieldFormat.updateTextIndent(aTextIndent);
			}
			else if (XMLHeadFormat.class.isAssignableFrom(element.getClass())){
				XMLHeadFormat aXMLHeadFormat = (XMLHeadFormat)element;
				aXMLHeadFormat.updateTextIndent(aTextIndent);
			}
		}
	}

	//重构该函数，处理文本缩进的问题
	public void addFormatField(FormatField field) {
		if (XMLWrapFormat.class.isAssignableFrom(field.getClass())) {
			((XMLWrapFormat)field).updateTextIndent(this.getTextIndent());
		}
		if (XMLArrayFormat.class.isAssignableFrom(field.getClass())) {
			((XMLArrayFormat)field).updateTextIndent(this.getTextIndent());
		}
		if (XMLFieldFormat.class.isAssignableFrom(field.getClass())) {
			((XMLFieldFormat)field).updateTextIndent(this.getTextIndent());
		}
		if (XMLHeadFormat.class.isAssignableFrom(field.getClass())) {
			((XMLHeadFormat)field).updateTextIndent(this.getTextIndent());
		}
		super.addFormatField(field);
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
	public String format(Context context) throws EMPFormatException {
		return format(context, null);
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
	public String format(Context context, String tagSuffix) throws EMPFormatException {
		StringBuffer retStr = new StringBuffer();
		boolean isNodeValueNull = true;
		DataElement theKCollElement = null;
		String newTagName = this.getName();
		
		if (tagSuffix != null)
			newTagName = newTagName + tagSuffix;

		//SFLogger.debug(context, "prepare to format <" + this.getName() + ">");
		//如果有KCollName，先查找并生成KColl
		if (this.getKCollName() != null) {
			try {
				String aDataElementName = SFUtil.getContextValueInAction(context, this.getKCollName());
				if (this.isAppend()) {
					context.getDataElement().setAppend(this.isAppend());
				}
				if (aDataElementName != null && aDataElementName.length() > 0) {
					theKCollElement = context.getDataElement(aDataElementName, this.getAppendClass() == null? KeyedCollection.class :Class.forName(this.getAppendClass()));
//					try {
//						theKCollElement = context.getDataElement(aDataElementName, this.getAppendClass() == null? KeyedCollection.class :Class.forName(this.getAppendClass()));
//					}
//					catch (Exception e)
//					{
//						theKCollElement = null;
//					}
				}
//				if (theKCollElement != null) {
//					SFLogger.debug(context, "format XMLKCollFormat find KCOLL [" + aDataElementName + "] in context");
//				}
//				else {
//					SFLogger.debug(context, "format XMLKCollFormat can't find KCOLL [" + aDataElementName + "] in context, , Continue with NULL");					
//				}
			}
			catch (Exception e) {
				SFLogger.error(context, "format <XMLKC id=\"" + this.getName() + "\" dataName=\"" + this.getKCollName() + "\"> occur error!");
				throw new EMPFormatException(e);
			}
		}
		
		if (newTagName != null && newTagName.length() > 1)
			retStr.append(this.getTextIndent() + "<").append(newTagName);

		//打包XML中本节点的属性
		if (null != this.getPropName() && this.getPropName().length > 0 && (this.getPropName().length == this.getPropDataName().length)) {
			for (int i = 0; i < this.getPropName().length; i++) {
				String proName = this.getPropName()[i];
				String proDataName = this.getPropDataName()[i];
			
				try {
					String tmpProName = SFUtil.getContextValueInAction(context, proName);
					String tmpProDataName = SFUtil.getContextValueInAction(context, proDataName);
					String tmpKCollName = SFUtil.getContextValueInAction(context, this.getKCollName());

					StringBuffer strBufDataValue = new StringBuffer();

					if (tmpKCollName != null && tmpProDataName != null)
						tmpProDataName = tmpKCollName + "." + tmpProDataName;
					
					try {
						this.format(strBufDataValue, tmpProDataName, context);
					}
					catch (Exception e)  {
						SFLogger.error(context, "XMLKC[" + this + "] format failed!"+ e);
						throw new EMPFormatException(e);
					}

					if (strBufDataValue.length() > 0) {
						isNodeValueNull = false;
						retStr.append(" " + tmpProName + "=\"").append(strBufDataValue + "\"");
					}
					else {
						if (this.nullAttrAppear) {
							retStr.append(" " + tmpProName + "=\"\"");
						}
					}
				}
				catch (Exception e) {
					SFLogger.error(context, "format <XMLKC id=\"" + this.getName() + "\"> occur error!");
					throw new EMPFormatException(e);
				}
			}
		}
		
		//递归循环对后续一项一项展现
		StringBuffer subNodeStr = new StringBuffer();
		for (int i = 0; i < this.fmtElements.size(); i++) {
			FormatElement element = (FormatElement)fmtElements.get(i);
			
			if (XMLWrapFormat.class.isAssignableFrom(element.getClass())) {
				XMLWrapFormat aXMLKCFormat = (XMLWrapFormat)element;
				if (aXMLKCFormat.getOpCtx() || this.getKCollName() == null) {
					subNodeStr.append("\r\n").append(aXMLKCFormat.format(context));
				}
				else {
					subNodeStr.append("\r\n").append(aXMLKCFormat.format(theKCollElement, context));
				}
			} 
			else if (XMLArrayFormat.class.isAssignableFrom(element.getClass())) {
				XMLArrayFormat aXMLArrayFormat = (XMLArrayFormat)element;
				
				if (aXMLArrayFormat.getOpCtx() || this.getKCollName() == null) {
					subNodeStr.append("\r\n").append(aXMLArrayFormat.format(context));
				}
				else {
					subNodeStr.append("\r\n").append(aXMLArrayFormat.format(theKCollElement, context));
				}
			} 
			else if (XMLFieldFormat.class.isAssignableFrom(element.getClass())){
				XMLFieldFormat aXMLFieldFormat = (XMLFieldFormat)element;

				if (aXMLFieldFormat.getOpCtx() || this.getKCollName() == null) {
					subNodeStr.append("\r\n").append(aXMLFieldFormat.format(context));
				}
				else {
					subNodeStr.append("\r\n").append(aXMLFieldFormat.format(theKCollElement, context));
				}
			}
			else if (XMLHeadFormat.class.isAssignableFrom(element.getClass())){
				XMLHeadFormat aXMLHeadFormat = (XMLHeadFormat)element;
				subNodeStr.append(aXMLHeadFormat.format(context));
			}
			else {
				SFLogger.error(context, "Format failed when format: " + element);
				throw new EMPFormatException("Invalid Format when format: " + this);
			}
		}

		//打包XML中本节点的值
		try {
			StringBuffer strBufDataValue = new StringBuffer();
			String tmpKCollName = SFUtil.getContextValueInAction(context, this.getKCollName());
			String tmpDataName = SFUtil.getContextValueInAction(context, this.getDataName());
			if (tmpKCollName != null && tmpDataName != null)
				tmpDataName = tmpKCollName + "." + tmpDataName;

			if (tmpDataName != null && tmpDataName.length() > 1)
				this.format(strBufDataValue, tmpDataName, context);
			strBufDataValue.append(subNodeStr);
			
			if (strBufDataValue.length() > 0) {
				isNodeValueNull = false;
				if (newTagName != null && newTagName.length() > 1)
					retStr.append(">" + strBufDataValue + "\r\n" + this.getTextIndent() + "</" + newTagName + ">");
				else
					retStr.append(strBufDataValue);
			}
			else {
				if (isNodeValueNull) {
					if (this.isNullAppear()) {
						if (this.isFullTag()) {
							if (newTagName != null && newTagName.length() > 1)
								retStr.append(">\r\n" + this.getTextIndent() + "</" + newTagName + ">");
						}
						else {
							if (newTagName != null && newTagName.length() > 1)
								retStr.append("/>");
						}
					}
					else {
						//值为空不展现
						retStr.setLength(0);
					}
				}
				else {
					if (this.isFullTag()) {
						if (newTagName != null && newTagName.length() > 1)
							retStr.append(">\r\n" + this.getTextIndent() + "</" + newTagName + ">");
					}
					else {
						if (newTagName != null && newTagName.length() > 1)
							retStr.append("/>");
					}
				}
			}
		}
		catch (Exception e)  {
			SFLogger.error(context, "XMLKC[" + this + "] format failed!"+ e);
			throw new EMPFormatException(e);
		}
		

		return retStr.toString();
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
	public String format(DataElement dataElement, Context context) throws EMPFormatException {
		return format(dataElement, context, null);
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
	public String format(DataElement dataElement, Context context, String tagSuffix) throws EMPFormatException {
		StringBuffer retStr = new StringBuffer();
		boolean isNodeValueNull = true;
		DataElement theKCollElement = dataElement;
		String newTagName = this.getName();
		
		if (tagSuffix != null)
			newTagName = newTagName + tagSuffix;

		//SFLogger.debug(context, "prepare to format <" + this.getName() + ">");
		//如果有KCollName，先查找并生成KColl
		if (this.getKCollName() != null) {
			try {
				String aDataElementName = SFUtil.getContextValueInAction(context, this.getKCollName());
				if (this.isAppend()) {
					((KeyedCollection)dataElement).setAppend(this.isAppend());
				}
				if (aDataElementName != null && aDataElementName.length() > 0) {
					theKCollElement = ((KeyedCollection)dataElement).getDataElement(aDataElementName, this.getAppendClass() == null? KeyedCollection.class :Class.forName(this.getAppendClass()));
//					try {
//						theKCollElement = ((KeyedCollection)dataElement).getDataElement(aDataElementName, this.getAppendClass() == null? KeyedCollection.class :Class.forName(this.getAppendClass()));
//					}
//					catch (Exception e)
//					{
//						theKCollElement = null;
//					}
				}
//				if (theKCollElement != null) {
//					SFLogger.debug(context, "format XMLKCollFormat find KCOLL [" + aDataElementName + "] in dataElement " + dataElement.getName());
//				}
//				else {
//					SFLogger.debug(context, "format XMLKCollFormat can't find KCOLL [" + aDataElementName + "] in dataElement " + dataElement.getName() +", Continue with NULL");				
//				}
			}
			catch (Exception e) {
				SFLogger.error(context, "format <XMLKC id=\"" + this.getName() + "\" dataName=\"" + this.getKCollName() + "\"> occur error!");
				throw new EMPFormatException(e);
			}
		}
		
		if (newTagName != null && newTagName.length() > 1)
			retStr.append(this.getTextIndent() + "<").append(newTagName);

		//打包XML中本节点的属性
		if (null != this.getPropName() && this.getPropName().length > 0 && (this.getPropName().length == this.getPropDataName().length)) {
			for (int i = 0; i < this.getPropName().length; i++) {
				String proName = this.getPropName()[i];
				String proDataName = this.getPropDataName()[i];
			
				try {
					String tmpProName = SFUtil.getContextValueInAction(context, proName);
					String tmpProDataName = SFUtil.getContextValueInAction(context, proDataName);
					String tmpKCollName = SFUtil.getContextValueInAction(context, this.getKCollName());

					StringBuffer strBufDataValue = new StringBuffer();

					if (tmpKCollName != null && tmpProDataName != null)
						tmpProDataName = tmpKCollName + "." + tmpProDataName;
					
					try {
						this.format(strBufDataValue, tmpProDataName, dataElement);
					}
					catch (Exception e)  {
						SFLogger.error(context, "XMLKC[" + this + "] format failed!"+ e);
						throw new EMPFormatException(e);
					}

					if (strBufDataValue.length() > 0) {
						isNodeValueNull = false;
						retStr.append(" " + tmpProName + "=\"").append(strBufDataValue + "\"");
					}
					else {
						if (this.nullAttrAppear) {
							retStr.append(" " + tmpProName + "=\"\"");
						}
					}
				}
				catch (Exception e) {
					SFLogger.error(context, "format <XMLKC id=\"" + this.getName() + "\"> occur error!");
					throw new EMPFormatException(e);
				}
			}
		}
		
		//递归循环对后续一项一项展现
		StringBuffer subNodeStr = new StringBuffer();
		for (int i = 0; i < this.fmtElements.size(); i++) {
			FormatElement element = (FormatElement)fmtElements.get(i);
			
			if (XMLWrapFormat.class.isAssignableFrom(element.getClass())) {
				XMLWrapFormat aXMLKCFormat = (XMLWrapFormat)element;
				if (aXMLKCFormat.getOpCtx()) {
					subNodeStr.append("\r\n").append(aXMLKCFormat.format(context));
				}
				else {
					subNodeStr.append("\r\n").append(aXMLKCFormat.format(theKCollElement, context));
				}
			} 
			else if (XMLArrayFormat.class.isAssignableFrom(element.getClass())) {
				XMLArrayFormat aXMLArrayFormat = (XMLArrayFormat)element;
				
				if (aXMLArrayFormat.getOpCtx()) {
					subNodeStr.append("\r\n").append(aXMLArrayFormat.format(context));
				}
				else {
					subNodeStr.append("\r\n").append(aXMLArrayFormat.format(theKCollElement, context));
				}
			} 
			else if (XMLFieldFormat.class.isAssignableFrom(element.getClass())){
				XMLFieldFormat aXMLFieldFormat = (XMLFieldFormat)element;

				if (aXMLFieldFormat.getOpCtx()) {
					subNodeStr.append("\r\n").append(aXMLFieldFormat.format(context));
				}
				else {
					subNodeStr.append("\r\n").append(aXMLFieldFormat.format(theKCollElement, context));
				}
			}
			else if (XMLHeadFormat.class.isAssignableFrom(element.getClass())){
				XMLHeadFormat aXMLHeadFormat = (XMLHeadFormat)element;
				subNodeStr.append("\r\n").append(aXMLHeadFormat.format(context));
			}
			else {
				SFLogger.error(context, "Format failed when format: " + element);
				throw new EMPFormatException("Invalid Format when format: " + this);
			}
		}

		//打包XML中本节点的值
		try {
			StringBuffer strBufDataValue = new StringBuffer();
			String tmpKCollName = SFUtil.getContextValueInAction(context, this.getKCollName());
			String tmpDataName = SFUtil.getContextValueInAction(context, this.getDataName());
			if (tmpKCollName != null && tmpDataName != null)
				tmpDataName = tmpKCollName + "." + tmpDataName;
			
			if (tmpDataName != null && tmpDataName.length() > 1)
				this.format(strBufDataValue, tmpDataName, dataElement);
			strBufDataValue.append(subNodeStr);
			
			if (strBufDataValue.length() > 0) {
				isNodeValueNull = false;
				if (newTagName != null && newTagName.length() > 1)
					retStr.append(">" + strBufDataValue + "\r\n" + this.getTextIndent() + "</" + newTagName + ">");
				else
					retStr.append(strBufDataValue);
			}
			else {
				if (isNodeValueNull) {
					if (this.isNullAppear()) {
						if (this.isFullTag()) {
							if (newTagName != null && newTagName.length() > 1)
								retStr.append(">\r\n" + this.getTextIndent() + "</" + newTagName + ">");
						}
						else {
							if (newTagName != null && newTagName.length() > 1)
								retStr.append("/>");
						}
					}
					else {
						//值为空不展现
						retStr.setLength(0);
					}
				}
				else {
					if (this.isFullTag()) {
						if (newTagName != null && newTagName.length() > 1)
							retStr.append(">\r\n" + this.getTextIndent() + "</" + newTagName + ">");
					}
					else {
						if (newTagName != null && newTagName.length() > 1)
							retStr.append("/>");
					}
				}
			}
		}
		catch (Exception e)  {
			SFLogger.error(context, "XMLKC[" + this + "] format failed!"+ e);
			throw new EMPFormatException(e);
		}
		

		return retStr.toString();
	}

	/**
	 * 字符串格式报文的打包入口。
	 * 
	 * @param output 字符串缓存
	 * @param context 交易上下文
	 * @throws Exception
	 */
	public void format(StringBuffer output, String dataName, Context context) throws Exception {
		DataField element = null;

		if (!isConstant()) {
			if (!this.isExpression()){
				element = (DataField) context.getDataElement(dataName);
			}
			else {
				PBankExpressCalculate aPBankExpCal = new PBankExpressCalculate();
				element = new DataField("EXP_RESULT", aPBankExpCal.execute(dataName, context));
			}
		}
	
		element = fbsTransfer(element);
		
		Object value = this.format(element);
		value = this.addDecoration(value);

		if (value.getClass().isArray()) {
			output.append(new String((byte[])value, this.getEncoding()));
		}
		else {
			output.append((String) value);
		}

		return;
	}

	/**
	 * 字符串格式报文的打包入口。
	 * 
	 * @param output 字符串缓存
	 * @param context 交易上下文
	 * @throws Exception
	 */
	public void format(StringBuffer output, String dataName, DataElement dataElement) throws Exception {
		DataField element = null;

		if (!isConstant()) {
			if (!this.isExpression()){
				element = (DataField)((KeyedCollection)dataElement).getDataElement(dataName);
			}
			else {
				PBankExpressCalculate aPBankExpCal = new PBankExpressCalculate();
				element = new DataField("EXP_RESULT", aPBankExpCal.execute(dataName, dataElement));
			}
		}
	
		element = fbsTransfer(element);
		
		Object value = this.format(element);
		value = this.addDecoration(value);

		if (value.getClass().isArray()) {
			output.append(new String((byte[])value, this.getEncoding()));
		}
		else {
			output.append((String) value);
		}

		return;
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
		DataElement aDataElement = null, theKCollElement = null;
		if (src == null){
			addFormatToContext(context);
			return;
		}
		//SFLogger.debug(context, "prepare to unFormat <" + this.getName() + "\">");
		
		XMLDocumentLoader loader = new XMLDocumentLoader();
		//替换XML语言不能识别的无法字符:如券商端加密后的密码
		src=src.toString().replaceAll("[\\x00-\\x08\\x0b-\\x0c\\x0e-\\x1f]", "");
		String unicode="GBK";
		
		/**
		 * 特殊券商编码处理
		 */
		SecCompData secCompData = SFUtil.getDataValue(context, SFConst.PUBLIC_SECU);//获取券商信息
		//if(secCompData!=null&&SFConst.SECU_ZHONGJINZQ.equals(secCompData.getSecCompCode())){
		if(secCompData!=null&&SFConst.SECU_ZL.equals(secCompData.getSztFlag())){//所有直联券商都以UTF-8编码
			//// 中金证券
			unicode="UTF-8";
		}
		Document doc = loader.loadXMLDocument(new ByteArrayInputStream(src.toString().trim().getBytes(unicode)));
		
		Node nd = doc.getDocumentElement().getElementsByTagName(this.getName()).item(0);
		
		
		//如果本节点的tag那么和格式中的不匹配，返回
		if (!this.getName().equals(nd.getNodeName().substring(0, this.getName().length()))) {
			SFLogger.debug(context, "can't find tag <" + this.getName() + "> in XML");
			return;
		}
		
		

		//如果有KCollName，先查找并生成KColl
		if (this.getKCollName() != null) {
			try {
				String aDataElementName = SFUtil.getContextValueInAction(context, this.getKCollName());
				if (this.isAppend()) {
					context.getDataElement().setAppend(this.isAppend());
				}
				if (aDataElementName != null && aDataElementName.length() > 0) {
					
					if(context.containsKey(aDataElementName)){
						theKCollElement = context.getDataElement(aDataElementName, this.getAppendClass() == null? KeyedCollection.class :Class.forName(this.getAppendClass()));
					}else{
						theKCollElement = new KeyedCollection(aDataElementName);
						
						theKCollElement.setAppend( true );
						context.addDataElement(theKCollElement);
						
					}
					
					
//					try {
//						theKCollElement = context.getDataElement(aDataElementName, this.getAppendClass() == null? KeyedCollection.class :Class.forName(this.getAppendClass()));
//					}
//					catch (Exception e)
//					{
//						theKCollElement = null;
//					}
				}
				if (theKCollElement != null) {
					//SFLogger.debug(context, "unFormat XMLKCollFormat find KCOLL [" + aDataElementName + "] in context");
				}
				else {
					//SFLogger.debug(context, "unFormat XMLKCollFormat can't find KCOLL [" + aDataElementName + "] in context, , Continue with NULL");					
				}
			}
			catch (Exception e) {
				SFLogger.error(context, "unFormat <XMLKC id=\"" + this.getName() + "\" dataName=\"" + this.getKCollName() + "\"> occur error!");
				throw new EMPFormatException(e);
			}
		}

		//解析XML中本节点的text值，存放到DataName配置中
		if (this.getDataName() != null) {
			String tmpDataName = SFUtil.getContextValueInAction(context, this.getDataName());
			String tmpKCollName = SFUtil.getContextValueInAction(context, this.getKCollName());
			
			if (tmpKCollName != null && tmpDataName != null)
				tmpDataName = tmpKCollName + "." + tmpDataName;
			
			try {
				if (this.isAppend()) {
					context.getDataElement().setAppend(this.isAppend());
				}
				if (tmpDataName != null && tmpDataName.length() > 0) {
					try {
						aDataElement = context.getDataElement(tmpDataName, this.getAppendClass() == null? DataField.class :Class.forName(this.getAppendClass()));
					}
					catch (Exception e)
					{
						aDataElement = null;
					}
				}
				if (aDataElement != null) {
					//SFLogger.debug(context, "unFormat XMLKCollFormat find DataField [" + tmpDataName + "] in context");
					//获取文本内容赋值给DataName字段
					NodeList nodeList = nd.getChildNodes();
					for (int i = 0; i < nodeList.getLength(); i++)
					{
						Node node = nodeList.item( i );
						if (node.getNodeType() == Node.TEXT_NODE && "#text".equals(node.getNodeName()))
						{
							((DataField)aDataElement).setValue(node.getTextContent().trim());
							break;
						}
					}
				}
				else {
					//SFLogger.debug(context, "unFormat XMLKCollFormat can't find DataField [" + tmpDataName + "] in context, , Continue with NULL");					
				}
			}
			catch (Exception e) {
				SFLogger.error(context, "unFormat <XMLKC id=\"" + this.getName() + "\"> occur error!");
				throw new EMPFormatException(e);
			}
		}
		
		
		//解析XML中本节点的属性，存放到propDataName配置的列表中
		NamedNodeMap attrs = nd.getAttributes();
		if (null != this.getPropName() && this.getPropName().length > 0 && (this.getPropName().length == this.getPropDataName().length) && attrs != null) {
			for (int i = 0; i < this.getPropName().length; i++) {
				String proName = this.getPropName()[i];
				String proDataName = this.getPropDataName()[i];
			
				String tmpProName = SFUtil.getContextValueInAction(context, proName);
				String tmpProDataName = SFUtil.getContextValueInAction(context, proDataName);
				String tmpKCollName = SFUtil.getContextValueInAction(context, this.getKCollName());
				
				if (tmpKCollName != null && tmpProDataName != null)
					tmpProDataName = tmpKCollName + "." + tmpProDataName;
				
				try {
					if (this.isAppend()) {
						context.getDataElement().setAppend(this.isAppend());
					}
					if (tmpProDataName != null && tmpProDataName.length() > 0) {
						try {
							aDataElement = context.getDataElement(tmpProDataName, this.getAppendClass() == null? DataField.class :Class.forName(this.getAppendClass()));
						}
						catch (Exception e)
						{
							aDataElement = null;
						}
					}
					if (aDataElement != null) {
						//SFLogger.debug(context, "unFormat XMLKCollFormat find DataField [" + tmpProDataName + "] in context");
						//获取文本内容赋值给DataName字段
						Node attrNode = attrs.getNamedItem(tmpProName);
						if (attrNode != null){
							String valueStr = attrNode.getNodeValue();
							((DataField)aDataElement).setValue(valueStr.trim());
						}
					}
					else {
						//SFLogger.debug(context, "unFormat XMLKCollFormat can't find DataField [" + tmpProDataName + "] in context, , Continue with NULL");					
					}
				}
				catch (Exception e) {
					SFLogger.error(context, "unFormat <XMLKC id=\"" + this.getName() + "\"> occur error!");
					throw new EMPFormatException(e);
				}
			}
		}
		
		//递归解析子节点
		for (int i = 0; i < this.fmtElements.size(); i++) {
			FormatElement element = (FormatElement)fmtElements.get(i);
			Node elementNode = null;
			
			//得到子Node
			elementNode = this.findElementNode(nd, element);
			//如果没有了，下一个格式中的节点
//			if (elementNode == null) {
//				continue;
//			}
			
			if (XMLWrapFormat.class.isAssignableFrom(element.getClass())) {
				XMLWrapFormat aXMLKCFormat = (XMLWrapFormat)element;
				if (aXMLKCFormat.getOpCtx() || this.getKCollName() == null) {
					aXMLKCFormat.unFormat(elementNode, context);
				}
				else {
					aXMLKCFormat.unFormat(theKCollElement, elementNode, context);
				}
			} 
			else if (XMLArrayFormat.class.isAssignableFrom(element.getClass())) {
				XMLArrayFormat aXMLArrayFormat = (XMLArrayFormat)element;
				
				if (aXMLArrayFormat.getOpCtx() || this.getKCollName() == null) {
					aXMLArrayFormat.unFormat(elementNode, context);
				}
				else {
					aXMLArrayFormat.unFormat(theKCollElement, elementNode, context);
				}
			} 
			else if (XMLFieldFormat.class.isAssignableFrom(element.getClass())){
				XMLFieldFormat aXMLFieldFormat = (XMLFieldFormat)element;
				
				if (aXMLFieldFormat.getOpCtx() || this.getKCollName() == null) {
					aXMLFieldFormat.unFormat(elementNode, context);
				}
				else {
					aXMLFieldFormat.unFormat(theKCollElement, elementNode, context);
				}
			}
			else if (XMLHeadFormat.class.isAssignableFrom(element.getClass())){
				continue;
			}
			else {
				SFLogger.error(context, "unFormat failed when unFormat: " + element);
				throw new EMPFormatException("Invalid Format when unFormat: " + this);
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
	public void unFormat(DataElement dataElement, Object src, Context context) throws Exception {
		DataElement aDataElement = null;
		DataElement theKCollElement = dataElement;
		
		
		if (src == null){
			addFormatToContext(dataElement,context);
			return;
		}
		
		
		//SFLogger.debug(context, "prepare to unFormat <" + this.getName() + "\">");
		
		//如果本节点的tag那么和格式中的不匹配，返回
		if (!this.getName().equals(((Node)src).getNodeName().substring(0, this.getName().length()))) {
			SFLogger.debug(context, "can't find tag <" + this.getName() + "> in XML");
			return;
		}

		//如果有KCollName，先查找并生成KColl
		if (this.getKCollName() != null) {
			try {
				String aDataElementName = SFUtil.getContextValueInAction(context, this.getKCollName());
				if (this.isAppend()) {
					dataElement.setAppend(this.isAppend());
				}
				if (aDataElementName != null && aDataElementName.length() > 0) {
					theKCollElement = ((KeyedCollection)dataElement).getDataElement(aDataElementName, this.getAppendClass() == null? KeyedCollection.class :Class.forName(this.getAppendClass()));
//					try {
//						theKCollElement = ((KeyedCollection)dataElement).getDataElement(aDataElementName, this.getAppendClass() == null? KeyedCollection.class :Class.forName(this.getAppendClass()));
//					}
//					catch (Exception e)
//					{
//						theKCollElement = null;
//					}
				}
				if (theKCollElement != null) {
					//SFLogger.debug(context, "unFormat XMLKCollFormat find KCOLL [" + aDataElementName + "] in dataElement " + dataElement.getName());
				}
				else {
					//SFLogger.debug(context, "unFormat XMLKCollFormat can't find KCOLL [" + aDataElementName + "] in dataElement " + dataElement.getName() +", Continue with NULL");	
				}
			}
			catch (Exception e) {
				SFLogger.error(context, "unFormat <XMLKC id=\"" + this.getName() + "\" dataName=\"" + this.getKCollName() + "\"> occur error!");
				throw new EMPFormatException(e);
			}
		}

		//解析XML中本节点的text值，存放到DataName配置中
		if (this.getDataName() != null) {
			String tmpDataName = SFUtil.getContextValueInAction(context, this.getDataName());
			String tmpKCollName = SFUtil.getContextValueInAction(context, this.getKCollName());
			
			if (tmpKCollName != null && tmpDataName != null)
				tmpDataName = tmpKCollName + "." + tmpDataName;
			
			try {
				if (this.isAppend()) {
					dataElement.setAppend(this.isAppend());
				}
				if (tmpDataName != null && tmpDataName.length() > 0) {
					try {
						aDataElement = ((KeyedCollection)dataElement).getDataElement(tmpDataName, this.getAppendClass() == null? DataField.class :Class.forName(this.getAppendClass()));
					}
					catch (Exception e)
					{
						aDataElement = null;
					}
				}
				if (aDataElement != null) {
					//SFLogger.debug(context, "unFormat XMLKCollFormat find DataField [" + tmpDataName + "] in dataElement " + dataElement.getName());
					//获取文本内容赋值给DataName字段
					NodeList nodeList = ((Node)src).getChildNodes();
					for (int i = 0; i < nodeList.getLength(); i++)
					{
						Node node = nodeList.item( i );
						if (node.getNodeType() == Node.TEXT_NODE && "#text".equals(node.getNodeName()))
						{
							((DataField)aDataElement).setValue(node.getTextContent().trim());
							break;
						}
					}
				}
				else {
					//SFLogger.debug(context, "unFormat XMLKCollFormat can't find DataField [" + tmpDataName + "] in dataElement " + dataElement.getName() +", Continue with NULL");					
				}
			}
			catch (Exception e) {
				SFLogger.error(context, "unFormat <XMLKC id=\"" + this.getName() + "\"> occur error!");
				throw new EMPFormatException(e);
			}
		}
		
		
		//解析XML中本节点的属性，存放到propDataName配置的列表中
		NamedNodeMap attrs = ((Node)src).getAttributes();
		if (null != this.getPropName() && this.getPropName().length > 0 && (this.getPropName().length == this.getPropDataName().length) && attrs != null) {
			for (int i = 0; i < this.getPropName().length; i++) {
				String proName = this.getPropName()[i];
				String proDataName = this.getPropDataName()[i];
			
				String tmpProName = SFUtil.getContextValueInAction(context, proName);
				String tmpProDataName = SFUtil.getContextValueInAction(context, proDataName);
				String tmpKCollName = SFUtil.getContextValueInAction(context, this.getKCollName());
				
				if (tmpKCollName != null && tmpProDataName != null)
					tmpProDataName = tmpKCollName + "." + tmpProDataName;
				
				try {
					if (this.isAppend()) {
						dataElement.setAppend(this.isAppend());
					}
					if (tmpProDataName != null && tmpProDataName.length() > 0) {
						try {
							aDataElement = ((KeyedCollection)dataElement).getDataElement(tmpProDataName, this.getAppendClass() == null? DataField.class :Class.forName(this.getAppendClass()));
						}
						catch (Exception e)
						{
							aDataElement = null;
						}
					}
					if (aDataElement != null) {
						//SFLogger.debug(context, "unFormat XMLKCollFormat find DataField [" + tmpProDataName + "] in dataElement " + dataElement.getName());
						//获取文本内容赋值给DataName字段
						Node attrNode = attrs.getNamedItem(tmpProName);
						if (attrNode != null){
							String valueStr = attrNode.getNodeValue();
							((DataField)aDataElement).setValue(valueStr.trim());
						}
					}
					else {
						//SFLogger.debug(context, "unFormat XMLKCollFormat can't find DataField [" + tmpProDataName + "] in dataElement " + dataElement.getName() +", Continue with NULL");					
					}
				}
				catch (Exception e) {
					SFLogger.error(context, "unFormat <XMLKC id=\"" + this.getName() + "\"> occur error!");
					throw new EMPFormatException(e);
				}
			}
		}
		
		//递归解析子节点
		for (int i = 0; i < this.fmtElements.size(); i++) {
			FormatElement element = (FormatElement)fmtElements.get(i);
			Node elementNode = null;
			
			//得到子Node
			elementNode = this.findElementNode((Node)src, element);
			//如果没有了，返回
			if (elementNode == null) {
				continue;
			}
			
			if (XMLWrapFormat.class.isAssignableFrom(element.getClass())) {
				XMLWrapFormat aXMLKCFormat = (XMLWrapFormat)element;
				if (aXMLKCFormat.getOpCtx()) {
					aXMLKCFormat.unFormat(elementNode, context);
				}
				else {
					aXMLKCFormat.unFormat(theKCollElement, elementNode, context);
				}
			} 
			else if (XMLArrayFormat.class.isAssignableFrom(element.getClass())) {
				XMLArrayFormat aXMLArrayFormat = (XMLArrayFormat)element;
				
				if (aXMLArrayFormat.getOpCtx()) {
					aXMLArrayFormat.unFormat(elementNode, context);
				}
				else {
					aXMLArrayFormat.unFormat(theKCollElement, elementNode, context);
				}
			} 
			else if (XMLFieldFormat.class.isAssignableFrom(element.getClass())){
				XMLFieldFormat aXMLFieldFormat = (XMLFieldFormat)element;
				
				if (aXMLFieldFormat.getOpCtx()) {
					aXMLFieldFormat.unFormat(elementNode, context);
				}
				else {
					aXMLFieldFormat.unFormat(theKCollElement, elementNode, context);
				}
			}
			else if (XMLHeadFormat.class.isAssignableFrom(element.getClass())){
				continue;
			}
			else {
				SFLogger.error(context, "unFormat failed when unFormat: " + element);
				throw new EMPFormatException("Invalid Format when unFormat: " + this);
			}
		}
	}
	
	public String toString(int tabCount) {
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < tabCount; i++)
			buf.append("\t");

		buf.append("<XMLKC id=\"").append(this.getName());
		buf.append("\" dataName=\"").append(this.getDataName());
		buf.append("\" opCtx=\"").append(this.getOpCtx());
		buf.append("\" textIndent=\"").append(this.getTextIndent());
		buf.append("\"/>\n");
		
		for (int i = 0; i < this.fmtElements.size(); i++) {
			FormatElement fmt = (FormatElement) fmtElements.get(i);
			buf.append(fmt.toString(tabCount + 1));
			buf.append("\n");
		}

		for (int i = 0; i < tabCount; i++)
			buf.append("\t");
		buf.append("</XMLKC>");

		for (int i = 0; i < this.getDecorators().size(); i++) {
			FormatElement fmt = (FormatElement) getDecorators().get(i);
			buf.append(fmt.toString(tabCount));
			buf.append("\n");

		}
		return buf.toString();
	}
	
	public Node findElementNode(Node node, FormatElement element) 
	{
		
	   String id = element.getName();
	   NodeList nodeList = node.getChildNodes();
	   Node arrayNode = null;
	   boolean cloneFlag = true;
	   if(XMLArrayFormat.class.isAssignableFrom(element.getClass())){
		   for (int i = 0; i < nodeList.getLength(); i++)
		   {
			   Node aNode = nodeList.item( i );
			   if (aNode.getNodeType() == Node.ELEMENT_NODE)
			   {
				   if (id.equals(aNode.getNodeName())){
					   if(cloneFlag){
						   arrayNode = aNode.cloneNode(false);
						   cloneFlag = false;
					   }
					   arrayNode.appendChild(aNode.cloneNode(true));
				   }
					  
			   }
		   }
		   return arrayNode;
	   }else{
		   for (int i = 0; i < nodeList.getLength(); i++)
		   {
			   Node aNode = nodeList.item( i );
			   if (aNode.getNodeType() == Node.ELEMENT_NODE)
			   {
				   if (id.equals(aNode.getNodeName()))
					   return aNode;
			   }
		   }
	   }
	   
	   return null;
	}
	
	
//	public boolean checkElementNode(NodeList nodeList, String id) 
//	{
//	   
//	   int num=0;
//	   for (int i = 0; i < nodeList.getLength(); i++)
//	   {
//		   Node aNode = nodeList.item( i );
//		   if (aNode.getNodeType() == Node.ELEMENT_NODE)
//		   {
//			   if (id.equals(aNode.getNodeName()))
//				   	num++;
//		   }
//	   }
//	   return num>1?true:false;
//	}
	
	
	
	public String getNodeAttributeValue(String attrName, Node node)
	{
	   if( node == null || attrName == null )
		   return null;
	  
	   NamedNodeMap attrs =  node.getAttributes();
	   if (attrs == null)
		   return null;
	   
	   Node attrNode = attrs.getNamedItem(attrName);
	   if (attrNode != null){
		   String valueStr = attrNode.getNodeValue();
		   return valueStr;
	   }
	   else
		   return null;
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
	public void unFormat(Object src, Context context,String type) throws Exception {
		DataElement aDataElement = null, theKCollElement = null;
		if (src == null)
			return;
		
		//SFLogger.debug(context, "prepare to unFormat <" + this.getName() + "\">");
		
		
		
		XMLDocumentLoader loader = new XMLDocumentLoader(); 
		
		Document doc = loader.loadXMLDocument(new ByteArrayInputStream(src.toString().getBytes("GBK")));
		
		
//		NodeList headList = doc.getDocumentElement().getElementsByTagName("Head").item(0).getChildNodes();
//		
//		NodeList bodyList = doc.getDocumentElement().getElementsByTagName("Body").item(0).getChildNodes();
		
		
		Node nd = doc.getDocumentElement().getElementsByTagName(this.getName()).item(0);
		
		
//		ndList.getLength();  ndList.item(0).getChildNodes().item(0).getTextContent()
		
		
		//如果本节点的tag那么和格式中的不匹配，返回
		if (!this.getName().equals(nd.getNodeName().substring(0, this.getName().length()))) {
			SFLogger.debug(context, "can't find tag <" + this.getName() + "> in XML");
			return;
		}
		
		

		//如果有KCollName，先查找并生成KColl
		if (this.getKCollName() != null) {
			try {
				String aDataElementName = SFUtil.getContextValueInAction(context, this.getKCollName());
				if (this.isAppend()) {
					context.getDataElement().setAppend(this.isAppend());
				}
				if (aDataElementName != null && aDataElementName.length() > 0) {
					
					if(context.containsKey(aDataElementName)){
						theKCollElement = context.getDataElement(aDataElementName, this.getAppendClass() == null? KeyedCollection.class :Class.forName(this.getAppendClass()));
					}else{
						theKCollElement = new KeyedCollection(aDataElementName);
						
						theKCollElement.setAppend( true );
						context.addDataElement(theKCollElement);
						
					}
					
					
//					try {
//						theKCollElement = context.getDataElement(aDataElementName, this.getAppendClass() == null? KeyedCollection.class :Class.forName(this.getAppendClass()));
//					}
//					catch (Exception e)
//					{
//						theKCollElement = null;
//					}
				}
				if (theKCollElement != null) {
					//SFLogger.debug(context, "unFormat XMLKCollFormat find KCOLL [" + aDataElementName + "] in context");
				}
				else {
					//SFLogger.debug(context, "unFormat XMLKCollFormat can't find KCOLL [" + aDataElementName + "] in context, , Continue with NULL");					
				}
			}
			catch (Exception e) {
				SFLogger.error(context, "unFormat <XMLKC id=\"" + this.getName() + "\" dataName=\"" + this.getKCollName() + "\"> occur error!");
				throw new EMPFormatException(e);
			}
		}

		//解析XML中本节点的text值，存放到DataName配置中
		if (this.getDataName() != null) {
			String tmpDataName = SFUtil.getContextValueInAction(context, this.getDataName());
			String tmpKCollName = SFUtil.getContextValueInAction(context, this.getKCollName());
			
			if (tmpKCollName != null && tmpDataName != null)
				tmpDataName = tmpKCollName + "." + tmpDataName;
			
			try {
				if (this.isAppend()) {
					context.getDataElement().setAppend(this.isAppend());
				}
				if (tmpDataName != null && tmpDataName.length() > 0) {
					try {
						aDataElement = context.getDataElement(tmpDataName, this.getAppendClass() == null? DataField.class :Class.forName(this.getAppendClass()));
					}
					catch (Exception e)
					{
						aDataElement = null;
					}
				}
				if (aDataElement != null) {
					//SFLogger.debug(context, "unFormat XMLKCollFormat find DataField [" + tmpDataName + "] in context");
					//获取文本内容赋值给DataName字段
					NodeList nodeList = nd.getChildNodes();
					for (int i = 0; i < nodeList.getLength(); i++)
					{
						Node node = nodeList.item( i );
						if (node.getNodeType() == Node.TEXT_NODE && "#text".equals(node.getNodeName()))
						{
							((DataField)aDataElement).setValue(node.getTextContent().trim());
							break;
						}
					}
				}
				else {
					//SFLogger.debug(context, "unFormat XMLKCollFormat can't find DataField [" + tmpDataName + "] in context, , Continue with NULL");					
				}
			}
			catch (Exception e) {
				SFLogger.error(context, "unFormat <XMLKC id=\"" + this.getName() + "\"> occur error!");
				throw new EMPFormatException(e);
			}
		}
		
		
		//解析XML中本节点的属性，存放到propDataName配置的列表中
		NamedNodeMap attrs = nd.getAttributes();
		if (null != this.getPropName() && this.getPropName().length > 0 && (this.getPropName().length == this.getPropDataName().length) && attrs != null) {
			for (int i = 0; i < this.getPropName().length; i++) {
				String proName = this.getPropName()[i];
				String proDataName = this.getPropDataName()[i];
			
				String tmpProName = SFUtil.getContextValueInAction(context, proName);
				String tmpProDataName = SFUtil.getContextValueInAction(context, proDataName);
				String tmpKCollName = SFUtil.getContextValueInAction(context, this.getKCollName());
				
				if (tmpKCollName != null && tmpProDataName != null)
					tmpProDataName = tmpKCollName + "." + tmpProDataName;
				
				try {
					if (this.isAppend()) {
						context.getDataElement().setAppend(this.isAppend());
					}
					if (tmpProDataName != null && tmpProDataName.length() > 0) {
						try {
							aDataElement = context.getDataElement(tmpProDataName, this.getAppendClass() == null? DataField.class :Class.forName(this.getAppendClass()));
						}
						catch (Exception e)
						{
							aDataElement = null;
						}
					}
					if (aDataElement != null) {
						//SFLogger.debug(context, "unFormat XMLKCollFormat find DataField [" + tmpProDataName + "] in context");
						//获取文本内容赋值给DataName字段
						Node attrNode = attrs.getNamedItem(tmpProName);
						if (attrNode != null){
							String valueStr = attrNode.getNodeValue();
							((DataField)aDataElement).setValue(valueStr.trim());
						}
					}
					else {
						//SFLogger.debug(context, "unFormat XMLKCollFormat can't find DataField [" + tmpProDataName + "] in context, , Continue with NULL");					
					}
				}
				catch (Exception e) {
					SFLogger.error(context, "unFormat <XMLKC id=\"" + this.getName() + "\"> occur error!");
					throw new EMPFormatException(e);
				}
			}
		}
		
		//递归解析子节点
		for (int i = 0; i < this.fmtElements.size(); i++) {
			FormatElement element = (FormatElement)fmtElements.get(i);
			Node elementNode = null;
			
			//得到子Node
			elementNode = this.findElementNode(nd, element);
			//如果没有了，下一个格式中的节点
			if (elementNode == null) {
				continue;
			}
			
			if (XMLWrapFormat.class.isAssignableFrom(element.getClass())) {
				XMLWrapFormat aXMLKCFormat = (XMLWrapFormat)element;
				if (aXMLKCFormat.getOpCtx() || this.getKCollName() == null) {
					aXMLKCFormat.unFormat(elementNode, context);
				}
				else {
					aXMLKCFormat.unFormat(theKCollElement, elementNode, context);
				}
			} 
			else if (XMLArrayFormat.class.isAssignableFrom(element.getClass())) {
				XMLArrayFormat aXMLArrayFormat = (XMLArrayFormat)element;
				
				if (aXMLArrayFormat.getOpCtx() || this.getKCollName() == null) {
					aXMLArrayFormat.unFormat(elementNode, context);
				}
				else {
					aXMLArrayFormat.unFormat(theKCollElement, elementNode, context);
				}
			} 
			else if (XMLFieldFormat.class.isAssignableFrom(element.getClass())){
				XMLFieldFormat aXMLFieldFormat = (XMLFieldFormat)element;
				
				if (aXMLFieldFormat.getOpCtx() || this.getKCollName() == null) {
					aXMLFieldFormat.unFormat(elementNode, context);
				}
				else {
					aXMLFieldFormat.unFormat(theKCollElement, elementNode, context);
				}
			}
			else if (XMLHeadFormat.class.isAssignableFrom(element.getClass())){
				continue;
			}
			else {
				SFLogger.error(context, "unFormat failed when unFormat: " + element);
				throw new EMPFormatException("Invalid Format when unFormat: " + this);
			}
		}
	}
	
	
	
	
	public void addFormatToContext(Context context)throws Exception{
		DataElement aDataElement = null, theKCollElement = null;
		
		//SFLogger.debug(context, "prepare to unFormat <" + this.getName() + "\">");
		

		//如果有KCollName，先查找并生成KColl
		if (this.getKCollName() != null) {
			try {
				String aDataElementName = SFUtil.getContextValueInAction(context, this.getKCollName());
				if (this.isAppend()) {
					context.getDataElement().setAppend(this.isAppend());
				}
				if (aDataElementName != null && aDataElementName.length() > 0) {
					
					if(context.containsKey(aDataElementName)){
						theKCollElement = context.getDataElement(aDataElementName, this.getAppendClass() == null? KeyedCollection.class :Class.forName(this.getAppendClass()));
					}else{
						theKCollElement = new KeyedCollection(aDataElementName);
						
						theKCollElement.setAppend( true );
						context.addDataElement(theKCollElement);
						
					}
					
					
//					try {
//						theKCollElement = context.getDataElement(aDataElementName, this.getAppendClass() == null? KeyedCollection.class :Class.forName(this.getAppendClass()));
//					}
//					catch (Exception e)
//					{
//						theKCollElement = null;
//					}
				}
				if (theKCollElement != null) {
					//SFLogger.debug(context, "unFormat XMLKCollFormat find KCOLL [" + aDataElementName + "] in context");
				}
				else {
					//SFLogger.debug(context, "unFormat XMLKCollFormat can't find KCOLL [" + aDataElementName + "] in context, , Continue with NULL");					
				}
			}
			catch (Exception e) {
				SFLogger.error(context, "unFormat <XMLKC id=\"" + this.getName() + "\" dataName=\"" + this.getKCollName() + "\"> occur error!");
				throw new EMPFormatException(e);
			}
		}

		//解析XML中本节点的text值，存放到DataName配置中
		if (this.getDataName() != null) {
			String tmpDataName = SFUtil.getContextValueInAction(context, this.getDataName());
			String tmpKCollName = SFUtil.getContextValueInAction(context, this.getKCollName());
			
			if (tmpKCollName != null && tmpDataName != null)
				tmpDataName = tmpKCollName + "." + tmpDataName;
			
			try {
				if (this.isAppend()) {
					context.getDataElement().setAppend(this.isAppend());
				}
				if (tmpDataName != null && tmpDataName.length() > 0) {
					try {
						aDataElement = context.getDataElement(tmpDataName, this.getAppendClass() == null? DataField.class :Class.forName(this.getAppendClass()));
					}
					catch (Exception e)
					{
						aDataElement = null;
					}
				}
				if (aDataElement != null) {
					//SFLogger.debug(context, "unFormat XMLKCollFormat find DataField [" + tmpDataName + "] in context");
					//获取文本内容赋值给DataName字段
//					NodeList nodeList = nd.getChildNodes();
//					for (int i = 0; i < nodeList.getLength(); i++)
//					{
//						Node node = nodeList.item( i );
//						if (node.getNodeType() == Node.TEXT_NODE && "#text".equals(node.getNodeName()))
//						{
//							((DataField)aDataElement).setValue(node.getTextContent().trim());
//							break;
//						}
//					}
				}
				else {
					//SFLogger.debug(context, "unFormat XMLKCollFormat can't find DataField [" + tmpDataName + "] in context, , Continue with NULL");					
				}
			}
			catch (Exception e) {
				SFLogger.error(context, "unFormat <XMLKC id=\"" + this.getName() + "\"> occur error!");
				throw new EMPFormatException(e);
			}
		}
		
		
//		//解析XML中本节点的属性，存放到propDataName配置的列表中
//		NamedNodeMap attrs = nd.getAttributes();
		if (null != this.getPropName() && this.getPropName().length > 0 && (this.getPropName().length == this.getPropDataName().length) ) {
			for (int i = 0; i < this.getPropName().length; i++) {
				String proName = this.getPropName()[i];
				String proDataName = this.getPropDataName()[i];
			
				String tmpProName = SFUtil.getContextValueInAction(context, proName);
				String tmpProDataName = SFUtil.getContextValueInAction(context, proDataName);
				String tmpKCollName = SFUtil.getContextValueInAction(context, this.getKCollName());
				
				if (tmpKCollName != null && tmpProDataName != null)
					tmpProDataName = tmpKCollName + "." + tmpProDataName;
				
				try {
					if (this.isAppend()) {
						context.getDataElement().setAppend(this.isAppend());
					}
					if (tmpProDataName != null && tmpProDataName.length() > 0) {
						try {
							aDataElement = context.getDataElement(tmpProDataName, this.getAppendClass() == null? DataField.class :Class.forName(this.getAppendClass()));
						}
						catch (Exception e)
						{
							aDataElement = null;
						}
					}
					if (aDataElement != null) {
						//SFLogger.debug(context, "unFormat XMLKCollFormat find DataField [" + tmpProDataName + "] in context");
						//获取文本内容赋值给DataName字段
//						Node attrNode = attrs.getNamedItem(tmpProName);
//						if (attrNode != null){
//							String valueStr = attrNode.getNodeValue();
//							((DataField)aDataElement).setValue(valueStr.trim());
//						}
					}
					else {
						//SFLogger.debug(context, "unFormat XMLKCollFormat can't find DataField [" + tmpProDataName + "] in context, , Continue with NULL");					
					}
				}
				catch (Exception e) {
					SFLogger.error(context, "unFormat <XMLKC id=\"" + this.getName() + "\"> occur error!");
					throw new EMPFormatException(e);
				}
			}
		}
		
		//递归解析子节点
		for (int i = 0; i < this.fmtElements.size(); i++) {
			FormatElement element = (FormatElement)fmtElements.get(i);
			Node elementNode = null;
			
			//得到子Node
//			elementNode = this.findElementNode(nd, element.getName());
//			//如果没有了，下一个格式中的节点
//			if (elementNode == null) {
//				continue;
//			}
			
			if (XMLWrapFormat.class.isAssignableFrom(element.getClass())) {
				XMLWrapFormat aXMLKCFormat = (XMLWrapFormat)element;
				if (aXMLKCFormat.getOpCtx() || this.getKCollName() == null) {
//					aXMLKCFormat.unFormat(elementNode, context);
					
					aXMLKCFormat.addFormatToContext(context);
				}
				else {
//					aXMLKCFormat.unFormat(theKCollElement, elementNode, context);
					
 					aXMLKCFormat.addFormatToContext(theKCollElement,context);
				}
			} 
			else if (XMLArrayFormat.class.isAssignableFrom(element.getClass())) {
				XMLArrayFormat aXMLArrayFormat = (XMLArrayFormat)element;
				
				if (aXMLArrayFormat.getOpCtx() || this.getKCollName() == null) {
					
					aXMLArrayFormat.addFormatToContext(context);
					
//					aXMLArrayFormat.addFormattoContext(context);
				}
				else {
					aXMLArrayFormat.addFormatToContext(theKCollElement);
					
//					aXMLArrayFormat.addFormattoContext(context);
				}
			} 
			else if (XMLFieldFormat.class.isAssignableFrom(element.getClass())){
				XMLFieldFormat aXMLFieldFormat = (XMLFieldFormat)element;
				
				if (aXMLFieldFormat.getOpCtx() || this.getKCollName() == null) {
//					aXMLFieldFormat.unFormat(elementNode, context);
					
					aXMLFieldFormat.addFormatToContext(context);
				}
				else {
//					aXMLFieldFormat.unFormat(theKCollElement, elementNode, context);
					
                    aXMLFieldFormat.addFormatToContext(theKCollElement,context);
				}
			}
			else if (XMLHeadFormat.class.isAssignableFrom(element.getClass())){
				continue;
			}
			else {
				SFLogger.error(context, "unFormat failed when unFormat: " + element);
				throw new EMPFormatException("Invalid Format when unFormat: " + this);
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
	public void addFormatToContext(DataElement dataElement,Context context) throws Exception {
		DataElement aDataElement = null;
		DataElement theKCollElement = dataElement;
		
		

		//如果有KCollName，先查找并生成KColl
		if (this.getKCollName() != null) {
			try {
				String aDataElementName = SFUtil.getContextValueInAction(context, this.getKCollName());
				if (this.isAppend()) {
					dataElement.setAppend(this.isAppend());
				}
				if (aDataElementName != null && aDataElementName.length() > 0) {
					theKCollElement = ((KeyedCollection)dataElement).getDataElement(aDataElementName, this.getAppendClass() == null? KeyedCollection.class :Class.forName(this.getAppendClass()));
//					try {
//						theKCollElement = ((KeyedCollection)dataElement).getDataElement(aDataElementName, this.getAppendClass() == null? KeyedCollection.class :Class.forName(this.getAppendClass()));
//					}
//					catch (Exception e)
//					{
//						theKCollElement = null;
//					}
				}
				if (theKCollElement != null) {
					//SFLogger.debug(context, "unFormat XMLKCollFormat find KCOLL [" + aDataElementName + "] in dataElement " + dataElement.getName());
				}
				else {
					//SFLogger.debug(context, "unFormat XMLKCollFormat can't find KCOLL [" + aDataElementName + "] in dataElement " + dataElement.getName() +", Continue with NULL");	
				}
			}
			catch (Exception e) {
				SFLogger.error(context, "unFormat <XMLKC id=\"" + this.getName() + "\" dataName=\"" + this.getKCollName() + "\"> occur error!");
				throw new EMPFormatException(e);
			}
		}

		//解析XML中本节点的text值，存放到DataName配置中
		if (this.getDataName() != null) {
			String tmpDataName = SFUtil.getContextValueInAction(context, this.getDataName());
			String tmpKCollName = SFUtil.getContextValueInAction(context, this.getKCollName());
			
			if (tmpKCollName != null && tmpDataName != null)
				tmpDataName = tmpKCollName + "." + tmpDataName;
			
			try {
				if (this.isAppend()) {
					dataElement.setAppend(this.isAppend());
				}
				if (tmpDataName != null && tmpDataName.length() > 0) {
					try {
						aDataElement = ((KeyedCollection)dataElement).getDataElement(tmpDataName, this.getAppendClass() == null? DataField.class :Class.forName(this.getAppendClass()));
					}
					catch (Exception e)
					{
						aDataElement = null;
					}
				}
				if (aDataElement != null) {
					//SFLogger.debug(context, "unFormat XMLKCollFormat find DataField [" + tmpDataName + "] in dataElement " + dataElement.getName());
					//获取文本内容赋值给DataName字段
//					NodeList nodeList = ((Node)src).getChildNodes();
//					for (int i = 0; i < nodeList.getLength(); i++)
//					{
//						Node node = nodeList.item( i );
//						if (node.getNodeType() == Node.TEXT_NODE && "#text".equals(node.getNodeName()))
//						{
//							((DataField)aDataElement).setValue(node.getTextContent().trim());
//							break;
//						}
//					}
				}
				else {
					//SFLogger.debug(context, "unFormat XMLKCollFormat can't find DataField [" + tmpDataName + "] in dataElement " + dataElement.getName() +", Continue with NULL");					
				}
			}
			catch (Exception e) {
				SFLogger.error(context, "unFormat <XMLKC id=\"" + this.getName() + "\"> occur error!");
				throw new EMPFormatException(e);
			}
		}
		
		
		if (null != this.getPropName() && this.getPropName().length > 0 && (this.getPropName().length == this.getPropDataName().length) ) {
			for (int i = 0; i < this.getPropName().length; i++) {
				String proName = this.getPropName()[i];
				String proDataName = this.getPropDataName()[i];
			
				String tmpProName = SFUtil.getContextValueInAction(context, proName);
				String tmpProDataName = SFUtil.getContextValueInAction(context, proDataName);
				String tmpKCollName = SFUtil.getContextValueInAction(context, this.getKCollName());
				
				if (tmpKCollName != null && tmpProDataName != null)
					tmpProDataName = tmpKCollName + "." + tmpProDataName;
				
				try {
					if (this.isAppend()) {
						dataElement.setAppend(this.isAppend());
					}
					if (tmpProDataName != null && tmpProDataName.length() > 0) {
						try {
							aDataElement = ((KeyedCollection)dataElement).getDataElement(tmpProDataName, this.getAppendClass() == null? DataField.class :Class.forName(this.getAppendClass()));
						}
						catch (Exception e)
						{
							aDataElement = null;
						}
					}
					if (aDataElement != null) {
						//SFLogger.debug(context, "unFormat XMLKCollFormat find DataField [" + tmpProDataName + "] in dataElement " + dataElement.getName());
						//获取文本内容赋值给DataName字段
//						Node attrNode = attrs.getNamedItem(tmpProName);
//						if (attrNode != null){
//							String valueStr = attrNode.getNodeValue();
//							((DataField)aDataElement).setValue(valueStr.trim());
//						}
					}
					else {
						//SFLogger.debug(context, "unFormat XMLKCollFormat can't find DataField [" + tmpProDataName + "] in dataElement " + dataElement.getName() +", Continue with NULL");					
					}
				}
				catch (Exception e) {
					SFLogger.error(context, "unFormat <XMLKC id=\"" + this.getName() + "\"> occur error!");
					throw new EMPFormatException(e);
				}
			}
		}
		
		//递归解析子节点
		for (int i = 0; i < this.fmtElements.size(); i++) {
			FormatElement element = (FormatElement)fmtElements.get(i);
			
			if (XMLWrapFormat.class.isAssignableFrom(element.getClass())) {
				XMLWrapFormat aXMLKCFormat = (XMLWrapFormat)element;
				if (aXMLKCFormat.getOpCtx()) {
//					aXMLKCFormat.unFormat(elementNode, context);
					
					aXMLKCFormat.addFormatToContext(context);
				}
				else {
//					aXMLKCFormat.unFormat(theKCollElement, elementNode, context);
					
					aXMLKCFormat.addFormatToContext(theKCollElement,context);
				}
			} 
			else if (XMLArrayFormat.class.isAssignableFrom(element.getClass())) {
				XMLArrayFormat aXMLArrayFormat = (XMLArrayFormat)element;
				
				if (aXMLArrayFormat.getOpCtx()) {
					
					aXMLArrayFormat.addFormatToContext(context);
//					aXMLArrayFormat.unFormat(elementNode, context);
				}
				else {
					
					aXMLArrayFormat.addFormatToContext(theKCollElement);
//					aXMLArrayFormat.unFormat(theKCollElement, elementNode, context);
				}
			} 
			else if (XMLFieldFormat.class.isAssignableFrom(element.getClass())){
				XMLFieldFormat aXMLFieldFormat = (XMLFieldFormat)element;
				
				if (aXMLFieldFormat.getOpCtx()) {
//					aXMLFieldFormat.unFormat(elementNode, context);
					
					aXMLFieldFormat.addFormatToContext(context);
				}
				else {
//					aXMLFieldFormat.unFormat(theKCollElement, elementNode, context);
					
					aXMLFieldFormat.addFormatToContext(theKCollElement,context);
				}
			}
			else if (XMLHeadFormat.class.isAssignableFrom(element.getClass())){
				continue;
			}
			else {
				SFLogger.error(context, "unFormat failed when unFormat: " + element);
				throw new EMPFormatException("Invalid Format when unFormat: " + this);
			}
		}
	}
	
	
	public String getKcollName(){
		return this.kCollName;
	}
	
}
