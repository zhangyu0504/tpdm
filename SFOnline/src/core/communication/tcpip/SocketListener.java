package core.communication.tcpip;
import java.net.*;

/**
 * 新套接字连接监听器接口。用于侦听新套接字连接状态的改变
 * 
 * Copyright (c) 2002 ECC All Rights Reserved.
 *   
 * @version 1.0 (2002-4-30 10:04:05)
 * @author  ZhongMingChang
 * @modifier LiJia 2006-11-17
 * @modifier liubq 2009-08-10
 */

public interface SocketListener {
	
	/**
	 * socket连接之前
	 */
	public void beforeSocketConnected();
	
	/**
	 * socket连接之后
	 * @param socket
	 */
	public void afterSocketConnected(Socket socket);
	
	/**
	 * socket中断之前
	 * @param socket
	 */
	public void beforeSocketClosed(Socket socket);
	
	/**
	 * socket中断之后
	 */
	public void afterSocketClosed();

}
