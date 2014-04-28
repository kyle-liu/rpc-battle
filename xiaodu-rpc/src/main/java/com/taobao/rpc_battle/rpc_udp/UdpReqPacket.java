
/**
 * xiaodu-rpc
 */
package com.taobao.rpc_battle.rpc_udp;

import java.io.Serializable;
import java.util.UUID;


/**
 * @author xiaodu
 *
 * 下午5:09:12
 */
public class UdpReqPacket implements Serializable{
	

	/**
	 * 
	 */
	private static final long serialVersionUID = 2475583529725370632L;

	private long uuid;
	
	private Object[] params;
	
	private String rpcMethodName;

	public UdpReqPacket(){
	}

	public Object[] getParams() {
		return params;
	}

	public void setParams(Object[] params) {
		this.params = params;
	}


	public String getRpcMethodName() {
		return rpcMethodName;
	}

	public void setRpcMethodName(String rpcMethodName) {
		this.rpcMethodName = rpcMethodName;
	}

	public long getUuid() {
		return uuid;
	}

	public void setUuid(long uuid) {
		this.uuid = uuid;
	}


	
	
	

}
