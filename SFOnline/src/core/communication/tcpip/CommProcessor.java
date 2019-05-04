package core.communication.tcpip;

import java.io.IOException;

import com.ecc.emp.core.EMPException;

/**
 *  通讯协议处理接口，通信程序会调用它的 readPackage(InputStream)来从输入流中
 *  读取数据包，具体的通讯协议也需要它处理。同时通讯程序会调用它的
 *  byte[] wrapeMessagePackage(byte[]) 来对发送数据包进行打包处理（如加入通信协议报头）。
 * 
 *   @(#) InputProcessor.java	1.0  
 *   Copyright (c) 2002 ECC All Rights Reserved.
 *   
 *   
 *    @version 1.0 (2002-4-30 9:57:49)
 *    @author  ZhongMingChang
 *    @modifier LiJia 2006-11-16
 */
public interface CommProcessor {

	
/**
 * 从输入流中读入一个标准数据包，它需要处理通信协议。
 *
 * @Creation date: (2002-4-30 10:00:55) 
 * @author  ZhongMingChang
 * @return byte[]
 * @param in java.io.InputStream
 */
byte[] readPackage(java.io.InputStream in) throws IOException, EMPException;


/**
 * 根据通信协议对数据包进行打包，如加入通信报文头
 * 
 * @Creation date: (2002-5-24 9:16:20) 
 * @author  ZhongMingChang
 * @return byte[]
 * @param msg byte[]
 */
byte[] wrapMessagePackage(byte[] msg);
}
