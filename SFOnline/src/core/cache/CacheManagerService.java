package core.cache;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ecc.emp.core.Context;
import com.ecc.emp.flow.reversal.ReversalController;
import com.ecc.emp.flow.reversal.ReversalHandler;
import com.ecc.emp.service.EMPService;
import common.exception.SFException;
import common.sql.dao.DBHandler;
import common.util.SFConst;
import common.util.SFUtil;

import core.log.SFLogger;
/**
 * 缓存管理
 * @author 汪华
 *
 */
public class CacheManagerService extends EMPService implements ReversalController {
	private SFLogger logger=SFLogger.getLogger("000000");
	private Map<String,CacheHandler> reversalHandlers = new HashMap<String,CacheHandler>();
	
	/**
	 * 业务逻辑定义的组件工厂名称，在RecersalHandler中通过此组件工程来获取业务逻辑处理流程定义
	 */
	private String factoryName = null;
	
	/**
	 * 初始化加载缓存:同步方法,避免当主线程启动时,其他的线程也同时在
	 * @param context
	 * @throws SFException
	 */
	public void init(Context context) throws SFException{
		SFLogger.info("000000",null,"缓存管理初始化-开始");
		//刷缓存,导致多处加载
		Connection connection =null;
		boolean connFlag=false;//标记数据库联接是否为交易联接
		try{
			
			if(context.containsKey(SFConst.PUBLIC_TRAN_CONNECTION)){
				connection=SFUtil.getDataValue(context,SFConst.PUBLIC_TRAN_CONNECTION);
				if(connection==null){
					connection = DBHandler.getConnection(context);
					connFlag=true;
				}
			}else{
				connection = DBHandler.getConnection(context);
				connFlag=true;
			}
			synchronized(this){
					if(reversalHandlers!=null&&reversalHandlers.size()>0){
						for(Map.Entry<String,CacheHandler> entry:reversalHandlers.entrySet()){
							CacheHandler handler=entry.getValue();
							handler.init(context,connection);
						}
					}
			}
		}catch(SFException e){
			DBHandler.rollBackTransaction(context, connection);
			throw e;
		}catch(Exception e){
			DBHandler.rollBackTransaction(context, connection);
			throw new SFException(e);
		}finally{
			DBHandler.commitTransaction(context, connection);
			if(connFlag){
				DBHandler.releaseConnection(context, connection);
			}
		}
		logger.info("缓存管理初始化-结束");
	}
	
	/**
	 * 刷新缓存:同步方法,避免当主线程启动时,其他的线程也同时在
	 * @param context
	 * @throws SFException
	 */
	public void refresh(Context context) throws SFException{
		//刷缓存,导致多处加载
		Connection connection =null;
		boolean connFlag=false;//标记数据库联接是否为交易联接
		try{
			if(context.containsKey(SFConst.PUBLIC_TRAN_CONNECTION)){
				connection=SFUtil.getDataValue(context,SFConst.PUBLIC_TRAN_CONNECTION);
				if(connection==null){
					connection = DBHandler.getConnection(context);
					connFlag=true;
				}
			}else{
				connection = DBHandler.getConnection(context);
				connFlag=true;
			}
			synchronized(this){
					if(reversalHandlers!=null&&reversalHandlers.size()>0){
						for(Map.Entry<String,CacheHandler> entry:reversalHandlers.entrySet()){
							CacheHandler handler=entry.getValue();
							handler.init(context,connection);
							//handler.refresh(context,connection);//刷新缓存
						}
					}
			}
		}catch(SFException e){
			DBHandler.rollBackTransaction(context, connection);
			throw e;
		}catch(Exception e){
			DBHandler.rollBackTransaction(context, connection);
			throw new SFException(e);
		}finally{
			DBHandler.commitTransaction(context, connection);
			if(connFlag){
				DBHandler.releaseConnection(context, connection);
			}
		}
	}

	public void addReversalHandler(ReversalHandler handler) {
		CacheHandler tHandler=(CacheHandler)handler;
		reversalHandlers.put(tHandler.getId(),tHandler);
	}

	public void doGlobalReversal(Context context, List accessList) {
		
	}

	public void doReversal(Context arg0, List arg1) {
		
	}
}
