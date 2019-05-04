package core.communication.format.string;

import com.ecc.emp.data.DataField;
import com.ecc.emp.format.EMPFormatException;
import com.ecc.emp.format.FormatElement;
import com.ecc.emp.util.EMPUtils;

import core.communication.format.FormatField;

/**
 * 
 * <b>����������</b><br>
 * ID=value��ʽ���ַ�����ʽ<br>
 * 
 * <b>����ʾ����</b><br>
 * &lt;fmtDef id="testID"&gt;<br>
 * &nbsp;&nbsp;&lt;record&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&lt;IDStringFormat dataName="Name" idName="����" hasQuot="true"&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;delim delimChar=";"/&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&lt;/IDStringFormat&gt;<br>
 * &nbsp;&nbsp;&lt;/record&gt;<br>
 * &lt;/fmtDef&gt;<br>
 * 
 * <b>����˵����</b><br>
 * <b>dataName</b> ����������<br>
 * <b>idName</b> ID����ʡ������dataName����<br>
 * <b>hasQuot</b> value�Ƿ���������������Ĭ��Ϊfalse<br>
 * 
 * @author ZhongMingChang
 * @modifier LiJia 2006-12-11
 */
public class IDStringFormat extends FormatField {

	private boolean hasQuot = false;
	
	private String idName = null;

	/**
	 * @roseuid 44FD3F20031C
	 */
	public IDStringFormat() {
		super();
	}

	public void setHasQuot(boolean hasQuot) {
		this.hasQuot = hasQuot;
	}

	public void setIdName(String idName) {
		this.idName = idName;
	}

	public boolean isHasQuot() {
		return hasQuot;
	}

	public String getIdName() {
		return idName;
	}

	/**
	 * ��������תΪID=value��ʽ�ַ���
	 * 
	 * @param DataField dataField //������
	 * @returns Object  //��ʽ������ַ���
	 */
	public Object format(DataField dataField) throws EMPFormatException {
		String value = null;
		
		try {
			String name = dataField.getName();	
			Object valueObj = dataField.getValue();
			if (valueObj.getClass().isArray()) {
				value = new String((byte[]) valueObj, this.getEncoding());
			}
			else {
				value = (String)dataField.getValue();
			}
			StringBuffer sb = new StringBuffer();
			if (idName==null || "".equals(idName)){
				sb.append(name).append("=");
			} else {
				sb.append(idName).append("=");
			}
			if (hasQuot) {
				sb.append("\"").append(value).append("\"");
			} else {
				sb.append(value);
			}
			return sb.toString();
		}
		catch (Exception e) {
			throw new EMPFormatException(e);
		}
	}

	
	/**
	 * �������ַ�����Ҫ������ִ����з���ʽ�������������EMP������
	 * 
	 * @param Object src //���봮
	 * @param DataField dataField //������
	 */
	public void unformat(Object src, DataField dataField) throws EMPFormatException {
		String mySrc = null;
		
		try {
			if (src.getClass().isArray()) {
				mySrc = new String((byte[]) src, this.getEncoding());
			}
			else {
				mySrc = (String)src;
			}
		}
		catch (Exception e) {
			throw new EMPFormatException("IDStringFormat unformat failed: invalid charset code!", e);
		}

		
		String realId = this.idName;
		if (realId == null)
			realId = this.getDataName();

		StringBuffer value = null;

		try {
			int e=mySrc.indexOf(realId+"=");
			value = new StringBuffer(mySrc.substring(mySrc.indexOf('=',e)+1));
			if (hasQuot){
				if (value.charAt(0)=='\"')
					value.deleteCharAt(0);
				if (value.charAt(value.length()-1)=='\"')
					value.deleteCharAt(value.length()-1);
			}
		} catch (RuntimeException err) {
			throw new EMPFormatException("IDStringFormat unformat failed!",err);
		}

		try {
			if (this.isBin()) {
				dataField.setValue(value.toString().getBytes(this.getEncoding()));
			}
			else {
				dataField.setValue(value.toString());
			}
		}
		catch (Exception e) {
			throw new EMPFormatException("IDStringFormat unformat failed: invalid charset code!", e);
		}
		
		return;
	}
	
	/**
	 * �õ�����ʽ��Ԫ����Ҫ�����ݳ���
	 * 
	 * @param String src //���봮
	 * @param int offset //ƫ����
	 * @returns int //���ݳ���
	 */
	public int extract (Object src, int offset) throws EMPFormatException {
		String realId = this.idName;
		if (realId == null)
			realId = this.getDataName();
		if (realId == null || "".equals(realId)) {
			throw new EMPFormatException("IDStringFormat unformat failed! Id ["+realId+"] not found!");
		} 

		if (src.getClass().isArray()) { //�����Ƹ�ʽ
			int retLen = 0;
			byte[] srcBytes = (byte[])src;

			int b = EMPUtils.findSubArray(srcBytes, (realId+"=").getBytes(), offset);
			if (b == -1)
				throw new EMPFormatException("IDStringFormat unformat failed! Id ["+realId+"] not found!");

			if (hasQuot)
			{//�����������������ж�
				int q1 = EMPUtils.findSubArray(srcBytes, ("\"").getBytes(), b);
				int q2 = EMPUtils.findSubArray(srcBytes, ("\"").getBytes(), q1+1);
				if (q1!=-1 && q2!=-1) {
					retLen = q2-offset+1;
					//�������η��ĳ���
					retLen = retLen + super.extract(srcBytes, offset+retLen);
				}
			}

			//����decorators�ж�
			if (retLen == 0) { 
				int aLen = super.extract(srcBytes, offset);
				if (aLen != -1)
					retLen = aLen;
				else 
					return -1;
			}
			
			//���û�����η�
			if (retLen == 0)
				retLen = srcBytes.length - offset;

			return retLen;
		}
		else {
			int retLen = 0;
			String mySrc = (String)src;

			int b = mySrc.indexOf(realId + "=", offset);
			if (b == -1)
				throw new EMPFormatException("IDStringFormat unformat failed! Id ["+realId+"] not found!");

			if (hasQuot)
			{	//�����������������ж�
				int q1 = mySrc.indexOf("\"",b);
				int q2 = mySrc.indexOf("\"",q1+1);
				if (q1!=-1 && q2!=-1) {
					retLen = q2-offset+1;
					//�������η��ĳ���
					retLen = retLen + super.extract(mySrc, offset+retLen);
				}
			}

			//����decorators�ж�
			if (retLen == 0) { 
				int aLen = super.extract(mySrc, offset);
				if (aLen != -1)
					retLen = aLen;
				else 
					return -1;
			}
			
			//���û�����η�
			if (retLen == 0)
				retLen = mySrc.length() - offset;

			return retLen;
		}
	}
	
	public String toString(int tabCount) {
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < tabCount; i++)
			buf.append("\t");

		buf.append("<IDStringFormat dataName=\"");
		buf.append(getDataName());
		buf.append("\" idName=\"").append(idName);
		buf.append("\" hasQuot=\"").append(hasQuot);
		buf.append("\"/>\n");

		for (int i = 0; i < this.getDecorators().size(); i++) {
			FormatElement fmt = (FormatElement) getDecorators().get(i);
			buf.append(fmt.toString(tabCount));
			buf.append("\n");
		}

		return buf.toString();
	}
}
