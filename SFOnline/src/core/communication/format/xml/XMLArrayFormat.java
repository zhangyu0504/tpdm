package core.communication.format.xml;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ecc.emp.core.Context;
import com.ecc.emp.core.EMPConstance;
import com.ecc.emp.data.DataElement;
import com.ecc.emp.data.DataField;
import com.ecc.emp.data.IndexedCollection;
import com.ecc.emp.data.KeyedCollection;
import com.ecc.emp.format.EMPFormatException;
import com.ecc.emp.format.FormatElement;
import com.ecc.emp.log.EMPLog;
import common.util.SFUtil;

import core.communication.format.FormatField;
import core.communication.format.KeyedFormat;
import core.log.SFLogger;
import core.service.PBankExpressCalculate;

/**
 * ���ݽṹ(kColl)�������ݵĸ�ʽ�������ࡣ
 * 
 * @author zhongmc
 * @version 2.1
 * @since 1.0 2006-12-4
 * @lastmodified 2008-7-2
 */
public class XMLArrayFormat extends KeyedFormat{

	private boolean opCtx = false;

	//����ĸ��ֶε�ֵΪ���Ƿ���Ҫ���ӵ������еı�־
	private boolean nullAppear = true;
	
	//����ĸ��ֶε�ֵΪ���Ƿ��������Ҫ���ӵ������еı�־
	private boolean nullAttrAppear = false;

	//����ĸ��ֶε�ֵΪ���Ƿ���Ҫ��ʾFULL TAG
	private boolean isFullTag = true;

	public void setNullAppear(boolean nullAppear)
	{
		this.nullAppear = nullAppear;
	}
	
	public Boolean isNullAppear()
	{
		return this.nullAppear;
	}

	public XMLArrayFormat() {
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

	private String ICollName;
	
	public String getICollName() {
		return ICollName;
	}

	public void setICollName(String iCollName) {
		ICollName = iCollName;
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

	private int circlePartten = 0;//0��ʾ�ڵ���������ţ�1��ʾ�ڵ�����������ţ��±��0��ʼ
	public int getCirclePartten() {
		return circlePartten;
	}

	public void setCirclePartten(int circlePartten) {
		this.circlePartten = circlePartten;
	}

	/**
	 * ����ʽ��ʱ�Ƿ�׷��ģʽ
	 */
	private boolean clear = false;

	/**
	 * �жϷ���ʽ��ʱ�Ƿ�׷��ģʽ��
	 * 
	 * @return �Ƿ�׷��ģʽ
	 */
	public boolean isClear() {
		return clear;
	}

	/**
	 * ���÷���ʽ��ʱ�Ƿ�׷��ģʽ�������ǣ��򷴸�ʽ��ʱ�����iColl��ǰ���ݡ�
	 * 
	 * @param append �Ƿ�׷��ģʽ
	 */
	public void setClear(boolean clear) {
		this.clear = clear;
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
		}
	}

	//�ع��ú����������ı�����������
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
		super.addFormatField(field);
	}

	/**
	 * ������ĵĵ�����ڡ�
	 * <p>
	 * Ӧ��ϵͳ�������������ݱ��������
	 * EMP�����ݱ��ĵ��趨����AtomData���ݶ���
	 * 
	 * @param context ����������
	 * @return AtomData �����ı���
	 * @throws EMPFormatException
	 */
	public String format(Context context) throws EMPFormatException {
		StringBuffer retStr = new StringBuffer();
		boolean isNodeValueNull = true;
		DataElement theKCollElement = null, theICollElement = null;

		//SFLogger.debug(context, "prepare to format <" + this.getName() + ">");
		//�����KCollName���Ȳ��Ҳ�����KColl
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
				if (theKCollElement != null) {
					SFLogger.debug(context, "format XMLArrayFormat find KCOLL [" + aDataElementName + "] in context");
				}
				else {
					EMPLog.log(EMPConstance.EMP_FORMAT, EMPLog.WARNING, 0, "format XMLArrayFormat can't find KCOLL [" + aDataElementName + "] in context, , Continue with NULL");					
				}
			}
			catch (Exception e) {
				EMPLog.log(EMPConstance.EMP_FORMAT, EMPLog.ERROR, 0, "format <XMLArray id=\"" + this.getName() + "\" dataName=\"" + this.getKCollName() + "\"> occur error!");
				throw new EMPFormatException(e);
			}
		}
		//�����ICollName���Ȳ��Ҳ�����IColl
		if (this.getICollName() != null) {
			try {
				String aDataElementName = SFUtil.getContextValueInAction(context, this.getICollName());
				if (this.isAppend()) {
					context.getDataElement().setAppend(this.isAppend());
				}
				if (aDataElementName != null && aDataElementName.length() > 0) {
					theICollElement = context.getDataElement(aDataElementName, this.getAppendClass() == null? IndexedCollection.class :Class.forName(this.getAppendClass()));
//					try {
//						theICollElement = context.getDataElement(aDataElementName, this.getAppendClass() == null? IndexedCollection.class :Class.forName(this.getAppendClass()));
//					}
//					catch (Exception e)
//					{
//						theICollElement = null;
//					}
				}
				if (theICollElement != null) {
					SFLogger.debug(context, "format XMLArrayFormat find ICOLL [" + aDataElementName + "] in context");
				}
				else {
					EMPLog.log(EMPConstance.EMP_FORMAT, EMPLog.WARNING, 0, "format XMLArrayFormat can't find ICOLL [" + aDataElementName + "] in context, , Continue with NULL");					
				}
			}
			catch (Exception e) {
				EMPLog.log(EMPConstance.EMP_FORMAT, EMPLog.ERROR, 0, "format <XMLArray id=\"" + this.getName() + "\" dataName=\"" + this.getDataName() + "\"> occur error!");
				throw new EMPFormatException(e);
			}
		}
		
//		retStr.append(this.getTextIndent() + "<").append(this.getName());

		//���XML�б��ڵ������
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
						EMPLog.log(EMPConstance.EMP_FORMAT,	EMPLog.ERROR, 0, "XMLIC[" + this + "] format failed!", e);
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
					EMPLog.log(EMPConstance.EMP_FORMAT, EMPLog.ERROR, 0, "format <XMLArray id=\"" + this.getName() + "\"> occur error!");
					throw new EMPFormatException(e);
				}
			}
		}
		
		//�ݹ�ѭ���Ժ���һ��һ��չ��
		StringBuffer subNodeStr = new StringBuffer();
		//�ݹ�����ӽڵ�
		for (int i = 0; i < this.fmtElements.size(); i++) {
			FormatElement element = (FormatElement)fmtElements.get(i);
			
			if (XMLWrapFormat.class.isAssignableFrom(element.getClass())) {
				XMLWrapFormat aXMLKCFormat = (XMLWrapFormat)element;
				if (aXMLKCFormat.isICMember()) {
					//��ICOLL����
					int nCount = ((IndexedCollection)theICollElement).size();
					for (int j = 0; j < nCount; j++) {
						String tagSuffix = null; 

						//��ICOLL�л�ȡÿһ����¼
						DataElement aRecord = ((IndexedCollection)theICollElement).getElementAt(j);
						if (this.circlePartten == 1) {
							tagSuffix = String.valueOf(j);
						}

						if (aXMLKCFormat.getOpCtx()) {
							subNodeStr.append("\r\n").append(aXMLKCFormat.format(context, tagSuffix));
						}
						else {
							subNodeStr.append("\r\n").append(aXMLKCFormat.format(aRecord, context, tagSuffix));
						}
					}
				}
				else {
					//��KColl�ڵ㴦��
					if (aXMLKCFormat.getOpCtx() || this.getKCollName() == null) {
						subNodeStr.append("\r\n").append(aXMLKCFormat.format(context));
					}
					else {
						subNodeStr.append("\r\n").append(aXMLKCFormat.format(theKCollElement, context));
					}
				}
			} 
			else if (XMLFieldFormat.class.isAssignableFrom(element.getClass())){
				XMLFieldFormat aXMLFieldFormat = (XMLFieldFormat)element;
				
				if (aXMLFieldFormat.getIsICMember()) {
					//��ICOLL����
					int nCount = ((IndexedCollection)theICollElement).size();
					for (int j = 0; j < nCount; j++) {
						String tagSuffix = null; 

						//��ICOLL�л�ȡÿһ����¼
						DataElement aRecord = ((IndexedCollection)theICollElement).getElementAt(j);
						if (this.circlePartten == 1) {
							tagSuffix = String.valueOf(j);
						}

						if (aXMLFieldFormat.getOpCtx()) {
							subNodeStr.append("\r\n").append(aXMLFieldFormat.format(context, tagSuffix));
						}
						else {
							subNodeStr.append("\r\n").append(aXMLFieldFormat.format(aRecord, context, tagSuffix));
						}
					}
				}
				else {
					//��KColl�ڵ㴦��
					if (aXMLFieldFormat.getOpCtx() || this.getKCollName() == null) {
						subNodeStr.append("\r\n").append(aXMLFieldFormat.format(context));
					}
					else {
						subNodeStr.append("\r\n").append(aXMLFieldFormat.format(theKCollElement, context));
					}
				}
			}
			else {
				EMPLog.log(EMPConstance.EMP_FORMAT, EMPLog.ERROR, 0, "Format failed when format: " + element);
				throw new EMPFormatException("Invalid Format when format: " + this);
			}
		}

		//���XML�б��ڵ��ֵ
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
//				retStr.append(">" + strBufDataValue + "\r\n" + this.getTextIndent() + "</" + this.getName() + ">");
				retStr.append(strBufDataValue);
			}
			else {
				if (isNodeValueNull) {
					if (this.isNullAppear()) {
						if (this.isFullTag()) {
							retStr.append(">\r\n" + this.getTextIndent() + "</" + this.getName() + ">");
						}
						else {
							retStr.append("/>");
						}
					}
					else {
						//ֵΪ�ղ�չ��
						retStr.setLength(0);
					}
				}
				else {
					if (this.isFullTag()) {
						retStr.append(">\r\n" + this.getTextIndent() + "</" + this.getName() + ">");
					}
					else {
						retStr.append("/>");
					}
				}
			}
		}
		catch (Exception e)  {
			EMPLog.log(EMPConstance.EMP_FORMAT,	EMPLog.ERROR, 0, "XMLArray[" + this + "] format failed!", e);
			throw new EMPFormatException(e);
		}
		

		return retStr.toString();
	}
	
	/**
	 * ������ĵĵ�����ڡ�
	 * <p>
	 * Ӧ��ϵͳ�������������ݱ��������
	 * EMP�����ݱ��ĵ��趨����AtomData���ݶ���
	 * 
	 * @param context ����������
	 * @param dataElement ����Դ
	 * @return AtomData �����ı���
	 * @throws EMPFormatException
	 */
	public String format(DataElement dataElement, Context context) throws EMPFormatException {
		StringBuffer retStr = new StringBuffer();
		boolean isNodeValueNull = true;
		DataElement theKCollElement = dataElement, theICollElement = null;

		//SFLogger.debug(context, "prepare to format <" + this.getName() + ">");
		//�����KCollName���Ȳ��Ҳ�����KColl
		if (this.getKCollName() != null) {
			try {
				String aDataElementName = SFUtil.getContextValueInAction(context, this.getKCollName());
				if (this.isAppend()) {
					context.getDataElement().setAppend(this.isAppend());
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
					SFLogger.debug(context, "format XMLArrayFormat find KCOLL [" + aDataElementName + "] in dataElement" + dataElement.getName());
				}
				else {
					EMPLog.log(EMPConstance.EMP_FORMAT, EMPLog.WARNING, 0, "format XMLArrayFormat can't find KCOLL [" + aDataElementName + "] in dataElement "+ dataElement.getName() + ", Continue with NULL");					
				}
			}
			catch (Exception e) {
				EMPLog.log(EMPConstance.EMP_FORMAT, EMPLog.ERROR, 0, "format <XMLArray id=\"" + this.getName() + "\" dataName=\"" + this.getKCollName() + "\"> occur error!");
				throw new EMPFormatException(e);
			}
		}
		//�����ICollName���Ȳ��Ҳ�����IColl
		if (this.getICollName() != null) {
			try {
				String aDataElementName = SFUtil.getContextValueInAction(context, this.getICollName());
				if (this.isAppend()) {
					context.getDataElement().setAppend(this.isAppend());
				}
				if (aDataElementName != null && aDataElementName.length() > 0) {
					theICollElement = ((KeyedCollection)dataElement).getDataElement(aDataElementName, this.getAppendClass() == null? IndexedCollection.class :Class.forName(this.getAppendClass()));
//					try {
//						theICollElement = ((KeyedCollection)dataElement).getDataElement(aDataElementName, this.getAppendClass() == null? IndexedCollection.class :Class.forName(this.getAppendClass()));
//					}
//					catch (Exception e)
//					{
//						theICollElement = null;
//					}
				}
				if (theICollElement != null) {
					SFLogger.debug(context, "format XMLArrayFormat find ICOLL [" + aDataElementName + "] in dataElement" + dataElement.getName());
				}
				else {
					EMPLog.log(EMPConstance.EMP_FORMAT, EMPLog.WARNING, 0, "format XMLArrayFormat can't find ICOLL [" + aDataElementName + "] in dataElement "+ dataElement.getName() + ", Continue with NULL");					
				}
			}
			catch (Exception e) {
				EMPLog.log(EMPConstance.EMP_FORMAT, EMPLog.ERROR, 0, "format <XMLArray id=\"" + this.getName() + "\" dataName=\"" + this.getDataName() + "\"> occur error!");
				throw new EMPFormatException(e);
			}
		}
		
		retStr.append(this.getTextIndent() + "<").append(this.getName());

		//���XML�б��ڵ������
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
						EMPLog.log(EMPConstance.EMP_FORMAT,	EMPLog.ERROR, 0, "XMLArray[" + this + "] format failed!", e);
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
					EMPLog.log(EMPConstance.EMP_FORMAT, EMPLog.ERROR, 0, "format <XMLArray id=\"" + this.getName() + "\"> occur error!");
					throw new EMPFormatException(e);
				}
			}
		}
		
		//�ݹ�ѭ���Ժ���һ��һ��չ��
		StringBuffer subNodeStr = new StringBuffer();
		//�ݹ�����ӽڵ�
		for (int i = 0; i < this.fmtElements.size(); i++) {
			FormatElement element = (FormatElement)fmtElements.get(i);
			
			if (XMLWrapFormat.class.isAssignableFrom(element.getClass())) {
				XMLWrapFormat aXMLKCFormat = (XMLWrapFormat)element;
				if (aXMLKCFormat.isICMember()) {
					//��ICOLL����
					int nCount = ((IndexedCollection)theICollElement).size();
					for (int j = 0; j < nCount; j++) {
						String tagSuffix = null; 

						//��ICOLL�л�ȡÿһ����¼
						DataElement aRecord = ((IndexedCollection)theICollElement).getElementAt(j);
						if (this.circlePartten == 1) {
							tagSuffix = String.valueOf(j);
						}

						if (aXMLKCFormat.getOpCtx()) {
							subNodeStr.append("\r\n").append(aXMLKCFormat.format(context, tagSuffix));
						}
						else {
							subNodeStr.append("\r\n").append(aXMLKCFormat.format(aRecord, context, tagSuffix));
						}
					}
				}
				else {
					//��KColl�ڵ㴦��
					if (aXMLKCFormat.getOpCtx()) {
						subNodeStr.append("\r\n").append(aXMLKCFormat.format(context));
					}
					else {
						subNodeStr.append("\r\n").append(aXMLKCFormat.format(theKCollElement, context));
					}
				}
			} 
			else if (XMLFieldFormat.class.isAssignableFrom(element.getClass())){
				XMLFieldFormat aXMLFieldFormat = (XMLFieldFormat)element;
				
				if (aXMLFieldFormat.getIsICMember()) {
					//��ICOLL����
					int nCount = ((IndexedCollection)theICollElement).size();
					for (int j = 0; j < nCount; j++) {
						String tagSuffix = null; 

						//��ICOLL�л�ȡÿһ����¼
						DataElement aRecord = ((IndexedCollection)theICollElement).getElementAt(j);
						if (this.circlePartten == 1) {
							tagSuffix = String.valueOf(j);
						}

						if (aXMLFieldFormat.getOpCtx()) {
							subNodeStr.append("\r\n").append(aXMLFieldFormat.format(context, tagSuffix));
						}
						else {
							subNodeStr.append("\r\n").append(aXMLFieldFormat.format(aRecord, context, tagSuffix));
						}
					}
				}
				else {
					//��KColl�ڵ㴦��
					if (aXMLFieldFormat.getOpCtx()) {
						subNodeStr.append("\r\n").append(aXMLFieldFormat.format(context));
					}
					else {
						subNodeStr.append("\r\n").append(aXMLFieldFormat.format(theKCollElement, context));
					}
				}
			}
			else {
				EMPLog.log(EMPConstance.EMP_FORMAT, EMPLog.ERROR, 0, "Format failed when format: " + element);
				throw new EMPFormatException("Invalid Format when format: " + this);
			}
		}

		//���XML�б��ڵ��ֵ
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
				retStr.append(">" + strBufDataValue + "\r\n" + this.getTextIndent() + "</" + this.getName() + ">");
			}
			else {
				if (isNodeValueNull) {
					if (this.isNullAppear()) {
						if (this.isFullTag()) {
							retStr.append(">\r\n" + this.getTextIndent() + "</" + this.getName() + ">");
						}
						else {
							retStr.append("/>");
						}
					}
					else {
						//ֵΪ�ղ�չ��
						retStr.setLength(0);
					}
				}
				else {
					if (this.isFullTag()) {
						retStr.append(">\r\n" + this.getTextIndent() + "</" + this.getName() + ">");
					}
					else {
						retStr.append("/>");
					}
				}
			}
		}
		catch (Exception e)  {
			EMPLog.log(EMPConstance.EMP_FORMAT,	EMPLog.ERROR, 0, "XMLArray[" + this + "] format failed!", e);
			throw new EMPFormatException(e);
		}
		

		return retStr.toString();
	}
		
	/**
	 * �ַ�����ʽ���ĵĴ����ڡ�
	 * 
	 * @param output �ַ�������
	 * @param context ����������
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
	 * �ַ�����ʽ���ĵĴ����ڡ�
	 * 
	 * @param output �ַ�������
	 * @param context ����������
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
	 * ���ݽ���ĵ�����ڡ�
	 * <p>
	 * �����趨��ʽ�����
	 * 
	 * @param context ����������
	 * @param atomData ���ⱨ��
	 * @throws EMPFormatException
	 */
	public void unFormat(Object src, Context context) throws Exception {
		DataElement aDataElement = null, theKCollElement = null, theICollElement = null;
		
		if (src == null){
			addFormatToContext(context);
			return;
		}
		
		//SFLogger.debug(context, "prepare to unformat <" + this.getName() + "\">");
		
		//������ڵ��tag��ô�͸�ʽ�еĲ�ƥ�䣬����
		if (!this.getName().equals(((Node)src).getNodeName())) {
			SFLogger.debug(context, "can't find tag <" + this.getName() + "> in XML");
			return;
		}

		//�����KCollName���Ȳ��Ҳ�����KColl
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
				if (theKCollElement != null) {
					//SFLogger.debug(context, "unformat XMLArrayFormat find KCOLL [" + aDataElementName + "] in context");
				}
				else {
					EMPLog.log(EMPConstance.EMP_FORMAT, EMPLog.WARNING, 0, "unformat XMLArrayFormat can't find KCOLL [" + aDataElementName + "] in context, , Continue with NULL");					
				}
			}
			catch (Exception e) {
				EMPLog.log(EMPConstance.EMP_FORMAT, EMPLog.ERROR, 0, "unformat <XMLArray id=\"" + this.getName() + "\" dataName=\"" + this.getDataName() + "\"> occur error!");
				throw new EMPFormatException(e);
			}
		}

		//����XML�б��ڵ��textֵ����ŵ�DataName������
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
					//SFLogger.debug(context, "unformat XMLArrayFormat find DataField [" + tmpDataName + "] in context");
					//��ȡ�ı����ݸ�ֵ��DataName�ֶ�
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
					EMPLog.log(EMPConstance.EMP_FORMAT, EMPLog.WARNING, 0, "unformat XMLArrayFormat can't find DataField [" + tmpDataName + "] in context, , Continue with NULL");					
				}
			}
			catch (Exception e) {
				EMPLog.log(EMPConstance.EMP_FORMAT, EMPLog.ERROR, 0, "unformat <XMLArray id=\"" + this.getName() + "\"> occur error!");
				throw new EMPFormatException(e);
			}
		}
	
		//����XML�б��ڵ�����ԣ���ŵ�propDataName���õ��б���
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
						//SFLogger.debug(context, "unformat XMLArrayFormat find DataField [" + tmpProDataName + "] in context");
						//��ȡ�ı����ݸ�ֵ��DataName�ֶ�
						Node attrNode = attrs.getNamedItem(tmpProName);
						if (attrNode != null){
							String valueStr = attrNode.getNodeValue();
							((DataField)aDataElement).setValue(valueStr.trim());
						}
					}
					else {
						EMPLog.log(EMPConstance.EMP_FORMAT, EMPLog.WARNING, 0, "unformat XMLArrayFormat can't find DataField [" + tmpProDataName + "] in context, , Continue with NULL");					
					}
				}
				catch (Exception e) {
					EMPLog.log(EMPConstance.EMP_FORMAT, EMPLog.ERROR, 0, "unformat <XMLArray id=\"" + this.getName() + "\"> occur error!");
					throw new EMPFormatException(e);
				}
			}
		}

		//�����ICollName���Ȳ��Ҳ�����IColl
		if (this.getICollName() != null) {
			try {
				String aDataElementName = SFUtil.getContextValueInAction(context, this.getICollName());
				if (this.isAppend()) {
					context.getDataElement().setAppend(this.isAppend());
				}
				if (aDataElementName != null && aDataElementName.length() > 0) {
					theICollElement = context.getDataElement(aDataElementName, this.getAppendClass() == null? IndexedCollection.class :Class.forName(this.getAppendClass()));
//					try {
//						theICollElement = context.getDataElement(aDataElementName, this.getAppendClass() == null? IndexedCollection.class :Class.forName(this.getAppendClass()));
//					}
//					catch (Exception e)
//					{
//						theICollElement = null;
//					}
				}
				if (theICollElement != null) {
					//SFLogger.debug(context, "unformat XMLArrayFormat find ICOLL [" + aDataElementName + "] in context");
				}
				else {
					EMPLog.log(EMPConstance.EMP_FORMAT, EMPLog.WARNING, 0, "unformat XMLArrayFormat can't find ICOLL [" + aDataElementName + "] in context, , Continue with NULL");					
				}
			}
			catch (Exception e) {
				EMPLog.log(EMPConstance.EMP_FORMAT, EMPLog.ERROR, 0, "unformat <XMLArray id=\"" + this.getName() + "\" dataName=\"" + this.getDataName() + "\"> occur error!");
				throw new EMPFormatException(e);
			}
		}
		
		DataElement aRecordTemplate = null;
		if (theICollElement != null) {
			 if (!this.isClear())
				 ((IndexedCollection)theICollElement).clear();
			aRecordTemplate = ((IndexedCollection)theICollElement).getDataElement();
		}

		//�ݹ�����ӽڵ�
		for (int i = 0; i < this.fmtElements.size(); i++) {
			FormatElement element = (FormatElement)fmtElements.get(i);
			
			if (XMLWrapFormat.class.isAssignableFrom(element.getClass())) {
				XMLWrapFormat aXMLKCFormat = (XMLWrapFormat)element;
				if (aXMLKCFormat.isICMember()) {
					//��ICOLL����
					int nCount = 0, nTimes = 0;
					while (true) {
						//��Node�л�ȡÿһ���ڵ�
						String id = aXMLKCFormat.getName();
						if (this.circlePartten == 1) {
							id = id + String.valueOf(nCount);
							nTimes = 0;
						}
						else {
							nTimes = nCount;
						}
						Node node = this.findElementNode((Node)src, id, nTimes);
						if (node == null)
							break;
						
						nCount ++;

						if (aXMLKCFormat.getOpCtx()) {
							aXMLKCFormat.unFormat(node, context);
						}
						else {
							DataElement aRecord = null;
							if (aRecordTemplate != null){
								aRecord = (DataElement)aRecordTemplate.clone();
								aRecord.setName(this.getICollName());
							}
							
							aXMLKCFormat.unFormat(aRecord, node, context);
							if (aRecord != null)
								((IndexedCollection)theICollElement).addDataElement(aRecord);
						}
					}
				}
				else {
					//��KColl�ڵ㴦��
					Node node = this.findElementNode((Node)src, aXMLKCFormat.getName(), 0);
					if (aXMLKCFormat.getOpCtx() || this.getKCollName() == null) {
						aXMLKCFormat.unFormat(node, context);
					}
					else {
						aXMLKCFormat.unFormat(theKCollElement, node, context);
					}
				}
			} 
			else if (XMLFieldFormat.class.isAssignableFrom(element.getClass())){
				XMLFieldFormat aXMLFieldFormat = (XMLFieldFormat)element;
				
				if (aXMLFieldFormat.getIsICMember()) {
					//��ICOLL����
					int nCount = 0, nTimes = 0;
					while (true) {
						//��Node�л�ȡÿһ���ڵ�
						String id = aXMLFieldFormat.getName();
						if (this.circlePartten == 1) {
							id = id + String.valueOf(nCount + 1);
							nTimes = 0;
						}
						else {
							nTimes = nCount;
						}
						Node node = this.findElementNode((Node)src, id, nTimes);
						if (node == null)
							break;
						
						nCount ++;

						if (aXMLFieldFormat.getOpCtx()) {
							aXMLFieldFormat.unFormat(node, context);
						}
						else {
							DataElement aRecord = null;
							if (aRecordTemplate != null) {
								aRecord = (DataElement)aRecordTemplate.clone();
								aRecord.setName(this.getICollName());
							}
							aXMLFieldFormat.unFormat(aRecord, node, context);
							if (aRecord != null)
								((IndexedCollection)theICollElement).addDataElement(aRecord);
						}
					}
				}
				else {
					//��KColl�ڵ㴦��
					Node node = this.findElementNode((Node)src, aXMLFieldFormat.getName(), 0);
					if (aXMLFieldFormat.getOpCtx() || this.getKCollName() == null) {
						aXMLFieldFormat.unFormat(node, context);
					}
					else {
						aXMLFieldFormat.unFormat(theKCollElement, node, context);
					}
				}
			}
			else {
				EMPLog.log(EMPConstance.EMP_FORMAT, EMPLog.ERROR, 0, "UnFormat failed when unformat: " + element);
				throw new EMPFormatException("Invalid Format when unformat: " + this);
			}
		}
	}
	
	/**
	 * ���ݽ���ĵ�����ڡ�
	 * <p>
	 * �����趨��ʽ�����
	 * 
	 * @param context ����������
	 * @param dataElement ������Ŷ���
	 * @param atomData ���ⱨ��
	 * @throws EMPFormatException
	 */
	public void unFormat(DataElement dataElement, Object src, Context context) throws Exception {
		DataElement aDataElement = null, theICollElement = null;
		DataElement theKCollElement = dataElement;
		
		if (src == null){
			addFormatToContext(dataElement);
			return;
		}
		
		
		//SFLogger.debug(context, "prepare to unformat <" + this.getName() + "\">");
		
		//������ڵ��tag��ô�͸�ʽ�еĲ�ƥ�䣬����
		if (!this.getName().equals(((Node)src).getNodeName())) {
			SFLogger.debug(context, "can't find tag <" + this.getName() + "> in XML");
			return;
		}

		//�����KCollName���Ȳ��Ҳ�����KColl
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
				if (theKCollElement != null) {
					//SFLogger.debug(context, "unformat XMLArrayFormat find KCOLL [" + aDataElementName + "] in dataElement " + dataElement.getName());
				}
				else {
					EMPLog.log(EMPConstance.EMP_FORMAT, EMPLog.WARNING, 0, "unformat XMLArrayFormat can't find KCOLL [" + aDataElementName + "] in dataElement " + dataElement.getName() +", Continue with NULL");					
				}
			}
			catch (Exception e) {
				EMPLog.log(EMPConstance.EMP_FORMAT, EMPLog.ERROR, 0, "unformat <XMLArray id=\"" + this.getName() + "\" dataName=\"" + this.getDataName() + "\"> occur error!");
				throw new EMPFormatException(e);
			}
		}

		//����XML�б��ڵ��textֵ����ŵ�DataName������
		if (this.getDataName() != null) {
			String tmpDataName = SFUtil.getContextValueInAction(context, this.getDataName());
			String tmpKCollName = SFUtil.getContextValueInAction(context, this.getKCollName());
			
			if (tmpKCollName != null && tmpDataName != null)
				tmpDataName = tmpKCollName + "." + tmpDataName;
			
			try {
				if (this.isAppend()) {
					((KeyedCollection)dataElement).setAppend(this.isAppend());
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
					//SFLogger.debug(context, "unformat XMLArrayFormat find DataField [" + tmpDataName + "] in dataElement " + dataElement.getName());
					//��ȡ�ı����ݸ�ֵ��DataName�ֶ�
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
					EMPLog.log(EMPConstance.EMP_FORMAT, EMPLog.WARNING, 0, "unformat XMLArrayFormat can't find DataField [" + tmpDataName + "] in dataElement " + dataElement.getName() +", Continue with NULL");					
				}
			}
			catch (Exception e) {
				EMPLog.log(EMPConstance.EMP_FORMAT, EMPLog.ERROR, 0, "unformat <XMLArray id=\"" + this.getName() + "\"> occur error!");
				throw new EMPFormatException(e);
			}
		}

		//����XML�б��ڵ�����ԣ���ŵ�propDataName���õ��б���
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
						//SFLogger.debug(context, "unformat XMLArrayFormat find DataField [" + tmpProDataName + "] in dataElement " + dataElement.getName());
						//��ȡ�ı����ݸ�ֵ��DataName�ֶ�
						Node attrNode = attrs.getNamedItem(tmpProName);
						if (attrNode != null){
							String valueStr = attrNode.getNodeValue();
							((DataField)aDataElement).setValue(valueStr.trim());
						}
					}
					else {
						EMPLog.log(EMPConstance.EMP_FORMAT, EMPLog.WARNING, 0, "unformat XMLArrayFormat can't find DataField [" + tmpProDataName + "] in dataElement " + dataElement.getName() +", Continue with NULL");		
					}
				}
				catch (Exception e) {
					EMPLog.log(EMPConstance.EMP_FORMAT, EMPLog.ERROR, 0, "unformat <XMLArray id=\"" + this.getName() + "\"> occur error!");
					throw new EMPFormatException(e);
				}
			}
		}

		//�����ICollName���Ȳ��Ҳ�����IColl
		if (this.getICollName() != null) {
			try {
				String aDataElementName = SFUtil.getContextValueInAction(context, this.getICollName());
				if (this.isAppend()) {
					context.getDataElement().setAppend(this.isAppend());
				}
				if (aDataElementName != null && aDataElementName.length() > 0) {
					theICollElement = context.getDataElement(aDataElementName, this.getAppendClass() == null? IndexedCollection.class :Class.forName(this.getAppendClass()));
//					try {
//						theICollElement = context.getDataElement(aDataElementName, this.getAppendClass() == null? IndexedCollection.class :Class.forName(this.getAppendClass()));
//					}
//					catch (Exception e)
//					{
//						theICollElement = null;
//					}
				}
				if (theICollElement != null) {
					//SFLogger.debug(context, "unformat XMLArrayFormat find ICOLL [" + aDataElementName + "] in dataElement " + dataElement.getName());
				}
				else {
					EMPLog.log(EMPConstance.EMP_FORMAT, EMPLog.WARNING, 0, "unformat XMLArrayFormat can't find ICOLL [" + aDataElementName + "] in " + dataElement.getName() + ", , Continue with NULL");					
				}
			}
			catch (Exception e) {
				EMPLog.log(EMPConstance.EMP_FORMAT, EMPLog.ERROR, 0, "unformat <XMLArray id=\"" + this.getName() + "\" dataName=\"" + this.getDataName() + "\"> occur error!");
				throw new EMPFormatException(e);
			}
		}
		
		DataElement aRecordTemplate = null;
		if (theICollElement != null) {
			if (!this.isClear()) {
				((IndexedCollection)theICollElement).clear();
			}
			aRecordTemplate = ((IndexedCollection)theICollElement).getDataElement();
		}

		//�ݹ�����ӽڵ�
		for (int i = 0; i < this.fmtElements.size(); i++) {
			FormatElement element = (FormatElement)fmtElements.get(i);
			
			if (XMLWrapFormat.class.isAssignableFrom(element.getClass())) {
				XMLWrapFormat aXMLKCFormat = (XMLWrapFormat)element;
				if (aXMLKCFormat.isICMember()) {
					//��ICOLL����
					int nCount = 0, nTimes = 0;
					while (true) {
						//��Node�л�ȡÿһ���ڵ�
						String id = aXMLKCFormat.getName();
						if (this.circlePartten == 1) {
							id = id + String.valueOf(nCount + 1);
							nTimes = 0;
						}
						else {
							nTimes = nCount;
						}
						Node node = this.findElementNode((Node)src, id, nTimes);
						if (node == null)
							break;
						
						nCount ++;

						if (aXMLKCFormat.getOpCtx()) {
							aXMLKCFormat.unFormat(node, context);
						}
						else {
							DataElement aRecord = null;
							if (aRecordTemplate != null) {
								aRecord = (DataElement)aRecordTemplate.clone();
								aRecord.setName(this.getICollName());
							}
							
							aXMLKCFormat.unFormat(aRecord, node, context);
							if (aRecord != null)
								((IndexedCollection)theICollElement).addDataElement(aRecord);
						}
					}
				}
				else {
					//��KColl�ڵ㴦��
					Node node = this.findElementNode((Node)src, aXMLKCFormat.getName(), 0);
					if (aXMLKCFormat.getOpCtx()) {
						aXMLKCFormat.unFormat(node, context);
					}
					else {
						aXMLKCFormat.unFormat(theKCollElement, node, context);
					}
				}
			} 
			else if (XMLFieldFormat.class.isAssignableFrom(element.getClass())){
				XMLFieldFormat aXMLFieldFormat = (XMLFieldFormat)element;
				
				if (aXMLFieldFormat.getIsICMember()) {
					//��ICOLL����
					int nCount = 0, nTimes = 0;
					while (true) {
						//��Node�л�ȡÿһ���ڵ�
						String id = aXMLFieldFormat.getName();
						if (this.circlePartten == 1) {
							id = id + String.valueOf(nCount + 1);
							nTimes = 0;
						}
						else {
							nTimes = nCount;
						}
						Node node = this.findElementNode((Node)src, id, nTimes);
						if (node == null)
							break;
						
						nCount ++;

						if (aXMLFieldFormat.getOpCtx()) {
							aXMLFieldFormat.unFormat(node, context);
						}
						else {
							DataElement aRecord = null;
							if (aRecordTemplate != null) {
								aRecord = (DataElement)aRecordTemplate.clone();
								aRecord.setName(this.getICollName());
							}
							aXMLFieldFormat.unFormat(aRecord, node, context);
							if (aRecord != null)
								((IndexedCollection)theICollElement).addDataElement(aRecord);
						}
					}
				}
				else {
					//��KColl�ڵ㴦��
					Node node = this.findElementNode((Node)src, aXMLFieldFormat.getName(), 0);
					if (aXMLFieldFormat.getOpCtx()) {
						aXMLFieldFormat.unFormat(node, context);
					}
					else {
						aXMLFieldFormat.unFormat(theKCollElement, node, context);
					}
				}
			}
			else {
				EMPLog.log(EMPConstance.EMP_FORMAT, EMPLog.ERROR, 0, "UnFormat failed when unformat: " + element);
				throw new EMPFormatException("Invalid Format when unformat: " + this);
			}
		}
	}

	
	
	public void addFormatToContext(Context context) throws Exception {
		DataElement aDataElement = null, theKCollElement = null, theICollElement = null;
		

		//�����KCollName���Ȳ��Ҳ�����KColl
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
				if (theKCollElement != null) {
					//SFLogger.debug(context, "unformat XMLArrayFormat find KCOLL [" + aDataElementName + "] in context");
				}
				else {
					EMPLog.log(EMPConstance.EMP_FORMAT, EMPLog.WARNING, 0, "unformat XMLArrayFormat can't find KCOLL [" + aDataElementName + "] in context, , Continue with NULL");					
				}
			}
			catch (Exception e) {
				EMPLog.log(EMPConstance.EMP_FORMAT, EMPLog.ERROR, 0, "unformat <XMLArray id=\"" + this.getName() + "\" dataName=\"" + this.getDataName() + "\"> occur error!");
				throw new EMPFormatException(e);
			}
		}

		//����XML�б��ڵ��textֵ����ŵ�DataName������
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
					
				}
				else {
					EMPLog.log(EMPConstance.EMP_FORMAT, EMPLog.WARNING, 0, "unformat XMLArrayFormat can't find DataField [" + tmpDataName + "] in context, , Continue with NULL");					
				}
			}
			catch (Exception e) {
				EMPLog.log(EMPConstance.EMP_FORMAT, EMPLog.ERROR, 0, "unformat <XMLArray id=\"" + this.getName() + "\"> occur error!");
				throw new EMPFormatException(e);
			}
		}
	
		//�����ICollName���Ȳ��Ҳ�����IColl
		if (this.getICollName() != null) {
			try {
				String aDataElementName = SFUtil.getContextValueInAction(context, this.getICollName());
				if (this.isAppend()) {
					context.getDataElement().setAppend(this.isAppend());
				}
				if (aDataElementName != null && aDataElementName.length() > 0) {
					theICollElement = context.getDataElement(aDataElementName, this.getAppendClass() == null? IndexedCollection.class :Class.forName(this.getAppendClass()));
//					try {
//						theICollElement = context.getDataElement(aDataElementName, this.getAppendClass() == null? IndexedCollection.class :Class.forName(this.getAppendClass()));
//					}
//					catch (Exception e)
//					{
//						theICollElement = null;
//					}
				}
				if (theICollElement != null) {
					//SFLogger.debug(context, "unformat XMLArrayFormat find ICOLL [" + aDataElementName + "] in context");
				}
				else {
					EMPLog.log(EMPConstance.EMP_FORMAT, EMPLog.WARNING, 0, "unformat XMLArrayFormat can't find ICOLL [" + aDataElementName + "] in context, , Continue with NULL");					
				}
			}
			catch (Exception e) {
				EMPLog.log(EMPConstance.EMP_FORMAT, EMPLog.ERROR, 0, "unformat <XMLArray id=\"" + this.getName() + "\" dataName=\"" + this.getDataName() + "\"> occur error!");
				throw new EMPFormatException(e);
			}
		}
		
		DataElement aRecordTemplate = null;
		if (theICollElement != null) {
			 if (!this.isClear())
				 ((IndexedCollection)theICollElement).clear();
			aRecordTemplate = ((IndexedCollection)theICollElement).getDataElement();
		}

		//�ݹ�����ӽڵ�
		for (int i = 0; i < this.fmtElements.size(); i++) {
			FormatElement element = (FormatElement)fmtElements.get(i);
			
			if (XMLWrapFormat.class.isAssignableFrom(element.getClass())) {
				XMLWrapFormat aXMLKCFormat = (XMLWrapFormat)element;
				DataElement aRecord = null;
				if (aRecordTemplate != null) {
					aRecord = (DataElement)aRecordTemplate.clone();
					aRecord.setName(this.getICollName());
				}
				
				aXMLKCFormat.addFormatToContext(aRecord, context);
				if (aRecord != null)
					((IndexedCollection)theICollElement).addDataElement(aRecord);
			} 
			else if (XMLFieldFormat.class.isAssignableFrom(element.getClass())){
				
			}
			else {
				EMPLog.log(EMPConstance.EMP_FORMAT, EMPLog.ERROR, 0, "UnFormat failed when unformat: " + element);
				throw new EMPFormatException("Invalid Format when unformat: " + this);
			}
		}
	}
	
		/**
	 * ��ø�����ַ������֡�
	 * 
	 * @param tabCount ������
	 * @return ������ַ�������
	 */
	public String toString(int tabCount) {
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < tabCount; i++)
			buf.append("\t");

		buf.append("<XMLArray id=\"").append(this.getName());
		buf.append("\" dataName=\"").append(this.getDataName());
		buf.append("\" opCtx=\"").append(String.valueOf(this.getOpCtx()));
		buf.append("\" textIndent=\"").append(this.getTextIndent());
		buf.append("\">\n");

		buf.append(getFormatElement().toString(tabCount + 1));

		buf.append("\n");
		for (int i = 0; i < tabCount; i++)
			buf.append("\t");
		buf.append("</XMLArray>\n");

		for (int i = 0; i < this.getDecorators().size(); i++) {
			FormatElement fmt = (FormatElement) getDecorators().get(i);
			buf.append(fmt.toString(tabCount));
			buf.append("\n");
		}

		return buf.toString();
	}

	public Node findElementNode(Node node, String id, int times) 
	{
		int nCount = 0;
		NodeList nodeList = node.getChildNodes();

		for (int i = 0; i < nodeList.getLength(); i++)
		{
			Node aNode = nodeList.item(i);
			if (aNode.getNodeType() == Node.ELEMENT_NODE)
			{
				if (id.equals(aNode.getNodeName()))
					nCount ++;
				
				if (nCount > times)
					return aNode;
			}
		}
		return null;
	}
}