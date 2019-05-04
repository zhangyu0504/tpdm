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
 * <b>����������</b><br>
 * ESB Field��ʽ<br>
 * ���Զ��峤�ȡ����ȡ����͡��հ�����ַ��Ͷ��뷽ʽ<br>
 * 
 * <b>����ʾ����</b><br>
 * &lt;fmtDef id="testID"&gt;<br>
 * &nbsp;&nbsp;&lt;record&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&lt;ESBFieldFormat dataName="myName" len="10" padChar="*" aligment="Left"&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;delim delimChar=";"/&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&lt;/ESBFieldFormat&gt;<br>
 * &nbsp;&nbsp;&lt;/record&gt;<br>
 * &lt;/fmtDef&gt;<br>
 * 
 * <b>����˵����</b><br>
 * <b>dataName</b> EMP����������<br>
 * <b>len</b> �ַ�������<br>
 * <b>padChar</b> �հ�����ַ���Ĭ��Ϊ�ո�<br>
 * <b>aligment</b> ���䷽ʽ��ȡֵΪnone��Ĭ�ϣ���left��right��center��
 * ��ʽ��ʱnone�൱��left������ʽ��ʱȡֵΪnone����ȥ���κοհ�����ַ���
 * 
 * @author ZhongMingChang
 * @modifier LiJia 2006-12-11
 */
public class ESBFieldFormat extends FixedLenFormat{

	//ESB���Ĺ淶�еĳ���
	private int esbLen = 0;

	//ESB���Ĺ淶�еĿ̶�
	private int scale = 0;

	//ESB���Ĺ淶�еı�������,Ĭ�����ַ���
	private String type = "string";
	
	//���ֶ��Ƿ�ֱ�Ӵ�context��ȡֵ��ֱ�Ӹ�ֵ��context�еı���,ESBCDFormat��ʹ�õ��˸ò���
	private boolean opCtx = false;
	
	//����ĸ��ֶε�ֵΪ���Ƿ���Ҫ���ӵ������еı�־
	private boolean nullAppear = false;
	
	//�ǿռ��
	private boolean nullCheck = false;
	
	//Ϊ��ʵ�ֳ���������û�д�ConstantFormat�̳У�����ʵ����ConstanctFormat�Ĺ��ܣ��������������¼�������
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
	 * ������ĵĵ�����ڡ�
	 * <p>
	 * Ӧ��ϵͳ�������������ݱ��������
	 * EMP�����ݱ��ĵ��趨����AtomData���ݶ���
	 * 
	 * @param context ����������
	 * @return AtomData �����ı���
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
			//֧��ESB������ϵ�����ö������ĸ�ʽ
			//Ŀǰ�������֧��ϵͳ���죬��ʱֻ֧��com.sdb.common.format.KeyedFormat
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
				throw new EMPFormatException("��ʱ��֧�ֵ�ESB��ʽ����:" + this);
			}
		}
		
		return aField;
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
			//֧��ESB������ϵ�����ö������ĸ�ʽ
			//Ŀǰ�������֧��ϵͳ���죬��ʱֻ֧��com.sdb.common.format.KeyedFormat
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
				throw new EMPFormatException("��ʱ��֧�ֵ�ESB��ʽ����:" + this);
			}
		}
		
		return aField;
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
						//context.getDataElement().setAppend(this.isAppend());����ı���context�����ԣ��ϵ�
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
			throw new EMPFormatException("��ʱ��֧�ֵ�ESB��ʽ����:" + this);
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
	 * ���ݽ���ĵ�����ڡ�
	 * <p>
	 * �����趨��ʽ�����
	 * 
	 * @param context ����������
	 * @param dataElement ������Ŷ���
	 * @param atomData ���ⱨ��
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
							//((KeyedCollection)dataElement).setAppend(this.isAppend());���ı丸�׵�DataElement������
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
					throw new EMPFormatException("�ֶ�["+this.getLabel()+"]������Ϊ��");
				}
				return;
			}
			
		}else{
			BigDecimal aBigDecimal = null;
			String msgType=aField.getFieldType().getName();
			if (type.equals("string")) {
				//����ʵ�ʱ��������뱨�����ò�һ�£�����ͬһ�ӿڲ�ͬ�������ͱ��ĵ����Ͳ�һ����
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
					throw new EMPFormatException("��ʱ��֧�ֵ�ESB��ʽ����:" + this);
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
				throw new EMPFormatException("��ʱ��֧�ֵ�ESB��ʽ����:" + this);
			}
		}
		
		if(this.nullCheck && SFUtil.isEmpty(sValue)){
			throw new EMPFormatException("�ֶ�["+this.getLabel()+"]������Ϊ��");
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
	 * ����format�����÷��ظ�ʽ�������ڻ�ʱ��
	 * 
	 * @return String //��ʽ�������ڻ�ʱ��
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
	 * ��������תΪ������ʱ�䣩�ַ���
	 * 
	 * @param DataField
	 *            dataField //������
	 * @returns Object //��ʽ������ַ���
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
	 * �������ַ�����Ҫ������ִ����з���ʽ�������������EMP������
	 * 
	 * @param Object src //���봮
	 * @param DataField dataField //������
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
	 * �ַ�����ʽ���Ľ����ڡ�
	 * 
	 * @param src ���ⱨ��(�ַ���)
	 * @param offset ��ʼλ��
	 * @param element ����ģ��(kColl)
	 * @return �������
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
						//context.getDataElement().setAppend(this.isAppend());����ı���context�����ԣ��ϵ�
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
						//((KeyedCollection)dataElement).setAppend(this.isAppend());���ı丸�׵�DataElement������
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
