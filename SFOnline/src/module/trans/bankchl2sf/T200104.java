package module.trans.bankchl2sf;

import java.util.List;

import module.bean.InvestData;
import module.bean.SecCompData;
import module.bean.SignAccountData;
import module.trans.TranBase;

import com.ecc.emp.core.Context;
import common.exception.SFException;
import common.sql.dao.DBHandler;
import common.util.SFConst;
import common.util.SFUtil;

import core.log.SFLogger;

/**
 * (������������) �˻���Ϣ�޸�
 * �����룺200104
 * @author ����
 *
 */
public class T200104 extends TranBase {

	/**
	 * ��ʼ��BIZ����
	 * @param context
	 * @throws SFException
	 */
	public void initialize(Context context) throws SFException{
		
	}
	@Override
	public void doHandle(Context context) throws SFException {
		//�޸Ŀͻ���Ϣ
		SFLogger.info(context, String.format("updInvestData()��ʼ"));
		updInvestData(context);
		SFLogger.info(context, String.format("updInvestData()����"));
	}

	private void updInvestData(Context context) throws SFException {
		//String initSide = SFUtil.getReqDataValue(context, "INIT_SIDE");//����
		String invType = SFUtil.getReqDataValue(context, "INV_TYPE");//�ͻ�����
		String acctId = SFUtil.getReqDataValue(context, "ACCT_ID");//����
		String capAcct = SFUtil.getReqDataValue(context, "CAP_ACCT");//�ʽ��ʺ�
		String emailAddr = SFUtil.getReqDataValue(context, "EMAIL_ADDR");//Email
		String mobile = SFUtil.getReqDataValue(context, "MOBILE");//�ֻ�����
		String fax = SFUtil.getReqDataValue(context, "FAX");//����
		String tel = SFUtil.getReqDataValue(context, "PHONE");//��ϵ�绰
		String zip = SFUtil.getReqDataValue(context, "ZIP");//�ʱ�
		String legalName = SFUtil.getReqDataValue(context, "LEGAL_NAME");//���˴�������
		String remark = SFUtil.getReqDataValue(context, "REMARK");//��ע
		String addRess = SFUtil.getReqDataValue(context, "ADDR");//��ַ
		try {
			DBHandler.beginTransaction(context, tranConnection);//��������
			/*String address = null;
		    String trnName = null;
		    String trnIdType = null;
		    String trnIdCode = null;
		    String trnPhone = null;
		    String trnMobile = null;*/
			/*if (SFConst.CTX_PUBLIC_INIT_SIDE_ABBANK.equals(initSide)) {
				address = SFUtil.getReqDataValue(context, "Address1");
				address+=SFUtil.getReqDataValue(context, "Address2");
				address+=SFUtil.getReqDataValue(context, "Address3");
				address+=SFUtil.getReqDataValue(context, "Address4");
				if (SFConst.CTX_PUBLIC_INV_TYPE_CORP.equals(invType)){
					//��������Ϣ
					trnName = SFUtil.getReqDataValue(context, "TrnName1");
					trnName += SFUtil.getReqDataValue(context, "TrnName2");
					trnIdType =  SFUtil.getReqDataValue(context, "TRN_ID_TYPE");//��Ȩ������֤������
					trnIdCode =  SFUtil.getReqDataValue(context, "TRN_ID_CODE");//��Ȩ������֤������
					trnPhone =  SFUtil.getReqDataValue(context, "TrnPhone");
					trnMobile =  SFUtil.getReqDataValue(context, "TrnMobile");
				}
			} else {
				address = SFUtil.getReqDataValue(context, "Address1");
				if (SFConst.CTX_PUBLIC_INV_TYPE_CORP.equals(invType)){
					trnName = SFUtil.getReqDataValue(context, "TRN_NAME");//��Ȩ����������
				}
			}*/

	   	   InvestData investData = new InvestData();
	   	   /*investData.setTrnName(trnName);
	   	   investData.setTrnIdType(trnIdType);
	   	   investData.setTrnIdCode(trnIdCode);
	   	   investData.setTrnPhone(trnPhone);
	   	   investData.setTrnMobile(trnMobile);
	   	   investData.setAddr(address);*/
	   	   investData.setAddr(addRess);
	   	   investData.setInvType(invType);
	   	   investData.setZip(zip);
	   	   investData.setPhone(tel);
	   	   investData.setFax(fax);
	   	   investData.setMobile(mobile);
	   	   investData.setEmailAddr(emailAddr);
	   	   investDataDao.updInvestDataByAcctId(context, tranConnection, investData, acctId);
	   	   DBHandler.commitTransaction(context, tranConnection);
	   	  
	   	   // ���׳ɹ����鷵�ر���
	   	   SFUtil.setResDataValue(context, "ACCT_ID", acctId);
	   	   SFUtil.setResDataValue(context, "CAP_ACCT", capAcct);
	   	   SFUtil.setResDataValue(context, "PHONE", tel);
	   	   SFUtil.setResDataValue(context, "MOBILE", mobile);
	   	   SFUtil.setResDataValue(context, "FAX", fax);
	   	   SFUtil.setResDataValue(context, "ZIP", zip);
	   	   SFUtil.setResDataValue(context, "EMAIL_ADDR", emailAddr);
	   	   SFUtil.setResDataValue(context, "ADDR", "");
	   	   SFUtil.setResDataValue(context, "TRN_NAME", "");
	   	   SFUtil.setResDataValue(context, "LEGAL_NAME", legalName);
	   	   SFUtil.setResDataValue(context, "REMARK", remark);
		} catch (SFException e){
			throw e;
		} catch (Exception e ){
			SFUtil.chkCond(context,"ST4895",String.format("����ʧ��%s",e.getMessage()));
		}
	
	}
	@Override
	public void doHost(Context context) throws SFException {

	}


	@Override
	public void doSecu(Context context) throws SFException {

	}


	@Override
	protected void chkStart(Context context) throws SFException {

	}

	@Override
	protected void chkEnd(Context context) throws SFException {
		List<SignAccountData> signAccountDataList = signAccountDataDao.qrySignAccountDataListBySignFlag(context, tranConnection,(String)SFUtil.getReqDataValue(context, "ACCT_ID"));
   	   	SFUtil.chkCond(context, signAccountDataList == null||signAccountDataList.size() == 0, "ST5720",String.format("�ͻ�ǩԼ��Ϣ������"));
   	   	String secCompCode = signAccountDataList.get(0).getSecCompCode();
   	   	SecCompData secCompData = secCompDataDao.qrySecCompData(context,tranConnection, secCompCode);
   	   	SFUtil.setDataValue(context, SFConst.PUBLIC_SECU,secCompData); // ���������д���ȯ�̶���
   	   //���ȯ���ڸ�ʱ��β���������
   	//   SFUtil.chkCond(context,!SecuService.chkSecu724(context, tranConnection),"ST4014", String.format("ȯ���ڸ�ʱ��β���������"));
	}

}