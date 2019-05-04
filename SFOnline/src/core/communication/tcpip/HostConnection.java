package core.communication.tcpip;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;

import com.ecc.emp.core.EMPConstance;
import com.ecc.emp.core.EMPException;
import com.ecc.emp.log.EMPLog;


/**
 * 
 * HostConnection.java<br>
 * Copyright (c) 2000, 2006 e-Channels Corporation <br>
 *
 * @author zhongmc<br>
 * @version 1.0.0<br>
 * @since 2007-1-9<br>
 * @lastmodified 2007-1-9<br>
 *
 *代表与某个TCP/IP服务器的连接
 *
 */

public class HostConnection {

	
	private CommProcessor commProcessor = null;

	private boolean isInUse = false;

	private String hostAddr;

	private int port;

	private Socket socket;

	private OutputStream out;

	private InputStream in;

	private boolean keepAlive = true;
	
	private boolean dual = true;

	/**
	 * SocketListener列表
	 */
	private List socketAcceptListeners;

	private int soTimeOut = 0;

	//最后一次访问时间戳
	private long lastAccess = 0l;
	
	//断线后是否立即重连
	private boolean reconnectImmediately = false;

	private Thread checkSocketThread = null;
	
	protected String socketInfo;
	
	
	public HostConnection(String hostAddr, int port, CommProcessor commProcessor, boolean keepAlive, boolean dual) {
		super();
		this.hostAddr = hostAddr;
		this.port = port;
		this.keepAlive = keepAlive;
		this.commProcessor = commProcessor;
		this.dual = dual;
	}

	/**
	 * 发送请求
	 * @param msg
	 * @throws EMPException
	 */
	public void send(byte[] msg) throws EMPException {
		try {
			lastAccess = System.currentTimeMillis();
			connect();

			byte[] wrapedMsg = commProcessor.wrapMessagePackage(msg);
			out.write(wrapedMsg, 0, wrapedMsg.length);
			if (!keepAlive)
				close();
		} catch (IOException ie) {
			close();
			throw new EMPException("IO Exception in send socket to " + hostAddr
					+ ":" + port, ie);
		} catch (Exception e) {
			close();
			throw new EMPException("Exception in send socket to " + hostAddr
					+ ":" + port, e);

		}

	}

	/**
	 * 发送请求，并等待返回
	 * @param msg
	 * @param timeOut
	 * @return
	 * @throws EMPException
	 */
	public byte[] sendAndWait(byte[] msg, int timeOut) throws EMPException {
		try {
			lastAccess = System.currentTimeMillis();
			connect();
			byte[] wrapedMsg = commProcessor.wrapMessagePackage(msg);
			socket.setSoTimeout(timeOut);
			out.write(wrapedMsg);
			byte[] retMsg = this.commProcessor.readPackage(in);
			if (!keepAlive)
				close();

			return retMsg;
		} catch (InterruptedIOException ire) //time Out
		{
			close(); //should close the socket when timeout???
			throw new EMPException("Time out in read socket " + this.socketInfo, ire);
		} catch (IOException ie) {
			close();
			throw new EMPException("IO Exception in read socket " + this.socketInfo, ie);

		} catch (Exception e) {
			close();
			throw new EMPException("Exception in read socket " + this.socketInfo, e);
		}
	}

	/**
	 * 连接到服务器
	 * @throws Exception
	 */
	public void connect() throws Exception {

		if (socket != null)
			return;
		
		this.fireSocketToBeConnectEvent();
		
		socket = new Socket(hostAddr, port);
		if (soTimeOut > 0)
			socket.setSoTimeout(soTimeOut);
		socket.setSoLinger(true, 0);
		socket.setTcpNoDelay(true);
		//socket.setReuseAddress(true);
		
		this.fireSocketConnectedEvent(socket);
		
		this.socketInfo = "["+this.socket.getLocalSocketAddress()+"<-->"+this.socket.getRemoteSocketAddress()+"]";

		EMPLog.log(EMPConstance.EMP_TCPIP, EMPLog.INFO, 0,
				"HostConnection connected the socket "+this.socketInfo);
		
		out = socket.getOutputStream();
		in = socket.getInputStream();
		
		if(this.keepAlive == true && this.dual == false){
			checkSocketThread = new Thread(new CheckSocketThread(this.in));
			checkSocketThread.start();
		}
	}

	/**
	 * 关闭与服务器的连接
	 */
	public void close() {
		if (this.socket == null)
			return;
		
		this.fireSocketToBeClosedEvent(socket);
		
		Socket socket = this.socket;
		InputStream in = this.in;
		OutputStream out = this.out;
		this.in = null;
		this.out = null;
		this.socket = null;
		
		try {
			in.close();
		} catch (Exception e) {
		}
		try {
			out.close();
		} catch (Exception e) {
		}
		try {
			socket.close();
		} catch (Exception e) {
		}
		
		this.fireSocketClosedEvent();

		EMPLog.log(EMPConstance.EMP_TCPIP, EMPLog.INFO, 0,
				"HostConnection closed the socket "+this.socketInfo);
		
		//如果是长连接且断线后需要立即重连，且立即重连
		if(this.keepAlive && this.reconnectImmediately){
			try {
				this.connect();
			} catch (Exception e) {
				EMPLog.log(EMPConstance.EMP_TCPIP, EMPLog.ERROR, 0,
						"IO Exception occured when reconnect the socket "+this.socketInfo, e);
			}
		}
	}

	public void terminate() {
		close();
	}
	
	protected class CheckSocketThread implements Runnable {
		
		private InputStream in = null;
		
		public CheckSocketThread(InputStream in) {
			this.in = in;
		}

		public void run() {
			while (true) {
				try {
					int ret = this.in.read();
					if (ret < 0) {
						throw new EMPException("Socket was closed!");
					}
				} catch (Exception e) {
					EMPLog.log(EMPConstance.EMP_TCPIP, EMPLog.ERROR, 0,
							"IO Exception in keep alive socket "+socketInfo, e);
					close();
					break;
				}
			}
		}
	}
	


	/**
	 * Socket接入之前触发的动作
	 *
	 */
	public void fireSocketToBeConnectEvent(){
		if( socketAcceptListeners == null )
			return;
		for( int i=0; i<socketAcceptListeners.size(); i++ )
		{
			SocketListener listener = ( SocketListener )socketAcceptListeners.get( i );
			listener.beforeSocketConnected();
		}
	}
	
	/**
	 * Socket接入之后触发的动作
	 * @param socket
	 */
	public void fireSocketConnectedEvent(Socket socket )
	{
		if( socketAcceptListeners == null )
			return;
		for( int i=0; i<socketAcceptListeners.size(); i++ )
		{
			SocketListener listener = ( SocketListener )socketAcceptListeners.get( i );
			listener.afterSocketConnected(socket);
		}
	}
	
	/**
	 * 侦听端口关闭一个socket之前触发的动作
	 * @param socket
	 */
	public void fireSocketToBeClosedEvent( Socket socket )
	{
		if( socketAcceptListeners == null )
			return;
		for( int i=0; i<socketAcceptListeners.size(); i++ )
		{
			SocketListener listener = ( SocketListener )socketAcceptListeners.get( i );
			listener.beforeSocketClosed( socket );
		}
	}
	
	/**
	 * 侦听端口关闭了一个socket
	 * @param socket
	 */
	public void fireSocketClosedEvent()
	{
		if( socketAcceptListeners == null )
			return;
		for( int i=0; i<socketAcceptListeners.size(); i++ )
		{
			SocketListener listener = ( SocketListener )socketAcceptListeners.get( i );
			listener.afterSocketClosed();
		}
	}
	

	public String getHostAddr() {
		return hostAddr;
	}

	public boolean isConnectionOk() {
		return socket != null;
	}

	public void setInUse(boolean value) {
		isInUse = value;
	}

	public boolean isInUse() {
		return isInUse;
	}

	public int getSoTimeOut() {
		return soTimeOut;
	}

	public void setSoTimeOut(int soTimeOut) {
		this.soTimeOut = soTimeOut;
	}

	public long getLastAccess() {
		return lastAccess;
	}

	public void setLastAccess(long lastAccess) {
		this.lastAccess = lastAccess;
	}

	public CommProcessor getCommProcessor() {
		return commProcessor;
	}

	public void setCommProcessor(CommProcessor commProcessor) {
		this.commProcessor = commProcessor;
	}

	public InputStream getIn() {
		return in;
	}

	public void setIn(InputStream in) {
		this.in = in;
	}

	public boolean isKeepAlive() {
		return keepAlive;
	}

	public void setKeepAlive(boolean keepAlive) {
		this.keepAlive = keepAlive;
	}

	public OutputStream getOut() {
		return out;
	}

	public void setOut(OutputStream out) {
		this.out = out;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public Socket getSocket() {
		return socket;
	}

	public void setSocket(Socket socket) {
		this.socket = socket;
	}

	public void setHostAddr(String hostAddr) {
		this.hostAddr = hostAddr;
	}

	public boolean isDual() {
		return dual;
	}

	public void setDual(boolean dual) {
		this.dual = dual;
	}

	public List getSocketAcceptListeners() {
		return socketAcceptListeners;
	}

	public void setSocketAcceptListeners(List socketAcceptListeners) {
		this.socketAcceptListeners = socketAcceptListeners;
	}

	public Thread getCheckSocketThread() {
		return checkSocketThread;
	}
	
	public boolean isReconnectImmediately() {
		return reconnectImmediately;
	}

	public void setReconnectImmediately(boolean reconnectImmediately) {
		this.reconnectImmediately = reconnectImmediately;
	}
	
	public String getSocketInfo() {
		return socketInfo;
	}
}
