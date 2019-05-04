package core.communication.tcpip;

import java.io.IOException;

import com.ecc.emp.core.EMPException;

/**
 *  ͨѶЭ�鴦��ӿڣ�ͨ�ų����������� readPackage(InputStream)������������
 *  ��ȡ���ݰ��������ͨѶЭ��Ҳ��Ҫ������ͬʱͨѶ������������
 *  byte[] wrapeMessagePackage(byte[]) ���Է������ݰ����д�����������ͨ��Э�鱨ͷ����
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
 * ���������ж���һ����׼���ݰ�������Ҫ����ͨ��Э�顣
 *
 * @Creation date: (2002-4-30 10:00:55) 
 * @author  ZhongMingChang
 * @return byte[]
 * @param in java.io.InputStream
 */
byte[] readPackage(java.io.InputStream in) throws IOException, EMPException;


/**
 * ����ͨ��Э������ݰ����д���������ͨ�ű���ͷ
 * 
 * @Creation date: (2002-5-24 9:16:20) 
 * @author  ZhongMingChang
 * @return byte[]
 * @param msg byte[]
 */
byte[] wrapMessagePackage(byte[] msg);
}
