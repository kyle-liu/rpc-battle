package com.taobao.rpc.bishan;

import com.taobao.rpc.api.RpcFactory;
import com.taobao.rpc.bishan.reflect.BsServiceServer;
import com.taobao.rpc.bishan.reflect.RpcDynamicProxy;

/**
 * An simple implementation of {@link RpcFactory}.
 * <p/>
 * Just wrap implementation to {@code MsgPack}.
 * <p/>
 * Date: 2013/1/4
 * 
 * @author shutong.dy
 */
public class BsRpcFactory implements RpcFactory {

	@Override
	public <T> void export(Class<T> type, T serviceObject) {
		try {
			BsServiceServer servicServer = new BsServiceServer();
			servicServer.init(1985);
			servicServer.registerService(serviceObject);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public <T> T getReference(Class<T> type, String ip) {
		RpcDynamicProxy cli;
		try {
			cli = new RpcDynamicProxy(type, ip);
			return type.cast(cli.getProxyObject());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public int getClientThreads() {
		return Runtime.getRuntime().availableProcessors() * 8;
	}

	@Override
	public String getAuthorId() {
		return "bishan-kryo";
	}
}
