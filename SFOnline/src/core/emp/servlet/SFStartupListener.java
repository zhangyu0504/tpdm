package core.emp.servlet;

import com.ecc.emp.util.StartupListener;
import common.util.BizUtil;
import common.util.SFConst;
import common.util.SFUtil;
/**
 * 服务启动监听
 * @author 汪华
 *
 */
public class SFStartupListener extends StartupListener{
	public SFStartupListener(){
		super();
		String macCode=SFUtil.getSysProperty( "APP_CODE" );
		System.setProperty("SF.LOGPATH",BizUtil.getLogPath()+"/"+SFConst.DEFAULT_TRXCODE+"-"+macCode + ".log");
	}
}
