package core.communication.access.http;

import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.dc.eai.data.CompositeData;
import com.dcfs.esb.client.converter.PackUtil;
import com.ecc.emp.access.http.EMPHTTPRequestService;
import com.ecc.emp.core.Context;
import com.ecc.emp.core.EMPConstance;
import com.ecc.emp.core.EMPException;
import com.ecc.emp.data.DataElement;
import com.ecc.emp.data.KeyedCollection;
import com.ecc.emp.flow.EMPFlow;
import com.ecc.emp.format.FormatElement;
import com.ecc.emp.log.EMPLog;
import com.ecc.emp.transaction.EMPTransactionManager;
import common.action.db.SqlExecAction;
import common.exception.SFException;
import common.util.SFConst;
import common.util.SFUtil;

import core.communication.format.PBankFormatElement;
import core.communication.format.paesb.ESBCDFormat;
import core.service.PBankGenContextBySql;

/**
 * PBank�ṩ��HTTP����������ʷ������һ��ʵ�֡�
 * <p>
 * �ɽ�EMPҵ���߼���������Ϊ��HTTP����������ʵķ���
 * ʹ��ͨ��HTTP���������ϵͳ����ͨ���������������ĵķ�ʽ����EMP��ҵ�����߼���
 * ��<tt>EMPHTTPRequestService</tt>��ͬ���ǣ�����ʹ�ñ��ĸ�ʽ��������
 * ���ǹ̶�����PAB ESB�����������ݽ���XML��ʽ������.
 * <p>
 * ������һ���������������HttpAccessServletContext�����ļ��У��������£�
 * <p>
 * id��HTTP Service��id��ͨ��ָ����id��Service���з���<br>
 * serviceName��id�ĵȼ۲���<br>
 * serviceType��Service�������ͣ���ѡ����session(�����Ự)��endSession(�����Ự)�Լ�����(Ĭ�ϣ���ͨ����)<br>
 * sessionContextName����serviceTypeΪsessionʱ��Ч��ָ��Ҫ�����Ự��ҵ���߼�Context����<br>
 * checkSession���������Ƿ���Ự����ѡ��true(Ĭ��)��false<br>
 * EMPFlowId����Service��Ӧ��EMPҵ���߼�������<br>
 * opId����Service��Ӧ��EMPҵ���߼������е�Operation ID<br>
 * description��������Ϣ<br>
 * enabled����Service�Ƿ����ã���ѡ��true(Ĭ��)��false<br>
 * 
 * @author zhongmc
 * @version 2.1
 * @since 1.0 2006-12-26
 * @lastmodified 2008-6-11
 * @emp:name HTTP����������ʷ���(DS)
 * @emp:document PBank�ṩ��HTTP����������ʷ��񣬿ɷ���EMPҵ���߼�����
 */
public class PBankHTTPRequestService extends EMPHTTPRequestService {

	/**
	 * ��Service��Ӧ��TranCode
	 */
	protected String tranCode = null;

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
	 * ��Service��Ӧ��PBankϵͳTranCode�������TRAN_CODE�����н����룬��Ҫ���ظ�������ص�����
	 */
	protected String fbsTranCode = null;

	public String getFbsTranCode() {
		return fbsTranCode;
	}

	public void setFbsTranCode(String fbsTranCode) {
		this.fbsTranCode = fbsTranCode;
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
	
	public PBankHTTPRequestService() {
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
	@SuppressWarnings("deprecation")
	public String handleRequest(HttpServletRequest request,	HttpServletResponse response, 
			Context sessionContext,	String sessionId) throws EMPException {

		EMPFlow flow = null;
		Context flowContext = null;
		SqlExecAction sqlExecAction = null;
		//SqlExecAction sqlExecAction_SEL = null;

		EMPLog.log(EMPConstance.EMP_HTTPACCESS, EMPLog.INFO, 0, 
				"EMPHTTPService [" + serviceName + "][" + this.getBizId() + "." + this.getOpId() + "] receive request :[" + 
				PackUtil.packXmlStr((CompositeData)request.getAttribute("reqData")) + "]");
		
		try {
			flow = this.factory.getEMPFlow(getEMPFlowId());

			if (flow == null)
				throw new EMPException("Flow named " + getEMPFlowId() + " not defined!");

			flowContext = (Context) flow.getContext().clone();

			if (flowContext.getParent() == null && sessionContext != null)
				flowContext.chainedTo(sessionContext);

			request.setAttribute(EMPConstance.ATTR_CONTEXT, flowContext);

			String opName = getOpId();

			DataElement outputData = null;
			DataElement inputDataDef = flow.getInput(opName);
			
			KeyedCollection headData = (KeyedCollection)request.getAttribute("headData");
			if(headData != null){
				for (int i=0; i<headData.size(); i++) {
					DataElement dataElement = headData.getDataElement(i);
					if (((KeyedCollection)inputDataDef).containsKey(dataElement.getName())) {
						flowContext.removeDataElement(dataElement.getName());
						flowContext.addDataElement(dataElement);
					}
				}
			}
			
			updateModel(request, response, flowContext, inputDataDef);

			EMPTransactionManager transactionManager = null;

			try {
				transactionManager = (EMPTransactionManager) flowContext.getService(EMPConstance.TRX_SVC_NAME);

				EMPLog.log(EMPConstance.EMP_HTTPACCESS, EMPLog.INFO, 0,
						"execute the Flow: " + getEMPFlowId() + "." + getOpId() + "...");

				String retValue = flow.execute(flowContext, opName);

				outputData = flow.getOutput(flowContext, opName);

				EMPLog.log(EMPConstance.EMP_HTTPACCESS, EMPLog.INFO, 0,
						"execute the Flow: " + getEMPFlowId() + " retValue:" + retValue);
				
				//�ύ֮ǰд��ESB_INFO��Ϣ��
				if (this.isLog){
					sqlExecAction = new SqlExecAction();
					sqlExecAction.setDataSource((String)flowContext.getDataValue(SFConst.SERVICE_DATASOURCE));
					sqlExecAction.setSqlService((String)flowContext.getDataValue(SFConst.SERVICE_SQL));
					sqlExecAction.setRefSQL("sqlEsbInfoInsert");
					sqlExecAction.initialize();

					if (sqlExecAction.execute(flowContext) != "0")
					{
						EMPLog.log("PBankHTTPRequestService", EMPLog.ERROR, 0, "PBankHTTPRequestService��д��ESB_INFO��Ϣ��ʧ��(sqlEsbInfoInsert)");
						throw new SFException("P0006O001","PBankHTTPRequestService��д��ESB_INFO��Ϣ��ʧ��!");
					}
				}
				
				/* Dongjie Shen Add for ESB Mutext Ctrol ���� 20170614 Begin */
				/**
				 * ��ESB���������ļ�httpchannel.xml�����������ԣ�
				 * 1����Ҫ���������أ�����������Ϊ��mutexKey�����عؼ��֣�mutexFlag:0���ף�1����ѯ
				 * 2������������ù����Զ�д��ESB_MUTEX_CONTROL����
				 *	A��mutexFlag=0�����߼���
				 *	����ʱֱ�Ӳ��뻥��������ڸñ����ڻ��⣬�������ݲ���Ͳ�ѯ������ͬһ�������»�����ȴ���Ϊ�˽��׳�ʱ֮�󣬲�ѯ�ܹ����췵�أ������ѯ�ֲ����ȴ���ʱ�����׵Ĳ��뾡���ŵ�����������β������֤����֮��ͽ��ܿ��ύҵ���ˣ���������ҵ������һ���ύ��
				 *	B��mutexFlag=1�����߼���
				 *	B1������һ����ѯ��¼�����������������ظ������ش��ڼ�¼���������ɹ�������������
				 */
				if (null != this.mutexFlag && null != this.mutexKey){
					if (flowContext.containsKey("_ESB_MUTEX_CHECK_RESULT")) {
						flowContext.setDataValue("_ESB_MUTEX_CHECK_RESULT", "F");
					}
					else {
						flowContext.addDataField("_ESB_MUTEX_CHECK_RESULT", "F");
					}

					sqlExecAction = new SqlExecAction();
					sqlExecAction.setDataSource((String)flowContext.getDataValue(SFConst.SERVICE_DATASOURCE));
					sqlExecAction.setSqlService((String)flowContext.getDataValue(SFConst.SERVICE_SQL));
					sqlExecAction.setRefSQL("sqlEsbMutextInsert");
					sqlExecAction.initialize();
					Context tmpContext = ((PBankGenContextBySql) flowContext.getService((String) flowContext
							.getDataValue(SFConst.SERVICE_GENCONTEXTBYSQL))).getContext(flowContext, "sqlEsbMutextInsert");
					
					tmpContext.setDataValue("SEQ_NO", flowContext.getDataValue(this.mutexKey));
					tmpContext.setDataValue("STATUS", this.mutexFlag);
					try {
						if (sqlExecAction.execute(flowContext, tmpContext) != "0")
						{
							EMPLog.log("PBankHTTPRequestService", EMPLog.ERROR, 0, "PBankHTTPRequestService��д��ESB_INFO��Ϣ��ʧ��(sqlEsbInfoInsert)");
							throw new SFException("P0006O001","PBankHTTPRequestService��д��ESB_INFO��Ϣ��ʧ��!");
						}
					} catch (Exception e) {
						if ("1".equals(this.mutexFlag) && SQLException.class.isAssignableFrom(e.getCause().getClass())) {
							SQLException aSqlException = (SQLException)e.getCause();
							if (1 == aSqlException.getErrorCode() && "23000".equals(aSqlException.getSQLState())) {
								EMPLog.log("PBankHTTPRequestService", EMPLog.ERROR, 0, "PBankHTTPRequestService��д��ESB_INFO��Ϣ��ʧ��(sqlEsbInfoInsert),�����ظ�");
								flowContext.setDataValue("_ESB_MUTEX_CHECK_RESULT", "S");
							}
						}
						else {
							throw e;
						}
					}
				}
				/* Dongjie Shen Add for ESB Mutext Ctrol ���� 20170614 End */

				// commit the
				// transaction if exist
				if (transactionManager != null)
					transactionManager.commit();

				String retMsg = this.getResponseMessage(request, response, flowContext, outputData);

				EMPLog.log(EMPConstance.EMP_HTTPACCESS, EMPLog.INFO, 0,
						"EMPHTTPService [" + serviceName + "] received, return :" + retMsg);

				return retMsg;

			} catch (Exception e) {
				if (transactionManager != null)
					transactionManager.rollback();
				
				/* 
				 * ����ΪPBank PAESB��������,���ʧ�ܿ��Ե���һ���ƺ��biz.20120520
				 */
				if (this.errBizId != null && this.errOpId != null)
				{
					EMPLog.log(EMPConstance.EMP_HTTPACCESS, EMPLog.INFO, 0,
							"EMPHTTPService [" + serviceName + 
							"] cause exception, call error biz:[" + errBizId + "]" + "[" + errOpId + "]");

					EMPFlow errFlow = this.factory.getEMPFlow(errBizId);
					errFlow.execute(flowContext, errOpId);
					if (transactionManager != null)
						transactionManager.commit();

					String retMsg = this.getResponseMessage(request, response, flowContext, outputData);

					EMPLog.log(EMPConstance.EMP_HTTPACCESS, EMPLog.INFO, 0,
							"EMPHTTPService [" + serviceName + "] received, return :" + retMsg);

					return retMsg;
				}
				else
				{
					throw e;
				}
				/* 
				 * ����ΪPBank PAESB��������,���ʧ�ܿ��Ե���һ���ƺ��biz.20120520
				 */
			}
		} catch (EMPException ee) {
			throw ee;
		} catch (Exception e) {
			throw new EMPException("Failed to process http service " + this.toString(), e);
		} finally {
			// if( flowContext != null )
			//flowContext.terminate();
		}
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
	public void handleRequestForFinalBiz(HttpServletRequest request, HttpServletResponse response, 
			Context sessionContext,	String sessionId) throws EMPException {

		EMPFlow flow = null;
		Context flowContext = null;

		try {
			flow = this.factory.getEMPFlow(getFinalBizId());

			if (flow == null)
				throw new EMPException("Flow named " + getFinalBizId() + " not defined!");

			flowContext = (Context) flow.getContext().clone();

			if (flowContext.getParent() == null && sessionContext != null)
				flowContext.chainedTo(sessionContext);

			/* Shendongjie Delete for FinalFlow execute 20120727 begin
			request.setAttribute(EMPConstance.ATTR_CONTEXT, flowContext);
			Shendongjie Delete for FinalFlow execute 20120727 end */

			String opName = getFinalOpId();

			DataElement outputData = null;
			DataElement inputDataDef = flow.getInput(opName);
			
			KeyedCollection headData = (KeyedCollection)request.getAttribute("headData");
			if(headData != null){
				for (int i=0; i<headData.size(); i++) {
					DataElement dataElement = headData.getDataElement(i);
					if (((KeyedCollection)inputDataDef).containsKey(dataElement.getName())) {
						flowContext.removeDataElement(dataElement.getName());
						flowContext.addDataElement(dataElement);
					}
				}
			}
			
			updateModel(request, response, flowContext, inputDataDef);

			EMPTransactionManager transactionManager = null;

			try {
				transactionManager = (EMPTransactionManager) flowContext.getService(EMPConstance.TRX_SVC_NAME);

				EMPLog.log(EMPConstance.EMP_HTTPACCESS, EMPLog.INFO, 0,
						"execute the Flow: " + getFinalBizId() + "." + getFinalOpId() + "...");

				String retValue = flow.execute(flowContext, opName);

				outputData = flow.getOutput(flowContext, opName);

				EMPLog.log(EMPConstance.EMP_HTTPACCESS, EMPLog.INFO, 0,
						"execute the Flow: " + getFinalBizId() + " retValue:" + retValue);
				
				// commit the
				// transaction if exist
				if (transactionManager != null)
					transactionManager.commit();

				/* ShenDongjie Delete for response flow execute 20120727 begin
				String retMsg = this.getResponseMessage(request, response, flowContext, outputData);

				EMPLog.log(EMPConstance.EMP_HTTPACCESS, EMPLog.INFO, 0,
						"EMPHTTPService [" + serviceName + "] received, return :" + retMsg);

				return retMsg;
				ShenDongjie Delete for response flow execute 20120727 end */

			} catch (Exception e) {
				if (transactionManager != null)
					transactionManager.rollback();
				throw e;
			}
		} catch (EMPException ee) {
			throw ee;
		} catch (Exception e) {
			throw new EMPException("Failed to process http service " + this.toString(), e);
		} finally {
			if (flowContext != null)
				 flowContext.terminate();
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
	public void updateModel(HttpServletRequest request,	HttpServletResponse response, 
			Context context, DataElement dataElementDef) throws EMPException {
		
		CompositeData reqCD = (CompositeData) request.getAttribute("reqData");
		if (reqCD == null)
			return;
		
		try {
			initPubMsgHead(context);
			FormatElement esbHeadFormat = context.getFormat("PAESBHEAD_I");
			esbHeadFormat.unFormat(reqCD, context);
			
			initMsgHead(0, context);
			FormatElement msgFormat = context.getFormat(tranCode + "_I");
			msgFormat.unFormat(reqCD, context);	
			
			updatePBankPublicData(context);
		}
		catch (EMPException e) {
			throw e;
		}
		catch (Exception e) {
			throw new SFException("PHTTPO001", "����ʽ������ʧ��!", e);
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
	protected String getResponseMessage(HttpServletRequest request,	HttpServletResponse response, 
			Context context, DataElement dataElement) throws Exception {
		
		initMsgHead(1, context);
		FormatElement msgFormat = context.getFormat(tranCode + "_O");
		PBankFormatElement cdFormat = (ESBCDFormat)msgFormat.getFormatElement();
		CompositeData responseCD = (CompositeData)cdFormat.format(context);
		return PackUtil.packXmlStr(responseCD);
	}
	
	/**
	 * ��ʼ������ͷ���ݡ�
	 * 
	 */
	public void initMsgHead(int type, Context context) throws Exception
	{
		String[] tmpStrings = null;
		String tmpName = null;
		
		for (int i = 0; i < 16; i++) {
			context.remove("_ESB_PARAM_" + i);
			context.addDataField("_ESB_PARAM_" + i, "");
		}

		int index = inOutDataArea.indexOf('|');
		if (type == 0) {
			//���������ʼ��
			if (inOutDataArea.length() > index && index > 0)
				tmpName = inOutDataArea.substring(0, index);
		}
		else {
			//���������ʼ��
			if (inOutDataArea.length() > index && index > 0)
				tmpName = inOutDataArea.substring(index+1);
		}
		tmpStrings = tmpName.split(";");

		for (int i = 0; i < tmpStrings.length; i++) {
			context.remove("_ESB_PARAM_" + i);
			context.addDataField("_ESB_PARAM_" + i, tmpStrings[i]);
		}
	}
	
	/**
	 * ��ʼ��ESB��������ͷ���ݡ�
	 * 
	 */
	public void initPubMsgHead(Context context) throws Exception
	{
		String[] tmpStrings = null;
		String tmpName = null;
		
		for (int i = 0; i < 16; i++) {
			context.remove("_ESB_PARAM_" + i);
			context.addDataField("_ESB_PARAM_" + i, "");
		}
		
		tmpName = "_ESB_HEAD;_ESB_HEAD;;";
		tmpStrings = tmpName.split(";");

		for (int i = 0; i < tmpStrings.length; i++) {
			context.remove("_ESB_PARAM_" + i);
			context.addDataField("_ESB_PARAM_" + i, tmpStrings[i]);
		}
		
		//����_ESB_HEAD����KCOLL
		context.addDataElement(SFUtil.getTemplateKColl(context, "_ESB_HEAD"));
	}
	
	/**
	 * ����PBank���׶��彫PBank�Ĺ�����֧����Ʊ�����ã��������ȹ������ݸ��µ�Context�С�
	 * @param request HTTP����
	 * @param context ����������
	 */
	private void updatePBankPublicData(Context context) throws EMPException {

	}

}