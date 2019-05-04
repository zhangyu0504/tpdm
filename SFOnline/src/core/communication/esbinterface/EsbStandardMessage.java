package core.communication.esbinterface;

import com.dc.eai.data.CompositeData;
import com.ecc.emp.core.Context;
import com.ecc.emp.core.EMPException;
import com.ecc.emp.data.IndexedCollection;
import com.ecc.emp.data.KeyedCollection;
import common.util.SFConst;
import common.util.SFUtil;


/**
 * �º�����Ŀ��FBS�����������ֽӿڵĻ��ࡣ
 * <p>
 * �򶫽�����begin�����಻ͬ��ͨ����ĵط����ڣ�����Ҫ����ESB�ı��ĵ�SYS_HEAD,APP_HEAD,RET����ȣ�ϵͳ�Զ����ӣ�
 * ֻ��Ҫ��INOUTAREA���涨�屨��Ҫ�ؼ��ɣ�������������ı���ʹ�ã��ָ�����֧�����ޣ�������ͨ�ã�����������
 * �����򶫽�д��ͨ���Խϸߵġ�
 * �򶫽�����end.
 *
 * @author FBS
 * @version 1.0
 * @since 1.0 2010-02-23
 * @lastmodified 2010-02-23
 */
public class EsbStandardMessage extends EsbGeneralMessage {

	public EsbStandardMessage(){
		super();
	}
	
	/**
	 * ��������֮ǰ�������ݵĽӿڡ�
	 * 
	 */
	public void beforeSendData(Context context, CompositeData requestCD, KeyedCollection sysHead, String seqNoOverRide) throws Exception
	{
		super.beforeSendData(context, requestCD, sysHead, "N");
	}

	/**
	 * ��ʼ������ͷ���ݡ�
	 * 
	 */
	public void initMsgHead(int type, Context context) throws Exception
	{
		if (type == 0) {
			//��ȡESB����ͷ�ṹ
			getIsysHead(context);
			//��ȡESB����ͷ�ṹ
			getAppHead(context);
//			//��ȡESB����ͷ�ṹ
//			getAppHeadUp(context);
//			//��ȡESB����ͷ�ṹ
//			getAppHeadDown(context);
			//��ȡESB����ͷ�ṹ
			getRet(context);
			
			String inOutDataParam = getInOutDataArea();
			String  inParamData= null, outParamData = null, outParamData1 = null;
			
			
			if(inOutDataParam==null){
				
				this.setInOutDataArea("SYS_HEAD;APP_HEAD;;;|SYS_HEAD;RET;APP_HEAD;");
				
			}else if (inOutDataParam.indexOf("APP_HEAD") == -1 && inOutDataParam.indexOf("SYS_HEAD") == -1){//�˴��߼����ж��Ƿ�Ϊȫ���ַ���,����ֻ�����������
				if (inOutDataParam.indexOf(";")!=-1){
					String[] paramDatas = inOutDataParam.split(";");
					if (paramDatas.length>2){
						inParamData = paramDatas[0];
						outParamData = paramDatas[1];
						outParamData1 = paramDatas[2];
					}
					if (paramDatas.length>1){
						inParamData = paramDatas[0];
						outParamData = paramDatas[1];
					}
					if (paramDatas.length==1){
						inParamData = paramDatas[0];
					}
				}
				if (null == inParamData)
					inParamData = "";
				if (null == outParamData)
					outParamData = "";
				if (null == outParamData1)
					outParamData1 = "";
				this.setInOutDataArea("SYS_HEAD;APP_HEAD;;;"+inParamData+"|SYS_HEAD;RET;APP_HEAD;"+outParamData+";"+outParamData1);
			}
		}
		super.initMsgHead(type, context);
	}
	
	/**
	 * ����������ϵͳͷ
	 * @param context
	 * @return
	 * @throws Exception
	 */
	private void getIsysHead(Context context) throws EMPException{
		//�Ȼ�ȡKEYCOLL���������ֱ�ӳ�ʼ��
		KeyedCollection sysHead = null;
		if(!context.containsKey("SYS_HEAD")){
			sysHead = new KeyedCollection("SYS_HEAD"); 
			sysHead.addDataField("SERVICE_CODE", "");
			sysHead.addDataField("CONSUMER_ID", "");
			sysHead.addDataField("SERVICE_SCENE", "");
			sysHead.addDataField("MODULE_ID", "");
			sysHead.addDataField("ORG_SYS_ID", SFConst.SYS_SYSID);
			sysHead.addDataField("ESB_SEQ_NO","");
			sysHead.addDataField("PROGRAM_ID", "");
			sysHead.addDataField("CONSUMER_SEQ_NO", "");
			sysHead.addDataField("CONSUMER_SVR_ID", "");
			sysHead.addDataField("WS_ID", "");
			sysHead.addDataField("TRAN_DATE", "");
			sysHead.addDataField("TRAN_TIMESTAMP", "");
			sysHead.addDataField("USER_LANG", "");
			sysHead.addDataField("FILE_PATH", "");
			sysHead.addDataField("RET_STATUS", "");
			context.addDataElement(sysHead);
		}else{
			sysHead = (KeyedCollection)context.getDataElement("SYS_HEAD");
//			sysHead.setDataValue("SERVICE_CODE", "");
//			sysHead.setDataValue("CONSUMER_ID", "");
//			sysHead.setDataValue("SERVICE_SCENE", "");
//			sysHead.setDataValue("MODULE_ID", "");
			if(!sysHead.containsKey("ORG_SYS_ID")){
				sysHead.addDataField("ORG_SYS_ID", SFConst.SYS_SYSID);
			}else{
				sysHead.setDataValue("ORG_SYS_ID", SFConst.SYS_SYSID);
			}
//			sysHead.setDataValue("ESB_SEQ_NO","");
//			sysHead.setDataValue("PROGRAM_ID", "");
//			sysHead.setDataValue("CONSUMER_SEQ_NO", "");
//			sysHead.setDataValue("CONSUMER_SVR_ID", "");
//			sysHead.setDataValue("WS_ID", "");
//			sysHead.setDataValue("TRAN_DATE", "");
//			sysHead.setDataValue("TRAN_TIMESTAMP", "");
//			sysHead.setDataValue("USER_LANG", "");
//			sysHead.setDataValue("FILE_PATH", "");
//			sysHead.setDataValue("RET_STATUS", "");
		}
		if(!context.containsKey("RET_STATUS")){
			context.addDataField("RET_STATUS", "");
		}else{
			context.setDataValue("RET_STATUS", "");
		}
	}
	/**
	 * ����������Ӧ��ͷ
	 * @param context
	 * @return
	 * @throws EMPException
	 */
	private void getAppHead(Context context) throws EMPException{
		KeyedCollection appHead = null;		
		String staffUm = null,  branchId = null;
		
		
		if(!context.containsKey("APP_HEAD")){
			appHead = new KeyedCollection("APP_HEAD");
			appHead.addDataField("BRANCH_ID", branchId);
			appHead.addDataField("USER_ID", staffUm);
			appHead.addDataField("USER_PASSWORD", "");
			appHead.addDataField("USER_LEVEL", "");
			appHead.addDataField("USER_TYPE", "");
			appHead.addDataField("APPR_FLAG", "");
			appHead.addDataField("AUTH_FLAG", "");
			appHead.addDataField("BIZ_SEQ_NO", "");
			appHead.addDataField("REVERSAL_DATE", "");
			appHead.addDataField("REVERSAL_SEQ_NO", "");
			appHead.addDataField("REVERSAL_BIZ_SEQ_NO", "");
			appHead.addDataField("QUERY_KEY", "");
			appHead.addDataField("PGUP_OR_PGDN", "");
			appHead.addDataField("PER_PAGE_NUM", "");
			appHead.addDataField("TRANT_FLAG", "");
			context.addDataElement(appHead);
		}else{
			appHead = (KeyedCollection)context.getDataElement("APP_HEAD");
			
			
			staffUm = (String)context.getDataValue("APP_HEAD.USER_ID");
			if (SFUtil.isEmpty(staffUm))
				staffUm = "VIRTU";
			appHead.setDataValue("USER_ID", staffUm);
			
			
			branchId = (String)context.getDataValue("APP_HEAD.BRANCH_ID");
			if (SFUtil.isEmpty(branchId))
				branchId = "9998";
			appHead.setDataValue("BRANCH_ID", branchId);
			
//			appHead = (KeyedCollection)objFieldKeyColl;
//			
//			appHead.setDataValue("BRANCH_ID", branchId);
//			appHead.setDataValue("USER_ID", staffUm);
//			appHead.setDataValue("USER_PASSWORD", "");
//			appHead.setDataValue("USER_LEVEL", "");
//			appHead.setDataValue("USER_TYPE", "");
//			appHead.setDataValue("APPR_FLAG", "");
//			appHead.setDataValue("AUTH_FLAG", "");
//			appHead.setDataValue("BIZ_SEQ_NO", "");
//			appHead.setDataValue("REVERSAL_DATE", "");
//			appHead.setDataValue("REVERSAL_SEQ_NO", "");
//			appHead.setDataValue("REVERSAL_BIZ_SEQ_NO", "");
//			appHead.setDataValue("QUERY_KEY", "");
//			appHead.setDataValue("PGUP_OR_PGDN", "");
//			appHead.setDataValue("PER_PAGE_NUM", "");
//			appHead.setDataValue("TRANT_FLAG", "");
		}
	}
	
	/**
	 * ESB����Ӧ��ͷ
	 * @param context
	 * @return
	 * @throws Exception
	 */
//	private void getAppHeadUp(Context context) throws EMPException{
//		KeyedCollection appHeadkey = null;
//		if(!context.containsKey("APP_HEAD_UP")){
//			appHeadkey = new KeyedCollection("APP_HEAD_UP");
//			appHeadkey.addDataField("BRANCH_ID","");
//			appHeadkey.addDataField("USER_ID","");
//			appHeadkey.addDataField("USER_PASSWORD","");
//			appHeadkey.addDataField("USER_LEVEL","");
//			appHeadkey.addDataField("USER_TYPE","");
//			appHeadkey.addDataField("APPR_FLAG","");
//			appHeadkey.addDataField("AUTH_FLAG","");
//			appHeadkey.addDataField("BIZ_SEQ_NO","");
//			appHeadkey.addDataField("REVERSAL_DATE","");
//			appHeadkey.addDataField("REVERSAL_SEQ_NO","");
//			appHeadkey.addDataField("QUERY_KEY","");
//			appHeadkey.addDataField("PGUP_OR_PGDN","");
//			appHeadkey.addDataField("PER_PAGE_NUM","");
//			context.addDataElement(appHeadkey);
//		}else{
//			//�Ȼ�ȡKEYCOLL���������ֱ�ӳ�ʼ��
//			Object objFieldKeyColl = context.getDataElement("APP_HEAD_UP");
//			appHeadkey = (KeyedCollection)objFieldKeyColl;
//			appHeadkey.setDataValue("BRANCH_ID","");
//			appHeadkey.setDataValue("USER_ID","");
//			appHeadkey.setDataValue("USER_PASSWORD","");
//			appHeadkey.setDataValue("USER_LEVEL","");
//			appHeadkey.setDataValue("USER_TYPE","");
//			appHeadkey.setDataValue("APPR_FLAG","");
//			appHeadkey.setDataValue("AUTH_FLAG","");
//			appHeadkey.setDataValue("BIZ_SEQ_NO","");
//			appHeadkey.setDataValue("REVERSAL_DATE","");
//			appHeadkey.setDataValue("REVERSAL_SEQ_NO","");
//			appHeadkey.setDataValue("QUERY_KEY","");
//			appHeadkey.setDataValue("PGUP_OR_PGDN","");
//			appHeadkey.setDataValue("PER_PAGE_NUM","");
//		}
//	}
	
	/**
	 * ����Ӧ�ñ���ͷ
	 * @param context
	 * @return
	 * @throws Exception
	 */
//	private void getAppHeadDown(Context context) throws EMPException{
//		KeyedCollection appHeadDown = null;
//		if(!context.containsKey("APP_HEAD_DOWN")){
//			appHeadDown = new KeyedCollection("APP_HEAD_DOWN");
//			appHeadDown.addDataField("BRANCH_ID","");
//			appHeadDown.addDataField("USER_ID","");
//			appHeadDown.addDataField("USER_PASSWORD","");
//			appHeadDown.addDataField("USER_LEVEL","");
//			appHeadDown.addDataField("USER_TYPE","");
//			appHeadDown.addDataField("APPR_FLAG","");
//			appHeadDown.addDataField("AUTH_FLAG","");
//			appHeadDown.addDataField("BIZ_SEQ_NO","");
//			appHeadDown.addDataField("REVERSAL_DATE","");
//			appHeadDown.addDataField("REVERSAL_SEQ_NO","");
//			appHeadDown.addDataField("SERV_SEQ_NO","");
//			appHeadDown.addDataField("PGUP_OR_PGDN","");
//			appHeadDown.addDataField("RESULT_NUM","");
//			appHeadDown.addDataField("TOTAL_NUM","");
//			appHeadDown.addDataField("END_FLAG","");
//			context.addDataElement(appHeadDown);
//		}else{
//			//�Ȼ�ȡKEYCOLL���������ֱ�ӳ�ʼ��
//			Object objFieldKeyColl = context.getDataElement("APP_HEAD_DOWN");
//			appHeadDown = (KeyedCollection)objFieldKeyColl;
//			appHeadDown.setDataValue("BRANCH_ID","");
//			appHeadDown.setDataValue("USER_ID","");
//			appHeadDown.setDataValue("USER_PASSWORD","");
//			appHeadDown.setDataValue("USER_LEVEL","");
//			appHeadDown.setDataValue("USER_TYPE","");
//			appHeadDown.setDataValue("APPR_FLAG","");
//			appHeadDown.setDataValue("AUTH_FLAG","");
//			appHeadDown.setDataValue("BIZ_SEQ_NO","");
//			appHeadDown.setDataValue("REVERSAL_DATE","");
//			appHeadDown.setDataValue("REVERSAL_SEQ_NO","");
//			appHeadDown.setDataValue("SERV_SEQ_NO","");
//			appHeadDown.setDataValue("PGUP_OR_PGDN","");
//			appHeadDown.setDataValue("RESULT_NUM","");
//			appHeadDown.setDataValue("TOTAL_NUM","");
//			appHeadDown.setDataValue("END_FLAG","");
//		}
//	}

	/**
	 * �������ر���ϵͳ����ͷ-���ش�������
	 * @param context
	 * @return
	 * @throws EMPException
	 */
	private void getRet(Context context) throws EMPException{
		IndexedCollection retIcoll = null;
		KeyedCollection retKeyColl = null;
		if(context.containsKey("RET")){
			retIcoll = (IndexedCollection)context.getDataElement("RET");
			for (Object objKeyColl : retIcoll) {
				retKeyColl = (KeyedCollection)objKeyColl;
				retKeyColl.setDataValue("RET_CODE", "");
				retKeyColl.setDataValue("RET_MSG", "");
			}
		}else{
			retIcoll = new IndexedCollection("RET");
			retKeyColl = new KeyedCollection("RET");
			retKeyColl.addDataField("RET_CODE","");
			retKeyColl.addDataField("RET_MSG","");
			retIcoll.addDataElement(retKeyColl);
			context.addDataElement(retIcoll);
		}
	}
}