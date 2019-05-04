package core.communication.tcpip;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.ecc.emp.concurrent.EMPConcurrentTask;
import com.ecc.emp.core.EMPConstance;
import com.ecc.emp.core.EMPException;
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
public class SocketProcessThread implements Runnable, EMPConcurrentTask {

	private boolean keepAlive = false;

	private CommProcessor commProcessor;

	private PackageReceiver packageReceiver;

	private Socket socket = null;

	private List socketAcceptListeners;

	private long lastAccess;
	
	private byte[] rejectMessageBytes;
	
	private long waitTime;

	/**
	 * 是否启动空闲超时检查(该属性只在长连接下起作用)。缺省：否
	 */
	private boolean idleCheck = false;

	/**
	 * 空闲超时时间。缺省：5*60000
	 */
	private int idleTimeOut = 5 * 60000;

	/**
	 * 检查空闲超时的间隔时间，缺省：60000
	 */
	private int idleCheckInterval = 60000;
	
	protected Timer idleCheckTimer = null;
	
	protected Object synObj = new Object();
	
	private boolean isStop = false;
	
	private boolean isHandle = false;
	
	protected String socketInfo;

	public SocketProcessThread(Socket socket, CommProcessor commProcessor,
			PackageReceiver packageReceiver, boolean keepAlive) {
		super();
		this.socket = socket;
		this.commProcessor = commProcessor;
		this.packageReceiver = packageReceiver;
		this.keepAlive = keepAlive;
		
		this.socketInfo = "["+this.socket.getLocalSocketAddress()+"<-->"+this.socket.getRemoteSocketAddress()+"]";
		this.lastAccess = System.currentTimeMillis();
	}

	public void run() {
		EMPLog.log(EMPConstance.EMP_TCPIP, EMPLog.INFO, 0, "SocketProcessThread start to handle the socket "+this.socketInfo);
		
		this.lastAccess = System.currentTimeMillis();
		if (this.keepAlive) {
			if(this.idleCheck){
				idleCheckTimer = new Timer();
				idleCheckTimer.schedule( new IdleCheckTask(), this.idleCheckInterval, this.idleCheckInterval);
			}
			InputStream in = null;
			try {
				in = socket.getInputStream();
				while (!isStop) {
					this.runTask(socket, in);
				}
			} catch (Exception e) {
				EMPLog.log(EMPConstance.EMP_TCPIP, EMPLog.ERROR, 0,
						"The keepAlive Socket "+this.socketInfo+" is terminated!", e);
			} finally {
				this.closeCurrentThread(socket, in, null);
			}
		} else {
			if(this.idleCheck){
				idleCheckTimer = new Timer();
				idleCheckTimer.schedule( new IdleCheckTask(), this.idleCheckInterval, this.idleCheckInterval);
			}
			
			InputStream in = null;
			try {
				in = socket.getInputStream();
				this.runTask(this.socket, in);
			} catch (Exception e) {
				EMPLog.log(EMPConstance.EMP_TCPIP, EMPLog.ERROR, 0,
						"An error is occured while handle the socket "+this.socketInfo+" in SocketProcessThread!", e);
			} finally {
				this.closeCurrentThread(socket, in, null);
			}
		}
	}

	protected void runTask(Socket socket, InputStream in) throws Exception {
		if (socket == null || in == null)
			throw new EMPException("The socket instance is null in SocketProcessThread!");
		
		byte[] readMsg = commProcessor.readPackage(in);
		synchronized (synObj) {
			this.lastAccess = System.currentTimeMillis();
			isHandle = true;
		}
		if (readMsg != null && packageReceiver != null) {
			packageReceiver.newPackageReceived(readMsg, socket);
		}
		synchronized (synObj) {
			isHandle = false;
		}
	}

	protected void closeCurrentThread(Socket socket, InputStream in, OutputStream out) {
		this.fireSocketToBeClosedEvent(socket);
		isStop = true;
		if (in != null) {
			try {
				in.close();
			} catch (Exception e) {
			}
		}
		if(out != null){
			try {
				out.close();
			} catch (Exception e) {
			}
		}
		if (socket != null) {
			try {
				socket.close();
			} catch (Exception e) {
			}
		}
		this.fireSocketClosedEvent();
		
		//关闭线程时释放监听
		isStop = true;
		isHandle = true;		
		//EMPLog.log(EMPConstance.EMP_TCPIP, EMPLog.INFO, 0, "SocketProcessThread closed the socket "+this.socketInfo);
	}

	public void sendRejectMessage() {
		Socket socket = this.socket;
		byte[] rejectMessageBytes = this.rejectMessageBytes;
		
		if (socket == null || rejectMessageBytes == null)
			return;
		
		OutputStream out = null;
		try {
			EMPLog.log(EMPConstance.EMP_TCPIP, EMPLog.INFO, 0,
					"Reject the socket "+this.socketInfo);
			out = socket.getOutputStream();
			out.write(rejectMessageBytes);
			out.flush();
		} catch (Exception e) {
			EMPLog.log(EMPConstance.EMP_TCPIP, EMPLog.INFO, 0,
					"Failed to reject the socket "+this.socketInfo, e);
		} finally {
			this.closeCurrentThread(socket, null, out);
		}
	}

	/**
	 * 侦听端口关闭一个socket之前触发的动作
	 * 
	 * @param socket
	 */
	public void fireSocketToBeClosedEvent(Socket socket) {
		if (socketAcceptListeners == null)
			return;
		for (int i = 0; i < socketAcceptListeners.size(); i++) {
			SocketListener listener = (SocketListener) socketAcceptListeners.get(i);
			listener.beforeSocketClosed(socket);
		}
	}

	/**
	 * 侦听端口关闭了一个socket
	 * 
	 * @param socket
	 */
	public void fireSocketClosedEvent() {
		if (socketAcceptListeners == null)
			return;
		for (int i = 0; i < socketAcceptListeners.size(); i++) {
			SocketListener listener = (SocketListener) socketAcceptListeners.get(i);
			listener.afterSocketClosed();
		}
	}
	
	/**
	 * 长连接情况下，如果长时间空闲，则释放相应的通道
	 * @author liubq
	 *
	 */
	private class IdleCheckTask extends TimerTask {

		public void run() {
			synchronized (synObj) {
				if(isHandle){
					idleCheckTimer.cancel();
					return;
				}
				
				long duringTime = System.currentTimeMillis() - lastAccess;
				if(duringTime > idleTimeOut){
//					EMPLog.log(EMPConstance.EMP_TCPIP, EMPLog.INFO, 0,"The keepAlive Socket "+socketInfo+" is idle for "+duringTime+" ms!");
					isStop = true;
					isHandle = true;
					closeCurrentThread(socket, null, null);
				}
			}
		}
	}


	public void destroyTask() {
		this.sendRejectMessage();
	}

	public void rejectTask() {
		this.sendRejectMessage();
	}
	
	public long leftHealthyTime() {
		if(this.waitTime > 0){
			long time = System.currentTimeMillis() - this.lastAccess;
			if(time >= this.waitTime){//超时，返回-1表示不处于健康状态
				return -1;
			}
			return (this.waitTime - time);
		}
		return 0;
	}
	
	
	public long getLastAccess() {
		return lastAccess;
	}

	public CommProcessor getCommProcessor() {
		return commProcessor;
	}

	public boolean isKeepAlive() {
		return keepAlive;
	}

	public PackageReceiver getPackageReceiver() {
		return packageReceiver;
	}

	public Socket getSocket() {
		return socket;
	}

	public List getSocketAcceptListeners() {
		return socketAcceptListeners;
	}

	public void setSocketAcceptListeners(List socketAcceptListeners) {
		this.socketAcceptListeners = socketAcceptListeners;
	}

	public boolean isIdleCheck() {
		return idleCheck;
	}

	public void setIdleCheck(boolean idleCheck) {
		this.idleCheck = idleCheck;
	}

	public int getIdleTimeOut() {
		return idleTimeOut;
	}

	public void setIdleTimeOut(int idleTimeOut) {
		this.idleTimeOut = idleTimeOut;
	}

	public int getIdleCheckInterval() {
		return idleCheckInterval;
	}

	public void setIdleCheckInterval(int idleCheckInterval) {
		this.idleCheckInterval = idleCheckInterval;
	}

	public boolean isStop() {
		return isStop;
	}
	
	public byte[] getRejectMessageBytes() {
		return rejectMessageBytes;
	}

	public void setRejectMessageBytes(byte[] rejectMessageBytes) {
		this.rejectMessageBytes = rejectMessageBytes;
	}
	
	public long getWaitTime() {
		return waitTime;
	}

	public void setWaitTime(long waitTime) {
		this.waitTime = waitTime;
	}
	
	public String getSocketInfo() {
		return socketInfo;
	}
}
