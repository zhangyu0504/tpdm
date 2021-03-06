 package common.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import module.cache.ParamCache;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.ecc.emp.component.factory.ComponentFactory;
import com.ecc.emp.component.factory.EMPFlowComponentFactory;
import com.ecc.emp.core.Context;
import com.ecc.emp.data.DataElement;
import com.ecc.emp.data.DuplicatedDataNameException;
import com.ecc.emp.data.IndexedCollection;
import com.ecc.emp.data.InvalidArgumentException;
import com.ecc.emp.data.KeyedCollection;
import com.ecc.emp.data.ObjectNotFoundException;
import com.ecc.emp.format.FormatElement;
import com.ecc.emp.log.EMPLog;
import common.exception.SFException;
import common.shellhandle.ShellReaderThread;

import core.cache.CacheMap;
import core.communication.format.xml.XMLWrapFormat;
import core.log.SFLogger;

/**
 * 基础工具处理类
 * @author 汪华
 *
 */
public class SFUtil {

	/**
	 * 在SF中调用Action，规定Action的参数可以传值和传Context变量名，但必须以'$('开始和')'结束。
	 * 
	 * @param dataName
	 *            需要处理的字符串名字 返回该字符串的值
	 */
	public static String getContextValueInAction( Context context, String dataName ) throws SFException {
		String strValue = null;

		if( dataName == null )
			return null;

		if( dataName.startsWith( "$(" ) && dataName.endsWith( ")" ) )
			try {
				strValue = ( String )context.getDataValue( dataName.substring( 2, dataName.length() - 1 ) );
			} catch( Exception e ) {
				throw new SFException( "获取指定上下文对象失败！" + e.toString() );
			}
		else
			strValue = dataName;

		return strValue;
	}

	/**
	 * 获取上下文根节点
	 * @return
	 */
	public static Context getRootContext() throws SFException {
		EMPFlowComponentFactory factory = ( EMPFlowComponentFactory )EMPFlowComponentFactory.getComponentFactory( SFConst.SYS_SYSNAME );
		Context rootContext = null;
		try {
			rootContext = factory.getContextNamed( factory.getRootContextName() );
		} catch( ObjectNotFoundException e ) {
			throw new SFException( "ST5799", e );
		} catch( Exception e ) {
			throw new SFException( "ST5799", e );
		}
		Context hostContext = ( Context )rootContext.clone();
		return hostContext;

	}

	/**
	 * 生成结构ICOLL
	 * 
	 * @param name
	 *            ICOLL名称
	 * @return IndexedCollection
	 */
	public synchronized static KeyedCollection getTemplateKColl( Context context, String name ) throws Exception {
		KeyedCollection KColl = ( KeyedCollection )CacheMap.getCache( name );
		if( KColl != null ) {
			return ( KeyedCollection )KColl.clone();
		}

		EMPFlowComponentFactory componentFactory = null;
		componentFactory = ( EMPFlowComponentFactory )ComponentFactory.getComponentFactory( ( String )context.getDataValue( SFConst.SERVICE_FACTORY ) );
		KColl = ( KeyedCollection )componentFactory.getDataElement( null, name );
		CacheMap.putCache( name, KColl );
		return ( KeyedCollection )KColl.clone();
	}

	/**
	 * 替换
	 * @param aInput
	 * @param aOldPattern
	 * @param aNewPattern
	 * @return
	 */
	public static String replaceFirst( final String aInput, final String aOldPattern, final String aNewPattern ) {
		if( aOldPattern.equals( "" ) ) {
			throw new IllegalArgumentException( "Old pattern must have content." );
		}
		final StringBuffer result = new StringBuffer();
		// startIdx and idxOld delimit various chunks of aInput; these
		// chunks always end where aOldPattern begins
		int startIdx = 0;
		int idxOld = 0;
		while( ( idxOld = aInput.indexOf( aOldPattern, startIdx ) ) >= 0 ) {
			// grab a part of aInput which does not include aOldPattern
			result.append( aInput.substring( startIdx, idxOld ) );
			// add aNewPattern to take place of aOldPattern
			result.append( aNewPattern );
			// reset the startIdx to just after the current match, to see
			// if there are any further matches
			startIdx = idxOld + aOldPattern.length();

			break;
		}
		// the final chunk will go to the end of aInput
		result.append( aInput.substring( startIdx ) );

		return result.toString();
	}

	/**
	 * 获取上下文值
	 * @param <T>
	 * @param context
	 * @param key
	 * @return
	 * @throws SFException
	 */
	public static <T> T getDataValue( Context context, String key ) throws SFException {

		// 支持多层结构
		String[] keyArray = key.split( "\\." );
		if( keyArray.length > 2 ) {
			String akey = "";
			String bkey = "";
			for( int i = 0; i < keyArray.length; i++ ) {
				if( i < 2 ) {
					akey = akey + keyArray[ i ] + ".";
				} else {
					bkey = bkey + keyArray[ i ] + ".";
				}

			}

			akey = akey.substring( 0, akey.length() - 1 );
			bkey = bkey.substring( 0, bkey.length() - 1 );

			KeyedCollection keyColl = getDataElement( context, akey );
			return ( T ) getDataValue( context, keyColl, bkey );
		}

		if( context.containsKey( key ) ) {
			try {
				return ( T )context.getDataValue( key );
			} catch( ObjectNotFoundException e ) {
				SFLogger.error( context, e );
				throw new SFException( "ST5799" );
			} catch( InvalidArgumentException e ) {
				SFLogger.error( context, e );
				throw new SFException( "ST5799" );
			}
		}
		return null;
	}

	/**
	 * 获取上下文值
	 * @param <T>
	 * @param context
	 * @param key
	 * @return
	 * @throws SFException
	 */
	public static <T> T getReqDataValue( Context context, String key ) throws SFException {
		String txCode = SFUtil.getDataValue( context, SFConst.PUBLIC_TX_CODE );// 获取交易码;
		String sKey = txCode + "_I." + key;
		if( context.containsKey( sKey ) ) {
			try {
				return ( T )context.getDataValue( sKey );
			} catch( ObjectNotFoundException e ) {
				SFLogger.error( context, e );
				throw new SFException( "ST5799" );
			} catch( InvalidArgumentException e ) {
				SFLogger.error( context, e );
				throw new SFException( "ST5799" );
			}
		}
		return null;
	}

	/**
	 * 获取上下文值
	 * @param <T>
	 * @param context
	 * @param key
	 * @return
	 * @throws SFException
	 */
	public static <T> T getDataElement( Context context, String key ) throws SFException {
		if( context.containsKey( key ) ) {
			try {
				return ( T )context.getDataElement( key );
			} catch( ObjectNotFoundException e ) {
				SFLogger.error( context, e );
				throw new SFException( "ST5799" );
			} catch( InvalidArgumentException e ) {
				SFLogger.error( context, e );
				throw new SFException( "ST5799" );
			}
		}
		return null;
	}

	public static <T> T getDataElement( Context context, KeyedCollection kColl, String key ) throws SFException {
		if( kColl.containsKey( key ) ) {

			try {
				return ( T )kColl.getDataElement( key );
			} catch( InvalidArgumentException e ) {
				SFLogger.error( context, e );
				throw new SFException( "ST5799" );
			}
		}
		return null;
	}

	/**
	 * 从kColl中取值
	 * @param <T>
	 * @param context
	 * @param key
	 * @return
	 * @throws SFException
	 */
	public static <T> T getDataValue( Context context, KeyedCollection kColl, String key ) throws SFException {

		// 支持多层结构
		String[] keyArray = key.split( "\\." );
		if( keyArray.length > 2 ) {
			String akey = "";
			String bkey = "";
			for( int i = 0; i < keyArray.length; i++ ) {
				if( i < 2 ) {
					akey = akey + keyArray[ i ] + ".";
				} else {
					bkey = bkey + keyArray[ i ] + ".";
				}

			}

			akey = akey.substring( 0, akey.length() - 1 );
			bkey = bkey.substring( 0, bkey.length() - 1 );

			KeyedCollection keyColl = getDataElement( context, akey );
			return ( T ) getDataValue( context, keyColl, bkey );
		}

		if( kColl.containsKey( key ) ) {
			try {
				return ( T )kColl.getDataValue( key );
			} catch( ObjectNotFoundException e ) {
				SFLogger.error( context, e );
				throw new SFException( "ST5799" );
			} catch( InvalidArgumentException e ) {
				SFLogger.error( context, e );
				throw new SFException( "ST5799" );
			}
		}
		return null;
	}

	/**
	 * 为上下文已经存在的KEY设值
	 * @param <T>
	 * @param context
	 * @param key
	 * @param value
	 * @throws SFException
	 */
	public static <T> void setDataValue( Context context, String key, T value ) throws SFException {

		// 目前最多支持4层结构
		String[] keyArray = key.split( "\\." );
		if( keyArray.length > 2 ) {
			String akey = "";
			String bkey = "";
			for( int i = 0; i < keyArray.length; i++ ) {
				if( i < 2 ) {
					akey = akey + keyArray[ i ] + ".";
				} else {
					bkey = bkey + keyArray[ i ] + ".";
				}

			}
			akey = akey.substring( 0, akey.length() - 1 );
			bkey = bkey.substring( 0, bkey.length() - 1 );

			KeyedCollection keyColl = getDataElement( context, akey );
			setDataValue( context, keyColl, bkey, value );
			return;
		}

		try {
			context.setDataValue( key, value );
		} catch( ObjectNotFoundException e ) {
			SFLogger.error( context, e );
			throw new SFException( "ST5799" );
		} catch( InvalidArgumentException e ) {
			SFLogger.error( context, e );
			throw new SFException( "ST5799" );
		}
		return;
	}

	/**
	 * 为上下文已经存在的KEY设值
	 * @param <T>
	 * @param context
	 * @param key
	 * @param value
	 * @throws SFException
	 */
	public static <T> void setDataValue( Context context, KeyedCollection kColl, String key, T value ) throws SFException {

		// 支持多层结构
		String[] keyArray = key.split( "\\." );
		if( keyArray.length > 2 ) {
			String akey = "";
			String bkey = "";
			for( int i = 0; i < keyArray.length; i++ ) {
				if( i < 2 ) {
					akey = akey + keyArray[ i ] + ".";
				} else {
					bkey = bkey + keyArray[ i ] + ".";
				}

			}

			akey = akey.substring( 0, akey.length() - 1 );
			bkey = bkey.substring( 0, bkey.length() - 1 );

			KeyedCollection keyColl = getDataElement( context, kColl, akey );
			setDataValue( context, keyColl, bkey, value );
			return;
		}

		try {
			kColl.setDataValue( key, value );
		} catch( ObjectNotFoundException e ) {
			SFLogger.error( context, e );
			throw new SFException( "ST5799" );
		} catch( InvalidArgumentException e ) {
			SFLogger.error( context, e );
			throw new SFException( "ST5799" );
		}
		return;
	}

	/**
	 * 为请求参数设值
	 * @param <T>
	 * @param context
	 * @param key
	 * @param value
	 * @throws SFException
	 */
	public static <T> void setReqDataValue( Context context, String key, T value ) throws SFException {
		try {
			String txCode = SFUtil.getDataValue( context, SFConst.PUBLIC_TX_CODE );// 获取交易码;
			context.setDataValue( txCode + "_I." + key, value );
		} catch( ObjectNotFoundException e ) {
			SFLogger.error( context, e );
			throw new SFException( "ST5799" );
		} catch( InvalidArgumentException e ) {
			SFLogger.error( context, e );
			throw new SFException( "ST5799" );
		}
		return;
	}

	/**
	 * 为响应参数设值
	 * @param <T>
	 * @param context
	 * @param key
	 * @param value
	 * @throws SFException
	 */
	public static <T> void setResDataValue( Context context, String key, T value ) throws SFException {
		try {
			String txCode = SFUtil.getDataValue( context, SFConst.PUBLIC_TX_CODE );// 获取交易码;
			context.setDataValue( txCode + "_O." + key, value );
		} catch( ObjectNotFoundException e ) {
			SFLogger.error( context, e );
			throw new SFException( "ST5799" );
		} catch( InvalidArgumentException e ) {
			SFLogger.error( context, e );
			throw new SFException( "ST5799" );
		}
		return;
	}

	/**
	 * 上下文不存在的Key设值
	 * @param <T>
	 * @param context
	 * @param key
	 * @param value
	 * @throws SFException
	 */
	public static <T> void addDataField( Context context, String key, T value ) throws SFException {
		try {
			context.addDataField( key, value );
		} catch( InvalidArgumentException e ) {
			SFLogger.error( context, e );
			throw new SFException( "ST5799" );
		} catch( DuplicatedDataNameException e ) {
			SFLogger.error( context, e );
			throw new SFException( "ST5799" );
		}
		return;
	}

	/**
	 * 上下文不存在的Key设值
	 * @param <T>
	 * @param context
	 * @param key
	 * @param value
	 * @throws SFException
	 */
	public static <T> void addDataField( Context context, KeyedCollection keyColl, String key, T value ) throws SFException {
		try {
			keyColl.addDataField( key, value );
		} catch( InvalidArgumentException e ) {
			SFLogger.error( context, e );
			throw new SFException( "ST5799" );
		} catch( DuplicatedDataNameException e ) {
			SFLogger.error( context, e );
			throw new SFException( "ST5799" );
		}
		return;
	}

	/**
	 * 为父节点添加keyColl
	 * @param <T>
	 * @param context
	 * @param keyColl
	 * @throws SFException
	 */
	public static void addDataElement( Context context, KeyedCollection parentKeyColl, KeyedCollection keyColl ) throws SFException {
		try {
			parentKeyColl.addDataElement( keyColl );
		} catch( InvalidArgumentException e ) {
			SFLogger.error( context, e );
			throw new SFException( "ST5799" );
		} catch( DuplicatedDataNameException e ) {
			SFLogger.error( context, e );
			throw new SFException( "ST5799" );
		}
		return;
	}

	/**
	 * 为上下添加keyColl
	 * @param <T>
	 * @param context
	 * @param keyColl
	 * @throws SFException
	 */
	public static void addDataElement( Context context, KeyedCollection keyColl ) throws SFException {
		try {
			context.addDataElement( keyColl );
		} catch( InvalidArgumentException e ) {
			SFLogger.error( context, e );
			throw new SFException( "ST5799" );
		} catch( DuplicatedDataNameException e ) {
			SFLogger.error( context, e );
			throw new SFException( "ST5799" );
		}
		return;
	}

	/**
	 * 为上下添加indColl
	 * @param <T>
	 * @param context
	 * @param keyColl
	 * @throws SFException
	 */
	public static void addDataElement( Context context, IndexedCollection iColl ) throws SFException {
		try {
			context.addDataElement( iColl );
		} catch( InvalidArgumentException e ) {
			SFLogger.error( context, e );
			throw new SFException( "ST5799" );
		} catch( DuplicatedDataNameException e ) {
			SFLogger.error( context, e );
			throw new SFException( "ST5799" );
		}
		return;
	}

	/**
	 * 为上下添加dataElement
	 * @param <T>
	 * @param context
	 * @param keyColl
	 * @throws SFException
	 */
	public static void addDataElement( Context context, DataElement dataElement ) throws SFException {
		try {
			context.addDataElement( dataElement );
		} catch( InvalidArgumentException e ) {
			SFLogger.error( context, e );
			throw new SFException( "ST5799" );
		} catch( DuplicatedDataNameException e ) {
			SFLogger.error( context, e );
			throw new SFException( "ST5799" );
		}
		return;
	}

	/**
	 * 条件检查
	 * @param context
	 * @param cond
	 * @param sfErrCode
	 * @throws SFException
	 */
	public static void chkCond( Context context, boolean cond, String sfErrCode ) throws SFException {
		if( cond ) {
			setDataValue( context, SFConst.CTX_ERRCODE, sfErrCode );
			String message = null;
			throw new SFException( sfErrCode, message );
		}
	}

	/**
	 * 条件检查
	 * @param context
	 * @param sfErrCode	存管错误码
	 * @param sfErrMsg	错误日志
	 * @throws SFException
	 */
	public static void chkCond( Context context, String sfErrCode, String sfErrMsg ) throws SFException {
		// SFLogger.error( context, sfErrMsg );
		chkCond( context, true, sfErrCode, sfErrMsg );
	}

	/**
	 * 条件检查
	 * @param context
	 * @param cond	判定条件
	 * @param sfErrCode 存管错误码
	 * @param sfErrMsg	错误日志
	 * @throws SFException
	 */
	public static void chkCond( Context context, boolean cond, String sfErrCode, String sfErrMsg ) throws SFException {
		if( cond ) {
			SFLogger.error( context, sfErrMsg );
			setDataValue( context, SFConst.CTX_ERRCODE, sfErrCode );
			throw new SFException( sfErrCode, sfErrMsg );
		}

	}

	/**
	 * 字符串转换为double
	 * @param str
	 * @return
	 * @throws SFException
	 */
	public static double object2Double( Object obj ) throws SFException {

		if( null != obj && !"".equals( obj.toString().trim() ) ) {
			return Double.valueOf( obj.toString().replaceAll( ",", "" ) );
		}
		return 0d;
	}

	/**
	 * 判断是否为空
	 * @param obj
	 * @return  为空返回true
	 */
	public static boolean isEmpty( Object obj ) {
		return obj == null || obj.toString() == null || obj.toString().trim().length() == 0;
	}

	/**
	 * 判断是否为空
	 * @param obj
	 * @return  不为空返回true
	 */
	public static boolean isNotEmpty( Object obj ) {
		return obj != null && obj.toString() != null && obj.toString().trim().length() > 0;
	}

	/**
	 * 将Object转为String
	 * @param value
	 * @return
	 */
	public static String objectToString( Object value ) {

		if( null == value || null == value.toString() || "".equals( value.toString().trim() ) ) {
			return "";
		} else {
			return value.toString();
		}
	}

	/** 
	 * 提供精确的加法运算。 
	 * @param v1 被加数 
	 * @param v2 加数 
	 * @return 两个参数的和 
	 */
	public static double add( Object v1, Object v2 ) {
		BigDecimal b1 = new BigDecimal( SFUtil.objectToString( v1 ) );
		BigDecimal b2 = new BigDecimal( SFUtil.objectToString( v2 ) );
		return b1.add( b2 ).doubleValue();
	}

	/** 
	 * 提供精确的减法运算。 
	 * @param v1 减数 
	 * @param v2 被减数 
	 * @return 两个参数的差 
	 */
	public static double sub( Object v1, Object v2 ) {
		BigDecimal b1 = new BigDecimal( SFUtil.objectToString( v1 ) );
		BigDecimal b2 = new BigDecimal( SFUtil.objectToString( v2 ) );
		return b1.subtract( b2 ).doubleValue();
	}

	/** 
	 * 提供精确的乘法运算。 
	 * @param v1 被乘数 
	 * @param v2 乘数 
	 * @return 两个参数的积 
	 */
	public static double mul( Object v1, Object v2 ) throws SFException {
		BigDecimal b1 = new BigDecimal( SFUtil.objectToString( v1 ) );
		BigDecimal b2 = new BigDecimal( SFUtil.objectToString( v2 ) );
		return b1.multiply( b2 ).doubleValue();
	}

	/** 
	 * 提供（相对）精确的除法运算，当发生除不尽的情况时，精确到 
	 * 小数点以后2位，以后的数字四舍五入。 
	 * @param v1 除数 
	 * @param v2 被除数 
	 * @return 两个参数的商 
	 */
	public static String div( Object v1, Object v2 ) throws SFException {
		return div( v1, v2, 2 );
	}

	/**
	 * 直联券商金额除100处理除不尽时保理2位小数
	 * @param 金额
	 * @return
	 * @throws SFException
	 */
	public static String div( Object v1 ) throws SFException {
		return div( v1, 100, 2 );
	}

	/** 
	 * 提供（相对）精确的除法运算。当发生除不尽的情况时，由scale参数指 
	 * 定精度，以后的数字四舍五入。 
	 * @param v1 除数 
	 * @param v2 被除数 
	 * @param scale 表示表示需要精确到小数点以后几位。 
	 * @return 两个参数的商 
	 */
	public static String div( Object v1, Object v2, int scale ) {
		if( scale < 0 ) {
			throw new IllegalArgumentException( "scale必须大于零!" );
		}
		BigDecimal b1 = new BigDecimal( SFUtil.objectToString( v1 ) );
		BigDecimal b2 = new BigDecimal( SFUtil.objectToString( v2 ) );
		return SFUtil.objectToString( b1.divide( b2, scale, BigDecimal.ROUND_HALF_UP ) );
	}

	/**
	 * Double类型转换成字符串,同时要先去除科学计数法
	 * @param val
	 * @param percision
	 * @return
	 */
	public static String double2String( Object val, int percision ) {
		StringBuffer bf = new StringBuffer( "0" );
		if( percision > 0 ) {
			bf.append( "." );
			while( percision-- > 0 ) {
				bf.append( "0" );
			}
		}
		DecimalFormat df = new DecimalFormat( bf.toString() );
		return df.format( val );
	}

	/**
	 * 判断字符串是否为数字
	 * 
	 * @param text
	 * @return
	 */
	public static boolean isNum( String text ) {
		return text.matches( "[0-9]+" );
	}

	/**
	 * 提供按照指定长度截取字符串长度
	 * @param text	需要截取的信息
	 * @param len	需要截取的长度
	 * @return
	 */
	public static String getSubString( String text, int len ) {
		if( SFUtil.isNotEmpty( text ) && text.length() > len ) {
			return text.substring( 0, len );
		}
		return text;
	}

	/**
	 * 克隆通信临时上下文
	 * @param context
	 * @param msg
	 * @return
	 * @throws SFException
	 */
	public static Context cloneMsgContext( Context context, Map<String, Object> msg ) throws SFException {
		/*
		 * 克隆新的通信上下文
		 */
		Context msgContext = SFUtil.getRootContext();
		/*
		 * 复制公共信息至临时上下文中
		 */
		try {
			if( msgContext.containsKey( SFConst.PUBLIC ) ) {
				msgContext.removeDataElement( SFConst.PUBLIC );
			}
			SFUtil.addDataElement( msgContext, context.getDataElement( SFConst.PUBLIC ) );
		} catch( ObjectNotFoundException e ) {
			SFLogger.error( context, e );
			throw new SFException( "ST5799" );
		} catch( InvalidArgumentException e ) {
			SFLogger.error( context, e );
			throw new SFException( "ST5799" );
		}
		/*
		 * COPY map对象中的kColl与iColl至临时上下文中
		 */
		if( msg != null ) {
			for( Map.Entry<String, Object> entry : msg.entrySet() ) {
				// String key=entry.getKey();
				Object value = entry.getValue();
				if( value instanceof KeyedCollection ) {
					SFUtil.addDataElement( msgContext, ( KeyedCollection )value );
				} else if( value instanceof IndexedCollection ) {
					SFUtil.addDataElement( msgContext, ( IndexedCollection )value );
				}
			}
		}
		return msgContext;
	}

	/**
	 * 克隆深证通通信临时上下文
	 * @param context
	 * @param msg
	 * @return
	 * @throws SFException
	 */
	public static Context cloneMsgContextForSZT( Context context, String msgCode ) throws SFException {
		Context msgContext = SFUtil.cloneMsgContext( context, null );
		addFormatToContext( msgContext, msgCode );
		return msgContext;
	}

	public static void addFormatToContext( Context context, String msgCode ) throws SFException {
		FormatElement msgFormat = context.getFormat( msgCode );
		// 增加输出字段到上下文
		XMLWrapFormat tranCodeO = ( XMLWrapFormat )msgFormat.getFormatElement();
		try {
			tranCodeO.addFormatToContext( context );
		} catch( Exception e ) {
			SFLogger.error( context, e );
			throw new SFException( "ST5799", e.getMessage(), e );
		}
	}

	/**
	 * HashMap对象转换成Json对象
	 * @param hmSource
	 * @param page
	 * @param total
	 * @param records
	 * @return
	 */
	public static JSONObject hashMap2Json( HashMap hmSource, int page, int total, int records ) {
		JSONObject jsonObj = new JSONObject(); // 根据jqGrid对JSON的数据格式要求给jsonObj赋值
		try {
			JSONArray rows = new JSONArray(); // 定义rows，存放数据
			String valueKey, value;
			if( hmSource != null && hmSource.size() > 0 ) {

				Iterator iter = hmSource.keySet().iterator();
				Iterator iterValue = null;
				HashMap hmValue = null;

				for( int i = 1; i <= hmSource.size(); i++ ) {
					JSONObject cell = new JSONObject(); // 存放一条记录的对象
					if( ( hmValue = ( HashMap )hmSource.get( i + "" ) ) != null ) {
						iterValue = hmValue.keySet().iterator();
						while( iterValue.hasNext() ) {
							valueKey = ( String )iterValue.next();
							if( ( value = ( String )hmValue.get( valueKey ) ) != null ) {
								cell.put( valueKey, value );
							}
						}
						rows.add( cell );// 将该记录放入rows中
					}
				}

				jsonObj.put( "rows", rows ); // 将rows放入json对象中
				if( page != -1 && page > 0 ) {
					jsonObj.put( "page", new Integer( page ) );
				}
				if( total != -1 && total > 0 ) {
					jsonObj.put( "total", new Integer( total ) );
				}
				if( records != -1 && records > 0 ) {
					jsonObj.put( "records", new Integer( records ) );
				}
			}
		} catch( Exception e ) {
			EMPLog.log( SFConst.DEFAULT_TRXCODE, EMPLog.ERROR, 0, e.getMessage(), e );
		}
		return jsonObj;
	}

	/**
	 * 获取系统环境变量配置
	 * @param key
	 * @return
	 */
	public static String getSysProperty( String key ) {
		// return System.getenv(key);
		return PropertyUtil.getProperty( key );
	}

	/**
	 * 公共方法： 字符串补位
	 * 
	 * @param seqId
	 * @return txSeqId
	 * 
	 */
	public static String fixChar( String str, int len, char c, String aligment ) {
		// if( SFUtil.isNotEmpty( str ) ) {//允许空字符串时补位
		if( str != null ) {
			str = str.trim();
			while( str.length() < len ) {// 长度不足，左补c
				if( "right".equals( aligment ) ) {
					str = str + c;
				} else {
					str = c + str;
				}
			}
		}
		return str;
	}

	/**
	 * 将Object转为String
	 * @param value
	 * @return
	 */
	public static String ObjectToString( Object value ) {
		if( null == value || null == value.toString() || "".trim().equals( value ) ) {
			return "";
		} else {
			return value.toString();
		}
	}

	/**
	 * 获取脚本文件路径
	 * @return
	 * @throws SFException 
	 */
	public static String getShellPath( Context context ) throws SFException {
		String shellPath = null;
		try {
			shellPath = ParamCache.getValue2( "SF_SHELL", "SHELL_PATH" );
			shellPath = processPath( context, shellPath );
		} catch( Exception e ) {
			chkCond( context, true, "ST5799", "获取脚本文件路径失败，请检查参数配置" );
		}
		return shellPath;
	}

	/**
	 * 路径处理
	 * @param context
	 * @param path
	 * @return
	 * @throws Exception
	 */
	public static String processPath( Context context, String path ) throws Exception {
		try {
			if( isEmpty( path ) || isEmpty( path.trim() ) )
				path = "";
			else {
				if( !( path.endsWith( "\\" ) || path.endsWith( "/" ) ) )
					path = path + "/";
			}
		} catch( Exception e ) {
			throw e;
		}
		return path;
	}

	/**
	 * 通用调shell文件方法
	 * @param context
	 * @param con
	 * @param shellFile
	 * @param param
	 * @throws Exception
	 */
	public static void executeShellFile( Context context, String shellFile, Object... param ) throws Exception {

		String shellPath = null;// 脚本文件路径
		Process process = null;
		int exitValue = -1;// 脚本执行完成返回值

		try {
			SFUtil.chkCond( context, SFUtil.isEmpty( shellFile ), "脚本名称不能为空" );
			shellPath = SFUtil.getShellPath( context );
			shellFile = shellPath + shellFile;// 脚本文件全路径（包含文件名）
			SFLogger.debug( context, String.format( "执行脚本[%s],参数%s", shellFile, Arrays.toString( param ) ) );

			List<String> list = new ArrayList<String>();
			list.add( "sh" );
			list.add( shellFile );
			if( param != null ) {
				for( Object para : param )
					list.add( SFUtil.ObjectToString( para ) );
			}

			String[] cmd = list.toArray( new String[ list.size() ] );
			process = Runtime.getRuntime().exec( cmd );

			ShellReaderThread readerInPutThread = new ShellReaderThread( context, process.getInputStream(), "INPUT" );
			readerInPutThread.start();

			ShellReaderThread readerErrorThread = new ShellReaderThread( context, process.getErrorStream(), "ERROR" );
			readerErrorThread.start();

			exitValue = process.waitFor();

			SFUtil.chkCond( context, exitValue != 0, "ST5799", String.format( "脚本[%s]执行失败,exitValue = [%s]", shellFile, exitValue ) );

			SFLogger.info( context, String.format( "脚本[%s]执行成功", shellFile ) );

		} catch( Exception e ) {
			throw e;
		} finally {
			try {
				if( process != null )
					process.destroy();
			} catch( Exception e1 ) {
				throw e1;
			}
		}

	}
}
