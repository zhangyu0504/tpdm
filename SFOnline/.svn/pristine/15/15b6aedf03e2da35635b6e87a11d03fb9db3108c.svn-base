package module.trans.sf2bankchl;

import java.util.HashMap;
import java.util.Map;

import module.communication.ESBClientBase;

import com.ecc.emp.core.Context;
import com.ecc.emp.data.KeyedCollection;
import common.exception.SFException;
import common.util.SFConst;
import common.util.SFUtil;

import core.log.SFLogger;

/**
 * 查询卡状态是否正常:R3036=账户信息查询;C3011=对公账户信息查询
 * @author 汪华
 *
 */
public class QryCardAttrClient extends ESBClientBase {

	protected Context doHandle( Context context, Map<String, Object> msg ) throws SFException {
		Context msgContext = null;
		String invType = SFUtil.getDataValue( context, SFConst.PUBLIC_INV_TYPE );// 客户类型
		if( SFConst.INV_TYPE_RETAIL.equals( invType ) ) {// 零售
			msgContext = this.qryRetailAcctInfo( context, msg );
		} else if( SFConst.INV_TYPE_CORP.equals( invType ) ) {// 对公
			msgContext = this.qryCorpAcctInfo( context, msg );
		}
		return msgContext;
	}

	/**
	 * 11003000038.28 R3036 账户信息查询
	 * @param context
	 * @return
	 * @throws SFException
	 */
	public Context qryRetailAcctInfo( Context context, Map<String, Object> msg ) throws SFException {
		SFLogger.info( context, "上主机账户信息查询[R3036]-开始" );
		// 发送报文
		KeyedCollection appKcoll = null;
		try {
			if( context.containsKey( "APP_HEAD" ) ) {
				appKcoll = SFUtil.getDataElement( context, "APP_HEAD" );
				appKcoll = ( KeyedCollection )appKcoll.clone();
			} else {
				appKcoll = SFUtil.getTemplateKColl( context, "APP_HEAD" );
				appKcoll = ( KeyedCollection )appKcoll.clone();
				SFUtil.addDataElement( context, appKcoll );
			}
		} catch( Exception e ) {
			// TODO: handle exception
			e.printStackTrace();
		}

		KeyedCollection keyColl = new KeyedCollection( "MSG_I" );
		SFUtil.addDataField( context, keyColl, "ACCT_ID", msg.get( "ACCT_ID" ) );// 账号
		SFUtil.addDataField( context, keyColl, "INFO_OBTAIN_FLAG", "Y" );// 信息获取标志
		Map<String, Object> tmpMsg = new HashMap<String, Object>();
		tmpMsg.put( "MSG_I", keyColl );
		if( SFUtil.isNotEmpty( appKcoll ) )
			tmpMsg.put( "APP_HEAD", appKcoll );
		Context msgContext = super.send( context, tmpMsg, "R3036", "11003000038_28" );
		SFLogger.info( context, "上主机账户信息查询[R3036]-结束" );
		return msgContext;
	}

	/**
	 * 11003000038.46 C3011 对公账户信息查询
	 * @param context
	 * @return
	 * @throws SFException
	 */
	public Context qryCorpAcctInfo( Context context, Map<String, Object> msg ) throws SFException {
		SFLogger.info( context, "上主机对公账户信息查询[C3011]-开始" );
		// 发送报文
		KeyedCollection appKcoll = null;
		try {
			if( context.containsKey( "APP_HEAD" ) ) {
				appKcoll = SFUtil.getDataElement( context, "APP_HEAD" );
				appKcoll = ( KeyedCollection )appKcoll.clone();
			} else {
				appKcoll = SFUtil.getTemplateKColl( context, "APP_HEAD" );
				appKcoll = ( KeyedCollection )appKcoll.clone();
				SFUtil.addDataElement( context, appKcoll );
			}
		} catch( Exception e ) {
			// TODO: handle exception
			e.printStackTrace();
		}

		KeyedCollection keyColl = new KeyedCollection( "MSG_I" );
		SFUtil.addDataField( context, keyColl, "ACCT_ID", msg.get( "ACCT_ID" ) );// 账号
		Map<String, Object> tmpMsg = new HashMap<String, Object>();
		tmpMsg.put( "MSG_I", keyColl );
		if( SFUtil.isNotEmpty( appKcoll ) )
			tmpMsg.put( "APP_HEAD", appKcoll );
		Context msgContext = super.send( context, tmpMsg, "C3011", "11003000038_46" );
		SFLogger.info( context, "上主机对公账户信息查询[C3011]-结束" );
		return msgContext;
	}
}
