package com.taobao.rpc.fish.client.network;

import com.taobao.rpc.fish.common.command.RpcRequestCommand;

public interface ClientService {

	public void start();
	public void stop();
	public void connect(String ip,int port);
	public Object send(String ip,int port,RpcRequestCommand request);
}
