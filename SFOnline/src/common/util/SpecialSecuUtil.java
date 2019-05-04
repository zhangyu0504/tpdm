package common.util;

import com.ecc.emp.core.Context;
import common.exception.SFException;

/**
 * ����ȯ�̴���������
 * @author ������
 *
 */
public class SpecialSecuUtil {

	/**
	 * 
	 * �˶Թ㷢���ʽ��˺�
	 * @param context
	 * @param secCompCode
	 * @param capAcct
	 * @param bankCapAcct
	 * @throws SFException
	 */
	public static void chkCapAcctBySecCompCode( Context context, String secCompCode, String capAcct, String bankCapAcct ) throws SFException {
		if( SFConst.SECU_GUANGFAZQ.equals( secCompCode ) || SFConst.SECU_GUANGFAXY.equals( secCompCode ) ) {
			SFUtil.chkCond( context, ( !capAcct.equals( bankCapAcct ) ), "ST5718", String.format( "�ʽ��˺Ų���" ) );
		}
	}
	
	/**
	 * 
	 * ����֤ȯ�����֤ȯ��У��ȯ��Ӫҵ����Ϣ
	 * @param context
	 * @param secCompCode
	 * @param secBrchId
	 * @throws SFException
	 */
	public static void chkSecBrchIdBySecCompCode( Context context, String secCompCode,String secBrchId) throws SFException {
		if( SFConst.SECU_YINHEZQ.equals( secCompCode ) || SFConst.SECU_WUKUANGZQ.equals( secCompCode ) ) {
			SFUtil.chkCond( context, SFUtil.isEmpty( secBrchId ), "ST5701", String.format( "[ȯ��Ӫҵ����Ϣ]����Ϊ��" ) );
		}
	}
		
	
}