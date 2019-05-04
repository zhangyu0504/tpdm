package core.communication.access.zlsecu;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;

import module.bean.LocalInfo;
import module.bean.SecCompData;
import module.cache.ParamCache;

import com.ecc.emp.core.Context;
import com.ecc.emp.core.EMPException;
import com.ecc.emp.data.InvalidArgumentException;
import com.ecc.emp.data.KeyedCollection;
import com.ecc.emp.data.ObjectNotFoundException;
import com.ecc.emp.flow.reversal.HostAccessAction;
import com.ecc.emp.format.FormatElement;
import com.ecc.emp.log.EMPLog;
import common.services.PublicService;
import common.util.BizUtil;
import common.util.DateUtil;
import common.util.SFConst;
import common.util.SFUtil;

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


	/**
	 * ��ʱʱ��
	 */
	private int timeOut = 40000;
	
	/**
	 * ���ļ����������������
	 */
//	private String identityField;
	
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
//	private String lengthHeadLen;
	/**
	 * MAC��ʶ
	 */
	private String mac;
	/**
	 * ȯ�̱��
	 */
	private String secCompCode;
	/**
	 * �������
	 */
	private String svrCode;
	
	
	/**
	 * �ͻ����
	 */
	private String userId;
	
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
	public String doExecute(Context context) throws EMPException {
		long beginTime=System.currentTimeMillis();
		SFLogger.info(context, "TCPIPSendActionִ�п�ʼʱ��:["+beginTime+"]");
		
		String hostIP = (String)getParamValue(context,"hostIP",this.hostIP,true);
		String hostPort = (String)getParamValue(context,"hostPort",this.hostPort,true);	
		
		String secCompCode = null , svrCode = null;
		
		secCompCode = getParamValue(context,"secCompCode",this.secCompCode,true).toString();
		svrCode = getParamValue(context,"svrCode",this.svrCode,true).toString();
		
//		if(mac.equals("1")){
//			bankNo = getParamValue(context,"bankNo",this.bankNo,true).toString();
//			svrCode = getParamValue(context,"svrCode",this.svrCode,true).toString();
//		}

		try {
			
			SFLogger.info(context, 
					"ͨѶ���ӷ��� [/"+InetAddress.getLocalHost().getHostAddress()+"<-->/"+hostIP+"]");

			BBCPCommClient client = null;
			byte[] repMsg = null;
			repMsg = getRequestMsg(context);
			
			
			//���ĵ�����
			PublicService pubService = ((PublicService) context.getService((String) context.getDataValue(SFConst.SERVICE_PUBLICSERVICENAME)));
			/*
			 * �ж��������ⷢ���Ƿ���ڵ���
			 */
			if(pubService.hasBaffle(context, "3", svrCode, secCompCode,timeOut)){
				String reqData = pubService.baffle(context, "3", svrCode, secCompCode,timeOut);
				if(SFUtil.isNotEmpty(reqData)){
					handleResponse(context,reqData.getBytes());
					return "0";
				}else {
					return "-1";
				}
			}
			
			client = new BBCPCommClient(hostIP,hostPort,timeOut,0);
			String ret = client.SendCMD(context,repMsg);
			if(!"0".equals(ret)){
				SFUtil.setDataValue(context,SFConst.PUBLIC_RET_FLAG, SFConst.RET_OVERTIME);//�ֹ����ó�ʱ
				
//				String outputName = svrCode + "_O";
//				FormatElement outputFormat = context.getFormat(outputName);
//				KeyedFormat tranCodeO = (KeyedFormat)outputFormat.getFormatElement();
//				tranCodeO.addFormatToContext(context);
//				
//				String outKcollName = tranCodeO.getKcollName();
//				SFUtil.setDataValue(context,outKcollName+".RESP_CODE", SFConst.RESPCODE_TIMEOUT_ZLSECU);
//				SFUtil.setDataValue(context,outKcollName+".RESP_MSG","ȯ����Ӧ����ʧ��");				
				throw new EMPException("YYPTERROR9999","TCPIPSendActionͨѶ����"+ret);	
			}
			byte[] toReciveMessage = null;
			toReciveMessage = client.getResult().getBytes();//����������ȫ������
			if(toReciveMessage == null || toReciveMessage.length == 0){
				SFLogger.info(context, "TCPIPSendActionͨѶ���󣺽��շ�������Ϊ�ա�������ʱ���ش��������س�ʱ4" );
				return "4";
			}
			handleResponse(context,toReciveMessage);
			
		} catch (Exception tempE) {
			EMPLog.log(SFConst.DEFAULT_TRXCODE, EMPLog.ERROR, 0, "ͨѶ�����쳣��"+tempE.getMessage(),  tempE );
			if("receiveTimeOut".equals(tempE.getMessage())){
				SFUtil.setDataValue(context,SFConst.PUBLIC_RET_FLAG, SFConst.RET_OVERTIME);//�ֹ����ó�ʱ
				
//				String outputName = svrCode + "_O";
//				FormatElement outputFormat = context.getFormat(outputName);
//				KeyedFormat tranCodeO = (KeyedFormat)outputFormat.getFormatElement();
//				String outKcollName = tranCodeO.getKcollName();
//				SFUtil.setDataValue(context,outKcollName+".RESP_CODE", SFConst.RESPCODE_TIMEOUT_ZLSECU);
//				SFUtil.setDataValue(context,outKcollName+".RESP_MSG","ȯ����Ӧ����ʧ��");
				return "4";						//��չ��������ؽ��ܳ�ʱ
			}
			return "3";
		}finally{
			long endTime=System.currentTimeMillis();
			SFLogger.info(context, "TCPIPSendActionִ�н���ʱ��:["+endTime+"],�ܺ�ʱ�� "+(endTime-beginTime)+"(ms)" );
		}
		
		return "0";
	}
	
	
	//������������
	private void handleResponse(Context context,byte[] responseMsg) throws Exception {
		KeyedCollection kColl = new KeyedCollection(AccessConstance.B_RESPONSE_HEAD);
		kColl.setAppend( true );
		FormatElement responseHeadFormat = context.getFormat(AccessConstance.B_RESPONSE_HEAD);
		if( responseHeadFormat.isBin() )
			responseHeadFormat.unFormat(responseMsg, kColl );
		else
			responseHeadFormat.unFormat(new String(responseMsg), kColl );
		
		if(kColl != null){
			//ȥ���ո�
		    for(int i=0;i<kColl.size();i++){
		    	String value = (String) kColl.getDataValue(kColl.getDataElement(i).getName());
		    	
		    	value = value.trim();
		    	int t = value.length();
		    	//char m = '.';
		    	while(t>0){
		    		char m = value.charAt(t-1);
		    		if(m=='.'){
		    			value = value.substring(0, t-1);
		    			value = value.trim();
		    			t = value.length();
		    		}else{
		    			break;
		    		}
		    	}
		    	
		    	kColl.setDataValue(kColl.getDataElement(i).getName(), value.trim());
		    }
		}

		if(context.containsKey(AccessConstance.B_RESPONSE_HEAD)){
			context.removeDataElement(AccessConstance.B_RESPONSE_HEAD);
		}
		context.addDataElement(kColl);

		
		
		KeyedFormat fixHeadFormat = (KeyedFormat)responseHeadFormat.getFormatElement();
		
		int headLen = fixHeadFormat.getHeadLen();
		
		
		byte[] buf = null;
		if( headLen > 0 )
		{
			int length = responseMsg.length - headLen;
			buf = new byte[length ];
			System.arraycopy(responseMsg, headLen, buf, 0, length);
		}
		
		
		byte[] responXmlMsg = buf;
		
		//����head��Ϣ
		FormatElement headFormat = context.getFormat(AccessConstance.ZLSECU_RESPONSE_HEAD);
		if (headFormat == null){
			throw new EMPException("The format:["+ AccessConstance.ZLSECU_RESPONSE_HEAD + "] not defined in context, please check the SFOnlineFormats.xml and settings.xml");
		}
		
		if( headFormat.isBin() )
			headFormat.unFormat(responXmlMsg, context );
		else
			headFormat.unFormat(new String(responXmlMsg), context );
		
		
		
		//����body��Ϣ
		FormatElement msgFormat = context.getFormat(this.svrCode + "_O");
		if (msgFormat == null){
			throw new EMPException("The format:["+ this.svrCode + "_O" + "] not defined in context, please check the SFOnlineFormats.xml and settings.xml");
		}
		
		//�������������
		KeyedFormat tranCodeO = (KeyedFormat)msgFormat.getFormatElement();
		tranCodeO.addFormatToContext(context);
		
	
		if( msgFormat.isBin() )
			msgFormat.unFormat(responXmlMsg, context );
		else
			msgFormat.unFormat(new String(responXmlMsg), context );
		
		
		//���ݱ���ͷ�ķ��������ж��Ƿ���Ҫ���뱨�������ݣ�ֻ�з���"000000"�ɹ�ʱ�ŷ��ر����壬�����漰��MACУ�����͵���Ϣ��
        if(!SFConst.RESPCODE_SUCCCODE.equals(SFUtil.getDataValue(context,this.svrCode + "_O"+".RESPCODE"))){
        	return;
        }
	}
	
	


	public byte[] getRequestMsg(Context context) throws EMPException, UnsupportedEncodingException {
		String encoding=this.encoding;
		initHeadKcoll(context);
        FormatElement rootHeadFormat = context.getFormat(AccessConstance.ZLSECU_REQUEST_HEAD);
		
		String responseHeadData = (String)rootHeadFormat.format(context);
        
		FormatElement msgFormat = context.getFormat(this.svrCode + "_I");
		
		String responseData = (String)msgFormat.format(context);
		
		
		String resData = "<?xml version=\"1.0\"?>\r\n<Root>\r\n"+responseHeadData+"\r\n"+responseData+"\r\n</Root>";//String.valueOf(len)+
		resData=resData.replaceAll("\r\n", "");
		int len = resData.getBytes().length;
		KeyedCollection headColl = SFUtil.getDataElement(context,AccessConstance.B_REQUEST_HEAD);
		headColl.setDataValue("MSGDATALEN", String.valueOf(len));
		FormatElement headFormat = context.getFormat(AccessConstance.B_REQUEST_HEAD);
		String headData = (String)headFormat.format(headColl);
		
		resData = headData+resData;
		/*
		 * ����ͬȯ���ش�����Ӧ���ı����ʽ
		 */
		SecCompData secCompData = (SecCompData)context.getDataValue(SFConst.PUBLIC_SECU);
		String secCompCode = null;
		if(secCompData!=null){
			 secCompCode = secCompData.getSecCompCode();
			//�ӻ����л�ȡ�ӿڱ���
			String secEncoding=ParamCache.getValue2("SEC_ZL_CHARSET", secCompCode+"_"+this.svrCode);
			if(SFUtil.isNotEmpty(secEncoding)){
				encoding=secEncoding;
			}else{
				secEncoding=ParamCache.getValue2("SEC_ZL_CHARSET", secCompCode);
				if(SFUtil.isNotEmpty(secEncoding)){
					encoding=secEncoding;
				}
			}
		}
		
		
		SFLogger.info(context,"ֱ��ȯ�������ı����ʽ��["+encoding+"]");
		if (encoding != null)
			return resData.getBytes(encoding);
		else
			return resData.getBytes();
	
	
		
	}
	
	
	
	public void initHeadKcoll(Context context) throws EMPException {
		LocalInfo localInfo = SFUtil.getDataValue(context,SFConst.PUBLIC_LOCAL_INFO);//Ӫҵʱ����Ϣ
		String txDate = localInfo.getWorkdate();//Ӫҵ����
		KeyedCollection headColl = null;
		if(context.containsKey(AccessConstance.B_REQUEST_HEAD)){
			headColl = SFUtil.getDataElement(context,AccessConstance.B_REQUEST_HEAD);
			headColl.setDataValue("VERSION", "01");
			headColl.setDataValue("PACKTYPE", "");
			headColl.setDataValue("TXCODE", this.svrCode);
			headColl.setDataValue("FUNCCODE", "000");
			headColl.setDataValue("COMMMODE", "M");
			headColl.setDataValue("TOTALBLOCK", "");
			headColl.setDataValue("CURBLOCK", "");
			headColl.setDataValue("MSGDATALEN", "");
			headColl.setDataValue("MSGEXTLEN", "");
			headColl.setDataValue("ENCRYPTMODE", "N");
			headColl.setDataValue("NOUSE", "");
		}else{
			headColl = new KeyedCollection(AccessConstance.B_REQUEST_HEAD);
			headColl.addDataField("VERSION", "01");
			headColl.addDataField("PACKTYPE", "");
			headColl.addDataField("TXCODE", this.svrCode);
			headColl.addDataField("FUNCCODE", "000");
			headColl.addDataField("COMMMODE", "M");
			headColl.addDataField("TOTALBLOCK", "");
			headColl.addDataField("CURBLOCK", "");
			headColl.addDataField("MSGDATALEN", "");
			headColl.addDataField("MSGEXTLEN", "");
			headColl.addDataField("ENCRYPTMODE", "N");
			headColl.addDataField("NOUSE", "");
			context.addDataElement(headColl);
		}
		KeyedCollection rootHeadColl = null;
		if(context.containsKey(AccessConstance.ZLSECU_REQUEST_HEAD)){
			rootHeadColl = SFUtil.getDataElement(context,AccessConstance.ZLSECU_REQUEST_HEAD);
			rootHeadColl.setDataValue("VERSION", "01");
			rootHeadColl.setDataValue("TXCODE", this.svrCode);
			rootHeadColl.setDataValue("FUNCCODE", "000");
			//����Ϊ��ת֤(6032)ʱ��0002��������0005
			if(this.svrCode.equals( SFConst.SF_TX_CODE_BANK_B2S )){
				handleKcollValue(rootHeadColl,"CHANNEL", "0002");	
			}else{
				handleKcollValue(rootHeadColl,"CHANNEL", "0005");
			}
			rootHeadColl.setDataValue("SUBCENTERID", "0110");
			rootHeadColl.setDataValue("NODEID", this.userId);
			rootHeadColl.setDataValue("TELLERID", "000000");
			String txSeqId = (String) rootHeadColl.getDataValue("TXSEQID");
			if(SFUtil.isEmpty(txSeqId)){
				txSeqId = SFUtil.getDataValue(context, SFConst.PUBLIC_MSG_SEQ_NO);
				if(SFUtil.isEmpty(txSeqId)){
					txSeqId = BizUtil.getTxSeqId(SFConst.SEQ_ID_LEN_8,BizUtil.getInitSeqId(context));
				}
				
			}
			
			rootHeadColl.setDataValue("TXSEQID", txSeqId);
			rootHeadColl.setDataValue("TXDATE", txDate);
			rootHeadColl.setDataValue("TXTIME", DateUtil.getMacTime());
			rootHeadColl.setDataValue("USERID", this.userId);
			
		}else{
			rootHeadColl = new KeyedCollection(AccessConstance.ZLSECU_REQUEST_HEAD);
			rootHeadColl.addDataField("VERSION", "01");
			rootHeadColl.addDataField("TXCODE", this.svrCode);
			rootHeadColl.addDataField("FUNCCODE", "000");
			//����Ϊ��ת֤(6032)ʱ��0002��������0005
			if(this.svrCode.equals( SFConst.SF_TX_CODE_BANK_B2S )){
				rootHeadColl.addDataField("CHANNEL", "0002");
			}else{
				rootHeadColl.addDataField("CHANNEL", "0005");
			}
			rootHeadColl.addDataField("SUBCENTERID", "0110");
			rootHeadColl.addDataField("NODEID", this.userId);
			rootHeadColl.addDataField("TELLERID", "000000");
			
			
			String txSeqId = SFUtil.getDataValue(context, SFConst.PUBLIC_MSG_SEQ_NO);
			if(SFUtil.isEmpty(txSeqId)){
				txSeqId = BizUtil.getTxSeqId(SFConst.SEQ_ID_LEN_8,BizUtil.getInitSeqId(context));
			}
			
			rootHeadColl.addDataField("TXSEQID", txSeqId);
			rootHeadColl.addDataField("TXDATE", txDate);
			rootHeadColl.addDataField("TXTIME", DateUtil.getMacTime());
			rootHeadColl.addDataField("USERID", this.userId);
			context.addDataElement(rootHeadColl);
		}
	}

	
	public void handleKcollValue(KeyedCollection kcoll,String key,String value){
		
		String oldValue = null;
		try {
			oldValue = (String) kcoll.getDataValue(key);
			if(SFUtil.isEmpty(oldValue)){
				kcoll.setDataValue(key, value);
			}
		} catch (ObjectNotFoundException e) {
			e.printStackTrace();
		} catch (InvalidArgumentException e) {
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
	/*private byte[] getMessageByMac(byte[] message,String bankNo,String svrCode) throws EMPException{
		String tmpMsg = new String(message);
		String mac = MacUtil.genMac(StringTool.FillString(tmpMsg, ' ', 1, message.length+8).getBytes(), getMacKey(bankNo, svrCode));
		
		return (tmpMsg+mac).getBytes();	
	}*/
	
	/*private String getMacKey(String bankNo,String svrCode)  throws EMPException{
		BankInfo bankInfo = SysParamCache.getSysCPIPMap().get(bankNo+svrCode);

		if(bankInfo == null){
			EMPLog.log(SFConst.SF_SYS_TRXCODE, EMPLog.ERROR, 0,
					"BBCPTCPIPDynamicAccessByMacAction:������δ���ã�ȡ�����з����Ӧ��������ϢΪ�գ�bankno=["+bankNo+"],svrcode=["+svrCode+"]");
			throw new EMPException("999999","ȡ�����з����Ӧ��������ϢΪ�գ�bankno=["+bankNo+"],svrcode=["+svrCode+"]");			
		}
		
		return bankInfo.getMacKey();
	}*/
	



	/**
	 * ����ͨѶ��ʱʱ�䡣
	 * 
	 * @param timeOut ͨѶ��ʱʱ��
	 * @emp:isAttribute true
	 * @emp:name ��ʱʱ��
	 * @emp:desc ͨѶ��ʱʱ��
	 */
	public void setTimeOut(int timeOut) {
		this.timeOut = timeOut;
	}

	/**
	 * �����ַ����롣
	 * 
	 * @param encoding �ַ�����
	 * @emp:isAttribute true
	 * @emp:name �ַ�����
	 * @emp:desc ����ת��Ϊ�ֽ�����ʹ�õ��ַ�����
	 */
	public void setEncoding(String encoding) {
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
//	public void setIdentityField(String identityField) {
//		this.identityField = identityField;
//	}

	public void setHostIP(String hostIP) {
		this.hostIP = hostIP;
	}

	public void setHostPort(String hostPort) {
		this.hostPort = hostPort;
	}
	
	/**
	 * @param mac the mac to set
	 */
	public void setMac(String mac) {
		this.mac = mac;
	}


	/**
	 * @param svrCode the svrCode to set
	 */
	public void setSvrCode(String svrCode) {
		this.svrCode = svrCode;
	}


	public void setSecCompCode(String secCompCode) {
		this.secCompCode = secCompCode;
	}



	public void setUserId(String userId) {
		this.userId = userId;
	}
	
	
	
	
	
}