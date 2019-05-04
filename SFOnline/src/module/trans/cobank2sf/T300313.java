package module.trans.cobank2sf;

import module.bean.AgtAgentBranch;
import module.bean.AgtAgentInfo;
import module.dao.AgtAgentBranchDao;
import module.dao.AgtAgentInfoDao;
import module.trans.TranBase;

import com.ecc.emp.core.Context;
import common.exception.SFException;
import common.sql.dao.DBHandler;
import common.util.DateUtil;
import common.util.SFUtil;

import core.log.SFLogger;

/**
 * ����Eͨ����̨����)��ɾ�Ĳ����������
 * @author ex_kjkfb_songshimin
 * tran_code:300313
 */
public class T300313 extends TranBase {
	private AgtAgentInfoDao agtAgentInfoDao = new AgtAgentInfoDao();
	
	@Override
	protected void initialize( Context context ) throws SFException {}

	@Override
	public void doHandle( Context context ) throws SFException {
		SFLogger.info( context, String.format( "qryAndAdd()��ʼ" ) );
		qryAndAdd( context );
		SFLogger.info( context, String.format( "qryAndAdd()����" ) );
	}

	private void qryAndAdd( Context context ) throws SFException {
		String tranType = SFUtil.getReqDataValue( context, "TRAN_TYPE" );
		String bankId = SFUtil.getReqDataValue( context, "BANK_ID" );
		String bankName = SFUtil.getReqDataValue( context, "BANK_NAME" );
		String branchCode = SFUtil.getReqDataValue( context, "BRANCH_CODE" );
		String bankAcct = SFUtil.getReqDataValue( context, "BANK_ACCT" );
		String bankIp = SFUtil.getReqDataValue( context, "BANK_IP" );
		String bankPort = SFUtil.getReqDataValue( context, "BANK_PORT" );
		String fsIp = SFUtil.getReqDataValue( context, "FS_IP" );
		String fsPort = SFUtil.getReqDataValue( context, "FS_PORT" );
		String ftpUser = SFUtil.getReqDataValue( context, "FTP_USER" );
		String ftpPass = SFUtil.getReqDataValue( context, "FTP_PASS" );
		String ftpPath = SFUtil.getReqDataValue( context, "FTP_PATH" );
		String status = SFUtil.getReqDataValue( context, "STATUS" );
		AgtAgentInfo agtAgentInfo = new AgtAgentInfo();
		AgtAgentBranch agtAgentBranch = new AgtAgentBranch();

		try {
			DBHandler.beginTransaction( context, tranConnection );// ��������1
			AgtAgentBranchDao agtAgentBranchDao = new AgtAgentBranchDao();
			// ��������Ϊ���������޸�
			if( "0".equals( tranType ) || "2".equals( tranType ) ) {
				// ����
				if( "0".equals( tranType ) ) {
					agtAgentInfo.setBankId( bankId );
					agtAgentInfo = agtAgentInfoDao.qryAgtAgentInfo( context, tranConnection, bankId );
					SFUtil.chkCond( context, agtAgentInfo != null, "SF0014", String.format( "������[%s]�Ѿ����ڣ������ظ�����", bankId ) );
					// ������������Ϣ
					agtAgentInfoDao.saveAgtAgentInfo( context, tranConnection, getAgtAgentInfo( context ) );
					// ���������л�����Ϣ
					agtAgentBranchDao.saveAgtAgentBranch( context, tranConnection, getAgtAgentBranch( context ) );
				} else if( "2".equals( tranType ) ) {// �޸�
					// ���������Ƿ����
					agtAgentInfo = agtAgentInfoDao.qryAgtAgentInfoByMacFlag( context, tranConnection, bankId );
					SFUtil.chkCond( context, agtAgentInfo == null, "ST5715", String.format( "�����к�[%s]������", bankId ) );
					agtAgentInfo.setBankName( bankName );
					agtAgentInfo.setBranchCode( branchCode );
					agtAgentInfo.setBankAcct( bankAcct );
					agtAgentInfo.setBankIp( bankIp );
					agtAgentInfo.setBankPort( bankPort );
					agtAgentInfo.setFsIp( fsIp );
					agtAgentInfo.setFsPort( fsPort );
					agtAgentInfo.setFtpUser( ftpUser );
					agtAgentInfo.setFtpPass( ftpPass );
					agtAgentInfo.setDesflag( "0" );
					agtAgentInfo.setAgentPath( ftpPath );
					agtAgentInfo.setStatus( status );
					agtAgentInfoDao.updAgtAgentInfo( context, tranConnection, agtAgentInfo );
				}

			} else if( "1".equals( tranType ) ) {// ��������Ϊ��ѯ
				// ���������Ƿ����
				agtAgentInfo = agtAgentInfoDao.qryAgtAgentInfoByMacFlag( context, tranConnection, bankId );
				SFUtil.chkCond( context, agtAgentInfo == null, "ST5715", String.format( "�����к�[%s]������", bankId ) );
				// ��װ���ر���
				SFUtil.setResDataValue( context, "BANK_NAME", agtAgentInfo.getBankName() );
				SFUtil.setResDataValue( context, "BRANCH_CODE", agtAgentInfo.getBranchCode() );
				SFUtil.setResDataValue( context, "BANK_ACCT", agtAgentInfo.getBankAcct() );
				SFUtil.setResDataValue( context, "BANK_IP", agtAgentInfo.getBankIp() );
				SFUtil.setResDataValue( context, "BANK_PORT", agtAgentInfo.getBankPort() );
				SFUtil.setResDataValue( context, "FS_IP", agtAgentInfo.getFsIp() );
				SFUtil.setResDataValue( context, "FS_PORT", agtAgentInfo.getFsPort() );
				SFUtil.setResDataValue( context, "FTP_USER", agtAgentInfo.getFtpUser() );
				SFUtil.setResDataValue( context, "FTP_PASS", agtAgentInfo.getFtpPass() );
				SFUtil.setResDataValue( context, "FTP_PATH", agtAgentInfo.getAgentPath() );
				SFUtil.setResDataValue( context, "STATUS", agtAgentInfo.getStatus() );
				SFUtil.setResDataValue( context, "OPEN_DATE", agtAgentInfo.getOpenDate() );
			} else if( "3".equals( tranType ) ) {// ��������Ϊɾ��
				// ���������Ƿ����
				agtAgentInfo = agtAgentInfoDao.qryAgtAgentInfoByMacFlag( context, tranConnection, bankId );
				SFUtil.chkCond( context, agtAgentInfo == null, "ST5715", String.format( "�����к�[%s]������", bankId ) );
				agtAgentInfoDao.delAgtAgentInfoByMacFlag( context, tranConnection, bankId );
				agtAgentBranch.setBankId( bankId );
				agtAgentBranchDao.delAgtAgentBranchByBankId( context, tranConnection, bankId );
			}
			DBHandler.commitTransaction( context, tranConnection );// �ύ����1
			/**
			 * �鷵�ؽڵ����
			 */
			SFUtil.setResDataValue( context, "TRAN_TYPE", tranType );
			SFUtil.setResDataValue( context, "BANK_ID", bankId );
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}
	}

	/**
	 * ��������Ϣ��ֵ
	 * @param context
	 * @return
	 * @throws SFException
	 */
	private AgtAgentInfo getAgtAgentInfo( Context context ) throws SFException {
		AgtAgentInfo agtAgentInfo = new AgtAgentInfo();
		agtAgentInfo.setBankId( SFUtil.objectToString( SFUtil.getReqDataValue( context, "BANK_ID" ) ) );
		agtAgentInfo.setBankName( SFUtil.objectToString( SFUtil.getReqDataValue( context, "BANK_NAME" ) ) );
		agtAgentInfo.setBranchCode( SFUtil.objectToString( SFUtil.getReqDataValue( context, "BRANCH_CODE" ) ) );
		agtAgentInfo.setBankAcct( SFUtil.objectToString( SFUtil.getReqDataValue( context, "BANK_ACCT" ) ) );
		agtAgentInfo.setWarnFlag( "1" );
		agtAgentInfo.setWarnMoney( "0" );
		agtAgentInfo.setBankIp( SFUtil.objectToString( SFUtil.getReqDataValue( context, "BANK_IP" ) ) );
		agtAgentInfo.setBankPort( SFUtil.objectToString( SFUtil.getReqDataValue( context, "BANK_PORT" ) ) );
		agtAgentInfo.setFtpFlag( "0" );
		agtAgentInfo.setFsIp( SFUtil.objectToString( SFUtil.getReqDataValue( context, "FS_IP" ) ) );
		agtAgentInfo.setFsPort( SFUtil.objectToString( SFUtil.getReqDataValue( context, "FS_PORT" ) ) );
		agtAgentInfo.setFtpUser( SFUtil.objectToString( SFUtil.getReqDataValue( context, "FTP_USER" ) ) );
		agtAgentInfo.setFtpPass( SFUtil.objectToString( SFUtil.getReqDataValue( context, "FTP_PASS" ) ) );
		agtAgentInfo.setAgentPath( SFUtil.objectToString( SFUtil.getReqDataValue( context, "FTP_PATH" ) ) );
		agtAgentInfo.setMackey( "212C4D54769A98C7" );
		agtAgentInfo.setPinkey( "1AA212CB453E4A89" );
		agtAgentInfo.setPinFlag( "2" );
		agtAgentInfo.setStatus( "0" );
		agtAgentInfo.setOpenDate( DateUtil.getMacDate() );
		agtAgentInfo.setMacFlag( "0" );
		return agtAgentInfo;
	}

	/**
	 * �����л�����Ϣ��ֵ
	 * @param context
	 * @return
	 * @throws SFException
	 */
	private AgtAgentBranch getAgtAgentBranch( Context context ) throws SFException {
		AgtAgentBranch agtAgentBranch = new AgtAgentBranch();
		agtAgentBranch.setBankId( SFUtil.objectToString( SFUtil.getReqDataValue( context, "BANK_ID" ) )  );
		agtAgentBranch.setBranchId( "0001" );
		agtAgentBranch.setBranchName( SFUtil.objectToString( SFUtil.getReqDataValue( context, "BANK_NAME" ) ) );
		agtAgentBranch.setFatherBranch( "FFFF" );
		agtAgentBranch.setStatus( "0" );
		agtAgentBranch.setOpenDate( DateUtil.getMacDate() );
		return agtAgentBranch;
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
		String tranType = SFUtil.getReqDataValue( context, "TRAN_TYPE" );
		SFUtil.chkCond( context, !"0".equals( tranType ) && !"1".equals( tranType ) && !"2".equals( tranType ) && !"3".equals( tranType ), "ST5702", String.format( "[��������[%s]]�Ƿ�", tranType ) );
	}

	@Override
	protected void chkEnd( Context context ) throws SFException {
		// TODO Auto-generated method stub

	}
}