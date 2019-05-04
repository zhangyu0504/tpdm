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
 *	EMP�ṩ��TCPIP������������˿ڣ����ӵ��������˿ڣ������Ƿ�����
 *
 */
public class TCPIPService extends EMPService implements PackageReceiver
{
	protected List listenPorts = new ArrayList();

	protected List connectToHosts = new ArrayList();
	
	/**
	 * �첽���صı��Ķ��У�������ÿһ����һ������
	 * �����һ��ֵΪ���ر��ĵ�����
	 * ����ڶ���ֵΪ���ص�ʱ��
	 */
	protected List receivedMsg = new ArrayList();
	
	/**
	 * ��ǰ�ȴ����ر��ĵ��߳��������б�
	 */
	protected List waitReceiveThreadList = new ArrayList();
	
	/**
	 * ͨ��Э�鴦���������ڴ���ͨ��Э��
	 * ȱʡ��EMPCommProcessor
	 */
	protected CommProcessor commProcessor = new EMPCommProcessor();

	/**
	 * ���Ĵ�����
	 */
	protected PackageProcessor packageProcessor;
	
	/**
	 * ����ʶ����
	 */
	protected PackageIdentity packageIdentity;
	
	/**
	 * �Ƿ�Ϊ˫����������ͬʱ�����ӳ�ȥ�ͽ�����ʱ���Ƿ���Ҫ��ͬһ�������ϴ�������/��Ӧ
	 */
	protected boolean dual = true;
	
	/**
	 * �Ƿ�����
	 */
	protected boolean keepAlive = false;
	
	/**
	 * ���ڷ���ʱ��ѯȷ�����͵�������
	 */
	private int curConnectToIdx = 0;
	
	/**
	 * �ȴ�������ӵ��߳���
	 */
	private int waitConnectionCount = 0; 
	
	/**
	 * �Ƿ�ʹ���̳߳ش����±�������
	 */
	private boolean poolThread = false;
	
	/**
	 * �̳߳ش�С
	 */
	private int poolSize = 10;
	
	/**
	 * �̳߳ض���
	 */
	private List threadPool = new ArrayList();
	
	/**
	 * �Ƿ���Ҫ���з��������ü��
	 */
	private boolean aliveCheck = false;

	/**
	 * ���ڷ��������ü��ı��ģ�ȱʡΪ�մ���������ͨ��Э�������£�
	 */
	private String aliveCheckPackage = "";

	/**
	 * ���з���������ʱ������ȱʡ��60000
	 */
	private long aliveCheckInterval = 60000;

	/**
	 * �Ƿ���Ҫ�ڳ�ʱ�����ʱ������������
	 */
	private boolean sendIdlePkg = false;

	/**
	 * �������ģ�ȱʡΪ�մ���������ͨ��Э�������£�
	 */
	private String idlePackage = "";
	
	/**
	 * ��鷢���������ĵļ����ȱʡ��60000
	 */
	private long idleInterval = 60000;
	
	/**
	 * ����ʱ�䣨������ʱ��û�л���������������ģ�
	 */
	private long idleTime = 180000;
	
	/**
	 * ͨ�ų�ʱʱ��
	 */
	private long timeOut = 10000;
	
	/**
	 * ��ȡ���ӵ����ȴ�ʱ��
	 */
	private long connectWaitTime = 1000;
	

	/**
	 * ������Ϣ��ѯ��ʱ����
	 */
	private int messagePollTime = -1;
	
	/**
	 * �Ƿ�����첽���صı��Ľ��г�ʱ�ж�
	 * �����ʱ�������������Ľ��ж���
	 */
	private boolean dirtyCheck = false;
	

	/**
	 * ���յ��ı��ĳ�ʱʱ��
	 */
	private int dirtyTimeOut = 10 * 60000;

	/**
	 * ��ѯ���ձ��ĵļ��ʱ��
	 */
	private int dirtyInterval = 2 * 60000;
	
	/**
	 * ���TCPIP���ӵĽ���״̬�ļ�ʱ��
	 */
	protected Timer aliveCheckTimer = null;
	
	/**
	 * ���TCPIP���ӿ���״̬�ļ�ʱ��
	 */
	protected Timer idleCheckTimer = null;
	
	/**
	 * �����Ҫ�����ķ��ر��ĵļ�ʱ��
	 */
	protected Timer dirtyCheckTimer = null;
	
	/**
	 * ��ǰConnectToHost�Ѿ�ʹ�õ�Ȩ����(ֻ����getConnectToHost������)
	 */
	private int currentPollingWeight = 0;
	
	/**
	 * ��ʼ��
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
		
		//����������£�����Ҫ���Ϳ��б���
		if(!this.keepAlive && this.sendIdlePkg){
			EMPLog.log(EMPConstance.EMP_TCPIP, EMPLog.WARNING, 0,
					"The SendIdlePkg is better to be false when keepAlive is false in TCPIPService!");
		}
		
		//����ListenPort����£����󲿷�����²��ᵱ���첽����
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
	 * ȡ�õ�ǰ�����Ӳ�����
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
	 * ȡ�����ڵȴ�������ӵ��߳���
	 * @return
	 */
	public int getWaitConnectionCount() {
		return this.waitConnectionCount;
	}
	
	
	/**
	 * �������󣬲��ȴ����أ�timeOut���������0������ʱʱ�䣬����ʹ��
	 * �����趨�ĳ�ʱʱ�䣬Ĭ��Ϊ10��
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
		
		if (this.listenPorts.size() > 0 && !dual) //����ģʽ�£��ұ���Ҫ�������˿�
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
	 * ���һ�����õ�ConnectToHost
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
						
						//������ѯȨ�صĶ�������Ƿ���ѯ����һ��ConnectToHost����
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
					EMPLog.log(EMPConstance.EMP_TCPIP, EMPLog.DEBUG, 0, "�ȴ�ʱ�䣺"+(this.connectWaitTime - waitTime)+"/"+this.connectWaitTime);
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
	 * �ͷ�һ��ConnectToHost
	 * @param connection
	 */
	protected synchronized void releaseConnecToHost(ConnectToHost connection){
		int inUsed = connection.getInUsedConnection();
		inUsed --;
		connection.setInUsedConnection(inUsed);
		notify();
	}
	

	/**
	 * ���TCPIP���ӵĽ���״̬
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
						
						//����һ�����ӻָ��˿����ԣ���Ӧ�û���һ���߳�
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
	 * ���TCPIP����״̬
	 */
	protected class IdleCheckTask extends TimerTask {
		
		public void run() {
			EMPLog.log(EMPConstance.EMP_TCPIP, EMPLog.DEBUG, 0,
					"Begin to check idle for TCPIP Connect�� " + getName());

			Object[] keys = null;
			
			keys = connectToHosts.toArray();
			for (int i = 0; i < keys.length; i++) {
				ConnectToHost con = (ConnectToHost) keys[i];
				con.sendIdlePackage(getIdlePackage().getBytes(), getIdleTime());
			}
			
			EMPLog.log(EMPConstance.EMP_TCPIP, EMPLog.DEBUG, 0,
					"The check idle for TCPIP Connect�� " + getName() +" is end!");
		}
	}
	

	/**
	 * �����Ҫ�����ķ��ر���
	 */
	protected class DirtyPackageCheckTask extends TimerTask {
		
		public void run() {
			EMPLog.log(EMPConstance.EMP_TCPIP, EMPLog.DEBUG, 0,
					"Begin to check dirty package for TCPIP Connect�� "
							+ getName());
			
			long curTime = System.currentTimeMillis();
			synchronized (receivedMsg) {
				for (int i = 0; i < receivedMsg.size(); i++) {
					Object[] msgObj = (Object[]) receivedMsg.get(i);
					if (msgObj == null)
						continue;
					long arriveTime = ((Long) msgObj[1]).longValue();// �õ����ĵĽ���ʱ��
					if ((curTime - arriveTime) >= dirtyTimeOut) {// ����һ��ʱ��û�б����գ�����Ϊ����������
						receivedMsg.remove(msgObj);
					}
				}
			}
			
			EMPLog.log(EMPConstance.EMP_TCPIP, EMPLog.DEBUG, 0,
					"The check dirty package for TCPIP Connect�� "
							+ getName() + " is end!");

		}
	}
	
	/**
	 * ������Ӧ
	 * @param identity ���ݸ����ļ������ı�ʶ����
	 * @param timeOut ��ʱʱ�䣬�������0������ʱʱ�䣬����ʹ���趨�ĳ�ʱʱ��
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
			if (packageProcessor.isRequestPackage(aPackage)) //����������ģ�����������
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
	 * ���û����msgObj��һ��Ԫ���Ǳ������ݣ��ڶ���Ԫ���ǽ��յ���ʱ��
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
	 * @emp:name ��ȡ���ӵ���ȴ�ʱ��
	 * @emp:mustSet false
	 * @emp:document �������޷����ʱ����ȴ��೤ʱ��Ż�������ӡ�-1:���ȴ���0��һֱ�ȴ����������ȴ��ĺ�������ȱʡ�ǣ����ȴ�
	 */
	public void setConnectWaitTime(long connectWaitTime) {
		this.connectWaitTime = connectWaitTime;
	}

	/**
	 * @emp:name ��/˫��ģʽ
	 * @emp:valueList true=˫��;false=����;
	 * @emp:mustSet false
	 * @emp:document ���ô���ʽ�ǵ�������˫����ȱʡ��˫��ģʽ
	 */
	public void setDual(boolean dual) {
		this.dual = dual;
	}

	/**
	 * @emp:name �Ƿ��ǳ�����
	 * @emp:valueList true=��;false=��;
	 * @emp:mustSet false
	 * @emp:document �����Ƿ��ǳ����ӷ�ʽ��ȱʡ����
	 */
	public void setKeepAlive(boolean keepAlive) {
		this.keepAlive = keepAlive;
	}

	/**
	 * @emp:name ȱʡ��������ʱʱ��
	 * @emp:mustSet false
	 * @emp:document ����ȱʡ��Socket��������������ʱʱ�䡣ȱʡ��10000(ms)
	 */
	public void setTimeOut(long timeOut) {
		this.timeOut = timeOut;
	}

	/**
	 * @emp:name �Ƿ�ʹ���̳߳�
	 * @emp:valueList true=��;false=��;
	 * @emp:mustSet false
	 * @emp:document �Ƿ�ʹ���̳߳����ڴ������ı��ġ�ȱʡ����
	 */
	public void setPoolThread(boolean poolThread) {
		this.poolThread = poolThread;
	}
	
	/**
	 * @emp:name �̳߳ش�С
	 * @emp:mustSet false
	 * @emp:document ��ʹ���̳߳����ڴ�����뱨�ĵ�����£������̳߳صĴ�С��ȱʡ��10���߳�
	 */
	public void setPoolSize(int poolSize) {
		this.poolSize = poolSize;
	}

	/**
	 * @emp:name �Ƿ������������
	 * @emp:valueList true=��;false=��;
	 * @emp:mustSet false
	 * @emp:document �Ƿ���Ҫ������ӽ��н�����飬�ж�����ͨ���Ƿ���á�ȱʡ����
	 */
	public void setAliveCheck(boolean aliveCheck) {
		this.aliveCheck = aliveCheck;
	}
	
	/**
	 * @emp:name ���������ѯʱ��
	 * @emp:mustSet false
	 * @emp:document �����������������£����һ����ʱ���ж�����ͨ���Ƿ���á�ȱʡ��60000(ms)
	 */
	public void setAliveCheckInterval(long aliveCheckInterval) {
		this.aliveCheckInterval = aliveCheckInterval;
	}
	
	/**
	 * @emp:name �������ķ��ͱ���
	 * @emp:mustSet false
	 * @emp:document �����������������£����͵Ľ�����鱨�ġ�ȱʡ�����ַ���
	 */
	public void setAliveCheckPackage(String aliveCheckPackage) {
		this.aliveCheckPackage = aliveCheckPackage;
	}

	/**
	 * @emp:name �Ƿ��������м��
	 * @emp:valueList true=��;false=��;
	 * @emp:mustSet false
	 * @emp:document �Ƿ���Ҫ������ӽ��п��м�顣�����г�ʱ�������������Ա�֤����ͨ�����á�ȱʡ����
	 */
	public void setSendIdlePkg(boolean sendIdlePkg) {
		this.sendIdlePkg = sendIdlePkg;
	}
	
	/**
	 * @emp:name ���м����ѯʱ��
	 * @emp:mustSet false
	 * @emp:document ���������м������£����һ����ʱ���ж������Ƿ񳬹�һ���Ŀ���ʱ�䡣ȱʡ��60000(ms)
	 */
	public void setIdleInterval(long idleInterval) {
		this.idleInterval = idleInterval;
	}
	
	/**
	 * @emp:name ���г�ʱʱ��
	 * @emp:mustSet false
	 * @emp:document �������ӿ��г����೤��ʱ��󣬷��Ϳ��м�鱨���Ա�֤����ͨ�����á�ȱʡ��60000(ms)
	 */
	public void setIdleTime(long idleTime) {
		this.idleTime = idleTime;
	}

	/**
	 * @emp:name ��������
	 * @emp:mustSet false
	 * @emp:document ���������м������£����͵��������ġ�ȱʡ�����ַ���
	 */
	public void setIdlePackage(String idlePackage) {
		this.idlePackage = idlePackage;
	}
	

	/**
	 * @emp:name �Ƿ������������ļ��
	 * @emp:valueList true=��;false=��;
	 * @emp:mustSet false
	 * @emp:document ��������£��Ƿ������������ļ�飬��鷵�صı�������Щ�Ѿ���ʱ����Ҫ��������ȱʡ����
	 */
	public void setDirtyCheck(boolean dirtyCheck) {
		this.dirtyCheck = dirtyCheck;
	}


	/**
	 * @emp:name �������ĳ�ʱʱ��
	 * @emp:mustSet false
	 * @emp:document ��������£������յ��ı��ĳ���һ��ʱ��δ����������Ϊ���������Ĳ�������
	 */
	public void setDirtyTimeOut(int dirtyTimeOut) {
		this.dirtyTimeOut = dirtyTimeOut;
	}

	/**
	 * @emp:name �������ļ����ѯʱ��
	 * @emp:mustSet false
	 * @emp:document ��������£����һ����ʱ����ѯ��鱨���Ƿ񳬹�һ��ʱ��δ������
	 */
	public void setDirtyInterval(int dirtyInterval) {
		this.dirtyInterval = dirtyInterval;
	}
	
	/**
	 * @emp:name ������Ϣ��ѯ��ʱ����
	 * @emp:mustSet false
	 * @emp:document ��������£����һ����ʱ���ѯ��Ϣ�Ƿ��Ѿ����ء�-1��ʾ����ѯ��ȱʡ�ǲ���ѯ
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
