package core.communication.tcpip;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.ecc.emp.comm.EMPTimeOutException;
import com.ecc.emp.core.EMPConstance;
import com.ecc.emp.core.EMPException;
import com.ecc.emp.jmx.support.EMPJMXManager;
import com.ecc.emp.log.EMPLog;
import com.ecc.emp.service.EMPService;


/**
 * 
 * TCPIPService.java<br>
 * Copyright (c) 2000, 2006 e-Channels Corporation <br>
 *
 * @author zhongmc<br>
 * @version 1.0.0<br>
 * @since 2007-1-9<br>
 * @lastmodified 2007-1-9<br>
 *
 *	EMP提供的TCPIP服务，允许监听端口，连接到服务器端口，定义是否长连接
 *
 */
public class TCPIPService extends EMPService implements PackageReceiver
{
	protected List listenPorts = new ArrayList();

	protected List connectToHosts = new ArrayList();
	
	/**
	 * 异步返回的报文队列，队列中每一项是一个数组
	 * 数组第一个值为返回报文的内容
	 * 数组第二个值为返回的时间
	 */
	protected List receivedMsg = new ArrayList();
	
	/**
	 * 当前等待返回报文的线程锁对象列表
	 */
	protected List waitReceiveThreadList = new ArrayList();
	
	/**
	 * 通信协议处理器，用于处理通信协议
	 * 缺省是EMPCommProcessor
	 */
	protected CommProcessor commProcessor = new EMPCommProcessor();

	/**
	 * 报文处理器
	 */
	protected PackageProcessor packageProcessor;
	
	/**
	 * 报文识别器
	 */
	protected PackageIdentity packageIdentity;
	
	/**
	 * 是否为双工工作，在同时有连接出去和进来的时候，是否需要在同一个连接上处理请求/响应
	 */
	protected boolean dual = true;
	
	/**
	 * 是否长连接
	 */
	protected boolean keepAlive = false;
	
	/**
	 * 用于发送时轮询确定发送到的主机
	 */
	private int curConnectToIdx = 0;
	
	/**
	 * 等待获得连接的线程数
	 */
	private int waitConnectionCount = 0; 
	
	/**
	 * 是否使用线程池处理新报文请求
	 */
	private boolean poolThread = false;
	
	/**
	 * 线程池大小
	 */
	private int poolSize = 10;
	
	/**
	 * 线程池队列
	 */
	private List threadPool = new ArrayList();
	
	/**
	 * 是否需要进行服务器可用检测
	 */
	private boolean aliveCheck = false;

	/**
	 * 用于服务器可用检测的报文，缺省为空串（不考虑通信协议的情况下）
	 */
	private String aliveCheckPackage = "";

	/**
	 * 进行服务器检测的时间间隔，缺省：60000
	 */
	private long aliveCheckInterval = 60000;

	/**
	 * 是否需要在长时间空闲时发送心跳报文
	 */
	private boolean sendIdlePkg = false;

	/**
	 * 心跳报文，缺省为空串（不考虑通信协议的情况下）
	 */
	private String idlePackage = "";
	
	/**
	 * 检查发送心跳报文的间隔，缺省：60000
	 */
	private long idleInterval = 60000;
	
	/**
	 * 空闲时间（超过此时间没有活动，将发生心跳报文）
	 */
	private long idleTime = 180000;
	
	/**
	 * 通信超时时间
	 */
	private long timeOut = 10000;
	
	/**
	 * 获取连接的最大等待时间
	 */
	private long connectWaitTime = 1000;
	

	/**
	 * 返回消息轮询的时间间隔
	 */
	private int messagePollTime = -1;
	
	/**
	 * 是否针对异步返回的报文进行超时判断
	 * 如果超时，则当作垃圾报文进行丢弃
	 */
	private boolean dirtyCheck = false;
	

	/**
	 * 接收到的报文超时时间
	 */
	private int dirtyTimeOut = 10 * 60000;

	/**
	 * 轮询接收报文的间隔时间
	 */
	private int dirtyInterval = 2 * 60000;
	
	/**
	 * 检查TCPIP连接的健康状态的计时器
	 */
	protected Timer aliveCheckTimer = null;
	
	/**
	 * 检查TCPIP连接空闲状态的计时器
	 */
	protected Timer idleCheckTimer = null;
	
	/**
	 * 检查需要丢弃的返回报文的计时器
	 */
	protected Timer dirtyCheckTimer = null;
	
	/**
	 * 当前ConnectToHost已经使用的权重数(只用在getConnectToHost方法中)
	 */
	private int currentPollingWeight = 0;
	
	/**
	 * 初始化
	 */
	public void initialize()
	{
		for(int i=0; i<this.listenPorts.size(); i++ )
		{
			ListenPort listenPort = (ListenPort)listenPorts.get( i );
			listenPort.commProcessor = this.commProcessor;
			listenPort.packageReceiver = this;
			listenPort.setKeepAlive( this.keepAlive );
			listenPort.startUp();
		}
		for(int i=0; i<this.connectToHosts.size(); i++ )
		{
			ConnectToHost connection = (ConnectToHost)connectToHosts.get( i );
			connection.commProcessor = this.commProcessor;
			connection.setKeepAlive(this.keepAlive);
			connection.setDual(this.dual);
			
			if(!this.keepAlive && connection.isReconnectImmediately()){
				EMPLog.log(EMPConstance.EMP_TCPIP, EMPLog.WARNING, 0,
					"The ConnectToHosts["+i+"] cannot reconnect immediately when the keepAlive is false!");
			}
		}
		
		//短连接情况下，不需要发送空闲报文
		if(!this.keepAlive && this.sendIdlePkg){
			EMPLog.log(EMPConstance.EMP_TCPIP, EMPLog.WARNING, 0,
					"The SendIdlePkg is better to be false when keepAlive is false in TCPIPService!");
		}
		
		//在无ListenPort情况下，绝大部分情况下不会当作异步处理
		if((this.listenPorts == null || this.listenPorts.size() == 0) && this.dirtyCheck){
			EMPLog.log(EMPConstance.EMP_TCPIP, EMPLog.WARNING, 0,
					"The DirtyCheck is better to be false when there is no ListenPort in TCPIPService!");
		}
		
		if(this.aliveCheck){
			aliveCheckTimer = new Timer();
			aliveCheckTimer.schedule( new AliveCheckTask(), this.aliveCheckInterval, this.aliveCheckInterval);
		}
		
		if(this.sendIdlePkg){
			idleCheckTimer = new Timer();
			idleCheckTimer.schedule( new IdleCheckTask(), this.idleInterval, this.idleInterval);
		}
		
		if(this.dirtyCheck){
			dirtyCheckTimer = new Timer();
			dirtyCheckTimer.schedule( new DirtyPackageCheckTask(), this.dirtyTimeOut, this.dirtyInterval);
		}
	}
	
	/**
	 * 取得当前的连接并发数
	 * @return
	 */
	public int getConnectionCount() {
		if (this.connectToHosts == null || this.connectToHosts.size() == 0)
			return 0;
		int count = 0;
		for (int i = 0; i < this.connectToHosts.size(); i++) {
			ConnectToHost connection = (ConnectToHost) connectToHosts.get(i);
			count += connection.getInUsedConnection();
		}
		return count;
	}
	
	/**
	 * 取得正在等待获得连接的线程数
	 * @return
	 */
	public int getWaitConnectionCount() {
		return this.waitConnectionCount;
	}
	
	
	/**
	 * 发送请求，并等待返回，timeOut：如果大于0，代表超时时间，否则使用
	 * 服务设定的超时时间，默认为10秒
	 * 
	 * @param identity
	 * @param msg
	 * @param timeOut
	 * @return
	 * @throws IOException
	 * @throws EMPException
	 */
	public byte[] sendAndWait(Object identity, byte[] msg, int timeOut )throws EMPException
	{
		long timeOutValue = timeOut;
		if (timeOutValue <= 0)
			timeOutValue = this.getTimeOut();
		
		if (this.listenPorts.size() > 0 && !dual) //单工模式下，且必须要有侦听端口
		{
			this.send(msg);
			return this.receive(identity, timeOutValue);
		} else {
			ConnectToHost connection = null;
			try{
				connection = this.getConnecToHost();
				return connection.sendAndWait(msg, (int) timeOutValue);
			} catch (EMPTimeOutException e) {
				EMPJMXManager.sendNotification(this, this.getId(), "WARNING", e.getMessage());
				throw e;
			} catch(EMPException e){
				throw e;
			} finally{
				if(connection != null)
					this.releaseConnecToHost(connection);
			}
		}
	}
	
	
	public void send(byte[] msg )throws EMPException
	{
		ConnectToHost connection = null;
		try{
			connection = this.getConnecToHost();
			connection.send(msg);
		}catch(EMPException e){
			throw e;
		}finally{
			if(connection != null)
				this.releaseConnecToHost(connection);
		}
	}

	
	public byte[] send(byte[] msg, Socket socket )throws IOException, EMPException
	{
		if( this.dual )
		{
			byte[] sendMsg = this.commProcessor.wrapMessagePackage( msg );
			
//			EMPLog.log(EMPConstance.EMP_TCPIPACCESS, EMPLog.INFO, 0,
//					"TCPIPService return: "
//							+ new String(sendMsg));
			
			OutputStream out = socket.getOutputStream();
			out.write( sendMsg );
			
			return sendMsg;
		}
		else
		{
			this.send(msg);
			return msg;
		}
	}
	
	
	/**
	 * 获得一个可用的ConnectToHost
	 * @return
	 * @throws EMPException
	 */
	protected synchronized ConnectToHost getConnecToHost() throws EMPException {
		long begin = System.currentTimeMillis();
		while(true){
			int count = 0;
			while (count < connectToHosts.size()) {
				ConnectToHost connection = (ConnectToHost) connectToHosts.get(curConnectToIdx);
				
				if (!isAliveCheck() || connection.isAlive()) {
					int inUsed = connection.getInUsedConnection();
					if (connection.getAllowedConnections() > inUsed) {
						inUsed++;
						connection.setInUsedConnection(inUsed);
						
						//按照轮询权重的定义决定是否轮询到下一个ConnectToHost定义
						this.currentPollingWeight ++;
						if(this.currentPollingWeight >= connection.getPollingWeight()){
							curConnectToIdx++;
							if (curConnectToIdx >= connectToHosts.size())
								curConnectToIdx = 0;
							this.currentPollingWeight = 0;
						}
						
						return connection;
					}
				}
				
				this.currentPollingWeight = 0;
				curConnectToIdx++;
				if (curConnectToIdx >= connectToHosts.size())
					curConnectToIdx = 0;
				count++;
			}

			if (this.connectWaitTime == -1) // no wait
			{
				throw new EMPException("No idle connection available!");
			}

			try {
				waitConnectionCount ++;
				if (this.connectWaitTime > 0) {
					long cur = System.currentTimeMillis();
					long waitTime = cur - begin;
					if (waitTime >= this.connectWaitTime) // wait timeout
					{
						throw new EMPException(
								"TimeOut to get connection!The waitTime="
										+ this.connectWaitTime + "ms!");
					}
					EMPLog.log(EMPConstance.EMP_TCPIP, EMPLog.DEBUG, 0, "等待时间："+(this.connectWaitTime - waitTime)+"/"+this.connectWaitTime);
					wait(this.connectWaitTime - waitTime);
				} else if (this.connectWaitTime == 0) {
					wait();
				}
			} catch (Exception e) {
				throw new EMPException("Error occured when wait for tcpip connection!", e);
			} finally{
				waitConnectionCount --;
			}
		}
	}
	
	/**
	 * 释放一个ConnectToHost
	 * @param connection
	 */
	protected synchronized void releaseConnecToHost(ConnectToHost connection){
		int inUsed = connection.getInUsedConnection();
		inUsed --;
		connection.setInUsedConnection(inUsed);
		notify();
	}
	

	/**
	 * 检查TCPIP连接的健康状态
	 */
	protected class AliveCheckTask extends TimerTask {
		
		public void run() {
			EMPLog.log(EMPConstance.EMP_TCPIP, EMPLog.DEBUG, 0,
					"Begin to Check the TCPIP Connect alive of: " + getName());

			Object[] keys = null;

			keys = connectToHosts.toArray();
			for (int i = 0; i < keys.length; i++) {
				ConnectToHost con = (ConnectToHost) keys[i];
				if (!con.isAlive()) {
					EMPLog.log(EMPConstance.EMP_TCPIP, EMPLog.INFO, 0,
							"The TCPIP Connection : " + con.toString()
									+ " was down!");

					try {
						if (con.isDual())
							con.sendAndWait(getAliveCheckPackage().getBytes(), con.getSoTimeOut());
						else
							con.send(getAliveCheckPackage().getBytes());
						
						//若有一个连接恢复了可用性，则应该唤醒一个线程
						synchronized(this){
							notify();
						}
						
					} catch (Exception e) {
						EMPLog.log(EMPConstance.EMP_TCPIP, EMPLog.WARNING, 0,
								"Can not to resume the TCPIP Connection : "
										+ con.toString()+"!");
					}
				}
			}
			EMPLog.log(EMPConstance.EMP_TCPIP, EMPLog.DEBUG, 0,
					"The Check of TCPIP Connect alive of " + getName()+" is end!");
		}
	}
	

	/**
	 * 检查TCPIP空闲状态
	 */
	protected class IdleCheckTask extends TimerTask {
		
		public void run() {
			EMPLog.log(EMPConstance.EMP_TCPIP, EMPLog.DEBUG, 0,
					"Begin to check idle for TCPIP Connect： " + getName());

			Object[] keys = null;
			
			keys = connectToHosts.toArray();
			for (int i = 0; i < keys.length; i++) {
				ConnectToHost con = (ConnectToHost) keys[i];
				con.sendIdlePackage(getIdlePackage().getBytes(), getIdleTime());
			}
			
			EMPLog.log(EMPConstance.EMP_TCPIP, EMPLog.DEBUG, 0,
					"The check idle for TCPIP Connect： " + getName() +" is end!");
		}
	}
	

	/**
	 * 检查需要丢弃的返回报文
	 */
	protected class DirtyPackageCheckTask extends TimerTask {
		
		public void run() {
			EMPLog.log(EMPConstance.EMP_TCPIP, EMPLog.DEBUG, 0,
					"Begin to check dirty package for TCPIP Connect： "
							+ getName());
			
			long curTime = System.currentTimeMillis();
			synchronized (receivedMsg) {
				for (int i = 0; i < receivedMsg.size(); i++) {
					Object[] msgObj = (Object[]) receivedMsg.get(i);
					if (msgObj == null)
						continue;
					long arriveTime = ((Long) msgObj[1]).longValue();// 得到报文的接收时间
					if ((curTime - arriveTime) >= dirtyTimeOut) {// 超过一定时间没有被接收，则认为是垃圾报文
						receivedMsg.remove(msgObj);
					}
				}
			}
			
			EMPLog.log(EMPConstance.EMP_TCPIP, EMPLog.DEBUG, 0,
					"The check dirty package for TCPIP Connect： "
							+ getName() + " is end!");

		}
	}
	
	/**
	 * 接收响应
	 * @param identity 传递给报文鉴别器的标识对象
	 * @param timeOut 超时时间，如果大于0，代表超时时间，否则使用设定的超时时间
	 * @return
	 * @throws EMPTimeOutException
	 */
	public byte[] receive(Object identity, long timeOut )throws EMPTimeOutException
	{
		long timeOutValue = timeOut;
		if (timeOutValue <= 0)
			timeOutValue = this.getTimeOut();
		
		long waitTime = -1;
		long beg = System.currentTimeMillis();
		while (true) {
			synchronized (receivedMsg) {
				this.waitReceiveThreadList.remove(identity);
				for (int i = 0; i < receivedMsg.size(); i++) {
					Object[] msgObj = (Object[]) this.receivedMsg.get(i);
					byte[] msg = (byte[]) msgObj[0];
					if (packageIdentity == null) {
						receivedMsg.remove(msgObj);
						return msg;
					}
					if (this.packageIdentity.isTargetPackage(identity, msg)) {
						receivedMsg.remove(msgObj);
						return msg;
					}
				}
				waitTime = timeOutValue - (System.currentTimeMillis() - beg);
				if (waitTime <= 0) {
					throw new EMPTimeOutException("Time out [over "+timeOutValue+"ms] when receive msg from TCPIPService");
				}
				this.waitReceiveThreadList.add(identity);
			}
			
			try {
				if (this.messagePollTime > 0 && waitTime > this.messagePollTime)
					waitTime = this.messagePollTime;
				EMPLog.log(EMPConstance.EMP_TCPIP, EMPLog.DEBUG, 0, "TCPIPService will wait receive return msg for " + waitTime + " ms.");
				
				synchronized (identity) {
					identity.wait(waitTime);
				}
			} catch (Exception e) {
				EMPLog.log(EMPConstance.EMP_TCPIP, EMPLog.ERROR, 0, "TCPIPService receive occur an error when waiting for the receiveMsg!", e);
			}
		}
	}

	public void newPackageReceived(byte[] aPackage, Socket socket)
	{
		if (packageProcessor != null) {
			if (packageProcessor.isRequestPackage(aPackage)) //如果是请求报文，则处理请求报文
			{
				processNewPackage(aPackage, socket);
			} else {
				addNewMessage(aPackage);
			}
		}

		else //save the message
		{
			addNewMessage(aPackage);
		}
			
	}

	

	/**
	 * 设置缓存的msgObj第一个元素是报文内容，第二个元素是接收到的时间
	 * @param msg
	 */
	protected void addNewMessage(byte[] msg) {

		Object msgIdentity = null;
		synchronized (receivedMsg) {
			Object msgObj[] = new Object[2];
			msgObj[0] = msg;
			msgObj[1] = new Long(System.currentTimeMillis());
			this.receivedMsg.add(msgObj);
			
			if(this.packageIdentity != null){
				for(int i=0;i<this.waitReceiveThreadList.size();i++){
					Object identity = this.waitReceiveThreadList.get(i);
					if(this.packageIdentity.isTargetPackage(identity, msg)){
						msgIdentity = identity;
						break;
					}
				}
			}
		}
		
		if(msgIdentity != null){
			synchronized (msgIdentity) {
				msgIdentity.notify();
			}
		}
	}
	
	private void processNewPackage(byte[] msg, Socket socket )
	{
		if (this.isPoolThread()) {
			synchronized (threadPool) {
				while (true) {
					for (int i = 0; i < this.threadPool.size(); i++) {
						PackageProcessThread packageThread = (PackageProcessThread) threadPool.get(i);
						if (packageThread.isBeFree()) {
							packageThread.setBeFree(false);
							packageThread.processPackage(msg, socket);
							return;
						}
					}

					if (threadPool.size() < this.getPoolSize()) {
						PackageProcessThread packageThread = new PackageProcessThread(
								packageProcessor, this, msg, socket);
						//packageThread.processPackage(msg, socket);
						threadPool.add(packageThread);
						return;
					}

					EMPLog.log(EMPConstance.EMP_TCPIP, EMPLog.WARNING, 0,
							"TCP/IP Service package process thread touch the max thread size:"
									+ this.getPoolSize());
					//waiting for free thread to process
					try {
						EMPLog.log(EMPConstance.EMP_TCPIP, EMPLog.INFO, 0,
								"TCPIPService: " + this
										+ "Waiting for free thread...");
						threadPool.wait();
					} catch (Exception e) {
						//EMPLog.log(EMPConstance.EMP_TCPIP, EMPLog.INFO, 0, "Failed to wait!", e);
					}
				}
			}

		} else {
			try {
				packageProcessor.processNewPackage(msg, this, socket);
			} catch (Exception e) {
				EMPLog.log(EMPConstance.EMP_TCPIP, EMPLog.ERROR, 0,
								"Failed to process received package!"
										+ new String(msg), e);
			}
		}
	}
	
	public void packageProcessorThreadFree(PackageProcessThread thread) {
		synchronized (threadPool) {
			thread.setBeFree(true);
			threadPool.notify();
		}
	}
	
	
	public void terminate()
	{

		for (int i = 0; i < this.listenPorts.size(); i++) {
			ListenPort listenPort = (ListenPort) listenPorts.get(i);
			listenPort.terminate();
		}
		for (int i = 0; i < this.connectToHosts.size(); i++) {
			ConnectToHost connection = (ConnectToHost) connectToHosts.get(i);
			connection.terminate();
		}
		if(this.aliveCheckTimer != null){
			try{
				this.aliveCheckTimer.cancel();
				this.aliveCheckTimer = null;
			}catch(Exception e){
				EMPLog.log(EMPConstance.EMP_TCPIP, EMPLog.ERROR, 0, "Failed to Terminate the aliveCheck for TCPIPServerService4CUPS [" + this.getName() + "]", e);
			}
		}
		
		if(this.idleCheckTimer != null){
			try{
				this.idleCheckTimer.cancel();
				this.idleCheckTimer = null;
			}catch(Exception e){
				EMPLog.log(EMPConstance.EMP_TCPIP, EMPLog.ERROR, 0, "Failed to Terminate the idleCheck for TCPIPServerService4CUPS [" + this.getName() + "]", e);
			}
		}
		
		if(this.dirtyCheckTimer != null){
			try{
				this.dirtyCheckTimer.cancel();
				this.dirtyCheckTimer = null;
			}catch(Exception e){
				EMPLog.log(EMPConstance.EMP_TCPIP, EMPLog.ERROR, 0, "Failed to Terminate the dirtyPackageCheck for TCPIPServerService4CUPS [" + this.getName() + "]", e);
			}
		}
	}
	

	public void addConnectToHost(ConnectToHost aHost )
	{
		connectToHosts.add( aHost );
	}

	public void addListenPort(ListenPort aPort )
	{
		listenPorts.add( aPort );
	}

	public int getDirtyTimeOut() {
		return dirtyTimeOut;
	}
	
	public int getDirtyInterval() {
		return dirtyInterval;
	}
	
	public void setCommProcessor(CommProcessor processor) {
		this.commProcessor = processor;
	}
	
	public PackageProcessor getPackageProcessor() {
		return packageProcessor;
	}
	
	public void setPackageProcessor(PackageProcessor packageProcessor) {
		this.packageProcessor = packageProcessor;
	}
	
	public boolean isDual() {
		return dual;
	}
	
	public boolean isKeepAlive() {
		return keepAlive;
	}
	
	public boolean isPoolThread() {
		return poolThread;
	}
	
	public int getPoolSize() {
		return poolSize;
	}
	
	public PackageIdentity getPackageIdentity() {
		return packageIdentity;
	}
	
	public void setPackageIdentity(PackageIdentity packageIdentity) {
		this.packageIdentity = packageIdentity;
	}
	
	public boolean isAliveCheck() {
		return aliveCheck;
	}
	
	public String getAliveCheckPackage() {
		return aliveCheckPackage;
	}

	public long getIdleInterval() {
		return idleInterval;
	}

	public String getIdlePackage() {
		return idlePackage;
	}

	public boolean isSendIdlePkg() {
		return sendIdlePkg;
	}

	public long getAliveCheckInterval() {
		return aliveCheckInterval;
	}

	public long getIdleTime() {
		return idleTime;
	}

	public long getTimeOut() {
		return timeOut;
	}

	public boolean isDirtyCheck() {
		return dirtyCheck;
	}

	public long getConnectWaitTime() {
		return connectWaitTime;
	}
	

	public int getMessagePollTime() {
		return messagePollTime;
	}

	public List getConnectToHosts() {
		return connectToHosts;
	}

	public List getListenPorts() {
		return listenPorts;
	}

	public List getReceivedMsg() {
		return receivedMsg;
	}

	public List getThreadPool() {
		return threadPool;
	}

	public CommProcessor getCommProcessor() {
		return commProcessor;
	}
	
	
	/**
	 * @emp:name 获取连接的最长等待时间
	 * @emp:mustSet false
	 * @emp:document 当连接无法获得时，最长等待多长时间才会放弃连接。-1:不等待；0：一直等待；其它：等待的毫秒数。缺省是：不等待
	 */
	public void setConnectWaitTime(long connectWaitTime) {
		this.connectWaitTime = connectWaitTime;
	}

	/**
	 * @emp:name 单/双工模式
	 * @emp:valueList true=双工;false=单工;
	 * @emp:mustSet false
	 * @emp:document 设置处理方式是单工还是双工。缺省：双工模式
	 */
	public void setDual(boolean dual) {
		this.dual = dual;
	}

	/**
	 * @emp:name 是否是长连接
	 * @emp:valueList true=是;false=否;
	 * @emp:mustSet false
	 * @emp:document 设置是否是长连接方式。缺省：否
	 */
	public void setKeepAlive(boolean keepAlive) {
		this.keepAlive = keepAlive;
	}

	/**
	 * @emp:name 缺省的阻塞超时时间
	 * @emp:mustSet false
	 * @emp:document 设置缺省的Socket发生阻塞后的最大超时时间。缺省：10000(ms)
	 */
	public void setTimeOut(long timeOut) {
		this.timeOut = timeOut;
	}

	/**
	 * @emp:name 是否使用线程池
	 * @emp:valueList true=是;false=否;
	 * @emp:mustSet false
	 * @emp:document 是否使用线程池用于处理接入的报文。缺省：是
	 */
	public void setPoolThread(boolean poolThread) {
		this.poolThread = poolThread;
	}
	
	/**
	 * @emp:name 线程池大小
	 * @emp:mustSet false
	 * @emp:document 在使用线程池用于处理接入报文的情况下，设置线程池的大小。缺省：10个线程
	 */
	public void setPoolSize(int poolSize) {
		this.poolSize = poolSize;
	}

	/**
	 * @emp:name 是否启动健康检查
	 * @emp:valueList true=是;false=否;
	 * @emp:mustSet false
	 * @emp:document 是否需要针对连接进行健康检查，判断连接通道是否可用。缺省：否
	 */
	public void setAliveCheck(boolean aliveCheck) {
		this.aliveCheck = aliveCheck;
	}
	
	/**
	 * @emp:name 健康检查轮询时间
	 * @emp:mustSet false
	 * @emp:document 在启动健康检查情况下，间隔一定的时间判断连接通道是否可用。缺省：60000(ms)
	 */
	public void setAliveCheckInterval(long aliveCheckInterval) {
		this.aliveCheckInterval = aliveCheckInterval;
	}
	
	/**
	 * @emp:name 健康检查的发送报文
	 * @emp:mustSet false
	 * @emp:document 在启动健康检查情况下，发送的健康检查报文。缺省：空字符串
	 */
	public void setAliveCheckPackage(String aliveCheckPackage) {
		this.aliveCheckPackage = aliveCheckPackage;
	}

	/**
	 * @emp:name 是否启动空闲检查
	 * @emp:valueList true=是;false=否;
	 * @emp:mustSet false
	 * @emp:document 是否需要针对连接进行空闲检查。当空闲超时后发送心跳报文以保证连接通道可用。缺省：否
	 */
	public void setSendIdlePkg(boolean sendIdlePkg) {
		this.sendIdlePkg = sendIdlePkg;
	}
	
	/**
	 * @emp:name 空闲检查轮询时间
	 * @emp:mustSet false
	 * @emp:document 在启动空闲检查情况下，间隔一定的时间判断连接是否超过一定的空闲时间。缺省：60000(ms)
	 */
	public void setIdleInterval(long idleInterval) {
		this.idleInterval = idleInterval;
	}
	
	/**
	 * @emp:name 空闲超时时间
	 * @emp:mustSet false
	 * @emp:document 设置连接空闲超过多长的时间后，发送空闲检查报文以保证连接通道可用。缺省：60000(ms)
	 */
	public void setIdleTime(long idleTime) {
		this.idleTime = idleTime;
	}

	/**
	 * @emp:name 心跳报文
	 * @emp:mustSet false
	 * @emp:document 在启动空闲检查情况下，发送的心跳报文。缺省：空字符串
	 */
	public void setIdlePackage(String idlePackage) {
		this.idlePackage = idlePackage;
	}
	

	/**
	 * @emp:name 是否启动垃圾报文检查
	 * @emp:valueList true=是;false=否;
	 * @emp:mustSet false
	 * @emp:document 单工情况下，是否启动垃圾报文检查，检查返回的报文中哪些已经超时并需要被丢弃。缺省：否
	 */
	public void setDirtyCheck(boolean dirtyCheck) {
		this.dirtyCheck = dirtyCheck;
	}


	/**
	 * @emp:name 垃圾报文超时时间
	 * @emp:mustSet false
	 * @emp:document 单工情况下，当接收到的报文超过一定时间未被处理，则认为是垃圾报文并被丢弃
	 */
	public void setDirtyTimeOut(int dirtyTimeOut) {
		this.dirtyTimeOut = dirtyTimeOut;
	}

	/**
	 * @emp:name 垃圾报文检查轮询时间
	 * @emp:mustSet false
	 * @emp:document 单工情况下，间隔一定的时间轮询检查报文是否超过一定时间未被处理
	 */
	public void setDirtyInterval(int dirtyInterval) {
		this.dirtyInterval = dirtyInterval;
	}
	
	/**
	 * @emp:name 返回消息轮询的时间间隔
	 * @emp:mustSet false
	 * @emp:document 单工情况下，间隔一定的时间查询消息是否已经返回。-1表示不轮询，缺省是不轮询
	 */
	public void setMessagePollTime(int messagePollTime) {
		this.messagePollTime = messagePollTime;
	}


	public Timer getAliveCheckTimer() {
		return aliveCheckTimer;
	}


	public Timer getDirtyCheckTimer() {
		return dirtyCheckTimer;
	}


	public Timer getIdleCheckTimer() {
		return idleCheckTimer;
	}


	public List getWaitReceiveThreadList() {
		return waitReceiveThreadList;
	}
	
}
