package common.util;

import java.sql.Connection;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import module.bean.AcctJour;
import module.bean.AgtAgentInfo;
import module.bean.AgtCustomerInfo;
import module.bean.AutoBecif;
import module.bean.BankCodeInfo;
import module.bean.InvestData;
import module.bean.LocalInfo;
import module.bean.Param;
import module.bean.SecCompData;
import module.bean.ServStatus;
import module.bean.SignAccountData;
import module.bean.Trans;
import module.cache.ParamCache;
import module.cache.TransCache;
import module.dao.AcctJourDao;
import module.dao.AgtAgentInfoDao;
import module.dao.AgtCustomerInfoDao;
import module.dao.AutoBecifDao;
import module.dao.BankCodeInfoDao;
import module.dao.ServStatusDao;
import module.dao.SignAccountDataDao;
import module.trans.sf2bankchl.QryBalClient;
import module.trans.sf2bankchl.QryCardAttrClient;
import module.trans.sf2bankchl.QryCardLevelClient;
import module.trans.sf2bankchl.QryCardTypeClient;
import module.trans.sf2bankchl.QryKeyInvestinfoClient;
import module.trans.sf2bankchl.SetCardStatusWordClient;
import module.trans.sf2cobank.T810026Client;

import com.ecc.emp.core.Context;
import com.ecc.emp.data.IndexedCollection;
import com.ecc.emp.data.KeyedCollection;
import common.exception.SFException;
import common.sql.dao.DBHandler;
import common.sql.dao.DaoBase;

import core.log.SFLogger;

public class BizUtil {

	/**
	 * ��ȡ����������־����
	 * 
	 * @param context
	 * @return
	 * @throws SFException
	 */
	public static String getLogLevel( Context context ) throws SFException {
		String txcode = SFUtil.getDataValue( context, SFConst.PUBLIC_TX_CODE );
		return getLogLevel( txcode );
	}

	/**
	 * ��ȡ����������־����
	 * 
	 * @param context
	 * @return
	 * @throws SFException
	 */
	public static String getLogLevel( String txcode ) throws SFException {
		Trans trans = TransCache.getValue( txcode );
		return trans != null ? trans.getLogLevel() : null;
	}

	/**********************20171214��ˮ����start****************************************/

	/**
	 * ���������� ��ȡ���ݿ����к�
	 * ��ȡ��0�Ķ����ַ���,Ŀǰ��Ҫ����������ˮ��
	 * @param context
	 * @param length ����
	 * @param sqeName ��������
	 * @return
	 * @throws SFException
	 */
	public static String genSeqId( Context context, int length, String sqeName ) throws SFException {
		DaoBase dao = new DaoBase();
		Connection tranConnection = null;
		String seqId = null;

		try {
			tranConnection = SFUtil.getDataValue( context, SFConst.PUBLIC_TRAN_CONNECTION );
			// ��ѯ���б���ǰ���к�sqeName,����lengthλ��0
			String sql = "SELECT LPAD(" + sqeName + ".NEXTVAL ," + length + ",'0') TRCNO FROM DUAL";
			Map<String, Object> result = dao.qryMap( context, tranConnection, sql );

			if( result != null ) {
				seqId = ( String )result.get( "TRCNO" );
			}

		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFLogger.error( context, e.getMessage() );
		}
		return seqId;
	}

	/**
	 * ���������� ���ɶ���ID��ϵͳ��ʼ��ˮ��14λ��
	 * ����14λ����ˮ�ţ������ڼ����е�logid�����߽�����ˮ���е�acctdealid���ֶ�
		��ʽ��yymmdd+genSeqId����
	 * @return
	 * @throws SFException
	 */
	public static String getInitSeqId( Context context ) throws SFException {
		return DateUtil.getDateShort() + genSeqId( context, 8, "TRCNO" );// 6λ����+8λseqId
	}

	/**
	 * ����subtxseqid��ˮ��16λ
	 * ������Trdacctjour/Trdacctdetail/trdbanksigndata��subtxseqid,
	 * ��ʽ��getInitSeqId()+2λ������ʶ��01~06��Ĭ��00��
	 * @return
	 * @throws SFException
	 */
	public static String getSubTxSeqId( String initSeqId ) throws SFException {
		String macCode = SFUtil.getSysProperty( "APP_CODE" );
		if( SFUtil.isEmpty( macCode ) || macCode.length() != 2 ) {
			macCode = "00";
		}
		return initSeqId + macCode;// 14λϵͳ��ˮ��+2λ������ʶ��01~06��Ĭ��00��
	}

	/**
	 * ���������� ����22λESB��ˮ��
	 * ��getSubTxSeqId��Ϊ���룬���22λ�����ݣ���ʽ��429696+getSubTxSeqId
	 * ��ʽ��429696+getSubTxSeqId()
	 * @param initSeqId
	 *            ��ʼ��ˮ��
	 * @return esbNo
	 */
	public static String getChlSeqId( Context context, String subTxSeqId ) throws SFException {
		String chlSeqId = SFConst.SYS_SYSID + subTxSeqId; // 6λϵͳID+16λ��ˮ
		SFUtil.chkCond( context, chlSeqId.length() != 22, "ST4098", String.format( "���ɵ�ESB��ˮ��[%s]��������", chlSeqId ) );
		return chlSeqId;
	}

	/**
	 * ���������� ��ʼ������ԤԼ��
	 * 8λϵͳ����+7λ���к�
	 * @param seqName
	 * @return
	 * @throws SFException
	 */
	public static String genBookId( Context context ) throws SFException {
		return DateUtil.getMacDate() + genSeqId( context, 7, "SEQ_BOOKNO" );// yyyyMMdd+7λseqId
	}

	/**
	 * ���������� ����Ͷ���˹����ʺ�
	 * 8888+16λ�����+1λУ��ֵ
	 * @return
	 * @throws SFException
	 */
	public static String genSecAcctId( Context context ) throws SFException {
		String seqId = "8888" + genSeqId( context, 16, "SEQ_SECACCT" );// 8888+16λ�����
		String secAcctId = seqId + genVerifyChar( context, seqId );// 8888+16λ�����+1λУ��ֵ
		return secAcctId;
	}

	/**
	 * ����У��λ
	 * @param context
	 * @param verifiedStr
	 * @return
	 * @throws SFException
	 */
	public static int genVerifyChar( Context context, String verifiedStr ) throws SFException {
		int i = 0;
		int j = 0;
		int c = 0;
		try {
			if( SFUtil.isNotEmpty( verifiedStr ) ) {
				for( j = 2; c < verifiedStr.length() - 1; c++ ) {
					if( j * ( verifiedStr.charAt( c ) - '0' ) < 10 ) {
						i = i + j * ( verifiedStr.charAt( c ) - '0' );
					} else {
						i = i + j * ( verifiedStr.charAt( c ) - '0' ) - 10 + 1;
					}
					if( --j == 0 ) {
						j = 2;
					}
				}
				c = i % 10;
				if( c != 0 ) {
					c = 10 - c;
				}
			}
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}
		SFLogger.info( context, String.format( "����У��λ genVerifyChar:[%s]", c % 10 ) );
		return c % 10;
	}

	/**********************20171214��ˮ����end****************************************/

	/**
	 * ���������� ���ɷ��𷽽�����ˮ�� ��8λȯ����ˮ�ţ���0��
	 * 
	 * @param seqId
	 * @return txSeqId
	 * 
	 */
	public static String getTxSeqId( int len, String seqId ) {

		int idxSeqId = ( seqId.length() - len ) < 0 ? 0 : ( seqId.length() - len );

		String txSeqId = seqId.substring( idxSeqId );

		// ���Ȳ��㣬��0
		while( txSeqId.length() < len ) {
			txSeqId = "0" + txSeqId;
		}

		return txSeqId;

	}

	/**
	 * ���������� ���ɷ��𷽽�����ˮ�� ��8λȯ����ˮ�ţ���0��
	 * 
	 * @param seqId
	 * @return txSeqId
	 * 
	 */
	public static String getTxSeqId( String seqId ) {

		String txSeqId = getTxSeqId( 8, seqId.trim() );
		return txSeqId;

	}

	/**
	 * ���ݽ������ж���֤��������
	 * 
	 * @param context
	 * @return
	 * @throws SFException
	 */
	public static String getTranType( Context context ) throws SFException {
		String tranType = null;
		String txCode = SFUtil.getDataValue( context, SFConst.PUBLIC_TX_CODE );// ��ȡ������
		if( "100200".equals( txCode ) || "200200".equals( txCode ) || "300200".equals( txCode ) ) {// ��ת֤����
			tranType = SFConst.BUSI_TYPE_B2S;
		} else if( "100201".equals( txCode ) || "200201".equals( txCode ) || "300201".equals( txCode ) ) {// ֤ת������
			tranType = SFConst.BUSI_TYPE_S2B;
		}
		return tranType;
	}

	/**
	 * ���������� ���ɶ��˵���
	 * 
	 * @return
	 * @throws SFException
	 */
	public static String getStatmentId( Context context ) throws SFException {
		String statmentId = null;
		statmentId = SFConst.STATEMENT_NO + DateUtil.getMacDate();
		return statmentId;
	}

	/**
	 * ��鳮���ʶ�Ƿ�Ϸ� add by lch
	 * 
	 * @param context
	 * @return
	 * @throws SFException
	 */
	public static String chkCashRemitFlag( Context context, String cashRemitFlag ) throws SFException {

		if( SFUtil.isEmpty( cashRemitFlag ) ) {// ��������ʶΪ��,��Ĭ��Ϊ��
			cashRemitFlag = SFConst.CASH_FLAG;
		} else if( !SFConst.REMIT_FLAG.equals( cashRemitFlag ) && !SFConst.CASH_FLAG.equals( cashRemitFlag ) ) {
			SFUtil.chkCond( context, "ST5717", "�����ʶ�Ƿ�" );
		}
		return cashRemitFlag;
	}

	/**
	 * ��ȡ��ǰ����ģʽ 0����������ʱ��� 1������ʱ��� 2���ڼ���ʱ��� 3������724ʱ���
	 * 
	 * @param context
	 * @throws SFException
	 */
	public static void setWorkMode( Context context, Connection connection ) throws SFException {
		try {
			LocalInfo localInfo = SFUtil.getDataValue( context, SFConst.PUBLIC_LOCAL_INFO );// Ӫҵʱ����Ϣ
			String txDate = DateUtil.getMacDate();// ϵͳ��������
			String bankDate = localInfo.getBankDate();// Ӫҵ����
			String lastBankDate = localInfo.getLastBankDate();// ǰһӪҵ����
			String workDate = localInfo.getWorkdate();// ��������

			ServStatusDao servStatusDao = new ServStatusDao();
			// �м�¼��Ϊƽ̨����ֹͣ��û��¼��Ϊ����
			ServStatus servStatus = servStatusDao.qryServStatus( context, connection, bankDate );

			if( txDate.equals( bankDate ) && txDate.equals( workDate ) && servStatus == null ) {// 0-��������ʱ���
				SFUtil.setDataValue( context, SFConst.PUBLIC_WORKMODE, SFConst.WORKMODE_NORMAL );
			} else {// 724ʱ���
				if( !bankDate.equals( workDate ) ) {// 1-����ʱ���
					SFUtil.setDataValue( context, SFConst.PUBLIC_WORKMODE, SFConst.WORKMODE_724CLEAR );
				} else if( txDate.compareTo( lastBankDate ) > 0 && txDate.compareTo( bankDate ) < 0 ) {// 2-�ڼ���ʱ���
					SFUtil.setDataValue( context, SFConst.PUBLIC_WORKMODE, SFConst.WORKMODE_724HOLIDAY );
				} else {// 3-724����ʱ���
					SFUtil.setDataValue( context, SFConst.PUBLIC_WORKMODE, SFConst.WORKMODE_724OTHER );
				}
			}

		} catch( Exception e ) {
			SFLogger.error( context, e.getMessage() );
			throw new SFException( e );
		}

	}

	/**
	 * ����֤������ת��ϵͳ֤������
	 * 
	 * @param idCode
	 *            ֤�����ͣ�����֤������/ϵͳ֤�����ͣ�
	 * @param type
	 * @return
	 */
	private static String convHostAndSF4IdType( Context context, String idType, String type ) throws SFException {
		String newTypeId = "";
		if( SFUtil.isNotEmpty( idType ) ) {
			idType = idType.trim(); // ��֤�����ͽ���ȥ�ո�
		}
		String idName = getIdTypeName( context, type, idType );
		Param param = null;
		SFLogger.info( context, String.format( "����֤������ת��ϵͳ֤������idCode=[%s],type=[%s],֤������[%s]", idType, type, idName ) );
		// ����֤������תϵͳ֤������
		if( SFUtil.isNotEmpty( idType ) && "HOSTTOSF".equals( type.trim() ) ) {
			param = ParamCache.getValue( "SF_HOST2SF_ID_TYPE", idType );
			if( param != null ) {
				newTypeId = param.getValue();
			}
		} else if( SFUtil.isNotEmpty( idType ) && "SFTOHOST".equals( type.trim() ) ) {// ϵͳ֤������ת����֤������+
			param = ParamCache.getValue( "SF_SF2HOST_ID_TYPE", idType );
			if( param != null ) {
				if( "0".equals( idType ) ) {
					String invType = SFUtil.getDataValue( context, SFConst.PUBLIC_INV_TYPE );// �ͻ�����
					newTypeId = SFConst.INV_TYPE_CORP.equals( invType ) ? "900" : "0";// 0-��������֤��,900-������ҵ֤��
				} else if( "51".equals( idType ) ) {
					String chlIdType = SFUtil.getReqDataValue( context, "ID_TYPE" );// ����������֤�����ͣ�û����Ϊnull
					newTypeId = "989".equals( chlIdType ) ? chlIdType : "996";// 996-Ӫҵִ��,989-ͳһ������ô���
																				// ����ʹ��996
				} else {
					newTypeId = param.getValue();
				}
			}
		} else if( SFUtil.isNotEmpty( idType ) && "COBANKTOSF".equals( type.trim() ) ) {// ������֤������תϵͳ֤������
			param = ParamCache.getValue( "SF_ZL2SF_ID_TYPE", idType );
			if( param != null ) {
				newTypeId = param.getValue();
			}
		} else if( SFUtil.isNotEmpty( idType ) && "SFTOCOBANK".equals( type.trim() ) ) {// ϵͳ֤������ת������֤������
			if( "3".equals( idType ) ) {// ���֤������Ϊ�۰�̨����
				String idCode = SFUtil.getReqDataValue( context, "INV_ID_CODE" );
				if( SFUtil.isEmpty( idCode ) ) {// ���֤������Ϊ�գ����Contextȡ���ͻ�����
					InvestData investData = SFUtil.getDataValue( context, SFConst.PUBLIC_INVEST_DATA );
					if( investData != null ) {
						idCode = investData.getInvIdCode();
					}
					if( SFUtil.isEmpty( idCode ) ) {
						idCode = SFUtil.getDataValue( context, "INV_ID_CODE" );
					}
				}
				SFLogger.info( context, String.format( "ϵͳ֤������ת��������֤�����ͣ�֤������Ϊ[%s]", idCode ) );

				if( SFUtil.isNotEmpty( idCode ) && ( 'H' == ( idCode.charAt( 0 ) ) || 'M' == ( idCode.charAt( 0 ) ) ) ) {// �ж�idCode��һλΪH����M���Ǹ۰�̨����
					return "16";// ����16(�۰�̨����ͨ��֤��֤ͨ����)
				} else if( SFUtil.isNotEmpty( idCode ) && SFUtil.isNum( SFUtil.objectToString( idCode.charAt( 0 ) ) ) ) {// �ж�idCode��λ�Ƿ�Ϊ����
					return "17";
				}

			} else {
				param = ParamCache.getValue( "SF_SF2ZL_ID_TYPE", idType );
				if( param != null ) {
					newTypeId = param.getValue();
				}
			}
		} else {// ������ȷ����������ֱ�ӷ��ؿ��ַ���
			return "";
		}
		SFLogger.info( context, String.format( "����֤������ת��ϵͳ֤�����ͳɹ����������أ�[%s],֤������[%s]", newTypeId, idName ) );
		return newTypeId;
	}

	/**
	 * ����ת����
	 * 
	 * @param context
	 * @param idCode
	 * @param type
	 * @return
	 */
	public static String convHost2SF4IdType( Context context, String idType ) throws SFException {
		return convHostAndSF4IdType( context, idType, "HOSTTOSF" );
	}

	/**
	 * ����ת����
	 * 
	 * @param context
	 * @param idCode
	 * @return
	 * @throws SFException
	 */
	public static String convSF2Host4IdType( Context context, String idType ) throws SFException {
		return convHostAndSF4IdType( context, idType, "SFTOHOST" );
	}

	/**
	 * ������ת����
	 * 
	 * @param context
	 * @param idCode
	 * @param type
	 * @return
	 */
	public static String convCoBank2SF4IdType( Context context, String idType ) throws SFException {
		return convHostAndSF4IdType( context, idType, "COBANKTOSF" );
	}

	/**
	 * ����ת������
	 * 
	 * @param context
	 * @param idCode
	 * @param type
	 * @return
	 */
	public static String convSF2CoBank4IdType( Context context, String idType ) throws SFException {
		return convHostAndSF4IdType( context, idType, "SFTOCOBANK" );
	}

	/**
	 * ȯ��֤������ת���֤������
	 * @param idCode
	 *            ֤������
	 * @param context
	 * @return ֤������
	 */
	private static String convSecuAndSF4IdType( Context context, String idType, String turnType ) throws SFException {
		SFLogger.info( context, String.format( "convSecuAndSF4IdType()ȯ��֤������ת���֤�����Ϳ�ʼ" ) );
		String newTypeId = "";
		Param param = null;
		String idName = getIdTypeName( context, turnType, idType );
		SFLogger.info( context, String.format( "ȯ��֤������[%s]ת���֤������[%s]", idType, idName ) );
		Object sztFlag = SFUtil.getDataValue( context, SFConst.PUBLIC_SECU_TYPE );// ���������л�ȡ��֤ͨ��ʶ
		if( "SECU2SF".equals( turnType ) ) {// ȯ��ת�������֤������
			if( SFConst.SECU_SZT.equals( sztFlag ) && ( !"60".equals( idType ) && !"70".equals( idType ) ) ) {// ��֤֤ͨ�����Ͳ��Ҳ��Ǹ۰�̨����
				param = ParamCache.getValue( "SF_SZT2SF_ID_TYPE", idType );
				if( param != null ) {
					newTypeId = param.getValue();
				}
			} else if( SFConst.SECU_ZL.equals( sztFlag ) && ( !"16".equals( idType ) && !"17".equals( idType ) ) ) {// ֱ��֤�����Ͳ��Ǹ۰�̨����
				param = ParamCache.getValue( "SF_ZL2SF_ID_TYPE", idType );
				if( param != null ) {
					newTypeId = param.getValue();
				}
			} else {// �۰�̨����
				return "3";
			}
		} else if( "SF2SECU".equals( turnType ) ) {// �������תȯ��֤������
			if( SFConst.SECU_SZT.equals( sztFlag ) ) {// ��֤֤ͨ������
				if( "3".equals( idType ) ) {// ���֤������Ϊ�۰�̨����
					String idCode = SFUtil.getReqDataValue( context, "INV_ID_CODE" );
					if( SFUtil.isEmpty( idCode ) ) {// ���֤������Ϊ�գ����Contextȡ���ͻ�����
						InvestData investData = SFUtil.getDataValue( context, SFConst.PUBLIC_INVEST_DATA );
						if( investData != null ) {
							idCode = investData.getInvIdCode();
						}
					}
					SFLogger.info( context, String.format( "ϵͳ֤������ת��ȯ�̼����ͣ�֤������Ϊ[%s]", idCode ) );
					if( SFUtil.isNotEmpty( idCode ) && ( 'H' == ( idCode.charAt( 0 ) ) || 'M' == ( idCode.charAt( 0 ) ) ) ) {// �ж�idCode��һλΪH����M���Ǹ۰�̨����
						return "60";// ����60(�۰�̨����ͨ��֤��֤ͨ����)
					} else if( SFUtil.isNotEmpty( idCode ) && SFUtil.isNum( SFUtil.objectToString( idCode.charAt( 0 ) ) ) ) {// �ж�idCode��λ�Ƿ�Ϊ����
						return "70";
					}
				} else {
					param = ParamCache.getValue( "SF_SF2SZT_ID_TYPE", idType );
					if( param != null ) {
						newTypeId = param.getValue();
					}
				}

			} else {// ֱ��֤������
				if( "3".equals( idType ) ) {// ���֤������Ϊ�۰�̨����
					String idCode = SFUtil.getReqDataValue( context, "INV_ID_CODE" );
					if( SFUtil.isEmpty( idCode ) ) {// ���֤������Ϊ�գ����Contextȡ���ͻ�����
						InvestData investData = SFUtil.getDataValue( context, SFConst.PUBLIC_INVEST_DATA );
						if( investData != null ) {
							idCode = investData.getInvIdCode();
						}
					}
					SFLogger.info( context, String.format( "ϵͳ֤������ת��ȯ�̼����ͣ�֤������Ϊ[%s]", idCode ) );
					if( SFUtil.isNotEmpty( idCode ) && ( 'H' == ( idCode.charAt( 0 ) ) || 'M' == ( idCode.charAt( 0 ) ) ) ) {// �ж�idCode��һλΪH����M���Ǹ۰�̨����
						return "16";// ����16(�۰�̨����ͨ��֤��֤ͨ����)
					} else if( SFUtil.isNotEmpty( idCode ) && SFUtil.isNum( SFUtil.objectToString( idCode.charAt( 0 ) ) ) ) {// �ж�idCode��λ�Ƿ�Ϊ����
						return "17";
					}
				} else {
					param = ParamCache.getValue( "SF_SF2ZL_ID_TYPE", idType );
					if( param != null ) {
						newTypeId = param.getValue();
					}
				}

			}
		}
		SFLogger.info( context, String.format( "convSecuAndSF4IdType()ȯ��֤������ת���֤�����ͽ���������:[%s],֤������[%s]", newTypeId, idName ) );
		return newTypeId;
	}

	/**
	 * ȯ��ת����
	 * 
	 * @param context
	 * @param idCode
	 * @param idType
	 * @return
	 * @throws SFException
	 */
	public static String convSecu2SF4IdType( Context context, String idType ) throws SFException {
		String newIdType = convSecuAndSF4IdType( context, idType, "SECU2SF" );
		return newIdType;
	}

	/**
	 * ����תȯ��
	 * 
	 * @param context
	 * @param idCode
	 * @param idType
	 * @return
	 * @throws SFException
	 */
	public static String convSF2Secu4IdType( Context context, String idType ) throws SFException {
		String newIdType = convSecuAndSF4IdType( context, idType, "SF2SECU" );
		return newIdType;
	}

	/**
	 * 
	 *������������������ȡ֤����������˵��
	 * @param context
	 * @param type ֤������
	 * @param id  ֤������ֵ
	 * @return
	 * @throws SFException
	 */
	private static String getIdTypeName( Context context, String type, String id ) throws SFException {
		String idName = null;
		try {
			String sztFlag = SFUtil.getDataValue( context, SFConst.PUBLIC_SECU_TYPE );
			Param param = null;
			if( SFUtil.isEmpty( type ) )
				return idName;
			if( "SECU2SF".equals( type ) ) {
				if( SFConst.SECU_SZT.equals( sztFlag ) && ( !"60".equals( id ) && !"70".equals( id ) ) ) {// ��֤ͨ
					param = ParamCache.getValue( "SF_SZT2SF_ID_TYPE", id );
					if( param != null ) {
						idName = param.getName();
					}
				} else if( SFConst.SECU_ZL.equals( sztFlag ) && ( !"16".equals( id ) && !"17".equals( id ) ) ) {// ֱ��
					param = ParamCache.getValue( "SF_ZL2SF_ID_TYPE", id );
					if( param != null ) {
						idName = param.getName();
					}

				} else {
					return "�۰�̨֤������";
				}
			} else if( "SF2SECU".equals( type ) ) {
				if( SFConst.SECU_SZT.equals( sztFlag ) && ( !"60".equals( id ) && !"70".equals( id ) ) ) {// ��֤ͨ
					param = ParamCache.getValue( "SF_SF2SZT_ID_TYPE", id );
					if( param != null ) {
						idName = param.getName();
					}
				} else if( SFConst.SECU_ZL.equals( sztFlag ) && ( !"16".equals( id ) && !"17".equals( id ) ) ) {// ֱ��
					param = ParamCache.getValue( "SF_SF2ZL_ID_TYPE", id );
					if( param != null ) {
						idName = param.getName();
					}
				} else {
					return "�۰�̨֤������";
				}
			} else if( "HOSTTOSF".equals( type ) ) {
				param = ParamCache.getValue( "SF_HOST2SF_ID_TYPE", id );
				if( param != null ) {
					idName = param.getName();
				}
			} else if( "SFTOHOST".equals( type ) ) {
				param = ParamCache.getValue( "SF_SF2HOST_ID_TYPE", id );
				if( param != null ) {
					idName = param.getName();
				}
			} else if( "COBANKTOSF".equals( type ) ) {
				param = ParamCache.getValue( "SF_ZL2SF_ID_TYPE", id );
				if( param != null ) {
					idName = param.getName();
				}
			} else if( "SFTOCOBANK".equals( type ) ) {
				param = ParamCache.getValue( "SF_SF2ZL_ID_TYPE", id );
				if( param != null ) {
					idName = param.getName();
				}
			}
		} catch( Exception e ) {
			SFLogger.error( context, String.format( "��ȡ֤������������������,[%s]", e ) );
		}
		return idName;
	}


	/**
	 * ��鵱���Ƿ�����ת�˽���
	 * 
	 * @param context
	 * @throws SFException
	 */
	public static void chkTransfer( Context context ) throws SFException {
		SFLogger.info( context, "chkTransfer()��ʼ" );
		try {
			Object tranCode = SFUtil.getDataValue( context, SFConst.PUBLIC_TX_CODE );// ���������л�ȡ������
			Connection tranConnection = SFUtil.getDataValue( context, SFConst.PUBLIC_TRAN_CONNECTION );
			AcctJour acctJour = new AcctJourDao().qryAcctJourByTxCode( context, tranConnection );
			if( "300102".equals( tranCode ) || "200102".equals( tranCode ) || "100102".equals( tranCode ) ) {
				SFUtil.chkCond( context, acctJour != null, "ST5775", "���շ�����ǩԼ��ת�˽���,��������������ʺ�" );
			} else if( "100101".equals( tranCode ) ) {
				SFUtil.chkCond( context, acctJour != null, "ST5775", "���շ�����ǩԼ��ת�˽���,����������ǩԼ��ϵ" );
			}
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}
		SFLogger.info( context, "chkTransfer()����" );
	}

	/**
	 * ���ַ�����ת������������
	 * 
	 * @param ca
	 *            �ַ�����
	 * @return ��������
	 */
	public static int[] converCharToInt( char[] ca ) {
		int len = ca.length;
		int[] iArr = new int[ len ];
		try {
			for( int i = 0; i < len; i++ ) {
				iArr[ i ] = Integer.parseInt( String.valueOf( ca[ i ] ) );
			}
		} catch( NumberFormatException e ) {
			e.printStackTrace();
		}
		return iArr;
	}

	/**
	 * ������֤
	 * 
	 * @param val
	 * @return ��ȡ�����֡�
	 */
	public static boolean isNum( String val ) {
		return val == null || "".equals( val ) ? false : val.matches( "^[0-9]*$" );
	}

	/** ÿλ��Ȩ���� */
	public static final int power[] = { 7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2 };

	/**
	 * ������֤��ÿλ�Ͷ�Ӧλ�ļ�Ȩ�������֮���ٵõ���ֵ
	 * 
	 * @param iArr
	 * @return ����֤���롣
	 */
	public static int getPowerSum( int[] iArr ) {
		int iSum = 0;
		if( power.length == iArr.length ) {
			for( int i = 0; i < iArr.length; i++ ) {
				for( int j = 0; j < power.length; j++ ) {
					if( i == j ) {
						iSum = iSum + iArr[ i ] * power[ j ];
					}
				}
			}
		}
		return iSum;
	}

	/**
	 * ��power��ֵ��11ȡģ�����������У�����ж�
	 * 
	 * @param iSum
	 * @return У��λ
	 */
	public static String getCheckCode18( int iSum ) {
		String sCode = "";
		switch( iSum % 11 ) {
		case 10:
			sCode = "2";
			break;
		case 9:
			sCode = "3";
			break;
		case 8:
			sCode = "4";
			break;
		case 7:
			sCode = "5";
			break;
		case 6:
			sCode = "6";
			break;
		case 5:
			sCode = "7";
			break;
		case 4:
			sCode = "8";
			break;
		case 3:
			sCode = "9";
			break;
		case 2:
			sCode = "X";
			break;
		case 1:
			sCode = "0";
			break;
		case 0:
			sCode = "1";
			break;
		}
		return sCode;
	}
	
	/**
	 * ��18λ����֤����ת��Ϊ15λ
	 * 
	 * @param invIdCode
	 * @return
	 * @throws SFException
	 */
	public static String converTo15( String invIdCode ) throws SFException {
		String invIdCode15 = invIdCode.substring(0, 6)+invIdCode.substring(8, 17); // ��ȡ��6λ+��ȡ8-17λ=���15λ����֤����		
		return invIdCode15;
	}
	
	
	/**
	 * ��15λ����֤����ת��Ϊ18λ��19**��
	 * 
	 * @param invIdCode
	 * @return
	 * @throws SFException
	 */
	public static String converTo18Card19( String invIdCode ) throws SFException {
		return conver15CardTo18( invIdCode, "19" );
	}

	/**
	 * ��15λ����֤����ת��Ϊ18λ��20**��
	 * 
	 * @param invIdCode
	 * @return
	 * @throws SFException
	 */
	public static String converTo18Card20( String invIdCode ) throws SFException {
		return conver15CardTo18( invIdCode, "20" );
	}

	/**
	 * ��15λ����֤����ת��Ϊ18λ
	 * 
	 * @param idCard
	 *            15λ���ݱ���
	 * @return 18λ���ݱ���
	 */
	private static String conver15CardTo18( String invIdCode, String preYear ) throws SFException {
		String idCard18 = null;
		if( SFUtil.isEmpty( invIdCode ) || invIdCode.length() != 15 ) {
			return null;
		}
		try {
			if( BizUtil.isNum( invIdCode ) ) {
				// ��ȡ����������
				String birthday = invIdCode.substring( 6, 12 );
				Date birthDate;
				birthDate = DateUtil.strToDate( "yyyyMMdd", preYear + birthday );
				Calendar cal = Calendar.getInstance();
				if( birthDate != null )
					cal.setTime( birthDate );
				// ��ȡ������(��ȫ������ʽ,�磺2010)
				String sYear = String.valueOf( cal.get( Calendar.YEAR ) );
				idCard18 = invIdCode.substring( 0, 6 ) + sYear + invIdCode.substring( 8 );
				// ת���ַ�����
				char[] cArr = idCard18.toCharArray();
				if( cArr != null ) {
					int[] iCard = BizUtil.converCharToInt( cArr );
					int iSum17 = BizUtil.getPowerSum( iCard );
					// ��ȡУ��λ
					String sVal = BizUtil.getCheckCode18( iSum17 );
					if( sVal.length() > 0 ) {
						idCard18 += sVal;
					} else {
						return null;
					}
				}
			}
		} catch( Exception e ) {
			throw new SFException( "ST5799", "��15λ����֤����ת��Ϊ18λʧ�ܣ�", e );
		}
		return idCard18;
	}

	/**
	 * ����֤���������־������־
	 * 
	 * @param IdType
	 * @return
	 */
	public static String convInterFlag( String idType ) {
		return ( "4".equals( idType ) || "3".equals( idType ) || "19".equals( idType ) || "20".equals( idType ) ) ? SFConst.INTER_FLAG_DOMESTIC : SFConst.INTER_FLAG_ABROAD;
	}

	/**
	 * 0/20/21���������˽���
	 * 
	 * @param IdType
	 * @return
	 * @throws SFException
	 */
	public static void chkIdType( Context context, String idType, String invType ) throws SFException {
		if( SFConst.INV_TYPE_RETAIL.equals( invType ) && SFConst.ID_TYPE_OTHERS.equals( idType ) ) {
			// ���˿ͻ�0֤�����Ͳ��������˽���
			SFUtil.chkCond( context, "ST5100", String.format( "��֤�����Ͳ���������ҵ��ID_TYPE=[%s]", idType ) );
		} else if( SFConst.INV_TYPE_CORP.equals( invType ) && ( SFConst.ID_TYPE_PERSON_WGGMQTZJ.equals( idType ) || "21".equals( idType ) ) ) {
			// �Թ��ͻ�20/21֤�����Ͳ��������˽���
			SFUtil.chkCond( context, "ST5100", String.format( "��֤�����Ͳ���������ҵ��ID_TYPE=[%s]", idType ) );
		}

	}

	/**
	 * ���ж˽��׽��ױ���У��
	 * 
	 * @param context
	 * @param initSide
	 *            ����
	 * @param curCode
	 *            ����
	 * @return
	 * @throws SFException
	 */
	public static String convCurCode( Context context, String curCode ) throws SFException {
		String initSide = SFUtil.getDataValue( context, "CTX_PUBLIC_INIT_SIDE" );// ��ȡ����
		if( !SFConst.INIT_SIDE_ABBANK.equals( initSide ) && SFUtil.isEmpty( curCode ) ) {
			curCode = SFConst.CUR_CODE_RMB;
		}
		SFUtil.chkCond( context, !SFConst.CUR_CODE_RMB.equals( curCode ), "ST5100", String.format( "���ִ���CUR_CODE=[%s]", curCode ) );
		return curCode;
	}

	/**
	 * ��������ת����ȯ���ʽ�����
	 * 
	 * @param context
	 * @param initSide
	 *            ����
	 * @param chnPwd
	 *            �ʽ�����
	 * @return
	 * @throws SFException
	 */
	public static String convBankChLEncryptPwd( Context context, String secCompCode, String initSide, String invType, String chnPwd ) throws SFException {
		String txtPwd = null;// ���ܺ�����
		String eptPwd = null;// ת���ܺ�����
		try {
			/*
			 * ��ȡȯ����Կ
			 */
			String secretKey = ParamCache.getValue2( "SEC_ENCRYPT", secCompCode );

			// �����ü��ܷ�ʽ���д��͵�����
			String channelStr = ParamCache.getValue2( "SF_SYS", "UNENCRYPTED_CHL" );

			/*
			 * ****************** ������������*****************
			 */
			if( channelStr.indexOf( initSide ) == -1 && "1".equals( invType ) ) {// ���ü��ܷ�ʽ���д��͵�����
				// �Ը��˿ͻ������������ʽ�������ܳ����ģ�����������Ϊ���Ĳ���ת������
				txtPwd = DESUtil.decode( chnPwd, SFConst.EB_ENCRYPT_KEY );
				if( txtPwd != null && txtPwd.length() > 6 ) {
					txtPwd = txtPwd.substring( 0, 6 );
				}
			} else {
				// ���������������ģ�����������Ϊ���ģ�����ת������
				txtPwd = chnPwd;
			}
			/*
			 * *****************����ȯ������*****************
			 */
			// ��Կ��ASCILLת����ʮ������
			String hKey = DESUtil.bytesToHex( secretKey.getBytes() );
			hKey = DESUtil.formatString( hKey, 16 );
			// ����ASCILLת����ʮ������
			String hData = DESUtil.bytesToHex( txtPwd.getBytes() );
			// String hKey=DESUtil.bytesToHex(secretKey.getBytes());
			// ��ʽ���ַ��������㲿�ݲ�0
			hData = DESUtil.formatString( hData, 16 );
			// hData=hData+"0030";
			// ����
			eptPwd = DESUtil.encode( hData, hKey );
			SFUtil.chkCond( context, SFUtil.isEmpty( eptPwd ), "ST4223", "����ȯ���ʽ�����ʧ�ܣ�" );
		} catch( SFException e ) {
			SFLogger.error( context, e.getMessage() );
			throw e;
		} catch( Exception e ) {
			SFLogger.error( context, e.getMessage() );
			throw new SFException( "ST4223", e.getMessage(), e );
		}
		return eptPwd;

	}

	/**
	 * �����ж˽���ת����ȯ���ʽ�����
	 * 
	 * @param context
	 * @param initSide
	 *            ����
	 * @param chnPwd
	 *            �ʽ�����
	 * @return
	 * @throws SFException
	 */
	public static String convCobankEncryptPwd( Context context, String secCompCode, AgtAgentInfo agtInfo, String chnPwd ) throws SFException {
		String txtPwd = null;// ���ܺ�����
		String eptPwd = null;// ת���ܺ�����
		try {
			if( agtInfo == null ) {
				throw new SFException( "ST4223", "֤ȯ�ʽ��ʺ��������ʧ�ܣ��������м�����Ա��ϵ��" );
			}

			/*
			 * ****************** ������������*****************
			 */
			if( SFUtil.isNotEmpty( agtInfo.getPinkey() ) && chnPwd.length() > 6 ) {
				// �������������ʽ�������ܳ����ģ�����������Ϊ���Ĳ���ת������
				txtPwd = DESUtil.decode( chnPwd, agtInfo.getPinkey() );
				if( txtPwd != null && txtPwd.length() > 6 ) {
					txtPwd = txtPwd.substring( 0, 6 );
				}
			} else {
				// ���������������ģ�����������Ϊ���ģ�����ת������
				txtPwd = chnPwd;
			}

			/*
			 * *****************����ȯ������*****************
			 */
			// ��ȡȯ����Կ
			String secretKey = ParamCache.getValue2( "SEC_ENCRYPT", secCompCode );
			// ��Կ��ASCILLת����ʮ������
			String hKey = DESUtil.bytesToHex( secretKey.getBytes() );
			hKey = DESUtil.formatString( hKey, 16 );
			// ����ASCILLת����ʮ������
			String hData = DESUtil.bytesToHex( txtPwd.getBytes() );
			// String hKey=DESUtil.bytesToHex(secretKey.getBytes());
			// ��ʽ���ַ��������㲿�ݲ�0
			hData = DESUtil.formatString( hData, 16 );
			// hData=hData+"0030";
			// ����
			eptPwd = DESUtil.encode( hData, hKey );
			SFUtil.chkCond( context, SFUtil.isEmpty( eptPwd ), "ST4223", "����ȯ���ʽ�����ʧ�ܣ�" );
		} catch( SFException e ) {
			SFLogger.error( context, e.getMessage() );
			throw e;
		} catch( Exception e ) {
			SFLogger.error( context, e.getMessage() );
			throw new SFException( "ST4223", e.getMessage(), e );
		}
		return eptPwd;

	}

	/**
	 * �ͻ�����ת������֤ͨת����
	 * 
	 * @param context
	 * @param invTypeSZT
	 * @return
	 * @throws SFException
	 */
	public static String convSZT2SFInvType( Context context, String invTypeSZT ) throws SFException {
		String invType = null;
		if( SFConst.INV_TYPE_CORP_SZT.equals( invTypeSZT ) ) {
			invType = SFConst.INV_TYPE_CORP;
		} else {
			invType = SFConst.INV_TYPE_RETAIL;
		}
		return invType;
	}

	/**
	 * �ͻ�����ת��������ת��֤ͨ
	 * 
	 * @param context
	 * @param invTypeSZT
	 * @return
	 * @throws SFException
	 */
	public static String convSF2SZTInvType( Context context, String invType ) throws SFException {
		String invTypeSZT = null;
		if( SFConst.INV_TYPE_CORP.equals( invType ) ) {
			invTypeSZT = SFConst.INV_TYPE_CORP_SZT;
		} else {
			invTypeSZT = SFConst.INV_TYPE_RETAIL_SZT;
		}
		return invTypeSZT;
	}

	/**
	 * 
	 * ͨ���ͻ���������Ĭ���Ա�
	 * 
	 * @param context
	 * @param sex
	 * @param invType
	 * @return
	 * @throws SFException
	 */
	public static String convSZT2SFSex( Context context, String sex, String invType ) throws SFException {
		if( SFUtil.isEmpty( sex ) ) {
			if( "1".equals( invType ) ) {
				sex = "M";
			} else {
				sex = "-";
			}
		}
		return sex;
	}

	public static String convSZT2SFNationality( Context context, String nationality ) throws SFException {
		if( SFUtil.isEmpty( nationality ) ) {
			nationality = "CHN";
		}
		return nationality;
	}

	/**
	 * ��������ѯУ�鿨״̬
	 * 
	 * @param context
	 * @param msg
	 * @return
	 * @throws SFException
	 */
	public static Context qryCardAttrClient( Context context, Map<String, Object> msg ) throws SFException {

		Context msgContext = null;
		try {
			Map<String, Object> tmpMsg = new HashMap<String, Object>();
			tmpMsg.put( "ACCT_ID", msg.get( "ACCT_ID" ) );// �˺�ACCT_NO
			QryCardAttrClient qryCardAttrClient = new QryCardAttrClient();
			msgContext = qryCardAttrClient.send( context, tmpMsg );

			String retFlag = SFUtil.getDataValue( msgContext, SFConst.PUBLIC_RET_FLAG );
			IndexedCollection iColl = SFUtil.getDataElement( msgContext, "RET" );
			KeyedCollection kColl = ( KeyedCollection )iColl.getElementAt( 0 );// ��ȡ��Ӧ����
			// String retCode = SFUtil.getDataValue(msgContext, kColl,
			// "RET_CODE");
			String retMsg = SFUtil.getDataValue( msgContext, kColl, "RET_MSG" );// ��Ӧ��Ϣ

			// ��������ʧ�ܻ��쳣,�����˳�
			SFUtil.chkCond( context, !SFConst.RET_SUCCESS.equals( retFlag ), "ST5601", String.format( "������[��ѯ��״̬]ʧ��,��������[%s]", retMsg ) );

			// ��״̬У��
			String resultFlag = "1";// �Ƿ���������ϵͳ��У��λ��0-�ǣ�1-��
			String invType = SFUtil.getDataValue( context, SFConst.PUBLIC_INV_TYPE );// �ͻ�����
			if( SFConst.INV_TYPE_RETAIL.equals( invType ) ) {// ����
				String retStat = SFUtil.getDataValue( msgContext, "SYS_HEAD.RET_STATUS" );
				if( "S".equals( retStat ) ) {
					// У�鿨��ʧ״̬
					IndexedCollection certArrayIColl = SFUtil.getDataElement( msgContext, "MSG_O_CERT_ARRAY" );
					for( int i = 0; certArrayIColl != null && i < certArrayIColl.size(); i++ ) {
						String certType = SFUtil.getDataValue( msgContext, ( KeyedCollection )certArrayIColl.getElementAt( i ), "CERT_TYPE" );// ƾ֤����
						if( "000".equals( certType ) ) {// 000-����Ϊ��
							certArrayIColl = SFUtil.getDataElement( msgContext, "CERT_PROPERTY_ARRAY" );
							String certStatus = SFUtil.getDataValue( msgContext, ( KeyedCollection )certArrayIColl.getElementAt( i ), "CERT_STATUS" );// ƾ֤״̬
							// 73-���п���ͷ��ʧ;74-���п������ʧ
							SFUtil.chkCond( context, "73".equals( certStatus ) || "74".equals( certStatus ), "ST5100", String.format( "��״̬�쳣�����ѹ�ʧ��" ) );
							// 71.˾������
							SFUtil.chkCond( context, "71".equals( certStatus ), "ST5101", String.format( "�����ʺ�״̬������[�˻��ѱ�˾������]�����ܿ�ͨ���������ҵ��" ) );
							// �ж�һ��ͨ���Ƿ����
							SFUtil.chkCond( context, "90".equals( certStatus ), "ST5101", String.format( "�����ʺ�״̬������[һ��ͨ���ѹ���]�����ܿ�ͨ���������ҵ��" ) );
						}
					}
					// У������ſ��˻�
					IndexedCollection acctArrayIColl = SFUtil.getDataElement( msgContext, "ACCT_PROPERTY_ARRAY" );
					for( int i = 0; acctArrayIColl != null && i < acctArrayIColl.size(); i++ ) {
						String acctStatus = SFUtil.getDataValue( msgContext, ( KeyedCollection )acctArrayIColl.getElementAt( i ), "ACCT_STATUS" );// �˻�״̬
						// У������ſ��˻� 51.�Ŵ��ʽ����˻�(��˾)
						SFUtil.chkCond( context, "51".equals( acctStatus ), "ST5102", String.format( "�������˺�Ϊ[�����˺�]�����ܿ�ͨ���������ҵ��" ) );
						// У���������״̬�� 50:���������
						if( "50".equals( acctStatus ) ) {
							resultFlag = "0";// ����������ʺ�
						}
					}
				}
			}
			SFUtil.addDataField( msgContext, "RESULT_FLAG", resultFlag );
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", String.format( "qryCardAttrClient()ʧ��%s", e.getMessage() ) );
		}

		return msgContext;
	}

	/**
	 * �Ͽ��ܲ�ѯ�����ͺ͵ȼ�
	 * 
	 * @param context
	 * @param msg
	 * @return
	 * @throws SFException
	 */
	public static Context qryCardTypeClient( Context context, Map<String, Object> msg ) throws SFException {
		SFLogger.info( context, "��������ѯ�Ƿ���������ʼ" );
		String cardType = null;// ������
		String cardlevel = null;// ���ȼ�
		String lmcard = null;// �Ƿ��������� 1:��
		Context msgContext = null;
		try {

			/**
			 * �Ͽ����ж��Ƿ���������
			 */
			Map<String, Object> tmpMsg = new HashMap<String, Object>();
			tmpMsg.put( "ACCT_ID", msg.get( "ACCT_ID" ) );// �˺�ACCT_NO
			QryCardTypeClient qcTypeClient = new QryCardTypeClient();

			msgContext = qcTypeClient.send( context, tmpMsg ); // ���ͱ���
			String retFlag = SFUtil.getDataValue( msgContext, SFConst.PUBLIC_RET_FLAG );
			IndexedCollection iColl = SFUtil.getDataElement( msgContext, "RET" );
			KeyedCollection kColl = ( KeyedCollection )iColl.getElementAt( 0 );// ��ȡ��Ӧ����
			String respMsg = SFUtil.getDataValue( msgContext, kColl, "RET_MSG" );// ��Ӧ��Ϣ
			String respCode = SFUtil.getDataValue( msgContext, kColl, "RET_CODE" );// ��Ӧ��
			String retstat = SFUtil.getDataValue( msgContext, "SYS_HEAD.RET_STATUS" );

			SFUtil.chkCond( context, !SFConst.RET_SUCCESS.equals( retFlag ), "ST4377", String.format( "��������ѯ�Ƿ�������ʧ��,��������[%s]", respMsg ) );
			SFUtil.chkCond( context, !SFConst.INIT_SIDE_SECU.equals( retstat ) || !SFConst.DEFAULT_TRXCODE.equals( respCode ), "ST4377", String.format( "��������ѯ�Ƿ�������ʧ��,��������[%s]", respMsg ) );

			// ��retstatΪ"S"����Ӧ��Ϊ000000ȡ������
			IndexedCollection cardMsgiColl = SFUtil.getDataElement( msgContext, "MSG_O.CARD_MSG_ARRAY" );
			SFUtil.chkCond( context, ( null == cardMsgiColl || cardMsgiColl.size() <= 0 ), "ST4403", String.format( "��������ѯ��Ƭ���ͳ���" ) );

			// ƥ����֤�������Ŀ����ͣ�һ�����Ͼ������˳�,020,200,300,400�ֱ���֤�տ��ĵȼ���094��095��096��097Ϊ��֤IC��
			for( int i = 0; i < cardMsgiColl.size(); i++ ) {
				cardType = SFUtil.getDataValue( msgContext, ( KeyedCollection )cardMsgiColl.getElementAt( i ), "CARD_TYPE" );
				if( "040".equals( cardType ) || "200".equals( cardType ) || "300".equals( cardType ) || "400".equals( cardType ) || "094".equals( cardType ) || "095".equals( cardType ) || "096".equals( cardType ) || "097".equals( cardType ) ) {
					lmcard = "1";
					break;
				}
			}

			// �жϸ��������Ƿ���ҪǩԼƽ��֤ȯ�����������ֱ���˳�����
			if( "1".equals( lmcard ) ) {
				SFLogger.info( context, String.format( "�˿�cardno[%s]Ϊ������", msg.get( "ACCT_ID" ) ) );
				SFUtil.chkCond( context, ( !SFConst.SECU_PINGANZQ.equals( msg.get( "SEC_COMP_CODE" ) ) && !SFConst.SECU_PINGANXY.equals( msg.get( "SEC_COMP_CODE" ) ) ), "ST5111", String.format( "��֤����������ǩԼ��ƽ��֤ȯ" ) );

				/**
				 * ������ǩԼ�Ͽ����жϿ��ȼ���ʼ
				 */
				tmpMsg = new HashMap<String, Object>();
				tmpMsg.put( "ACCT_ID", msg.get( "ACCT_ID" ) );// �˺�ACCT_NO
				QryCardLevelClient qcLeveClient = new QryCardLevelClient();
				msgContext = qcLeveClient.send( context, tmpMsg ); // ���ͱ���

				retFlag = SFUtil.getDataValue( msgContext, SFConst.PUBLIC_RET_FLAG );
				IndexedCollection retiColl = SFUtil.getDataElement( msgContext, "RET" );
				KeyedCollection ikColl = ( KeyedCollection )retiColl.getElementAt( 0 );// ��ȡ��Ӧ����
				String retMsg = SFUtil.getDataValue( msgContext, ikColl, "RET_MSG" );// ��Ӧ��Ϣ

				// �жϲ�ѯ�Ƿ�ɹ�
				SFUtil.chkCond( context, !SFConst.RET_SUCCESS.equals( retFlag ), "ST4377", String.format( "��������ѯ�������ȼ�ʧ��,��������[%s]", retMsg ) );
				// �������ȼ�
				cardlevel = SFUtil.getDataValue( msgContext, "MSG_O.CARD_LEVEL" );
				SFLogger.info( context, String.format( "������ǩԼƽ��֤ȯ��Ƭ�ȼ�Ϊcardlv[%s]", cardlevel ) );
			}
			SFUtil.addDataField( msgContext, "CARD_LEVEL", cardlevel );
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", String.format( "��������ѯ�����ͺ͵ȼ�ʧ��[%s]", e.getMessage() ) );
		}
		return msgContext;
	}

	/**
	 * ��������ѯ�����ʺſ�ʼ
	 * 
	 * @param context
	 * @param msg
	 * @return
	 * @throws SFException
	 */
	public static Context qryBalClient( Context context ) throws SFException {
		Context msgContext = null;
		try {
			String acctId = SFUtil.getReqDataValue( context, "ACCT_ID" );// �˺�ACCT_NO
			String curCode = SFUtil.getReqDataValue( context, "CUR_CODE" );// ����CCY
			String invName = SFUtil.getReqDataValue( context, "INV_NAME" );// �˻�����ACCT_NAME
			String secCompCode = SFUtil.getReqDataValue( context, "SEC_COMP_CODE" );// ȯ�̴���

			Map<String, Object> tmpMsg = new HashMap<String, Object>();
			tmpMsg.put( "ACCT_ID", acctId );
			tmpMsg.put( "CUR_CODE", curCode );
			tmpMsg.put( "INV_NAME", invName );
			QryBalClient qryBalClient = new QryBalClient();
			msgContext = qryBalClient.send( context, tmpMsg ); // ���ͱ���
			String retFlag = SFUtil.getDataValue( msgContext, SFConst.PUBLIC_RET_FLAG );
			IndexedCollection iColl = SFUtil.getDataElement( msgContext, "RET" );
			KeyedCollection kColl = ( KeyedCollection )iColl.getElementAt( 0 );// ��ȡ��Ӧ����
			String respMsg = SFUtil.getDataValue( msgContext, kColl, "RET_MSG" );// ��Ӧ��Ϣ

			SFUtil.chkCond( context, !SFConst.RET_SUCCESS.equals( retFlag ), "ST5602", String.format( "��������ȡ[�����˺�]ʧ�ܣ���������[%s]", respMsg ) );

			String savAcct = null;
			String branchId = null;
			String invType = SFUtil.getDataValue( context, SFConst.PUBLIC_INV_TYPE );// �ͻ�����
			if( SFConst.INV_TYPE_RETAIL.equals( invType ) ) {// ����
				IndexedCollection iColl1 = SFUtil.getDataElement( msgContext, "MSG_O_ICOLL" );
				kColl = ( KeyedCollection )iColl1.getElementAt( 0 );// ��ȡ��Ӧ����
				savAcct = SFUtil.getDataValue( msgContext, kColl, "ACCT_ID" );// �����ʺ�
				SFLogger.info( context, String.format( "ȡ�������ʺ�[%s]", savAcct ) );
				SFUtil.chkCond( context, SFUtil.isEmpty( savAcct ), "ST5602", String.format( "��������ȡ[�����˺�]ʧ��" ) );

				String pbcAcctType = SFUtil.getDataValue( msgContext, kColl, "PBC_ACCT_TYPE" );// ȡ�����˻�����
				SFLogger.info( context, String.format( "ȡ�����˻�����[%s]", pbcAcctType ) );
				SFUtil.chkCond( context, SFUtil.isEmpty( pbcAcctType ), "ST5602", String.format( "��������ȡ[�����˻�����]ʧ��" ) );

				if( "2".equals( pbcAcctType ) ) {// �����˻�
					SFLogger.info( context, String.format( "��ȯ���ѽ�������˻�У��[%s]", secCompCode ) );
					// û�鵽���������˻�ǩԼ��¼�����ر���
					SecCompData secCompData = SFUtil.getDataValue( context, SFConst.PUBLIC_SECU );// ��ȡȯ����Ϣ
					SFUtil.chkCond( context, !"1".equals( secCompData.getIIAcctFlag() ), "ST5421", String.format( "��ȯ�̲���������[�����˻�ǩԼ]" ) );
					SFLogger.info( context, String.format( "��ȯ�̶����˻�У��ͨ��" ) );
				}
				branchId = SFUtil.getDataValue( msgContext, kColl, "OPEN_DEP_ID" );// ���������
				SFLogger.info( context, String.format( "ȡ�����������[%s]", branchId ) );
				SFUtil.chkCond( context, SFUtil.isEmpty( branchId ), "ST5602", String.format( "��������ȡ[�˺ſ�������]ʧ��" ) );

			}

			SFUtil.addDataField( msgContext, "SAV_ACCT", savAcct );
			SFUtil.addDataField( msgContext, "OPEN_DEP_ID", branchId );
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", String.format( "qryCardAttrClient()ʧ��%s", e.getMessage() ) );
		}
		return msgContext;
	}

	/**
	 * ����BECIFЭ��
	 * 
	 * @param context
	 * @return
	 * @throws SFException
	 */
	public static void addAutoBecif( Context context, Connection tranConnection, Map<String, Object> msg ) throws SFException {
		SFLogger.info( context, "addAutoBecif()��ʼ" );
		try {
			// ����BECIFЭ��
			String openDepId = null;// �Ƽ������
			String secCompCode = SFUtil.objectToString( msg.get( "SEC_COMP_CODE" ) );// ȯ�̴���
			String acctId = SFUtil.objectToString( msg.get( "ACCT_ID" ) );// �����ʺ�
			String initSide = SFUtil.objectToString( msg.get( "INIT_SIDE" ) );// ����
			String becifNo = SFUtil.objectToString( msg.get( "ECIF_NO" ) );
			String invType = SFUtil.objectToString( msg.get( "INV_TYPE" ) );
			String counterID = SFUtil.objectToString( msg.get( "USER_ID" ) );
			String txDate = SFUtil.objectToString( msg.get( "TX_DATE" ) );
			String txTime = SFUtil.objectToString( msg.get( "TX_TIME" ) );

			SFLogger.info( context, String.format( "becifNo:[%s]", becifNo ) );

			AutoBecifDao autoBecifDao = new AutoBecifDao();
			SignAccountDataDao signAccountDataDao = new SignAccountDataDao();
			SecCompData secCompData = SFUtil.getDataValue( context, SFConst.PUBLIC_SECU );// ��ȡȯ����Ϣ
			DBHandler.beginTransaction( context, tranConnection );// ��������

			String subType = null;
			String tpdmFlag = secCompData.getTpdmFlag();
			/* �ж�����֤ͨȯ����������ȯ */
			if( SFConst.TPDM_FLAG_NORMAL.equals( tpdmFlag ) ) {// ��֤ͨȯ
				subType = "R81";
			} else if( SFConst.TPDM_FLAG_MARGIN.equals( tpdmFlag ) ) {// ������ȯ
				subType = "R83";
			}
			// ƴ��Э��� ȯ�̴��루8λ��-Э��С�ࣨR81 ΪA�ɡ�R82ΪB�� ��R83Ϊ������ȯ��-����
			String agreementNo = secCompCode + "-" + subType + "-" + acctId;

			// ��ѯ���Ŷ�Ӧ�ɹ�ǩԼ��ȯ�̵ĸ��������ֻ��һ�������Ǹո�ǩԼ����һ������Ҫ��Э�鵽BECIF
			List<SignAccountData> signAccountDataList = signAccountDataDao.qrySignAccountDataListByAcctId( context, tranConnection, acctId, secCompCode );
			if( signAccountDataList != null && signAccountDataList.size() == 1 && !SFUtil.isEmpty( subType ) && SFUtil.isNotEmpty( becifNo ) && SFConst.INV_TYPE_RETAIL.equals( invType ) ) {

				if( !SFConst.INIT_SIDE_ABBANK.equals( initSide ) ) {// �ǹ���ĵ����������Ľ��ף�������Ա��ΪEB001
					counterID = "EB001";
					openDepId = "9998";
				}

				// ƴ��Э��� ȯ�̴��루8λ��-Э��С�ࣨR81 ΪA�ɡ�R82ΪB�� ��R83Ϊ������ȯ��-����
				AutoBecif autoBecif = new AutoBecif();
				autoBecif.setTxTime( txTime );
				autoBecif.setStatus( "0" );
				autoBecif.setAgreementNo( agreementNo );
				autoBecif.setBecifNo( becifNo );
				autoBecif.setAgreementType( "R8" );
				autoBecif.setAgreementSubType( subType );
				autoBecif.setAgreementStatus( "1" );
				autoBecif.setProductNo( acctId );
				autoBecif.setOpenDate( txDate );
				autoBecif.setCloseDate( "" );
				autoBecif.setDeptNo( openDepId );
				autoBecif.setUserId( counterID );
				autoBecif.setBusinessSeriesCD( SFConst.SYS_SYSID );
				autoBecif.setTxDate( txDate );
				autoBecif.setSubTxSeqId( SFUtil.objectToString( msg.get( "SUB_TX_SEQ_ID" ) ) );
				autoBecifDao.saveAutoBecif( context, tranConnection, autoBecif );

				DBHandler.commitTransaction( context, tranConnection ); // �ύ����
			}
		} catch( SFException e ) {
			SFLogger.error( context, e.getMessage() );
			throw e;
		} catch( Exception e ) {
			SFLogger.error( context, e.getMessage() );
			SFUtil.chkCond( context, "ST4895", String.format( "������ѯ��Э�鵽BECIF����ʧ��" ) );
		}
		SFLogger.info( context, "addAutoBecif()����" );
	}

	/**
	 * 
	 * ���������������� ��������֤�ͻ���Ϣ - ���ָ��˺ͻ����ͻ�
	 * 
	 * @param context
	 * @param msg
	 * @throws SFException
	 */
	public static void chkKeyInvestInfoClient( Context context, Map<String, Object> msg ) throws SFException {
		SFLogger.info( context, "chkKeyInvestInfoClient()��ʼ" );
		try {

			QryKeyInvestinfoClient qryKeyInvestInfoClient = new QryKeyInvestinfoClient();// �ͻ���Ϣ��ѯ
			String invName = SFUtil.getReqDataValue( context, "INV_NAME" );// �ͻ�����
			String invType = SFUtil.getReqDataValue( context, "INV_TYPE" );// �ͻ�����
			String invIdCode = SFUtil.getReqDataValue( context, "INV_ID_CODE" );// ֤������
			String hostIdType = SFUtil.getDataValue( context, SFConst.PUBLIC_ID_TYPE );// ȯ��֤������ת��Ϊ����֤������
			String txcode = SFUtil.getDataValue( context, SFConst.PUBLIC_TX_CODE );// ˽�н�����
			SFLogger.info( context, String.format( "֤������ת��Ϊ����֤������ [%s]", hostIdType ) );

			// ��������֤������
			String hostRetIdType = null;
			// ��������֤������
			String hostInvIdCode = null;

			// ��������ѯ�ͻ���Ϣ
			Context msgContext = qryKeyInvestInfoClient.send( context, msg );

			// ��Ӧ���ķ��ر�ʶ
			String retFlag = SFUtil.getDataValue( msgContext, SFConst.PUBLIC_RET_FLAG );
			IndexedCollection iColl = SFUtil.getDataElement( msgContext, "RET" );
			KeyedCollection kColl = ( KeyedCollection )iColl.getElementAt( 0 );// ��ȡ��Ӧ����
			String retMsg = SFUtil.getDataValue( msgContext, kColl, "RET_MSG" );// ��Ӧ��Ϣ
			SFUtil.chkCond( context, !SFConst.RET_SUCCESS.equals( retFlag ), "ST5603", String.format( "��������ѯ�ͻ���Ϣʧ��[%s]", retMsg ) );

			/* �������ؿͻ����͡��ͻ����롢�ͻ����� */
			KeyedCollection oKeyColl = ( KeyedCollection )msgContext.getDataElement( "MSG_O" );
			String hostInvName = SFUtil.getDataValue( msgContext, oKeyColl, "INV_NAME" );

			if( SFConst.INV_TYPE_RETAIL.equals( invType ) ) {// ���˿ͻ�

				hostRetIdType = SFUtil.getDataValue( msgContext, oKeyColl, "ID_TYPE" );
				hostInvIdCode = SFUtil.getDataValue( msgContext, oKeyColl, "INV_ID_CODE" );

				SFLogger.info( context, String.format( "��ѯ�������ݷ��ؿͻ����Ż�ȡ�ͻ�֤������ [%s]", hostRetIdType ) );
				SFLogger.info( context, String.format( "��ѯ�������ݷ��ؿͻ����Ż�ȡ�ͻ����� [%s]", hostInvName ) );
				SFLogger.info( context, String.format( "��ѯ�������ݷ��ؿͻ����Ż�ȡ�ͻ�֤������ [%s]", hostInvIdCode ) );

				// У��֤������&�ͻ�����
				SFUtil.chkCond( context, ( !hostIdType.equals( hostRetIdType ) ), "ST4090", String.format( "�ͻ�֤����Ϣ����" ) );
				SFUtil.chkCond( context, ( !invName.equals( hostInvName ) ), "ST4531", String.format( "�ͻ����Ʋ���" ) );

				if( SFConst.ID_TYPE_PERSON_SFZ.equals( hostIdType ) ) {

					// У��֤������ ����15,18λת�� ����
					chkRetailInvIdCode( context, invIdCode, hostInvIdCode );
				}
			} else { // �����ͻ�

				SFUtil.chkCond( context, !SFConst.RET_SUCCESS.equals( retFlag ), "ST5601", String.format( "������[��ѯ�ͻ�����]ʧ��" ) );

				hostRetIdType = SFUtil.getDataValue( msgContext, oKeyColl, "GLOBAL_TYPE" );
				hostInvIdCode = SFUtil.getDataValue( msgContext, oKeyColl, "GLOBAL_ID" );
				String finFlag = SFUtil.getDataValue( msgContext, oKeyColl, "FIN_FLAG" );

				SFLogger.info( context, String.format( "��ѯ�������ݷ��ؿͻ����Ż�ȡ�ͻ�֤������ [%s]", hostRetIdType ) );
				SFLogger.info( context, String.format( "��ѯ�������ݷ��ؿͻ����Ż�ȡ�ͻ����� [%s]", hostInvName ) );
				SFLogger.info( context, String.format( "��ѯ�������ݷ��ؿͻ����Ż�ȡ�ͻ�֤������ [%s]", hostInvIdCode ) );
				SFLogger.info( context, String.format( "��ѯ�������ݷ��ؿͻ����Ż�ȡͬҵ��ʶ [%s]", finFlag ) );

				// ͬҵ�˺Ŵ�OTHER_PROVE_FILE_TYPEȡ֤�����ͣ�OTHER_PROVE_FILE_NOȡ֤������
				if( "I".equals( finFlag ) ) {
					hostRetIdType = SFUtil.getDataValue( msgContext, oKeyColl, "OTHER_PROVE_FILE_TYPE" );
					hostInvIdCode = SFUtil.getDataValue( msgContext, oKeyColl, "OTHER_PROVE_FILE_NO" );
				}
				if( "73".equals( hostRetIdType ) ) {
					hostRetIdType = "51";
				}

				// ����֤������ת����֤������
				hostRetIdType = BizUtil.convHost2SF4IdType( msgContext, hostRetIdType );

				// 20180202-�����Թ��ͻ�ͬ���ͻ���Ϣ��Ҫ�����ж�ȯ���й�֤�����ͺͺ���һ����
				if( "100104".equals( txcode ) ) {

					// ����C3011��ѯȯ���й�֤������&����
					QryCardAttrClient qryCardAttrClient = new QryCardAttrClient();
					msgContext = qryCardAttrClient.send( context, msg );

					retFlag = SFUtil.getDataValue( msgContext, SFConst.PUBLIC_RET_FLAG );
					iColl = SFUtil.getDataElement( msgContext, "RET" );
					kColl = ( KeyedCollection )iColl.getElementAt( 0 );// ��ȡ��Ӧ����
					retMsg = SFUtil.getDataValue( msgContext, kColl, "RET_MSG" );// ��Ӧ��Ϣ

					// ��������ʧ�ܻ��쳣,�����˳�
					SFUtil.chkCond( context, !SFConst.RET_SUCCESS.equals( retFlag ), "ST5601", String.format( "������[��ѯ��״̬]ʧ��,��������[%s]", retMsg ) );

					// �����ɹ���ȡȯ���й�֤������&����
					String trustIdType = SFUtil.getDataValue( msgContext, "MSG_O.STOCKJOBBER_TRUST_GLOBAL_TYPE" );// ȯ���й�֤������
					String trustInvCodeId = SFUtil.getDataValue( msgContext, "MSG_O.STOCKJOBBER_TRUST_GLOBAL_NO" );// ȯ���й�֤������

					SFLogger.info( context, String.format( "��ѯ�������ݷ���ȯ���й�֤������ [%s]", trustIdType ) );
					SFLogger.info( context, String.format( "��ѯ�������ݷ���ȯ���й�֤������ [%s]", trustInvCodeId ) );

					SFUtil.chkCond( context, ( ( !hostIdType.equals( BizUtil.convHost2SF4IdType( context, trustIdType ) ) || !invIdCode.equals( trustInvCodeId ) ) && ( !hostIdType.equals( hostRetIdType ) || !invIdCode.equals( hostInvIdCode ) ) ), "ST5040", String.format( "[�ͻ���Ϣ]�������ͻ������ȵ����и�������" ) );
				} else {
					SFUtil.chkCond( context, ( !hostIdType.equals( hostRetIdType ) || !invIdCode.equals( hostInvIdCode ) ), "ST5040", String.format( "[�ͻ���Ϣ]�������ͻ������ȵ����и�������" ) );
				}
			}

		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", String.format( "qryKeyInvestInfoClient()ʧ��%s", e.getMessage() ) );
		}
		SFLogger.info( context, "chkKeyInvestInfoClient()����" );

	}

	/**
	 * 
	 *  �Ϻ����в�ѯ�ͻ���Ϣ
	 * 
	 * @param context
	 * @param tranConnection
	 * @param coBankMsg
	 * @param bankId
	 * @throws SFException
	 */
	public static Context qryKeyInvestorBycoBankClient( Context context, Connection tranConnection, String capAcct, String secCompCode ) throws SFException {
		SFLogger.info( context, "qryKeyInvestorBycoBankClient()��ʼ" );
		Context msgContext = null;
		try {
			String invName = SFUtil.getReqDataValue( context, "INV_NAME" );// �ͻ�����
			String invType = SFUtil.getReqDataValue( context, "INV_TYPE" );// �ͻ�����
			String invIdCode = SFUtil.getReqDataValue( context, "INV_ID_CODE" );// ֤������
			String idType = SFUtil.getReqDataValue( context, "ID_TYPE" );// ֤������

			AgtCustomerInfoDao agtCustomerInfoDao = new AgtCustomerInfoDao();
			AgtCustomerInfo agtCustomerInfo = agtCustomerInfoDao.qryAgtCustomerInfo( context, tranConnection, capAcct, secCompCode );
			SFUtil.chkCond( context, ( null == agtCustomerInfo ), "ST4090", String.format( "�Ϻ����в�ѯ�ͻ�����ʧ��" ) );

			String cobankInvName = agtCustomerInfo.getInvName();
			String cobankIdType = agtCustomerInfo.getIdType();
			String cobankInvIdCode = agtCustomerInfo.getInvidCode();

			SFLogger.info( context, String.format( "��ѯ�����з��ؿͻ����Ż�ȡ�ͻ�֤������ [%s]", cobankIdType ) );
			SFLogger.info( context, String.format( "��ѯ�����з��ؿͻ����Ż�ȡ�ͻ����� [%s]", cobankInvName ) );
			SFLogger.info( context, String.format( "��ѯ�����з��ؿͻ����Ż�ȡ�ͻ�֤������ [%s]", cobankInvIdCode ) );

			// ����֤������ת��Ϊ������֤������
			SFUtil.chkCond( context, ( !invName.equals( cobankInvName ) || ( !cobankIdType.equals( idType ) ) ), "ST4090", String.format( "�ͻ�������Ϣ�˶Բ���" ) );

			// У��֤�������Ƿ�һ��
			if( !invIdCode.equals( cobankInvIdCode ) ) {
				if( SFConst.INV_TYPE_RETAIL.equals( invType ) && SFConst.ID_TYPE_PERSON_SFZ.equals( idType ) ) {

					// У��֤������ ����15,18λת�� ����
					chkRetailInvIdCode( context, invIdCode, cobankInvIdCode );
				} else {
					SFUtil.chkCond( context, "ST5040", String.format( "�ͻ�֤�������������ϵͳ��һ��,�ͻ������ȵ����н��и�������" ) );
				}
			}

		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", String.format( "qryKeyInvestorBycoBankClient()ʧ��%s", e.getMessage() ) );
		}
		SFLogger.info( context, "qryKeyInvestorBycoBankClient()����" );
		return msgContext;
	}

	/**
	 * 
	 * ���������������� �Ϻ�������֤�ͻ���Ϣ
	 * 612326 �Ϻ�����У��һ��ʽǩԼ�ͻ���Ϣ
	 * 
	 * @param context
	 * @param tranConnection
	 * @param coBankMsg
	 * @param bankId
	 * @throws SFException
	 */
	public static Context chkKeyInvestorBycoBankClient( Context context, Connection tranConnection, Map<String, Object> coBankMsg, String bankId ) throws SFException {
		SFLogger.info( context, "qryKeyInvestorBycoBankClient()��ʼ" );
		Context msgContext = null;
		try {
			String invName = SFUtil.getReqDataValue( context, "INV_NAME" );// �ͻ�����
			String invType = SFUtil.getReqDataValue( context, "INV_TYPE" );// �ͻ�����
			String invIdCode = SFUtil.getReqDataValue( context, "INV_ID_CODE" );// ֤������
			String hostIdType = SFUtil.getDataValue( context, SFConst.PUBLIC_ID_TYPE );// ȯ��֤������ת��Ϊ����֤������

			// tranToHost 612326 �Ϻ�����У��һ��ʽǩԼ�ͻ���Ϣ
			T810026Client qryKeyInvestorBycoBankClient = new T810026Client();// �����з����˻���Ϣ�Լ��˻�״̬
			msgContext = qryKeyInvestorBycoBankClient.send( context, coBankMsg, bankId );

			// ���ر�����Ϣ
			String retFlag = SFUtil.getDataValue( msgContext, SFConst.PUBLIC_RET_FLAG );
			String respCode = SFUtil.getDataValue( msgContext, "A_REQUEST_HEAD.RESPCODE" );// ���ر��Ļ�ȡ������&������Ϣ

			// �Ϻ�������ȷ����ʧ��
			if( SFConst.RET_FAILURE.equals( retFlag ) ) {
				// ���ݴ���������ݿ����ҳ���Ӧ�Ĵ�����Ϣ
				BankCodeInfoDao bankCodeInfoDao = new BankCodeInfoDao();
				BankCodeInfo bankCodeInfo = bankCodeInfoDao.qryBankCodeInfo( context, tranConnection, respCode );
				if( null != bankCodeInfo ) {
					SFUtil.chkCond( context, ( !SFConst.RET_SUCCESS.equals( retFlag ) ), "ST4091", String.format( bankCodeInfo.getMsg() ) );
				} else {
					SFLogger.info( context, "���ݿ���δ���������������,������" );
				}
			}

			// �Ϻ����г�ʱ���쳣
			SFUtil.chkCond( context, ( SFConst.RET_OVERTIME.equals( retFlag ) ), "ST5799", String.format( "���д���ʧ��" ) );

			// �Ϻ����з��سɹ�
			KeyedCollection oKeyCol = SFUtil.getDataElement( msgContext, "810026_O" );
			String coBankInvName = SFUtil.getDataValue( msgContext, oKeyCol, "INV_NAME" );// ���ж˿ͻ�����InvName
			String coBankHostIdType = SFUtil.getDataValue( msgContext, oKeyCol, "ID_TYPE" );// ���ж�֤������IdType
			String coBankInvIdCode = SFUtil.getDataValue( msgContext, oKeyCol, "INV_ID_CODE" );// ���ж�֤������InvIdCode

			SFLogger.info( context, String.format( "��ѯ�����з��ؿͻ����Ż�ȡ�ͻ�֤������ [%s]", coBankHostIdType ) );
			SFLogger.info( context, String.format( "��ѯ�����з��ؿͻ����Ż�ȡ�ͻ����� [%s]", coBankInvName ) );
			SFLogger.info( context, String.format( "��ѯ�����з��ؿͻ����Ż�ȡ�ͻ�֤������ [%s]", coBankInvIdCode ) );

			// ����֤������ת��Ϊ������֤������
			String dlHostIdType = BizUtil.convSF2CoBank4IdType( context, hostIdType );
			SFUtil.chkCond( context, ( !invName.equals( coBankInvName ) ), "ST4531", String.format( "�ͻ����Ʋ���" ) );
			SFUtil.chkCond( context, ( !dlHostIdType.equals( coBankHostIdType ) ), "ST4090", String.format( "�ͻ�֤����Ϣ����" ) );

			// У��֤�������Ƿ�һ��
			if( !coBankInvIdCode.equals( invIdCode ) ) {
				if( SFConst.INV_TYPE_RETAIL.equals( invType ) && SFConst.ID_TYPE_PERSON_SFZ.equals( hostIdType ) ) {
					// У��֤������ ����15,18λת�� ����
					chkRetailInvIdCode( context, invIdCode, coBankInvIdCode );
				} else {
					SFUtil.chkCond( context, "ST5040", String.format( "�ͻ�֤�������������ϵͳ��һ��,�ͻ������ȵ����н��и�������" ) );
				}
			}
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", String.format( "qryKeyInvestorBycoBankClient()ʧ��%s", e.getMessage() ) );
		}
		SFLogger.info( context, "qryKeyInvestorBycoBankClient()����" );
		return msgContext;
	}

	/**
	 * У���ܸ��˿ͻ�֤�����������������з���֤������
	 * ֤������ת�� ����15,18λ����
	 * 
	 * @param context��������
	 * @param invIdCode�����֤������
	 * @param hostInvIdCode������������з���֤������
	 * @throws SFException�������쳣
	 */
	public static void chkRetailInvIdCode( Context context, String invIdCode, String hostInvIdCode ) throws SFException {
		SFLogger.info( context, "chkRetailInvIdCode()��ʼ" );
		String invIdCode18Card19 = null; // 15λת18λ֤�����룬20����
		String invIdCode18Card20 = null; // 15λת18λ֤�����룬21����
		String hostInvIdCode18Card19 = null; // ����������з���֤������15λת18λ֤�����룬20����
		String hostInvIdCode18Card20 = null; // ����������з���֤������15λת18λ֤�����룬21����
		try {

			// ����1�����߶���18λ����ת�� 2. ����15λ������18λ 3.����18λ������15λ 4.��������������15λ
			if( SFUtil.isNotEmpty( hostInvIdCode ) && hostInvIdCode.length() == 18 && SFUtil.isNotEmpty( invIdCode ) && invIdCode.length() == 18 ) {
				SFUtil.chkCond( context, ( !hostInvIdCode.equals( invIdCode ) ), "ST4090", String.format( "�ͻ�������Ϣ�˶Բ���" ) );
			}
			// ����2������15λ������15λ
			if( SFUtil.isNotEmpty( invIdCode ) && invIdCode.length() == 15 && SFUtil.isNotEmpty( hostInvIdCode ) && hostInvIdCode.length() == 15 ) {
				SFUtil.chkCond( context, ( !hostInvIdCode.equals( invIdCode ) ), "ST4090", String.format( "�ͻ�������Ϣ�˶Բ���" ) );
			}

			// ����3������15λ������18λ[����֤������15λת18λ������������18λ֤������Ƚ�]
			if( SFUtil.isNotEmpty( invIdCode ) && invIdCode.length() == 15 && SFUtil.isNotEmpty( hostInvIdCode ) && hostInvIdCode.length() == 18 ) {
				invIdCode18Card19 = BizUtil.converTo18Card19( invIdCode );
				invIdCode18Card20 = BizUtil.converTo18Card20( invIdCode );
				if( ( SFUtil.isNotEmpty( invIdCode18Card19 ) && SFUtil.isNotEmpty( invIdCode18Card20 ) ) && !invIdCode18Card19.equals( hostInvIdCode ) && ( !invIdCode18Card20.equals( hostInvIdCode ) ) ) {
					SFUtil.chkCond( context, "ST4090", String.format( "�ͻ�������Ϣ�˶Բ���" ) );
				}
			}

			// ����4������18λ������15λ[��������֤������15λת18λ��������֤������Ƚ�]
			if( SFUtil.isNotEmpty( invIdCode ) && invIdCode.length() == 18 && SFUtil.isNotEmpty( hostInvIdCode ) && hostInvIdCode.length() == 15 ) {
				hostInvIdCode18Card19 = BizUtil.converTo18Card19( hostInvIdCode );
				hostInvIdCode18Card20 = BizUtil.converTo18Card20( hostInvIdCode );
				if( SFUtil.isNotEmpty( hostInvIdCode18Card19 ) && SFUtil.isNotEmpty( hostInvIdCode18Card20 ) && !invIdCode.equals( hostInvIdCode18Card19 ) && ( !invIdCode.equals( hostInvIdCode18Card20 ) ) ) {
					SFUtil.chkCond( context, "ST4090", String.format( "�ͻ�������Ϣ�˶Բ���" ) );
				}
			}
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", String.format( "chkRetailInvIdCode()ʧ��%s", e.getMessage() ) );
		}
		SFLogger.info( context, "chkRetailInvIdCode()����" );
	}

	/**
	 * ���������������� �������ÿ�״̬��
	 * 
	 * @param context
	 * @return
	 * @throws SFException
	 */
	public static Context setCardStatusWord( Context context, String acctId, String funcCode, String openDepId ) throws SFException {
		SFLogger.info( context, "������ά����״̬�ֿ�ʼ" );
		Context msgContext = null;
		String retFlag = null;
		String respMsg = null;
		String respCode = null;
		try {
			// ��չ������/�Թ��˺�
			Map<String, Object> msg = new HashMap<String, Object>();
			msg.put( "BIZ_SEQ_NO", BizUtil.getChlSeqId( context, getSubTxSeqId( BizUtil.getInitSeqId( context ) ) ) );// ������22λ��ˮSYS_HEAD.CONSUMER_SEQ_NO�ֶ�
			msg.put( "ACCT_ID", acctId );// �˺�ACCT_NO
			msg.put( "FUNCTION_CODE", funcCode );// ������ A-����
			if( SFUtil.isNotEmpty( openDepId ) ) {
				msg.put( "BRANCH_ID", openDepId );// ��������
			}
			SetCardStatusWordClient setCardStatusWordClient = new SetCardStatusWordClient();// ���ÿ�״̬
			msgContext = setCardStatusWordClient.send( context, msg );

			retFlag = SFUtil.getDataValue( msgContext, SFConst.PUBLIC_RET_FLAG );

			if( !SFConst.RET_SUCCESS.equals( retFlag ) ) {
				IndexedCollection iColl1 = SFUtil.getDataElement( msgContext, "RET" );
				KeyedCollection kColl = ( KeyedCollection )iColl1.getElementAt( 0 );// ��ȡ��Ӧ����

				respCode = SFUtil.getDataValue( msgContext, kColl, "RET_CODE" );
				respMsg = SFUtil.getDataValue( msgContext, kColl, "RET_MSG" );

				// �Ѵ��ڻ��߲�����[�������й��˻�]״̬������ȯ���ÿ�״̬�ɹ�
				if( ( respCode.endsWith( "ME4022" ) && "A".equals( funcCode ) ) || ( respCode.endsWith( "ME4021" ) && "D".equals( funcCode ) ) ) {
					retFlag = SFConst.RET_SUCCESS;
				}
			}

			SFUtil.chkCond( context, !SFConst.RET_SUCCESS.equals( retFlag ), "ST5601", respMsg );

		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}
		SFLogger.info( context, "������ά����״̬�ֽ���" );
		return msgContext;
	}

	/**
	 * ����ȯ��Ӫҵ�����룬�������֤ȯ�����֤ȯ
	 * 
	 * @param secCompCode
	 * @param map
	 * @param secBrchId
	 */
	public static void setSecBrchId( String secCompCode, Map<String, Object> map, String secBrchId ) {
		if( SFConst.SECU_YINHEZQ.equals( secCompCode ) || SFConst.SECU_WUKUANGZQ.equals( secCompCode ) ) {
			map.put( "SEC_BRCH_ID", secBrchId );
		}
	}

	/**
	 * �������Ž�ͨΪ����֤ȯ
	 * 
	 * @param context
	 * @throws SFException
	 */
	public static void setZhongXinSecuCompCode( Context context ) throws SFException {
		String secCompCode = SFUtil.getReqDataValue( context, "SEC_COMP_CODE" );// ȯ�̴���
		if( SFConst.SECU_ZHONGXINJTZQ.equals( secCompCode ) ) {// ����12710000ȯ�̴���Ϊ11020000
			SFUtil.setReqDataValue( context, "SEC_COMP_CODE", SFConst.SECU_ZHONGXINZQ );
		}
	}

	/**
	 * 
	 * ����ظ���ˮ
	 * 
	 * @param context
	 * @throws SFException
	 */
	public static void chkRepeatAcctJour( Context context, Connection tranConnection ) throws SFException {
		String secCompCode = SFUtil.getReqDataValue( context, "SEC_COMP_CODE" );// ȯ�̴���
		String acctId = SFUtil.getReqDataValue( context, "ACCT_ID" );// ���п���
		String secSeqId = SFUtil.getReqDataValue( context, "SEC_SEQ_ID" );// ȯ����ˮ��
		String txDate = ( ( LocalInfo )SFUtil.getDataValue( context, SFConst.PUBLIC_LOCAL_INFO ) ).getWorkdate();// ��������
		AcctJour acctJour = new AcctJourDao().qryAcctJourByJourFlag( context, tranConnection, secSeqId, secCompCode, acctId, "00", txDate );
		SFUtil.chkCond( context, ( null != acctJour ), "ST5704", String.format( "[�ͻ���ˮ��]�ظ�" ) );
	}

	/**
	 * �Ƿ����û������Ĭ��ֵ�ж�
	 * 
	 * @param type
	 *            ��������
	 * @return
	 */
	public static String getDefaultParam( String type ) {
		String[] arr = new String[] { "SEC_APP_ID", "SEC_INTEREST_FILE", "SEC_CLEARING_FILE", "SEC_CLOSE_ACCOUNT_FILE", "SEC_OPEN_ACCT_ABNORMAL_FILE", "SEC_INTEREST_FILE_REC", "SEC_CLEARING_FILE_REC", "SEC_CLOSE_ACCOUNT_FILE_REC", "SEC_OPEN_ACCT_ABNORMAL_FILE_REC", "SEC_TRANSFER_FILE_REC", "SEC_CHECKING_FILE_REC", "SEC_INTEREST_FILE_FMT", "SEC_CLEARING_FILE_FMT", "SEC_OPEN_ACCT_ABNORMAL_FILE_FMT", "SEC_TRANSFER_FILE_FMT", "SEC_CHECKING_FILE_FMT", "SEC_INTEREST_TAX", "SEC_ACCT_BAL_CHK" };
		for( int i = 0; i < arr.length; i++ ) {
			if( type.equals( arr[ i ] ) ) {
				return "DEFAULT";
			}
		}
		return null;
	}

	/**
	 * ·������
	 * 
	 * @param context
	 * @param path
	 * @return
	 * @throws Exception
	 */
	public static String processPath( Context context, String path ) throws Exception {
		try {
			SFUtil.chkCond( context, path == null, "ST4589", "·��Ϊ�գ������������" );
			if( !( path.endsWith( "\\" ) || path.endsWith( "/" ) ) )
				path = path + "/";
		} catch( Exception e ) {
			throw e;
		}
		return path;
	}

	/**
	 * ��ȡ��־·��
	 * 
	 * @return
	 */
	public final static String getLogPath() {
		// log���·��
		String logPath = "/logs";
		String sLogPath = SFUtil.getSysProperty( "LOG_PATH" );// ParamCache.getValue2( "SF_LOG", "SYS_LOG_PATH" );
		if( SFUtil.isNotEmpty( sLogPath ) ) {
			logPath = sLogPath;
		}
		return logPath;
	}

	/**
	 * ������MACУ��
	 * 
	 * @param context
	 * @param msgSource
	 * @throws SFException
	 */
	public static void chkCoBankMac( Context context, String msgSource ) throws SFException {
		SFLogger.debug( context, String.format( "������MACУ��:msgSource��%s��", msgSource ) );
		Connection connection = SFUtil.getDataValue( context, SFConst.PUBLIC_TRAN_CONNECTION );
		AgtAgentInfo agentInfo = SFUtil.getDataValue( context, SFConst.PUBLIC_AGENTINFO );// ����������ȡ��������
		if( agentInfo == null ) {
			/*
			 * �ӱ���ͷ�л�ȡ��������Ϣ
			 */
			AgtAgentInfoDao agtAgentInfoDao = new AgtAgentInfoDao();
			String bankNo = null;
			if( !context.containsKey( "A_RESPONSE_HEAD.CHCICSCODE" ) ) {// ����Eͨ���Ĵ�����ͷ��ȡ�������к�
				bankNo = SFUtil.getDataValue( context, "A_REQUEST_HEAD.BANKNO" );// ��ͷ��Ϣȡ��BankId
			} else {
				bankNo = SFUtil.getDataValue( context, "BANK_ID" );// ��ͷ��Ϣȡ��BankId
			}
			if( SFUtil.isEmpty( bankNo ) ) {
				SFLogger.error( context, "����ͷ�л�ȡ��������Ϣʧ�ܣ�" );
				throw new SFException( "ST4528", "����ͷ�л�ȡ��������Ϣʧ�ܣ�" );
			}
			agentInfo = agtAgentInfoDao.qryAgtAgentInfo( context, connection, bankNo );
		}
		if( agentInfo == null ) {
			SFLogger.error( context, "�������л�ȡ��������Ϣʧ�ܣ�" );
			throw new SFException( "ST4528", "�������л�ȡ��������Ϣʧ�ܣ�" );
		}
		/*
		 * ����MACУ��
		 */
		if( "0".equals( agentInfo.getMacFlag() ) ) {
			return;
		}
		/*
		 * MACУ��
		 */
		try {
			boolean retFlag = MacUtil.checkMac( agentInfo.getMackey(), msgSource );
			if( !retFlag ) {
				SFLogger.error( context, "MACУ�����" );
				throw new SFException( "ST4528", "MACУ�����" );
			}
		} catch( Exception e ) {
			SFLogger.error( context, "MACУ�����" );
			throw new SFException( "ST4528", "MACУ�����" );
		}

	}

	/**
	 * ���ɺ�����MACУ����
	 * 
	 * @param context
	 * @param msgSource
	 * @throws SFException
	 */
	public static String genCoBankMac( Context context, String msgSource ) throws SFException {
		if( SFUtil.isEmpty( msgSource ) || msgSource.length() > 3008 ) {
			SFLogger.error( context, String.format( "���뱨�ĳ���[%d]���ڶ���ı���source����[3000]!", msgSource.length() ) );
			throw new SFException( "ST4406", String.format( "���뱨�ĳ���[%d]���ڶ���ı���source����[3000]!", msgSource.length() ) );
		}
		AgtAgentInfo agentInfo = SFUtil.getDataValue( context, SFConst.PUBLIC_AGENTINFO );// ����������ȡ��������

		/**
		 * �������������δȡ����������Ϣ���ٽ��в�ѯ
		 */
		if( agentInfo == null ) {
			/*
			 * �ӱ���ͷ�л�ȡ��������Ϣ
			 */
			AgtAgentInfoDao agtAgentInfoDao = new AgtAgentInfoDao();
			String bankNo = null;
			if( !context.containsKey( "A_RESPONSE_HEAD.CHCICSCODE" ) ) {// ����Eͨ���Ĵ�����ͷ��ȡ�������к�
				bankNo = SFUtil.getDataValue( context, "A_REQUEST_HEAD.BANKNO" );// ��ͷ��Ϣȡ��BankId
			} else {
				bankNo = SFUtil.getDataValue( context, "BANK_ID" );// ��ͷ��Ϣȡ��BankId
			}
			if( SFUtil.isEmpty( bankNo ) ) {
				SFLogger.error( context, "����ͷ�л�ȡ��������Ϣʧ�ܣ�" );
				throw new SFException( "ST4528", "����ͷ�л�ȡ��������Ϣʧ�ܣ�" );
			}
			Connection connection = SFUtil.getDataValue( context, SFConst.PUBLIC_TRAN_CONNECTION );
			agentInfo = agtAgentInfoDao.qryAgtAgentInfo( context, connection, bankNo );
		}
		if( agentInfo == null ) {
			SFLogger.error( context, "�������л�ȡ��������Ϣʧ�ܣ�" );
			throw new SFException( "ST4406", "�������л�ȡ��������Ϣʧ�ܣ�" );
		}
		// ����MACУ��
		// if ("0".equals(agentInfo.getMacFlag())) {//��ʱע�ͣ�ԭC�������Ƿ���ҪУ��
		// return "";
		// }
		String retMac = "";
		// MAC����
		try {
			retMac = MacUtil.generateMac8( agentInfo.getMackey(), msgSource );
		} catch( Exception e ) {
			SFLogger.error( context, "MACУ�����" );
			throw new SFException( "ST4406", "MACУ�����" );
		}
		SFLogger.debug( context, String.format( "������MACУ�������:msgSource��%s��,MAC��%s��", msgSource, retMac ) );
		return retMac;
	}

	public static int getSourceLength( Context context, int length ) throws SFException {
		AgtAgentInfo agentInfo = SFUtil.getDataValue( context, SFConst.PUBLIC_AGENTINFO );
		if( agentInfo == null ) {
			AgtAgentInfoDao agtAgentInfoDao = new AgtAgentInfoDao();
			String bankNo = SFUtil.getDataValue( context, "A_REQUEST_HEAD.BANKNO" );// ��ͷ��Ϣȡ��BankId
			Connection connection = SFUtil.getDataValue( context, SFConst.PUBLIC_TRAN_CONNECTION );
			agentInfo = agtAgentInfoDao.qryAgtAgentInfo( context, connection, bankNo );
		}
		/*
		 * ����MACУ��
		 */
		if( "0".equals( agentInfo.getMacFlag() ) ) {
			return length;
		}

		return length + 8;
	}

	/**
	 * ��ȡ��������0��Ľ��
	 *����������������
	 * @param context
	 * @param tranAmount
	 * @return
	 * @throws SFException
	 */
	public static String getCobankTranAmount( Context context, String tranAmount ) {
		return SFUtil.fixChar( tranAmount, 13, '0', "left" );
	}

	/**
	 * ���쳣��Ӧ���ĳ���ʱ��ȡ�쳣��Ӧ������ȷ�����ı�
	 *����������������
	 * @param context 
	 * @param retMsg �쳣��Ϣ�ֽ�����
	 * @param len  ��Ҫ��ȡ�ĳ���
	 * @return
	 */
	public static String getResponseMsg( Context context, String retMsg, int len ) {
		int subCount = 0;
		byte[] msg = retMsg.getBytes();
		for( int i = 0; i < len; i++ ) {
			if( msg[ i ] < 0 ) {
				subCount++;
			}
		}
		if( subCount % 2 == 0 ) {
			len = subCount;
		} else {
			len -= 1;
		}
		return new String( msg, 0, len );
	}
	
	
	/**
	 * ��ȡ����������ˮ��
	 *����������������
	 * @param context
	 * @param chlTranSeq ������������ˮ��
	 * @param initSide   ����
	 * @param invType    �ͻ�����
	 * @return
	 */
	public static String getBankSeqid( Context context,String chlTranSeq, String initSide, String invType) {
		if (SFConst.INV_TYPE_RETAIL.equals(invType)&&chlTranSeq!=null){//����
			if (chlTranSeq.length()>21){
				chlTranSeq = chlTranSeq.substring(14, 22);
			}else if (chlTranSeq.length()>14){
				chlTranSeq = chlTranSeq.substring(14);
			} else {
				chlTranSeq = "";
			}
		}else {//�Թ�
			if(!"B".equals(initSide)&&chlTranSeq!=null){
				if (chlTranSeq.length()>23){
					chlTranSeq = chlTranSeq.substring(4, 24);
				}else if (chlTranSeq.length()>4){
					chlTranSeq = chlTranSeq.substring(4);
				} else {
					chlTranSeq = "";
				}
			} else {
				chlTranSeq = "";
			}
		}
		return chlTranSeq;
	}
	

	/**
	 * ��ȡӦ�ñ���
	 * @param context
	 * @return
	 */
	public static String getMacCode( Context context ) {
		return SFUtil.getSysProperty( "APP_CODE" );
	}

	/**
	 * ��ȡӦ������
	 * @param context
	 * @return
	 */
	public static String getMacType( Context context ) {
		return SFUtil.getSysProperty( "APP_TYPE" );
	}
	
	
	/**
	 * ��ȡParam�������Valueֵ
	 * @param context
	 * @param type
	 * @param id
	 * @return
	 */
	public static String getParamValue( Context context,String type ,String id ) {
		String value = null;
		try {
			value = ParamCache.getValue2(type,id);
		} catch (SFException e) {
			SFLogger.error( context, "��ȡ���������" );
		}
		return value;
	}
	
	/**
	 * 
	 *��ȡParam�������Value1ֵ
	 * @param context
	 * @return
	 */
	public static String getParamValue1( Context context,String type,String id ) {
		String value1 = null;
		try {
			Param param=ParamCache.getValue(type,id);
			if (param!=null){
				value1 = param.getValue1();
			}
		} catch (SFException e) {
			SFLogger.error( context, "��ȡ���������" );
		}
		return value1;
	}

	
	/**
	 * ��ȯ�̽�ȡ������Ϣ
	 * ֱ��ȯ�̽�ȡ100λ�ַ�
	 * ��֤ͨȯ�̽�ȡ50λ�ַ�
	 * 
	 * @param context
	 * @param errMsg
	 * @return
	 */
	public static String returnSecuErrMsg( Context context, String errMsg ) throws SFException {
		try {
			String secuType = SFUtil.getDataValue( context, SFConst.PUBLIC_SECU_TYPE );// ȯ������
			SFUtil.chkCond( context, SFUtil.isEmpty( secuType ), "ST5701", String.format( "ȯ�����Ͳ���Ϊ��[%s]", secuType ) );
			if( SFConst.SECU_ZL.equals( secuType ) ) { // ֱ��ģʽ
				errMsg = SFUtil.getSubString( errMsg, 100 );
			} else if( SFConst.SECU_SZT.equals( secuType ) ) { // ��֤ͨģʽ
				errMsg = SFUtil.getSubString( errMsg, 50 );
			} else {
				errMsg = SFUtil.getSubString( errMsg, 80 );
			}
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}
		return errMsg;
	}
	
	/**
	 * ����ȯ�̡�����������ʱ������Ϣ��ȡ60�ַ�
	 * @param context
	 * @param errMsg	������Ϣ
	 * @return
	 */
	public static String returnErrMsg( Context context, String type, String errMsg) throws SFException {
		try {
			int len = 60;
			if ("SZT".equals(type)||"ZL".equals(type)||"COBANK".equals(type)){
				String value = BizUtil.getParamValue(context, type, "SF_ERRMSG_CUT_OUT");
				if (value!=null){
					len = Integer.parseInt(value);
				}
			}				
			errMsg = SFUtil.getSubString( errMsg, len );
		}  catch( Exception e ) {
			SFLogger.error(context,e);
		}
		return errMsg;
	}
}