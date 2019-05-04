package module.trans.bankchl2sf;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import module.bean.BankSignData;
import module.bean.InvestData;
import module.bean.SignAccountData;
import module.dao.BankSignDataDao;
import module.trans.TranBase;
import module.trans.sf2bankchl.QryKeyInvestinfoClient;

import com.ecc.emp.core.Context;
import com.ecc.emp.data.IndexedCollection;
import com.ecc.emp.data.KeyedCollection;
import common.exception.SFException;
import common.util.BizUtil;
import common.util.SFConst;
import common.util.SFUtil;

import core.log.SFLogger;

/**
 * (������������) ��ѯǩԼ��ϵ ������ : 200300
 * 
 * @author ex_kjkfb_zhumingtao
 * 
 */
public class T200300 extends TranBase {

	private String secAcct = null;

	private String cardId = null;

	private String invType = null;

	private String capAcct = null;

	private String invIdCode = null;

	private String signFlag = null;

	private String initSide = null;

	private String sfIdType = null;

	private String tpdmFlag = null;

	@Override
	protected void initialize( Context context ) throws SFException {

	}

	@Override
	public void doHandle( Context context ) throws SFException {

		// �Թ�������
		if( SFConst.INV_TYPE_CORP.equals( invType ) && SFConst.INIT_SIDE_ABBANK.equals( initSide ) && SFConst.SIGN_FLAG_SECU_PRE.equals( signFlag )) {
			if( SFConst.ID_TYPE_COMPANY_JRJGXKZ.equals( sfIdType )){
				// ��ʼ������
				doHost( context );				
			}
		} else {
			cardId = SFUtil.getReqDataValue( context, "ACCT_ID" );
		}

		// ��ѯǩԼ��ϵ
		qrySignAccountData( context );

	}

	@Override
	public void doHost( Context context ) throws SFException {
		SFLogger.info( context, "doHost()��ʼ" );
		String acctId = SFUtil.getReqDataValue( context, "ACCT_ID" );// �����ʺ�
		try {
			Context msgContext = null;
			String retFlag = null;

			SFUtil.chkCond( context, SFUtil.isEmpty( acctId ), "ST4069", "ͬҵ�˺Ų���Ϊ��" );

			SFLogger.info( context, "��������ѯ֤����Ϣ��ʼ" );

			/* D+ M8010 �ӿڲ�ѯͬҵ�˺�֤������֤���� */
			Map<String, Object> msg = new HashMap<String, Object>();
			msg.put( "ACCT_ID", acctId );
			QryKeyInvestinfoClient qkClient = new QryKeyInvestinfoClient();
			// ���ͱ���
			msgContext = qkClient.send( context, msg );

			retFlag = SFUtil.getDataValue( msgContext, SFConst.PUBLIC_RET_FLAG );

			// ��������ʧ�ܻ��쳣,�����˳�
			SFUtil.chkCond( context, !SFConst.RET_SUCCESS.equals( retFlag ), "ST5601", "������[��ѯ֤����Ϣ]ʧ��" );

			String retMsg = SFUtil.getDataValue( msgContext, "MSG_O.RET_MSG" );// ��Ӧ��Ϣ
			// �жϲ�ѯ�Ƿ�ɹ�
			if( !SFConst.RET_SUCCESS.equals( retFlag ) ) {
				SFUtil.chkCond( context, "ST5800", String.format( retMsg ) );
			}

			// ��������ͬҵ�˺�֤������֤����
			IndexedCollection iColl1 = SFUtil.getDataElement( msgContext, "MSG_O_ICOLL" );
			KeyedCollection kColl = ( KeyedCollection )iColl1.getElementAt( 0 ); // ��ȡ��Ӧ����

			String otherProveFileType = SFUtil.getDataValue( msgContext, kColl, "OTHER_PROVE_FILE_TYPE" ); // ����֤���ļ�����
			SFLogger.info( context, String.format( "ȡ����֤������[����֤���ļ�����]:[%s]", otherProveFileType ) );

			String otherProveFileNo = SFUtil.getDataValue( msgContext, kColl, "OTHER_PROVE_FILE_NO" ); // ����֤���ļ�����
			SFLogger.info( context, String.format( "ȡ����֤������[����֤���ļ�����]:[%s]", otherProveFileNo ) );

			SFUtil.chkCond( context, SFUtil.isEmpty( otherProveFileType ) || SFUtil.isEmpty( otherProveFileNo ), "ST5602", "��������ȡ֤����Ϣʧ��" );

			// ����֤������ת����֤������
			sfIdType = BizUtil.convHost2SF4IdType( context, otherProveFileType );
			invIdCode = otherProveFileNo;

			SFLogger.info( context, "��������ѯ֤����Ϣ����" );

		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", String.format( "doHost����ʧ��%s", e.getMessage() ) );
		}
		SFLogger.info( context, "doHost()����" );
	}

	@Override
	public void doSecu( Context context ) throws SFException {
		// TODO Auto-generated method stub

	}

	@Override
	protected void chkStart( Context context ) throws SFException {

	}

	@Override
	protected void chkEnd( Context context ) throws SFException {
		invType = SFUtil.getReqDataValue( context, "INV_TYPE" );// �ͻ�����
		capAcct = SFUtil.getReqDataValue( context, "CAP_ACCT" );// �ʽ��ʺ�
		invIdCode = SFUtil.getReqDataValue( context, "INV_ID_CODE" ); // ֤������
		signFlag = SFUtil.getReqDataValue( context, "SIGN_FLAG" ); // ǩԼ��־
		initSide = SFUtil.getReqDataValue( context, "INIT_SIDE" ); // ����
		tpdmFlag = SFUtil.getReqDataValue( context, "TPDM_FLAG" ); // ȯ������
		sfIdType = SFUtil.getDataValue( context, SFConst.PUBLIC_ID_TYPE );// ת�����֤������
	}

	private void qrySignAccountData( Context context ) throws SFException {
		SFLogger.info( context, String.format( "qrySignAccountData()��ʼ" ) );

		InvestData investData = null;
		SignAccountData signAccountData = null;
		BankSignData bankSignData = new BankSignData();

		String convInvIdCode1 = null; //ת����֤�����룬ת��ǰ15λ��ת����Ϊ18λ20ʵ��֤�����룻ת��ǰ18λ��ת����Ϊ15λ֤�����룻
		String convInvIdCode2 = null; //ת����֤�����룬ת��ǰ15λ��ת����Ϊ18λ20ʵ��֤�����룻ת��ǰ18λ����ת����Ϊ��ֵ��
		
		try {

			// �����Ų�ѯǩԼ��ϵ
			if( SFUtil.isNotEmpty( cardId ) ) {

				signAccountData = signAccountDataDao.qrySignAccountDataByAcctId( context, tranConnection, cardId );
				SFUtil.chkCond( context, signAccountData == null, "ST4069", "�ͻ�ǩԼ��Ϣ������" );
				secAcct = signAccountData.getSecAcct();

				// �Թ����ʽ��ʺŲ�ѯǩԼ��ϵ
			} else if( SFConst.INV_TYPE_CORP.equals( invType ) && SFUtil.isNotEmpty( capAcct ) ) {

				signAccountData = signAccountDataDao.qrySignAccountDataByCapAcct( context, tranConnection, capAcct );
				SFUtil.chkCond( context, signAccountData == null, "ST4069", "�ͻ�ǩԼ��Ϣ������" );
				secAcct = signAccountData.getSecAcct();

			} else {

				SFUtil.chkCond( context, SFUtil.isEmpty( sfIdType ) || SFUtil.isEmpty( invIdCode ), "ST4069", "֤����Ϣ����Ϊ��" );

				 if( SFConst.ID_TYPE_PERSON_SFZ.equals( sfIdType ) || SFConst.ID_TYPE_PERSON_LSSFZ.equals( sfIdType ) ) {
					if( invIdCode.length() == 15 ) {
						convInvIdCode1 = BizUtil.converTo18Card19( invIdCode ); // 15λ����֤����ת��Ϊ18λ
						convInvIdCode2 = BizUtil.converTo18Card20( invIdCode ); // 15λ����֤����ת��Ϊ18λ
					}
					if( invIdCode.length() == 18 ) {
						convInvIdCode1 = BizUtil.converTo15( invIdCode ); // 18λ����֤����ת��Ϊ15λ
						convInvIdCode2 = "";
					}				
				 }
				
				investData = investDataDao.qryInvestDataByInvIdCode( context, tranConnection, sfIdType, invIdCode, convInvIdCode1, convInvIdCode2 );

				SFUtil.chkCond( context, investData == null, "ST4069", "�ͻ�ǩԼ��Ϣ������" );
				
				secAcct = investData.getSecAcct();
				
			}

			if( secAcct == null ) {
				SFUtil.chkCond( context, "ST4392", String.format( "�޴˿ͻ���Ϣ" ) );
			}

			investData = investDataDao.qryInvestDataBySecAcct( context, tranConnection, secAcct );
			
			List<Map<String,Object>> listAccountData = signAccountDataDao.qrySignAccountDataList( context, tranConnection, cardId, signFlag, tpdmFlag, capAcct, sfIdType, invIdCode, convInvIdCode1, convInvIdCode2 );
			if( listAccountData == null || listAccountData.size() == 0 ) {
				SFUtil.chkCond( context, "ST4069", String.format( "�޷��������ļ�¼" ) );
			}
			// ��װ��������
			IndexedCollection indexColl = SFUtil.getDataElement( context, "200300_O_ICOLL" );
			
			for(Map<String,Object> mapAccData:listAccountData){
				KeyedCollection keyColl = new KeyedCollection();
				if( SFConst.INV_TYPE_RETAIL.equals( invType ) && SFConst.SIGN_FLAG_BANK_PRE.equals(SFUtil.objectToString(mapAccData.get("SIGNMODE"))) ) {
					// ģʽΪ2-����ԤԼ��ȯ�̼���ģʽ������Ҫ��ѯ����ԤԼ��ϵ��TRDBankSignData������Ԥ�ƶ�����(TxDate)+��������(opendate)
					BankSignDataDao bankSignDataDao = new BankSignDataDao();
					bankSignData = bankSignDataDao.qryBankSignDataByAcctId( context, tranConnection, cardId, sfIdType, invIdCode );
					SFUtil.chkCond( context, null == bankSignData, "ST4895", String.format( "����ʧ��" ) );

					SFUtil.addDataField( context, keyColl, "TX_DATE", bankSignData.getTxDate() );
					SFUtil.addDataField( context, keyColl, "OPEN_DATE", bankSignData.getOpenDate() );
				}
				SFUtil.addDataField( context, keyColl, "ACCT_ID", SFUtil.objectToString(mapAccData.get("ACCTID")));
				SFUtil.addDataField( context, keyColl, "INV_NAME", SFUtil.objectToString(mapAccData.get("INVNAME")));
				SFUtil.addDataField( context, keyColl, "CUR_CODE", SFUtil.objectToString(mapAccData.get("CURCODE")));
				SFUtil.addDataField( context, keyColl, "ID_TYPE", BizUtil.convSF2Host4IdType( context, SFUtil.objectToString(mapAccData.get("IDTYPE"))) );
				SFUtil.addDataField( context, keyColl, "INV_ID_CODE", SFUtil.objectToString(mapAccData.get("INVIDCODE")));
				SFUtil.addDataField( context, keyColl, "CAP_ACCT", SFUtil.objectToString(mapAccData.get("CAPACCT")));
				SFUtil.addDataField( context, keyColl, "INV_TYPE", SFUtil.objectToString(mapAccData.get("INVTYPE")));
				SFUtil.addDataField( context, keyColl, "SEC_COMP_CODE", SFUtil.objectToString(mapAccData.get("SECCOMPCODE")));
				SFUtil.addDataField( context, keyColl, "SEC_COMP_NAME", SFUtil.objectToString(mapAccData.get("SECCOMPNAME")));
				SFUtil.addDataField( context, keyColl, "SIGN_FLAG", SFUtil.objectToString(mapAccData.get("SIGNFLAG")));
				SFUtil.addDataField( context, keyColl, "CHANNEL", SFUtil.objectToString(mapAccData.get("CHANNEL")));
				SFUtil.addDataField( context, keyColl, "CUS_MAG_NO", SFUtil.objectToString(mapAccData.get("CUSMAGNO")));
				SFUtil.addDataField( context, keyColl, "DEP_ID", SFUtil.objectToString(mapAccData.get("DEPID")));
				if( keyColl.containsKey( "OPEN_DATE" ) ) {
					SFUtil.setDataValue( context, keyColl, "OPEN_DATE", SFUtil.objectToString( mapAccData.get( "OPENDATE" ) ) );
				} else {
					SFUtil.addDataField( context, keyColl, "OPEN_DATE", SFUtil.objectToString( mapAccData.get( "OPENDATE" ) ) );
				}
				SFUtil.addDataField( context, keyColl, "SIGN_DATE", SFUtil.objectToString(mapAccData.get("SIGNDATE")));
				SFUtil.addDataField( context, keyColl, "SIGN_MODE", SFUtil.objectToString(mapAccData.get("SIGNDATE")));
				SFUtil.addDataField( context, keyColl, "TPDM_FLAG", "5".equals(SFUtil.objectToString(mapAccData.get("TPDMFLAG"))) ? "1" : "0" );
				if( investData != null ) {
					SFUtil.addDataField( context, keyColl, "TRN_NAME", investData.getTrnName() );
					SFUtil.addDataField( context, keyColl, "MOBILE", investData.getMobile() );
					SFUtil.addDataField( context, keyColl, "PHONE", investData.getPhone() );
					SFUtil.addDataField( context, keyColl, "ADDR", investData.getAddr() );
					SFUtil.addDataField( context, keyColl, "ZIP", investData.getZip() );
					SFUtil.addDataField( context, keyColl, "FAX", investData.getFax() );
					SFUtil.addDataField( context, keyColl, "EMAIL_ADDR", investData.getEmailAddr() );
					SFUtil.addDataField( context, keyColl, "LEGAL_NAME", investData.getLegalName() );
				}
				indexColl.add( keyColl );
			}
			
			SFUtil.setDataValue( context, "APP_HEAD.TOTAL_NUM", listAccountData.size() );
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", String.format( "����ʧ��%s", e.getMessage() ) );
		}
		SFLogger.info( context, String.format( "qrySignAccountData()����" ) );
	}
}