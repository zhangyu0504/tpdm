package module.communication;

import java.util.Map;

import module.bean.SecCompData;

import com.ecc.emp.core.Context;
import com.ecc.emp.data.KeyedCollection;
import com.ecc.emp.format.FormatElement;
import common.exception.SFException;
import common.util.SFConst;
import common.util.SFUtil;

import core.communication.access.zlsecu.TCPIPSendAction;
import core.communication.format.KeyedFormat;
import core.log.SFLogger;
/**
 * ֱ��ȯ�����Ѷ˴�����
 * @author ����
 *
 */
public final class ZLSecuClientBase {
	public Context send(Context context,Map<String,Object>msg,String msgCode)throws SFException{
		/*
		 * ��¡�µ�ͨ��������
		 */
		Context msgContext=SFUtil.cloneMsgContext(context, msg);
		try{
			/*
			 * �����Ļ�ȡ��ǰȯ����Ϣ
			 */
			SecCompData secCompData = SFUtil.getDataValue(context,SFConst.PUBLIC_SECU); // (SecCompData)context.getDataValue(SFConst.PUBLIC_SECU);
			//�ж�ȯ���Ƿ����
			if(secCompData==null){
				SFUtil.setDataValue(msgContext,SFConst.PUBLIC_RET_FLAG, SFConst.RET_FAILURE);
				SFLogger.error(msgContext, String.format("ֱ��ȯ��ִ�С�%s������ʧ�ܣ�ȯ�̡������ڣ�", msgCode));
				return msgContext;
			}
			/*
			 * ���ͱ���
			 */
			TCPIPSendAction access=  new TCPIPSendAction();
			access.setHostIP(secCompData.getIp());
			access.setHostPort(secCompData.getPort());
			access.setSvrCode(msgCode);
			access.setSecCompCode(secCompData.getSecCompCode());
			access.setUserId(secCompData.getUserId());
			String result=access.doExecute(msgContext);
			if(!"0".equals(result)){
				SFUtil.chkCond( context,SFConst.CTX_ERRCODE_UNKNOWN,SFConst.CTX_ERRMSG_UNKNOWN);
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
			SFLogger.error(msgContext, String.format("ֱ��ȯ��ִ�С�%s������ʧ�ܣ�%s", msgCode, e.getMessage()));
			SFUtil.setDataValue(msgContext,SFConst.PUBLIC_RET_FLAG, SFConst.RET_OVERTIME);
			/*
			 * ���÷���ֵ
			 */
			String outputName = msgCode + "_O";
			FormatElement outputFormat = context.getFormat(outputName);
			KeyedFormat tranCodeO = (KeyedFormat)outputFormat.getFormatElement();
			String outKcollName = tranCodeO.getKcollName();
			//�����Ӧ���������������
			String retCode=SFUtil.getDataValue(msgContext,outKcollName+".RESP_CODE");
			String retMsg=SFUtil.getDataValue(msgContext,outKcollName+"RESP_MSG");
			SFLogger.info( msgContext, String.format( "ֱ��ȯ��Ӧ���롾%s��,Ӧ����Ϣ��%s��", retCode,retMsg ) );
			String retFlag=SFUtil.getDataValue(context,SFConst.PUBLIC_RET_FLAG);//�ֹ����ó�ʱ
			if(SFUtil.isNotEmpty(retCode)){
				if(retCode.startsWith("M")){//�ɹ�
					SFUtil.setDataValue(msgContext,SFConst.PUBLIC_RET_FLAG, SFConst.RET_SUCCESS);
				}else if(retCode.startsWith("R") || "E1011".equals(retCode)
						|| "E9999".equals(retCode)
						||SFConst.RET_OVERTIME.equals(retFlag)){//��ʱ
					SFUtil.setDataValue(msgContext,SFConst.PUBLIC_RET_FLAG, SFConst.RET_OVERTIME);
					retCode=SFConst.RESPCODE_TIMEOUT_ZLSECU;
					retMsg="ȯ����Ӧ����ʧ��";
				}else{
					SFUtil.setDataValue(msgContext,SFConst.PUBLIC_RET_FLAG, SFConst.RET_FAILURE);//����errorMSG����
				}
			}else{
				SFUtil.setDataValue(msgContext,SFConst.PUBLIC_RET_FLAG, SFConst.RET_OVERTIME);
				retCode=SFConst.CTX_ERRCODE_UNKNOWN;
				retMsg=SFConst.CTX_ERRMSG_UNKNOWN;
				//��д����
				KeyedCollection kColl=SFUtil.getDataElement(msgContext,outKcollName);
				if(kColl==null){
					kColl=new KeyedCollection(outKcollName);
					SFUtil.addDataField(msgContext,kColl,"RESP_CODE",retCode);
					SFUtil.addDataField(msgContext,kColl,"RESP_MSG",retMsg);
					SFUtil.addDataElement(msgContext, kColl);
				}else{
					SFUtil.setDataValue(msgContext,kColl,"RESP_CODE",retCode);
					SFUtil.setDataValue(msgContext,kColl,"RESP_MSG",retMsg);
				}
			}
			SFUtil.setDataValue(msgContext, SFConst.CTX_ERRCODE, retCode);
			SFUtil.setDataValue(msgContext, SFConst.CTX_ERRMSG, retMsg);
			return msgContext;
		}
		/*
		 * ���÷���ֵ
		 */
		String outputName = msgCode + "_O";
		FormatElement outputFormat = context.getFormat(outputName);
		KeyedFormat tranCodeO = (KeyedFormat)outputFormat.getFormatElement();
		String outKcollName = tranCodeO.getKcollName();
		
		if(!msgContext.containsKey( outKcollName )){
			try {
				tranCodeO.addFormatToContext(msgContext);
			} catch( Exception e ) {
				SFLogger.error(msgContext, String.format("ֱ��ȯ��ִ�С�%s������ʧ�ܣ�%s", msgCode, e.getMessage()));
			}
		}
		
//		String tranCode = SFUtil.getDataValue(msgContext, SFConst.PUBLIC_TX_CODE);
		//�����Ӧ���������������
		String retCode=SFUtil.getDataValue(msgContext,outKcollName+".RESP_CODE");
		String retMsg=SFUtil.getDataValue(msgContext,outKcollName+".RESP_MSG");
		SFLogger.info( msgContext, String.format( "ֱ��ȯ��Ӧ���롾%s��,Ӧ����Ϣ��%s��", retCode,retMsg ) );
		String retFlag=SFUtil.getDataValue(context,SFConst.PUBLIC_RET_FLAG);//�ֹ����ó�ʱ
		if(SFUtil.isNotEmpty(retCode)){
			if(retCode.startsWith("M")){//�ɹ�
				SFUtil.setDataValue(msgContext,SFConst.PUBLIC_RET_FLAG, SFConst.RET_SUCCESS);
			}else if(retCode.startsWith("R") 
					|| "E1011".equals(retCode)
					|| "E9999".equals(retCode)
					||SFConst.RET_OVERTIME.equals(retFlag)){//��ʱ
				SFUtil.setDataValue(msgContext,SFConst.PUBLIC_RET_FLAG, SFConst.RET_OVERTIME);
				retCode=SFConst.RESPCODE_TIMEOUT_ZLSECU;
				retMsg="ȯ����Ӧ����ʧ��";
			}else{
				SFUtil.setDataValue(msgContext,SFConst.PUBLIC_RET_FLAG, SFConst.RET_FAILURE);//����errorMSG����
			}
		}else{
			SFUtil.setDataValue(msgContext,SFConst.PUBLIC_RET_FLAG, SFConst.RET_OVERTIME);
			retCode=SFConst.RESPCODE_TIMEOUT_ZLSECU;
			retMsg="ȯ����Ӧ����ʧ��";
		}
		
		SFUtil.setDataValue(msgContext, SFConst.CTX_ERRCODE, retCode);
		SFUtil.setDataValue(msgContext, SFConst.CTX_ERRMSG, retMsg);		
		//ֱ��������ȡ��4λ
		if(SFUtil.isNotEmpty(retCode)){
			SFUtil.setDataValue(msgContext,outKcollName+".RESP_CODE",retCode.length()>3?retCode.substring( retCode.length()-4 ):retCode);
			SFUtil.setDataValue(msgContext,outKcollName+".RESP_MSG",retMsg);

		}
	
		
		
		
		return msgContext;
	}
}