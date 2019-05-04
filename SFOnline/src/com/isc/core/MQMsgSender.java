package com.isc.core;

import java.util.Hashtable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ecc.emp.core.Context;
import com.ibm.mq.MQException;
import com.ibm.mq.MQMessage;
import com.ibm.mq.MQPutMessageOptions;
import com.ibm.mq.MQQueue;
import com.ibm.mq.MQQueueManager;
import com.ibm.mq.MQSimpleConnectionManager;

import core.log.SFLogger;

/**
 * MQ��Ϣ������
 * 
 * ��д��JAR������Ϣ��������������˽�н���Context 
 * ʵ��˽�н�����Ϣ����ʵʱ��־
 * <p>Description:	ʵ������Ϣ���ͽӿڣ���־���¹滮��˽�н�����	<p>
 * <p>Module:			<p>
 *
 * @author EX_KJKFB_LVCHAOHONG
 * @date 2018-3-14 ����02:48:51
 * @since 1.0
 */
public class MQMsgSender implements MsgSender {

	private static Log log = LogFactory.getLog( MQMsgSender.class );

	private MQSimpleConnectionManager myConnMan = null;

	private Hashtable hashtable;

	private String qManagerName;

	private String queueName;

	private long lastTime = 0L;

	private boolean isAlive = true;

	public MQMsgSender( MQSimpleConnectionManager myConnMan, Hashtable hashtable, String qManagerName, String queueName ) {
		this.myConnMan = myConnMan;
		this.hashtable = hashtable;
		this.qManagerName = qManagerName;
		this.queueName = queueName;
	}

	public MQSimpleConnectionManager getMyConnMan() {
		return this.myConnMan;
	}

	public void setMyConnMan( MQSimpleConnectionManager myConnMan ) {
		this.myConnMan = myConnMan;
	}

	public Hashtable getHashtable() {
		return this.hashtable;
	}

	public void setHashtable( Hashtable hashtable ) {
		this.hashtable = hashtable;
	}

	public String getqManagerName() {
		return this.qManagerName;
	}

	public void setqManagerName( String qManagerName ) {
		this.qManagerName = qManagerName;
	}

	private void disAbled() {
		this.isAlive = false;
		this.lastTime = System.currentTimeMillis();
	}

	public boolean isAlive() {
		if( this.isAlive ) {
			return this.isAlive;
		}
		long unalivetime = System.currentTimeMillis() - this.lastTime;
		if( unalivetime > 30000L ) {
			boolean alive = testAlive();
			return alive;
		}

		return this.isAlive;
	}

	public String getQueueName() {
		return this.queueName;
	}

	public void setQueueName( String queueName ) {
		this.queueName = queueName;
	}

	public MQMsgSender() {}

	private MQQueueManager getMQQueueManager() {
		MQQueueManager qMgr = null;
		try {
			if( this.myConnMan != null )
				qMgr = new MQQueueManager( this.qManagerName, this.hashtable, this.myConnMan );
			else
				qMgr = new MQQueueManager( this.qManagerName, this.hashtable );
		} catch( Exception e ) {
			log.error( "����MQ������ʧ��[" + toString() + "]", e );

			disAbled();
			return null;
		}

		return qMgr;
	}

	public boolean testAlive() {
		MQQueueManager qMgr = null;
		try {
			if( this.myConnMan != null )
				qMgr = new MQQueueManager( this.qManagerName, this.hashtable, this.myConnMan );
			else {
				qMgr = new MQQueueManager( this.qManagerName, this.hashtable );
			}

			int openOptions = 16;
			MQQueue queue = qMgr.accessQueue( this.queueName, openOptions );
			queue.close();
			qMgr.disconnect();
		} catch( Exception e ) {
			disAbled();
			return false;
		}

		return true;
	}

	public String toString() {
		return "MQMsgSender [hashtable=" + this.hashtable + ", qManagerName=" + this.qManagerName + ", queueName=" + this.queueName + "]";
	}

	public void SendMsg( Context context, Hashtable MsgProperty, byte[] Msg ) throws Exception {
		SendMsg( context, this.queueName, MsgProperty, Msg );
	}

	public void SendMsg( Context context, String queueName, Hashtable MsgProperty, byte[] Msg ) throws Exception {
		MQQueueManager qMgr = null;
		try {
			qMgr = getMQQueueManager();

			if( qMgr == null ) {
				throw new Exception( "����Զ��MQ������ʧ��qManagerName" );
			}

			MQMessage msg = new MQMessage();

			MQPutMessageOptions putOpt = new MQPutMessageOptions();
			putOpt.options = ( putOpt.options + 8192 + 2 );

			msg.write( Msg );
			qMgr.put( queueName, msg, putOpt );
			qMgr.commit();
			qMgr.disconnect();

			SFLogger.info( context, "��Ϣ���ͳɹ�  head info[" + MsgProperty + "]" );
		} catch( MQException mqe ) {
			int CompCode = mqe.getCompCode();
			int ReasonCode = mqe.getReason();
			SFLogger.error( context, "[" + toString() + "]��Ϣ����ʧ��CompCode[" + CompCode + "]ReasonCode[" + ReasonCode + "]" );
			log.error( "[" + toString() + "]��Ϣ����ʧ��CompCode[" + CompCode + "]ReasonCode[" + ReasonCode + "]", mqe );
			try {
				if( qMgr != null ) {
					qMgr.backout();
					qMgr.disconnect();
				}
			} catch( Exception re ) {
				SFLogger.error( context, "[" + toString() + "��Ϣ����ʧ��" );
				log.error( "[" + toString() + "��Ϣ����ʧ��", re );
			}
			throw mqe;
		} catch( Exception e ) {
			SFLogger.error( context, "[" + toString() + "��Ϣ����ʧ��" );
			log.error( "[" + toString() + "��Ϣ����ʧ��", e );
			try {
				if( qMgr != null ) {
					qMgr.backout();
					qMgr.disconnect();
				}
			} catch( Exception re ) {
				SFLogger.error( context, "[" + toString() + "��Ϣ�ع�ʧ��" );
				log.error( "[" + toString() + "��Ϣ�ع�ʧ��", re );
			}
			throw e;
		}
	}

}