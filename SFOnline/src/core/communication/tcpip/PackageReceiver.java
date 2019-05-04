package core.communication.tcpip;

import java.net.Socket;

/**
 * 服务端数据包处理接口定义，
 * 当readThread接收到数据包时，会调用此接口的newPackageReceived方法
 * 
 *  Intrerface of a Server package processer, when a new pacakage was received by readThread
 *  it will call the newPackageReceived method to indicated that a new package was received
 *
 * @(#) ServerPackageProcessor.java	1.0  
 *    Copyright (c) 2002 ECC All Rights Reserved.
 *   
 *   
 *    @version 1.0 (2002-4-30 12:14:05)
 *    @author  ZhongMingChang
 */

public interface PackageReceiver {
	
	public void newPackageReceived(byte[] aPackage, Socket socket);
}
