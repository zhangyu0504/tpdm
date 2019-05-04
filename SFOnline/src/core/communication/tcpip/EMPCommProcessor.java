package core.communication.tcpip;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.ecc.emp.core.EMPConstance;
import com.ecc.emp.core.EMPException;
import com.ecc.emp.log.EMPLog;

/**
 * EMP提供的TCP/IP通信报文处理类。
 * 
 * @author zhongmc@yuchengtech.com
 * @author lijia@yuchengtech.com
 * @since 2007.01.11
 */
public class EMPCommProcessor implements CommProcessor {

	/**
	 * 报文长度字段的长度
	 */
	int lengthHeadLen = 4;

	/**
	 * 报文长度的表示方法，0为ASCII方式，1为二进制方式
	 */
	int lengthHeadType = 0;

	/**
	 * 从输入流中读取数据包，并进行通讯协议处理。
	 * 若最前面有若干位报文长度，则从输入流中读取相应长度；
	 * 否则读到没有数据位为止。
	 * 
	 * @param in tcp/ip输出流
	 * @return 读取到的报文
	 * @throws IOException
	 * @throws EMPException
	 */
	public byte[] readPackage(InputStream in) throws IOException, EMPException {
		
		int contentLength = 0;
		if (this.lengthHeadLen != 0) {	// 获得报文长度
			
			byte[] lenHeadBuf = new byte[lengthHeadLen];
			int off = 0;
			while (off < lengthHeadLen) {
				off = off + in.read(lenHeadBuf, off, lengthHeadLen - off);
				if (off < 0) {
					throw new EMPException("Socket was closed! while reading!");
				}
			}
	
			EMPLog.log(EMPConstance.EMP_TCPIP, EMPLog.DEBUG, 0,
					"Recieve package length:" + new String(lenHeadBuf));	
			
			if (lengthHeadType == 0) {		// ascii
				contentLength = Integer.parseInt(new String(lenHeadBuf).trim());
			} else {		// bin			
				for (int i = lengthHeadLen - 1; i >= 0; i--) {
					int value = (int) (lenHeadBuf[i] & 0xff);
					contentLength = contentLength * 256 + value;
				}
			}
		} else {	//没有报文长度字段
			contentLength = -1;
		}
		
		byte[] contentBuf = null;
		if (contentLength < 0) {	//如果没有可参照的报文长度，则循环读取直到读取到末尾
			
			BufferedInputStream di = new BufferedInputStream(in); 
			ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
			byte[] buff = new byte[100]; 	//buff用于存放循环读取的临时数据
			int rc = 0;
			while ((rc = di.read(buff, 0, 100)) > 0) {
				swapStream.write(buff, 0, rc);
			}

			contentBuf = swapStream.toByteArray();
		} else if (contentLength>0) {	//否则按照长度读取

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
			throw new EMPException("Read package error!");
		}
		EMPLog.log(EMPConstance.EMP_TCPIP, EMPLog.DEBUG, 0,
				"EMP CommProcessor Read in package As:"
						+ new String(contentBuf));
		return contentBuf;
	}

	/**
	 * 根据通讯协议对数据包进行打包，加入通讯协议头（报文长度）。
	 * 
	 * @param msg 待发送的数据包
	 * @return 加入通讯协议头的数据包
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
