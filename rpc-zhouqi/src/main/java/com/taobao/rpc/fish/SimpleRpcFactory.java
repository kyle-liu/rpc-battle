package com.taobao.rpc.fish;

import com.taobao.rpc.api.RpcFactory;
import com.taobao.rpc.fish.client.RpcClient;
import com.taobao.rpc.fish.server.RpcServer;


public class SimpleRpcFactory implements RpcFactory{
	int port=8079;
	@Override
	public <T> void export(Class<T> type, T serviceObject) {
		Object object=new Object();
		RpcServer server=new RpcServer();
		server.start();
		server.register(type, serviceObject);
		//server.start();
		synchronized(object){
			try {
				object.wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}

	@Override
	public <T> T getReference(Class<T> type, String ip) {	
		RpcClient client=new RpcClient(4);
		return client.getReference(type, ip);
	}

	@Override
	public int getClientThreads() {
		// TODO Auto-generated method stub
		return 100;
	}

	@Override
	public String getAuthorId() {
		// TODO Auto-generated method stub
		return "zhouqi-rpc";
	}

}
