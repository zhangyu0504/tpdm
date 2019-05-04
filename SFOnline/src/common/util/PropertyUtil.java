package common.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

/**
 * 配置文件读取工具类：以单例初始化后到内存，每次都是在内在中获取参数配置
 * 
 * @author
 * @date
 * @version V 1.0
 */

public class PropertyUtil {
	private static PropertyUtil instance = null;
	private Properties pro = new Properties();

	private PropertyUtil() {
		try {
			pro.load(getClass().getClassLoader().getResourceAsStream("system.properties"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static PropertyUtil getInstance() {
		if(instance==null){
			instance=new PropertyUtil();
		}
		return instance;
	}

	/**
	 * 获取配置文件属性
	 * 
	 * @param key
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	private String getValue(String key){
		return pro.getProperty(key);
	}

	/**
	 * 获取配置文件属性
	 * 
	 * @param key
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static String getProperty(String key){
		PropertyUtil propertyUtil=PropertyUtil.getInstance();
		return propertyUtil.getValue(key);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws UnsupportedEncodingException {
		System.out.println(PropertyUtil.getProperty("APP_CODE"));
		System.out.println(PropertyUtil.getProperty("APP_CODE"));
	}

}
