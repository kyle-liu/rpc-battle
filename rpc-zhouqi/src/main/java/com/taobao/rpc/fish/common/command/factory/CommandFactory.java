package com.taobao.rpc.fish.common.command.factory;

import com.taobao.rpc.fish.common.command.RpcRequestCommand;

public class CommandFactory {

	public RpcRequestCommand createRequest(String interfaceName,String method,Object params[]){
		RpcRequestCommand command=new RpcRequestCommand();
		command.setOpaque(1);
		return command;
	}
}
