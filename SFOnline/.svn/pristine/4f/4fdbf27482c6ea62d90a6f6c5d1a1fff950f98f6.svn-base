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
 * 直联劵商转发给管理台
 */
public class ZLSecuTranspondAction  {
	
	
	
	/**
	 * 实际发送报文
	 * @param context
	 * @param msg
	 * @param msgCode
	 * @param serviceCode
	 * @return
	 * @throws SFException
	 */
	public Context send(Context context,String msgCode,String serviceCode)throws SFException{
		/*
		 * 克隆新的通信上下文
		 */
		try{
			String secCompCode = SFUtil.getReqDataValue( context, "SEC_COMP_CODE" );// 券商流水号
			if(SFUtil.isNotEmpty(secCompCode)){
				Connection tranConnection = SFUtil.getDataValue( context, SFConst.PUBLIC_TRAN_CONNECTION );
				SecCompDataDao secCompDataDao = new SecCompDataDao();
				SecCompData secCompData = secCompDataDao.qrySecCompData( context, tranConnection, secCompCode );
				SFUtil.setDataValue( context, SFConst.PUBLIC_SECU, secCompData ); // 在上下文中存入券商对象
				if(null != secCompData) {
					SFUtil.setDataValue( context, SFConst.PUBLIC_SECU_TYPE, secCompData.getSztFlag() );// 将券商类型放入上下文中
				}				
			}
			
			
			/*
			 * 发送报文
			 */
			SFSendEsbAction SFSendEsbAction = new SFSendEsbAction();
			SFSendEsbAction.setHostId("PAESB");
			SFSendEsbAction.setHostInterfaceClass("core.communication.esbinterface.EsbStandardMessage");
			
			SFSendEsbAction.setTrxCode(msgCode);
			//失败是否冲正
			SFSendEsbAction.setAcctInterfaceFlag(false);
			// 修改抛出异常
			SFSendEsbAction.setThrowExceptionFlag(false);
			SFSendEsbAction.setSaveInDatabase(false);
			
			SFSendEsbAction.setTcpipServiceName("tcpipservice_os390");
			SFSendEsbAction.setServiceCode(serviceCode);
			// 发送ESB
			SFSendEsbAction.execute(context);
		}catch(Exception e){
			/*
			 * 设置返回值
			 */
			IndexedCollection iColl=SFUtil.getDataElement(context,"RET");
			KeyedCollection kColl=(KeyedCollection)iColl.getElementAt(0);
			String retCode=SFUtil.getDataValue(context,kColl,"RET_CODE");//从响应报文中取响应码
			if(SFConst.RESPCODE_TIMEOUT_ESB.equals(retCode)){//超时
				SFUtil.setDataValue(context,SFConst.PUBLIC_RET_FLAG, SFConst.RET_OVERTIME);
			}else{
				SFUtil.setDataValue(context,SFConst.PUBLIC_RET_FLAG, SFConst.RET_FAILURE);//增加errorMSG参数
			}
			return context;
		}
		/*
		 * 设置返回值
		 */
		IndexedCollection iColl=SFUtil.getDataElement(context,"RET");
		KeyedCollection kColl=(KeyedCollection)iColl.getElementAt(0);
		String retCode=SFUtil.getDataValue(context,kColl,"RET_CODE");//从响应报文中取响应码
		
		if(SFConst.RESPCODE_SUCCCODE_ESB.equals(retCode)
				||SFConst.RESPCODE_SUCCCODE_ZLSECU.equals(retCode)){//成功
			SFUtil.setDataValue(context,SFConst.PUBLIC_RET_FLAG, SFConst.RET_SUCCESS);
			
			/*
			 * 接收券商清算文件转换成功码为M0001
			 */
			String secTxCode =SFUtil.getDataValue( context, "ZLSECU_REQUEST_HEAD.TXCODE" );// 券商交易码
			if("6166".equals(secTxCode)||"6167".equals(secTxCode)
					||"6168".equals(secTxCode)||"6169".equals(secTxCode)){
				String secMagCode=SFUtil.getDataValue(context,SFConst.CTX_ERRCODE);
				if(SFUtil.isEmpty(secMagCode)||SFConst.RESPCODE_SUCCCODE_ZLSECU.equals(secMagCode)){
					SFUtil.setDataValue(context,SFConst.CTX_ERRCODE,"M0001");
					SFUtil.setDataValue(context,kColl,"RET_CODE","M0001");//从响应报文中取响应码
				}
			}
		}else if(!SFConst.RESPCODE_SUCCCODE_ESB.equals(retCode)&&!SFConst.RESPCODE_TIMEOUT_ESB.equals(retCode)){//失败
			SFUtil.setDataValue(context,SFConst.PUBLIC_RET_FLAG, SFConst.RET_FAILURE);//增加errorMSG参数
		}else if(SFConst.RESPCODE_TIMEOUT_ESB.equals(retCode)){//超时
			SFUtil.setDataValue(context,SFConst.PUBLIC_RET_FLAG, SFConst.RET_OVERTIME);
		}
		return context;
	}
}
