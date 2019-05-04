package core.communication.format;

import java.io.ByteArrayOutputStream;

import com.ecc.emp.core.Context;
import com.ecc.emp.core.EMPConstance;
import com.ecc.emp.core.EMPException;
import com.ecc.emp.data.DataElement;
import com.ecc.emp.data.IndexedCollection;
import com.ecc.emp.data.KeyedCollection;
import com.ecc.emp.format.EMPFormatException;
import com.ecc.emp.format.FormatElement;
import com.ecc.emp.log.EMPLog;

/**
 * 数据集合(iColl)类型数据的格式化处理类。
 * 
 * @author zhongmc
 * @version 2.1
 * @since 1.0 2006-12-4
 * @lastmodified 2008-7-2
 */
public class IndexedFormat extends FormatField {

	/**
	 * 反格式化时是否追加模式
	 */
	private boolean clear = false;

	/**
	 * 记录数的长度
	 */
	private int countLen = 0;

	public IndexedFormat() {

	}

	public Object format(DataElement dataElement) {
		return null;
	}

	public void unformat(Object src, DataElement dataElement) throws EMPException {

	}

	/**
	 * 二进制格式报文的打包入口。用内含的格式化元素(kColl)依次对iColl中的每条记录进行格式化。
	 * 
	 * @param outPut 字节输出流
	 * @param context 交易上下文
	 * @throws Exception
	 */
	public void format(ByteArrayOutputStream outPut, Context context) throws Exception {
		ByteArrayOutputStream out = outPut;
		if (this.getDecorators().size() != 0)
			out = new ByteArrayOutputStream();

		IndexedCollection iColl = (IndexedCollection) context.getDataElement(getDataName());
		if (countLen != 0) {
			out.write(String.format("%0" + countLen + "d", iColl.size()).getBytes());
		}
		
		FormatElement formatElement = this.getFormatElement();
		for (int i = 0; i < iColl.size(); i++) {
			DataElement element = iColl.getElementAt(i);
			formatElement.format(out, element);
		}

		if (this.getDecorators().size() != 0) {
			byte[] outBuf = out.toByteArray();
			for (int i = 0; i < getDecorators().size(); i++) {
				Decorator decorator = (Decorator) getDecorators().get(i);
				outBuf = (byte[]) decorator.addDecoration(outBuf);
			}
			outPut.write(outBuf);
		}

		return;
	}

	/**
	 * 字符串格式报文的打包入口。用内含的格式化元素(kColl)依次对iColl中的每条记录进行格式化。
	 * 
	 * @param output 字符串缓存
	 * @param context 交易上下文
	 * @throws Exception
	 */
	public void format(StringBuffer outPut, Context context) throws Exception {
		StringBuffer out = outPut;
		if (this.getDecorators().size() != 0)
			out = new StringBuffer();

		IndexedCollection iColl = (IndexedCollection) context.getDataElement(getDataName());
		if (countLen != 0) {
			out.append(String.format("%0" + countLen + "d", iColl.size()));
		}
		
		FormatElement formatElement = this.getFormatElement();
		for (int i = 0; i < iColl.size(); i++) {
			DataElement element = iColl.getElementAt(i);
			formatElement.format(out, element);
		}

		if (this.getDecorators().size() != 0) {
			String outStr = out.toString();
			for (int i = 0; i < getDecorators().size(); i++) {
				Decorator decorator = (Decorator) getDecorators().get(i);
				outStr = (String) decorator.addDecoration(outStr);
			}
			outPut.append(outStr);
		}
		return;
	}

	/**
	 * 二进制格式报文的打包入口。用内含的格式化元素(kColl)依次对iColl中的每条记录进行格式化。
	 * 
	 * @param outPut 字节输出流
	 * @param dataElement 数据模型(kColl)
	 * @throws Exception
	 */
	public void format(ByteArrayOutputStream outPut, DataElement element) throws Exception {
		ByteArrayOutputStream out = outPut;
		if (this.getDecorators().size() != 0)
			out = new ByteArrayOutputStream();

		KeyedCollection kColl = (KeyedCollection) element;
		IndexedCollection iColl = (IndexedCollection) kColl.getDataElement(getDataName());
		if (countLen != 0) {
			out.write(String.format("%0" + countLen + "d", iColl.size()).getBytes());
		}
		FormatElement formatElement = this.getFormatElement();
		for (int i = 0; i < iColl.size(); i++) {
			DataElement aElement = iColl.getElementAt(i);
			formatElement.format(out, aElement);
		}

		if (this.getDecorators().size() != 0) {
			byte[] outBuf = out.toByteArray();
			for (int i = 0; i < getDecorators().size(); i++) {
				Decorator decorator = (Decorator) getDecorators().get(i);
				outBuf = (byte[]) decorator.addDecoration(outBuf);
			}

			outPut.write(outBuf);
		}

		return;
	}

	/**
	 * 字符串格式报文的打包入口。用内含的格式化元素(kColl)依次对iColl中的每条记录进行格式化。
	 * 
	 * @param output 字符串缓存
	 * @param dataElement 数据模型(kColl)
	 * @throws Exception
	 */
	public void format(StringBuffer outPut, DataElement element) throws Exception {
		StringBuffer out = outPut;
		if (this.getDecorators().size() != 0)
			out = new StringBuffer();

		KeyedCollection kColl = (KeyedCollection) element;

		IndexedCollection iColl = (IndexedCollection) kColl.getDataElement(getDataName());
		if (countLen != 0) {
			out.append(String.format("%0" + countLen + "d", iColl.size()));
		}
		
		FormatElement formatElement = this.getFormatElement();
		for (int i = 0; i < iColl.size(); i++) {
			DataElement aelement = iColl.getElementAt(i);
			formatElement.format(out, aelement);
		}

		if (this.getDecorators().size() != 0) {
			String outStr = out.toString();
			for (int i = 0; i < getDecorators().size(); i++) {
				Decorator decorator = (Decorator) getDecorators().get(i);
				outStr = (String) decorator.addDecoration(outStr);
			}
			outPut.append(outStr);
		}
		return;
	}

	/**
	 * 二进制格式报文解包入口。用内含的格式化元素(kColl)对报文中的多条记录进行反格式化，依次放入iColl中。
	 * 
	 * @param src 待解报文(字节数组)
	 * @param offset 起始位置
	 * @param context 交易上下文
	 * @return 解包长度
	 * @throws Exception
	 */
	public int unformat(byte[] src, int offset, Context context) throws Exception {
		int len = 0, rows = 0, aLen = 0;

		int realLen = src.length - offset;

		if (countLen != 0) {
			len += countLen;
			rows = Integer.parseInt(new String(src, offset, countLen));
		}

		IndexedCollection iColl = (IndexedCollection) context.getDataElement(getDataName(), IndexedCollection.class);
		if (!clear)
			iColl.clear();
		
		KeyedCollection kColl = (KeyedCollection) iColl.getDataElement();

		FormatElement formatElement = this.getFormatElement();
		while (((countLen != 0 && rows > 0)) || ((countLen == 0) && len < realLen)) {
			if (countLen != 0)
				rows--;
			
			KeyedCollection aColl = (KeyedCollection) kColl.clone();
			iColl.addDataElement(aColl);
			
			aLen = formatElement.unformat(src, offset + len, aColl);
			if (aLen < 0) {
				EMPLog.log(EMPConstance.EMP_FORMAT, EMPLog.ERROR, 0, "Unformat failed when unformat : " + formatElement);
				throw new EMPFormatException("Unformat failed when unformat : " + formatElement);
			}
			len = len + aLen;
		}
		
		//计算修饰符长度
		aLen = super.extract(src, offset+len);
		if (aLen < 0) {
			EMPLog.log(EMPConstance.EMP_FORMAT, EMPLog.ERROR, 0, "Unformat failed when unformat decorators: " + this);
			throw new EMPFormatException("Unformat failed when unformat decorators: " + this);
		}
		len = len + aLen;
		if (len > realLen) {
			EMPLog.log(EMPConstance.EMP_FORMAT, EMPLog.ERROR, 0, "Unformat failed when unformat decorators: " + this);
			throw new EMPFormatException("Invalid Src package when unformat decorators: " + this);
		}
		
		return len;
	}

	/**
	 * 二进制格式报文解包入口。用内含的格式化元素(kColl)对报文中的多条记录进行反格式化，依次放入iColl中。
	 * 
	 * @param src 待解报文(字节数组)
	 * @param offset 起始位置
	 * @param dataElement 数据模型(kColl)
	 * @return 解包长度
	 * @throws Exception
	 */
	public int unformat(byte[] src, int offset, DataElement dataElement) throws Exception {
		int len = 0, rows = 0, aLen = 0;

		int realLen = src.length - offset;

		if (countLen != 0) {
			len += countLen;
			rows = Integer.parseInt(new String(src, offset, countLen));
		}

		KeyedCollection akColl = (KeyedCollection) dataElement;
		IndexedCollection iColl = (IndexedCollection) akColl.getDataElement(getDataName(), IndexedCollection.class);
		if (!clear)
			iColl.clear();
		
		KeyedCollection kColl = (KeyedCollection) iColl.getDataElement();

		FormatElement formatElement = this.getFormatElement();
		while (((countLen != 0 && rows > 0)) || ((countLen == 0) && len < realLen)) {
			if (countLen != 0)
				rows--;
			
			KeyedCollection aColl = (KeyedCollection) kColl.clone();
			iColl.addDataElement(aColl);

			aLen = formatElement.unformat(src, offset + len, aColl);
			if (aLen < 0) {
				EMPLog.log(EMPConstance.EMP_FORMAT, EMPLog.ERROR, 0, "Unformat failed when unformat : " + formatElement);
				throw new EMPFormatException("Unformat failed when unformat : " + formatElement);
			}
			len = len + aLen;
		}
		
		//计算修饰符长度
		aLen = super.extract(src, offset+len);
		if (aLen < 0) {
			EMPLog.log(EMPConstance.EMP_FORMAT, EMPLog.ERROR, 0, "Unformat failed when unformat decorators: " + this);
			throw new EMPFormatException("Unformat failed when unformat decorators: " + this);
		}
		len = len + aLen;
		if (len > realLen) {
			EMPLog.log(EMPConstance.EMP_FORMAT, EMPLog.ERROR, 0, "Unformat failed when unformat decorators: " + this);
			throw new EMPFormatException("Invalid Src package when unformat decorators: " + this);
		}
		
		return len;
	}

	/**
	 * 字符串格式报文解包入口。用内含的格式化元素(kColl)对报文中的多条记录进行反格式化，依次放入iColl中。
	 * 
	 * @param src 待解报文(字符串)
	 * @param offset 起始位置
	 * @param context 交易上下文
	 * @return 解包长度
	 * @throws Exception
	 */
	public int unformat(String src, int offset, Context context) throws Exception {
		int len = 0, rows = 0, aLen = 0;

		int realLen = src.length() - offset;

		if (countLen != 0) {
			len += countLen;
			rows = Integer.parseInt(src.substring(offset, offset + countLen));
		}

		
		if (this.isAppend()) {
			//context.getDataElement().setAppend(this.isAppend());这个改变了context的属性，废掉
			if (!context.containsKey(getDataName())){
				DataElement theAppendDataElement = (DataElement) (this.getAppendClass() == null? IndexedCollection.class :Class.forName(this.getAppendClass())).newInstance();
				theAppendDataElement.setName(getDataName());
				theAppendDataElement.setAppend(this.isAppend());
				context.addDataElement(theAppendDataElement);
			}
		}
		IndexedCollection iColl = (IndexedCollection) context.getDataElement(getDataName(), IndexedCollection.class);
		if (!clear)
			iColl.clear();
		
		KeyedCollection kColl = (KeyedCollection) iColl.getDataElement();
		
//		if(){
//			
//		}

		FormatElement formatElement = this.getFormatElement();
		while (((countLen != 0 && rows > 0)) || ((countLen == 0) && len < realLen)) {
			if (countLen != 0)
				rows--;
			
			KeyedCollection aColl = (KeyedCollection) kColl.clone();
			iColl.addDataElement(aColl);

			aLen = formatElement.unformat(src, offset + len, aColl);
			if (aLen < 0) {
				EMPLog.log(EMPConstance.EMP_FORMAT, EMPLog.ERROR, 0, "Unformat failed when unformat : " + formatElement);
				throw new EMPFormatException("Unformat failed when unformat : " + formatElement);
			}
			len = len + aLen;
		}
		
		//计算修饰符长度
		aLen = super.extract(src, offset+len);
		if (aLen < 0) {
			EMPLog.log(EMPConstance.EMP_FORMAT, EMPLog.ERROR, 0, "Unformat failed when unformat decorators: " + this);
			throw new EMPFormatException("Unformat failed when unformat decorators: " + this);
		}
		len = len + aLen;
		if (len > realLen) {
			EMPLog.log(EMPConstance.EMP_FORMAT, EMPLog.ERROR, 0, "Unformat failed when unformat decorators: " + this);
			throw new EMPFormatException("Invalid Src package when unformat decorators: " + this);
		}
		
		return len;
	}

	/**
	 * 字符串格式报文解包入口。用内含的格式化元素(kColl)对报文中的多条记录进行反格式化，依次放入iColl中。
	 * 
	 * @param src 待解报文(字符串)
	 * @param offset 起始位置
	 * @param dataElement 数据模型(kColl)
	 * @return 解包长度
	 * @throws Exception
	 */
	public int unformat(String src, int offset, DataElement dataElement) throws Exception {
		int len = 0, rows = 0, aLen = 0;

		int realLen = src.length() - offset;

		if (countLen != 0) {
			len += countLen;
			rows = Integer.parseInt(src.substring(offset, offset + countLen));
		}

		KeyedCollection akColl = (KeyedCollection) dataElement;
		IndexedCollection iColl = (IndexedCollection) akColl.getDataElement(getDataName(), IndexedCollection.class);
		if (!clear)
			iColl.clear();
		
		KeyedCollection kColl = (KeyedCollection) iColl.getDataElement();

		FormatElement formatElement = this.getFormatElement();
		while (((countLen != 0 && rows > 0)) || ((countLen == 0) && len < realLen)) {
			if (countLen != 0)
				rows--;
			
			KeyedCollection aColl = (KeyedCollection) kColl.clone();
			iColl.addDataElement(aColl);

			aLen = formatElement.unformat(src, offset + len, aColl);
			if (aLen < 0) {
				EMPLog.log(EMPConstance.EMP_FORMAT, EMPLog.ERROR, 0, "Unformat failed when unformat : " + formatElement);
				throw new EMPFormatException("Unformat failed when unformat : " + formatElement);
			}
			len = len + aLen;
		}
		
		//计算修饰符长度
		aLen = super.extract(src, offset+len);
		if (aLen < 0) {
			EMPLog.log(EMPConstance.EMP_FORMAT, EMPLog.ERROR, 0, "Unformat failed when unformat decorators: " + this);
			throw new EMPFormatException("Unformat failed when unformat decorators: " + this);
		}
		len = len + aLen;
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
		int len = 0, rows = 0, aLen = 0, realLen = 0;

		len = len + countLen;
		
		if (src.getClass().isArray()) {
			realLen = ((byte[])src).length - offset;
			rows = Integer.parseInt(new String((byte[])src, offset, countLen));

		}
		else {
			realLen = ((String)src).length() - offset;
			if (countLen != 0) {
				rows = Integer.parseInt(((String)src).substring(offset, offset + countLen));
			}
		}


		FormatElement formatElement = this.getFormatElement();
		while (((countLen != 0 && rows > 0)) || ((countLen == 0) && len < realLen)) {
			if (countLen != 0)
				rows--;
			aLen = formatElement.extract(src, offset + len);
			if (aLen < 0) {
				EMPLog.log(EMPConstance.EMP_FORMAT, EMPLog.ERROR, 0, "Extract failed when Extract : " + formatElement);
				throw new EMPFormatException("Extract failed when Extract : " + formatElement);
			}
			len = len + aLen;
		}
		
		//计算修饰符长度
		aLen = super.extract(src, offset+len);
		if (aLen < 0) {
			EMPLog.log(EMPConstance.EMP_FORMAT, EMPLog.ERROR, 0, "Extract failed when extract decorators: " + this);
			throw new EMPFormatException("Extract failed when extract decorators: " + this);
		}
		len = len + aLen;
		if (len > realLen) {
			EMPLog.log(EMPConstance.EMP_FORMAT, EMPLog.ERROR, 0, "Extract failed when extract decorators: " + this);
			throw new EMPFormatException("Invalid Src package when extract decorators: " + this);
		}
		
		return len;
	}

	/**
	 * 判断反格式化时是否追加模式。
	 * 
	 * @return 是否追加模式
	 */
	public boolean isClear() {
		return clear;
	}

	/**
	 * 设置反格式化时是否追加模式。若不是，则反格式化时先清空iColl当前数据。
	 * 
	 * @param append 是否追加模式
	 */
	public void setClear(boolean clear) {
		this.clear = clear;
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

		buf.append("<iColl dataName=\"");
		buf.append(getDataName());
		buf.append("\" clear=\"");
		buf.append(String.valueOf(clear));
		buf.append("\">\n");

		buf.append(getFormatElement().toString(tabCount + 1));

		buf.append("\n");
		for (int i = 0; i < tabCount; i++)
			buf.append("\t");
		buf.append("</iColl>\n");

		for (int i = 0; i < this.getDecorators().size(); i++) {
			FormatElement fmt = (FormatElement) getDecorators().get(i);
			buf.append(fmt.toString(tabCount));
			buf.append("\n");

		}

		return buf.toString();
	}

	public int getCountLen() {
		return countLen;
	}

	public void setCountLen(int countLen) {
		this.countLen = countLen;
	}

	public void addFormatToContext(DataElement dataElement) throws Exception{


		KeyedCollection akColl = (KeyedCollection) dataElement;
		IndexedCollection iColl = (IndexedCollection) akColl.getDataElement(getDataName(), IndexedCollection.class);
		
		KeyedCollection kColl = (KeyedCollection) iColl.getDataElement();

		KeyedFormat formatElement = (KeyedFormat)this.getFormatElement();
		
		KeyedCollection aColl = (KeyedCollection) kColl.clone();
		iColl.addDataElement(aColl);

		formatElement.addFormatToContext(aColl);
		
	
		
	}

	public void addFormatToContext(Context context) throws Exception{

		
		if (this.isAppend()) {
			//context.getDataElement().setAppend(this.isAppend());这个改变了context的属性，废掉
			if (!context.containsKey(getDataName())){
				DataElement theAppendDataElement = (DataElement) (this.getAppendClass() == null? IndexedCollection.class :Class.forName(this.getAppendClass())).newInstance();
				theAppendDataElement.setName(getDataName());
				theAppendDataElement.setAppend(this.isAppend());
				context.addDataElement(theAppendDataElement);
			}
		}
		IndexedCollection iColl = (IndexedCollection) context.getDataElement(getDataName(), IndexedCollection.class);
		if (!clear)
			iColl.clear();
		
		KeyedCollection kColl = (KeyedCollection) iColl.getDataElement();
		

		KeyedFormat formatElement = (KeyedFormat)this.getFormatElement();
		
		
		KeyedCollection aColl = (KeyedCollection) kColl.clone();
		iColl.addDataElement(aColl);

		formatElement.addFormatToContext(aColl);
		
		
	
		
	}

}
