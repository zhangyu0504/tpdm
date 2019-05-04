package common.util;

import com.ecc.emp.core.Context;
import common.exception.SFException;

/**
 * 特殊券商处理工具类
 * @author 吕超鸿
 *
 */
public class SpecialSecuUtil {

	/**
	 * 
	 * 核对广发的资金账号
	 * @param context
	 * @param secCompCode
	 * @param capAcct
	 * @param bankCapAcct
	 * @throws SFException
	 */
	public static void chkCapAcctBySecCompCode( Context context, String secCompCode, String capAcct, String bankCapAcct ) throws SFException {
		if( SFConst.SECU_GUANGFAZQ.equals( secCompCode ) || SFConst.SECU_GUANGFAXY.equals( secCompCode ) ) {
			SFUtil.chkCond( context, ( !capAcct.equals( bankCapAcct ) ), "ST5718", String.format( "资金账号不符" ) );
		}
	}
	
	/**
	 * 
	 * 银河证券、五矿证券须校验券商营业部信息
	 * @param context
	 * @param secCompCode
	 * @param secBrchId
	 * @throws SFException
	 */
	public static void chkSecBrchIdBySecCompCode( Context context, String secCompCode,String secBrchId) throws SFException {
		if( SFConst.SECU_YINHEZQ.equals( secCompCode ) || SFConst.SECU_WUKUANGZQ.equals( secCompCode ) ) {
			SFUtil.chkCond( context, SFUtil.isEmpty( secBrchId ), "ST5701", String.format( "[券商营业部信息]不能为空" ) );
		}
	}
		
	
}
