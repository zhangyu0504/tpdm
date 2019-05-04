package module.trans.cobank2sf;

import module.bean.AgtAgentInfo;
import module.dao.AgtAgentInfoDao;
import module.trans.TranBase;

import com.ecc.emp.core.Context;
import common.exception.SFException;
import common.sql.dao.DBHandler;
import common.util.SFUtil;

import core.log.SFLogger;

/**
 * ����Ϊ�����з����ʽ�Ԥ����ѯ/����
 * @author ex_kjkfb_songshimin
 * tran_code:300204
 */
public class T300204 extends TranBase {

	@Override
	protected void initialize( Context context ) throws SFException {}

	@Override
	public void doHandle( Context context ) throws SFException {
		// ���»��ѯ�ʽ�Ԥ����Ϣ
		SFLogger.info( context, String.format( "updOrQryAgtAgentInfo()��ʼ" ) );
		updOrQryAgtAgentInfo( context );
		SFLogger.info( context, String.format( "updOrQryAgtAgentInfo()����" ) );
	}

	/**
	 * �����ʽ�Ԥ����Ϣ
	 * @param context
	 * @throws SFException
	 */
	private void updOrQryAgtAgentInfo( Context context ) throws SFException {
		String flag = SFUtil.getReqDataValue( context, "FLAG" );// ��������
		String warnFlag = null;
		String warnMoney = null;
		String bankId = SFUtil.getDataValue( context, "A_REQUEST_HEAD.BANKNO" );// ���������ĵ�û�����ָ��ֶ�
		AgtAgentInfo agtAgentInfo = new AgtAgentInfo();
		AgtAgentInfoDao agtAgentDao = new AgtAgentInfoDao();
		try {
			if( "1".equals( flag ) ) {// ����Ԥ��
				warnFlag = SFUtil.getReqDataValue( context, "WARN_FLAG" );// Ԥ������
				warnMoney = SFUtil.getReqDataValue( context, "WARN_MONEY" );// Ԥ�����
				if( !"1".equals( warnFlag ) && !"2".equals( warnFlag ) ) {
					SFUtil.chkCond( context, "ST5702", String.format( "[Ԥ������[%s]�Ƿ�", warnFlag ) );

				}
				DBHandler.beginTransaction( context, tranConnection );// ��������1
				agtAgentInfo.setBankId( bankId );
				agtAgentInfo.setWarnFlag( warnFlag );
				agtAgentInfo.setWarnMoney( warnMoney );
				agtAgentDao.saveAgtAgentInfo( context, tranConnection, agtAgentInfo );
				DBHandler.commitTransaction( context, tranConnection );

			} else if( "2".equals( flag ) ) {// ��ѯԤ��
				agtAgentInfo.setBankId( bankId );
				agtAgentInfo = agtAgentDao.qryAgtAgentInfo( context, tranConnection, bankId );
				warnFlag = agtAgentInfo.getWarnFlag();
				warnMoney = agtAgentInfo.getWarnMoney();
			}

			/**
			 * �鷵�ؽڵ����
			 */
			SFUtil.setResDataValue( context, "WARN_FLAG", warnFlag );
			SFUtil.setResDataValue( context, "WARN_MONEY", warnMoney );
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}

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
		String flag = SFUtil.getReqDataValue( context, "FLAG" );// ��������
		SFUtil.chkCond( context, !"1".equals( flag ) && !"2".equals( flag ), "ST5702", String.format( "[��������[%s]]�Ƿ�", flag ) );

	}

	@Override
	protected void chkEnd( Context context ) throws SFException {
		// TODO Auto-generated method stub

	}
}