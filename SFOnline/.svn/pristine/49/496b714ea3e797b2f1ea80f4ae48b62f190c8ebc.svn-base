package core.communication.tcpip;

import java.util.ArrayList;
import java.util.List;

import com.ecc.emp.core.EMPConstance;
import com.ecc.emp.core.EMPException;
import com.ecc.emp.log.EMPLog;


/**
 * 
 * ConnectToHost.java<br>
 * Copyright (c) 2000, 2006 e-Channels Corporation <br>
 *
 * @author zhongmc<br>
 * @version 1.0.0<br>
 * @since 2007-1-9<br>
 * @lastmodified 2009-8-10<br>
 * @modifier liubq
 * 
 * 连接到某个TCP/IP服务器的处理类
 *
 * @emp:name 主机连接
 * @emp:document 设置连接的TCPIP主机的相关配置
 */
public class ConnectToHost {

	/**
	 * 主机地址
	 */
	protected String hostAddr;
	
	/**
	 * 端口
	 */
	protected int port = 0;

	/**
	 * 是否是长连接，缺省：否
	 */
	protected boolean keepAlive = false;
	
	/**
	 * 最近一次交易时间
	 */
	private int lastCommTime = 0;

	/**
	 * 是否需要按照最近的交易时间控制最大连接数，缺省：否
	 */
	private boolean connectionCtrl = false;

	/**
	 * 按照交易时间控制最大连接数时的斜率
	 */
	private float rate;
	
	/**
	 * 通讯协议处理接口
	 */
	protected CommProcessor commProcessor;

	/**
	 * 最大连接数，缺少：10
	 */
	private int maxConnection = 10;	

	/**
	 * 实际的连接列表
	 */
	private List connections = new ArrayList();;

	/**
	 * 最大的阻塞超时时间，缺省：永远不会超时
	 */
	private int soTimeOut = 0;

	/**
	 * 被使用的连接个数
	 */
	private int inUsedConnection = 0;

	/**
	 * 服务器是否可用
	 */
	private boolean alive = true;
	
	/**
	 * SocketListener列表
	 */
	private List socketAcceptListeners;
	
	/**
	 * 是否是双工通讯模式，缺省：是
	 * 该属性用于标识，接收的信息是否是从该socket返回
	 */
	protected boolean dual = true;
	
	//断线后是否立即重连
	private boolean reconnectImmediately = false;

	/**
	 * 轮询权重，缺省：1
	 * 该属性用于TCPIPService在轮询获取ConnectToHost时定义轮询的权重
	 */
	private int pollingWeight = 1;

	public ConnectToHost(){
		super();
	}
	
	public ConnectToHost(String hostAddr, int port){
		super();
		this.hostAddr = hostAddr;
		this.port = port;
	}

	public void send(byte[] msg) throws EMPException {
		HostConnection connection = null;
		try {
			connection = this.getConnection();
			connection.send(msg);
			this.alive = true;
		} catch (EMPException ee) {
			this.alive = false;
			throw ee;
		} catch (Exception e) {
			this.alive = false;
			throw new EMPException("Exception in send message to [" + this.hostAddr+":"+this.port+"]!",
					e);
		} finally {
			if (connection != null)
				this.releaseConnection(connection);
		}
	}

	public byte[] receive(Object identity, int timeOut) throws EMPException {
		return null;
	}

	public byte[] sendAndWait(byte[] msg, int timeOut) throws EMPException {
		HostConnection connection = null;
		try {
			long beg = System.currentTimeMillis();
			connection = this.getConnection();
			byte[] retMsg = connection.sendAndWait(msg, timeOut);
			this.lastCommTime = (int) (System.currentTimeMillis() - beg);
			this.alive = true;
			return retMsg;
		} catch (EMPException ee) {
			this.alive = false;
			throw ee;
		} catch (Exception e) {
			this.alive = false;
			throw new EMPException("Exception in send message to [" + this.hostAddr+":"+this.port+"]!",
					e);
		} finally {
			if (connection != null)
				this.releaseConnection(connection);
		}
	}

	/**
	 * 对于长连接的情况，向服务器发送空闲报文
	 * @param idlePkg
	 * @param idleTime
	 * @param dual
	 */
	protected void sendIdlePackage(byte[] idlePkg, long idleTime) {
		if (!this.keepAlive)
			return;
		
		synchronized (this) {
			for (int i = 0; i < this.connections.size(); i++) {
				HostConnection conc = (HostConnection) connections.get(i);
				if (conc.isInUse())
					continue;
				
				if ((System.currentTimeMillis() - conc.getLastAccess()) > idleTime) {
					conc.setInUse(true);
					try {
						if (this.dual)
							conc.sendAndWait(idlePkg, this.soTimeOut);
						else
							conc.send(idlePkg);
					} catch (Exception e) {
						EMPLog.log(EMPConstance.EMP_TCPIP, EMPLog.WARNING, 0,
								"Failed to sendIdlePackage!", e);
					}
					conc.setInUse(false);
				}
			}
		}
	}
	
	/**
	 * 得到当前可以建立的最大连接数
	 * 
	 * @return
	 */
	protected int getAllowedConnections() {
		if (!connectionCtrl)
			return maxConnection;
		else {
			int value = (int) (maxConnection - this.rate * this.lastCommTime
					/ 1000);
			if (value <= 0)
				value = 0;
			return value;
		}
	}
		
	
	protected synchronized HostConnection getConnection() throws EMPException {
		
		for (int i = 0; i < this.connections.size(); i++) {
			HostConnection connection = (HostConnection) connections.get(i);
			if (!connection.isInUse()) {
				connection.setInUse(true);
				return connection;
			}
		}

		if (connections.size() < maxConnection || maxConnection <= 0) {
			HostConnection connection = new HostConnection(this.hostAddr,
					this.port, this.commProcessor, this.keepAlive, this.dual);
			connection.setSoTimeOut(soTimeOut);
			connection.setSocketAcceptListeners(socketAcceptListeners);
			connection.setReconnectImmediately(this.reconnectImmediately);
			
			connection.setInUse(true);
			connections.add(connection);
			return connection;
		}
		
		throw new EMPException("All connections are unavailable for ["
				+ this.hostAddr + ":" + this.port + "]!");
	}
	
	protected synchronized void releaseConnection(HostConnection connection) {
		connection.setInUse(false);
	}

	public void terminate() {
		for (int i = 0; i < this.connections.size(); i++) {
			HostConnection connection = (HostConnection) connections.get(i);
			connection.terminate();
			
			//已经关闭的connection应该被释放
			this.releaseConnection(connection);
		}
	}
	
	public CommProcessor getCommProcessor() {
		return commProcessor;
	}
	
	public int getSoTimeOut() {
		return soTimeOut;
	}

	public float getRate() {
		return rate;
	}

	public boolean isAlive() {
		return alive;
	}

	public void setAlive(boolean alive) {
		this.alive = alive;
	}

	public void setInUsedConnection(int inUsedConnection) {
		this.inUsedConnection = inUsedConnection;
	}
	
	public int getInUsedConnection() {
		return inUsedConnection;
	}

	public int getMaxConnection() {
		return maxConnection;
	}
	
	public boolean isConnectionCtrl() {
		return connectionCtrl;
	}
	
	public String getHostAddr() {
		return hostAddr;
	}

	public boolean isKeepAlive() {
		return keepAlive;
	}

	public int getPort() {
		return port;
	}
	
	public List getConnections() {
		return connections;
	}

	public void setConnections(List connections) {
		this.connections = connections;
	}

	public int getLastCommTime() {
		return lastCommTime;
	}

	public void setLastCommTime(int lastCommTime) {
		this.lastCommTime = lastCommTime;
	}
	
	public boolean isDual() {
		return dual;
	}
	

	/**
	 * 添加Socket连接监听者
	 * @param listener
	 */
	public void addSocketListener( SocketListener listener )
	{
		if( this.socketAcceptListeners == null )
			socketAcceptListeners = new ArrayList();
		socketAcceptListeners.add(listener );
	}
	
	/**
	 * @emp:name 单/双工模式
	 * @emp:valueList true=双工;false=单工;
	 * @emp:mustSet false
	 * @emp:document 选择单工或双工模式。缺省：双工
	 */
	public void setDual(boolean dual) {
		this.dual = dual;
	}

	/**
	 * @emp:name 主机地址
	 * @emp:mustSet true
	 * @emp:document 所要连接的主机地址
	 */
	public void setHostAddr(String hostAddr) {
		this.hostAddr = hostAddr;
	}

	/**
	 * @emp:name 是否是长连接
	 * @emp:valueList true=是;false=否;
	 * @emp:mustSet false
	 * @emp:document 是否是长连接。缺省：否
	 */
	public void setKeepAlive(boolean keepAlive) {
		this.keepAlive = keepAlive;
	}

	/**
	 * @emp:name 端口
	 * @emp:mustSet true
	 * @emp:document 所要连接的主机端口
	 */
	public void setPort(int port) {
		this.port = port;
	}
	
	/**
	 * @emp:name 最大连接数
	 * @emp:mustSet false
	 * @emp:document 设置最大连接数。缺省：10个连接
	 */
	public void setMaxConnection(int maxConnection) {
		this.maxConnection = maxConnection;
	}

	/**
	 * @emp:name 是否动态控制最大连接数
	 * @emp:valueList true=是;false=否;
	 * @emp:mustSet false
	 * @emp:document 是否需要按照最近的交易时间控制最大连接数，缺省：否
	 */
	public void setConnectionCtrl(boolean connectionCtrl) {
		this.connectionCtrl = connectionCtrl;
	}
	
	/**
	 * @emp:name 最大连接数的变化斜率
	 * @emp:mustSet false
	 * @emp:document 按照交易时间控制最大连接数时的斜率。按照（最近交易时间的秒数＊斜率）来动态减少当前的最大连接数
	 */
	public void setRate(float rate) {
		this.rate = rate;
	}
	
	/**
	 * @emp:name 最大的阻塞超时时间
	 * @emp:mustSet false
	 * @emp:document Socket发生阻塞后的最大超时时间。缺省：0(永远不超时)
	 */
	public void setSoTimeOut(int soTimeOut) {
		this.soTimeOut = soTimeOut;
	}
	
	/**
	 * @emp:name 通讯协议处理接口
	 * @emp:mustSet false
	 * @emp:editorClass class
	 * @emp:document 通讯协议处理接口的实现类，通常由TCPIPService进行设置
	 */
	public void setCommProcessor(CommProcessor commProcessor) {
		this.commProcessor = commProcessor;
	}
	
	
	public int getPollingWeight() {
		return pollingWeight;
	}
	
	public boolean isReconnectImmediately() {
		return reconnectImmediately;
	}
	
	/**
	 * @emp:name 是否断线后立即重连
	 * @emp:valueList true=是;false=否;
	 * @emp:mustSet false
	 * @emp:document 在长连接情况下，Socket中断后是否立即进行重连，缺省：否
	 */
	public void setReconnectImmediately(boolean reconnectImmediately) {
		this.reconnectImmediately = reconnectImmediately;
	}
	
	/**
	 * @emp:name 轮询权重
	 * @emp:mustSet false
	 * @emp:document 该属性用于TCPIPService在轮询获取ConnectToHost时定义轮询的权重
	 */
	public void setPollingWeight(int pollingWeight) {
		if(pollingWeight < 1)
			pollingWeight = 1;
		this.pollingWeight = pollingWeight;
	}

	public List getSocketAcceptListeners() {
		return socketAcceptListeners;
	}

	public void setSocketAcceptListeners(List socketAcceptListeners) {
		this.socketAcceptListeners = socketAcceptListeners;
	}
	
}
