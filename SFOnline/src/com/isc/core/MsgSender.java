package com.isc.core;

import java.util.Hashtable;

import com.ecc.emp.core.Context;

/**
 * ��Ϣ���ͽӿ�
 * <p>Description:		<p>
 * <p>Module:			<p>
 *
 * @author EX_KJKFB_LVCHAOHONG
 * @date 2018-3-14 ����02:49:07
 * @since 1.0
 */
public abstract interface MsgSender {

	public abstract void SendMsg( Context context, Hashtable paramHashtable, byte[] paramArrayOfByte ) throws Exception;
}
