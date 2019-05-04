package core.communication.access.stzsecu;

import java.util.Date;
import java.util.Map;

import module.bean.LocalInfo;
import module.bean.Param;
import module.cache.ParamCache;

import com.ecc.emp.core.Context;
import com.ecc.emp.core.EMPException;
import com.ecc.emp.data.KeyedCollection;
import com.ecc.emp.flow.reversal.HostAccessAction;
import com.ecc.emp.format.FormatElement;
import com.sscc.fdep.mrapi;
import common.exception.SFException;
import common.services.PublicService;
import common.util.BizUtil;
import common.util.DateUtil;
import common.util.SFConst;
import common.util.SFUtil;

import core.cache.CacheMap;
import core.communication.format.xml.XMLWrapFormat;
import core.communication.util.AccessConstance;
import core.log.SFLogger;
/**
 * ��֤ͨ���ͱ���
 * @author ����
 *
 */
public class SZTSendAction extends HostAccessAction{
	/**
	 * ��ʱʱ��
	 */
	private int timeOut = 40000;
	
	/**
	 * �ַ�����
	 */
	private String encoding;
	
	/**
	 * �������
	 */
	private String svrCode;
	
	/**
	 * ȯ�̱��
	 */
	private String userId;
	

	/**
	 * ȯ�̱��
	 */
	private String secCompCode;
	
	public SZTSendAction() {
		super();
	}
	
	
	private Map<String,String> tcpipService = null;
	
	@Override
	public String doExecute(Context context) throws EMPException {
		/*
		 * ��ʼ��:������֤ͨ������
		 */
    	//SZTInitializer.initialize();
		
    	initRequestHeadMsg(context);
    	/*
		 * �������Ĵ����������
		 */
		String reqMsg = getRequestMsg(context);
		/*
		 * ���ͱ���
		 */
		String pkgId=this.send(context,reqMsg);
		/*
		 * ��Ӧ���Ľ�ѹ��������
		 */
		if(!"0".equals(pkgId)){
			if(SFUtil.isNotEmpty(pkgId)){
				handleResponse(context,pkgId);			
			}else{
				return "-1";
			}
		}
		
		return "0";
	}
	
	
	/**
	 * �����֤ͨ����
	 * @param context
	 * @return
	 * @throws EMPException
	 */
	public void initRequestHeadMsg(Context context) throws EMPException {
		
		
		
		if(!context.containsKey("MsgHdr")){
			SFUtil.addFormatToContext(context, "MsgHdr");
		}
		
//		FormatElement msgFormat = context.getFormat("MsgHdr");
//		// ��������ֶε�������
//		XMLWrapFormat tranCodeO = (XMLWrapFormat) msgFormat
//				.getFormatElement();
//		try {
//			tranCodeO.addFormatToContext(context);
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		
		
		KeyedCollection kColl= SFUtil.getDataElement(context, "MsgHdr");
		
		SFUtil.setDataValue(context, kColl,"Ver","1.0");
		
		
		String sType = secCompCode.substring(secCompCode.length()-4);
		//ȡ��Ӧϵͳ����
		if("9999".equals(sType)){
			SFUtil.setDataValue(context, kColl,"SysType","3");
		}else{
			SFUtil.setDataValue(context, kColl,"SysType","0");
		}
		
		SFUtil.setDataValue(context, kColl,"InstrCd",this.svrCode);
		SFUtil.setDataValue(context, kColl,"TradSrc","B");
		
		LocalInfo localInfo = SFUtil.getDataValue(context,SFConst.PUBLIC_LOCAL_INFO);//Ӫҵʱ����Ϣ
		String txDate = localInfo.getWorkdate();//Ӫҵ����
		
		SFUtil.setDataValue(context, kColl,"Date",txDate); //DateUtil.getMacDate()
		SFUtil.setDataValue(context, kColl,"Time",DateUtil.getMacTime());
		SFUtil.setDataValue(context, kColl,"LstFrag","Y");
		
		KeyedCollection senderKcoll = SFUtil.getDataElement(context,kColl, "Sender"); 
		
		SFUtil.setDataValue(context, senderKcoll, "InstType","B");
		SFUtil.setDataValue(context, senderKcoll, "InstId","2");
		
		KeyedCollection recverKcoll = SFUtil.getDataElement(context,kColl, "Recver"); 
		SFUtil.setDataValue(context, recverKcoll, "InstType","S");
		SFUtil.setDataValue(context, recverKcoll, "InstId",secCompCode);
		String secBrchId = SFUtil.getDataValue( context, "SEC_BRCH_ID" );
		if(SFUtil.isNotEmpty( secBrchId )){
			SFUtil.setDataValue( context, recverKcoll,"BrchId", secBrchId );
		}
		KeyedCollection refKcoll = SFUtil.getDataElement(context,kColl, "Ref"); 
		
		String ref = SFUtil.getDataValue(context,refKcoll, "Ref");
		
		if(SFUtil.isEmpty(ref)){
//			ref = BizUtil.getTxSeqId(SFConst.SEQ_ID_LEN_8,BizUtil.getInitSeqId(context));
			ref = SFUtil.getDataValue(context, SFConst.PUBLIC_MSG_SEQ_NO);
			if(SFUtil.isEmpty(ref)){
				ref =  BizUtil.getInitSeqId(context);
			}
		}		
		
		SFUtil.setDataValue(context, refKcoll, "Ref",ref);
		SFUtil.setDataValue(context, refKcoll, "IssrType","B");
		
		
	}
	
	
	/**
	 * �����֤ͨ����
	 * @param context
	 * @return
	 * @throws EMPException
	 */
	public String getRequestMsg(Context context) throws EMPException {
        FormatElement headFormat = context.getFormat("MsgHdr");		
		String responseHeadData = (String)headFormat.format(context);
		
		
		String serverId = SFUtil.getDataValue(context,"MsgHdr.InstrCd");
		Map<String,Map<String,String>> tcpipServiceMap =  CacheMap.getCache("SZT_TCPIPSERVICE");
		tcpipService = tcpipServiceMap.get(serverId);
		
		
		
		String bodyFormat  = tcpipService.get("formatInput");// ReadProperty.getValue(this.svrCode + "_I");        
		FormatElement msgFormat = context.getFormat(bodyFormat);		
		String responseData = (String)msgFormat.format(context);
		
		if(SFUtil.isEmpty(responseData)){
			throw new SFException("������֤ͨ����Ϊ�գ�����context��");
		}
		responseData = responseData.trim();
		
		/*
		 * ������
		 */		
		int pos = responseData.indexOf(bodyFormat);
		
		String resp1 = responseData.substring(pos-1,bodyFormat.length()+2);
		
		String resp2 = responseData.substring(bodyFormat.length()+3);
		
		String responseBodyData = resp1+"\r\n"+responseHeadData+resp2;
		
		
		/*
		 * ��װ���ͱ���		
		 */
		StringBuffer sendData=new StringBuffer();
		int msgLength=responseBodyData.getBytes().length;//�����峤��
		msgLength=msgLength+105;//�������93���ַ�����ͷ����
		String strMsgLength=SFUtil.fixChar(String.valueOf(msgLength), 5, '0', "left");
		
		sendData.append("<IFTS Len=\""+strMsgLength+"\" DataVer=\"1.0.0.1\" SeqNo=\"0\" Type=\"B\" Dup=\"N\" CheckSum=\"\"><MsgText>\r\n");	
		sendData.append(responseBodyData);
		sendData.append("\r\n</MsgText></IFTS>");		
		return sendData.toString();
		
	}
	/**
	 * �������ͱ���
	 * @param sendData
	 * @return
	 * @throws EMPException
	 */
	private String send(Context context,String sendData) throws EMPException{
		SFLogger.info(context,"������֤ͨ����=["+sendData+"]");
		Param param = ParamCache.getValue(AccessConstance.SZT_TIMEOUT, "SZT");
		int timeout=Integer.parseInt(param.getValue());
		//������
		PublicService pubService = ((PublicService) context.getService((String) context.getDataValue(SFConst.SERVICE_PUBLICSERVICENAME)));
		/*
		 * �ж��������ⷢ���Ƿ���ڵ���
		 */
		if(pubService.hasBaffle(context, "4", svrCode,secCompCode,timeout)){
			String reqData = pubService.baffle(context, "4", svrCode,secCompCode,timeout);
			if(SFUtil.isNotEmpty(reqData)){
				//������������
		    	SZTResponseHandler handler = new SZTResponseHandler();
		    	handler.handleRequest(context,reqData.getBytes(),this.svrCode);
				return "0";
			}else{
				throw new EMPException( "YYPTERROR9999", "SZTSendActionͨѶ���󣺷��ر���Ϊ�գ�");
			}
		}
		
		/*
		 * ��ȡ��֤ͨ����
		 */
		SZTAccessParam sztParam=CacheMap.getCache("SZT_ACCESS_PARAM");
		String sourceAppId = sztParam.getClientDestAppId();
		String sourceUserId =sztParam.getClientDestUserId();

		
		/*
		 * ��ȡȯ�̼���֤ͨ����
		 */
		String destAppId=ParamCache.getValue2("SEC_APP_ID",secCompCode);
		String destUerId=ParamCache.getValue2("SEC_USER_ID",secCompCode);

		
		byte[] sztMessage = sendData.getBytes();
		//������Ϣ���õ���Ϣ����ʶpkgid
		String pkgId = mrapi.Mr2Send(sztMessage, sourceUserId, sourceAppId, destUerId, SFUtil.isEmpty(destAppId)?"app1":destAppId,"", "", "", "", "", (byte)0, (byte)0, (byte)0, (byte)0, 2000);
		//���pkgid�ַ���Ϊ�գ���������ʧ�ܡ�
		if(SFUtil.isEmpty(pkgId))
		{
			SFLogger.info(context,"��������ʧ�ܣ�������֤ͨ Mr2Send����ʧ�ܣ�");
			throw new EMPException("��������ʧ�ܣ�������֤ͨ Mr2Send����ʧ�ܣ�");
		}
//		mrapi.MrDestroy("app2");
		return pkgId;
	}
	/**
	 * ����Ӧ����
	 * @param sendData
	 * @param pkgId
	 * @return
	 * @throws EMPException
	 */
	private void handleResponse(Context context,String pkgId) throws EMPException {
		Param param = ParamCache.getValue(AccessConstance.SZT_TIMEOUT, "SZT");
		int timeout=Integer.parseInt(param.getValue());
		long start=(new Date()).getTime();
		while(true){
			long next=(new Date()).getTime();
			if(next-start>timeout){
				SFLogger.info(context,"��֤ͨӦ���ĳ�ʱ!");
				
				String outputFormat = tcpipService.get("formatOutput");// ReadProperty.getValue(msgCode+ "_O");		
				
				FormatElement msgFormat = context.getFormat(outputFormat);
				if (msgFormat == null) {
					throw new EMPException("The format:["+ outputFormat+ "] not defined in context, please check the SFOnlineFormats.xml and settings.xml");
				}
				// ��������ֶε�������
				XMLWrapFormat tranCodeO = (XMLWrapFormat) msgFormat.getFormatElement();
				try {
					tranCodeO.addFormatToContext(context);
					String outKcollName = outputFormat.replaceAll("\\.", "");
					KeyedCollection outKcoll = SFUtil.getDataElement(context,outKcollName);		
					KeyedCollection rstKcoll = SFUtil.getDataElement(context,outKcoll, "Rst");
					SFUtil.setDataValue(context,rstKcoll,"RESP_CODE",SFConst.RESPCODE_TIMEOUT_SZTSECU);
					SFUtil.setDataValue(context,rstKcoll,"RESP_MSG","��֤ͨӦ���ĳ�ʱ");
				} catch (Exception e) {
					SFLogger.error(context, "��֤ͨӦ����ʧ��:",e);
				}
				throw new EMPException("��֤ͨӦ���ĳ�ʱ");
			}
			
			SZTAccessParam sztParam = CacheMap.getCache("SZT_ACCESS_PARAM");
			
			String clientAppId =  sztParam.getClientAppId();//
			SFLogger.info(context,"SZTSendAction appId=["+clientAppId+"],Mr2CreatePkgID=[" + pkgId + "]");		
			// ������Ϣ���õ���Ϣ����ʶpkgid
			byte[] rspData = mrapi.Mr2Receive1(clientAppId, "", "", "", "", "",pkgId, "", "", 2000);
			String result = new String(rspData);	
	        if(result.length()>9)
	        {
	        	SFLogger.info(context, "��֤ͨӦ����:[" + result+"]^");
				//������������
		    	SZTResponseHandler handler = new SZTResponseHandler();
		    	handler.handleRequest(context,rspData,this.svrCode);
		    	break;  	
	        }else{
		        try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					SFLogger.error(context, "��֤ͨ�ȴ�ʧ��:",e);
				}
	        }
		}
	}

	public int getTimeOut() {
		return timeOut;
	}

	public void setTimeOut(int timeOut) {
		this.timeOut = timeOut;
	}

	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public String getSvrCode() {
		return svrCode;
	}

	public void setSvrCode(String svrCode) {
		this.svrCode = svrCode;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getSecCompCode() {
		return secCompCode;
	}

	public void setSecCompCode(String secCompCode) {
		this.secCompCode = secCompCode;
	}

	
}