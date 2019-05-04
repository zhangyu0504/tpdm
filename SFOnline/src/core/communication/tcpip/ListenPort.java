package core.communication.tcpip;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import com.ecc.emp.concurrent.EMPConcurrentTask;
import com.ecc.emp.concurrent.HealthyArrayBlockingQueue;
import com.ecc.emp.concurrent.HealthyBlockingQueue;
import com.ecc.emp.concurrent.HealthyLinkedBlockingQueue;
import com.ecc.emp.core.EMPConstance;
import com.ecc.emp.log.EMPLog;

import edu.emory.mathcs.backport.java.util.concurrent.BlockingQueue;
import edu.emory.mathcs.backport.java.util.concurrent.RejectedExecutionHandler;
import edu.emory.mathcs.backport.java.util.concurrent.ThreadPoolExecutor;
import edu.emory.mathcs.backport.java.util.concurrent.TimeUnit;



/**
 * 
 * ListenPort.java<br>
 * Copyright (c) 2000, 2006 e-Channels Corporation <br>
 *
 * @author zhongmc<br>
 * @version 1.0.0<br>
 * @since 2007-1-9<br>
 * @lastmodified liubq 2009-12-28<br>
 *
 *����������
 *
 */
public class ListenPort implements Runnable {
	
	/**
	 * ȱʡ���ӳش�С��10
	 */
	public static int DEFAULT_POOL_SIZE = 10;
	
	/**
	 * ȱʡ�ȴ����г��ȣ�10
	 */
	public static int DEFAULT_QUEUE_SIZE = 10;
	
	/**
	 * �ڶ����е�ȱʡ�ȴ�ʱ�䣺0(����)
	 */
	public static long DEFAULT_WAIT_TIME = 0;

	
	/**
	 * ͨ��Э�鴦���������ڴ���ͨ��Э��
	 */
	protected CommProcessor commProcessor;

	/**
	 * ���Ĵ�����
	 */
	protected PackageReceiver packageReceiver;
	
	/**
	 * �����˿�
	 */
	protected int port = 0;
	
	/**
	 * �Ƿ��ǳ����ӣ�ȱʡ����
	 */
	protected boolean keepAlive = false;
	
	/**
	 * ������Socket����
	 */
	private ServerSocket socket;
	
	/**
	 * �����Ƿ���ֹ��ȱʡ����
	 */
	private boolean isStop = false;
	
	/**
	 * SocketListener�б�
	 */
	private List socketAcceptListeners;
	
	/**
	 * �������߳�
	 */
	private Thread theThread;
	

	/**
	 * �������ӳض���
	 */
	private ThreadPoolExecutor threadPool = null;
	
	/**
	 * ���ӳصĴ�С��ȱʡ��DEFAULT_POOL_SIZE
	 */
	private int poolSize = DEFAULT_POOL_SIZE;
	
	
	/**
	 * ��������������(������ƽ����Ĳ���)��ȱʡ��DEFAULT_POOL_SIZE
	 */
	protected int maxConnection = DEFAULT_POOL_SIZE;
	
	/**
	 * �ȴ����г��ȣ�ȱʡ��DEFAULT_QUEUE_SIZE
	 */
	protected int queueSize = DEFAULT_QUEUE_SIZE;
	
	/**
	 * �����ڶ����е����ȴ�ʱ��(>0���ȴ�ʱ�䣻����������)��ȱʡ��DEFAULT_WAIT_TIME
	 */
	protected long waitTime = DEFAULT_WAIT_TIME;
	
	/**
	 * �ܾ���Ϣ�����̳߳شﵽ���ʱ������ܾ����󣬷��ص���Ϣ
	 * ȱʡ�ܾ���Ϣ��System busy!
	 */
	private String rejectMessage = "System busy!";
	
	/**
	 * ͨ��commProcessor����ľܾ���Ϣ�ֽ���
	 */
	protected byte[] rejectMessageBytes;
	
	/**
	 * �󶨵�ǰ�����е�ĳ��IP��ַ
	 */
	private String bindAddress = null;
	
	/**
	 * ServerSocket��backlog�������ã�ȱʡΪ0
	 */
	private int backlog = 0;
	
	/**
	 * �Ƿ��������г�ʱ���(������ֻ�ڳ�������������)��ȱʡ����
	 */
	private boolean idleCheck = false;

	/**
	 * ���г�ʱʱ�䡣ȱʡ��5*60000
	 */
	private int idleTimeOut = 5*60000;
	
	/**
	 * �����г�ʱ�ļ��ʱ�䣬ȱʡ��60000
	 */
	private int idleCheckInterval = 10000;
	
	/**
	 * ϵͳ�����������ܾ������Ӹ���
	 */
	private long rejectTaskCount = 0;
	
	/**
	 * ���ӵȴ����������õ�ʵ�ַ�ʽ
	 */
	private String queueType = "Array";

	/**
	 * ������ǰ�����̵߳����ӵȴ�����
	 */
	protected HealthyBlockingQueue socketWaitingQueue = null;

	public ListenPort(){
		super();
	}
	
	public ListenPort(int port){
		super();
		this.port = port;
	}
	
	
	/**
	 * ������������
	 */
	public void startUp()
	{
		theThread = new Thread(this);
		this.isStop = false;
		theThread.setName("EMP Listen Thread [" + port + "]");
		theThread.start();
	}
	
	
	public void run() 
	{
		//���������̳߳ؼ��ȴ�����
		socketWaitingQueue = this.createHealthyBlockingQueue();
		if(this.maxConnection < this.poolSize){
			EMPLog.log(EMPConstance.EMP_TCPIP, EMPLog.WARNING, 0,
							"ListenPort maxConnection[" + this.maxConnection
									+ "] is less than poolSize["
									+ this.poolSize + "]!");
			this.maxConnection = this.poolSize;
		}
		this.threadPool =  new ThreadPoolExecutor(this.poolSize, this.maxConnection,0L, TimeUnit.MILLISECONDS, socketWaitingQueue);
		
		//�����ȴ����к�ܾ��µ�����
		RejectedExecutionHandler handler = new RejectedExecutionHandler(){
			public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
	        	if(r instanceof EMPConcurrentTask){
	        		EMPConcurrentTask task = (EMPConcurrentTask)r;
	        		task.rejectTask();
	        		synchronized (this) {
	        			rejectTaskCount ++;
					}
	        	}
	        }
		};
		this.threadPool.setRejectedExecutionHandler(handler);
		//��ʼ���ܾ�������ʱ��Ҫ���صı�������
		this.rejectMessageBytes = this.commProcessor.wrapMessagePackage(this.rejectMessage.getBytes());
		
		//��ӵ�еȴ����������õȴ�ʱ������Ӷ��еĳ�ʱ����߳�
		if(this.queueSize > 0 && this.waitTime > 0){
			socketWaitingQueue.startTaskHealthyCheck();
		}
		
		//����Socket�˿�����
		try {
			InetAddress bindAddr = null;
			if(this.bindAddress != null){
				bindAddr = InetAddress.getByName(this.bindAddress);
			}
			EMPLog.log(EMPConstance.EMP_TCPIP, EMPLog.INFO,  0, "Listen thread listen at port: " + port );
			
			socket = new ServerSocket(port, this.backlog, bindAddr);
		} catch (Exception e) {
			EMPLog.log(EMPConstance.EMP_TCPIP, EMPLog.ERROR,  0,  "Failed to listen on: " + port,   e);
			return;
		}
		
		while (!isStop) 
		{
			try {
				this.fireSocketToBeAcceptEvent();
				
				Socket aSocket = socket.accept();
				
				if (aSocket != null ) 
				{
					//EMPLog.log(EMPConstance.EMP_TCPIP, EMPLog.INFO, 0, "New socket accept from " + aSocket.getRemoteSocketAddress() );
					
					aSocket.setReuseAddress( true );
					
					fireSocketAcceptEvent( aSocket );
					
					SocketProcessThread socketProcessorThread = new SocketProcessThread(aSocket, this.commProcessor, this.packageReceiver, this.keepAlive);
					socketProcessorThread.setSocketAcceptListeners(this.socketAcceptListeners);
					socketProcessorThread.setRejectMessageBytes(this.rejectMessageBytes);
					socketProcessorThread.setWaitTime(this.waitTime);
					socketProcessorThread.setIdleCheck(this.idleCheck);
					socketProcessorThread.setIdleCheckInterval(this.idleCheckInterval);
					socketProcessorThread.setIdleTimeOut(this.idleTimeOut);
					
					this.threadPool.execute(socketProcessorThread);
				}
				
			} catch (java.io.IOException e) 
			{
				EMPLog.log(EMPConstance.EMP_TCPIP, EMPLog.ERROR, 0,  "Exception from ListenThread on port[" + port + "]" , e);
				break;
			}
		}
	}
	

	/**
	 * ֹͣ�̣߳��ر��׽���
	 * @Creation date: (2002-4-30 10:42:31)
	 * @author ZhongMingChang
	 */
	public void terminate() 
	{
		
		isStop = true;
		try {
			socket.close();
		} catch (Exception e) 
		{
		}
		this.socketWaitingQueue.endTaskHealthyCheck();
		this.threadPool.shutdownNow();
	}
	
	/**
	 * ����һ�����ӵȴ����е�ʵ��
	 * �÷����ɹ�������д�����޸Ķ��е�ʵ�ַ�ʽ
	 * @return
	 */
	protected HealthyBlockingQueue createHealthyBlockingQueue(){
		HealthyBlockingQueue queue = null;
		if("Linked".equalsIgnoreCase(this.queueType)){
			queue = new HealthyLinkedBlockingQueue(this.queueSize);
		}else 
			queue = new HealthyArrayBlockingQueue(this.queueSize);
		return queue;
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
	 * Socket����֮ǰ�����Ķ���
	 *
	 */
	public void fireSocketToBeAcceptEvent(){
		if( socketAcceptListeners == null )
			return;
		for( int i=0; i<socketAcceptListeners.size(); i++ )
		{
			SocketListener listener = ( SocketListener )socketAcceptListeners.get( i );
			listener.beforeSocketConnected();
		}
	}
	
	/**
	 * Socket����֮�󴥷��Ķ���
	 * @param socket
	 */
	public void fireSocketAcceptEvent(Socket socket )
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
	 * ���ӳش�С
	 * @return
	 */
	public int getPoolSize() {
		return this.poolSize;
	}
	
	/**
	 * ��ǰ���ڴ����������
	 * @return
	 */
	public int getCurrentConnections(){
		return this.threadPool.getActiveCount();
	}
	
	/**
	 * �Ѵ����������
	 * @return
	 */
	public long getTaskCount() {
		return this.threadPool.getTaskCount();
	}
	
	/**
	 * ��ʱ��������
	 * @return
	 */
	public long getTimeOutTaskCount(){
		BlockingQueue queue = this.threadPool.getQueue();
		if(queue instanceof HealthyBlockingQueue){
			return ((HealthyBlockingQueue)queue).getDestroyTaskCount();
		}else
			return 0;
	}
	
	/**
	 * ���ܾ���������
	 * @return
	 */
	public long getRejectTaskCount(){
		return this.rejectTaskCount;
	}
	
	/**
	 * ��ʷ����߳���
	 * @return
	 */
	public long getLargestPoolSize() {
		return this.threadPool.getLargestPoolSize();
	}
	
	
	/**
	 * �ȴ����д�С
	 * @return
	 */
	public int getQueueSize() {
		return queueSize;
	}
	
	/**
	 * ���ڵȴ���������
	 * @return
	 */
	public int getWaitCount(){
		return this.threadPool.getQueue().size();
	}

	/**
	 * �����ȴ����к��ܹ���������������
	 * @return
	 */
	public int getMaxConnection() {
		return maxConnection;
	}
	
	public ThreadPoolExecutor getThreadPool() {
		return threadPool;
	}

	public String getRejectMessage() {
		return rejectMessage;
	}
	
	public long getWaitTime() {
		return waitTime;
	}
	

	public boolean isKeepAlive() {
		return keepAlive;
	}

	public int getPort() {
		return port;
	}

	public int getIdleCheckInterval() {
		return idleCheckInterval;
	}

	public int getIdleTimeOut() {
		return idleTimeOut;
	}

	public String getBindAddress() {
		return bindAddress;
	}

	public CommProcessor getCommProcessor() {
		return commProcessor;
	}

	public PackageReceiver getPackageReceiver() {
		return packageReceiver;
	}

	public List getSocketAcceptListeners() {
		return socketAcceptListeners;
	}

	public boolean isIdleCheck() {
		return idleCheck;
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
	 * @emp:name �̳߳ش�С
	 * @emp:mustSet false
	 * @emp:document ���ڴ���Socket������̳߳ش�С��ȱʡ��DEFAULT_POOL_SIZE
	 */
	public void setPoolSize(int poolSize) {
		if(poolSize < 0){
			poolSize = Integer.MAX_VALUE;
		}
		this.poolSize = poolSize;
	}
	
	/**
	 * @emp:name ��������������
	 * @emp:mustSet false
	 * @emp:document ���ȴ���������֮���������е�����߳���(�����ֵ����poolSize�����ڶ�������֮��ᴴ���߳̽��д���)��ȱʡ��DEFAULT_POOL_SIZE
	 */
	public void setMaxConnection(int maxConnection) {
		if(maxConnection < 0){
			maxConnection = Integer.MAX_VALUE;
		}
		this.maxConnection = maxConnection;
	}
	
	/**
	 * @emp:name ���Ӿܾ�����Ϣ
	 * @emp:mustSet false
	 * @emp:document �ܾ�����ʱ�����ص���Ϣ��ȱʡ��System busy!
	 */
	public void setRejectMessage(String rejectMessage) {
		this.rejectMessage = rejectMessage;
	}

	/**
	 * @emp:name ���ӵȴ�ʱ��
	 * @emp:mustSet false
	 * @emp:document �ﵽ������Ӻ������ӵȴ���ʱ��(>0:�ȴ��ĺ�����;����:����)��ȱʡ�ǣ�����
	 */
	public void setWaitTime(long waitTime) {
		this.waitTime = waitTime;
	}
	
	/**
	 * @emp:name �ȴ����д�С
	 * @emp:mustSet false
	 * @emp:document ���ڽ���Socket�ȴ��Ķ��д�С��ȱʡ��DEFAULT_QUEUE_SIZE
	 */
	public void setQueueSize(int queueSize) {
		if(queueSize < 0){
			queueSize = Integer.MAX_VALUE;
		}
		this.queueSize = queueSize;
	}


	/**
	 * @emp:name ���г�ʱʱ��
	 * @emp:mustSet false
	 * @emp:document �ڳ�����״̬�£����ӿ����������һ����ʱ����ᱻ�ͷ�(�������Ϊ-1�����ʾ��������ʱʱ��)��ȱʡ�����������
	 */
	public void setIdleTimeOut(int idleTimeOut) {
		this.idleTimeOut = idleTimeOut;
	}
	
	/**
	 * @emp:name ���м�����ѯʱ��
	 * @emp:mustSet false
	 * @emp:document �ڳ�����״̬�£����һ����ʱ����ѯ������ӵĿ���ʱ�䡣ȱʡ�ǣ�5*60000(ms)
	 */
	public void setIdleCheckInterval(int idleCheckInterval) {
		this.idleCheckInterval = idleCheckInterval;
	}

	/**
	 * @emp:name ��IP��ַ
	 * @emp:mustSet false
	 * @emp:document �ڱ��ض������ַ������£��󶨱��ص�ĳ����ַ
	 */
	public void setBindAddress(String bindAddress) {
		this.bindAddress = bindAddress;
	}
	
	/**
	 * @emp:name �Ƿ��������г�ʱ���
	 * @emp:valueList true=��;false=��;
	 * @emp:mustSet false
	 * @emp:document �����������£��Ƿ���Ҫ��Խ����Socket���п��г�ʱ��顣ȱʡ����
	 */
	public void setIdleCheck(boolean idleCheck) {
		this.idleCheck = idleCheck;
	}
	
	public String getQueueType() {
		return queueType;
	}
	
	/**
	 * @emp:name ��������
	 * @emp:valueList Array=Array;Linked=Linked;
	 * @emp:mustSet false
	 * @emp:document ���ӵȴ����������õ�ʵ�ַ�ʽ��ȱʡ��Array
	 */
	public void setQueueType(String queueType) {
		this.queueType = queueType;
	}
	
	public int getBacklog() {
		return backlog;
	}

	/**
	 * @emp:name ����ϵͳ������д�С
	 * @emp:mustSet false
	 * @emp:document ����ϵͳ������д�С(ServerSocket��backlog��������)��ȱʡΪ0
	 */
	public void setBacklog(int backlog) {
		this.backlog = backlog;
	}

	public ServerSocket getSocket() {
		return socket;
	}

	public Thread getTheThread() {
		return theThread;
	}

	public void setCommProcessor(CommProcessor commProcessor) {
		this.commProcessor = commProcessor;
	}

	public void setPackageReceiver(PackageReceiver packageReceiver) {
		this.packageReceiver = packageReceiver;
	}

	public void setSocketAcceptListeners(List socketAcceptListeners) {
		this.socketAcceptListeners = socketAcceptListeners;
	}

}
