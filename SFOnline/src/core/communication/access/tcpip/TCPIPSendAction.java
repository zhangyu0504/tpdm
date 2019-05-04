package core.communication.access.tcpip;

import java.net.InetAddress;

import module.bean.LocalInfo;
import module.bean.Param;
import module.cache.ParamCache;

import com.ecc.emp.core.Context;
import com.ecc.emp.core.EMPException;
import com.ecc.emp.data.InvalidArgumentException;
import com.ecc.emp.data.KeyedCollection;
import com.ecc.emp.data.ObjectNotFoundException;
import com.ecc.emp.flow.reversal.HostAccessAction;
import com.ecc.emp.format.FormatElement;
import common.exception.SFException;
import common.services.PublicService;
import common.util.BizUtil;
import common.util.DateUtil;
import common.util.SFConst;
import common.util.SFUtil;

import core.cache.CacheMap;
import core.communication.format.KeyedFormat;
import core.communication.util.AccessConstance;
import core.log.SFLogger;

/**
 * ����������̬TCPIP����ͨѶ��չ��  
 * <p>
 * �๦��˵����
 * <pre>
 * ��̬TCPIP����ͨѶ��չ�ࡣ
 * ��չTCPIP����ͨѶ���ܣ�ʹ��ʱ���ö�̬IP�Ͷ�̬�˿ڡ�
 * ����TCPIP�����Ĳ������裬ͨ������TCPIP��������TCPIPService����ʵ��TCP/IPЭ���ͨѶ���ܡ�
 * </pre> 
 * ʹ��˵���� 
 * <pre>
 * 
 * </pre>
 * ����˵����
 *<pre>
 * ����״̬��<br>
 * 0���ɹ���2���õ�TCPIPService�������3������ͨѶ�쳣��4����ʱ�쳣
 *</pre>
 * ����˵����
 *<pre>
 * ���Ա����˵�� 
 * serviceName��TCPIPͨѶ����ID<br>
 * timeOut��ͨѶ��ʱʱ��(ms)<br>
 * SendFormatName�����ͱ��ĸ�ʽ�������ƣ�Ĭ��ΪsendHostFormat<br>
 * ReceiveFormatName�����ձ��ĸ�ʽ�������ƣ�Ĭ��ΪreceiveFormatName<br>
 * encoding���ַ�����
 * hostIP: ����IP��ַ
 * hostPort: �����˿�
 *</pre>
 * @author hzf
 * @since  1.0 2011-04-16
 * @version 1.0
 * �޸�˵�����޸���ͨѶʵ���࣬ԭempʵ�ֵ���ѹ�����Ը߲������޷�����ѹ�� 
 * �����Ŀǰ֧��6λ��4λ��0λ���ĳ��ȵ�ͨѶЭ�� �����Ҫ֧����������Э������Ҫ����Ӧ�޸� by zhanglm@hundsun
 */
public class TCPIPSendAction extends HostAccessAction {

	// /**
	// * ���ͱ��ĸ�ʽ��������
	// */
	// private String sendFormatName = "sendHostFormat";
	//
	// /**
	// * ���ձ��ĸ�ʽ��������
	// */
	// private String receiveFormatName = "receiveFormatName";
	//
	/**
	 * ��ʱʱ��
	 */
	private int connectTimeOut = 40000;

	private int socketTimeOut = 40000;
	/**
	 * ���ļ����������������
	 */
	// private String identityField;

	/**
	 * �ַ�����
	 */
	private String encoding;

	/**
	 * ����IP
	 */
	private String hostIP;

	/**
	 * �����˿�
	 */
	private String hostPort;

	/**
	 * ���ĳ����ֶεĳ���
	 */
	// private String lengthHeadLen;
	/**
	 * MAC��ʶ
	 */
	private String mac;

	/**
	 * �����б��
	 */
	private String bankNo;

	/**
	 * �������
	 */
	private String svrCode;

	public TCPIPSendAction() {
		super();
	}

	/**
	 * ��������˵����
	 * <pre>
	 * HostAccessAction��ִ����ڡ�����TCPIPService����ͨѶ������
	 * </pre>
	 *  �޸ļ�¼˵���� 
	 * <pre>
	 * 
	 * </pre>
	 * @param context ����������
	 * @return 0���ɹ���2���õ�TCPIPService�������3������ͨѶ�쳣; 4, ��ʱ
	 * @throws EMPException
	 * 
	 */
	public String doExecute( Context context ) throws EMPException {
		String hostIP = ( String )getParamValue( context, "hostIP", this.hostIP, true );
		String hostPort = ( String )getParamValue( context, "hostPort", this.hostPort, true );
		String bankNo = null, svrCode = null;
		bankNo = getParamValue( context, "bankNo", this.bankNo, true ).toString();
		svrCode = getParamValue( context, "svrCode", this.svrCode, true ).toString();
		try {
			SFLogger.info( context, "ͨѶ���ӷ��� [/" + InetAddress.getLocalHost().getHostAddress() + "<-->/" + hostIP + "]" );
			BBCPCommClient client = null;
			byte[] toSendMessage = null;
			byte[] repMsg = null;
			repMsg = getRequestMsg( context );
			
			String cTimeout = ParamCache.getValue2("COBANK", "CONNECT_TIMEOUT");//���ӳ�ʱʱ��
			String sTimeout = ParamCache.getValue2("COBANK", "SOCKET_TIMEOUT");//ͨѶ��ʱʱ��
			if(SFUtil.isNotEmpty(cTimeout)){
				connectTimeOut = Integer.parseInt(cTimeout);
			}
			if(SFUtil.isNotEmpty(sTimeout)){
				socketTimeOut = Integer.parseInt(sTimeout);
			}
			// ������
			PublicService pubService = ( ( PublicService )context.getService( ( String )context.getDataValue( SFConst.SERVICE_PUBLICSERVICENAME ) ) );
			/*
			 * �ж��������ⷢ���Ƿ���ڵ���
			 */
			if(pubService.hasBaffle(context, "2", svrCode, bankNo, connectTimeOut)){
				String reqData = pubService.baffle(context, "2", svrCode, bankNo, connectTimeOut);
				if( SFUtil.isNotEmpty( reqData ) ) {
					handleResponse( context, reqData.getBytes() );
					return "0";
				}else{
					return "-1";
				}
			}

			// SFLogger.info(context, "�������ݣ�"+new String(repMsg) );
			client = new BBCPCommClient( hostIP, hostPort, connectTimeOut,socketTimeOut, 4 );
			// ���㱨�ĳ��ȣ������ҪMACУ����Ҫ���ĳ��Ȼ����ϼ�8λ�ַ�
			// int msgLength=BizUtil.getSourceLength(context,repMsg.length);
			String len = "0000" + ( repMsg.length + 8 );
			toSendMessage = new byte[ 4 + repMsg.length ];
			System.arraycopy( ( len.substring( len.length() - 4, len.length() ) ).getBytes(), 0, toSendMessage, 0, 4 );
			System.arraycopy( repMsg, 0, toSendMessage, 4, repMsg.length );
			/*
			 * Mac ����
			 */
			String sendMsg = new String( toSendMessage );
			sendMsg = sendMsg + BizUtil.genCoBankMac( context, sendMsg );
			toSendMessage = sendMsg.getBytes();

			String ret = client.SendCMD( context, toSendMessage );
			if( !"0".equals( ret ) ) {
				SFUtil.setDataValue( context, SFConst.PUBLIC_RET_FLAG, SFConst.RET_OVERTIME );// �ֹ����ó�ʱ

				// SFUtil.setDataValue(context,"A_RESPONSE_HEAD.RESPCODE", SFConst.RESPCODE_TIMEOUT_COBANK);
				// SFUtil.setDataValue(context,"A_RESPONSE_HEAD.RESPMSG", "��������Ӧ����ʧ��");
				throw new EMPException( "YYPTERROR9999", "TCPIPDynamicAccessActionͨѶ����" + ret );
			}
			byte[] toReciveMessage = null;
			toReciveMessage = client.getResult().getBytes();// ����������ȫ������
			if( toReciveMessage == null || toReciveMessage.length == 0 ) {
				SFLogger.info( context, "TCPIPDynamicAccessActionͨѶ���󣺽��շ�������Ϊ�ա�������ʱ���ش��������س�ʱ4" );
				return "4";
			}
			/*
			 * Mac У��
			 */
			String macMsg = SFUtil.fixChar( String.valueOf( toReciveMessage.length ), 4, '0', "left" ) + new String( toReciveMessage );
			if( !context.containsKey( "A_RESPONSE_HEAD.CHCICSCODE" ) ) {
				BizUtil.chkCoBankMac( context, macMsg );
			}

			handleResponse( context, toReciveMessage );

		} catch( Exception tempE ) {
			SFLogger.error( context, "ͨѶ�����쳣��" + tempE.getMessage(), tempE );
			if( "receiveTimeOut".equals( tempE.getMessage() ) ) {
				SFUtil.setDataValue( context, SFConst.PUBLIC_RET_FLAG, SFConst.RET_OVERTIME );// �ֹ����ó�ʱ

				// SFUtil.setDataValue(context,"A_RESPONSE_HEAD.RESPCODE", SFConst.RESPCODE_TIMEOUT_COBANK);
				// SFUtil.setDataValue(context,"A_RESPONSE_HEAD.RESPMSG", "��������Ӧ����ʧ��");
				return "4"; // ��չ�����BBCPComm���ؽ��ܳ�ʱ
			}
			return "3";
		} finally {
			long endTime = System.currentTimeMillis();
			// SFLogger.info(context, "TCPIPDynamicAccessActionִ�н���ʱ��:["+endTime+"],�ܺ�ʱ�� "+(endTime-beginTime)+"(ms)" );
		}

		return "0";
	}

	// ������������
	private void handleResponse( Context context, byte[] responseMsg ) throws Exception {

		KeyedCollection kColl = new KeyedCollection( AccessConstance.A_RESPONSE_HEAD );
		kColl.setAppend( true );
		FormatElement requestHeadFormat = context.getFormat( AccessConstance.A_RESPONSE_HEAD );
		if( requestHeadFormat.isBin() )
			requestHeadFormat.unFormat( responseMsg, kColl );
		else
			requestHeadFormat.unFormat( new String( responseMsg ), kColl );

		// ȥ���ո�
		if( kColl != null ) {
			for( int i = 0; i < kColl.size(); i++ ) {
				String value = ( String )kColl.getDataValue( kColl.getDataElement( i ).getName() );
				kColl.setDataValue( kColl.getDataElement( i ).getName(), value.trim() );

			}

		}

		if( !context.containsKey( AccessConstance.A_RESPONSE_HEAD ) ) {
			context.addDataElement( kColl );
		} else {
			context.removeDataElement( AccessConstance.A_RESPONSE_HEAD );
			context.addDataElement( kColl );
		}

		// ���ݱ���ͷ�ķ��������ж��Ƿ���Ҫ���뱨�������ݣ�ֻ�з���"000000"�ɹ�ʱ�ŷ��ر�����
		if( !SFConst.RESPCODE_SUCCCODE_COBANK.equals( SFUtil.getDataValue( context, "A_RESPONSE_HEAD.RESPCODE" ) ) ) {
			return;
		}

		KeyedFormat headFormat = ( KeyedFormat )requestHeadFormat.getFormatElement();

		int headLen = headFormat.getHeadLen();

		int length = responseMsg.length - headLen;
		if( length > 0 ) {
			byte[] msg = new byte[ length ];
			System.arraycopy( responseMsg, headLen, msg, 0, length );

			// KeyedCollection outColl = new KeyedCollection(this.svrCode+"_O");
			// outColl.setAppend( true );
			FormatElement resFormat = context.getFormat( this.svrCode + "_O" );
			if( resFormat.isBin() )
				resFormat.unFormat( msg, context );
			else
				resFormat.unFormat( new String( msg ), context );

		}

	}

	public byte[] getRequestMsg( Context context ) throws EMPException {

		initHeadKcoll( context );
		// initMsgHead(1, context);
		KeyedCollection headColl = ( KeyedCollection )context.getDataElement( AccessConstance.A_REQUEST_HEAD );
		FormatElement headFormat = context.getFormat( AccessConstance.A_REQUEST_HEAD );
		String headData = ( String )headFormat.format( headColl );
		// KeyedCollection inColl = (KeyedCollection)context.getDataElement(this.svrCode + "_I");
		FormatElement msgFormat = context.getFormat( this.svrCode + "_I" );

		if( msgFormat == null ) {
			throw new SFException( "ST9999", "������û�ҵ�" + this.svrCode + "_I" + "����CobankFormats.xml�ļ�" );
		}

		String responseData = ( String )msgFormat.format( context );
		// String responseData = (String)msgFormat.format(outColl);
		int len = headData.length() + responseData.length();
		String resData = headData + responseData;// String.valueOf(len)+
		return resData.getBytes();

	}

	public void initHeadKcoll( Context context ) throws EMPException {
		// initMsgHead(1, context);
		String strJournalNo = null;
		String initSide = SFUtil.getDataValue( context, SFConst.PUBLIC_INIT_SIDE );// ��ȡ����
		LocalInfo localInfo = SFUtil.getDataValue( context, SFConst.PUBLIC_LOCAL_INFO );
		strJournalNo = SFUtil.getDataValue( context, SFConst.PUBLIC_MSG_SEQ_NO );
		if( SFUtil.isEmpty( strJournalNo ) ) {
			strJournalNo = BizUtil.getInitSeqId( context );
		}

		KeyedCollection headColl = null;
		if( context.containsKey( AccessConstance.A_REQUEST_HEAD ) ) {
			headColl = SFUtil.getDataElement( context, AccessConstance.A_REQUEST_HEAD );// (KeyedCollection)context.getDataElement(AccessConstance.A_REQUEST_HEAD);

			handleKcollValue( headColl, "RESPCODE", "SF9999" );
			handleKcollValue( headColl, "RESPMSG", "" );

			handleKcollValue( headColl, "serviceId", this.svrCode );
			handleKcollValue( headColl, "TRANSDATE", DateUtil.getMacDate() );
			handleKcollValue( headColl, "TRANSTIME", DateUtil.getMacTimeFormat() );

			handleKcollValue( headColl, "CHANNELSERIALNO", strJournalNo );
			if( localInfo != null ) {
				handleKcollValue( headColl, "BUSINESSDATE", localInfo.getWorkdate() );
			} else {
				handleKcollValue( headColl, "BUSINESSDATE", DateUtil.getMacDate() );
			}

			handleKcollValue( headColl, "BANKNO", this.bankNo );

			// �޶�ȯ�̶˳���ǩԼ������810021�ӿڱ���ͷδ�͹�Ա������ edit by lch 20180224
			if( SFConst.INIT_SIDE_SECU.equals( initSide ) && ( "810021".equals( svrCode ) || "810022".equals( svrCode ) || "810025".equals( svrCode ) || "810026".equals( svrCode ) ) ) {
				handleKcollValue( headColl, "RESERVE", "        " + SFConst.DEFAULT_COUNTER_ID );
			} else {
				handleKcollValue( headColl, "RESERVE", "" );
			}

		} else {

			headColl = new KeyedCollection( AccessConstance.A_REQUEST_HEAD );
			headColl.addDataField( "RESPCODE", "SF9999" );
			headColl.addDataField( "RESPMSG", "" );
			headColl.addDataField( "serviceId", this.svrCode );
			headColl.addDataField( "TRANSDATE", DateUtil.getMacDate() );
			headColl.addDataField( "TRANSTIME", DateUtil.getMacTimeFormat() );
			headColl.addDataField( "CHANNELSERIALNO", strJournalNo );
			if( localInfo != null ) {
				headColl.addDataField( "BUSINESSDATE", localInfo.getWorkdate() );
			} else {
				headColl.addDataField( "BUSINESSDATE", DateUtil.getMacDate() );
			}
			headColl.addDataField( "BANKNO", this.bankNo );
			// �޶�ȯ�̶˳���ǩԼ������810021�ӿڱ���ͷδ�͹�Ա������ edit by lch 20180224
			if( SFConst.INIT_SIDE_SECU.equals( initSide ) && ( "810021".equals( svrCode ) || "810022".equals( svrCode ) || "810025".equals( svrCode ) || "810026".equals( svrCode ) ) ) {
				headColl.addDataField( "RESERVE", "        " + SFConst.DEFAULT_COUNTER_ID );
			} else {
				headColl.addDataField( "RESERVE", "" );
			}
			context.addDataElement( headColl );
		}
	}

	public void handleKcollValue( KeyedCollection kcoll, String key, String value ) {

		String oldValue = null;
		try {
			oldValue = ( String )kcoll.getDataValue( key );
			if( SFUtil.isEmpty( oldValue ) ) {
				kcoll.setDataValue( key, value );
			}

		} catch( ObjectNotFoundException e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch( InvalidArgumentException e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * �����ļ���mac��
	 * @param msg ����mac�ı���
	 * @param bankNo 
	 * @param svrCode
	 * @return  ��mac�ı���
	 * @throws EMPException 
	 */
	/*
	 * private byte[] getMessageByMac(byte[] message,String bankNo,String svrCode) throws EMPException{ String tmpMsg = new String(message); String mac = MacUtil.genMac(StringTool.FillString(tmpMsg, ' ', 1, message.length+8).getBytes(), getMacKey(bankNo, svrCode)); return (tmpMsg+mac).getBytes(); }
	 */

	/*
	 * private String getMacKey(String bankNo,String svrCode) throws EMPException{ BankInfo bankInfo = SysParamCache.getSysCPIPMap().get(bankNo+svrCode); if(bankInfo == null){ EMPLog.log(SFConst.SF_SYS_TRXCODE, EMPLog.ERROR, 0, "BBCPTCPIPDynamicAccessByMacAction:������δ���ã�ȡ�����з����Ӧ��������ϢΪ�գ�bankno=["+bankNo+"],svrcode=["+svrCode+"]"); throw new EMPException("999999","ȡ�����з����Ӧ��������ϢΪ�գ�bankno=["+bankNo+"],svrcode=["+svrCode+"]"); } return bankInfo.getMacKey(); }
	 */

	/**
	 * ����ͨѶ��ʱʱ�䡣
	 * 
	 * @param timeOut ͨѶ���ӳ�ʱʱ��
	 * @emp:isAttribute true
	 * @emp:name ��ʱʱ��
	 * @emp:desc ͨѶ��ʱʱ��
	 */
	public void setConnectTimeOut( int timeOut ) {
		this.connectTimeOut = timeOut;
	}

	/**
	 * ����ͨѶ��ʱʱ�䡣
	 * 
	 * @param timeOut ͨѶ��Ӧ��ʱʱ��
	 * @emp:isAttribute true
	 * @emp:name ��ʱʱ��
	 * @emp:desc ͨѶ��ʱʱ��
	 */
	public void setSocketTimeOut( int timeOut ) {
		this.socketTimeOut = timeOut;
	}
	/**
	 * �����ַ����롣
	 * 
	 * @param encoding �ַ�����
	 * @emp:isAttribute true
	 * @emp:name �ַ�����
	 * @emp:desc ����ת��Ϊ�ֽ�����ʹ�õ��ַ�����
	 */
	public void setEncoding( String encoding ) {
		this.encoding = encoding;
	}

	/**
	 * ���ñ��ļ����������������
	 * �Ὣ���������д�ŵĶ��󴫸�TCPIPService���ڽ��յ���Ӧ���ĺ���ݸö����ж��Ƿ�Ϊ��Ҫ���ġ�
	 * 
	 * @param identityField ���ļ����������������
	 * @emp:isAttribute true
	 * @emp:name ���ļ������������
	 * @emp:desc �Ὣ���������д�ŵĶ��󴫸�TCPIPService���ڽ��յ���Ӧ���ĺ���ݸö����ж��Ƿ�Ϊ��Ҫ����
	 * @emp:editClass com.ecc.ide.editor.transaction.DataNamePropertyEditor
	 */
	// public void setIdentityField(String identityField) {
	// this.identityField = identityField;
	// }

	public void setHostIP( String hostIP ) {
		this.hostIP = hostIP;
	}

	public void setHostPort( String hostPort ) {
		this.hostPort = hostPort;
	}

	// public void setLengthHeadLen(String lengthHeadLen) {
	// this.lengthHeadLen = lengthHeadLen;
	// }

	/**
	 * @param mac the mac to set
	 */
	public void setMac( String mac ) {
		this.mac = mac;
	}

	/**
	 * @param bankNo the bankNo to set
	 */
	public void setBankNo( String bankNo ) {
		this.bankNo = bankNo;
	}

	/**
	 * @param svrCode the svrCode to set
	 */
	public void setSvrCode( String svrCode ) {
		this.svrCode = svrCode;
	}

}