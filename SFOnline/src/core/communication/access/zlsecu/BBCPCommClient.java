package core.communication.access.zlsecu;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import module.bean.SecCompData;

import com.ecc.emp.core.Context;
import common.util.SFConst;

import core.log.SFLogger;

/**
 * ���������ͻ���TCPIPͨѶ�ࡣ
 * <p>
 * �๦��˵����
 * 
 * <pre>
 * ʵ������ƽ̨����Χϵͳ��TCPIPͨѶ
 * </pre>
 * 
 * @author zhanglm@hundsun.com
 * @version 2.1
 * @since 1.0 2011-4-14
 * 
 */
public class BBCPCommClient {
	private DataInputStream in;
	private DataOutputStream out;

	private String sIP;
	private String sPort;
	private int timeOut;
	private int pkgLen;// ���ĳ��ȵ�λ��
	private String sResult = null;

	public BBCPCommClient(String sIP, String sPort, int timeOut, int pkgLen) {
		this.sIP = sIP;
		this.sPort = sPort;
		this.timeOut = timeOut;
		this.pkgLen = pkgLen;
	}

	public String getResult() {
		return sResult;
	}

	/**
	 * ʹ��Socket����Χϵͳ����
	 * 
	 * @param sCmd:
	 *            �ַ���
	 * @return: 0Ϊ�ɹ�; 1Ϊ�޷���������; 2Ϊ���ͳ�ʱ; 3Ϊ���ճ�ʱ; 4Ϊ�����쳣;5��������
	 */
	public String SendCMD(Context context,byte[] sCmd) {
		int state = 1;
		Socket sock = null;
		ByteArrayOutputStream swapStream = null;

		try {
			//SFLogger.info(context,"connect ip:" + sIP + ",port:" + sPort + ",timeOut:"+ timeOut + ",pkgLen=" + pkgLen);			
			SFLogger.info(context,"TCPIP��������:[" + new String(sCmd) + "]");			
			sock = new Socket(sIP, Integer.parseInt(sPort));
			// ���ó�ʱ
			sock.setSoTimeout(timeOut);
			out = new DataOutputStream(sock.getOutputStream());

			// ���ͱ�������
			out.write(sCmd);
			out.flush();
			// out.close();
			

			// ����Ϊ����״̬
			state = 2;
			in = new DataInputStream(sock.getInputStream());
			swapStream = new ByteArrayOutputStream();
			int rc = 0;

			if (pkgLen > 0) {
				byte[] lenByte = new byte[pkgLen];
				in.read(lenByte, 0, lenByte.length);
				int len = Integer.parseInt(new String(lenByte));
				SFLogger.info(context,"�������ݱ��ĳ���:[" + len + "]");
				byte[] buff = new byte[1024]; // buff���ڴ��ѭ����ȡ����ʱ����
				int off = 0;
				while ((rc = in.read(buff, 0, buff.length)) > 0) {
					swapStream.write(buff, 0, rc);
					off = off + rc;
					if (off == len) {
						break;
					}
				}
				sResult = new String(swapStream.toByteArray());
			} else {
				/*
				 * ����Դ����ղ�ͬȯ�̷��ر��ģ�
				 * 1�����ų����Ļ�������ݶ�����
				 * 2����̩����̩�������ر��ĺ󲻻�ֱ�ӹر�socket����
				 */
				SecCompData secCompData = (SecCompData)context.getDataValue(SFConst.PUBLIC_SECU);
				String secCompCode = null;
				if(secCompData!=null){
					 secCompCode = secCompData.getSecCompCode();
				}
				if(secCompCode!=null&&(SFConst.SECU_GUOXINZQ.equals(secCompCode)||SFConst.SECU_GUOXINXY.equals(secCompCode))){ //���� 
					byte[] buff = new byte[2048]; // buff���ڴ��ѭ����ȡ����ʱ����
					while ((rc = in.read(buff, 0, buff.length)) > 0) {
						swapStream.write(buff, 0, rc);
						//if(rc < buff.length){ break; }
					}
				}else{
					/*
					 * ֱ��ȯ�̷�����������Ӧ���ģ�
					 * 1����ȡ��������ͷ111���ֽ�
					 * 2��������ͷ�͵ĳ��Ƚ�ȡ�����峤��
					 */
					//ȡ����ͷ
					int msgLength=0;
					byte[] headBuff = new byte[111];
					while ((rc = in.read(headBuff, 0, headBuff.length)) > 0) {
						swapStream.write(headBuff, 0, rc);
						if(headBuff.length==111){ break; }	
					}
					if(headBuff!=null&&headBuff.length>0){
						msgLength = Integer.parseInt(new String(headBuff).substring(40, 49).trim());					
					}
					
					//ȡ������
					int off = 0;
					byte[] contentBuf = new byte[msgLength];
					while (off < msgLength) {	
						int len = in.read(contentBuf, off, msgLength - off);
						swapStream.write(contentBuf, 0, len);
						if (len <= 0) {
							break;
						}
						off = off + len;
					}
				}
				sResult = new String(swapStream.toByteArray());
			}

			SFLogger.info(context,"�������ݳɹ�.��������:[" + sResult + "]");
		} catch (ConnectException e) {
			SFLogger.error(context,"ConnectExceptionͨѶ�쳣:" ,e);
			return "1:Ϊ�޷���������";
		} catch (SocketTimeoutException e) {
			SFLogger.error(context,"SocketTimeoutExceptionͨѶ�쳣:" ,e);
			if (state == 1)
				return "2:Ϊ���ͳ�ʱ";
			else{
				SFLogger.error(context,"SocketTimeoutExceptionͨѶ�쳣:���ճ�ʱ" ,e);
				throw new RuntimeException("receiveTimeOut");
			}
		} catch (SocketException e) {
			SFLogger.error(context,"SocketExceptionͨѶ�쳣:" ,e);
			return "4:Ϊ�����쳣";
		} catch (IOException e) {
			SFLogger.error(context,"IOExceptionͨѶ�쳣:" ,e);	
			return "4:Ϊ�����쳣";
		} catch (Exception e) {
			SFLogger.error(context,"ExceptionͨѶ�쳣:" ,e);	
			return "5:��������";
		} finally {
			try {
				if (in != null) {
					in.close();
				}
				if (out != null) {
					out.close();
				}
				if (swapStream != null) {
					swapStream.close();
				}
				if (sock != null) {
					sock.close();
				}
			} catch (IOException e) {
				SFLogger.error(context,"IOException�ر��������쳣:" ,e);
				return "5:��������";
			}
		}

		return "0";
	}

	/**
	 * ������
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		BBCPCommClient client = new BBCPCommClient("10.14.154.23", "10003",60000, 4);
		try {
			String ret = client.SendCMD(null,"0322810010                                                                                                                                                            2016120108:44:2116113008670632      2016120100050361                            16223228801371134                102800000700466485            1RMB0000000000050".getBytes());
			System.out.println("ret="+ret);
		} catch (Exception e) {
		    e.printStackTrace();
		}
	}

}