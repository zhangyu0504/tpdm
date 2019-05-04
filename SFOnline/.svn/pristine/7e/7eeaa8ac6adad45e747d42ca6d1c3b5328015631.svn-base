package core.communication.access.tcpip;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import com.ecc.emp.core.Context;

import core.log.SFLogger;

/**
 * 【类名】客户端TCPIP通讯类。
 * <p>
 * 类功能说明：
 * 
 * <pre>
 * 实现银银平台与外围系统的TCPIP通讯
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
	private int connectTimeOut;
	private int socketTimeOut;
	private int pkgLen;// 报文长度的位数
	private String sResult = null;

	public BBCPCommClient(String sIP, String sPort, int connectTimeOut, int socketTimeOut,int pkgLen) {
		this.sIP = sIP;
		this.sPort = sPort;
		this.connectTimeOut = connectTimeOut;
		this.socketTimeOut = socketTimeOut;
		this.pkgLen = pkgLen;
	}

	public String getResult() {
		return sResult;
	}

	/**
	 * 使用Socket向外围系统发送
	 * 
	 * @param sCmd:
	 *            字符串
	 * @return: 0为成功; 1为无法建立连接; 2为发送超时; 3为接收超时; 4为出现异常;5其他错误
	 */
	public String SendCMD(Context context,byte[] sCmd) {
		int state = 1;
		Socket sock = null;
		ByteArrayOutputStream swapStream = null;
		SFLogger.info(context,"请求报文:[" + new String(sCmd) + "]");
		
		
		try {
			SFLogger.info(context,"connect ip:" + sIP + ",port:" + sPort + ",connectTimeOut:"+ connectTimeOut + ",socketTimeOut:"+ socketTimeOut +",pkgLen=" + pkgLen);
			//sock = new Socket(sIP, Integer.parseInt(sPort));

			sock = new Socket();
			SocketAddress addr = new InetSocketAddress( sIP, Integer.parseInt( sPort ) );
			sock.connect( addr,  connectTimeOut);			
			sock.setSoTimeout(socketTimeOut);//设置超时
			out = new DataOutputStream(sock.getOutputStream());

			// 发送报文内容
			out.write(sCmd);
			out.flush();
			// out.close();
		
			// 设置为接收状态
			state = 2;
			in = new DataInputStream(sock.getInputStream());
			swapStream = new ByteArrayOutputStream();
			int rc = 0;

			if (pkgLen > 0) {
				byte[] lenByte = new byte[pkgLen];
				in.read(lenByte, 0, lenByte.length);
				int len = Integer.parseInt(new String(lenByte));
				SFLogger.info(context,"应答报文长度:[" + len + "]");
				byte[] buff = new byte[1024]; // buff用于存放循环读取的临时数据
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
				byte[] buff = new byte[256]; // buff用于存放循环读取的临时数据
				while ((rc = in.read(buff, 0, buff.length)) > 0) {
					swapStream.write(buff, 0, rc);
					//if(rc < buff.length){ break; }
				}
				sResult = new String(swapStream.toByteArray());
			}

			SFLogger.info(context,"应答报文:[" + sResult + "]");
		} catch (ConnectException e) {
			SFLogger.error(context,"ConnectException通讯异常:",e);
			return "1:为无法建立连接";
		} catch (SocketTimeoutException e) {
			if (state == 1)
				return "2:为发送超时";
			else{
				SFLogger.error(context,"SocketTimeoutException通讯异常:接收超时",e);
				return "3:为接收超时";
//				throw new RuntimeException("receiveTimeOut");
			}
		} catch (SocketException e) {
			SFLogger.error(context,	"SocketException通讯异常:" ,e);
			return "4:为出现异常";
		} catch (IOException e) {
			SFLogger.error(context,	"IOException通讯异常:" ,e);	
			return "4:为出现异常";
		} catch (Exception e) {
			SFLogger.error(context,	"Exception通讯异常:" ,e);	
			return "5:其他错误";
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
				SFLogger.error(context,"IOException关闭数据流异常:",e);
				return "5:其他错误";
			}
		}

		return "0";
	}

	/**
	 * 测试用
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		BBCPCommClient client = new BBCPCommClient("10.14.205.241", "18200",60000,20000, 4);
		try {
			String ret = client.SendCMD(null,"0322810010                                                                                                                                                            2016120108:44:2116113008670632      2016120100050361                            16223228801371134                102800000700466485            1RMB0000000000050".getBytes());
			System.out.println("ret="+ret);
		} catch (Exception e) {
		    e.printStackTrace();
		}
	}

}
