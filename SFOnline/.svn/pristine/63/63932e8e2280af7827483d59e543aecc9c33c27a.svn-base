package module.trans.sf2sf;

import module.trans.TranBase;

import com.ecc.emp.core.Context;
import com.ecc.emp.data.InvalidArgumentException;
import com.ecc.emp.data.ObjectNotFoundException;
import common.exception.SFException;
import common.util.SFConst;
import common.util.SFUtil;

import core.cache.CacheManagerService;
import core.communication.access.stzsecu.SZTThreadInitialize;
import core.log.SFLogger;

/**
 * (管理平台发起) 刷新缓存
 * 交易码 : 200481
 * @author 李其聪
 *
 */
public class T200481 extends TranBase {

	/**
	 * 初始化BIZ参数
	 * @param context
	 * @throws SFException
	 */
	public void initialize(Context context) throws SFException{
		
	}
	
	@Override
	public void doHandle(Context context) throws SFException {
		SFLogger.info(context,"刷新缓存啦！");
		//装载缓存服务到上下文
		CacheManagerService systemCache;
		try {
//			Context context = SFUtil.getcontext();
			systemCache = ((CacheManagerService) context.getService((String)context.getDataValue(SFConst.SERVICE_CACHEMANAGER)));
			systemCache.init(context);
			
			/*
			 * 根据参数配置开启深证通轮训线程
			 */
			SZTThreadInitialize threadInitialize=new SZTThreadInitialize();
			threadInitialize.execute();
			
			SFUtil.setDataValue(context, SFConst.CTX_ERRCODE, SFConst.RESPCODE_SUCCCODE);
			SFUtil.setDataValue(context, SFConst.CTX_ERRMSG, "交易成功");
			
		} catch (ObjectNotFoundException e) {
			e.printStackTrace();
			SFUtil.chkCond(context, true, "ST9999", "刷新APP缓存异常");
		} catch (InvalidArgumentException e) {
			SFUtil.chkCond(context, true, "ST9999", "刷新APP缓存异常");
		}
		SFLogger.info(context,"刷新缓存结束啦！");
	}

	@Override
	public void doHost(Context context) throws SFException {
		// TODO Auto-generated method stub

	}

	@Override
	public void doSecu(Context context) throws SFException {
		// TODO Auto-generated method stub

	}

	@Override
	protected void chkStart(Context context) throws SFException {
		SFLogger.info(context,"刷新APP缓存交易 chkStart开始");
		String FLUSH_TYPE = SFUtil.getReqDataValue(context, "FLUSH_TYPE");//卡号CARD_NO
		SFUtil.chkCond(context, !"1".equals(FLUSH_TYPE), "ST9999", "刷新APP缓存交易请求报文参数异常");									
	}

	@Override
	protected void chkEnd(Context context) throws SFException {
		// TODO Auto-generated method stub

	}


		
}
