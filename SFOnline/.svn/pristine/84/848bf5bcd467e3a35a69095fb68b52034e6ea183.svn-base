package common.exception;

import com.ecc.emp.core.EMPException;

/**
 * SF(存管)系统异常类。
 * 
 * @author SF
 * @version 1.0
 * @since 1.0 2010-01-15
 * @lastmodified
 */
public class SFException extends EMPException {
	

	private static final long serialVersionUID = 2L;

	/**
	 * 构造器方法。
	 */
	public SFException() {
		super();
	}

	/**
	 * 带有异常信息的构造器方法。
	 *  如果不传入异常码,只有异常信息默认为当前系统异常码
	 * @param message
	 *            异常信息
	 */
	public SFException(String errorCode) {
		super(errorCode,errorCode);
	}

	/**
	 * 带有错误码和异常信息的构造器方法。
	 * 
	 * @param errorCode
	 * @param message
	 */
	public SFException(String errorCode, String message) {
		super(errorCode, message);
	}

	/**
	 * 带有异常信息和可抛出异常对象的构造器方法。
	 * 
	 * @param message
	 * @param cause
	 */
	public SFException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * 带有错误码、异常信息和可抛出异常对象的构造器方法。
	 * 
	 * @param erroCode
	 * @param message
	 * @param cause
	 */
	public SFException(String errorCode, String message, Throwable cause) {
		super(errorCode, message, cause);
	}

	/**
	 * 根据可抛出异常对象创建的EMP异常构造器方法。
	 * 
	 * @param cause
	 */
	public SFException(Throwable cause) {
		super(cause);
	}

	/**
	 * 根据可抛出异常对象创建的EMP异常构造器方法。
	 * 
	 * @param cause
	 */
	public SFException(SFException e) {
		super(e.getErrorCode(), e.getMessage(), e);
	}
	@Override
	public String toString() {
		return getMessage();
	}
}
