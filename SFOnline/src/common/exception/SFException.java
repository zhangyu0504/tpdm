package common.exception;

import com.ecc.emp.core.EMPException;

/**
 * SF(���)ϵͳ�쳣�ࡣ
 * 
 * @author SF
 * @version 1.0
 * @since 1.0 2010-01-15
 * @lastmodified
 */
public class SFException extends EMPException {
	

	private static final long serialVersionUID = 2L;

	/**
	 * ������������
	 */
	public SFException() {
		super();
	}

	/**
	 * �����쳣��Ϣ�Ĺ�����������
	 *  ����������쳣��,ֻ���쳣��ϢĬ��Ϊ��ǰϵͳ�쳣��
	 * @param message
	 *            �쳣��Ϣ
	 */
	public SFException(String errorCode) {
		super(errorCode,errorCode);
	}

	/**
	 * ���д�������쳣��Ϣ�Ĺ�����������
	 * 
	 * @param errorCode
	 * @param message
	 */
	public SFException(String errorCode, String message) {
		super(errorCode, message);
	}

	/**
	 * �����쳣��Ϣ�Ϳ��׳��쳣����Ĺ�����������
	 * 
	 * @param message
	 * @param cause
	 */
	public SFException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * ���д����롢�쳣��Ϣ�Ϳ��׳��쳣����Ĺ�����������
	 * 
	 * @param erroCode
	 * @param message
	 * @param cause
	 */
	public SFException(String errorCode, String message, Throwable cause) {
		super(errorCode, message, cause);
	}

	/**
	 * ���ݿ��׳��쳣���󴴽���EMP�쳣������������
	 * 
	 * @param cause
	 */
	public SFException(Throwable cause) {
		super(cause);
	}

	/**
	 * ���ݿ��׳��쳣���󴴽���EMP�쳣������������
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
