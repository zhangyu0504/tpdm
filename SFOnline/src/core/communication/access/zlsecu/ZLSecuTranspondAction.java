package core.communication.access.zlsecu;

import java.sql.Connection;

import module.bean.SecCompData;
import module.dao.SecCompDataDao;

import com.ecc.emp.core.Context;
import com.ecc.emp.data.IndexedCollection;
import com.ecc.emp.data.KeyedCollection;
import common.exception.SFException;
import common.util.SFConst;
import common.util.SFUtil;

import core.communication.esb.SFSendEsbAction;



/**
 * ֱ������ת��������̨
 */
public class ZLSecuTranspondAction  {
	
	
	
	/**
	 * ʵ�ʷ��ͱ���
	 * @param context
	 * @param msg
	 * @param msgCode
	 * @param serviceCode
	 * @return
	 * @throws SFException
	 */
	public Context send(Context context,String msgCode,String serviceCode)throws SFException{
		/*
		 * ��¡�µ�ͨ��������
		 */
		try{
			String secCompCode = SFUtil.getReqDataValue( context, "SEC_COMP_CODE" );// ȯ����ˮ��
			if(SFUtil.isNotEmpty(secCompCode)){
				Connection tranConnection = SFUtil.getDataValue( context, SFConst.PUBLIC_TRAN_CONNECTION );
				SecCompDataDao secCompDataDao = new SecCompDataDao();
				SecCompData secCompData = secCompDataDao.qrySecCompData( context, tranConnection, secCompCode );
				SFUtil.setDataValue( context, SFConst.PUBLIC_SECU, secCompData ); // ���������д���ȯ�̶���
				if(null != secCompData) {
					SFUtil.setDataValue( context, SFConst.PUBLIC_SECU_TYPE, secCompData.getSztFlag() );// ��ȯ�����ͷ�����������
				}				
			}
			
			
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
			SFSendEsbAction.setThrowExceptionFlag(false);
			SFSendEsbAction.setSaveInDatabase(false);
			
			SFSendEsbAction.setTcpipServiceName("tcpipservice_os390");
			SFSendEsbAction.setServiceCode(serviceCode);
			// ����ESB
			SFSendEsbAction.execute(context);
		}catch(Exception e){
			/*
			 * ���÷���ֵ
			 */
			IndexedCollection iColl=SFUtil.getDataElement(context,"RET");
			KeyedCollection kColl=(KeyedCollection)iColl.getElementAt(0);
			String retCode=SFUtil.getDataValue(context,kColl,"RET_CODE");//����Ӧ������ȡ��Ӧ��
			if(SFConst.RESPCODE_TIMEOUT_ESB.equals(retCode)){//��ʱ
				SFUtil.setDataValue(context,SFConst.PUBLIC_RET_FLAG, SFConst.RET_OVERTIME);
			}else{
				SFUtil.setDataValue(context,SFConst.PUBLIC_RET_FLAG, SFConst.RET_FAILURE);//����errorMSG����
			}
			return context;
		}
		/*
		 * ���÷���ֵ
		 */
		IndexedCollection iColl=SFUtil.getDataElement(context,"RET");
		KeyedCollection kColl=(KeyedCollection)iColl.getElementAt(0);
		String retCode=SFUtil.getDataValue(context,kColl,"RET_CODE");//����Ӧ������ȡ��Ӧ��
		
		if(SFConst.RESPCODE_SUCCCODE_ESB.equals(retCode)
				||SFConst.RESPCODE_SUCCCODE_ZLSECU.equals(retCode)){//�ɹ�
			SFUtil.setDataValue(context,SFConst.PUBLIC_RET_FLAG, SFConst.RET_SUCCESS);
			
			/*
			 * ����ȯ�������ļ�ת���ɹ���ΪM0001
			 */
			String secTxCode =SFUtil.getDataValue( context, "ZLSECU_REQUEST_HEAD.TXCODE" );// ȯ�̽�����
			if("6166".equals(secTxCode)||"6167".equals(secTxCode)
					||"6168".equals(secTxCode)||"6169".equals(secTxCode)){
				String secMagCode=SFUtil.getDataValue(context,SFConst.CTX_ERRCODE);
				if(SFUtil.isEmpty(secMagCode)||SFConst.RESPCODE_SUCCCODE_ZLSECU.equals(secMagCode)){
					SFUtil.setDataValue(context,SFConst.CTX_ERRCODE,"M0001");
					SFUtil.setDataValue(context,kColl,"RET_CODE","M0001");//����Ӧ������ȡ��Ӧ��
				}
			}
		}else if(!SFConst.RESPCODE_SUCCCODE_ESB.equals(retCode)&&!SFConst.RESPCODE_TIMEOUT_ESB.equals(retCode)){//ʧ��
			SFUtil.setDataValue(context,SFConst.PUBLIC_RET_FLAG, SFConst.RET_FAILURE);//����errorMSG����
		}else if(SFConst.RESPCODE_TIMEOUT_ESB.equals(retCode)){//��ʱ
			SFUtil.setDataValue(context,SFConst.PUBLIC_RET_FLAG, SFConst.RET_OVERTIME);
		}
		return context;
	}
}
