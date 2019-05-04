package core.communication.tcpip;
import java.net.*;

/**
 * ���׽������Ӽ������ӿڡ������������׽�������״̬�ĸı�
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
	 * socket����֮ǰ
	 */
	public void beforeSocketConnected();
	
	/**
	 * socket����֮��
	 * @param socket
	 */
	public void afterSocketConnected(Socket socket);
	
	/**
	 * socket�ж�֮ǰ
	 * @param socket
	 */
	public void beforeSocketClosed(Socket socket);
	
	/**
	 * socket�ж�֮��
	 */
	public void afterSocketClosed();

}
