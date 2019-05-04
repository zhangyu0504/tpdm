package module.service;

import java.sql.Connection;

import module.bean.Holiday;
import module.bean.LocalInfo;
import module.bean.ProductInfo;
import module.bean.SecCompData;
import module.bean.SecNoServTime;
import module.bean.SecServStatus;
import module.bean.ServStatus;
import module.cache.ProductInfoCache;
import module.dao.HolidayDao;
import module.dao.HolidayDateDao;
import module.dao.SecNoServTimeDao;
import module.dao.ServStatusDao;

import com.ecc.emp.core.Context;
import common.exception.SFException;
import common.util.DateUtil;
import common.util.SFConst;
import common.util.SFUtil;

import core.log.SFLogger;

public class SecuService {

	/**
	 * ���ϵͳ״̬
	 * 
	 * @param context
	 * @throws SFException
	 * 
	 */
	public static void chkSubCenterStatus( Context context ) throws SFException {
		SFLogger.info( context, "chkSubCenterStatus()��ʼ" );
		SFLogger.info( context, "���ϵͳ״̬" );
		try {
			LocalInfo localInfo = SFUtil.getDataValue( context, SFConst.PUBLIC_LOCAL_INFO );
			SFUtil.chkCond( context, !"1".equals( localInfo.getSubCenterStatus() ), "ST5700", "��ǰʱ�䲻������" );
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}
		SFLogger.info( context, "chkSubCenterStatus()����" );
	}

	/**
	 * ��鵱ǰȯ���Ƿ�������иñ��ֽ���
	 * 
	 * @param secComCode
	 *            ȯ�̴���
	 * @param productType
	 *            ��Ʒ����
	 * @param curCode
	 *            ����
	 */
	public static void chkCurCode( Context context, Connection connection ) throws SFException {
		SFLogger.info( context, "chkCurCode()��ʼ" );
		SFLogger.info( context, "��鵱ǰȯ���Ƿ�������иñ��ֽ���" );
		String secCompCode = SFUtil.getReqDataValue( context, "SEC_COMP_CODE" );// ��������ȡ��ȯ�̴���
		String curCode = SFUtil.getReqDataValue( context, "CUR_CODE" );// ȡ������
		try {
			SFUtil.chkCond( context,  SFUtil.isNotEmpty( curCode ) && !curCode.equals(SFConst.CUR_CODE_RMB) , "ST4074", "��ȯ�̲�֧�ִ˱���ҵ��" );
			ProductInfo productInfo = ProductInfoCache.getValue(secCompCode);// ����ȯ�̴����ȡȯ�̲�Ʒ�������
			SFUtil.chkCond( context, productInfo == null || !"1".equals(productInfo.getPermitFlag() ), "ST4074", "��ȯ�̲�֧�ִ˱���ҵ��" );
			SFLogger.info( context, "chkCurCode()����" );
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}
	}

	/**
	 * ���724״̬
	 * 
	 * @param context
	 *            �����Ķ���
	 * @param secCompCode
	 *            ȯ�̴���
	 * @throws SFException
	 * @return 1-��ͨ����ֵ�������Ƿ���ʱ��
	 */
	public static boolean chkSecu724( Context context, Connection connection ) throws SFException {
		SFLogger.info( context, String.format( "chkSecu724()��ʼ" ) );
		SFLogger.info( context, "���ȯ��724״̬" );
		boolean chkFlag = true;
		String secuTxDate = null;// ȯ�����ͽ�������
		String allDayFlag = null;// ��ֵ�����ʶ
		String adBeginTime = null;// ��ֵ����ʼʱ��
		String adEndTime = null;// ��ֵ�������ʱ��
		String txDate = DateUtil.getMacDate();// ��ȡ����
		String txTime = DateUtil.getMacTime();// ��ȡʱ����
		SecCompData secCompData = SFUtil.getDataValue( context, SFConst.PUBLIC_SECU );
		LocalInfo localInfo = SFUtil.getDataValue( context, SFConst.PUBLIC_LOCAL_INFO );// Ӫҵʱ����Ϣ
		String workDate = localInfo.getWorkdate();// ��������
		String bankDate = localInfo.getBankDate();// Ӫҵ����
		SecNoServTime secNoSerTime = null;
		SecNoServTimeDao secNoServTimeDao = new SecNoServTimeDao();
		String secCompCode = null;
		try {
			SFUtil.chkCond( context, ( null == secCompData ), "ST5711", String.format( "��ȯ����Ϣ������" ) );
			secCompCode = secCompData.getSecCompCode();
			allDayFlag = secCompData.getAllDayFlag();
			adBeginTime = secCompData.getADBeginTime();
			adEndTime = secCompData.getADENDTIME();

			if( SFUtil.isNotEmpty( allDayFlag ) && !"0".equals( allDayFlag ) ) {
				if( "1".equals( allDayFlag ) ) {// ������ͨ��7*24Сʱ
					secNoSerTime = secNoServTimeDao.qrySecNoServTimeByBetweenTime( context, connection, secCompCode, txDate, txTime, txTime );
					if( secNoSerTime != null ) {
						chkFlag = false;
					}

					// �޶�724ȯ���ڴ�����յ�ȯ��δ��������µ�������ϵͳʱ�䲻һ����֤����δУ��ʱ��һ���� edit by lch 20180525 11:00 start
					String secuType = secCompData.getSztFlag();
					if( SFUtil.isNotEmpty( secuType ) && SFConst.SECU_ZL.equals( secuType ) ) { // ֱ��ģʽ
						secuTxDate = SFUtil.getDataValue( context, "ZLSECU_REQUEST_HEAD.TXDATE" );// ZLȯ�̽�������
					} else if( SFUtil.isNotEmpty( secuType ) && SFConst.SECU_SZT.equals( secuType ) ) {
						secuTxDate = SFUtil.getDataValue( context, "MsgHdr.Date" );// SZTȯ�̽�������
					}
					if( SFUtil.isNotEmpty( secuTxDate ) && !secuTxDate.equals( workDate ) ) {
						chkFlag = false;
					}
					// �޶�724ȯ���ڴ�����յ�ȯ��δ��������µ�������ϵͳʱ�䲻һ����֤����δУ��ʱ��һ���� edit by lch 20180525 11:00 end

				} else if( "2".equals( allDayFlag ) ) {// ������ͨ��7*8
					if( txDate.equals( bankDate ) && workDate.equals( bankDate ) ) {
						chkFlag = false;
					}
					if( !txDate.equals( bankDate ) && !workDate.equals( bankDate ) ) {/* �ڼ��� */
						if( txTime.compareTo( adBeginTime ) < 0 && txTime.compareTo( adEndTime ) > 0 ) {
							chkFlag = false;
						}
						secNoSerTime = secNoServTimeDao.qrySecNoServTimeByBetweenTime( context, connection, secCompCode, txDate, txTime, txTime );
						if( secNoSerTime != null ) {
							chkFlag = false;
						}
					}
				}
			} else {// δ��ͨ��ֵҵ��
				chkFlag = false;
			}
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}

		SFLogger.info( context, "chkSecu724()����" );
		return chkFlag;
	}

	/**
	 * ���ȯ�̼������Ǳ�־
	 * 
	 * @param context
	 * @param secCompCode
	 * @return 0������������ܽ����� ;1����������
	 */
	public static boolean chkSecuHoliday( Context context, Connection connection ) throws SFException {
		SFLogger.info( context, "chkSecuHoliday()��ʼ" );
		SFLogger.info( context, "���ȯ�̼������Ǳ�ʶ" );
		boolean chkFlag = false;
		String holidayFlag = null;
		String startTime = null;
		String endTime = null;// ��ʼ���������Ǳ�־���ֶ�
		SecCompData secCompData = SFUtil.getDataValue( context, SFConst.PUBLIC_SECU );
		String txTime = DateUtil.getMacTime();
		String tranDate = DateUtil.getMacDate();
		Holiday holiday = new HolidayDao().qryHoliday( context, connection,tranDate );
		try {
			String secCompCode = null;
			if( secCompData != null ) {
				secCompCode = secCompData.getSecCompCode();// ��ȡȯ�̴���
				holidayFlag = secCompData.getHolidayFlag();
				startTime = secCompData.getStartTime();
				endTime = secCompData.getEndTime();
			}

			if( "1".equals( holidayFlag ) && holiday != null && "1".equals(holiday.getHoliDayFlag())) {// �п�ͨ���������ж��Ƿ��ڷ���ʱ����
				if( ( txTime.compareTo( endTime ) < 0 && txTime.compareTo( startTime ) > 0 ) && new HolidayDateDao().qryHolidayData( context, connection, secCompCode, tranDate ) == null ) {
					chkFlag = true;
				}
			}
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}
		SFLogger.info( context, "chkSecuHoliday()����" );
		return chkFlag;
	}

	/**
	 * ������ͣ��״̬
	 * 
	 * @param context
	 * @return 0-ֹͣ 1-����
	 * @throws SFException
	 */
	public static boolean chkSecuStatus( Context context, Connection connection ) throws SFException {
		SFLogger.info( context, "chkSecuStatus()��ʼ" );
		SFLogger.info( context, "������ͣ��״̬" );
		boolean chkFlag = true;
		try {
			LocalInfo localInfo = SFUtil.getDataValue( context, SFConst.PUBLIC_LOCAL_INFO );
			ServStatus servStatus = new ServStatusDao().qryServStatus( context, connection, localInfo.getBankDate() );
			if( servStatus != null ) {
				chkFlag = false;
			}
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}
		SFLogger.info( context, "chkSecuStatus()����" );
		return chkFlag;
	}

	/**
	 * ����˻�������־
	 * 
	 * @param context
	 * @param secCompCode
	 *            ȯ�̴���
	 * @return 0-ֹͣ 1-����
	 * @throws SFException
	 */
	public static void chkSecuAcctFlag( Context context, Connection connection ) throws SFException {
		SFLogger.info( context, "chkSecuAcctFlag()��ʼ" );
		SFLogger.info( context, "����˻�������־" );
		try {
			SecServStatus secSerStatus = SFUtil.getDataValue( context, SFConst.PUBLIC_SEC_SERV_STATUS );
			SFUtil.chkCond( context, secSerStatus == null || "0".equals( secSerStatus.getAcctServFlag() ), "ST4371", "δ�����˻������" );
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}

		SFLogger.info( context, "chkSecuAcctFlag()����" );
	}

	/**
	 * ��鵥����Ϣ�����־
	 * 
	 * @param context
	 * @param secCompCode
	 *            ȯ�̴���
	 * @return 0-ֹͣ 1-����
	 * @throws SFException
	 */
	public static void chkSecuAccrualFlag( Context context ) throws SFException {
		SFLogger.info( context, "chkAccrualFlag()��ʼ" );
		SFLogger.info( context, "��鵥����Ϣ�����־" );
		try {
			SecServStatus secServStatus = SFUtil.getDataValue( context, SFConst.PUBLIC_SEC_SERV_STATUS );
			SFUtil.chkCond( context, secServStatus == null || "0".equals( secServStatus.getIntServFlag() ), "ST4371", "δ����������Ϣ����" );
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}
		SFLogger.info( context, "chkAccrualFlag()����" );
	}

	/**
	 * ����Ƿ��������з�����ת֤
	 * 
	 * @param context
	 * @param secCompCode
	 *            ȯ�̴���
	 * @return 0-ֹͣ 1-����
	 * @throws SFException
	 */
	public static void chkSecuBankChlB2S( Context context ) throws SFException {
		SFLogger.info( context, "chkBankChlB2S()��ʼ" );
		SFLogger.info( context, "����Ƿ��������з�����ת֤" );
		SecCompData secCompData = SFUtil.getDataValue( context, SFConst.PUBLIC_SECU );
		SFUtil.chkCond( context, secCompData == null, "ST5705", "ȯ�̴���Ϊ��" );
		try {
			SecServStatus secServStatus = SFUtil.getDataValue( context, SFConst.PUBLIC_SEC_SERV_STATUS );
			SFUtil.chkCond( context, secServStatus == null || "0".equals( secServStatus.getTransServFlag1() ), "ST4371", "���������з�����ת֤" );
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}
		SFLogger.info( context, "chkBankChlB2S()����" );

	}

	/**
	 * ����Ƿ��������з���֤ת��
	 * 
	 * @param context
	 * @param secCompCode
	 *            ȯ�̴���
	 * @return 0-ֹͣ 1-����
	 * @throws SFException
	 */
	public static void chkSecuBankChlS2B( Context context ) throws SFException {
		SFLogger.info( context, "chkBankChlS2B()��ʼ" );
		SFLogger.info( context, "����Ƿ��������з���֤ת��" );
		SecCompData secCompData = SFUtil.getDataValue( context, SFConst.PUBLIC_SECU );
		SFUtil.chkCond( context, secCompData == null, "ST5705", "ȯ�̴���Ϊ��" );
		try {
			SecServStatus secServStatus = SFUtil.getDataValue( context, SFConst.PUBLIC_SEC_SERV_STATUS );
			SFUtil.chkCond( context, secServStatus == null || "0".equals( secServStatus.getTransServFlag2() ), "ST4371", "���������з���֤ת��" );
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}
		SFLogger.info( context, "chkBankChlS2B()����" );

	}

	/**
	 * ����Ƿ�����ȯ�̷�����ת֤
	 * 
	 * @param context
	 * @param secCompCode
	 *            ȯ�̴���
	 * @return 0-ֹͣ 1-����
	 * @throws SFException
	 */
	public static void chkSecuChlB2S( Context context ) throws SFException {
		SFLogger.info( context, "chkSecuChlB2S()��ʼ" );
		SFLogger.info( context, "����Ƿ�����ȯ�̷�����ת֤" );
		try {
			SecServStatus secServStatus = SFUtil.getDataValue( context, SFConst.PUBLIC_SEC_SERV_STATUS );
			SFUtil.chkCond( context, secServStatus == null || "0".equals( secServStatus.getTransServFlag3() ), "ST4371", "������ȯ�̷�����ת֤" );
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}
		SFLogger.info( context, "chkSecuChlB2S()����" );

	}

	/**
	 * ����Ƿ�����ȯ�̷���֤ת��
	 * 
	 * @param context
	 * @param secCompCode
	 *            ȯ�̴���
	 * @return 0-ֹͣ 1-����
	 * @throws SFException
	 */
	public static void chkSecuChlS2B( Context context ) throws SFException {
		SFLogger.info( context, "chkSecuChlS2B()��ʼ" );
		SFLogger.info( context, "����Ƿ�����ȯ�̷���֤ת��" );
		try {
			SecServStatus secServStatus = SFUtil.getDataValue( context, SFConst.PUBLIC_SEC_SERV_STATUS );
			SFUtil.chkCond( context, secServStatus == null || "0".equals( secServStatus.getTransServFlag4() ), "ST4371", "������ȯ�̷���֤ת��" );
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}
		SFLogger.info( context, "chkSecuChlS2B()����" );
	}

}
