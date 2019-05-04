package core.communication.tcpip;

import java.net.Socket;

import com.ecc.emp.core.EMPException;

/**
 * 
 * �������ݰ�����ӿڶ��壬������Щ��Ҫ����Է��������ͨ�ŷ���
 * ��ͨ�ŷ�����յ����ݰ�ʱ������ô˽ӿڵ�processNewPackage(byte[]��InputStream) 
 * �����������½��յ����ݰ���
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
	 * ���յ������ݰ�����ӿڣ���ͨ�ŷ�����յ��µ����ݰ�ʱ�����ô˽ӿڣ�
	 * �������������˽ӿڴ������������ģ�����null����ԭ�����ء�
	 * ����OutputStream�ǵ�ͨ�ŷ�����˫��ͨ��ʱ����Ӧ�������
	 * 
	 * @Creation date: (2002-4-30 9:59:26)
	 * @author ZhongMingChang
	 * @param msg byte[]
	 * @param len int
	 */
	public byte[] processNewPackage(byte[] msg, TCPIPService service, Socket socket)throws EMPException;
	
	public boolean isRequestPackage(byte[] msg );
}
