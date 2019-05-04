package core.communication.access.tcpip;

import java.net.Socket;
import java.util.Map;

import module.bean.ErrorMap;
import module.cache.ErrorMapCache;

import com.ecc.emp.access.tcpip.EMPTCPIPRequest;
import com.ecc.emp.component.factory.ComponentFactory;
import com.ecc.emp.component.factory.EMPFlowComponentFactory;
import com.ecc.emp.core.Context;
import com.ecc.emp.core.EMPConstance;
import com.ecc.emp.core.EMPException;
import com.ecc.emp.data.KeyedCollection;
import com.ecc.emp.format.FormatElement;
import common.exception.SFException;
import common.util.BizUtil;
import common.util.SFConst;
import common.util.SFUtil;

import core.communication.format.KeyedFormat;
import core.communication.tcpip.TCPIPService;
import core.communication.util.AccessConstance;
import core.log.SFLogger;

/**
 * EMPƽ̨ʵ�ֵ�TCP/IP����������
 * <p>
 * ���ڴ���TCP/IP����ı���ͷ���Լ��ӱ���ͷ��ȡ��Ҫ����Ϣ��
 * ��Щ��Ϣ����sessionId��serviceId(�������TCP/IP����ID������)�ȡ�
 * <p>
 * �������£�
 * <p>
 * serviceIdField������ͷ�⿪���ServiceId������<br>
 * sessionIdField������ͷ�⿪���SessionId������<br>
 * appendReqHead���Ƿ���Ҫ��ȥ������ͷ����ѡ��true(Ĭ��)��false<br>
 * appendRepHead���Ƿ���Ҫ�ڷ��ر����и�������ͷ����ѡ��true(Ĭ��)��false<br>
 * errorCodeField��������������������<br>
 * encoding������ͷ����
 * <p>
 * ��Ԫ�أ�
 * <p>
 * requestHeadFormat��������ͷ��ʽ������<br>
 * responseHeadFormat����Ӧ����ͷ��ʽ������<br>
 * serviceIdMap��ServiceIdӳ�䣬�������������ServiceIdӳ�䵽��������ServiceId�У����ڽ��׷ַ�<br>
 * 
 * @author zhongmc
 * @version 2.1
 * @since 1.0 2006-11-29
 * @lastmodified 2008-6-11
 * 
 */
public class PBankTCPIPRequestHandler implements TCPIPRequestHandler {

	/**
	 * ����ͷ����
	 */
	private String encoding = null;

	/**
	 * ����������������
	 */
	private String errorCodeField = "errorCode";

	/**
	 * ������ͷ��ʽ������
	 */
	private FormatElement requestHeadFormat = null;

	/**
	 * ��Ӧ����ͷ��ʽ������
	 */
	private FormatElement responseHeadFormat = null;

	/**
	 * �Ƿ���Ҫ��ȥ������ͷ
	 */
	private boolean appendReqHead = true;

	/**
	 * �Ƿ���Ҫ�ڷ��ر����и�������ͷ
	 */
	private boolean appendRepHead = true;

	/**
	 * ����ͷ�⿪���ServiceId������
	 */
	private String serviceIdField;

	/**
	 * ����ͷ�⿪���SessionId������
	 */
	private String sessionIdField;

	/**
	 * ServiceIdӳ�䣬�������������ServiceIdӳ�䵽��������ServiceId�У����ڽ��׷ַ�
	 */
	private Map serviceIdMap = null;

	public PBankTCPIPRequestHandler() {
		super();
	}

	/**
	 * �����������жϸ������Ƿ�Ϊҵ��������
	 * 
	 * @param msg ������
	 * @return �Ƿ�ҵ��������
	 */
	public boolean isRequestPackage( byte[] msg ) {
		return true;
	}

	// public void setDataTypeDef(Map dataTypeDefs )
	// {
	// this.dataTypeDefs = dataTypeDefs;
	// }

	// public byte[] handleRequest(byte[] requestMsg, Context sessionContext, TCPIPServiceInfo serviceInfo )
	// {
	// return null;
	// }

	/**
	 * ��������EMPTCPIPRequest��װ����
	 * <p>
	 * ���������н������ͷ������ȡ��Session id��Service id��
	 * ��ͬ������һ���װΪEMPTCPIPRequest�Խ��н�һ��������
	 * 
	 * @param requestMsg ������
	 * @return EMPTCPIPRequest
	 */
	public EMPTCPIPRequest getTCPIPRequest( byte[] requestMsg, TCPIPService service, Socket socket ) {
		EMPTCPIPRequest request = new EMPTCPIPRequest();

		try {
			EMPFlowComponentFactory factory = ( EMPFlowComponentFactory )ComponentFactory.getComponentFactory( "SFOnline" );
			Context rootContext = factory.getContextNamed( factory.getRootContextName() );

			KeyedCollection kColl = new KeyedCollection( "head" );
			kColl.setAppend( true );
			FormatElement requestHeadFormat = null;
			if( "a".equals( service.getName() ) ) {
				requestHeadFormat = rootContext.getFormat( AccessConstance.A_REQUEST_HEAD );
			} else {
				requestHeadFormat = rootContext.getFormat( AccessConstance.AA_REQUEST_HEAD );
			}
			request.setAttribute( "serviceName", service.getName() );

			if( requestHeadFormat.isBin() )
				requestHeadFormat.unFormat( requestMsg, kColl );
			else
				requestHeadFormat.unFormat( new String( requestMsg ), kColl );

			request.setAttribute( "headData", kColl );

			KeyedFormat headFormat = ( KeyedFormat )requestHeadFormat.getFormatElement();

			int headLen = headFormat.getHeadLen();

			if( headLen > 0 ) {
				int length = requestMsg.length - headLen;
				byte[] buf = new byte[ length ];
				System.arraycopy( requestMsg, headLen, buf, 0, length );
				request.setAttribute( "reqData", buf );
			} else
				request.setAttribute( "reqData", requestMsg );

			request.setAttribute( "requestMsg", requestMsg );

			if( "b".equals( service.getName() ) ) {
				kColl.addDataField( serviceIdField, ( String )kColl.getDataValue( "CHAPCODE" ) + ( String )kColl.getDataValue( "CHFUNCCODE" ) );
			}

			String serviceId = ( String )kColl.getDataValue( this.serviceIdField );
			request.setAttribute( "serviceId", serviceId );

			return request;
		} catch( Exception e ) {
			SFLogger.error( SFConst.DEFAULT_TRXCODE, null, "Failed to parse the requestMsg in PBankTCPIPRequestHandler to get sessionId", e );
			return null;
		}
	}

	/**
	 * ������������Ӧ���ġ�
	 * <p>
	 * �����ݴ���ɷ��ر���ͷ�����ͷ��ر�����һ�����response��
	 * 
	 * @param request TCPIP����
	 * @param retMsg ���ر�����
	 * @return ��Ӧ����
	 */
	public byte[] getResponsePackage( EMPTCPIPRequest request, byte[] retMsg ) {
		Context context = null;
		try {
			context = ( Context )request.getAttribute( EMPConstance.ATTR_CONTEXT );
			if( this.appendRepHead && this.responseHeadFormat != null ) {
				byte[] headBytes;
				Object retHead = responseHeadFormat.format( context );
				if( responseHeadFormat.isBin() )
					headBytes = ( byte[] )retHead;
				else
					headBytes = ( ( String )retHead ).getBytes();

				if( retMsg != null ) {
					int len = headBytes.length;
					byte[] buf = new byte[ len + retMsg.length ];

					System.arraycopy( headBytes, 0, buf, 0, len );
					System.arraycopy( retMsg, 0, buf, len, retMsg.length );
					return buf;
				} else
					return headBytes;
			}
			return retMsg;

		} catch( Exception e ) {

			SFLogger.error( context, "Failed to process response package! " + e );
			return null;
		}
	}

	/**
	 * �����쳣ʱ����Ӧ���ġ�
	 * <p>
	 * �����ݺ��쳣��Ϣ����ɷ��ر���ͷ��
	 * 
	 * @param request TCPIP����
	 * @param e �쳣
	 * @return �쳣��Ӧ����
	 */
	public byte[] getExceptionResponse( EMPTCPIPRequest request, Exception e ) {
		Context context = null;
		try {
			context = ( Context )request.getAttribute( EMPConstance.ATTR_CONTEXT );
			if( context == null ) {
				context = ( Context )request.getAttribute( EMPConstance.ATTR_SESSION_CONTEXT );
			}

			String serviceId = ( String )request.getAttribute( "serviceId" );
			//
			initHeadKcoll( context );

			String errCode = null;
			if( !context.containsKey( "A_RESPONSE_HEAD.CHCICSCODE" ) ) {
				if( e instanceof SFException ) {
					SFException sfe = ( SFException )e;
					errCode = sfe.getErrorCode();
					String errMsg = sfe.getMessage();
					// ת������ ��
					ErrorMap errorMap = ErrorMapCache.getSdbValue( errCode );

					if( errorMap != null ) {
						// ���쳣��Ӧ��Ϣ�������ó���ʱ���н�ȡ
						//if( errMsg.getBytes().length > 150 ) {
						//	errMsg = BizUtil.getResponseMsg( context, errMsg, 150 );
						//}
						SFUtil.setDataValue( context, "A_RESPONSE_HEAD.RESPCODE", errorMap.getSdbCode() );

						if( "810003".equals( serviceId ) ) {// ��ѯ������ϸ�쳣��Ӧ��Ϣ����$
							SFUtil.setDataValue( context, "A_RESPONSE_HEAD.RESPMSG", "$" );
						} else if( "810005".equals( serviceId ) ) {// ��ѯǩԼ��ϵ�쳣��Ӧ��Ϣ����""
							SFUtil.setDataValue( context, "A_RESPONSE_HEAD.RESPMSG", "" );
						} else {
							SFUtil.setDataValue( context, "A_RESPONSE_HEAD.RESPMSG", SFUtil.isEmpty(errMsg)?errorMap.getErrExplain():BizUtil.returnErrMsg(context,"COBANK", errMsg) );
						}

					} else {
						SFUtil.setDataValue( context, "A_RESPONSE_HEAD.RESPCODE", errCode );
						if( "810005".equals( serviceId ) || "810003".equals( serviceId ) ) {
							SFUtil.setDataValue( context, "A_RESPONSE_HEAD.RESPMSG", "" );
						} else {
							// ���쳣��Ӧ��Ϣ�������ó���ʱ���н�ȡ
							//if( errMsg.getBytes().length > 150 ) {
							//	errMsg = BizUtil.getResponseMsg( context, errMsg, 150 );
							//}
							SFUtil.setDataValue( context, "A_RESPONSE_HEAD.RESPMSG",SFUtil.isEmpty(errMsg)?"����ʧ��":BizUtil.returnErrMsg(context,"COBANK", errMsg));
						}

					}

				} else {
					SFUtil.setDataValue( context, "A_RESPONSE_HEAD.RESPCODE", SFConst.RESPCODE_FAILCODE_COBANK );
					SFUtil.setDataValue( context, "A_RESPONSE_HEAD.RESPMSG", "����ʧ��" );
				}
			} else {
				if( e instanceof SFException ) {
					SFException sfe = ( SFException )e;
					errCode = sfe.getErrorCode();
				}
				SFUtil.setDataValue( context, "A_RESPONSE_HEAD.CHMSGCODE", errCode );
			}

			KeyedCollection headColl = SFUtil.getDataElement( context, AccessConstance.A_RESPONSE_HEAD );// (KeyedCollection)context.getDataElement(AccessConstance.A_RESPONSE_HEAD);

			FormatElement headFormat = null;
			String headData = null;
			if( !context.containsKey( "A_RESPONSE_HEAD.CHCICSCODE" ) ) {
				headFormat = context.getFormat( AccessConstance.A_RESPONSE_HEAD );
				headData = ( String )headFormat.format( headColl );
			} else {
				headFormat = context.getFormat( AccessConstance.AA_RESPONSE_HEAD );
			}

			if( !context.containsKey( "A_RESPONSE_HEAD.CHCICSCODE" ) ) {
				// ���ݱ���ͷ�ķ��������ж��Ƿ���Ҫ���뱨�������ݣ�ֻ�з���"000000"�ɹ�ʱ�ŷ��ر����壬�����漰��MACУ�����͵���Ϣ��
				if( !SFConst.RESPCODE_SUCCCODE.equals( SFUtil.getDataValue( context, "A_REQUEST_HEAD.RESPCODE" ) ) ) {
					// ��ѯǩԼ��ϵ����Ҫ���ر�����
					if( "810005".equals( serviceId ) ) {
						/*
						 * Mac ����
						 */
						// �����в�ѯǩԼ��ϵ��Ҫ����Ӧ���Ķ�ƴ����λ
						headData = headData + "  ";

						String macSource = SFUtil.fixChar( String.valueOf( headData.getBytes().length + 8 ), 4, '0', "left" ) + headData;
						headData = headData + BizUtil.genCoBankMac( context, macSource );
						return headData.getBytes();
					}

				}
			}

			String resData =null;
			/*
			 * Mac ����
			 */
			FormatElement msgFormat = context.getFormat( serviceId + "_O" );
			String responseData = ( String )msgFormat.format( context );
			if( !context.containsKey( "A_RESPONSE_HEAD.CHCICSCODE" ) ) {
				responseData = headData + responseData;//
				String macSource = SFUtil.fixChar( String.valueOf( responseData.getBytes().length + 8 ), 4, '0', "left" ) + responseData;
				String macCode=null;
				try{
					macCode=BizUtil.genCoBankMac( context, macSource );
				}catch(Exception ex){
					macCode="      ";
				}
				resData = responseData +macCode;
			} else {
				responseData = responseData + "      ";
				int len = responseData.getBytes().length;
				context.setDataValue( "A_RESPONSE_HEAD.CHMSGLEN", SFUtil.fixChar( String.valueOf( len ), 4, '0', "left" ) );
				headData = ( String )headFormat.format( headColl );
				resData = headData + responseData;// String.valueOf(len)+
			}


			if( encoding != null )
				return resData.getBytes( encoding );
			else
				return resData.getBytes();

		} catch( Exception ee ) {
			SFLogger.error( context, "Failed to handle exception!" + ee );
		}
		return null;
	}

	public void initHeadKcoll( Context context ) throws EMPException {
		KeyedCollection headColl = null;
		if( context.containsKey( AccessConstance.A_RESPONSE_HEAD ) ) {
			headColl = SFUtil.getDataElement( context, AccessConstance.A_RESPONSE_HEAD );// (KeyedCollection)context.getDataElement(AccessConstance.A_RESPONSE_HEAD);

			if( headColl.containsKey( "CHCICSCODE" ) ) {
				headColl.setDataValue( "CHCICSCODE", "TISC0000" );
				headColl.setDataValue( "CHMSGTYPE", "E" );
			} else {
				headColl.setDataValue( "serviceId", SFUtil.getDataValue( context, "A_REQUEST_HEAD.serviceId" ) );
				if( "810010".equals( SFUtil.getDataValue( context, "A_REQUEST_HEAD.serviceId" ) ) ) {// ��������ת֤ԭ·����CHANNELSERIALNO
					headColl.setDataValue( "CHANNELSERIALNO", SFUtil.getDataValue( context, "A_REQUEST_HEAD.CHANNELSERIALNO" ) );
				}
				headColl.setDataValue( "BANKNO", SFUtil.getDataValue( context, "A_REQUEST_HEAD.BANKNO" ) );
			}

		} else {

			headColl = new KeyedCollection();

			headColl.addDataField( "serviceId", SFUtil.getDataValue( context, "A_REQUEST_HEAD.serviceId" ) );
			if( "810010".equals( SFUtil.getDataValue( context, "A_REQUEST_HEAD.serviceId" ) ) ) {// ��������ת֤ԭ·����CHANNELSERIALNO
				headColl.addDataField( "CHANNELSERIALNO", SFUtil.getDataValue( context, "A_REQUEST_HEAD.CHANNELSERIALNO" ) );
			}
			headColl.addDataField( "BANKNO", SFUtil.getDataValue( context, "A_REQUEST_HEAD.BANKNO" ) );
			context.addDataElement( headColl );
		}
	}

	/**
	 * ��ñ���������������
	 * 
	 * @return ����������������
	 */
	public String getErrorCodeField() {
		return errorCodeField;
	}

	/**
	 * ���ñ���������������
	 * 
	 * @param errorCodeField ����������������
	 * @emp:isAttribute true
	 * @emp:name ����������������
	 * @emp:desc ��ִ���з����쳣���򽫴����뱣���ڸ���������
	 * @emp:defaultValue errorCode
	 */
	public void setErrorCodeField( String errorCodeField ) {
		this.errorCodeField = errorCodeField;
	}

	/**
	 * ���������ͷ��ʽ��������
	 * 
	 * @return ������ͷ��ʽ������
	 */
	public FormatElement getRequestHeadFormat() {
		return requestHeadFormat;
	}

	/**
	 * ע��������ͷ��ʽ��������
	 * 
	 * @param headFormat ������ͷ��ʽ������
	 * @emp:isChild com.ecc.emp.format.FormatElement
	 */
	public void setRequestHeadFormat( FormatElement headFormat ) {
		this.requestHeadFormat = headFormat;
	}

	/**
	 * �����Ӧ����ͷ��ʽ��������
	 * 
	 * @return ��Ӧ����ͷ��ʽ������
	 */
	public FormatElement getResponseHeadFormat() {
		return responseHeadFormat;
	}

	/**
	 * ע����Ӧ����ͷ��ʽ��������
	 * 
	 * @param responseHeadFormat ��Ӧ����ͷ��ʽ������
	 * @emp:isChild com.ecc.emp.format.FormatElement
	 */
	public void setResponseHeadFormat( FormatElement responseHeadFormat ) {
		this.responseHeadFormat = responseHeadFormat;
	}

	/**
	 * ��ñ���serviceId��������
	 * 
	 * @return ����serviceId��������
	 */
	public String getServiceIdField() {
		return serviceIdField;
	}

	/**
	 * ���ñ���serviceId��������
	 * 
	 * @param serviceIdField ����serviceId����������
	 * @emp:isAttribute true
	 * @emp:name ����serviceId��������
	 * @emp:desc �⿪����ͷ�󣬴����������еĸ���������ȡ��service id
	 * @emp:mustSet true
	 */
	public void setServiceIdField( String serviceIdField ) {
		this.serviceIdField = serviceIdField;
	}

	/**
	 * ��ñ���sessionId��������
	 * 
	 * @return ����sessionId��������
	 */
	public String getSessionIdField() {
		return sessionIdField;
	}

	/**
	 * ���ñ���sessionId��������
	 * 
	 * @param sessionIdField ����sessionId����������
	 * @emp:isAttribute true
	 * @emp:name ����sessionId��������
	 * @emp:desc �⿪����ͷ�󣬴����������еĸ���������ȡ��session id
	 * @emp:mustSet true
	 */
	public void setSessionIdField( String sessionIdField ) {
		this.sessionIdField = sessionIdField;
	}

	/**
	 * ����Ƿ���Ҫ��ȥ������ͷ��
	 * 
	 * @return �Ƿ���Ҫ��ȥ������ͷ
	 */
	public boolean isAppendReqHead() {
		return appendReqHead;
	}

	/**
	 * �����Ƿ���Ҫ��ȥ������ͷ��
	 * 
	 * @param appendHead �Ƿ���Ҫ��ȥ������ͷ����ѡ��true(Ĭ��)��false
	 * @emp:isAttribute true
	 * @emp:name �Ƿ���Ҫ��ȥ������ͷ
	 * @emp:desc ��Ϊtrue����������ͷ�⿪������ֻ�������������request����һ������
	 * @emp:defaultValue true
	 */
	public void setAppendReqHead( boolean appendHead ) {
		this.appendReqHead = appendHead;
	}

	/**
	 * ����Ƿ���Ҫ�ڷ��ر����и�������ͷ��
	 * 
	 * @return �Ƿ���Ҫ�ڷ��ر����и�������ͷ
	 */
	public boolean isAppendRepHead() {
		return appendRepHead;
	}

	/**
	 * �����Ƿ���Ҫ�ڷ��ر����и�������ͷ��
	 * 
	 * @param appendHead �Ƿ���Ҫ�ڷ��ر����и�������ͷ����ѡ��true(Ĭ��)��false
	 * @emp:isAttribute true
	 * @emp:name �Ƿ���Ҫ�ڷ��ر����и�������ͷ
	 * @emp:desc ��Ϊtrue������Ӧ���ݰ�����Ӧ����ͷ��ʽ�������׷������Ӧ������֮ǰ
	 * @emp:defaultValue true
	 */
	public void setAppendRepHead( boolean appendHead ) {
		this.appendRepHead = appendHead;
	}

	/**
	 * ���ServiceIdӳ�䡣
	 * 
	 * @return ServiceIdӳ��
	 */
	public Map getServiceIdMap() {
		return serviceIdMap;
	}

	/**
	 * ע��ServiceIdӳ�䡣
	 * 
	 * @param serviceIdMap ServiceIdӳ��
	 * @emp:isChild java.util.Map
	 */
	public void setServiceIdMap( Map serviceIdMap ) {
		this.serviceIdMap = serviceIdMap;
	}

	/**
	 * ��ñ���ͷ���롣
	 * 
	 * @return ����ͷ����
	 */
	public String getEncoding() {
		return encoding;
	}

	/**
	 * ���ñ���ͷ���롣
	 * 
	 * @param encoding ����ͷ����
	 */
	public void setEncoding( String encoding ) {
		this.encoding = encoding;
	}
}