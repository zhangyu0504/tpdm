package module.trans.cobank2sf;

import module.bean.AgtAgentBranch;
import module.bean.AgtCustomerInfo;
import module.dao.AgtAgentBranchDao;
import module.dao.AgtCustomerInfoDao;
import module.trans.TranBase;

import com.ecc.emp.core.Context;
import common.exception.SFException;
import common.sql.dao.DBHandler;
import common.util.SFUtil;

import core.log.SFLogger;

/**
 * ����Ϊ�����з����޸Ŀͻ���������
 * @author ex_kjkfb_songshimin
 * ������:300120
 */
public class T300120 extends TranBase {

	private AgtCustomerInfoDao agtCustomerInfoDao = new AgtCustomerInfoDao();
	@Override
	protected void initialize( Context context ) throws SFException {}

	@Override
	public void doHandle( Context context ) throws SFException {
		// ���¿ͻ���������
		SFLogger.info( context, String.format( "updAgtCustomerInfo()��ʼ" ) );
		updAgtCustomerInfo( context );
		SFLogger.info( context, String.format( "updAgtCustomerInfo()����" ) );

	}

	@Override
	public void doHost( Context context ) throws SFException {
		// TODO Auto-generated method stub

	}

	@Override
	public void doSecu( Context context ) throws SFException {
		// TODO Auto-generated method stub

	}

	@Override
	protected void chkStart( Context context ) throws SFException {
		String secCompCode = SFUtil.getReqDataValue( context, "SEC_COMP_CODE" );
		String capAcct = SFUtil.getReqDataValue( context, "CAP_ACCT" );
		String openBranch = SFUtil.getReqDataValue( context, "OPEN_BRANCH_ID" );
		AgtCustomerInfo agtCustomerInfo = new AgtCustomerInfo();
		AgtAgentBranch agtAgentBranch = new AgtAgentBranch();
		try {
			agtCustomerInfo = agtCustomerInfoDao.qryAgtCustomerInfo( context, tranConnection, capAcct, secCompCode );
			SFUtil.chkCond( context, agtCustomerInfo == null, "ST5041", String.format( "�ÿͻ������ڸú�����" ) );
			String bankId = agtCustomerInfo.getBankId();

			// ������������Ƿ����ڸú�����
			AgtAgentBranchDao agtAgentBranchDao = new AgtAgentBranchDao();
			agtAgentBranch = agtAgentBranchDao.qryAgtAgentBranch( context, tranConnection, bankId, openBranch );
			SFUtil.chkCond( context, agtAgentBranch == null, "ST5705", String.format( "[��������[%s]]������", openBranch ) );
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}

	}

	/**
	 * �������ݲ���
	 * @param context
	 * @throws SFException
	 */
	private void updAgtCustomerInfo( Context context ) throws SFException {
		String secCompCode = SFUtil.getReqDataValue( context, "SEC_COMP_CODE" );
		String capAcct = SFUtil.getReqDataValue( context, "CAP_ACCT" );
		String openBranch = SFUtil.getReqDataValue( context, "OPEN_BRANCH_ID" );
		String bankId = SFUtil.getDataValue( context, "A_REQUEST_HEAD.BANKNO" );
		try {
			DBHandler.beginTransaction( context, tranConnection );// ��������1
			agtCustomerInfoDao.updAgtCustomerInfoByBankId( context, tranConnection, capAcct, secCompCode, bankId, openBranch );
			DBHandler.commitTransaction( context, tranConnection );// �ύ����1
			/**
			 * �鷵�ؽڵ����
			 */
			SFUtil.setResDataValue( context, "SEC_COMP_CODE", secCompCode );
			SFUtil.setResDataValue( context, "CAP_ACCT", capAcct );
			SFUtil.setResDataValue( context, "OPEN_BRANCH_ID", openBranch );
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}
	}

	@Override
	protected void chkEnd( Context context ) throws SFException {
		// TODO Auto-generated method stub

	}

}