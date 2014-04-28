package com.taobao.rpc.fish.server.queue;

import com.taobao.gecko.core.buffer.IoBuffer;
import com.taobao.gecko.core.core.Session;
import com.taobao.gecko.service.Connection;
import com.taobao.rpc.fish.common.command.RpcRequestCommand;

public interface RpcRequestQueue {

	public boolean addRpcRequest(RpcRequestCommand command,Session session);
public void addRpcRequest(int opaque,int length,IoBuffer buffer,Session session);
	public void dispose();
}
