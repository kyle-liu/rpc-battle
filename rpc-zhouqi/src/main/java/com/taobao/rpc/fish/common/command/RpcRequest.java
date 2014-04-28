package com.taobao.rpc.fish.common.command;

import java.io.Serializable;

/**
 * rpc request
 * @author zhouqi.zhm
 *
 */
public class RpcRequest implements Serializable{
	private Object params[];
	
	public Object[] getParams() {
		return params;
	}
	public void setParams(Object[] params) {
		this.params = params;
	}
	
}
