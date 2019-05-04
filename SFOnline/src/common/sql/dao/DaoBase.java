package common.sql.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import module.trans.Page;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ColumnListHandler;
import org.apache.commons.dbutils.handlers.MapHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;

import com.ecc.emp.core.Context;
import common.exception.SFException;
import common.sql.bean.SQLStruct;
import common.util.SFUtil;

import core.log.SFLogger;
import edu.emory.mathcs.backport.java.util.Arrays;

public class DaoBase {
	/**
	 * ����
	 * 
	 * @param context
	 * @param connection
	 * @param sqlStruct
	 * @throws SFException
	 * @throws Exception
	 */
	public int save(Context context, Connection connection,
			SQLStruct sqlStruct) throws SFException {
		int result = 0;
		String sql = sqlStruct.getSql();
		Object[] param = sqlStruct.getValues();
		QueryRunner runner = new QueryRunner();
		try {
			SFLogger.debug(context, String.format("SQL[%s]", sql));
			SFLogger.debug(context,String.format("SQL����%s", Arrays.toString(param)));
			result = runner.update(connection, sql, param);
		} catch (Exception e) {
			SFLogger.error( context, e.getMessage() );
			SFUtil.chkCond( context, "ST4895", "���ݿ��������!" );
		}
		return result;
	}

	/**
	 * ����
	 * 
	 * @param context
	 * @param connection
	 * @param sql
	 * @param obj
	 * @return
	 * @throws SFException
	 */
	public int save(Context context, Connection connection, String sql,
			Object... param) throws SFException {
		int result = 0;
		QueryRunner runner = new QueryRunner();
		try {
			SFLogger.debug(context, String.format("SQL[%s]", sql));
			SFLogger.debug(context,String.format("SQL����%s", Arrays.toString(param)));
			result = runner.update(connection, sql, param);
		} catch (Exception e) {
			SFLogger.error( context, e.getMessage() );
			SFUtil.chkCond( context, "ST4895", "���ݿ��������!" );
		}
		return result;
	}

	/**
	 * �������ʵ�ָ��²���
	 * 
	 * @param <T>
	 * @param context
	 *            ������
	 * @param connection
	 *            ���ݿ�����
	 * @param sql
	 * @param param
	 * @return
	 * @throws SFException
	 */
	public int update(Context context, Connection connection, String sql,
			Object[] param) throws SFException {
		QueryRunner runner = new QueryRunner();
		int result = 0;
		try {
			SFLogger.debug(context, String.format("SQL[%s]", sql));
			SFLogger.debug(context,String.format("SQL����%s", Arrays.toString(param)));
			result = runner.update(connection, sql, param);
		} catch (Exception e) {
			SFLogger.error( context, e.getMessage() );
			SFUtil.chkCond( context, "ST4895", "���ݿ��������!" );
		}
		return result;
	}

	/**
	 * �������-����������ѯʵ��bean
	 * 
	 * @param context:�����ģ���ӡ��־
	 * @param connection������
	 * @param sqlStruct��bean�й���Ĳ�ѯ�ṹ��
	 * @return
	 * @throws SFException
	 */
	@SuppressWarnings("unchecked")
	public <T> T qry(Context context, Connection connection,
			SQLStruct sqlStruct, Class<T> clazz) throws SFException {
		QueryRunner runner = new QueryRunner();
		T t = null;
		try {
			String sql = sqlStruct.getSql();
			Object[] param = sqlStruct.getValues();
			
			SFLogger.debug(context, String.format("SQL[%s]", sql));
			SFLogger.debug(context,String.format("SQL����%s", Arrays.toString(param)));
			t = (T) runner.query(connection, sql, param, new BeanHandler(clazz));
		} catch (Exception e) {
			SFLogger.error( context, e.getMessage() );
			SFUtil.chkCond( context, "ST4895", "���ݿ��������!" );
		}
		return t;
	}

	/**
	 * ����SQL�Ͳ�����ѯBean
	 * 
	 * @param <T>
	 * @param context
	 * @param connection
	 * @param sql
	 * @param clazz
	 * @param param
	 * @return
	 * @throws SFException
	 */
	@SuppressWarnings("unchecked")
	public <T> T qry(Context context, Connection connection, String sql,
			Class<T> clazz, Object... param) throws SFException {
		QueryRunner runner = new QueryRunner();
		T obj = null;
		try {
			SFLogger.debug(context, String.format("SQL[%s]", sql));
			SFLogger.debug(context,String.format("SQL����%s", Arrays.toString(param)));
			obj = (T) runner.query(connection, sql, param, new BeanHandler(clazz));
		} catch (Exception e) {
			SFLogger.error( context, e.getMessage() );
			SFUtil.chkCond( context, "ST4895", "���ݿ��������!" );
		}
		return obj;
	}

	/**
	 * ����SQL�Ͳ�����ѯMap<String,Object>
	 * 
	 * @param <T>
	 * @param context
	 * @param connection
	 * @param sql
	 * @param clazz
	 * @param param
	 * @return
	 * @throws SFException
	 */
	@SuppressWarnings("unchecked")
	public Map<String,Object> qryMap(Context context, Connection connection, String sql,Object... param) throws SFException {
		QueryRunner runner = new QueryRunner();
		Map<String,Object> result=null;
		try {
			SFLogger.debug(context, String.format("SQL[%s]", sql));
			SFLogger.debug(context,String.format("SQL����%s", Arrays.toString(param)));
			result = (Map<String, Object>) runner.query(connection, sql, param, new MapHandler());
		} catch (Exception e) {
			SFLogger.error( context, e.getMessage() );
			SFUtil.chkCond( context, "ST4895", "���ݿ��������!" );
		}
		return result;
	}

	/**
	 * ����SQL�Ͳ�����ѯList<Map<String,Object>>
	 * 
	 * @param <T>
	 * @param context
	 * @param connection
	 * @param sql
	 * @param clazz
	 * @param param
	 * @return
	 * @throws SFException
	 */
	@SuppressWarnings("unchecked")
	public List<Map<String,Object>> qryListMap(Context context, Connection connection, String sql,Object... param) throws SFException {
		QueryRunner runner = new QueryRunner();
		List<Map<String,Object>> result=null;
		try {
			SFLogger.debug(context, String.format("SQL[%s]", sql));
			SFLogger.debug(context,String.format("SQL����%s", Arrays.toString(param)));
			result = (List<Map<String,Object>>) runner.query(connection, sql, param, new MapListHandler());
		} catch (Exception e) {
			SFLogger.error( context, e.getMessage() );
			SFUtil.chkCond( context, "ST4895", "���ݿ��������!" );
		}
		return result;
	}
	
	

	@SuppressWarnings("unchecked")
	public List<Object> qryList(Context context, Connection connection, String sql,String key,Object... param) throws SFException {
		QueryRunner runner = new QueryRunner();
		List<Object> result=null;
		try {
			SFLogger.debug(context, String.format("SQL[%s]", sql));
			SFLogger.debug(context,String.format("SQL����%s", Arrays.toString(param)));
			result = (List<Object>) runner.query(connection, sql, param, new ColumnListHandler(key));
		} catch (Exception e) {
			SFLogger.error( context, e.getMessage() );
			SFUtil.chkCond( context, "ST4895", "���ݿ��������!" );
		}
		return result;
	}

	/**
	 * ��ѯ����
	 * 
	 * @param <T>
	 * @param context
	 * @param conn
	 * @param sql
	 * @param param
	 * @param clazz
	 * @return
	 * @throws SFException
	 */
	@SuppressWarnings("unchecked")
	public <T> List<T> qryForOList(Context context, Connection conn,
			String sql, Object[] param, Class<T> clazz) throws SFException {
		QueryRunner runner = new QueryRunner();
		List<T> obj = null;
		try {
			SFLogger.debug(context, String.format("SQL[%s]", sql));
			SFLogger.debug(context,String.format("SQL����%s", Arrays.toString(param)));
			obj = (List<T>) runner.query(conn, sql, param, new BeanListHandler(clazz));
		} catch (SQLException e) {
			SFLogger.error( context, e.getMessage() );
			SFUtil.chkCond( context, "ST4895", "���ݿ��������!" );
		}
		return obj;
	}


	/**
	 * ��ѯ����
	 * 
	 * @param context
	 * @param connection
	 * @param sql
	 * @param obj
	 * @return
	 * @throws SFException
	 */
	public static long qryCount(Context context, Connection connection,
			String sql, Object... param) throws SFException {
		QueryRunner runner = new QueryRunner();
		BigDecimal num = null;
		try {
			SFLogger.debug(context, String.format("SQL[%s]", sql));
			SFLogger.debug(context,String.format("SQL����%s", Arrays.toString(param)));
			num = (BigDecimal) runner.query(connection, sql, param,new ScalarHandler(1));
		} catch (Exception e) {
			SFLogger.error( context, e.getMessage() );
			SFUtil.chkCond( context, "ST4895", "���ݿ��������!" );
		}
		return num != null ? num.longValue() : 0;
	}
	
	/**
	 * ��ҳ��ѯ
	 * @param <T>
	 * @param context
	 * @param conn
	 * @param sql
	 * @param param
	 * @param clazz
	 * @param page
	 * @return
	 * @throws SFException
	 */
	public <T> Page<T> qryPageing(Context context, Connection conn,
			String sql, Class<T> clazz ,Page page, Object... param)throws SFException {
		QueryRunner runner = new QueryRunner();
		List<T> pageData = null;
		try {
			
			StringBuffer sb=new StringBuffer();
			sb.append("SELECT * FROM (SELECT C.*, ROWNUM RN FROM(");
			sb.append(sql);
			sb.append(")C WHERE ROWNUM<=");
			sb.append(page.getEndNum());
			sb.append(") WHERE RN>");
			sb.append(page.getStartNum());
			
			SFLogger.debug(context, String.format("SQL[%s]", sb.toString()));
			SFLogger.debug(context,String.format("SQL����%s", Arrays.toString(param)));
			/*
			 * ��ҳ��ѯ
			 */
			pageData = (List<T>) runner.query(conn, sb.toString(), param, new BeanListHandler(clazz));
			
			/*
			 * ͳ���ܼ�¼��
			 */
			sb=new StringBuffer();
			sb.append("SELECT COUNT(1) FROM (");
			sb.append(sql);
			sb.append(") C");
			
			SFLogger.debug(context, String.format("SQL[%s]", sb.toString()));
			SFLogger.debug(context,String.format("SQL����%s", Arrays.toString(param)));
			long totalNum=qryCount(context,conn,sb.toString(),param);
			
			//��װ���ؽ��
			page.setPageData(pageData);
			page.setTotalNum(totalNum);
		} catch (SQLException e) {
			SFLogger.error( context, e.getMessage() );
			SFUtil.chkCond( context, "ST4895", "���ݿ��������!" );
		}
		return page;
	}
}