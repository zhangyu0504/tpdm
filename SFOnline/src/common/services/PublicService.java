package common.services;

import java.sql.Connection;

import module.cache.ParamCache;

import com.ecc.emp.core.Context;
import com.ecc.emp.core.EMPException;
import com.ecc.emp.data.IndexedCollection;
import com.ecc.emp.data.KeyedCollection;
import com.ecc.emp.service.EMPService;
import common.action.db.SqlExecAction;
import common.exception.SFException;
import common.sql.dao.DaoBase;
import common.util.BizUtil;
import common.util.SFConst;
import common.util.SFUtil;

import core.communication.access.tcpip.BBCPCommClient;
import core.log.SFLogger;

/**
 * 
 * <b>����������</b><br>
 * FBS������������ṩ���ִ��ϵͳ�Ļ�����������<br>
 * <b>�����б���</b><br>
 * &lt;name="getSQLExecAction"&gt;&lt;function="��ȡSql���ִ�е�Action"&gt;<br>
 * @version 1.0
 * @modifier
 * 
 */

public class PublicService extends EMPService {

	/**
	 * �²���һ��������SQL���ִ�ж���.
	 * 
	 * @param context
	 * @throws EMPException
	 */
	public SqlExecAction getSQLExecAction(Context context)
			throws EMPException {
		SqlExecAction sqlExecAction = null;

		sqlExecAction = new SqlExecAction();
		sqlExecAction.setDataSource((String) context
				.getDataValue(SFConst.SERVICE_DATASOURCE));
		sqlExecAction.setSqlService((String) context
				.getDataValue(SFConst.SERVICE_SQL));

		return sqlExecAction;
	}
	
	/**
	 * ����Ƿ���ڵ���
	 * @param context
	 * @param type
	 * @param serverId
	 * @param receiveNo
	 * @param timeOut
	 * @return
	 * @throws SFException
	 */
	public boolean hasBaffle(Context context,String type,String serverId,String receiveNo,int timeOut)throws SFException{
		String isOn= ParamCache.getValue2("BAFFLE","ON-OFF");//�����ַ
		if(!"1".equals(isOn)){
			return false;
		}
		Connection connection = SFUtil.getDataValue(context, SFConst.PUBLIC_TRAN_CONNECTION);
		String tranCode = SFUtil.getDataValue(context, SFConst.PUBLIC_TX_CODE);//���׺�
		/*
		 * ���㵱ǰ�����Ƿ���ڵ���
		 */
		String sql = "SELECT COUNT(T.TRANCODE) AS NUM FROM TRDBAFFLE T WHERE T.TRANCODE = ? AND T.TYPE = ?  AND T.SERVERID = ? AND (? IS NULL OR T.RECEIVENO = ?) AND T.CONDITION = '1'";
		long baffleCount = DaoBase.qryCount(context, connection, sql, tranCode,type,serverId,receiveNo,receiveNo);
		if(baffleCount<=0){
			return false;
		}
		return true;		
	}
	
	/**
	 * ת������
	 * @param context
	 * @param type
	 * @param serverId
	 * @param receiveNo
	 * @param timeOut
	 * @return
	 * @throws SFException
	 */
	public String baffle(Context context,String type,String serverId,String receiveNo,int timeOut)throws SFException{
//		/*
//		 * ����Ƿ���ڵ���
//		 */
//		if(!this.hasBaffle(context, type, serverId, receiveNo, timeOut)){
//			return null;
//		}
		SFLogger.info(context, "���ڱ��ĵ���");
		String tranCode = SFUtil.getDataValue(context, SFConst.PUBLIC_TX_CODE);//���׺�
		//��ȡ������
		String macCode=BizUtil.getMacCode(context);
		String baffleIP=ParamCache.getValue2("BAFFLE","BAFFLE_IP");//�����ַ
		String bafflePort=ParamCache.getValue2("BAFFLE","BAFFLE_PORT_"+macCode);//����˿�
		receiveNo=SFUtil.isEmpty(receiveNo)?" ":receiveNo;
		
		//��������
		StringBuffer message=new StringBuffer();
		message.append(SFUtil.fixChar(tranCode, 20, ' ',"right"));
		message.append(SFUtil.fixChar(serverId, 20, ' ',"right"));
		message.append(type);
		message.append(SFUtil.fixChar(receiveNo, 20, ' ',"right"));
		message.append(SFUtil.fixChar(String.valueOf(timeOut), 6, ' ',"right"));
		
		//�ַ�������ת�����ֽ�����
		byte[] toSendMessage = new byte[message.length()+10];
		toSendMessage = ("0073BF0001"+message.toString()).getBytes();
		
		BBCPCommClient client =  new BBCPCommClient(baffleIP,bafflePort,timeOut,timeOut,0);				
		SFLogger.info(context, "��ʼ���󵲰����......");
		String ret = client.SendCMD(context,toSendMessage);
		
		if(!"0".equals(ret)){
			SFUtil.setDataValue(context,SFConst.PUBLIC_RET_FLAG, SFConst.RET_OVERTIME);//�ֹ����ó�ʱ
			if("1".equals(type)){  //esb
				IndexedCollection iColl=SFUtil.getDataElement(context,"RET");
				KeyedCollection kColl=(KeyedCollection)iColl.getElementAt(0);
				SFUtil.setDataValue(context,kColl,"RET_CODE", SFConst.RESPCODE_TIMEOUT_ESB);
				SFUtil.setDataValue(context,kColl,"RET_MSG","������Ӧ����ʧ��");
			}
//					else if("2".equals(type)){  //������
//						SFUtil.setDataValue(context,"A_RESPONSE_HEAD.RESPCODE", SFConst.RESPCODE_TIMEOUT_COBANK);
//						SFUtil.setDataValue(context,"A_RESPONSE_HEAD.RESPMSG", "��������Ӧ����ʧ��");
//					}else if("3".equals(type)){ //ֱ��
//						String outputName = serverId + "_O";
//						FormatElement outputFormat = context.getFormat(outputName);
//						KeyedFormat tranCodeO = (KeyedFormat)outputFormat.getFormatElement();
//						String outKcollName = tranCodeO.getKcollName();
//						SFUtil.setDataValue(context,outKcollName+".RESP_CODE", SFConst.RESPCODE_TIMEOUT_ZLSECU);
//						SFUtil.setDataValue(context,outKcollName+".RESP_MSG","ȯ����Ӧ����ʧ��");
//					}else if("4".equals(type)){    //��֤ͨ
//						Map<String,Map<String,String>> tcpipServiceMap =  CacheMap.getCache("SZT_TCPIPSERVICE");
//						Map<String,String> tcpipService = tcpipServiceMap.get(serverId);		
//						String outKcollName = tcpipService.get("formatOutput");// ReadProperty.getValue(msgCode+ "_O");		
//						outKcollName = outKcollName.replaceAll("\\.", "");
//						KeyedCollection outKcoll = SFUtil.getDataElement(context,outKcollName);		
//						KeyedCollection rstKcoll = SFUtil.getDataElement(context,outKcoll, "Rst");
//						SFUtil.setDataValue(context,rstKcoll,"RESP_CODE", SFConst.RESPCODE_TIMEOUT_SZTSECU);
//						SFUtil.setDataValue(context,rstKcoll,"RESP_MSG","��֤ͨ��Ӧ���ĳ�ʱ");
//					}
			throw new SFException("YYPTERROR9999","����ͨ�Ŵ���"+ret);	
		}
		
		String msgText = client.getResult();
		SFLogger.info(context, "���巵�ر�������Ϊ��"+msgText);
		return msgText;
	}
}