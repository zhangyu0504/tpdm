package module.service;

import java.sql.SQLException;

import module.bean.InvestData;
import module.bean.SecCompData;
import module.bean.SignAccountData;

import com.ecc.emp.core.Context;
import common.exception.SFException;
import common.util.BizUtil;
import common.util.SFConst;
import common.util.SFUtil;

import core.log.SFLogger;

/**
 * �ͻ�������鹫����
 * 
 */
public class InvestorService {

	/**
	 * Ͷ�������ݼ��
	 * 
	 * @param context
	 * @throws SFException
	 */
	public static void chkInvestor( Context context ) throws SFException {
		SFLogger.info( context, String.format( "chkInvestor()��ʼ" ) );
		SFLogger.info( context, String.format( "Ͷ�������ݼ��" ) );

		try {
			InvestData investData = SFUtil.getDataValue( context, SFConst.PUBLIC_INVEST_DATA );
			SFUtil.chkCond( context, investData == null, "ST4392", "Ͷ���˻�����Ϣ������" );

			String idType = SFUtil.getDataValue( context, SFConst.PUBLIC_ID_TYPE );
			String idCode = SFUtil.getReqDataValue( context, "INV_ID_CODE" );

			SFLogger.info( context, String.format( "����֤������:[%s]֤������:[%s]; �������֤������:[%s]֤������[%s]", idType,idCode, investData.getIdType(),investData.getInvIdCode() ) );
			// ���֤�����͡�֤�����룬����֤��15/18λ����
			if( SFConst.ID_TYPE_PERSON_SFZ.equals( idType ) && SFUtil.isNotEmpty(idCode) && idCode.length() == 15 ) {
				String invIdCode18Card19 = BizUtil.converTo18Card19( idCode ); // 15λת18λ֤������,20����
				String invIdCode18Card20 = BizUtil.converTo18Card20( idCode ); // 15λת18λ֤�����룬21����
				SFLogger.info( context, String.format( "����ת��18λ֤�����룺20����[%s]21����[%s]",invIdCode18Card19, invIdCode18Card20 ) );
				SFUtil.chkCond( context, !investData.getIdType().equals( idType ) || (!investData.getInvIdCode().equals( invIdCode18Card19 )&&!investData.getInvIdCode().equals( invIdCode18Card20 )&&!investData.getInvIdCode().equals( idCode )), "ST4090", "֤������" );
			} else if (SFConst.ID_TYPE_PERSON_SFZ.equals( idType ) && SFUtil.isNotEmpty(idCode) && idCode.length() == 18 ){
				String subIdCode6 = idCode.substring(0, 6);//��ȡ��6λ
				String subIdCode9 = idCode.substring(8, 17);//��ȡ8-17λ
				String invIdCode15 = subIdCode6+subIdCode9;//���15λ����֤����
				SFLogger.info( context, String.format( "����ת��15λ֤�����룺[%s]", invIdCode15 ) );
				SFUtil.chkCond( context, !investData.getIdType().equals( idType ) || (!investData.getInvIdCode().equals( invIdCode15 )&&!investData.getInvIdCode().equals( idCode )), "ST4090", "֤������" );
			} else {
				SFUtil.chkCond( context, !investData.getIdType().equals( idType ) || !investData.getInvIdCode().equals( idCode ), "ST4090", "֤������" );
			}
			
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}
		SFLogger.info( context, String.format( "chkInvestor()����" ) );
	}

	/**
	 * �����пͻ��������ڽڼ���ʱ��η���ý���
	 * 
	 * @param context
	 * @return
	 * @throws SFException
	 */
	public static void chkCobankHoliday( Context context ) throws SFException {
		SFLogger.info( context, String.format( "chkCobankHoliday()��ʼ" ) );
		SFLogger.info( context, String.format( "�������пͻ��Ƿ������ڽڼ���ʱ��η���ý���" ) );

		try {
			SignAccountData signAccountData = SFUtil.getDataValue( context, SFConst.PUBLIC_SIGN_ACCOUNT_DATA );
			SFUtil.chkCond( context, signAccountData == null, "ST5720", "�ͻ�ǩԼ��Ϣ������" );
			String initSide = signAccountData.getInitSide();
			String workMode = SFUtil.getDataValue( context, SFConst.PUBLIC_WORKMODE );// ��ǰ����ģʽ
			SFUtil.chkCond( context, workMode.equals( SFConst.WORKMODE_724HOLIDAY ) && SFConst.INIT_SIDE_COBANK.equals( initSide ), "ST5774", "�����пͻ��������ڽڼ���ʱ��η���ý���!" );
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}
		SFLogger.info( context, String.format( "chkCobankHoliday()����" ) );
	}

	/**
	 * �����пͻ���������724ʱ��η���ý���
	 * 
	 * @param context
	 * @throws SFException
	 */
	public static void chkCobank724( Context context ) throws SFException {
		SFLogger.info( context, String.format( "chkCobank724()��ʼ" ) );
		SFLogger.info( context, String.format( "�������пͻ��Ƿ�������724ʱ��η���ý���" ) );
		try {
			SignAccountData signAccountData = SFUtil.getDataValue( context, SFConst.PUBLIC_SIGN_ACCOUNT_DATA );
			SFUtil.chkCond( context, signAccountData == null, "ST5720", "�ͻ�ǩԼ��Ϣ������" );
			String initSide = signAccountData.getInitSide();
			String workMode = SFUtil.getDataValue( context, SFConst.PUBLIC_WORKMODE );// ��ǰ����ģʽ
			SFUtil.chkCond( context, !workMode.equals( SFConst.WORKMODE_NORMAL ) && SFConst.INIT_SIDE_COBANK.equals( initSide ), "ST5774", "�����пͻ���������724ʱ��η���ý���!" );

		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}
		SFLogger.info( context, String.format( "chkCobank724()����" ) );
	}

	/**
	 * �˻�����״̬
	 * 
	 * @return
	 * @throws SQLException
	 */
	public static void chkActiveFlag( Context context ) throws SFException {
		SFLogger.info( context, String.format( "chkActiveFlag()��ʼ" ) );
		SFLogger.info( context, String.format( "�˻�����״̬���" ) );

		try {
			SignAccountData signAccountData = SFUtil.getDataValue( context, SFConst.PUBLIC_SIGN_ACCOUNT_DATA );
			SFUtil.chkCond( context, signAccountData == null, "ST5720", "�ͻ�ǩԼ��Ϣ������" );

			String activeFlag = signAccountData.getActiveFlag();// �����־
			SFUtil.chkCond( context, "1".equals( activeFlag ), "ST5762", "���ȴ����ж˷�����ת֤�����Լ�����ʽ��˺�!" );

		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}

		SFLogger.info( context, String.format( "chkActiveFlag()������" ) );
	}

	/**
	 * �Ƿ�������ȯ�̵Ļ�����������֤����
	 * 
	 * @param context
	 * @return
	 * @throws SQLException
	 */
	public static void chkSecuCorpTrans( Context context ) throws SFException {
		SFLogger.info( context, String.format( "chkSecuCorpTrans()��ʼ" ) );
		SFLogger.info( context, String.format( "����Ƿ�������ȯ�̵Ļ�����������֤����" ) );

		SignAccountData signAccountData = SFUtil.getDataValue( context, SFConst.PUBLIC_SIGN_ACCOUNT_DATA );
		String invType = signAccountData.getInvType();// 1-���˿ͻ� 2-�����ͻ�

		if( SFConst.INV_TYPE_CORP.equals( invType ) ) {// ��ǰ���׿ͻ�Ϊ�����ͻ�ʱ�ż��
			try {
				SecCompData secCompData = SFUtil.getDataValue( context, SFConst.PUBLIC_SECU );
				SFUtil.chkCond( context, secCompData == null, "ST5711", "ȯ����Ϣ������!" );

				String JGZZFlag = secCompData.getJGZZFlag();// �Ƿ�����ȯ�̷��������֤ת�����ױ�־
				String JGZZFlag1 = secCompData.getJGZZFlag1();// �Ƿ�����ȯ�̷����������ת֤���ױ�־
				SFUtil.chkCond( context, !"1".equals( JGZZFlag ) || !"1".equals( JGZZFlag1 ), "ST5773", "��������ȯ�̵Ļ�����������֤����!" );

			} catch( SFException e ) {
				throw e;
			} catch( Exception e ) {
				SFUtil.chkCond( context, "ST4895", e.getMessage() );
			}
		}

		SFLogger.info( context, String.format( "chkSecuCorpTrans()����" ) );
	}

	/**
	 * ���������и��˿ͻ�����ý���
	 * 
	 * @return
	 * @throws SQLException
	 */
	public static void chkBankChlRetailTrans( Context context ) throws SFException {
		SFLogger.info( context, String.format( "chkBankChlRetailTrans()��ʼ" ) );
		SFLogger.info( context, String.format( "����Ƿ��������и��˿ͻ�����ý���" ) );
		try {

			SignAccountData signAccountData = SFUtil.getDataValue( context, SFConst.PUBLIC_SIGN_ACCOUNT_DATA );
			SFUtil.chkCond( context, signAccountData == null, "ST5720", "�ͻ�ǩԼ��Ϣ������" );

			String initSide = signAccountData.getInitSide();// A-�����пͻ� ����-���пͻ�
			String invType = signAccountData.getInvType();// 1-���˿ͻ� 2-�����ͻ�

			if( !SFConst.INIT_SIDE_COBANK.equals( initSide ) && SFConst.INV_TYPE_RETAIL.equals( invType ) )
				SFUtil.chkCond( context, "ST5774", "���������и��˿ͻ�����ý���!" );

		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}
		SFLogger.info( context, String.format( "chkBankChlRetailTrans()����" ) );
	}

	/**
	 * ���������л����ͻ�����ý���
	 * 
	 * @return
	 * @throws SQLException
	 */
	public static void chkCorpTrans( Context context ) throws SFException {
		SFLogger.info( context, String.format( "chkCorpTrans()��ʼ" ) );
		SFLogger.info( context, String.format( "����Ƿ��������л����ͻ�����ý���" ) );
		try {
			SignAccountData signAccountData = SFUtil.getDataValue( context, SFConst.PUBLIC_SIGN_ACCOUNT_DATA );
			SFUtil.chkCond( context, signAccountData == null, "ST5720", "�ͻ�ǩԼ��Ϣ������" );

			String initSide = signAccountData.getInitSide();// A-�����пͻ� ����-���пͻ�
			String invType = signAccountData.getInvType();// 1-���˿ͻ� 2-�����ͻ�
			if( !SFConst.INIT_SIDE_COBANK.equals( initSide ) && SFConst.INV_TYPE_CORP.equals( invType ) )
				SFUtil.chkCond( context, "ST5774", "���������л����ͻ�����˽���!" );

		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}
		SFLogger.info( context, String.format( "chkCorpTrans()����" ) );
	}

	/**
	 * �����������и��˿ͻ�����ý���
	 * 
	 * @return
	 * @throws SQLException
	 */
	public static void chkCobankRetailTrans( Context context ) throws SFException {
		SFLogger.info( context, String.format( "chkCobankRetailTrans()��ʼ" ) );
		SFLogger.info( context, String.format( "����Ƿ����������и��˿ͻ�����ý���" ) );
		try {
			SignAccountData signAccountData = SFUtil.getDataValue( context, SFConst.PUBLIC_SIGN_ACCOUNT_DATA );
			SFUtil.chkCond( context, signAccountData == null, "ST5720", "�ͻ�ǩԼ��Ϣ������" );

			String initSide = signAccountData.getInitSide();// A-�����пͻ� ����-���пͻ�
			String invType = signAccountData.getInvType();// 1-���˿ͻ� 2-�����ͻ�
			if( SFConst.INIT_SIDE_COBANK.equals( initSide ) && SFConst.INV_TYPE_RETAIL.equals( invType ) )
				SFUtil.chkCond( context, "ST5774", "�����������и��˿ͻ�����ý���!" );

		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}
		SFLogger.info( context, String.format( "chkCobankRetailTrans()����" ) );
	}

	/**
	 * �����������л����ͻ�����ý���
	 * 
	 * @return
	 * @throws SQLException
	 */
	public static void chkCobankCorpTrans( Context context ) throws SFException {
		SFLogger.info( context, String.format( "chkCobankCorpTrans()��ʼ" ) );
		SFLogger.info( context, String.format( "����Ƿ����������л����ͻ�����ý���" ) );
		try {
			SignAccountData signAccountData = SFUtil.getDataValue( context, SFConst.PUBLIC_SIGN_ACCOUNT_DATA );
			SFUtil.chkCond( context, signAccountData == null, "ST5720", "�ͻ�ǩԼ��Ϣ������" );

			String initSide = signAccountData.getInitSide();// A-�����пͻ� ����-���пͻ�
			String invType = signAccountData.getInvType();// 1-���˿ͻ� 2-�����ͻ�
			if( SFConst.INIT_SIDE_COBANK.equals( initSide ) && SFConst.INV_TYPE_CORP.equals( invType ) )
				SFUtil.chkCond( context, "ST5774", "�����������л����ͻ�����ý���!" );

		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}
		SFLogger.info( context, String.format( "chkCobankCorpTrans()����" ) );
	}

	/**
	 * ���ǩԼ��ϵsign
	 * 
	 * @return
	 * @throws SQLException
	 */
	public static void chkSignAccount( Context context ) throws SFException {
		SFLogger.info( context, String.format( "chkSignAccount()��ʼ" ) );
		SFLogger.info( context, String.format( "ǩԼ��ϵ���" ) );
		try {
			String acctId = SFUtil.getReqDataValue( context, "ACCT_ID" );// ���п���

			InvestData investData = SFUtil.getDataValue( context, SFConst.PUBLIC_INVEST_DATA );
			SFUtil.chkCond( context, investData == null, "ST4392", "Ͷ���˻�����Ϣ������" );

			SignAccountData signAccountData = SFUtil.getDataValue( context, SFConst.PUBLIC_SIGN_ACCOUNT_DATA );
			SFUtil.chkCond( context, signAccountData == null, "ST5720", "�ͻ�ǩԼ��Ϣ������" );

			String dbAcctId = signAccountData.getAcctId();

			if( SFUtil.isNotEmpty( acctId ) && SFUtil.isNotEmpty( dbAcctId ) ) {
				SFUtil.chkCond( context, !acctId.equals( dbAcctId ), "ST4532", "���Ų�һ��" );
			}
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}

		SFLogger.info( context, String.format( "chkSignAccount()����" ) );
	}

}