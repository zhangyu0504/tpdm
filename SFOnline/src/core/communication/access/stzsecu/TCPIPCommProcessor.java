package core.communication.access.stzsecu;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.ecc.emp.core.EMPConstance;
import com.ecc.emp.core.EMPException;
import com.ecc.emp.log.EMPLog;
import common.util.SFConst;

import core.communication.tcpip.CommProcessor;
import core.log.SFLogger;

/**
 * 
 * ��������EMP�ṩ��TCP/IPͨѶЭ�鴦���� 
 * <p>
 * �๦��˵����
 * <pre>
 * ��������ͨѶЭ�鴦��
 * </pre> 
 * ʹ��˵���� 
 * <pre>
 * </pre>
 * ����˵����
 *<pre>
 *</pre>
 * ����˵����
 *<pre>
 * ���Ա����˵�� 
 *</pre>
 * @author zhanglm@hundsun.com
 * @since  1.0 2011-04-14
 * @version 1.0
 */
public class TCPIPCommProcessor implements CommProcessor {

	/**
	 * ���ĳ����ֶεĳ���
	 */
	int lengthHeadLen = 0;

	/**
	 * ���ĳ��ȵı�ʾ������0ΪASCII��ʽ��1Ϊ�����Ʒ�ʽ
	 */
	int lengthHeadType = 0;

	/**
	 * ���������ж�ȡ���ݰ���������ͨѶЭ�鴦����
	 * ����ǰ��������λ���ĳ��ȣ�����������ж�ȡ��Ӧ���ȣ�
	 * �������û������λΪֹ��
	 * 
	 * @param in tcp/ip�����
	 * @return ��ȡ���ı���
	 * @throws IOException
	 * @throws EMPException
	 */
	public byte[] readPackage(InputStream in) throws IOException, EMPException {
		SFLogger.info(SFConst.DEFAULT_TRXCODE, null,
				"��֤ͨ���룬TCPIP��ʼ�ձ���...");
		
		int contentLength = 0;
		if (this.lengthHeadLen != 0) {	// ��ñ��ĳ���
			
			byte[] lenHeadBuf = new byte[lengthHeadLen];
			int off = 0;
			try{
				while (off < lengthHeadLen) {
					off = off + in.read(lenHeadBuf, off, lengthHeadLen - off);
					if (off < 0) {
						throw new EMPException("Socket was closed! while reading!");
					}
				}				
			}catch(java.net.SocketTimeoutException ste){
				EMPLog.log(EMPConstance.EMP_TCPIPACCESS, EMPLog.ERROR, 0,
						"��֤ͨ���룬��ȡ���ݰ�[��ʱ]�������ϵͳ�����������ƣ�socket�ڽ������Ӻ�20s��û���յ��κα��ľ͹رո�socket���ӡ�");
				throw ste;
			}
	
			SFLogger.info(SFConst.DEFAULT_TRXCODE, null,
					"��֤ͨ���룬�յ����ĳ���:[" + new String(lenHeadBuf)+"]");	
			
			if (lengthHeadType == 0) {		// ascii
				contentLength = Integer.parseInt(new String(lenHeadBuf).trim());
			} else {		// bin			
				for (int i = lengthHeadLen - 1; i >= 0; i--) {
					int value = (int) (lenHeadBuf[i] & 0xff);
					contentLength = contentLength * 256 + value;
				}
			}
		} else {	//û�б��ĳ����ֶ�
			contentLength = -1;
		}
		
		byte[] contentBuf = null;
		if (contentLength < 0) {//���û�пɲ��յı��ĳ��ȣ���ѭ����ȡֱ����ȡ��ĩβ
//			20180521-wanghua modify for(��������λ���ĳ�ʱ�ȴ�)-s
			ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
			byte[] buff = new byte[2048]; 	//buff���ڴ��ѭ����ȡ����ʱ����
			int rc = 0;
			while ((rc = in.read(buff, 0, 2048)) > 0) {
				swapStream.write(buff, 0, rc);
				if(rc<buff.length){
					break;
				}
			}
			contentBuf = swapStream.toByteArray();
			//��ȡ����Ϣ
			//contentBuf=readStram(in);
//			20180521-wanghua modify for(��������λ���ĳ�ʱ�ȴ�)-e			
		} else if (contentLength>0) {	//�����ճ��ȶ�ȡ
			int off = 0;
			contentBuf = new byte[contentLength];
			while (off < contentLength) {	
				int len = in.read(contentBuf, off, contentLength - off);
				if (len <= 0) {
					break;
				}
				off = off + len;
			}
		} else {
			EMPLog.log(EMPConstance.EMP_TCPIP, EMPLog.ERROR, 0,
					"��֤ͨ���룬��ȡ����ʧ��!");	
			throw new EMPException("��֤ͨ���룬��ȡ����ʧ��!");
		}

		SFLogger.info(SFConst.DEFAULT_TRXCODE, null,
		        "��֤ͨ���룬TCPIP�յ��������ݣ�["+new String(contentBuf)+"]");
		return contentBuf;
	}
	
	/**
	 * ��ȡ����Ϣ
	 * @param inStream
	 * @return
	 * @throws IOException
	 */
	public static byte[] readStram(InputStream inStream) throws IOException{
		int count=0;
		while(count==0){
			count = inStream.available();
			System.out.println(">>>>>>>>>>>>>>>>count=:"+count);
		}
		byte[] b=new byte[count];
		inStream.read(b);
		return b;
	}

	/**
	 * ����ͨѶЭ������ݰ����д��������ͨѶЭ��ͷ�����ĳ��ȣ���
	 * 
	 * @param msg �����͵����ݰ�
	 * @return ����ͨѶЭ��ͷ�����ݰ�
	 */
	public byte[] wrapMessagePackage(byte[] msg) {
		
		if (msg == null)
			return msg;

		if (this.lengthHeadLen == 0) return msg;
		
		int length = msg.length;
		byte[] buf = new byte[length + this.lengthHeadLen];
		System.arraycopy(msg, 0, buf, lengthHeadLen, length);

		if (this.lengthHeadType == 0) {	// ascii
			String lenValue = String.valueOf(length);
			for (int i = 0; i < lengthHeadLen; i++)
				buf[i] = '0';
			int idx = 0;
			for (int i = lengthHeadLen - lenValue.length(); i < lengthHeadLen; i++) {
				buf[i] = (byte) lenValue.charAt(idx++);
			}
		} else {		// bin		
			int l = length;
			for (int i = 0; i < lengthHeadLen; i++) {
				buf[i] = (byte) (l % 256);
				l = l / 256;
			}
		}
		SFLogger.info(SFConst.DEFAULT_TRXCODE, null,
		        "��֤ͨ���룬TCPIP���ر������ݣ�["+new String(buf)+"]");
		return buf;
	}

	public int getLengthHeadLen() {
		return lengthHeadLen;
	}

	public void setLengthHeadLen(int lengthHeadLen) {
		this.lengthHeadLen = lengthHeadLen;
	}

	public int getLengthHeadType() {
		return lengthHeadType;
	}

	public void setLengthHeadType(int lengthHeadType) {
		this.lengthHeadType = lengthHeadType;
	}	
	
}