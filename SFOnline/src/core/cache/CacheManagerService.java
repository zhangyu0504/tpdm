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
 * �������
 * @author ����
 *
 */
public class CacheManagerService extends EMPService implements ReversalController {
	private SFLogger logger=SFLogger.getLogger("000000");
	private Map<String,CacheHandler> reversalHandlers = new HashMap<String,CacheHandler>();
	
	/**
	 * ҵ���߼����������������ƣ���RecersalHandler��ͨ���������������ȡҵ���߼��������̶���
	 */
	private String factoryName = null;
	
	/**
	 * ��ʼ�����ػ���:ͬ������,���⵱���߳�����ʱ,�������߳�Ҳͬʱ��
	 * @param context
	 * @throws SFException
	 */
	public void init(Context context) throws SFException{
		SFLogger.info("000000",null,"��������ʼ��-��ʼ");
		//ˢ����,���¶ദ����
		Connection connection =null;
		boolean connFlag=false;//������ݿ������Ƿ�Ϊ��������
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
		logger.info("��������ʼ��-����");
	}
	
	/**
	 * ˢ�»���:ͬ������,���⵱���߳�����ʱ,�������߳�Ҳͬʱ��
	 * @param context
	 * @throws SFException
	 */
	public void refresh(Context context) throws SFException{
		//ˢ����,���¶ദ����
		Connection connection =null;
		boolean connFlag=false;//������ݿ������Ƿ�Ϊ��������
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
							//handler.refresh(context,connection);//ˢ�»���
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
