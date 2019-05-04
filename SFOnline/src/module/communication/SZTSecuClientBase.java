package module.communication;

import java.util.Map;

import module.bean.SecCompData;

import com.ecc.emp.core.Context;
import com.ecc.emp.data.KeyedCollection;
import common.exception.SFException;
import common.util.SFConst;
import common.util.SFUtil;

import core.cache.CacheMap;
import core.communication.access.stzsecu.SZTSendAction;
import core.log.SFLogger;

/**
 * ��֤ͨ���Ѷ˴�����
 * @author ����
 *
 */
public final class SZTSecuClientBase{
	public Context send(Context msgContext,Map<String,Object>msg,String msgCode)throws SFException{
		/*
		 * ��¡�µ�ͨ��������
		 */
		//Context msgContext=SFUtil.cloneMsgContext(context, msg);
		try{
			/*
//			 * �����Ļ�ȡ��ǰȯ����Ϣ
//			 */
			SecCompData secCompData =SFUtil.getDataValue(msgContext,SFConst.PUBLIC_SECU);
			//�ж�ȯ���Ƿ����
			if(secCompData==null){
				SFUtil.setDataValue(msgContext,SFConst.PUBLIC_RET_FLAG, SFConst.RET_FAILURE);
				SFLogger.error(msgContext, String.format("��֤ͨģʽִ��ҵ�����롾%s������ʧ�ܣ�ȯ�̲����ڣ�", msgCode));
				return msgContext;
			}
			/*
			 * ���ͱ���
			 */
			SZTSendAction sender=  new SZTSendAction();
			sender.setSvrCode(msgCode);
			sender.setSecCompCode(secCompData.getSecCompCode());
			String result=sender.doExecute(msgContext);
			if(!"0".equals(result)){
				SFUtil.chkCond(msgContext,SFConst.CTX_ERRCODE_UNKNOWN,SFConst.CTX_ERRMSG_UNKNOWN);
			}
		}catch(Exception e){
			/*
			 * ���в����쳣������ʱ����
			 * <p>
			 * 1���޷���������
			 * 2�����ͳ�ʱ
			 * 3�����ճ�ʱ
			 * 4��SOCKET ��IO�����쳣
			 * 5���������󣨰����޷��������ر��ģ�
			 */
			SFLogger.error(msgContext, String.format("��֤ͨģʽִ��ҵ�����롾%s������ʧ�ܣ�%s", msgCode, e.getMessage()));
//			SFUtil.setDataValue(msgContext,SFConst.PUBLIC_RET_FLAG, SFConst.RET_OVERTIME);
//			SFUtil.setDataValue(msgContext, SFConst.CTX_ERRCODE, SFConst.CTX_ERRCODE_UNKNOWN);
//			SFUtil.setDataValue(msgContext, SFConst.CTX_ERRMSG, SFConst.CTX_ERRMSG_UNKNOWN);
			/*
			 * ���÷���ֵ
			 */
			Map<String,Map<String,String>> tcpipServiceMap =  CacheMap.getCache("SZT_TCPIPSERVICE");
			Map<String,String> tcpipService = tcpipServiceMap.get(msgCode);		
			String outKcollName = tcpipService.get("formatOutput");// ReadProperty.getValue(msgCode+ "_O");		
			outKcollName = outKcollName.replaceAll("\\.", "");
			boolean otherFlag=false;
			if(msgContext.containsKey(outKcollName)){
				KeyedCollection outKcoll = SFUtil.getDataElement(msgContext,outKcollName);
				if(outKcoll!=null&&outKcoll.containsKey("Rst")){
					KeyedCollection rstKcoll = SFUtil.getDataElement(msgContext,outKcoll, "Rst");
					//�����Ӧ���������������
					String retCode=SFUtil.getDataValue(msgContext,rstKcoll,"RESP_CODE");
					String retMsg=SFUtil.getDataValue(msgContext,rstKcoll,"RESP_MSG");
					SFLogger.info( msgContext, String.format( "��֤ͨȯ��Ӧ���롾%s��,Ӧ����Ϣ��%s��", retCode,retMsg ) );
					SFUtil.setDataValue(msgContext, SFConst.CTX_ERRCODE, retCode);
					SFUtil.setDataValue(msgContext, SFConst.CTX_ERRMSG, retMsg);
					if(!SFConst.RESPCODE_SUCCCODE_SZTSECU.equals(retCode)&&!SFConst.RESPCODE_TIMEOUT_SZTSECU.equals(retCode)){//ʧ��
						SFUtil.setDataValue(msgContext,SFConst.PUBLIC_RET_FLAG, SFConst.RET_FAILURE);//����errorMSG����
					}else if(SFConst.RESPCODE_TIMEOUT_SZTSECU.equals(retCode)){//��ʱ
						SFUtil.setDataValue(msgContext,SFConst.PUBLIC_RET_FLAG, SFConst.RET_OVERTIME);
					}else{
						otherFlag=true;
					}
				}else{
					otherFlag=true;
				}
			}else{
				otherFlag=true;
			}
			/*
			 * δ֪������Գ�ʱ����
			 */
			if(otherFlag){
				SFUtil.setDataValue(msgContext,SFConst.PUBLIC_RET_FLAG, SFConst.RET_OVERTIME);
				SFUtil.setDataValue(msgContext, SFConst.CTX_ERRCODE, SFConst.CTX_ERRCODE_UNKNOWN);
				SFUtil.setDataValue(msgContext, SFConst.CTX_ERRMSG, SFConst.CTX_ERRMSG_UNKNOWN);
				//��д����
				KeyedCollection kColl = SFUtil.getDataElement(msgContext,outKcollName);
				if(kColl==null){
					kColl=new KeyedCollection(outKcollName);
					KeyedCollection rstKColl=new KeyedCollection("Rst");
					SFUtil.addDataField(msgContext,rstKColl,"RESP_CODE",SFConst.CTX_ERRCODE_UNKNOWN);
					SFUtil.addDataField(msgContext,rstKColl,"RESP_MSG",SFConst.CTX_ERRMSG_UNKNOWN);
					SFUtil.addDataElement(msgContext, kColl,rstKColl);
					SFUtil.addDataElement(msgContext, kColl);
				}else{
					KeyedCollection rstKColl = SFUtil.getDataElement(msgContext,outKcollName);
					if(rstKColl==null){
						rstKColl=new KeyedCollection("Rst");
						SFUtil.addDataField(msgContext,rstKColl,"RESP_CODE",SFConst.CTX_ERRCODE_UNKNOWN);
						SFUtil.addDataField(msgContext,rstKColl,"RESP_MSG",SFConst.CTX_ERRMSG_UNKNOWN);
						SFUtil.addDataElement(msgContext, kColl,rstKColl);						
					}
					SFUtil.setDataValue(msgContext,rstKColl,"RESP_CODE",SFConst.CTX_ERRCODE_UNKNOWN);
					SFUtil.setDataValue(msgContext,rstKColl,"RESP_MSG",SFConst.CTX_ERRMSG_UNKNOWN);
				}
			}
			return msgContext;
		}
		/*
		 * ���÷���ֵ
		 */
		
		// ����body��Ϣ
		//String tranCode = SFUtil.getDataValue(msgContext, SFConst.CTX_PUBLIC_TX_CODE);
		
		
		Map<String,Map<String,String>> tcpipServiceMap =  CacheMap.getCache("SZT_TCPIPSERVICE");
		Map<String,String> tcpipService = tcpipServiceMap.get(msgCode);		
		String outKcollName = tcpipService.get("formatOutput");// ReadProperty.getValue(msgCode+ "_O");		
		outKcollName = outKcollName.replaceAll("\\.", "");
		KeyedCollection outKcoll = SFUtil.getDataElement(msgContext,outKcollName);		
		KeyedCollection rstKcoll = SFUtil.getDataElement(msgContext,outKcoll, "Rst");
		//�����Ӧ���������������
		String retCode=SFUtil.getDataValue(msgContext,rstKcoll,"RESP_CODE");
		String retMsg=SFUtil.getDataValue(msgContext,rstKcoll,"RESP_MSG");
		if(SFUtil.isNotEmpty(retCode)){
			SFUtil.setDataValue(msgContext, SFConst.CTX_ERRCODE, retCode);
			SFUtil.setDataValue(msgContext, SFConst.CTX_ERRMSG, retMsg);	
		}else{
			SFUtil.setDataValue(msgContext, SFConst.CTX_ERRCODE, SFConst.CTX_ERRCODE_UNKNOWN);
			SFUtil.setDataValue(msgContext, SFConst.CTX_ERRMSG, SFConst.CTX_ERRMSG_UNKNOWN);			
		}
		SFLogger.info( msgContext, String.format( "��֤ͨȯ��Ӧ���롾%s��,Ӧ����Ϣ��%s��", retCode,retMsg ) );
		if(SFConst.RESPCODE_SUCCCODE_SZTSECU.equals(retCode)){//�ɹ�
			SFUtil.setDataValue(msgContext,SFConst.PUBLIC_RET_FLAG, SFConst.RET_SUCCESS);
		}else if(!SFConst.RESPCODE_SUCCCODE_SZTSECU.equals(retCode)&&!SFConst.RESPCODE_TIMEOUT_SZTSECU.equals(retCode)){//ʧ��
			SFUtil.setDataValue(msgContext,SFConst.PUBLIC_RET_FLAG, SFConst.RET_FAILURE);//����errorMSG����
		}else if(SFConst.RESPCODE_TIMEOUT_SZTSECU.equals(retCode)){//��ʱ
			SFUtil.setDataValue(msgContext,SFConst.PUBLIC_RET_FLAG, SFConst.RET_OVERTIME);
		}
		return msgContext;
	}
}
