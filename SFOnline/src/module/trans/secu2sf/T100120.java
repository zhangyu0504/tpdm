package module.trans.secu2sf;

import module.bean.SecCompData;
import module.bean.SecServStatus;
import module.dao.SecServStatusDao;
import module.trans.TranBase;

import com.ecc.emp.core.Context;
import com.ecc.emp.data.KeyedCollection;
import common.exception.SFException;
import common.sql.dao.DBHandler;
import common.util.SFConst;
import common.util.SFUtil;

import core.log.SFLogger;

/**
 * A��ȯ�̶˷���--ǩ��/ǩ�� ����
 * ȯ������/ֹͣ���������ȯ�̷���״̬
 * 
 * �����߼���
 * 			1.����ȯ�̽��� ��<��>Sysm.001.01/10001(����)��10002��ֹͣ����<ֱ>6065/612209��
 * 			2.����������Ч��,�������
 * 			3.���·���״̬
 * 			4.����ȯ��          ��<��>Sysm.002.01/10001(����)��10002��ֹͣ����<ֱ>6065/612209��
 * 
 * tran code :100120
 * @author ������
 *
 */
public class T100120 extends TranBase {

	private String acctServFlag = null; // �˻�������־

	private String productType = "03"; // ��Ʒ����

	private String intServFlag1 = null; // ������Ϣ�����־

	private String transServFlag1 = null; // ���з���֤ת��

	private String transServFlag2 = null; // ���з���֤ת��

	private String transServFlag3 = null; // ȯ�̷�����ת֤

	private String transServFlag4 = null; // ȯ�̷���֤ת��

	private String funcCode = null; // ������

	private String txCode = null;// ������

	// private String txDate = null;// ��������

	private String secCompCode = null;// ȯ�̴���

	private String secSeqId = null;// ȯ����ˮ��

	private String secuType = null;// ȯ������

	// private String sysType = null; /*Ӧ��ϵͳ���ͣ�0-��������� 3-������ȯ�� ���ڷ�����֤ͨȯ��ͷ����Ϣ*/

	@Override
	protected void initialize( Context context ) throws SFException {
		KeyedCollection kColl = null;
		KeyedCollection senderKcoll = null;

		try {

			secCompCode = SFUtil.getReqDataValue( context, "SEC_COMP_CODE" );
			if( SFUtil.isEmpty( secCompCode ) ) {
				kColl = SFUtil.getDataElement( context, "MsgHdr" );
				senderKcoll = SFUtil.getDataElement( context, kColl, "Sender" );
				secCompCode = SFUtil.getDataValue( context, senderKcoll, "InstId" );// ȯ�̴���
			}

			SFUtil.chkCond( context, SFUtil.isEmpty( secCompCode ), "ST5711", String.format( "��ȯ����Ϣ������" ) );
			SecCompData secCompData = secCompDataDao.qrySecCompData( context, tranConnection, secCompCode );
			SFUtil.chkCond( context, ( null == secCompData ), "ST5711", String.format( "��ȯ����Ϣ������" ) );

			SFUtil.setDataValue( context, SFConst.PUBLIC_SECU_TYPE, secCompData.getSztFlag() );// ��ȯ�����ͷ�����������
			SFUtil.setDataValue( context, SFConst.PUBLIC_SECU, secCompData );

			// ȯ������
			secuType = SFUtil.getDataValue( context, SFConst.PUBLIC_SECU_TYPE );

			SFUtil.chkCond( context, SFUtil.isEmpty( secuType ), "ST5701", String.format( "ȯ�����Ͳ���Ϊ��[%s]", secuType ) );

			if( SFConst.SECU_ZL.equals( secuType ) ) { // ֱ��ģʽ
				funcCode = SFUtil.getDataValue( context, "ZLSECU_REQUEST_HEAD.FUNCCODE" );
				secCompCode = SFUtil.getReqDataValue( context, "SEC_COMP_CODE" ); // ȯ�̴���
				secSeqId = SFUtil.getReqDataValue( context, "SEC_SEQ_ID" ); // ȯ����ˮ��

			} else if( SFConst.SECU_SZT.equals( secuType ) ) { // ��֤ͨģʽ

				KeyedCollection refKcoll = SFUtil.getDataElement( context, kColl, "Ref" );

				// sysType = SFUtil.getDataValue( context,kColl,"SysType");//Ӧ��ϵͳ����
				secCompCode = SFUtil.getDataValue( context, senderKcoll, "InstId" );// ȯ�̴���
				secSeqId = SFUtil.getDataValue( context, refKcoll, "Ref" );// ȯ����ˮ��
				txCode = SFUtil.getDataValue( context, kColl, "InstrCd" );// ������

				// ��װȯ��ͨ��������
				KeyedCollection keyColl = new KeyedCollection( "100120_I" );

				SFUtil.addDataField( context, keyColl, "SEC_COMP_CODE", secCompCode );// ȯ�̴���
				SFUtil.addDataField( context, keyColl, "SEC_SEQ_ID", secSeqId );// ȯ����ˮ��

				SFUtil.addDataElement( context, keyColl );
			}

		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}
	}

	@Override
	public void doHandle( Context context ) throws SFException {

		// ��鲢���·���״̬��
		updSecServStatus( context );

		// �������ȯ��
		doSecu( context );

	}

	public void updSecServStatus( Context context ) throws SFException {
		SFLogger.info( context, "updSecServStatus()��ʼ" );
		try {

			// ��������
			DBHandler.beginTransaction( context, tranConnection );

			/* �������ݿ��,����pics����,���жϳ�ʼ״̬,ֱ�Ӹ��� */
			SecServStatus secServStatus = new SecServStatus();
			secServStatus.setAcctServFlag( acctServFlag );
			secServStatus.setIntServFlag( intServFlag1 );
			secServStatus.setTransServFlag1( transServFlag1 );
			secServStatus.setTransServFlag2( transServFlag2 );
			secServStatus.setTransServFlag3( transServFlag3 );
			secServStatus.setTransServFlag4( transServFlag4 );
			secServStatus.setSecCompCode( secCompCode );
			secServStatus.setProductType( productType );

			SecServStatusDao secServStatusDao = new SecServStatusDao();
			secServStatusDao.saveSecServStatus( context, tranConnection, secServStatus );
			DBHandler.commitTransaction( context, tranConnection );

		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFLogger.error( context, e.getMessage() );
			SFUtil.chkCond( context, "ST4895", "����ȯ�̷���״̬ʧ��" );
		}
		SFLogger.info( context, "updSecServStatus()����" );
	}

	@Override
	public void doHost( Context context ) throws SFException {

	}

	@Override
	public void doSecu( Context context ) throws SFException {
		SFLogger.info( context, "doSecu()��ʼ" );
		try {

			// ���׳ɹ�-�������ȯ��
			if( SFConst.SECU_ZL.equals( secuType ) ) { // ֱ��ģʽ
				// 10200000��������????,����ͷ��Ϣ��������
				// ���
				SFUtil.setResDataValue( context, "RESP_CODE", SFConst.RESPCODE_SUCCCODE );
				SFUtil.setResDataValue( context, "RESP_MSG", "ǩ��/ǩ�˽��׳ɹ�" );
				SFUtil.setResDataValue( context, "SEC_SEQ_ID", secSeqId );

			} else if( SFConst.SECU_SZT.equals( secuType ) ) { // ��֤ͨģʽ�������ȯ��

				KeyedCollection kColl = SFUtil.getDataElement( context, "Sysm00201" );

				// ��<Rst>���
				KeyedCollection rstKcoll = SFUtil.getDataElement( context, kColl, "Rst" );
				SFUtil.setDataValue( context, rstKcoll, "RESP_CODE", SFConst.RESPCODE_SUCCCODE );
				SFUtil.setDataValue( context, rstKcoll, "RESP_MSG", "ǩ��/ǩ�˽��׳ɹ�" );

			}
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}
		SFLogger.info( context, "doSecu()����" );
	}

	@Override
	protected void chkStart( Context context ) throws SFException {

	}

	@Override
	protected void chkEnd( Context context ) throws SFException {
		try {

			// //����Ӫҵ���ں�ƽ̨������ж�
			// LocalInfo localInfo = localInfoDao.qryLocalInfo(context, tranConnection);
			// SFUtil.chkCond(context, (null == localInfo), "ST5700", String.format("ƽ̨����δ����"));
			//
			// txDate = ((LocalInfo)SFUtil.getDataValue(context, SFConst.PUBLIC_LOCAL_INFO)).getWorkdate();//��������

			// ֱ��ȯ��ʹ�� funcCode ������ͣ����֤ͨȯ��ʹ�� txCode������ͣ
			if( "000".equals( funcCode ) || "10001".equals( txCode ) ) {
				// SFUtil.chkCond(context, !txDate.equals(localInfo.getWorkdate()), "ST1032", String.format("TxDate��BankDate���ڲ�һ��"));
				// ����
				acctServFlag = "1";
				intServFlag1 = "1";
				transServFlag1 = "1";
				transServFlag2 = "1";
				transServFlag3 = "1";
				transServFlag4 = "1";

				// ֹͣ
			} else if( "001".equals( funcCode ) || "10002".equals( txCode ) ) {

				acctServFlag = "0";
				intServFlag1 = "0";
				transServFlag1 = "0";
				transServFlag2 = "0";
				transServFlag3 = "0";
				transServFlag4 = "0";

			} else {
				SFUtil.chkCond( context, "ST5702", String.format( "[�����������]�Ƿ�" ) );
			}

			/* ������״̬�� */
			SecServStatus secServStatus = SFUtil.getDataValue( context, SFConst.PUBLIC_SEC_SERV_STATUS );// ȯ��ǩ����
			SFUtil.chkCond( context, ( null == secServStatus ), "ST5705", String.format( "ȯ�̴������" ) );

		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}
	}
}