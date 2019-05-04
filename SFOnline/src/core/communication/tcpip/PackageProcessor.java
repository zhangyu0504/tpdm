package core.communication.tcpip;

import java.net.Socket;

import com.ecc.emp.core.EMPException;

/**
 * 
 * 接收数据包处理接口定义，用于那些需要处理对方请求包的通信服务，
 * 当通信服务接收到数据包时，会调用此接口的processNewPackage(byte[]，InputStream) 
 * 方法来处理新接收的数据包。
 * 
 * @(#) InputPackageProcessor.java 1.0 
 * Copyright (c) 2002 ECC All Rights Reserved. 
 * 
 * @version 1.0 (2002-4-30 9:58:12)
 * @author ZhongMingChang
 * @modifier LiJia 2006-11-16
 */
public interface PackageProcessor {
	/**
	 * 
	 * 接收到新数据包处理接口，当通信服务接收到新的数据包时，调用此接口，
	 * 如果是请求报文则此接口处理数据请求报文，返回null否则原包返回。
	 * 其中OutputStream是当通信服务是双工通信时的响应输出流。
	 * 
	 * @Creation date: (2002-4-30 9:59:26)
	 * @author ZhongMingChang
	 * @param msg byte[]
	 * @param len int
	 */
	public byte[] processNewPackage(byte[] msg, TCPIPService service, Socket socket)throws EMPException;
	
	public boolean isRequestPackage(byte[] msg );
}
