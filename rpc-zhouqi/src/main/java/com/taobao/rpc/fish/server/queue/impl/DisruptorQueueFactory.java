package com.taobao.rpc.fish.server.queue.impl;

import com.taobao.rpc.fish.server.ServerRegister;
import com.taobao.rpc.fish.server.queue.RpcRequestQueue;

public class DisruptorQueueFactory {
	public static  ServerRegister register;
	public static RpcRequestQueue createDisruptorQueue(){
		if(register==null)return null;
		DisruptorRpcRequestQueue queue=new DisruptorRpcRequestQueue(register);
		return queue;
	}
}
