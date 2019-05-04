package module.trans.bankchl2sf;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import module.bean.SignAccountData;
import module.trans.TranBase;

import com.ecc.emp.core.Context;
import common.exception.SFException;
import common.sql.dao.DBHandler;
import common.util.BizUtil;
import common.util.SFConst;
import common.util.SFUtil;

import core.log.SFLogger;

/**
 * (������������) ����/ȡ����״̬��
 *  �����룺200103
 * @author ����
 * 
 */
public class T200103 extends TranBase {

	private String acctId = null;//����
	private String functionCode = null;//������
	/**
	 * ��ʼ��BIZ����
	 * @param context
	 * @throws SFException
	 */
	public void initialize(Context context) throws SFException {

	}

	@Override
	public void doHandle(Context context) throws SFException {
		//��ʼ������
		SFLogger.info(context, String.format("doHost()��ʼ"));
		doHost(context);
		SFLogger.info(context, String.format("doHost()����"));
	}

	@Override
	public void doHost(Context context) throws SFException {
		try {
			String branchId = null;//���������
			DBHandler.beginTransaction(context, tranConnection);// ��������
			if( SFConst.INV_TYPE_CORP.equals( SFUtil.getReqDataValue( context, "INV_TYPE" ) ) ) {//�Թ��ͻ�ͨ��C3011�ӿڲ�ѯ�ͻ���Ϣ
				/**************************************************************************
				 *                    ��������ѯ��״̬��ʼ
				 ***************************************************************************/
				Map<String,Object> msg=new HashMap<String,Object>();
				msg.put("ACCT_ID",acctId);//�˺�ACCT_NO
				Context msgContext = BizUtil.qryCardAttrClient(context, msg);
				branchId = SFUtil.getDataValue(msgContext,"MSG_O.OPEN_DEP_ID");//���������
				/**********************��������ѯ��״̬�ֽ���********************************/
			}
			// ����������״̬��
			BizUtil.setCardStatusWord(context,acctId,"O".equals(functionCode) ? "A" : "D",branchId);//������ A-����
			//�������سɹ�
			if ("O".equals(functionCode)){//������״̬��
				signAccountDataDao.updSignAccountDataByAcctId(context,tranConnection, acctId, "1");
			} else {//ȡ����״̬��
				signAccountDataDao.updSignAccountDataByAcctId(context,tranConnection, acctId, "0");
			}
			DBHandler.commitTransaction(context, tranConnection);
			
			// ���׳ɹ����鷵�ر���
			//if(SFConst.INV_TYPE_RETAIL.equals(SFUtil.getReqDataValue( context, "INV_TYPE" ))){
				SFUtil.setResDataValue(context, "ACCT_ID", acctId);//�Թ����۶���Ҫ����
			//}
			SFUtil.setResDataValue(context, "FUNCTION_CODE", "O".equals(functionCode)?"����":"ȡ��");
		} catch (SFException e) {
			throw e;
		} catch (Exception e) {
			SFUtil.chkCond(context, "ST4895",String.format("����ʧ��%s", e.getMessage()));
		} 

	}

	@Override
	public void doSecu(Context context) throws SFException {

	}

	@Override
	protected void chkStart(Context context) throws SFException {
		functionCode = SFUtil.getReqDataValue(context, "FUNCTION_CODE");// ������
		// ������Ϸ��Լ��
		SFUtil.chkCond(context,!"O".equals(functionCode) && !"F".equals(functionCode),"ST5702", String.format("[������]�Ƿ�"));
	}

	@Override
	protected void chkEnd(Context context) throws SFException {
		acctId = SFUtil.getReqDataValue(context, "ACCT_ID");// ����
		//���ÿ�״̬�ּ��
		List<SignAccountData> signList = signAccountDataDao.qrySignAccountDataListByAcctId(context, tranConnection,acctId, "");
		if ("O".equals(functionCode)) {
			//δ����ǩԼ��ϵ, ���ܽ�����״̬��
			SFUtil.chkCond(context,signList.size() == 0, "ST5611",String.format("������������״̬��ʧ��[��/�˻���δ����ǩԼ��ϵ]"));
		} else {
			//�Ѿ�����ǩԼ��ϵ, ����ȡ����״̬��
			SFUtil.chkCond(context,signList.size() > 0, "ST5611",String.format("������ȡ����״̬��ʧ��[��/�˻��Ѿ�����ǩԼ��ϵ]"));
		}
	}

}