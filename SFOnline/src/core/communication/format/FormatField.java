package core.communication.format;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.ecc.emp.core.Context;
import com.ecc.emp.data.DataElement;
import com.ecc.emp.data.DataField;
import com.ecc.emp.data.KeyedCollection;
import com.ecc.emp.format.EMPFormatException;
import com.ecc.emp.format.FormatElement;

import common.util.AmtUtil;

import core.service.PBankExpressCalculate;

/**
 * ��Ӧ������ĸ�ʽ�����������ࡣ
 * 
 * @author zhongmc
 * @version 2.1
 * @since 1.0 2006-12-4
 * @lastmodified 2008-7-2
 */
public class FormatField extends PBankFormatElement {

	/**
	 * ��������
	 */
	private String dataName;

	/**
	 * �Ƿ���
	 */
	private boolean constant = false;

	/**
	 * ���Եı���
	 */
	private int multiple = 1;

	/**
	 * ���Եı���
	 */
	private int scale = 0;

	/**
	 * �Ƿ���Ҫת���������
	 */
	private boolean transferAmt = false;

	/**
	 * ��ʽ�����η�
	 */
	private List decorators = new ArrayList(3);

	public FormatField() {
		super();
	}

	public FormatField(String name) {
		super(name);
	}

	/**
	 * �����Ƹ�ʽ���ĵĴ����ڡ�
	 * 
	 * @param out �ֽ������
	 * @param context ����������
	 * @throws Exception
	 */
	public void format(ByteArrayOutputStream out, Context context) throws Exception {
		DataField element = null;
		if (!isConstant()) {
			if (!this.isExpression()){
				element = (DataField) context.getDataElement(getDataName());
			}
			else {
				PBankExpressCalculate aPBankExpCal = new PBankExpressCalculate();
				element = new DataField("EXP_RESULT", aPBankExpCal.execute(getDataName(), context));
			}
		}
		
		element = fbsTransfer(element); 
		
		Object value = this.format(element);
		value = this.addDecoration(value);

		if (value.getClass().isArray()) {
			out.write((byte[]) value);
		}
		else {
			out.write(value.toString().getBytes(this.getEncoding()));
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
	public void format(StringBuffer output, Context context) throws Exception {
		DataField element = null;
		if (!isConstant()) {
			if (!this.isExpression()){
				element = (DataField) context.getDataElement(getDataName());
			}
			else {
				PBankExpressCalculate aPBankExpCal = new PBankExpressCalculate();
				element = new DataField("EXP_RESULT", aPBankExpCal.execute(getDataName(), context));
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
	 * �����Ƹ�ʽ���ĵĴ����ڡ�
	 * 
	 * @param out �ֽ������
	 * @param dataElement ����ģ��(kColl)
	 * @throws Exception
	 */
	public void format(ByteArrayOutputStream out, DataElement dataElement) throws Exception {
		DataField element = null;
		if (!isConstant()) {
			if (!this.isExpression()){
				element = (DataField) ((KeyedCollection) dataElement).getDataElement(getDataName());
			}
			else {
				PBankExpressCalculate aPBankExpCal = new PBankExpressCalculate();
				element = new DataField("EXP_RESULT", aPBankExpCal.execute(getDataName(), (KeyedCollection) dataElement));
			}
		}

		element = fbsTransfer(element);
		
		Object value = this.format(element);
		value = this.addDecoration(value);
		
		if (value.getClass().isArray()) {
			out.write((byte[]) value);
		}
		else {
			out.write(value.toString().getBytes(this.getEncoding()));
		}
		
		return;
	}

	/**
	 * �ַ�����ʽ���ĵĴ����ڡ�
	 * 
	 * @param output �ַ�������
	 * @param dataElement ����ģ��(kColl)
	 * @throws Exception
	 */
	public void format(StringBuffer output, DataElement dataElement) throws Exception {
		DataField element = null;
		if (!isConstant()) {
			if (!this.isExpression()){
				element = (DataField) ((KeyedCollection) dataElement).getDataElement(getDataName());
			}
			else {
				PBankExpressCalculate aPBankExpCal = new PBankExpressCalculate();
				element = new DataField("EXP_RESULT", aPBankExpCal.execute(getDataName(), (KeyedCollection) dataElement));
			}
		}

		element = fbsTransfer(element);
		
		Object value = this.format(element);
		value = this.addDecoration(value);
		if (value.getClass().isArray()) {
			output.append(new String((byte[])value, this.getEncoding()));
		}
		else {
			output.append(String.valueOf(value));
		}

		return;
	}

	/**
	 * �����Ƹ�ʽ���Ľ����ڡ�
	 * 
	 * @param src ���ⱨ��(�ֽ�����)
	 * @param offset ��ʼλ��
	 * @param context ����������
	 * @return �������
	 * @throws Exception
	 */
	public int unformat(byte[] src, int offset, Context context) throws Exception {
		String dataName = getDataName(src);

		DataField field = null;
		if (!isConstant())
			field = (DataField) context.getDataElement(dataName, DataField.class);
		int len = this.extract(src, offset);

		if (len > 0) {
			byte[] dst = new byte[len];
			System.arraycopy(src, offset, dst, 0, len);
			dst = (byte[]) removeDecoration(dst);

			dst = (byte[])fbsUntransfer(dst);
			this.unformat(dst, field);
		}
		return len;
	}

	/**
	 * �ַ�����ʽ���Ľ����ڡ�
	 * 
	 * @param src ���ⱨ��(�ַ���)
	 * @param offset ��ʼλ��
	 * @param context ����������
	 * @return �������
	 * @throws Exception
	 */
	public int unformat(String src, int offset, Context context) throws Exception {
		String dataName = getDataName(src);
		DataField field = null;
		
		if (this.isAppend()) {
			//((KeyedCollection)dataElement).setAppend(this.isAppend());���ı丸�׵�DataElement������
			if (!(context.containsKey(dataName))){
				DataElement theAppendDataElement = (DataElement) (this.getAppendClass() == null? DataField.class :Class.forName(this.getAppendClass())).newInstance();
				theAppendDataElement.setName(dataName);
				theAppendDataElement.setAppend(this.isAppend());
				context.addDataElement(theAppendDataElement);
			}
		}
		
		
		if (!isConstant())
			field = (DataField) context.getDataElement(dataName, DataField.class);
		int len = this.extract(src, offset);

		if (len > 0) {
			String dst = src.substring(offset, offset + len);
			dst = (String) removeDecoration(dst);
			dst = (String)fbsUntransfer(dst);
			this.unformat(dst, field);
		}
		return len;
	}

	/**
	 * �����Ƹ�ʽ���Ľ����ڡ�
	 * 
	 * @param src ���ⱨ��(�ֽ�����)
	 * @param offset ��ʼλ��
	 * @param element ����ģ��(kColl)
	 * @return �������
	 * @throws Exception
	 */
	public int unformat(byte[] src, int offset, DataElement element) throws Exception {

		String dataName = getDataName(src);
		DataField field = null;
		
		if (this.isAppend()) {
			//((KeyedCollection)dataElement).setAppend(this.isAppend());���ı丸�׵�DataElement������
			if (!(((KeyedCollection)element).containsKey(dataName))){
				DataElement theAppendDataElement = (DataElement) (this.getAppendClass() == null? DataField.class :Class.forName(this.getAppendClass())).newInstance();
				theAppendDataElement.setName(dataName);
				theAppendDataElement.setAppend(this.isAppend());
				((KeyedCollection)element).addDataElement(theAppendDataElement);
			}
		}
		
		
		if (!isConstant()) {
			field = (DataField) ((KeyedCollection) element).getDataElement(dataName, DataField.class);
			if (field == null)
				throw new EMPFormatException("FormatField unformat failed! Can't find DataField [" + dataName + "]");
		}

		int len = this.extract(src, offset);

		if (len > 0) {
			byte[] dst = new byte[len];
			System.arraycopy(src, offset, dst, 0, len);
			dst = (byte[]) removeDecoration(dst);
			dst = (byte[])fbsUntransfer(dst);
			this.unformat(dst, field);
		}
		return len;
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
		
		if (this.isAppend()) {
			//((KeyedCollection)dataElement).setAppend(this.isAppend());���ı丸�׵�DataElement������
			if (!(((KeyedCollection)element).containsKey(dataName))){
				DataElement theAppendDataElement = (DataElement) (this.getAppendClass() == null? DataField.class :Class.forName(this.getAppendClass())).newInstance();
				theAppendDataElement.setName(dataName);
				theAppendDataElement.setAppend(this.isAppend());
				((KeyedCollection)element).addDataElement(theAppendDataElement);
			}
		}
		
		
		if (!isConstant()){
			
			field = (DataField) ((KeyedCollection) element).getDataElement(dataName, DataField.class);
			if (field == null)
				throw new EMPFormatException("FormatField unformat failed! Can't find DataField [" + dataName + "]");
		}
			
		int len = this.extract(src, offset);

		if (len > 0) {
			String dst = src.substring(offset, offset + len);
			if(dst!=null){
				dst = dst.trim();
			}
			dst = (String) removeDecoration(dst);
			dst = (String)fbsUntransfer(dst);
			this.unformat(dst, field);
		}
		return len;
	}

	/**
	 * ��Դ����src��ƫ��Offset�𣬵õ�����ʽ��Ԫ����Ҫ�����ݣ��������ݳ��ȡ�
	 * 
	 * @param src Դ����
	 * @param offset ��ʼλ��
	 * @return int ���賤��
	 * @throws EMPFormatException
	 */
	public int extract(Object src, int offset) throws EMPFormatException {
		int len = 0;
		for (int i = 0; i < this.decorators.size(); i++) {
			Decorator decorator = (Decorator) decorators.get(i);
			int aLen = decorator.extract(src, offset + len);
			if (aLen == -1)
				return -1;
			len = len + aLen;
		}
		return len;
	}

	/**
	 * ���Ѹ�ʽ���õ�����src�������Σ���ָ���ȡ�
	 * 
	 * @param src ��ʽ����ı���
	 * @return Object ���κ�ı���
	 */
	public Object addDecoration(Object src) {

		for (int i = 0; i < getDecorators().size(); i++) {
			Decorator decorator = (Decorator) getDecorators().get(i);
			src = decorator.addDecoration(src);
		}

		return src;
	}

	/**
	 * ������Ԫ��ȥ�����Ρ�
	 * 
	 * @param src ����
	 * @return Object ȥ���κ�ı���
	 */
	public Object removeDecoration(Object src) throws EMPFormatException {
		for (int i = getDecorators().size() - 1; i >= 0; i--) {
			Decorator decorator = (Decorator) getDecorators().get(i);
			src = decorator.removeDecoration(src);
		}
		return src;
	}

	/**
	 * �õ����ݸ�ʽ�������ֵ������ֵ�����������Σ���
	 * 
	 * @param dataField ������
	 * @return Object 
	 */
	public Object format(DataField dataField) throws EMPFormatException {
		Object tmpValue = dataField.getValue();
		if (tmpValue == null)
			return "";
		else
			return tmpValue;

	}

	/**
	 * ����ʽ�����������趨���������С�
	 * 
	 * @param src ����
	 * @param dataField ������
	 */
	public void unformat(Object src, DataField dataField) throws EMPFormatException {
		try {
			/* ���Ƿ���Ҫת��Ϊ�����Ʊ��� */
			if (this.isBin()) {
				if (src.getClass().isArray())
					dataField.setValue(src);
				else
					dataField.setValue(((String)src).getBytes(this.getEncoding()));
			}
			else {
				if (src.getClass().isArray())
					dataField.setValue(new String((byte [])src, this.getEncoding()));
				else
					dataField.setValue(src);
			}
		}
		catch (Exception e) {
			throw new EMPFormatException("FormatField unformat failed: invalid charset code!", e);
		}
	}

	/**
	 * �ж��Ƿ���Ҫ���η�������Ҫ�����ڴ��/�����ʱ�������η����д�����
	 * 
	 * @return �Ƿ���Ҫ���η�
	 */
	public boolean isNeedDecorator() {
		if (this.decorators.size() > 0)
			return true;
		return false;
	}

	/**
	 * �ж��Ƿ�Ϊ��������Ϊ�������ڴ��/���ʱ����ȥȡ������
	 * 
	 * @return �Ƿ���
	 */
	public boolean isConstant() {
		return constant;
	}

	/**
	 * ����һ�����η���
	 * 
	 * @param dectorator �����ӵ����η�
	 */
	public void addDecorator(Decorator dectorator) {
		this.decorators.add(dectorator);
	}

	/**
	 * PBank���ת����
	 * 
	 * @param DataField dataField
	 */
	public DataField fbsTransfer(DataField dataField) throws EMPFormatException{
		DataField tmpDataField = null;
		Object value = null;
		
		if (multiple == 1 && !transferAmt)
			return dataField;
		
		if (dataField == null || dataField.getValue() == null)
			return dataField;
		
		tmpDataField = (DataField)dataField.clone();
		value = tmpDataField.getValue();
		try {
			if (value.getClass().isArray()) {
				value = new String((byte [])value, this.getEncoding());
			}
			
			value = ((String) value).trim();
			/*���ֳ��Ա����������ת��*/
			if (multiple != 1) {
				BigDecimal decimal = new BigDecimal((String)value);
				value = decimal.multiply(new BigDecimal(multiple)).setScale(scale, BigDecimal.ROUND_HALF_UP).toString();
			}
			if (transferAmt) {
				value = AmtUtil.transferHostAmt((String)value, "0");
			}
			
			tmpDataField.setValue(value);
		}
		catch (Exception e) {
			throw new EMPFormatException("FormatField fbsTransfer failed!", e);
		}
		
		return tmpDataField;
	}

	/**
	 * PBank���ת����
	 * 
	 * @param DataField dataField
	 */
	public Object fbsUntransfer(Object value) throws EMPFormatException{
		Object tmpValue = value;
		if (multiple == 1 && !transferAmt)
			return value;

		if (value == null)
			return value;
		
		try {
			if (tmpValue.getClass().isArray()) {
				tmpValue = new String((byte [])tmpValue, this.getEncoding());
			}
			
			tmpValue = ((String) tmpValue).trim();
			/*���ֳ��Ա����������ת��*/
			if (transferAmt) {
				tmpValue = AmtUtil.transferHostAmt((String)tmpValue, "1");
			}
			if (multiple != 1) {
				BigDecimal decimal = new BigDecimal((String)tmpValue);
				tmpValue = decimal.divide((new BigDecimal(multiple)), scale, BigDecimal.ROUND_HALF_UP).toString();
			}

			if (value.getClass().isArray()) 
				return ((String)tmpValue).getBytes(this.getEncoding());
			else 
				return tmpValue;
		}
		catch (Exception e) {
			throw new EMPFormatException("FormatField fbsUntransfer failed!", e);
		}
		
	}

	/**
	 * ��øø�ʽ��Ԫ�ض�Ӧ������������
	 * 
	 * @return ��������
	 */
	public String getDataName() {
		return dataName;
	}

	/**
	 * ���ݱ������ݣ���øø�ʽ��Ԫ�ض�Ӧ������������
	 * ����ĳЩ������ɱ���ָ���������Ƶ������
	 * 
	 * @param src ����
	 * @return ��������
	 */
	public String getDataName(Object src) throws EMPFormatException {
		return dataName;
	}

	/**
	 * ���øø�ʽ��Ԫ�ض�Ӧ������������
	 * 
	 * @param dataName ��������
	 */
	public void setDataName(String dataName) {
		this.dataName = dataName;
	}

	/**
	 * ������η��б���
	 * 
	 * @return ���η��б�
	 */
	public List getDecorators() {
		return decorators;
	}

	/**
	 * ��ø�����ַ������֡�
	 * 
	 * @return ������ַ�������
	 */
	public String toString() {
		return toString(0);
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

		buf.append("<fmtField dataName=\"");
		buf.append(getDataName());
		buf.append("\">\n");

		for (int i = 0; i < this.getDecorators().size(); i++) {
			FormatElement fmt = (FormatElement) getDecorators().get(i);
			buf.append(fmt.toString(tabCount));

		}

		return buf.toString();
	}

	/**
	 * �����Ƿ�����
	 * 
	 * @param constant �Ƿ���
	 */
	public void setConstant(boolean constant) {
		this.constant = constant;
	}

	public int getMultiple() {
		return multiple;
	}

	/**
	 * ���ó˵ı�����
	 * 
	 * @param multiple �˵ı���
	 */
	public void setMultiple(int multiple) {
		this.multiple = multiple;
	}

	public boolean isTransferAmt() {
		return transferAmt;
	}

	/**
	 * ����ת���������ı�־��
	 * 
	 * @param transferAmt �Ƿ���Ҫת��
	 */
	public void setTransferAmt(boolean transferAmt) {
		this.transferAmt = transferAmt;
	}

	public int getScale() {
		return scale;
	}

	public void setScale(int scale) {
		this.scale = scale;
	}

	public void addFormatToContext(DataElement element) throws Exception{
		

		if (this.isAppend()) {
			//((KeyedCollection)dataElement).setAppend(this.isAppend());���ı丸�׵�DataElement������
			if (!(((KeyedCollection)element).containsKey(dataName))){
				DataElement theAppendDataElement = (DataElement) (this.getAppendClass() == null? DataField.class :Class.forName(this.getAppendClass())).newInstance();
				theAppendDataElement.setName(dataName);
				theAppendDataElement.setAppend(this.isAppend());
				((KeyedCollection)element).addDataElement(theAppendDataElement);
			}
		}
		
			
	
		
	}

	public void addFormatToContext(Context context) throws Exception{
		
		if (this.isAppend()) {
			//((KeyedCollection)dataElement).setAppend(this.isAppend());���ı丸�׵�DataElement������
			if (!(context.containsKey(dataName))){
				DataElement theAppendDataElement = (DataElement) (this.getAppendClass() == null? DataField.class :Class.forName(this.getAppendClass())).newInstance();
				theAppendDataElement.setName(dataName);
				theAppendDataElement.setAppend(this.isAppend());
				context.addDataElement(theAppendDataElement);
			}
		}
		
	
		
	}
}