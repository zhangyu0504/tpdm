package core.communication.access.esb;

import java.sql.Connection;
import java.util.Iterator;

import module.trans.TranHandler;

import com.dc.eai.data.CompositeData;
import com.dcfs.esb.client.converter.PackUtil;
import com.ecc.emp.access.tcpip.EMPTCPIPRequest;
import com.ecc.emp.access.tcpip.EMPTCPIPRequestService;
import com.ecc.emp.core.Context;
import com.ecc.emp.core.EMPConstance;
import com.ecc.emp.core.EMPException;
import com.ecc.emp.format.FormatElement;
import common.exception.SFException;
import common.sql.dao.DBHandler;
import common.util.BizUtil;
import common.util.SFConst;
import common.util.SFUtil;

import core.communication.format.PBankFormatElement;
import core.communication.format.paesb.ESBCDFormat;
import core.log.SFLogger;

/**
 * EMP�ṩ��TCP/IP����������ʷ������һ��ʵ�֡�
 * <p>
 * �ɽ�EMPҵ���߼���������Ϊ��TCP/IP����������ʵķ���
 * ʹ��ͨ��TCP/IP���������ϵͳ����ͨ���������������ĵķ�ʽ����EMP��ҵ�����߼���
 * ��<tt>EMPTCPIPRequestService</tt>��ͬ���ǣ�����ʹ�ñ��ĸ�ʽ��������
 * ���ǹ̶�����EMP����ģ�����û�������<tt>DataElementSerializer</tt>�����ݽ���XML��ʽ��������
 * �ø�ʽ��EMP���ݶ���һ�¡�
 * <p>
 * ������һ���������������TcpipAccessServletContext�����ļ��У��������£�
 * <p>
 * id��HTTP Service��id��ͨ��ָ����id��Service���з���<br>
 * serviceName��id�ĵȼ۲���<br>
 * serviceType��Service�������ͣ���ѡ����session(�����Ự)��endSession(�����Ự)�Լ�����(Ĭ�ϣ���ͨ����)<br>
 * sessionContextName����serviceTypeΪsessionʱ��Ч��ָ��Ҫ�����Ự��ҵ���߼�Context����<br>
 * checkSession���������Ƿ���Ự����ѡ��true(Ĭ��)��false<br>
 * EMPFlowId����Service��Ӧ��EMPҵ���߼�������<br>
 * opId����Service��Ӧ��EMPҵ���߼������е�Operation ID<br>
 * description��������Ϣ<br>
 * encoding�����������<br>
 * enabled����Service�Ƿ����ã���ѡ��true(Ĭ��)��false<br>
 * 
 * @author zhongmc
 * @version 2.1
 * @since 1.0 2006-12-26
 * @lastmodified 2008-6-11
 * @emp:name TCP/IP����������ʷ���(DS)
 * @emp:document EMP�ṩ��TCP/IP����������ʷ��񣬿ɷ���EMPҵ���߼�����
 */
public class PBankTCPIPDataSerializerService extends EMPTCPIPRequestService {

	/**
	 * ��Service��Ӧ��TranCode
	 */
	protected String tranCode = null;
	
	
	protected String className = null;

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getTranCode() {
		return tranCode;
	}

	public void setTranCode(String tranCode) {
		this.tranCode = tranCode;
	}

	/**
	 * �������Ƿ��ȷ���Ӧ��Ȼ��ִ��ҵ���߼�
	 */
	protected boolean responseFirst = false;

	/**
	 * ��ø������Ƿ��ȷ���Ӧ��
	 * 
	 * @return �������Ƿ��ȷ���Ӧ��
	 */
	public boolean isResponseFirst() {
		return responseFirst;
	}

	/**
	 * ���ø������Ƿ��ȷ���Ӧ��
	 * 
	 * @param responseFirst �������Ƿ��ȷ���Ӧ�𣬿�ѡ��true(Ĭ��)��false
	 * @emp:isAttribute true
	 * @emp:name �Ƿ��ȷ���Ӧ��
	 * @emp:desc �������Ƿ��ȷ���Ӧ��
	 * @emp:defaultValue false
	 */
	public void setResponseFirst(boolean responseFirst) {
		this.responseFirst = responseFirst;
	}

	/**
	 * ��ESB�Ƿ�дESB_INFO��Ϣ��
	 */
	protected boolean isLog = false;

	public boolean getIsLog() {
		return isLog;
	}

	public void setIsLog(boolean isLog) {
		this.isLog = isLog;
	}

	/**
	 * �ýӿ����������������KeyedCollection��
	 */
	protected String inOutDataArea = null;

	/**
	 * ע��ýӿ������������������(KeyedCollection)�����ơ�
	 * 
	 * @param inOutDataArea�����������������(KeyedCollection)������
	 */
	public void setInOutDataArea(String inOutDataArea)
	{
		this.inOutDataArea = inOutDataArea;
	}

	/**
	 * ��ȡ�ýӿ������������������(KeyedCollection)�����ơ�
	 * 
	 */
	public String getInOutDataArea()
	{
		return this.inOutDataArea;
	}

	/**
	 * ��Service��Ӧ��EMPҵ���߼�������
	 */
	protected String errBizId = null;

	/**
	 * ��Service��Ӧ��EMPҵ���߼������е�Operation ID
	 */
	protected String errOpId = null;

	/**
	 * ��ø�Service��Ӧ���쳣������ҵ���߼���������
	 * 
	 * @return �쳣������ҵ���߼�������
	 */
	public String getErrBizId() {
		return errBizId;
	}
	
	/**
	 * ��ø�Service��Ӧ��EMPҵ���߼�op id��
	 * 
	 * @return ҵ���߼�op id
	 */
	public String getErrOpId() {
		return errOpId;
	}

	/**
	 * ��ESB���������ļ�httpchannel.xml�����������ԣ�
	 * 1����Ҫ���������أ�����������Ϊ��mutexKey�����عؼ��֣�mutexFlag:0���ף�1����ѯ
	 * 2������������ù����Զ�д��ESB_MUTEX_CONTROL����
	 *	A��mutexFlag=0�����߼���
	 *	����ʱֱ�Ӳ��뻥��������ڸñ����ڻ��⣬�������ݲ���Ͳ�ѯ������ͬһ�������»�����ȴ���Ϊ�˽��׳�ʱ֮�󣬲�ѯ�ܹ����췵�أ������ѯ�ֲ����ȴ���ʱ�����׵Ĳ��뾡���ŵ�����������β������֤����֮��ͽ��ܿ��ύҵ���ˣ���������ҵ������һ���ύ��
	 *	B��mutexFlag=1�����߼���
	 *	B1������һ����ѯ��¼�����������������ظ������ش��ڼ�¼���������ɹ�������������
	 */
	protected String mutexKey = null;

	/**
	 * ���عؼ��֡�
	 * 
	 * @return ���عؼ���
	 */
	public String getMutexKey() {
		return mutexKey;
	}
	
	/**
	 * ���عؼ��֡�
	 * 
	 * @return 
	 */
	public void setMutexKey(String mutexKey) {
		this.mutexKey = mutexKey;
	}
	/**
	 * mutexFlag:0���ף�1����ѯ
	 */
	protected String mutexFlag = null;

	/**
	 * mutexFlag:0���ף�1����ѯ
	 * 
	 * @return mutexFlag:0���ף�1����ѯ
	 */
	public String geMutexFlag() {
		return mutexFlag;
	}
	
	/**
	 * mutexFlag:0���ף�1����ѯ��
	 * 
	 * @return 
	 */
	public void setMutexFlag(String mutexFlag) {
		this.mutexFlag = mutexFlag;
	}

	/**
	 * ���ø�Service��Ӧ��EMPҵ���߼�op id��
	 * 
	 * @param opId ҵ���߼�op id
	 * @emp:isAttribute true
	 * @emp:name ҵ���߼�op id
	 * @emp:desc Ҫ����ΪHTTP Service��ҵ���߼������е�Operation id
	 * @emp:mustSet true
	 */
	public void setErrOpId(String errOpId) {
		this.errOpId = errOpId;
	}

	/**
	 * ���ø�Service��Ӧ���쳣������EMPҵ���߼���������
	 * 
	 * @param bizId �쳣����ҵ���߼�������
	 * @emp:isAttribute true
	 * @emp:name �쳣����ҵ���߼�������
	 * @emp:desc Ҫ����ΪHTTP Service��ҵ���߼�����id
	 * @emp:mustSet true
	 */
	public void setErrBizId(String errBizId) {
		this.errBizId = errBizId;
	}


	/**
	 * ��Service��Ӧ���ȷ���Ӧ��EMPҵ���߼�������
	 */
	protected String finalBizId = null;

	/**
	 * ��Service��Ӧ���ȷ���Ӧ��EMPҵ���߼������е�Operation ID
	 */
	protected String finalOpId = null;

	/**
	 * ��ø�Service��Ӧ���ȷ���Ӧ������ҵ���߼���������
	 * 
	 * @return �ȷ���Ӧ���ҵ���߼�������
	 */
	public String getFinalBizId() {
		return finalBizId;
	}
	
	/**
	 * ��ø�Service��Ӧ���ȷ���Ӧ��EMPҵ���߼�op id��
	 * 
	 * @return �ȷ���Ӧ��ҵ���߼�op id
	 */
	public String getFinalOpId() {
		return finalOpId;
	}

	/**
	 * ���ø�Service��Ӧ���ȷ���Ӧ��֮���EMPҵ���߼�op id��
	 * 
	 * @param finalOpId �ȷ���Ӧ��ҵ���߼�֮���op id
	 * @emp:isAttribute true
	 * @emp:name �ȷ���Ӧ��ҵ���߼�op id
	 * @emp:desc Ҫ����ΪHTTP Service���ȷ���Ӧ��ҵ���߼������е�Operation id
	 * @emp:mustSet true
	 */
	public void setFinalOpId(String finalOpId) {
		this.finalOpId = finalOpId;
	}

	/**
	 * ���ø�Service��Ӧ���ȷ���Ӧ������EMPҵ���߼���������
	 * 
	 * @param finalBizId �ȷ���Ӧ����ҵ���߼�������
	 * @emp:isAttribute true
	 * @emp:name �ȷ���Ӧ����ҵ���߼�������
	 * @emp:desc Ҫ����ΪHTTP Service��ҵ���߼�����id
	 * @emp:mustSet true
	 */
	public void setFinalBizId(String finalBizId) {
		this.finalBizId = finalBizId;
	}
	
	public PBankTCPIPDataSerializerService() {
		super();
		this.setCheckSession(false);//�����session
	}

	
	
	
	/**
	 * �����������ʵ�ַ�����
	 * 
	 * @param request HTTP����
	 * @param response HTTP��Ӧ
	 * @param sessionContext Session Context
	 * @param sessionId SessionId
	 * @return �������ݣ�������õı�����
	 * @throws EMPException
	 */
	
	@Override
	public byte[] handleRequest(EMPTCPIPRequest request, Context sessionContext)
			throws EMPException {
		Connection connection =null;
		Context flowContext = null;
		try {
			
			flowContext = SFUtil.getRootContext();
			
			if (flowContext.getParent() == null && sessionContext != null)
				flowContext.chainedTo(sessionContext);
			
			/**
			 * ���ù���������ֵ
			 */
			connection = DBHandler.getConnection(flowContext);
			SFUtil.setDataValue(flowContext, SFConst.PUBLIC_TRAN_CONNECTION, connection);
			
			//���ɶ���ID��ϵͳ��ʼ��ˮ��16λ��
			String logId=BizUtil.getInitSeqId(flowContext);
			SFUtil.setDataValue(flowContext, SFConst.PUBLIC_LOG_ID, logId);
			//���׺�
			SFUtil.setDataValue(flowContext, SFConst.PUBLIC_TX_CODE, tranCode);
			
			//������Ӧ���ı�ʶ
			SFUtil.setDataValue(flowContext, SFConst.PUBLIC_IS_RET_RESP, SFConst.RET_RESP_YES);
			SFUtil.setDataValue(flowContext, SFConst.PUBLIC_SERVER_ID, this.serviceName);
			SFLogger.info(flowContext, "ESB������: [" + serviceName + "][" + PackUtil.packXmlStr((CompositeData)request.getAttribute("reqData")) + "]");
			
			request.setAttribute(EMPConstance.ATTR_CONTEXT, flowContext);
			//EMPTransactionManager transactionManager = null;
			try {
				//transactionManager = (EMPTransactionManager) flowContext.getService(EMPConstance.TRX_SVC_NAME);
				DBHandler.beginTransaction(flowContext, connection);
				//���õ���class
				flowContext.addDataField("CLASS_NAME", className);
				SFUtil.setDataValue(flowContext, SFConst.PUBLIC_TX_CODE, tranCode);
				updateModel(request,flowContext);
				//˽�д����߼�
				TranHandler.doHandle(flowContext);
				
				/**
				 * ��ESB���������ļ�httpchannel.xml�����������ԣ�
				 * 1����Ҫ���������أ�����������Ϊ��mutexKey�����عؼ��֣�mutexFlag:0���ף�1����ѯ
				 * 2������������ù����Զ�д��ESB_MUTEX_CONTROL����
				 *	A��mutexFlag=0�����߼���
				 *	����ʱֱ�Ӳ��뻥��������ڸñ����ڻ��⣬�������ݲ���Ͳ�ѯ������ͬһ�������»�����ȴ���Ϊ�˽��׳�ʱ֮�󣬲�ѯ�ܹ����췵�أ������ѯ�ֲ����ȴ���ʱ�����׵Ĳ��뾡���ŵ�����������β������֤����֮��ͽ��ܿ��ύҵ���ˣ���������ҵ������һ���ύ��
				 *	B��mutexFlag=1�����߼���
				 *	B1������һ����ѯ��¼�����������������ظ������ش��ڼ�¼���������ɹ�������������
				 */
				// commit the
				// transaction if exist
//				if (transactionManager != null)
//					transactionManager.commit();
				DBHandler.commitTransaction(flowContext, connection);
				String retMsg = this.getResponseMessage(request, flowContext);
//				SFLogger.info(flowContext,"EMPHTTPService [" + serviceName + "] received, return :" + retMsg);

				return retMsg.getBytes();

			} catch (Exception e) {
//				if (transactionManager != null)
//					transactionManager.rollback();
				DBHandler.rollBackTransaction(flowContext, connection);
				
				/* 
				 * ʧ�ܵ����쳣���ط���
				 */
				SFLogger.error(flowContext,"ESB�쳣: [" + serviceName + "] cause exception, call error class:[" + className + "]",e);
				String retMsg = this.getExceptionResponseMessage(request,flowContext,e);
				
//				if (transactionManager != null)
//					transactionManager.commit();
				DBHandler.commitTransaction(flowContext, connection);
//				SFLogger.info(flowContext,"ESB�쳣���ر��ģ� [" + serviceName + "] received, return :" + retMsg);
				return retMsg.getBytes();
			
			}
		} catch (EMPException ee) {
			throw ee;
		} catch (Exception e) {
			throw new EMPException("Failed to process http service " + this.toString(), e);
		} finally {
			//Context context = (Context)request.getAttribute(EMPConstance.ATTR_CONTEXT);//�ر�����
			DBHandler.releaseConnection(flowContext, connection);			
		}
	}

	
	
	/**
	 * ʹ���������ݸ�������ģ�͡�
	 * <p>
	 * ʹ��EMP����ģ�����û�������<tt>DataElementSerializer</tt>��������ת�������ݡ�
	 * 
	 * @param request HTTP����
	 * @param response HTTP��Ӧ
	 * @param context ����������
	 * @param dataElementDef input����
	 * @throws EMPException
	 */
	public void updateModel(EMPTCPIPRequest request,
			Context context) throws EMPException{
		
		CompositeData reqCD = (CompositeData) request.getAttribute("reqData");
		if (reqCD == null)
			return;
		
		try {
			initPubMsgHead(0,context);
			FormatElement esbHeadFormat = context.getFormat("PAESBHEAD_I");
			esbHeadFormat.unFormat(reqCD, context);
			
			// ������������
			String svcId = (String) request.getAttribute("serviceId");
			FormatElement msgFormat = context.getFormat(svcId + "_I");
			
			msgFormat.unFormat(reqCD, context);	
			
			updatePBankPublicData(context);
			
			//������ӵ�context
			FormatElement outMsgFormat = context.getFormat(svcId + "_O");
			ESBCDFormat outMsg = (ESBCDFormat) outMsgFormat.getFormatElement();
			
			outMsg.addFormatToContext(context);			
			
//			SFLogger.info(context,"EMPHTTPService [" + serviceName + "] received, return :" );
		}
		catch (EMPException e) {
			throw e;
//			throw new EMPException("����ʽ������ʧ��!", e);
		}
		catch (Exception e) {
//			throw new SFException("PHTTPO001", "����ʽ������ʧ��!", e);
			throw new SFException(e);
		}
	}
	
	/**
	 * ��÷��ر����塣
	 * <p>
	 * 
	 * @param request HTTP����
	 * @param response HTTP��Ӧ
	 * @param context ����������
	 * @param dataElement output����
	 * @return ���ر�����
	 * @throws Exception
	 */
	protected String getResponseMessage(EMPTCPIPRequest request,
			Context context) throws Exception {
		
		initPubMsgHead(1,context);
		context.setDataValue("SYS_HEAD.RET_STATUS", "S");	
		
		//String retCode = (String)context.getDataValue("RET.RET_CODE");
		//String retMsg = (String)context.getDataValue("RET.RET_MSG");
		String retCode = SFUtil.getDataValue(context,SFConst.CTX_ERRCODE);
		String retMsg =  SFUtil.getDataValue(context,SFConst.CTX_ERRMSG);
		if(SFConst.RESPCODE_SUCCCODE.equals(retCode)||SFUtil.isEmpty(retCode)){
			context.setDataValue("RET.RET_CODE", SFConst.RESPCODE_SUCCCODE_ESB);
			context.setDataValue("RET.RET_MSG", SFUtil.isEmpty(retMsg)?"���׳ɹ�":retMsg);
		}else{
			context.setDataValue("RET.RET_CODE", retCode);
			context.setDataValue("RET.RET_MSG", retMsg);
			
		}
		
		
		
		FormatElement respHeadFormat = context.getFormat("PAESBHEAD_O");
		PBankFormatElement headFormat = (ESBCDFormat)respHeadFormat.getFormatElement();
		CompositeData responseHeadCD = (CompositeData)headFormat.format(context);
		
//		if(!SFConst.RESPCODE_SUCCCODE_ESB.equals(SFUtil.getDataValue(context, "RET.RET_CODE"))){
//			return PackUtil.packXmlStr(responseHeadCD);
//		}
		
		
		String svcId = (String) request.getAttribute("serviceId");
		FormatElement msgFormat = context.getFormat(svcId + "_O");
		PBankFormatElement cdFormat = (ESBCDFormat)msgFormat.getFormatElement();
		CompositeData responseCD = (CompositeData)cdFormat.format(context);
		
		if(responseCD!=null){
			Iterator ir = responseCD.iterator();
			while(ir.hasNext()){
				String key = (String)ir.next();
				CompositeData cd = responseCD.getStruct(key);
				responseHeadCD.addStruct(key, cd);
				
			}
		}
		return PackUtil.packXmlStr(responseHeadCD);
	}
	
	
	
	/**
	 * ����쳣���ر����塣
	 * <p>
	 * 
	 * @param request HTTP����
	 * @param response HTTP��Ӧ
	 * @param context ����������
	 * @param dataElement output����
	 * @return ���ر�����
	 * @throws Exception
	 */
	protected String getExceptionResponseMessage(EMPTCPIPRequest request,
			Context context,Exception e) throws Exception {
		initPubMsgHead(1,context);		
		context.setDataValue("SYS_HEAD.RET_STATUS", "F");
		
		if(e instanceof SFException){
			SFException sfe = (SFException)e;
			String errorCode = sfe.getErrorCode();
			
			context.setDataValue("RET.RET_CODE", errorCode);
			if(SFUtil.isEmpty(sfe.getMessage())){
				context.setDataValue("RET.RET_MSG", "����ʧ��");
			}else{
				context.setDataValue("RET.RET_MSG",sfe.getMessage());
			}
		}else{
			context.setDataValue("RET.RET_CODE", SFConst.RESPCODE_FAILCODE_ESB);
			context.setDataValue("RET.RET_MSG",SFUtil.isNotEmpty(e.getMessage())?e.getMessage():"����ʧ��");
			
		}
		
		FormatElement respHeadFormat = context.getFormat("PAESBHEAD_O");
		PBankFormatElement headFormat = (ESBCDFormat)respHeadFormat.getFormatElement();
		CompositeData responseHeadCD = (CompositeData)headFormat.format(context);
		return PackUtil.packXmlStr(responseHeadCD);
	}
	
	
	/**
	 * ��ʼ��ESB��������ͷ���ݡ�
	 * 
	 */
	public void initPubMsgHead(int type,Context context) throws Exception
	{
		String[] tmpStrings = null;
		String tmpName = null;
		
		for (int i = 0; i < 5; i++) {
			context.remove("_ESB_PARAM_" + i);
			context.addDataField("_ESB_PARAM_" + i, "");
		}
		
		
		if (type == 0) {
			//����_ESB_HEAD����KCOLL
			context.addDataElement(SFUtil.getTemplateKColl(context, "SYS_HEAD"));
			context.addDataElement(SFUtil.getTemplateKColl(context, "APP_HEAD"));
			context.addDataElement(SFUtil.getTemplateKColl(context, "RET"));
			
//			//����_ESB_HEAD����KCOLL
//			context.addDataElement(SFUtil.getTemplateKColl(context, "_ESB_HEAD"));
			
			//���������ʼ��
			tmpName = "SYS_HEAD;APP_HEAD;;";
			
		}
		else {
			//���������ʼ��
			tmpName = "SYS_HEAD;RET;APP_HEAD;";
		}
		tmpStrings = tmpName.split(";");

		for (int i = 0; i < tmpStrings.length; i++) {
			context.remove("_ESB_PARAM_" + i);
			context.addDataField("_ESB_PARAM_" + i, tmpStrings[i]);
		}
		
	}
	
	/**
	 * ����PBank���׶��彫PBank�Ĺ�����֧����Ʊ�����ã��������ȹ������ݸ��µ�Context�С�
	 * @param request HTTP����
	 * @param context ����������
	 */
	private void updatePBankPublicData(Context context) throws EMPException {

	}

}