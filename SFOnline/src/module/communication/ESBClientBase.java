package module.communication;

import java.util.Map;

import com.ecc.emp.core.Context;
import com.ecc.emp.data.IndexedCollection;
import com.ecc.emp.data.KeyedCollection;
import common.exception.SFException;
import common.util.SFConst;
import common.util.SFUtil;

import core.communication.esb.SFSendEsbAction;
import core.log.SFLogger;
/**
 * ��������ESB���Ѷ˴�����
 * @author ����
 *
 */
public abstract class ESBClientBase {
	/**
	 * ���ͱ���ͳһ���
	 * @param context
	 * @param msg
	 * @return
	 * @throws SFException
	 */
	public Context send(Context context,Map<String,Object>msg)throws SFException{
		//˽��ҵ������������װ���뱨�ģ����������볡����
		return doHandle(context,msg);
	}
	
	
	/**
	 * ʵ�ʷ��ͱ���
	 * @param context
	 * @param msg
	 * @param msgCode
	 * @param serviceCode
	 * @return
	 * @throws SFException
	 */
	protected Context send(Context context,Map<String,Object>msg,String msgCode,String serviceCode)throws SFException{
		/*
		 * ��¡�µ�ͨ��������
		 */
		Context msgContext=SFUtil.cloneMsgContext(context, msg);
		try{			
			/*
			 * ���ͱ���
			 */
			SFSendEsbAction SFSendEsbAction = new SFSendEsbAction();
			SFSendEsbAction.setHostId("PAESB");
			SFSendEsbAction.setHostInterfaceClass("core.communication.esbinterface.EsbStandardMessage");
			
			SFSendEsbAction.setTrxCode(msgCode);
			//ʧ���Ƿ����
			SFSendEsbAction.setAcctInterfaceFlag(false);
			// �޸��׳��쳣
			SFSendEsbAction.setThrowExceptionFlag(true);
			SFSendEsbAction.setSaveInDatabase(false);
			
			SFSendEsbAction.setTcpipServiceName("tcpipservice_os390");
			SFSendEsbAction.setServiceCode(serviceCode);
			// ����ESB
			SFSendEsbAction.execute(msgContext);
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
			SFLogger.error(msgContext, String.format("ִ��ESB��%s������ʧ�ܣ�%s", msgCode, e.getMessage()),e);
			/*
			 * ���÷���ֵ
			 */
			IndexedCollection iColl=SFUtil.getDataElement(msgContext,"RET");
			boolean otherFlag=false;
			if(iColl!=null&&iColl.size()>0){
				KeyedCollection kColl=(KeyedCollection)iColl.getElementAt(0);
				//�����Ӧ���������������
				String retCode=SFUtil.getDataValue(msgContext,kColl,"RET_CODE");//����Ӧ������ȡ��Ӧ��
				String retMsg=SFUtil.getDataValue(msgContext,kColl,"RET_MSG");//����Ӧ������ȡ��Ӧ��Ϣ
				SFLogger.info( context, String.format( "����Ӧ���롾%s��,Ӧ����Ϣ��%s��", retCode,retMsg ) );
				SFUtil.setDataValue(msgContext, SFConst.CTX_ERRCODE, retCode);
				SFUtil.setDataValue(msgContext, SFConst.CTX_ERRMSG, retMsg);
				if(SFConst.RESPCODE_TIMEOUT_ESB.equals(retCode)||SFConst.RESPCODE_TIMEOUT_AE0666_ESB.equals(retCode)){//��ʱ
					SFUtil.setDataValue(msgContext,SFConst.PUBLIC_RET_FLAG, SFConst.RET_OVERTIME);
				}else if(SFUtil.isNotEmpty(retCode)&&!SFConst.RESPCODE_SUCCCODE_ESB.equals(retCode)){
					SFUtil.setDataValue(msgContext,SFConst.PUBLIC_RET_FLAG, SFConst.RET_FAILURE);//����errorMSG����
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
				try{					
					KeyedCollection keyColl = new KeyedCollection();
					SFUtil.addDataField(msgContext,keyColl,"RET_CODE", SFConst.CTX_ERRCODE_UNKNOWN);
					SFUtil.addDataField(msgContext,keyColl,"RET_MSG", SFConst.CTX_ERRMSG_UNKNOWN);
					if(iColl==null){
						iColl=new IndexedCollection("RET");
						iColl.add(keyColl);
						SFUtil.addDataElement(msgContext, iColl);
					}else{
						iColl.add(keyColl);	
					}
				}catch(Exception ex){
					SFLogger.error(msgContext, String.format("ִ��ESB��%s������ʧ�ܣ�%s", msgCode, ex.getMessage()),ex);
				}
				SFUtil.setDataValue(msgContext, SFConst.CTX_ERRCODE, SFConst.CTX_ERRCODE_UNKNOWN);
				SFUtil.setDataValue(msgContext, SFConst.CTX_ERRMSG, SFConst.CTX_ERRMSG_UNKNOWN);				
			}
			return msgContext;
		}
		/*
		 * ���÷���ֵ
		 */
		IndexedCollection iColl=SFUtil.getDataElement(msgContext,"RET");
		KeyedCollection kColl=(KeyedCollection)iColl.getElementAt(0);
		//�����Ӧ���������������
		String retCode=SFUtil.getDataValue(msgContext,kColl,"RET_CODE");//����Ӧ������ȡ��Ӧ��
		String retMsg=SFUtil.getDataValue(msgContext,kColl,"RET_MSG");//����Ӧ������ȡ��Ӧ��Ϣ
		SFLogger.info( context, String.format( "����Ӧ���롾%s��,Ӧ����Ϣ��%s��", retCode,retMsg ) );
		SFUtil.setDataValue(msgContext, SFConst.CTX_ERRCODE, retCode);
		SFUtil.setDataValue(msgContext, SFConst.CTX_ERRMSG, retMsg);
		
		if(SFConst.RESPCODE_SUCCCODE_ESB.equals(retCode)){//�ɹ�
			SFUtil.setDataValue(msgContext,SFConst.PUBLIC_RET_FLAG, SFConst.RET_SUCCESS);
		}else if(!SFConst.RESPCODE_SUCCCODE_ESB.equals(retCode)&&!SFConst.RESPCODE_TIMEOUT_ESB.equals(retCode)){//ʧ��
			SFUtil.setDataValue(msgContext,SFConst.PUBLIC_RET_FLAG, SFConst.RET_FAILURE);//����errorMSG����
		}else if(SFConst.RESPCODE_TIMEOUT_ESB.equals(retCode)||SFConst.RESPCODE_TIMEOUT_AE0666_ESB.equals(retCode)){//��ʱ
			SFUtil.setDataValue(msgContext,SFConst.PUBLIC_RET_FLAG, SFConst.RET_OVERTIME);
		}
		return msgContext;
	}
	
	/**
	 * ˽��ҵ������������װ���뱨�ģ����������볡����
	 * @param context
	 * @param msgCode
	 * @param serviceCode
	 * @return
	 * @throws SFException
	 */
	protected abstract Context doHandle(Context context,Map<String,Object>msg)throws SFException;
	
}