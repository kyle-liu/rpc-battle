
/**
 * xiaodu-rpc
 */
package com.taobao.rpc_battle.rpc_udp;

import java.io.Serializable;

/**
 * @author xiaodu
 *
 * 下午5:31:52
 */
public class UdpRespPacket implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 723895360598103646L;
	
	private long uuid;

	private Object returnValue;
	
	public Object getReturnValue() {
		return returnValue;
	}

	public void setReturnValue(Object returnValue) {
		this.returnValue = returnValue;
	}

	public long getUuid() {
		return uuid;
	}

	public void setUuid(long uuid) {
		this.uuid = uuid;
	}

	
	

}
