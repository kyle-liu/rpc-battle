package me.mayou.rpc.factory;

import me.mayou.rpc.proxy.ProxyFactory;
import me.mayou.rpc.server.ServerFactory;

import com.taobao.rpc.api.RpcFactory;

public class MayouRpcFactory implements RpcFactory {
	
	@Override
	public <T> void export(Class<T> type, T serviceObject) {
		ServerFactory.getServer().register(type.getName(), serviceObject);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getReference(Class<T> type, String ip) {
		return (T)ProxyFactory.getConsumerProxy(type, ip);
	}

	@Override
	public int getClientThreads() {
		return 80;
	}

	@Override
	public String getAuthorId() {
		return "mayou.lyt";
	}

}
