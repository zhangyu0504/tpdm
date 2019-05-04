package module.trans.bankchl2sf;

import module.bean.AcctDetail;
import module.bean.LocalInfo;
import module.bean.Param;
import module.bean.SignAccountData;
import module.cache.ParamCache;
import module.dao.AcctDetailDao;
import module.trans.Page;
import module.trans.TranBase;

import com.ecc.emp.core.Context;
import com.ecc.emp.data.IndexedCollection;
import com.ecc.emp.data.KeyedCollection;
import common.exception.SFException;
import common.util.DateUtil;
import common.util.SFConst;
import common.util.SFUtil;

import core.log.SFLogger;

/**
 * (������������) ��ѯ������ϸ
 * ������ : 200302
 * @author ex_kjkfb_zhumingtao
 *
 */

public class T200302 extends TranBase {

	private SignAccountData signAccountData = null;

	@Override
	public void initialize( Context context ) throws SFException {

	}

	@Override
	public void doHandle( Context context ) throws SFException {

		// ��ѯ������ϸ
		qryAcctDetail( context );

	}

	@Override
	public void doHost( Context context ) throws SFException {

	}

	@Override
	public void doSecu( Context context ) throws SFException {

	}

	@Override
	protected void chkStart( Context context ) throws SFException {}

	@Override
	protected void chkEnd( Context context ) throws SFException {
		try {
			String startDate = SFUtil.getReqDataValue( context, "START_DATE" ); // ��ʼ����
			String endDate = SFUtil.getReqDataValue( context, "END_DATE" ); // ��������
			String acctId = SFUtil.getReqDataValue( context, "ACCT_ID" );// ����
			String capAcct = SFUtil.getReqDataValue( context, "CAP_ACCT" );
			String secCompCode = SFUtil.getReqDataValue( context, "SEC_COMP_CODE" );
			String invType = SFUtil.getReqDataValue( context, "INV_TYPE" );
			LocalInfo localInfo = SFUtil.getDataValue( context, SFConst.PUBLIC_LOCAL_INFO );// ��ȡӪҵʱ����Ϣ
			// �����������Ϊ�գ�������ΪbankDate
			if( SFUtil.isEmpty( endDate ) ) {
				endDate = localInfo.getBankDate();
				if( SFUtil.isEmpty( startDate ) ) {
					startDate = localInfo.getBankDate();
				}
			}
			int days = DateUtil.getBetweenDays( DateUtil.formatToHyphenDate( startDate ), DateUtil.formatToHyphenDate( endDate ) );
			SFUtil.chkCond( context, startDate.length() != 8 || endDate.length() != 8, "ST4012", "��ѯ���ڳ���ֻ��Ϊ8λ" );
			SFUtil.chkCond( context, days > 90, "ST4012", "��ѯ���ڿ�Ȳ��ܳ���90��" );

			/* ��֤ǩԼ��ϵ */
			// ���ۿͻ�ֻ������п��������
			if( SFConst.INV_TYPE_RETAIL.equals( invType ) ) {

				signAccountData = signAccountDataDao.qrySignAccountDataByAcctId( context, tranConnection, acctId );
				SFUtil.chkCond( context, null == signAccountData, "ST5720", "ǩԼ��ϵ������" );

				// �Թ��ͻ������ʽ��˺�+ȯ�̴��룬�����п���
			} else if( SFConst.INV_TYPE_CORP.equals( invType ) ) {

				if( SFUtil.isNotEmpty( capAcct ) && SFUtil.isNotEmpty( secCompCode ) ) {// ѡ�����CapAcct��SecCompCodeҪô�������룬Ҫô��Ҫ����
					signAccountData = signAccountDataDao.qrySignAccountData( context, tranConnection, capAcct, secCompCode, false );
					SFUtil.chkCond( context, null == signAccountData, "ST5720", "�ʽ��ʺ�ǩԼ��ϵ������" );
					if( !signAccountData.getAcctId().equals( acctId ) ) {
						SFUtil.chkCond( context, null == signAccountData, "ST5720", "��������п������ʽ��ʺ�ǩԼ�����п��Ų���" );
					}
				} else {// ����acctId��ѯ�Ƿ�ǩԼ��ϵ
					signAccountData = signAccountDataDao.qrySignAccountDataByAcctId( context, tranConnection, acctId );
					SFUtil.chkCond( context, null == signAccountData, "ST5720", "ǩԼ��ϵ������" );
				}

			}
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", String.format( "����ʧ��", e.getMessage() ) );
		}
	}

	private void qryAcctDetail( Context context ) throws SFException {
		SFLogger.info( context, String.format( "qryAcctDetail()��ʼ" ) );
		try {
			String acctId = SFUtil.getReqDataValue( context, "ACCT_ID" );// ����
			String curCode = SFUtil.getReqDataValue( context, "CUR_CODE" );// ����
			curCode = ( SFUtil.isNotEmpty( curCode ) && !"ALL".equals( curCode ) ) ? curCode : SFConst.CUR_CODE_RMB;
			String secCompCode = SFUtil.getReqDataValue( context, "SEC_COMP_CODE" );// ȯ�̴���
			String capAcct = SFUtil.getReqDataValue( context, "CAP_ACCT" ); // ֤ȯ�ʽ��˺�
			String startDate = SFUtil.getReqDataValue( context, "START_DATE" ); // ��ʼ����
			String endDate = SFUtil.getReqDataValue( context, "END_DATE" ); // ��������
			String strPageNum = SFUtil.getReqDataValue( context, "PAGE_NUM" ); // ��ѯҳ��
			// String pageSize = SFUtil.getDataValue( context, "APP_HEAD.PER_PAGE_NUM" );//ÿҳ��¼��
			Param param = ParamCache.getValue( "SF_PERPAGE_NUM", SFConst.SF_PERPAGE_NUM_BANK );// ÿҳ��¼��
			String pageSize = ( null != param ) ? param.getValue() : "10";
			LocalInfo localInfo = SFUtil.getDataValue( context, SFConst.PUBLIC_LOCAL_INFO );// ��ȡӪҵʱ����Ϣ
			String workMode = SFUtil.getDataValue( context, SFConst.PUBLIC_WORKMODE );
			String workDate = null; // ��ȡӪҵʱ��
			// ��7*24ʱ��˻�ȡbankDate
			if( !SFConst.WORKMODE_724CLEAR.equals( workMode ) ) {
				workDate = localInfo.getBankDate(); // ��ȡӪҵʱ��
			} else {
				// 7*24ʱ��λ�ȡworkDate
				workDate = localInfo.getWorkdate();
			}

			int pageNum = 0;

			// Ĭ�ϴӵ�һҳ��ʼ��ѯ
			if( SFUtil.isNotEmpty( strPageNum ) ) {
				pageNum = Integer.parseInt( strPageNum );
			} else {
				pageNum = 1;
			}
			// �ȴӱ���ͷ��ȡÿҳ��ʾ��������ȡΪ��ȡ�����ֵ�Ĭ��20��
			Page<AcctDetail> page = new Page<AcctDetail>( SFConst.SF_PERPAGE_NUM_BANK, pageNum );
			if( SFUtil.isNotEmpty( pageSize ) ) {
				page.setPageSize( Integer.valueOf( pageSize ) );
			}
			AcctDetailDao acctDetailDao = new AcctDetailDao();
			page = acctDetailDao.qryAcctDetailInfo( context, tranConnection, startDate, endDate, acctId, capAcct, secCompCode, curCode, workDate, page );
			SFUtil.chkCond( context, null == page || null == page.getPageData() || page.getPageData().size() == 0, "ST4069", String.format( "��ѯ�޼�¼" ) );

			pageNum = 0;
			IndexedCollection indexColl = SFUtil.getDataElement( context, "200302_O_ICOLL" );
			for( AcctDetail acctdetail : page.getPageData() ) {
				// ��װ������������
				KeyedCollection keyColl = new KeyedCollection();
				SFUtil.addDataField( context, keyColl, "CUR_CODE", acctdetail.getCurCode() );
				SFUtil.addDataField( context, keyColl, "TX_AMOUNT", acctdetail.getTxAmount() );
				SFUtil.addDataField( context, keyColl, "TX_DATE", acctdetail.getTxDate() );
				SFUtil.addDataField( context, keyColl, "CAP_ACCT", acctdetail.getCapAcct() );
				SFUtil.addDataField( context, keyColl, "SEC_COMP_CODE", acctdetail.getSecCompCode() );
				SFUtil.addDataField( context, keyColl, "SEC_COMP_NAME", acctdetail.getSecCompName() );
				SFUtil.addDataField( context, keyColl, "INIT_SIDE", acctdetail.getInitSideCN() );
				SFUtil.addDataField( context, keyColl, "BUSI_TYPE", acctdetail.getBusiTypeCN() );
				if( SFConst.INV_TYPE_CORP.equals( acctdetail.getInvType() ) ) {// �Թ�
					SFUtil.addDataField( context, keyColl, "INV_NAME", signAccountData.getInvName() );// �ͻ�����
				}
				indexColl.add( keyColl );
				pageNum++;
			}
			SFUtil.setDataValue( context, "APP_HEAD.TOTAL_NUM", page.getTotalNum() );// �ܼ�¼��
			SFUtil.setDataValue( context, "APP_HEAD.END_FLAG", pageNum == page.getPageData().size() ? "Y" : "N" );// ������ʶ
			SFUtil.setResDataValue( context, "ACCT_ID", signAccountData.getAcctId() );// �˺�
			if( SFConst.INV_TYPE_RETAIL.equals( SFUtil.getReqDataValue( context, "INV_TYPE" ) ) ) {// �Թ�
				SFUtil.setResDataValue( context, "CUR_CODE", signAccountData.getCurCode() );// ����
				SFUtil.setResDataValue( context, "INV_NAME", signAccountData.getInvName() );// �ͻ�����
			} else {
				SFUtil.setResDataValue( context, "CUR_CODE", "ALL" );// ����
				// SFUtil.setResDataValue(context, "TOTAL_REC",page.getTotalNum());//���ϲ�ѯ�����ļ�¼����
				SFUtil.setResDataValue( context, "END_FLAG", pageNum == page.getPageData().size() ? "Y" : "N" );// ������־
			}
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", String.format( "����ʧ��", e.getMessage() ) );
		}
		SFLogger.info( context, String.format( "qryAcctDetail()����" ) );
	}
}