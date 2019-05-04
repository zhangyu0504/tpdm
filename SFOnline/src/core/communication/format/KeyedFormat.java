package core.communication.format;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import com.ecc.emp.core.Context;
import com.ecc.emp.core.EMPConstance;
import com.ecc.emp.data.DataElement;
import com.ecc.emp.data.KeyedCollection;
import com.ecc.emp.format.EMPFormatException;
import com.ecc.emp.format.FormatElement;
import com.ecc.emp.log.EMPLog;
import common.util.SFUtil;

import core.communication.format.string.FixedLenFormat;

/**
 * 数据结构(kColl)类型数据的格式化处理类。
 * 
 * @author zhongmc
 * @version 2.1
 * @since 1.0 2006-12-4
 * @lastmodified 2008-7-2
 */
public class KeyedFormat extends FormatField {

	/**
	 * 包含的格式化元素
	 */
	protected List fmtElements;

	private FormatField recentElement = null;

	public KeyedFormat() {

		super();
		fmtElements = new ArrayList();
	}

	public KeyedFormat(String name) {
		super(name);
		fmtElements = new ArrayList();
	}

	/**
	 * 二进制格式报文的打包入口。依次对包含的所有格式化元素进行格式化。
	 * 
	 * @param outPut 字节输出流
	 * @param context 交易上下文
	 * @throws Exception
	 */
	public void format(ByteArrayOutputStream outPut, Context context) throws Exception {
		ByteArrayOutputStream out = outPut;
		if (this.getDecorators().size() != 0)
			out = new ByteArrayOutputStream();

		for (int i = 0; i < this.fmtElements.size(); i++) {
			FormatElement element = (FormatElement) fmtElements.get(i);

			if (KeyedFormat.class.isAssignableFrom(element.getClass())) {
				ByteArrayOutputStream bo = new ByteArrayOutputStream();
				((KeyedFormat) element).format(bo, context);
				out.write(bo.toByteArray());
			} else if (IndexedFormat.class.isAssignableFrom(element.getClass())) {

				ByteArrayOutputStream bo = new ByteArrayOutputStream();
				((IndexedFormat) element).format(bo, context);
				out.write(bo.toByteArray());
			} else {
				FormatField field = (FormatField) element;
				field.format(out, context);
			}
		}

		if (this.getDecorators().size() != 0) {
			byte[] outBuf = out.toByteArray();
			outBuf = (byte[]) this.addDecoration(outBuf);
			outPut.write(outBuf);
		}
		return;
	}

	/**
	 * 二进制格式报文的打包入口。依次对包含的所有格式化元素进行格式化。
	 * 
	 * @param outPut 字符串缓存
	 * @param context 交易上下文
	 * @throws Exception
	 */
	public void format(StringBuffer outPut, Context context) throws Exception {
		StringBuffer out = outPut;
		if (this.getDecorators().size() != 0)
			out = new StringBuffer();

		
		DataElement aDataElement = null;
		if (this.getDataName() != null) {
			try {
				String aDataElementName = SFUtil.getContextValueInAction(context, this.getDataName());
				if (aDataElementName != null && aDataElementName.length() > 0)
					aDataElement = context.getDataElement(aDataElementName);
				else
					EMPLog.log("PAESB", EMPLog.WARNING, 0, "format ESBCDFormat, can't find dataElement [" + this.getDataName() + "] in context");
			}
			catch (Exception e) {
				throw new EMPFormatException(e);
			}
		}
		
		
		for (int i = 0; i < this.fmtElements.size(); i++) {
			FormatElement element = (FormatElement) fmtElements.get(i);
			if (KeyedFormat.class.isAssignableFrom(element.getClass())) {
				StringBuffer buf = new StringBuffer();
				
				if(aDataElement!=null){
					((KeyedFormat) element).format(buf, aDataElement);
				}else{
					((KeyedFormat) element).format(buf, context);
				}
				
				
				out.append(buf);
			} else if (IndexedFormat.class.isAssignableFrom(element.getClass())) {

				StringBuffer buf = new StringBuffer();
				
				if(aDataElement!=null){
					((IndexedFormat) element).format(buf, aDataElement);
				}else{
					((IndexedFormat) element).format(buf, context);
				}
				
				out.append(buf);
			} else {
				FormatField field = (FormatField) element;
				
				
				if(aDataElement!=null){
					field.format(out, aDataElement);
				}else{
					field.format(out, context);
				}
				
			}
		}

		if (this.getDecorators().size() != 0) {

			String outStr = out.toString();
			outStr = (String) addDecoration(outStr);
			outPut.append(outStr);
		}
		return;
	}

	/**
	 * 二进制格式报文的打包入口。依次对包含的所有格式化元素进行格式化。
	 * 
	 * @param outPut 字节输出流
	 * @param dataElement 数据模型(kColl)
	 * @throws Exception
	 */
	public void format(ByteArrayOutputStream outPut, DataElement dataElement) throws Exception {
		ByteArrayOutputStream out = outPut;
		if (this.getDecorators().size() != 0)
			out = new ByteArrayOutputStream();

		for (int i = 0; i < this.fmtElements.size(); i++) {
			FormatElement element = (FormatElement) fmtElements.get(i);

			if (KeyedFormat.class.isAssignableFrom(element.getClass())) {
				ByteArrayOutputStream bo = new ByteArrayOutputStream();
				((KeyedFormat) element).format(bo, dataElement);
				out.write(bo.toByteArray());
			} else if (IndexedFormat.class.isAssignableFrom(element.getClass())) {

				ByteArrayOutputStream bo = new ByteArrayOutputStream();
				((IndexedFormat) element).format(bo, dataElement);
				out.write(bo.toByteArray());
			} else {
				FormatField field = (FormatField) element;
				field.format(out, dataElement);
			}
		}

		if (this.getDecorators().size() != 0) {
			byte[] outBuf = out.toByteArray();
			outBuf = (byte[]) addDecoration(outBuf);
			outPut.write(outBuf);
		}
		return;
	}

	/**
	 * 字符串格式报文的打包入口。依次对包含的所有格式化元素进行格式化。
	 * 
	 * @param output 字符串缓存
	 * @param dataElement 数据模型(kColl)
	 * @throws Exception
	 */
	public void format(StringBuffer outPut, DataElement dataElement) throws Exception {
		StringBuffer out = outPut;
		if (this.getDecorators().size() != 0)
			out = new StringBuffer();

		for (int i = 0; i < this.fmtElements.size(); i++) {
			FormatElement element = (FormatElement) fmtElements.get(i);
			if (KeyedFormat.class.isAssignableFrom(element.getClass())) {
				StringBuffer buf = new StringBuffer();
				((KeyedFormat) element).format(buf, dataElement);
				out.append(buf);
			} else if (IndexedFormat.class.isAssignableFrom(element.getClass())) {

				StringBuffer buf = new StringBuffer();
				((IndexedFormat) element).format(buf, dataElement);
				out.append(buf);
			} else {
				FormatField field = (FormatField) element;
				field.format(out, dataElement);
			}
		}

		if (this.getDecorators().size() != 0) {
			String outStr = out.toString();
			outStr = (String) addDecoration(outStr);
			outPut.append(outStr);
		}
		return;
	}

	/**
	 * 二进制格式报文解包入口。依次对包含的所有格式化元素进行反格式化。
	 * 
	 * @param src 待解报文(字节数组)
	 * @param offset 起始位置
	 * @param context 交易上下文
	 * @return 解包长度
	 * @throws Exception
	 */
	public int unformat(byte[] src, int offset, Context context) throws Exception {
		int realLen = src.length - offset;

		int len = 0, retLen = 0;
		for (int i = 0; i < this.fmtElements.size(); i++) {
			FormatElement element = (FormatElement) fmtElements.get(i);

			if (KeyedFormat.class.isAssignableFrom(element.getClass())) {
				retLen = ((KeyedFormat) element).unformat(src, offset + len, context);
			} 
			else if (IndexedFormat.class.isAssignableFrom(element.getClass())) {
				retLen = ((IndexedFormat) element).unformat(src, offset + len, context);
			} 
			else {
				FormatField field = (FormatField) element;
				retLen = field.unformat(src, offset + len, context);
			}

			if (retLen < 0) {
				EMPLog.log(EMPConstance.EMP_FORMAT, EMPLog.ERROR, 0, "Unformat failed when unformat: " + element);
				throw new EMPFormatException("Unformat failed when unformat: " + element);
			}
			len = len + retLen;
			if (len > realLen) {
				EMPLog.log(EMPConstance.EMP_FORMAT, EMPLog.ERROR, 0, "Unformat failed when unformat: " + element);
				throw new EMPFormatException("Invalid Src package when unformat: " + this);
			}

		}
		
		//计算修饰符长度
		retLen = super.extract(src, offset+len);
		if (retLen < 0) {
			EMPLog.log(EMPConstance.EMP_FORMAT, EMPLog.ERROR, 0, "Unformat failed when unformat decorators: " + this);
			throw new EMPFormatException("Unformat failed when unformat: " + this);
		}
		len = len + retLen;
		if (len > realLen) {
			EMPLog.log(EMPConstance.EMP_FORMAT, EMPLog.ERROR, 0, "Unformat failed when unformat: " + this);
			throw new EMPFormatException("Invalid Src package when unformat: " + this);
		}

		return len;
	}

	/**
	 * 字符串格式报文解包入口。依次对包含的所有格式化元素进行反格式化。
	 * 
	 * @param src 待解报文(字符串)
	 * @param offset 起始位置
	 * @param context 交易上下文
	 * @return 解包长度
	 * @throws Exception
	 */
	public int unformat(String src, int offset, Context context) throws Exception {

		int realLen = src.length() - offset;

		int len = 0, retLen = 0;
		DataElement aDataElement = null;
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
					EMPLog.log("PAESB", EMPLog.DEBUG, 0, "unformat ESBCDFormat find KCOLL [" + aDataElementName + "] in context");
				}
				else {
					EMPLog.log("PAESB", EMPLog.WARNING, 0, "unformat ESBCDFormat can't find KCOLL [" + aDataElementName + "] in context, , Continue with NULL");					
				}
			}
			catch (Exception e) {
				EMPLog.log("PAESB", EMPLog.ERROR, 0, "unformat <ESBCD id=\"" + this.getName() + "\" dataName=\"" + this.getDataName() + "\"> occur error!");
				throw new EMPFormatException(e);
			}
		}
		
		
		for (int i = 0; i < this.fmtElements.size(); i++) {
			FormatElement element = (FormatElement) fmtElements.get(i);

			if (KeyedFormat.class.isAssignableFrom(element.getClass())) {
				
				if(aDataElement!=null){
					retLen = ((KeyedFormat) element).unformat(src, offset + len,aDataElement);
				}else{
					retLen = ((KeyedFormat) element).unformat(src, offset + len, context);
				}
				
			} 
			else if (IndexedFormat.class.isAssignableFrom(element.getClass())) {
				
				if(aDataElement!=null){
					retLen = ((IndexedFormat) element).unformat(src, offset + len, aDataElement);
				}else{
					retLen = ((IndexedFormat) element).unformat(src, offset + len, context);
				}
				
			} 
			else {
				FormatField field = (FormatField) element;
				
				if(aDataElement!=null){
					retLen = field.unformat(src, offset + len, aDataElement);
				}else{
					retLen = field.unformat(src, offset + len, context);
				}
				
			}

			if (retLen < 0) {
				EMPLog.log(EMPConstance.EMP_FORMAT, EMPLog.ERROR, 0, "Unformat failed when unformat: " + element, null);
				throw new EMPFormatException("Unformat failed when unformat: " + element);
			}
			len = len + retLen;
			if (len > realLen) {
				EMPLog.log(EMPConstance.EMP_FORMAT, EMPLog.ERROR, 0, "Unformat failed when unformat: " + element, null);
				throw new EMPFormatException("Invalid Src package when unformat: " + this);
			}

		}

		//计算修饰符长度
		retLen = super.extract(src, offset+len);
		if (retLen < 0) {
			EMPLog.log(EMPConstance.EMP_FORMAT, EMPLog.ERROR, 0, "Unformat failed when unformat decorators: " + this);
			throw new EMPFormatException("Unformat failed when unformat decorators: " + this);
		}
		len = len + retLen;
		if (len > realLen) {
			EMPLog.log(EMPConstance.EMP_FORMAT, EMPLog.ERROR, 0, "Unformat failed when unformat decorators: " + this);
			throw new EMPFormatException("Invalid Src package when unformat decorators: " + this);
		}

		return len;
	}

	/**
	 * 二进制格式报文解包入口。依次对包含的所有格式化元素进行反格式化。
	 * 
	 * @param src 待解报文(字节数组)
	 * @param offset 起始位置
	 * @param element 数据模型(kColl)
	 * @return 解包长度
	 * @throws Exception
	 */
	public int unformat(byte[] src, int offset, DataElement dataElement) throws Exception {
		int realLen = src.length - offset;

		int len = 0, retLen = 0;
		for (int i = 0; i < this.fmtElements.size(); i++) {
			FormatElement element = (FormatElement) fmtElements.get(i);

			if (KeyedFormat.class.isAssignableFrom(element.getClass())) {
				retLen = ((KeyedFormat) element).unformat(src, offset + len, dataElement);
			} 
			else if (IndexedFormat.class.isAssignableFrom(element.getClass())) {
				retLen = ((IndexedFormat) element).unformat(src, offset + len, dataElement);
			} 
			else {
				FormatField field = (FormatField) element;
				retLen = field.unformat(src, offset + len, dataElement);
			}

			if (retLen < 0) {
				EMPLog.log(EMPConstance.EMP_FORMAT, EMPLog.ERROR, 0, "Unformat failed when unformat: " + element);
				throw new EMPFormatException("Unformat failed when unformat: " + element);
			}
			len = len + retLen;
			if (len > realLen) {
				EMPLog.log(EMPConstance.EMP_FORMAT, EMPLog.ERROR, 0, "Unformat failed when unformat: " + element);
				throw new EMPFormatException("Invalid Src package when unformat: " + this);
			}

		}
		
		//计算修饰符长度
		retLen = super.extract(src, offset+len);
		if (retLen < 0) {
			EMPLog.log(EMPConstance.EMP_FORMAT, EMPLog.ERROR, 0, "Unformat failed when unformat decorators: " + this);
			throw new EMPFormatException("Unformat failed when unformat: " + this);
		}
		len = len + retLen;
		if (len > realLen) {
			EMPLog.log(EMPConstance.EMP_FORMAT, EMPLog.ERROR, 0, "Unformat failed when unformat: " + this);
			throw new EMPFormatException("Invalid Src package when unformat: " + this);
		}

		return len;
	}

	/**
	 * 字符串格式报文解包入口。依次对包含的所有格式化元素进行反格式化。
	 * 
	 * @param src 待解报文(字符串)
	 * @param offset 起始位置
	 * @param element 数据模型(kColl)
	 * @return 解包长度
	 * @throws Exception
	 */
	public int unformat(String src, int offset, DataElement dataElement) throws Exception {

		int realLen = src.length() - offset;

		int len = 0, retLen = 0;
		for (int i = 0; i < this.fmtElements.size(); i++) {
			FormatElement element = (FormatElement) fmtElements.get(i);

			if (KeyedFormat.class.isAssignableFrom(element.getClass())) {
				retLen = ((KeyedFormat) element).unformat(src, offset + len, dataElement);
			} 
			else if (IndexedFormat.class.isAssignableFrom(element.getClass())) {
				retLen = ((IndexedFormat) element).unformat(src, offset + len, dataElement);
			} 
			else {
				FormatField field = (FormatField) element;
				retLen = field.unformat(src, offset + len, dataElement);
			}

			if (retLen < 0) {
				EMPLog.log(EMPConstance.EMP_FORMAT, EMPLog.ERROR, 0, "Unformat failed when unformat: " + element, null);
				throw new EMPFormatException("Unformat failed when unformat: " + element);
			}
			len = len + retLen;
			if (len > realLen) {
				EMPLog.log(EMPConstance.EMP_FORMAT, EMPLog.ERROR, 0, "Unformat failed when unformat: " + element, null);
				throw new EMPFormatException("Invalid Src package when unformat: " + this);
			}

		}

		//计算修饰符长度
		retLen = super.extract(src, offset+len);
		if (retLen < 0) {
			EMPLog.log(EMPConstance.EMP_FORMAT, EMPLog.ERROR, 0, "Unformat failed when unformat decorators: " + this);
			throw new EMPFormatException("Unformat failed when unformat decorators: " + this);
		}
		len = len + retLen;
		if (len > realLen) {
			EMPLog.log(EMPConstance.EMP_FORMAT, EMPLog.ERROR, 0, "Unformat failed when unformat decorators: " + this);
			throw new EMPFormatException("Invalid Src package when unformat decorators: " + this);
		}

		return len;
	}

	/**
	 * 从源数据src的偏移Offset起，得到本格式化元素需要的数据，返回数据长度。
	 * 
	 * @param src 源报文
	 * @param offset 起始位置
	 * @return int 所需长度
	 * @throws EMPFormatException
	 */
	public int extract(Object src, int offset) throws EMPFormatException {
		int len = 0, aLen = 0;
		for (int i = 0; i < this.fmtElements.size(); i++) {
			FormatElement element = (FormatElement) fmtElements.get(i);
			aLen = element.extract(src, offset+len);	
			if (aLen == -1) 
				return -1;
			len += aLen;
		}		

		aLen = super.extract(src, offset + len);
		if (aLen == -1)
			return -1;
		len += aLen;

		return len;
	}
	
	
	
	/**
	 * 获取 长度。
	 * 
	 * @param src 源报文
	 * @param offset 起始位置
	 * @return int 所需长度
	 * @throws EMPFormatException
	 */
	public int getHeadLen() throws EMPFormatException {
		
		int len = 0;
		for (int i = 0; i < this.fmtElements.size(); i++) {
			FixedLenFormat element = (FixedLenFormat) fmtElements.get(i);
			int aLen = element.getLen();
			len += aLen;
		}		

		return len;
	}
	
	
	
	
//	/**
//	 * 把输入输出放到上下文
//	 * 
//	 * @param src 源报文
//	 * @param offset 起始位置
//	 * @return int 所需长度
//	 * @throws EMPFormatException
//	 */
//	public void putIntoContext(Context context,String kcollName) throws EMPFormatException {
//		
//		KeyedCollection kColl = null; 
//		if(!context.containsKey(kcollName)){
//			kColl = new KeyedCollection(kcollName);
//			try {
//			for (int i = 0; i < this.fmtElements.size(); i++) {
//				
//				FormatField element = (FormatField) fmtElements.get(i);
//				
//				if(element instanceof FixedLenFormat){
//					String name = element.getDataName();
//					kColl.addDataField(name, "");
//				}
//				    
//				
//			}
//				context.addDataElement(kColl);
//			} catch (InvalidArgumentException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (DuplicatedDataNameException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			
//		}
//		
//		
//				
//
//	}
	
	
	public void addFormatToContext(Context context) throws Exception {
		DataElement aDataElement = null;
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
					EMPLog.log("PAESB", EMPLog.DEBUG, 0, "unformat ESBCDFormat find KCOLL [" + aDataElementName + "] in context");
				}
				else {
					EMPLog.log("PAESB", EMPLog.WARNING, 0, "unformat ESBCDFormat can't find KCOLL [" + aDataElementName + "] in context, , Continue with NULL");					
				}
			}
			catch (Exception e) {
				EMPLog.log("PAESB", EMPLog.ERROR, 0, "unformat <ESBCD id=\"" + this.getName() + "\" dataName=\"" + this.getDataName() + "\"> occur error!");
				throw new EMPFormatException(e);
			}
		}
		
		
		for (int i = 0; i < this.fmtElements.size(); i++) {
			FormatElement element = (FormatElement) fmtElements.get(i);

			if (KeyedFormat.class.isAssignableFrom(element.getClass())) {
				
				if(aDataElement!=null){
					((KeyedFormat) element).addFormatToContext(aDataElement);
				}else{
					((KeyedFormat) element).addFormatToContext(context);
				}
				
			} 
			else if (IndexedFormat.class.isAssignableFrom(element.getClass())) {
				
				if(aDataElement!=null){
					((IndexedFormat) element).addFormatToContext(aDataElement);
				}else{
					((IndexedFormat) element).addFormatToContext(context);
				}
				
			} 
			else {
				FormatField field = (FormatField) element;
				
				if(aDataElement!=null){
					field.addFormatToContext(aDataElement);
				}else{
					field.addFormatToContext(context);
				}
				
			}


		}


	}
	
	public void addFormatToContext(DataElement dataElement) throws Exception {

		for (int i = 0; i < this.fmtElements.size(); i++) {
			FormatElement element = (FormatElement) fmtElements.get(i);

			if (KeyedFormat.class.isAssignableFrom(element.getClass())) {
				((KeyedFormat) element).addFormatToContext(dataElement);
			} 
			else if (IndexedFormat.class.isAssignableFrom(element.getClass())) {
				((IndexedFormat) element).addFormatToContext(dataElement);
			} 
			else {
				FormatField field = (FormatField) element;
				field.addFormatToContext(dataElement);
			}


		}

	}
	
	public String getKcollName(){
		return this.getKcollName();
	}

	/**
	 * 添加一个格式化元素。
	 * 
	 * @param field 格式化元素
	 */
	public void addFormatField(FormatField field) {
		this.fmtElements.add(field);
		recentElement = field;
	}

	/**
	 * 添加一个修饰符。
	 * 
	 * @param dectorator 待添加的修饰符
	 */
	public void addDecorator(Decorator dectorator) {
		//	   if( recentElement != null )
		//		   recentElement.addDecorator( dectorator );
		//	   else
		super.addDecorator(dectorator);
	}

	/**
	 * 获得该类的字符串表现。
	 * 
	 * @return 该类的字符串表现
	 */
	public String toString() {
		return toString(0);
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

		buf.append("<record>\n");

		for (int i = 0; i < this.fmtElements.size(); i++) {
			FormatElement fmt = (FormatElement) fmtElements.get(i);
			buf.append(fmt.toString(tabCount + 1));
			buf.append("\n");
		}

		for (int i = 0; i < tabCount; i++)
			buf.append("\t");
		buf.append("</record>");

		for (int i = 0; i < this.getDecorators().size(); i++) {
			FormatElement fmt = (FormatElement) getDecorators().get(i);
			buf.append(fmt.toString(tabCount));
			buf.append("\n");

		}
		return buf.toString();
	}

}
