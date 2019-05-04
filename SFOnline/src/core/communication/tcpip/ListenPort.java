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
 *监听处理类
 *
 */
public class ListenPort implements Runnable {
	
	/**
	 * 缺省连接池大小：10
	 */
	public static int DEFAULT_POOL_SIZE = 10;
	
	/**
	 * 缺省等待队列长度：10
	 */
	public static int DEFAULT_QUEUE_SIZE = 10;
	
	/**
	 * 在队列中的缺省等待时间：0(死等)
	 */
	public static long DEFAULT_WAIT_TIME = 0;

	
	/**
	 * 通信协议处理器，用于处理通信协议
	 */
	protected CommProcessor commProcessor;

	/**
	 * 报文处理器
	 */
	protected PackageReceiver packageReceiver;
	
	/**
	 * 侦听端口
	 */
	protected int port = 0;
	
	/**
	 * 是否是长连接，缺省：否
	 */
	protected boolean keepAlive = false;
	
	/**
	 * 侦听的Socket对象
	 */
	private ServerSocket socket;
	
	/**
	 * 侦听是否中止，缺省：否
	 */
	private boolean isStop = false;
	
	/**
	 * SocketListener列表
	 */
	private List socketAcceptListeners;
	
	/**
	 * 侦听的线程
	 */
	private Thread theThread;
	

	/**
	 * 并发连接池对象
	 */
	private ThreadPoolExecutor threadPool = null;
	
	/**
	 * 连接池的大小，缺省：DEFAULT_POOL_SIZE
	 */
	private int poolSize = DEFAULT_POOL_SIZE;
	
	
	/**
	 * 最大允许的连接数(用于削平请求的波峰)，缺省：DEFAULT_POOL_SIZE
	 */
	protected int maxConnection = DEFAULT_POOL_SIZE;
	
	/**
	 * 等待队列长度，缺省：DEFAULT_QUEUE_SIZE
	 */
	protected int queueSize = DEFAULT_QUEUE_SIZE;
	
	/**
	 * 连接在队列中的最大等待时间(>0：等待时间；其它：死等)，缺省：DEFAULT_WAIT_TIME
	 */
	protected long waitTime = DEFAULT_WAIT_TIME;
	
	/**
	 * 拒绝信息，当线程池达到最大时，如果拒绝访求，返回的信息
	 * 缺省拒绝信息：System busy!
	 */
	private String rejectMessage = "System busy!";
	
	/**
	 * 通过commProcessor处理的拒绝信息字节流
	 */
	protected byte[] rejectMessageBytes;
	
	/**
	 * 绑定当前环境中的某个IP地址
	 */
	private String bindAddress = null;
	
	/**
	 * ServerSocket的backlog参数设置，缺省为0
	 */
	private int backlog = 0;
	
	/**
	 * 是否启动空闲超时检查(该属性只在长连接下起作用)。缺省：否
	 */
	private boolean idleCheck = false;

	/**
	 * 空闲超时时间。缺省：5*60000
	 */
	private int idleTimeOut = 5*60000;
	
	/**
	 * 检查空闲超时的间隔时间，缺省：60000
	 */
	private int idleCheckInterval = 10000;
	
	/**
	 * 系统运行以来被拒绝的连接个数
	 */
	private long rejectTaskCount = 0;
	
	/**
	 * 连接等待队列所采用的实现方式
	 */
	private String queueType = "Array";

	/**
	 * 超过当前并发线程的连接等待队列
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
	 * 启动监听服务
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
		//创建并发线程池及等待队列
		socketWaitingQueue = this.createHealthyBlockingQueue();
		if(this.maxConnection < this.poolSize){
			EMPLog.log(EMPConstance.EMP_TCPIP, EMPLog.WARNING, 0,
							"ListenPort maxConnection[" + this.maxConnection
									+ "] is less than poolSize["
									+ this.poolSize + "]!");
			this.maxConnection = this.poolSize;
		}
		this.threadPool =  new ThreadPoolExecutor(this.poolSize, this.maxConnection,0L, TimeUnit.MILLISECONDS, socketWaitingQueue);
		
		//超过等待队列后拒绝新的连接
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
		//初始化拒绝新连接时所要返回的报文内容
		this.rejectMessageBytes = this.commProcessor.wrapMessagePackage(this.rejectMessage.getBytes());
		
		//当拥有等待队列且设置等待时间后，增加队列的超时检测线程
		if(this.queueSize > 0 && this.waitTime > 0){
			socketWaitingQueue.startTaskHealthyCheck();
		}
		
		//启动Socket端口侦听
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
	 * 停止线程，关闭套接字
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
	 * 创建一个连接等待队列的实例
	 * 该方法可供子类重写，以修改队列的实现方式
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
	 * Socket接入之前触发的动作
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
	 * Socket接入之后触发的动作
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
	 * 连接池大小
	 * @return
	 */
	public int getPoolSize() {
		return this.poolSize;
	}
	
	/**
	 * 当前正在处理的连接数
	 * @return
	 */
	public int getCurrentConnections(){
		return this.threadPool.getActiveCount();
	}
	
	/**
	 * 已处理的连接数
	 * @return
	 */
	public long getTaskCount() {
		return this.threadPool.getTaskCount();
	}
	
	/**
	 * 超时的连接数
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
	 * 被拒绝的连接数
	 * @return
	 */
	public long getRejectTaskCount(){
		return this.rejectTaskCount;
	}
	
	/**
	 * 历史最大线程数
	 * @return
	 */
	public long getLargestPoolSize() {
		return this.threadPool.getLargestPoolSize();
	}
	
	
	/**
	 * 等待队列大小
	 * @return
	 */
	public int getQueueSize() {
		return queueSize;
	}
	
	/**
	 * 正在等待的连接数
	 * @return
	 */
	public int getWaitCount(){
		return this.threadPool.getQueue().size();
	}

	/**
	 * 超过等待队列后能够处理的最大连接数
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
	 * @emp:name 线程池大小
	 * @emp:mustSet false
	 * @emp:document 用于处理Socket接入的线程池大小。缺省：DEFAULT_POOL_SIZE
	 */
	public void setPoolSize(int poolSize) {
		if(poolSize < 0){
			poolSize = Integer.MAX_VALUE;
		}
		this.poolSize = poolSize;
	}
	
	/**
	 * @emp:name 最大允许的连接数
	 * @emp:mustSet false
	 * @emp:document 当等待队列满了之后，允许运行的最大线程数(如果该值大于poolSize，则在队列满了之后会创建线程进行处理)。缺省：DEFAULT_POOL_SIZE
	 */
	public void setMaxConnection(int maxConnection) {
		if(maxConnection < 0){
			maxConnection = Integer.MAX_VALUE;
		}
		this.maxConnection = maxConnection;
	}
	
	/**
	 * @emp:name 连接拒绝的信息
	 * @emp:mustSet false
	 * @emp:document 拒绝接入时所返回的信息。缺省：System busy!
	 */
	public void setRejectMessage(String rejectMessage) {
		this.rejectMessage = rejectMessage;
	}

	/**
	 * @emp:name 连接等待时间
	 * @emp:mustSet false
	 * @emp:document 达到最大连接后，新连接等待的时间(>0:等待的毫秒数;其它:死等)。缺省是：死等
	 */
	public void setWaitTime(long waitTime) {
		this.waitTime = waitTime;
	}
	
	/**
	 * @emp:name 等待队列大小
	 * @emp:mustSet false
	 * @emp:document 用于接入Socket等待的队列大小。缺省：DEFAULT_QUEUE_SIZE
	 */
	public void setQueueSize(int queueSize) {
		if(queueSize < 0){
			queueSize = Integer.MAX_VALUE;
		}
		this.queueSize = queueSize;
	}


	/**
	 * @emp:name 空闲超时时间
	 * @emp:mustSet false
	 * @emp:document 在长连接状态下，连接空闲如果超过一定的时间则会被释放(如果设置为-1，则表示不启动超时时间)。缺省：不启动检查
	 */
	public void setIdleTimeOut(int idleTimeOut) {
		this.idleTimeOut = idleTimeOut;
	}
	
	/**
	 * @emp:name 空闲检查的轮询时间
	 * @emp:mustSet false
	 * @emp:document 在长连接状态下，间隔一定的时间轮询检查连接的空闲时间。缺省是：5*60000(ms)
	 */
	public void setIdleCheckInterval(int idleCheckInterval) {
		this.idleCheckInterval = idleCheckInterval;
	}

	/**
	 * @emp:name 绑定IP地址
	 * @emp:mustSet false
	 * @emp:document 在本地多网络地址的情况下，绑定本地的某个地址
	 */
	public void setBindAddress(String bindAddress) {
		this.bindAddress = bindAddress;
	}
	
	/**
	 * @emp:name 是否启动空闲超时检查
	 * @emp:valueList true=是;false=否;
	 * @emp:mustSet false
	 * @emp:document 长连接条件下，是否需要针对接入的Socket进行空闲超时检查。缺省：否
	 */
	public void setIdleCheck(boolean idleCheck) {
		this.idleCheck = idleCheck;
	}
	
	public String getQueueType() {
		return queueType;
	}
	
	/**
	 * @emp:name 队列类型
	 * @emp:valueList Array=Array;Linked=Linked;
	 * @emp:mustSet false
	 * @emp:document 连接等待队列所采用的实现方式。缺省：Array
	 */
	public void setQueueType(String queueType) {
		this.queueType = queueType;
	}
	
	public int getBacklog() {
		return backlog;
	}

	/**
	 * @emp:name 操作系统请求队列大小
	 * @emp:mustSet false
	 * @emp:document 操作系统请求队列大小(ServerSocket的backlog参数设置)，缺省为0
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
