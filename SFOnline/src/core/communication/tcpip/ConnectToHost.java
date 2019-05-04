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
 * ���ӵ�ĳ��TCP/IP�������Ĵ�����
 *
 * @emp:name ��������
 * @emp:document �������ӵ�TCPIP�������������
 */
public class ConnectToHost {

	/**
	 * ������ַ
	 */
	protected String hostAddr;
	
	/**
	 * �˿�
	 */
	protected int port = 0;

	/**
	 * �Ƿ��ǳ����ӣ�ȱʡ����
	 */
	protected boolean keepAlive = false;
	
	/**
	 * ���һ�ν���ʱ��
	 */
	private int lastCommTime = 0;

	/**
	 * �Ƿ���Ҫ��������Ľ���ʱ����������������ȱʡ����
	 */
	private boolean connectionCtrl = false;

	/**
	 * ���ս���ʱ��������������ʱ��б��
	 */
	private float rate;
	
	/**
	 * ͨѶЭ�鴦��ӿ�
	 */
	protected CommProcessor commProcessor;

	/**
	 * �����������ȱ�٣�10
	 */
	private int maxConnection = 10;	

	/**
	 * ʵ�ʵ������б�
	 */
	private List connections = new ArrayList();;

	/**
	 * ����������ʱʱ�䣬ȱʡ����Զ���ᳬʱ
	 */
	private int soTimeOut = 0;

	/**
	 * ��ʹ�õ����Ӹ���
	 */
	private int inUsedConnection = 0;

	/**
	 * �������Ƿ����
	 */
	private boolean alive = true;
	
	/**
	 * SocketListener�б�
	 */
	private List socketAcceptListeners;
	
	/**
	 * �Ƿ���˫��ͨѶģʽ��ȱʡ����
	 * ���������ڱ�ʶ�����յ���Ϣ�Ƿ��ǴӸ�socket����
	 */
	protected boolean dual = true;
	
	//���ߺ��Ƿ���������
	private boolean reconnectImmediately = false;

	/**
	 * ��ѯȨ�أ�ȱʡ��1
	 * ����������TCPIPService����ѯ��ȡConnectToHostʱ������ѯ��Ȩ��
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
	 * ���ڳ����ӵ����������������Ϳ��б���
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
	 * �õ���ǰ���Խ��������������
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
			
			//�Ѿ��رյ�connectionӦ�ñ��ͷ�
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
	 * ���Socket���Ӽ�����
	 * @param listener
	 */
	public void addSocketListener( SocketListener listener )
	{
		if( this.socketAcceptListeners == null )
			socketAcceptListeners = new ArrayList();
		socketAcceptListeners.add(listener );
	}
	
	/**
	 * @emp:name ��/˫��ģʽ
	 * @emp:valueList true=˫��;false=����;
	 * @emp:mustSet false
	 * @emp:document ѡ�񵥹���˫��ģʽ��ȱʡ��˫��
	 */
	public void setDual(boolean dual) {
		this.dual = dual;
	}

	/**
	 * @emp:name ������ַ
	 * @emp:mustSet true
	 * @emp:document ��Ҫ���ӵ�������ַ
	 */
	public void setHostAddr(String hostAddr) {
		this.hostAddr = hostAddr;
	}

	/**
	 * @emp:name �Ƿ��ǳ�����
	 * @emp:valueList true=��;false=��;
	 * @emp:mustSet false
	 * @emp:document �Ƿ��ǳ����ӡ�ȱʡ����
	 */
	public void setKeepAlive(boolean keepAlive) {
		this.keepAlive = keepAlive;
	}

	/**
	 * @emp:name �˿�
	 * @emp:mustSet true
	 * @emp:document ��Ҫ���ӵ������˿�
	 */
	public void setPort(int port) {
		this.port = port;
	}
	
	/**
	 * @emp:name ���������
	 * @emp:mustSet false
	 * @emp:document ���������������ȱʡ��10������
	 */
	public void setMaxConnection(int maxConnection) {
		this.maxConnection = maxConnection;
	}

	/**
	 * @emp:name �Ƿ�̬�������������
	 * @emp:valueList true=��;false=��;
	 * @emp:mustSet false
	 * @emp:document �Ƿ���Ҫ��������Ľ���ʱ����������������ȱʡ����
	 */
	public void setConnectionCtrl(boolean connectionCtrl) {
		this.connectionCtrl = connectionCtrl;
	}
	
	/**
	 * @emp:name ����������ı仯б��
	 * @emp:mustSet false
	 * @emp:document ���ս���ʱ��������������ʱ��б�ʡ����գ��������ʱ���������б�ʣ�����̬���ٵ�ǰ�����������
	 */
	public void setRate(float rate) {
		this.rate = rate;
	}
	
	/**
	 * @emp:name ����������ʱʱ��
	 * @emp:mustSet false
	 * @emp:document Socket��������������ʱʱ�䡣ȱʡ��0(��Զ����ʱ)
	 */
	public void setSoTimeOut(int soTimeOut) {
		this.soTimeOut = soTimeOut;
	}
	
	/**
	 * @emp:name ͨѶЭ�鴦��ӿ�
	 * @emp:mustSet false
	 * @emp:editorClass class
	 * @emp:document ͨѶЭ�鴦��ӿڵ�ʵ���࣬ͨ����TCPIPService��������
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
	 * @emp:name �Ƿ���ߺ���������
	 * @emp:valueList true=��;false=��;
	 * @emp:mustSet false
	 * @emp:document �ڳ���������£�Socket�жϺ��Ƿ���������������ȱʡ����
	 */
	public void setReconnectImmediately(boolean reconnectImmediately) {
		this.reconnectImmediately = reconnectImmediately;
	}
	
	/**
	 * @emp:name ��ѯȨ��
	 * @emp:mustSet false
	 * @emp:document ����������TCPIPService����ѯ��ȡConnectToHostʱ������ѯ��Ȩ��
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
