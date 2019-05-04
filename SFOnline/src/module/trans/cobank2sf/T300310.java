package module.trans.cobank2sf;

import java.util.List;

import com.ecc.emp.core.Context;
import com.ecc.emp.data.IndexedCollection;
import com.ecc.emp.data.KeyedCollection;

import common.exception.SFException;
import common.util.SFUtil;
import core.log.SFLogger;
import module.bean.AgtAgentInfo;
import module.dao.AgtAgentInfoDao;
import module.trans.TranBase;

/**
 * ����Eͨ����̨����)��ѯ��������Ϣ
 * tran code :300310
 * @author ex_kjkfb_zhumingtao
 *
 */
public class T300310 extends TranBase {
	private AgtAgentInfoDao agtAgentInfoDao = new AgtAgentInfoDao();
	/**
	 * ��ʼ��BIZ����
	 * @param context
	 * @throws SFException
	 */
	public void initialize(Context context) throws SFException{

	}	
	
	@Override
	public void doHandle(Context context) throws SFException {		
		SFLogger.info( context, String.format("qryAgtAgentInfo()��ʼ") );
		qryAgtAgentInfo(context);	//��ѯ��������Ϣ
		SFLogger.info( context, String.format("qryAgtAgentInfo()����") );
	}

	@Override
	public void doHost(Context context) throws SFException {
		// TODO Auto-generated method stub

	}

	@Override
	public void doSecu(Context context) throws SFException {
		// TODO Auto-generated method stub

	}

	@Override
	protected void chkStart(Context context) throws SFException {
		SFLogger.info(context,String.format("chkStart()��ʼ"));
		String bankId = SFUtil.getReqDataValue(context, "BANK_ID");		//�����к�BankId
		try{
			if(SFUtil.isEmpty(bankId)){	
				long intCount = agtAgentInfoDao.qryCountAgtAgentInfoList(context, tranConnection);
				if(intCount>99){
					SFUtil.chkCond(context, "", String.format("��ѯȫ������������IntCount:[%s]����",intCount));
				}			
			}else{//����bankId��ѯǩԼ��ϵ
				List<AgtAgentInfo> list = agtAgentInfoDao.qryAgtAgentInfoList(context, tranConnection,bankId);		
				SFUtil.chkCond(context, list.size()==0||list==null, "ST5705", "�ú�������Ϣ������");
			}		
			SFLogger.info(context,String.format("chkStart()����"));		
		} catch (SFException e){
			throw e;
		} catch (Exception e ){
			SFUtil.chkCond(context,"ST4895",String.format("����ʧ��",e.getMessage()));
		}		
	}

	@Override
	protected void chkEnd(Context context) throws SFException {
		// TODO Auto-generated method stub

	}

	private void qryAgtAgentInfo(Context context) throws SFException{
		String bankId = SFUtil.getReqDataValue(context, "BANK_ID");		//�����к�BankId
		try {				
			List<AgtAgentInfo> list = agtAgentInfoDao.qryAgtAgentInfoList(context, tranConnection,bankId);		
			if(list==null||list.size()==0){				
				return;
			}
			IndexedCollection indexColl = SFUtil.getDataElement(context, "300310_O_ICOLL");
			for (AgtAgentInfo agtInfo : list) {
				//��װ��������
				KeyedCollection keyColl = new KeyedCollection();
				SFUtil.addDataField(context,keyColl, "BANK_ID", agtInfo.getBankId());		
				SFUtil.addDataField(context,keyColl, "BRANCH_NAME", agtInfo.getBankName());
				SFUtil.addDataField(context,keyColl, "BRANCH_CODE", agtInfo.getBranchCode());
				SFUtil.addDataField(context,keyColl, "BANK_ACCT", agtInfo.getBankAcct());
				SFUtil.addDataField(context,keyColl, "STATUS", agtInfo.getStatus());
				SFUtil.addDataField(context,keyColl, "OPEN_DATE", agtInfo.getOpenDate());
				indexColl.add(keyColl);
			}
		} catch (SFException e){
			throw e;
		} catch (Exception e ){
			SFUtil.chkCond(context,"ST4895",String.format("����ʧ��",e.getMessage()));
		}			
	}	
	
}