package common.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;

import com.ecc.emp.core.Context;
import com.ecc.emp.core.EMPException;

import common.exception.SFException;

/**
 * ����������
 */
public class AmtUtil {
	/**
	 * ǧ�ڼ��
	 * 
	 * @param context
	 * @param txAmount
	 * @throws SFException
	 */
	public static void chkMaxAmount( Context context, double txAmount ) throws SFException {
		SFUtil.chkCond( context, txAmount > 100000000000d, "ST4516", "���ʽ��׽���" );
	}

	/**
	 * �Խ��С��λ�Ƿ�Ƿ����м��
	 * @param context
	 * @param txAmount
	 * @throws SFException
	 */
	public static void chkAmtValid( Context context, String txAmount ) throws SFException {
		BigDecimal BigDecAmt = new BigDecimal(txAmount);	//�������
		if ( SFUtil.isNotEmpty(txAmount) && txAmount.contains(".")){//�ж��Ƿ��С��λ
			DecimalFormat df = new DecimalFormat( "#.00" );
			BigDecimal formatAmt = new BigDecimal(df.format(BigDecAmt));//ת����2λС��
			SFUtil.chkCond( context, formatAmt.compareTo(BigDecAmt)!=0, "ST4895", String.format("���׽��С��λ�Ƿ�!txAmount=[%s]" , txAmount));
		}
	}
	
	/**
	 * �����н���100����
	 * 
	 * @param context
	 * @param txAmount
	 *            ���
	 * @return ԪΪ��λ���
	 * @throws SFException
	 */
	public static String conv2CoBankDivAmount( Context context, Object txAmount ) throws SFException {
		if (SFUtil.isNotEmpty(txAmount)){
			if( SFConst.INIT_SIDE_COBANK.equals( SFUtil.getDataValue( context, SFConst.PUBLIC_INIT_SIDE ) ) ) {
				// �����н���100����
				return SFUtil.div( txAmount );
			} 
			return SFUtil.objectToString( txAmount );
		} 
		return "0.00";
	}
	/**
	 * ֱ��ȯ�̽���100����
	 * 
	 * @param context
	 * @param txAmount
	 *            ���
	 * @return ԪΪ��λ���
	 * @throws SFException
	 */
	public static String conv2SecuDivAmount( Context context, Object txAmount ) throws SFException {
		if (SFUtil.isNotEmpty(txAmount) ) {
			if(  SFConst.SECU_ZL.equals( SFUtil.getDataValue( context, SFConst.PUBLIC_SECU_TYPE ) ) ) {
				// ֱ��ȯ�̽���100����
				return SFUtil.div( txAmount );
			}
			return SFUtil.objectToString( txAmount );
		} 
		return "0.00";
	}

	/**
	 * �����н���100����
	 * 
	 * @param context
	 * @param txAmount
	 *            ���
	 * @return �ѷ�Ϊ��λ���
	 * @throws SFException
	 */
	public static String conv2CoBankMulAmount( Context context, Object txAmount ) throws SFException {
		if(SFUtil.isNotEmpty(txAmount)){
			if( SFConst.INIT_SIDE_COBANK.equals( SFUtil.getDataValue( context, SFConst.PUBLIC_INIT_SIDE ) ) ) {
				// �����н���100����
				DecimalFormat df = new DecimalFormat( "###" );
				return df.format( SFUtil.mul( txAmount, 100 ) );
			} 
			return SFUtil.double2String( txAmount, 2 );
		}
		return "0";
	}

	/**
	 * ֱ��ȯ�̽���100����
	 * 
	 * @param context
	 * @param txAmount
	 *            ���
	 * @return ��Ϊ��λ���
	 * @throws SFException
	 */
	public static String conv2SecuMulAmount( Context context, Object txAmount ) throws SFException {
		if(SFUtil.isNotEmpty(txAmount)){
			if( SFConst.SECU_ZL.equals( SFUtil.getDataValue( context, SFConst.PUBLIC_SECU_TYPE ) ) ) {
				// ֱ��ȯ�̽���100����
				DecimalFormat df = new DecimalFormat( "###" );
				return df.format( SFUtil.mul( txAmount, 100 ) );
			} 
			return SFUtil.double2String( txAmount, 2 );
		}
		return "0";
	}
	
	/**
	 * ת���������صĽ��.
	 * 
	 * @param context
	 * @param amount
	 *            :��Ҫת���Ľ��
	 * @param transformFlag
	 *            ��0��ʾ�ҷ����Է�ת�� 1��ʾ�Է����ҷ�ת��
	 * @throws EMPException
	 */
	public static String transferHostAmt( String amount, String transferFlag ) {
		char theLastOne = '0';
		String tmpAmt = null;

		if( "0".equals( transferFlag ) ) {
			theLastOne = amount.charAt( amount.length() - 1 );

			if( amount.startsWith( "-" ) ) {
				tmpAmt = amount.substring( 1, amount.length() - 1 );

				if( theLastOne == '0' )
					theLastOne = '}';
				else if( theLastOne >= '1' && theLastOne <= '9' )
					theLastOne = ( char )( theLastOne + 25 );

				tmpAmt = tmpAmt + theLastOne;
			} else {
				tmpAmt = amount.substring( 0, amount.length() - 1 );

				if( theLastOne == '0' )
					theLastOne = '{';
				else if( theLastOne >= '1' && theLastOne <= '9' )
					theLastOne = ( char )( theLastOne + 16 );

				tmpAmt = tmpAmt + theLastOne;
			}
		} else {
			theLastOne = amount.charAt( amount.length() - 1 );
			tmpAmt = amount.substring( 0, amount.length() - 1 );

			if( theLastOne == '{' ) {
				tmpAmt = tmpAmt + "0";
			} else if( theLastOne == '}' ) {
				tmpAmt = "-" + tmpAmt + "0";
			} else if( theLastOne >= 'A' && theLastOne <= 'I' ) {
				tmpAmt = tmpAmt + ( char )( theLastOne - 16 );
			} else if( theLastOne >= 'J' && theLastOne <= 'R' ) {
				tmpAmt = "-" + tmpAmt + ( char )( theLastOne - 25 );
			} else {
				tmpAmt = tmpAmt + theLastOne;
			}
		}

		return tmpAmt;
	}
}