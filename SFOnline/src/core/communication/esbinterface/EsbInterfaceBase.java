package core.communication.esbinterface;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.dc.eai.data.Array;
import com.dc.eai.data.CompositeData;
import com.dc.eai.data.Field;
import com.dc.eai.data.FieldAttr;
import com.dc.eai.data.FieldType;
import com.dcfs.esb.client.converter.PackUtil;
import com.ecc.emp.core.Context;
import com.ecc.emp.core.EMPException;
import com.ecc.emp.data.DataElement;
import com.ecc.emp.data.DataField;
import com.ecc.emp.data.IndexedCollection;
import com.ecc.emp.data.KeyedCollection;
import com.ecc.emp.flow.EMPAction;
import com.ecc.emp.format.FormatElement;
import common.exception.SFException;
import common.services.PublicService;
import common.util.BizUtil;
import common.util.DateUtil;
import common.util.SFConst;
import common.util.SFUtil;

import core.cache.CacheMap;
import core.communication.access.esb.ESBAccessParam;
import core.communication.access.esb.ESBSendAction;
import core.communication.access.esb.SFAccessParam;
import core.communication.hostinterface.HostInterface;
import core.communication.util.AccessConstance;
import core.log.SFLogger;

/**
 * PBank�����������ֽӿڵĻ��ࡣ
 * <p>
 * �̳д���ʵ�ָ��������ӿ��࣬��Ҫʵ���鷽����
 *
 * @author PBank
 * @version 1.0
 * @since 1.0 2010-02-23
 * @lastmodified 2010-02-23
 */
public abstract class EsbInterfaceBase extends EMPAction implements HostInterface{

	/**
	 * �ýӿڵ�����
	 */
	protected String trxCode = null;
	
	/**
	 * �ýӿ�ʧ���Ƿ��׳������־
	 */
	protected boolean throwExceptionFlag = false;

	/**
	 * �ýӿ����������������KeyedCollection��
	 */
	protected String inOutDataArea = null;
	
	/**
	 * TCPIPͨѶ����ID
	 */
	protected String tcpipServiceName;
	
	/**
	 * �ַ�����
	 */
	protected String encoding = "GB18030";

	/**
	 * �Ƿ񽫱��ļ�¼�����ݿ���
	 */
	protected boolean saveInDatabase = false;
	
	/**
	 * ��̬����,��������ǰ�û���ˮ��
	 *
	 */
	
	
	protected String serviceCode = null;
	
	public String getServiceCode() {
		return serviceCode;
	}

	public void setServiceCode(String serviceCode) {
		this.serviceCode = serviceCode;
	}

	private static long journalNo = 0; //������
	private static Object journalNoLock = new Object();
	
	public EsbInterfaceBase() {
	}

	public EsbInterfaceBase(String trxCode) {
		this.trxCode = trxCode;
	}
	
	/**
	 * ���׷�������ʱ����ʵʱ������
	 * 
	 * @throws SFException
	 */
	public abstract void reversal(Context context) throws SFException;

	/**
	 * ��������֮ǰ�������ݵĽӿڡ�
	 * 
	 */
	public void beforeSendData(Context context, CompositeData requestCD, KeyedCollection sysHead, String seqNoOverRide) throws Exception
	{
		String strJournalNo = null, tranTime = null,consumerId = null;
		
		CompositeData sysHeadCD = requestCD.getStruct("SYS_HEAD");
		CompositeData appHeadCD = requestCD.getStruct("APP_HEAD");
		//CompositeData localHeadCD = requestCD.getStruct("LOCAL_HEAD");
		if (sysHeadCD == null || appHeadCD == null) {
			SFLogger.error(context, "ESB ����CD����SysHead��AppHead!");
			throw new SFException("ESB ����CD����SysHead��AppHead!");
		}
		
		//��ȡʱ��
		tranTime = DateUtil.getDateTime("yyyyMMddHHmmssSSS");
		consumerId = SFConst.SYS_SYSID;
		
		String sCode = null,sScene = null;
		if(this.serviceCode!=null){
			
			String [] sCAraay = this.serviceCode.split("_");
			sCode = sCAraay[0];
			sScene = sCAraay[1];
		}
		
				
		//ȡ������ͷ����FCR����ӿڴ���sysHead
		KeyedCollection sysHeadKColl = null;
		if (sysHead == null)
			sysHeadKColl = (KeyedCollection)context.getDataElement((String)context.getDataValue("_ESB_PARAM_0"));
		else
			sysHeadKColl = sysHead ;
			
		for (int i = 0; i < sysHeadKColl.size(); i++) {
			DataField aDataField = (DataField)sysHeadKColl.getDataElement(i);
			String aName = (String)(aDataField.getName());
			String aValue = (String)(aDataField.getValue());
			
			if (aName.equals("SERVICE_CODE")) {
				Field aField = new Field(new FieldAttr(FieldType.FIELD_STRING, 30, 0));
				aField.setValue(sCode);
				sysHeadCD.addField(aName, aField);
				aDataField.setValue(sCode);
				continue;
				
			}
			
			if (aName.equals("SERVICE_SCENE")) {
				
				Field aField = new Field(new FieldAttr(FieldType.FIELD_STRING, 2, 0));
				aField.setValue(sScene);
				sysHeadCD.addField(aName, aField);
				aDataField.setValue(sScene);
				continue;
				
			}
			
			
			if (aName.equals("CONSUMER_ID")) {
				
				if (context.containsKey("SYS_HEAD"))
				{ 
					//consumerId = (String)context.getDataValue("SYS_HEAD.CONSUMER_ID");
					//if (SFUtil.isEmpty( consumerId )){
						consumerId = SFConst.SYS_SYSID;
					//}
				}
				Field aField = new Field(new FieldAttr(FieldType.FIELD_STRING, 6, 0));
				aField.setValue(consumerId);
				sysHeadCD.addField(aName, aField);
				aDataField.setValue(consumerId);
				continue;
			
			}
			
			
			
			if (aName.equals("CONSUMER_SEQ_NO")) {
				if ("Y".equals(seqNoOverRide) || (aValue == null) || "".equals(aValue)){
				//	strJournalNo =    PBankConstance._PBank_SYSID + SFUtil.getDateTime("yyMMdd") + voucherNo;// getVoucherNo(context);
					strJournalNo = SFUtil.getDataValue(context, SFConst.PUBLIC_MSG_SEQ_NO);
					if(SFUtil.isEmpty(strJournalNo)){
						strJournalNo =  BizUtil.getChlSeqId(context,BizUtil.getSubTxSeqId(BizUtil.getInitSeqId(context)));//  
					}
					
					Field aField = new Field(new FieldAttr(FieldType.FIELD_STRING, 52, 0));
					aField.setValue(strJournalNo);
					sysHeadCD.addField(aName, aField);
					aDataField.setValue(strJournalNo);
				}
				
				continue;
			}
			
			if (aName.equals("TRAN_DATE")) {
				Field aField = new Field(new FieldAttr(FieldType.FIELD_STRING, 8, 0));
				aField.setValue(tranTime.substring(0, 8));
				sysHeadCD.addField(aName, aField);
				aDataField.setValue(tranTime.substring(0, 8));
				continue;
			}
			
			if (aName.equals("TRAN_TIMESTAMP")) {
				Field aField = new Field(new FieldAttr(FieldType.FIELD_STRING, 9, 0));
				aField.setValue(tranTime.substring(8, 17));
				sysHeadCD.addField(aName, aField);
				aDataField.setValue(tranTime.substring(8, 17));
				continue;
			}
			
		}
		
		//��APP_HEAD��USER_ID��ֵ���������ΧϵͳESB��������ģ�ֱ�ӻ�ȡ��ESB��
		Field userIdField = appHeadCD.getField("USER_ID");
		if (userIdField == null || userIdField.strValue().length() == 0){//����ýӿ��Լ���ֵ�ˣ��Ͳ��ٴ����ˡ�
			String staffUm = "";
			
			//�����ESB����Χϵͳ����PBank��̨������_ESB_HEAD�������󣬴���Χ�����͵��û�Ϊ׼
			if (context.containsKey("_ESB_HEAD"))
			{ 
				staffUm = (String)context.getDataValue("_ESB_HEAD.USER_ID");
			}
			
			if (!SFUtil.isNotEmpty(staffUm))
			{
//				if (context.containsKey("_PBank_USER_STAFFNO"))
//				{
//					staffUm = (String)context.getDataValue("_PBank_USER_STAFFNO");
//				}
				
				if (!SFUtil.isNotEmpty(staffUm) || "Computer".equals(staffUm) || "computer".equals(staffUm)){
					staffUm = "VIRTU";
				}
			}
			
			Field aField = new Field(new FieldAttr(FieldType.FIELD_STRING, 30, 0));
			aField.setValue(staffUm);
			appHeadCD.addField("USER_ID", aField);
		}
		
		//��APP_HEAD��BRANCH_ID��ֵ���������ΧϵͳESB��������ģ�ֱ�ӻ�ȡ��ESB��
		Field branchIdField = appHeadCD.getField("BRANCH_ID");
		if (branchIdField == null || branchIdField.strValue().length() == 0){//����ýӿ��Լ���ֵ�ˣ��Ͳ��ٴ����ˡ�
			String branchId = null;
			
			if (context.containsKey("_ESB_HEAD"))
			{ 
				branchId = (String)context.getDataValue("_ESB_HEAD.BRANCH_ID");
			}
			
//			if(context.containsKey("_PBank_USER_BRNO")){
//				branchId = (String)context.getDataValue("_PBank_USER_BRNO");
//			}
			if(!SFUtil.isNotEmpty(branchId)){
				branchId = "9998";
			}
			
			Field aField = new Field(new FieldAttr(FieldType.FIELD_STRING, 9, 0));
			aField.setValue(branchId);
			appHeadCD.addField("BRANCH_ID", aField);
		}
		
		//��APP_HEAD��BIZ_SEQ_NO��ֵ
		Field bizSeqNoField = appHeadCD.getField("BIZ_SEQ_NO");
		if (bizSeqNoField == null || bizSeqNoField.strValue().length() == 0)
		{
//			if (context.containsKey("_ESB_HEAD") && SFUtil.isNotEmpty((String)context.getDataValue("_ESB_HEAD.BIZ_SEQ_NO")))
//			{//ֱ�ӻ�ȡԭESB��ˮ��
//				strJournalNo = (String)context.getDataValue("_ESB_HEAD.BIZ_SEQ_NO");
//			}
//			else
//			{
//				//�ӵ���context�ж�ȡҵ����ˮ�ţ�
//				//ÿ��ϵͳ�Լ�ʵ��ȡҵ����ˮ��
//				//strJournalNo = ((PBankVoucherService) context.getService((String) context
//				//		.getDataValue(PBankConstance.PBank_VOUCHERSERVICENAME))).getBizSeqNo(context);
//				if ( strJournalNo == null || "".equals(strJournalNo)){
//					//���Ϊ�գ�ֱ�ӻ�ȡ������ˮ��
//					strJournalNo = (String)sysHeadCD.getField("CONSUMER_SEQ_NO").getValue();
//				}
//			}
			
			if ( strJournalNo == null || "".equals(strJournalNo)){
				//���Ϊ�գ�ֱ�ӻ�ȡ������ˮ��
				strJournalNo = (String)sysHeadCD.getField("CONSUMER_SEQ_NO").getValue();
			}
			
			Field aField = new Field(new FieldAttr(FieldType.FIELD_STRING, 52, 0));
			aField.setValue(strJournalNo);
			appHeadCD.addField("BIZ_SEQ_NO", aField);
		}
		bizSeqNoField = appHeadCD.getField("BIZ_SEQ_NO");
		//ҵ����ˮ����ԭʼ����ϵͳ���Ҫ�����ֱ�ӽ�ȡ��ֵ
		Field aField = new Field(new FieldAttr(FieldType.FIELD_STRING, 6, 0));
		aField.setValue(bizSeqNoField.strValue().substring(0,6));
		sysHeadCD.addField("ORG_SYS_ID", aField);
		
		//���ױ�ʶ
		Field trantFlagField = appHeadCD.getField("TRANT_FLAG");
		if (trantFlagField == null || trantFlagField.strValue().length() == 0)
		{ 
			String trantFlag = null;
			if (context.containsKey("_ESB_HEAD") && SFUtil.isNotEmpty((String)context.getDataValue("_ESB_HEAD.TRANT_FLAG")))
			{
				trantFlag = (String)context.getDataValue("_ESB_HEAD.TRANT_FLAG");
				Field aField1 = new Field(new FieldAttr(FieldType.FIELD_STRING, 1, 0));
				aField1.setValue(trantFlag);
				appHeadCD.addField("TRANT_FLAG", aField1);
			}
		}
		
		
		//��ֵ��������ջ�нṹ�����ڳ���ʱ�ṩԭ����
/*		try {
			CompositeData inOutDTO = (CompositeData)context.getDataValue(PBankConstance.PBank_ESB_INOUTDTONAME);
			if(inOutDTO!=null){ //�ǿ��ж�
				inOutDTO.addStruct("_ESB_REQUEST_DATA", requestCD);
			}
		}
		catch (ObjectNotFoundException e)
		{
		}
		catch (Exception e)
		{
			throw e;
		}
*/	}

	/**
	 * ��������֮�������ݵĽӿڡ�
	 * 
	 */
	public void afterSendData(Context context, CompositeData requestCD, CompositeData responseCD) throws Exception 
	{
		//���ͷ�е�KColl�е���Ϣ
		
		//ESB ��Ӧ��ȡ����λ
		KeyedCollection retKcoll =  (KeyedCollection) ((IndexedCollection)context.getDataElement("RET")).get(0);
		String retCode = (String)retKcoll.getDataValue("RET_CODE");
		if(SFUtil.isNotEmpty(retCode)&&retCode.length()>=6){
			retCode = retCode.substring(retCode.length()-6, retCode.length());
			retKcoll.setDataValue("RET_CODE", retCode);
		}		
		return;
	}

	/**
	 * ����ESB���ض���õ�������װ��Ϣ��
	 * 
	 */
	public String getRetErrMsg(Context context, CompositeData responseCD) throws SFException {
		if (responseCD == null)
			return "δ�õ�ESB�ķ�����Ϣ";
		
		String errMsg = "", tmpRetCode = null, tmpRetMsg = null;
		
		Array retArray = responseCD.getStruct("SYS_HEAD").getArray("RET");		
		for (int k = 0; k < retArray.size(); k++)
		{							
			tmpRetCode = retArray.getStruct(k).getField("RET_CODE").strValue();
			tmpRetMsg = retArray.getStruct(k).getField("RET_MSG").strValue();
			
			if (tmpRetCode != null && !"".equals(tmpRetCode))
				errMsg = errMsg + "������[" + tmpRetCode + "],������Ϣ[" + tmpRetMsg +"];";
		}
		
		errMsg = errMsg + "������ˮ[" + responseCD.getStruct("SYS_HEAD").getField("CONSUMER_SEQ_NO").strValue() + "]";
		
		return errMsg;
	}
	
	/**
	 * �жϽ����Ƿ�ɹ�,�ɹ�����true,ʧ�ܷ���false��
	 * 
	 */
	public boolean checkResult(Context context, CompositeData respCD) throws SFException {
		if (respCD == null)
			return false;
		
		String retrunStatus = (String)respCD.getStruct("SYS_HEAD").getField("RET_STATUS").getValue();
		String returnCode = (String)respCD.getStruct("SYS_HEAD").getArray("RET").getStruct(0).getField("RET_CODE").getValue();
		
		if ("S".equals(retrunStatus) && "000000".equals(returnCode)){
			return true;
		}
		
		return false;
	}

	/**
	 * ִ�нӿ��߼�,�����ӿ����ݡ�
	 * 
	 * @throws SFException
	 */
	public String execute(Context context) throws EMPException
	{
		CompositeData responseCD = null;
		Map inOutDTO = null;
		
		if (context.containsKey(AccessConstance.PBank_ESB_INOUTDTONAME))
			inOutDTO = (Map)context.getDataValue(AccessConstance.PBank_ESB_INOUTDTONAME);
		
		//�������ı�����ֵ�����ĸ�ʽ��ʱʹ��
		try {
			initMsgHead(0, context);
			
			//��ʽ��ͷ����
			FormatElement headMsgFormat = context.getFormat("PAESBHEAD_I");
			CompositeData requestHeadCD = (CompositeData)headMsgFormat.format(context);
			
			//��ʽ������
			FormatElement msgFormat = context.getFormat(trxCode + "_I");
			CompositeData requestCD = (CompositeData)msgFormat.format(context);

			
			//����ͷ�ӱ�����
			if(requestCD!=null){
				Iterator ir = requestCD.iterator();
				while(ir.hasNext()){
					String key = (String)ir.next();
					CompositeData cd = requestCD.getStruct(key);
					requestHeadCD.addStruct(key, cd);
					
				}
			}
			requestCD = requestHeadCD;
			
			beforeSendData(context, requestCD, null, "Y");

			//SFLogger.info(context, "Send message to PAESB, the request is [" + PackUtil.packXmlStr(requestCD) + "]");
			//inOutDTO�����ڳ����ġ���������б����˷��͵ı��ģ�Ӧ��ı��ģ�����ʱ��������ĵ�context
			//�ж��Ƿ�Ҫ��������ʹ�ýӿڵı��ģ�contextֻ��һ���ο���ɾ���ϵ�ʱʹ��context������ɾ��������������ѯ�򶫽�
			if (inOutDTO != null)
				inOutDTO.put("_ESB_INDTO", requestCD);
			
//			responseCD = ESBClient.request(requestCD);
			//���ĵ�����
			int timeOut = 40000;
			String isTranSpond = SFUtil.getDataValue(context, SFConst.PUBLIC_IS_TRANSPOND);
			if("Y".equals(isTranSpond)){   //ת��������̨
				SFAccessParam SFParam = CacheMap.getCache("SF_ACCESS_PARAM");
				timeOut = SFParam.getSFTimeOut();
			}else{
				ESBAccessParam esbParam = CacheMap.getCache("ESB_ACCESS_PARAM");
				timeOut = esbParam.getEsbTimeOut();
			}
			String reqData=null;
			PublicService pubService = ((PublicService) context.getService((String) context.getDataValue(SFConst.SERVICE_PUBLICSERVICENAME)));
			/*
			 * �ж��������ⷢ���Ƿ���ڵ���
			 */
			if(!pubService.hasBaffle(context, "1", serviceCode, "",timeOut)){
				ESBSendAction esbSendAction = new ESBSendAction();
				reqData = esbSendAction.send(context,requestCD);
			}else{
				reqData = pubService.baffle(context, "1", serviceCode, "",timeOut);
			}
			
			responseCD = PackUtil.unpackXmlStr(reqData.trim());
			
			if (inOutDTO != null)
				inOutDTO.put("_ESB_OUTDTO", responseCD);
			//SFLogger.info(context, "the response is [" + PackUtil.packXmlStr(responseCD) + "]");
	
			//�������ı�����ֵ�����ķ���ʽ��ʱʹ��
			initMsgHead(1, context);
			
			
			//����ʽ������ͷ
			msgFormat = context.getFormat("PAESBHEAD_O");
			msgFormat.unFormat(responseCD, context);

			//����ʽ��������
			msgFormat = context.getFormat(trxCode + "_O");
			msgFormat.unFormat(responseCD, context);
	
			afterSendData(context, requestCD, responseCD);
		}
		catch (Exception e) {
			throw new EMPException("�����ķ��͵�ESBʧ��", e);
		}

		if (!checkResult(context, responseCD))
		{
			String tmpRetCode = null, tmpRetMsg = null , tmpSeq = null,  errMsg = "";

			if (responseCD == null) {
				tmpRetCode = "P0022O9999";
				tmpRetMsg = "δ֪����";
				tmpSeq = "";
			}
			else {
				Array retArray = responseCD.getStruct("SYS_HEAD").getArray("RET");				
				for (int k = 0; k < retArray.size(); k++)
				{							
					tmpRetCode = retArray.getStruct(k).getField("RET_CODE").strValue();
					tmpRetMsg = retArray.getStruct(k).getField("RET_MSG").strValue();
					
					if (tmpRetCode != null && !"".equals(tmpRetCode))
						errMsg = errMsg + "������[" + tmpRetCode + "],������Ϣ[" + tmpRetMsg +"]��";
				}
				
				tmpSeq = responseCD.getStruct("SYS_HEAD").getField("CONSUMER_SEQ_NO").strValue();
			}
			if (throwExceptionFlag) {
				throw new SFException("P0022O001", "ESB����ʧ��!" + errMsg + "������ˮ["+tmpSeq+"]");
			}

			return "-1";
		}
		
		//inOutDTO�����ڳ����ġ���������б����˷��͵ı��ģ�Ӧ��ı��ģ�����ʱ��������ĵ�context
		//�ж��Ƿ�Ҫ��������ʹ�ýӿڵı��ģ�contextֻ��һ���ο���ɾ���ϵ�ʱʹ��context������ɾ��������������ѯ�򶫽�
		//������汣��һ�£����û�����ϣ�ʹ�õ�ǰ��context���г���������
		if (inOutDTO != null){
			//����û�е���context��clone��������Ϊ�ͷűȽ����ѣ��п���©��terminate()
			Context tmpContext = new Context(context.getName());
			tmpContext.setDataElement((DataElement)context.getDataElement().clone());
			tmpContext.setParentContextName(context.getParentContextName());
			if(context.getParent() != null)
				tmpContext.setParent(context.getParent());
			
			inOutDTO.put("_ESB_INDTO_CONTEXT", tmpContext);
		}
		return "0";
	}
	
	
	
	/**
	 * ��������ӿ����ơ�
	 * 
	 * @return �����ӿ�����
	 */
	public String getTranCode() {
		return trxCode;
	}

	/**
	 * ���������ӿ����ơ�
	 * 
	 * @param name �����ӿ�����
	 */
	public void setTranCode(String trxCode) {
		this.trxCode = trxCode;
	}

	/**
	 * ע��ýӿ�ʧ���Ƿ��׳������־��
	 * 
	 * @param reversalFlag �״���־
	 */
	public void setThrowExceptionFlag(boolean throwExceptionFlag)
	{
		this.throwExceptionFlag = throwExceptionFlag;
	}
	
	/**
	 * ��øýӿ�ʧ���Ƿ��׳������־��
	 * 
	 */
	public boolean getThrowExceptionFlag()
	{
		return this.throwExceptionFlag;
	}

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
	 * ���ɸýӿ�����������󣬷���context�С�
	 * 
	 */
	public Object genInOutDTO(Context context) throws SFException
	{
		Map inOutDTO = new HashMap();
		context.remove(AccessConstance.PBank_ESB_INOUTDTONAME);

		try {
			//context.addDataField(PBankConstance.PBank_ESB_INOUTDTONAME, inOutDTO);
		}
		catch(Exception e) {
			throw new SFException("���ɳ�����ϵ�ı�������������ݴ������", e);
		}
		
		return inOutDTO;
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
	 * ��ȡTcpIp�������ơ�
	 * 
	 */
	public String getTcpipServiceName() {
		return tcpipServiceName;
	}

	/**
	 * ����TcpIp�������ơ�
	 * 
	 */
	public void setTcpipServiceName(String tcpipServiceName) {
		this.tcpipServiceName = tcpipServiceName;
	}

	/**
	 * ��ȡ�����ַ������롣
	 * 
	 */
	public String getEncoding() {
		return encoding;
	}

	/**
	 * ���ô����ַ������롣
	 * 
	 */
	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}
	
	/**
	 * ע���Ƿ񽫱��ļ�¼�����ݿ�ı�־��
	 * 
	 * @param saveInDatabase �Ƿ񽫱��ļ�¼�����ݿ�ı�־
	 */
	public void setSaveInDatabase(boolean saveInDatabase)
	{
		this.saveInDatabase = saveInDatabase;
	}

	/**
	 * ��ȡ�Ƿ񽫱��ļ�¼�����ݿ�ı�־��
	 * 
	 */
	public boolean getSaveInDatabase()
	{
		return this.saveInDatabase;
	}
}