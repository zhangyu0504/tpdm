package com.ecc.emp.tcpip;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.ecc.emp.concurrent.EMPConcurrentTask;
import com.ecc.emp.core.EMPConstance;
import com.ecc.emp.core.EMPException;
import common.util.SFConst;

import core.log.SFLogger;


public class SocketProcessThread
  implements Runnable, EMPConcurrentTask
{
  private boolean keepAlive = false;
  private CommProcessor commProcessor;
  private PackageReceiver packageReceiver;
  private Socket socket = null;
  private List socketAcceptListeners;
  private long lastAccess;
  private byte[] rejectMessageBytes;
  private long waitTime;
  private boolean idleCheck = false;

  private int idleTimeOut = 300000;

  private int idleCheckInterval = 60000;

  protected Timer idleCheckTimer = null;

  protected Object synObj = new Object();

  private boolean isStop = false;

  private boolean isHandle = true;
  protected String socketInfo;

  public SocketProcessThread(Socket socket, CommProcessor commProcessor, PackageReceiver packageReceiver, boolean keepAlive)
  {
    this.socket = socket;
    this.commProcessor = commProcessor;
    this.packageReceiver = packageReceiver;
    this.keepAlive = keepAlive;

    this.socketInfo = ("[" + this.socket.getLocalSocketAddress() + "<-->" + this.socket.getRemoteSocketAddress() + "]");
    this.lastAccess = System.currentTimeMillis();
  }

  public void run() {
    //SFLogger.info(SFConst.DEFAULT_TRXCODE, null, "SocketProcessThread start to handle the socket " + this.socketInfo);
    
    
    this.lastAccess = System.currentTimeMillis();
    if (this.keepAlive) {
      if (this.idleCheck) {
        this.idleCheckTimer = new Timer();
        this.idleCheckTimer.schedule(new IdleCheckTask(), this.idleCheckInterval, this.idleCheckInterval);
      }
      InputStream in = null;
      try {
        in = this.socket.getInputStream();
        while (!this.isStop)
          runTask(this.socket, in);
      }
      catch (Exception e) {
        SFLogger.error(SFConst.DEFAULT_TRXCODE, null, "The keepAlive Socket " + this.socketInfo + " is terminated!", e);
      }
      finally {
        closeCurrentThread(this.socket, in, null);
      }
    } else {
      InputStream in = null;
      try {
        in = this.socket.getInputStream();
        runTask(this.socket, in);
      } catch (Exception e) {
        SFLogger.error(SFConst.DEFAULT_TRXCODE, null, "An error is occured while handle the socket " + this.socketInfo + " in SocketProcessThread!", e);
      }
      finally {
        closeCurrentThread(this.socket, in, null);
      }
    }
  }

  protected void runTask(Socket socket, InputStream in) throws Exception {
    if ((socket == null) || (in == null)) {
      throw new EMPException("The socket instance is null in SocketProcessThread!");
    }
    byte[] readMsg = this.commProcessor.readPackage(in);
    synchronized (this.synObj) {
      this.lastAccess = System.currentTimeMillis();
      this.isHandle = true;
    }
    if ((readMsg != null) && (this.packageReceiver != null)) {
      this.packageReceiver.newPackageReceived(readMsg, socket);
    }
    synchronized (this.synObj) {
      this.isHandle = false;
    }
  }

  protected void closeCurrentThread(Socket socket, InputStream in, OutputStream out) {
    fireSocketToBeClosedEvent(socket);
    this.isStop = true;
    if (in != null)
      try {
        in.close();
      }
      catch (Exception e) {
      }
    if (out != null)
      try {
        out.close();
      }
      catch (Exception e) {
      }
    if (socket != null)
      try {
        socket.close();
      }
      catch (Exception e) {
      }
    fireSocketClosedEvent();

    //SFLogger.info(SFConst.DEFAULT_TRXCODE, null, "SocketProcessThread closed the socket " + this.socketInfo);
  }

  public void sendRejectMessage() {
    Socket socket = this.socket;
    byte[] rejectMessageBytes = this.rejectMessageBytes;

    if ((socket == null) || (rejectMessageBytes == null)) {
      return;
    }
    OutputStream out = null;
    try {
      SFLogger.info(SFConst.DEFAULT_TRXCODE, null, "Reject the socket " + this.socketInfo);

      out = socket.getOutputStream();
      out.write(rejectMessageBytes);
      out.flush();
    } catch (Exception e) {
      SFLogger.error(SFConst.DEFAULT_TRXCODE, null, "Failed to reject the socket " + this.socketInfo, e);
    }
    finally {
      closeCurrentThread(socket, null, out);
    }
  }

  public void fireSocketToBeClosedEvent(Socket socket)
  {
    if (this.socketAcceptListeners == null)
      return;
    for (int i = 0; i < this.socketAcceptListeners.size(); i++) {
      SocketListener listener = (SocketListener)this.socketAcceptListeners.get(i);
      listener.beforeSocketClosed(socket);
    }
  }

  public void fireSocketClosedEvent()
  {
    if (this.socketAcceptListeners == null)
      return;
    for (int i = 0; i < this.socketAcceptListeners.size(); i++) {
      SocketListener listener = (SocketListener)this.socketAcceptListeners.get(i);
      listener.afterSocketClosed();
    }
  }

  public void destroyTask()
  {
    sendRejectMessage();
  }

  public void rejectTask() {
    sendRejectMessage();
  }

  public long leftHealthyTime() {
    if (this.waitTime > 0L) {
      long time = System.currentTimeMillis() - this.lastAccess;
      if (time >= this.waitTime) {
        return -1L;
      }
      return this.waitTime - time;
    }
    return 0L;
  }

  public long getLastAccess()
  {
    return this.lastAccess;
  }

  public CommProcessor getCommProcessor() {
    return this.commProcessor;
  }

  public boolean isKeepAlive() {
    return this.keepAlive;
  }

  public PackageReceiver getPackageReceiver() {
    return this.packageReceiver;
  }

  public Socket getSocket() {
    return this.socket;
  }

  public List getSocketAcceptListeners() {
    return this.socketAcceptListeners;
  }

  public void setSocketAcceptListeners(List socketAcceptListeners) {
    this.socketAcceptListeners = socketAcceptListeners;
  }

  public boolean isIdleCheck() {
    return this.idleCheck;
  }

  public void setIdleCheck(boolean idleCheck) {
    this.idleCheck = idleCheck;
  }

  public int getIdleTimeOut() {
    return this.idleTimeOut;
  }

  public void setIdleTimeOut(int idleTimeOut) {
    this.idleTimeOut = idleTimeOut;
  }

  public int getIdleCheckInterval() {
    return this.idleCheckInterval;
  }

  public void setIdleCheckInterval(int idleCheckInterval) {
    this.idleCheckInterval = idleCheckInterval;
  }

  public boolean isStop() {
    return this.isStop;
  }

  public byte[] getRejectMessageBytes() {
    return this.rejectMessageBytes;
  }

  public void setRejectMessageBytes(byte[] rejectMessageBytes) {
    this.rejectMessageBytes = rejectMessageBytes;
  }

  public long getWaitTime() {
    return this.waitTime;
  }

  public void setWaitTime(long waitTime) {
    this.waitTime = waitTime;
  }

  public String getSocketInfo() {
    return this.socketInfo;
  }
  
  private class IdleCheckTask extends TimerTask
  {
	 public IdleCheckTask(){
		 
	 }

	@Override
	public void run() {
	    synchronized (SocketProcessThread.this.synObj) {
	      if (SocketProcessThread.this.isHandle)
	        return;
	      long duringTime = System.currentTimeMillis() - SocketProcessThread.this.lastAccess;
	      if (duringTime > SocketProcessThread.this.idleTimeOut) {
	        //SFLogger.info(SFConst.DEFAULT_TRXCODE, null, "The keepAlive Socket " + SocketProcessThread.this.socketInfo + " is idle for " + duringTime + " ms!");

	        SocketProcessThread.this.isStop = true;
	        SocketProcessThread.this.closeCurrentThread(SocketProcessThread.this.socket, null, null);
	      }
	    }
	}
	  
  }
}