package module.trans.bankchl2sf;

import java.util.Map;

import module.dao.SystemDao;
import module.trans.TranBase;

import com.ecc.emp.core.Context;
import common.exception.SFException;
import common.util.SFConst;
import common.util.SFUtil;

import core.log.SFLogger;

/**
 * （F5发起）交易分发监测
 * 交易码 : 200309
 */
public class T200309 extends TranBase {

	@Override
	protected void initialize(Context context) throws SFException {

	}
	@Override
	public void doHandle(Context context) throws SFException {
		SFLogger.info(context, String.format("qryDual()开始"));
		qryDual(context);
		SFLogger.info(context, String.format("qryDual()结束"));
	}
	
	public void qryDual(Context context) throws SFException {
		SystemDao dao=new SystemDao();
		Map<String, Object> result=dao.qryDual(context, tranConnection);
		if(result==null||!result.containsKey("DUAL_VALUE")){
			SFUtil.chkCond(context, "ST4895", "数据库查询默认值失败！");
		}
		SFUtil.setDataValue( context, SFConst.CTX_ERRCODE, "000000" );
		SFUtil.setDataValue( context, SFConst.CTX_ERRMSG, "SUCCESS" );
	}
	
	@Override
	public void doHost(Context context) throws SFException {
		
	}
	
	@Override
	public void doSecu(Context context) throws SFException {
	}

	@Override
	protected void chkStart(Context context) throws SFException {
	}

	@Override
	protected void chkEnd(Context context) throws SFException {
		
	}

}
