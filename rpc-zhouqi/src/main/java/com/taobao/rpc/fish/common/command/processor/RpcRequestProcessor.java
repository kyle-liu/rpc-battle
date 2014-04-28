package com.taobao.rpc.fish.common.command.processor;

import java.util.concurrent.ThreadPoolExecutor;

import com.taobao.gecko.service.Connection;
import com.taobao.gecko.service.RequestProcessor;
import com.taobao.rpc.fish.common.command.RpcRequestCommand;
import com.taobao.rpc.fish.server.queue.RpcRequestQueue;

public class RpcRequestProcessor implements RequestProcessor<RpcRequestCommand>{

	private RpcRequestQueue queue;
	public RpcRequestProcessor(RpcRequestQueue queue){
		this.queue=queue;
	}
	@Override
	public ThreadPoolExecutor getExecutor() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void handleRequest(RpcRequestCommand request, Connection conn) {
		//queue.addRpcRequest(request,conn.);
	}

}
