package core.cache;

import java.util.HashMap;
import java.util.Map;

import common.exception.SFException;
/**
 * »º´æ»ùÀà
 * @author Íô»ª
 *
 */
public class CacheMap {
	private static Map<String,Object> cacheMap=new HashMap<String,Object>();
	
	public static <T> T getCache(String key)throws SFException{
		return (T) cacheMap.get(key);
	}
	
	public static void putCache(String key,Object value)throws SFException{
		cacheMap.put(key, value);
	}	
}
