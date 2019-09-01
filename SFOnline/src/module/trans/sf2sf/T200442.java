package module.trans.sf2sf;

import java.text.DecimalFormat;
import java.util.Map;

import module.bean.Trans;
import module.cache.TransCache;
import module.trans.TranBase;

import com.ecc.emp.core.Context;
import com.ecc.emp.data.IndexedCollection;
import com.ecc.emp.data.KeyedCollection;
import common.exception.SFException;
import common.util.BizUtil;
import common.util.SFConst;
import common.util.SFUtil;

import core.cache.CacheMap;
import core.log.SFLogger;

/**
 *  实时查询当前应用服务状态(内部通讯接口供管理台调用)
 *  交易说明： 
 *  <p>应用区分直连和非直连，要求直连显示出券商代码和券商名称，当前活动数，活动率，空闲率</p>
 *  <p>非直连显示出交易码和交易名称，当前活动数，活动率，空闲率</p>
 *  
 *  
 * tran code :200442 
 * @author 吕超鸿
 */
public class T200442 extends TranBase {

	@Override
	protected void initialize( Context context ) throws SFException {

	}

	@Override
	public void doHandle( Context context ) throws SFException {
		SFLogger.info( context, "实时查询当前应用服务状态开始" );
		DecimalFormat df = new DecimalFormat( "#.##%" );// 数字格式化，保留两位小数
		try {
			IndexedCollection indexColl = SFUtil.getDataElement( context, "200442_O_ICOLL" );

			// 从系统属性获取应用编号和应用属性
			String macType = BizUtil.getMacType( context );
			// 当应用属于非直连则返回A股联机交易相关信息
			if( SFUtil.isEmpty( macType ) || !SFConst.SYS_SYSNAME.equals( macType ) ) {
				return;
			}
			// if( SFUtil.isNotEmpty( macType ) && SFConst.SYS_SYSNAME.equals( macType ) ) { [ 11家直连券商相关信息无需监测交易状态 20180416 edit]
			Map<String, Trans> transMap = TransCache.getAllValue();
			if( null != transMap ) {// keySet()遍历
				for( Map.Entry<String, Trans> entry : transMap.entrySet() ) {
					String txCode = entry.getKey();// 交易码
					Trans trans = entry.getValue();// 交易对象
					if( SFUtil.isNotEmpty( trans.getType() ) && "0".equals( trans.getType() ) ) {

						// 计算当前交易的连接数:接收请求时增加在线计录数
						Map<String, Integer> mapTrad = CacheMap.getCache( SFConst.SYS_CACHE_TRAD );
						Integer tradReqNum = null != mapTrad.get( txCode ) ? mapTrad.get( txCode ) : 0;// 请求记录数
						Integer maxProc = trans.getMaxProc();// 允许最大活动数
						String activeRate = df.format( ( double )tradReqNum / maxProc );// 活动率(%)
						String idleRate = df.format( ( double )( maxProc - tradReqNum ) / maxProc );// 空闲率(%)

						// 组装返回数据
						KeyedCollection keyColl = new KeyedCollection();
						SFUtil.addDataField( context, keyColl, "APP_CODE", BizUtil.getMacCode( context ) );// 应用编码
						SFUtil.addDataField( context, keyColl, "TX_CODE", trans.getTxCode() );// 交易码(券商代码)
						SFUtil.addDataField( context, keyColl, "TX_NAME", trans.getTxName() );// 交易名称(券商名称)
						SFUtil.addDataField( context, keyColl, "CUR_PROC", String.valueOf( tradReqNum ) );// 当前活动数
						SFUtil.addDataField( context, keyColl, "MAX_PROC", String.valueOf( maxProc ) );// 允许最大活动数
						SFUtil.addDataField( context, keyColl, "ACTIVE_RATE", activeRate );// 活动率(%)
						SFUtil.addDataField( context, keyColl, "IDLE_RATE", idleRate );// 空闲率(%)
						indexColl.add( keyColl );
					}
				}
			}
			// } else {// 返回11家直连券商相关信息
			// Map<String, Param> cacheParam = ParamCache.getGroupParam( "SEC_ZL_MAX_LISTEN_NUM" );
			// if( null != cacheParam ) {// keySet()遍历
			// for( Map.Entry<String, Param> entry : cacheParam.entrySet() ) {
			// String secCompCode = entry.getKey();// 券商代码
			// Param param = entry.getValue();// 券商对象
			//
			// // 计算当前交易的连接数:接收请求时增加在线计录数
			// Map<String, Integer> mapTrad = CacheMap.getCache( SFConst.SYS_CACHE_TRAD );
			// Integer tradReqNum = null != mapTrad.get( secCompCode ) ? mapTrad.get( secCompCode ) : 0;// 请求记录数
			// Integer maxProc = Integer.valueOf( param.getValue() );// 允许最大活动数
			// String activeRate = df.format( ( double )tradReqNum / maxProc );// 活动率(%)
			// String idleRate = df.format( ( double )( maxProc - tradReqNum ) / maxProc );// 空闲率(%)
			//
			// // 组装返回数据
			// KeyedCollection keyColl = new KeyedCollection();
			// SFUtil.addDataField( context, keyColl, "APP_CODE", BizUtil.getMacCode( context ) );// 应用编码
			// SFUtil.addDataField( context, keyColl, "TX_CODE", param.getId() );// 交易码(券商代码)
			// SFUtil.addDataField( context, keyColl, "TX_NAME", param.getName() );// 交易名称(券商名称)
			// SFUtil.addDataField( context, keyColl, "CUR_PROC", String.valueOf( tradReqNum ) );// 当前活动数
			// SFUtil.addDataField( context, keyColl, "MAX_PROC", param.getValue() );// 允许最大活动数
			// SFUtil.addDataField( context, keyColl, "ACTIVE_RATE", activeRate );// 活动率(%)
			// SFUtil.addDataField( context, keyColl, "IDLE_RATE", idleRate );// 空闲率(%)
			// indexColl.add( keyColl );
			// }
			// }
			// }
			SFUtil.setDataValue( context, SFConst.CTX_ERRCODE, SFConst.RESPCODE_SUCCCODE );
			SFUtil.setDataValue( context, SFConst.CTX_ERRMSG, "交易成功" );

		} catch( SFException e ) {
			SFLogger.error( context, e.getMessage() );
			SFUtil.chkCond( context, true, "ST9999", "实时查询当前应用服务状态异常" );
		} catch( Exception e ) {
			SFLogger.error( context, e.getMessage() );
			SFUtil.chkCond( context, true, "ST9999", "实时查询当前应用服务状态异常" );
		}
		SFLogger.info( context, "实时查询当前应用服务状态结束" );
	}

	@Override
	public void doHost( Context context ) throws SFException {

	}

	@Override
	public void doSecu( Context context ) throws SFException {

	}

	@Override
	protected void chkStart( Context context ) throws SFException {

	}

	@Override
	protected void chkEnd( Context context ) throws SFException {

	}
}