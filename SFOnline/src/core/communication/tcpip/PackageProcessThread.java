package core.communication.tcpip;

import java.net.Socket;

import com.ecc.emp.core.EMPConstance;
import com.ecc.emp.log.EMPLog;


/**
 * 
 * SocketProcessThread.java<br>
 * Copyright (c) 2000, 2006 e-Channels Corporation <br>
 *
 * @author zhongmc<br>
 * @version 1.0.0<br>
 * @since 2007-1-11<br>
 * @lastmodified 2007-1-11<br>
 *
 *
 */
public class PackageProcessThread implements Runnable 
{
	
	private PackageProcessor packageProcessor;
	
	private TCPIPService service;
	
	private byte[] msg;

	private boolean isStop = false;

	private Thread thread = null;
	
	private boolean beFree = true;
	
	private Socket socket;
	
	
	public PackageProcessThread() {
		super();
	}

	public PackageProcessThread(PackageProcessor packageProcessor, TCPIPService service, byte[] msg, Socket socket ) 
	{
		this.packageProcessor = packageProcessor;
		this.service = service;
		this.socket = socket;
		this.msg = msg;
		this.beFree = false;
		
		Thread aThread = new Thread(this);
		aThread.setName("EMP TCPIP PackageProcess Thread");
		aThread.start();
		thread = aThread;
	}

	


	public void run() 
	{
		while (!isStop) 
		{
			if( msg != null )
			{
				try{
					packageProcessor.processNewPackage(msg, service, socket);
				}catch(Exception e)
				{
					EMPLog.log(EMPConstance.EMP_TCPIP, EMPLog.ERROR, 0, "Fatal error in process TCPIP req package: " + new String(msg), e);
				}
			}
			
			synchronized (this) {
				service.packageProcessorThreadFree(this);
				try {
					wait();
				} catch (Exception e) {
				}
			}
		}
	}

	
	/**
	 * 对Socket 请求进行处理
	 * @param socket
	 */
	public synchronized void processPackage(byte[] msg, Socket socket )
	{
		
		this.msg = msg;
		this.socket = socket;
		notify();
	
	}

	
	/**
	 * 设置线程名
	 * 
	 * @Creation date: (2002-4-30 13:37:30)
	 * @author ZhongMingChang
	 * @param name java.lang.String
	 */
	public void setThreadName(String name) {
		if (thread != null)
			thread.setName(name);
	}

	/**
	 * 停止线程，关闭套接字
	 * 
	 * @Creation date: (2002-4-30 10:46:01)
	 * @author ZhongMingChang
	 */
	public void terminate() 
	{
		try {
			isStop = true;
			synchronized(this)
			{
				notify();
			}
		} 
		catch (Exception e) 
		{
			EMPLog.log(EMPConstance.EMP_TCPIP, EMPLog.ERROR, 0, "ReadThread method stop() ERROR! " , e);
		}
	}

	public boolean isBeFree() {
		return beFree;
	}

	public void setBeFree(boolean beFree) {
		this.beFree = beFree;
	}
}
