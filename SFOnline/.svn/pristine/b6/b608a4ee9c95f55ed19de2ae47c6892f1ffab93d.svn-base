package common.sql.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * 对象属性描述
 * 用于描述当前BEAN对象是否用于
 * 数据库操作如果用于数据库操作,必须申明
 * 对应的数据库表名,默认为当前类对象名称
 * @author hey
 *
 */
@Target(ElementType.TYPE)
public @interface TableName {
	/**
     * 数据表名称注解，默认值为类名称
     * @return
     */
    public String name() default "className";
}
