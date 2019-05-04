package module.cache;

import java.sql.Connection;

import com.ecc.emp.core.Context;
import common.exception.SFException;

import core.cache.CacheHandler;
import core.cache.CacheMap;
import core.communication.access.esb.ESBAccessParam;
import core.communication.access.esb.SFAccessParam;
import core.communication.access.stzsecu.SZTAccessParam;
import core.log.SFLogger;
/**
 * 缓存结果映射类
 * @author 汪华
 *
 */
public class CacheSetting extends CacheHandler{
	private SFLogger logger=SFLogger.getLogger("000000");

	public void init(Context context,Connection connection) throws SFException {
		logger.info("初始化-缓存结果映射开始");
		SZTAccessParam sztParam=new SZTAccessParam();
		/*
		 * 服务端深证通配置
		 */
		String sevAppId=ParamCache.getValue2("SZT","SERVER_APP_ID");
		String sevUserId=ParamCache.getValue2("SZT","SERVER_USER_ID");
		String sevPwd=ParamCache.getValue2("SZT","SERVER_PWD");
		String sevDestAppId=ParamCache.getValue2("SZT","SERVER_DEST_APP_ID");
		String sevDestUserId=ParamCache.getValue2("SZT","SERVER_DEST_USER_ID");
		sztParam.setServerAppId(sevAppId);
		sztParam.setServerUserId(sevUserId);
		sztParam.setServerPwd(sevPwd);
		sztParam.setServerDestAppId(sevDestAppId);
		sztParam.setServerDestUserId(sevDestUserId);
		
		/*
		 * 客户深证配置
		 */
		String cltAppId=ParamCache.getValue2("SZT","CLIENT_APP_ID");
		String cltUserId=ParamCache.getValue2("SZT","CLIENT_USER_ID");
		String cltPwd=ParamCache.getValue2("SZT","CLIENT_PWD");
		String cltDestAppId=ParamCache.getValue2("SZT","CLIENT_DEST_APP_ID");
		String cltDestUserId=ParamCache.getValue2("SZT","CLIENT_DEST_USER_ID");
		sztParam.setClientAppId(cltAppId);
		sztParam.setClientUserId(cltUserId);
		sztParam.setClientPwd(cltPwd);
		sztParam.setClientDestAppId(cltDestAppId);
		sztParam.setClientDestUserId(cltDestUserId);
		
		
		/*
		 *  深证通B股前置机IP配置&dll
		 */
		String sztMrIp=ParamCache.getValue2("SZT","SZT_MR_IP");//B股接入客户端消息路由器的IP地址
		String sztMrPort=ParamCache.getValue2("SZT","SZT_MR_PORT");//B股接入客户端消息路由器的连接端口
		String sztMrIpBak=ParamCache.getValue2("SZT","SZT_MR_IP_BAK");//B股备用消息路由器的IP 地址
		String sztMrPortBak=ParamCache.getValue2("SZT","SZT_MR_PORT_BAK");//B股备用消息路由器的连接端口
		String sztDllPath=ParamCache.getValue2("SZT","SZT_DLL_PATH");//B股深证通动态链接库路径
		sztParam.setMrIp(sztMrIp);
		sztParam.setMrPort(Short.parseShort(sztMrPort));
		sztParam.setMrIpBak(sztMrIpBak);
		sztParam.setMrPortBak(Short.parseShort(sztMrPortBak));
		sztParam.setDllPath(sztDllPath);
		CacheMap.putCache("SZT_ACCESS_PARAM", sztParam);
		
		/*
		 * ESB
		 */
		String esbIp=ParamCache.getValue2("ESB","ESB_IP");//A股ESB地址
		String esbPort=ParamCache.getValue2("ESB","ESB_PORT");//A股ESB端口
		String esbTimeOut=ParamCache.getValue2("ESB","TIME_OUT");//A股ESB超时时间
		ESBAccessParam esbParam=new ESBAccessParam();
		esbParam.setEsbIp(esbIp);
		esbParam.setEsbPort(esbPort);
		esbParam.setEsbTimeOut(Integer.parseInt(esbTimeOut));
		CacheMap.putCache("ESB_ACCESS_PARAM", esbParam);
		
		
		
		/*
		 * 内部通信
		 */
		String SFIp=ParamCache.getValue2("SF_SYS","SFBATCH_IP");//A股ESB地址
		String SFPort=ParamCache.getValue2("SF_SYS","SFBATCH_PORT");//A股ESB端口
		String SFTimeOut=ParamCache.getValue2("SF_SYS","SFBATCH_TIME_OUT");//A股ESB超时时间
		SFAccessParam SFParam=new SFAccessParam();
		SFParam.setSFIp(SFIp);
		SFParam.setSFPort(SFPort);
		SFParam.setSFTimeOut(Integer.parseInt(SFTimeOut));
		CacheMap.putCache("SF_ACCESS_PARAM", SFParam);
		logger.info("初始化-缓存结果映射结束");
	}
}
