package module.trans.sf2bankchl;

import java.util.HashMap;
import java.util.Map;

import com.ecc.emp.core.Context;
import com.ecc.emp.data.KeyedCollection;

import common.exception.SFException;
import common.util.SFUtil;
import core.log.SFLogger;

import module.communication.ESBClientBase;

/**
 * 更新系统会计日期：M8004 当前工作日查询
 * <p>Description:		<p>
 * <p>Module:			<p>
 *
 * @author EX_KJKFB_LVCHAOHONG
 * @date 2017-11-2 上午11:07:42
 * @since 1.0
 */
public class UpdTxDateClient extends ESBClientBase {

	@Override
	protected Context doHandle( Context context, Map<String, Object> msg ) throws SFException {

		SFLogger.info( context, "上主机当前工作日查询[M8004]-开始" );
		Map<String, Object> tmpMsg = new HashMap<String, Object>();
		Context msgContext = null;
		try {
			// 组SYS_HEAD
			KeyedCollection sysHeadKColl = SFUtil.getTemplateKColl( context, "SYS_HEAD" );
			SFUtil.setDataValue( context, sysHeadKColl, "CONSUMER_ID", msg.get( "CONSUMER_ID" ) );// 系统ID
			SFUtil.setDataValue( context, sysHeadKColl, "TRAN_DATE", msg.get( "TRAN_DATE" ) );// 交易日期
			tmpMsg.put( "SYS_HEAD", sysHeadKColl );

			// 组MSG_I
			KeyedCollection msgIColl = new KeyedCollection( "MSG_I" );
			SFUtil.addDataField( context, msgIColl, "VALIDATION_FLAG", msg.get( "VALIDATION_FLAG" ) );// 查询标志
			tmpMsg.put( "MSG_I", msgIColl );

			// 发送报文
			msgContext = super.send( context, tmpMsg, "M8004", "11003000018_06" );
		} catch( Exception e ) {
			SFLogger.info( context, String.format( "上主机当前工作日查询失败[%s]", e.getMessage() ) );
			throw new SFException( "上主机当前工作日查询失败" );
		}

		SFLogger.info( context, "上主机当前工作日查询[M8004]-结束" );
		return msgContext;
	}

}
