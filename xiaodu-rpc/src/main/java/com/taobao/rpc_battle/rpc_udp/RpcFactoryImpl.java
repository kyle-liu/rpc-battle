
/**
 * xiaodu-rpc
 */
package com.taobao.rpc_battle.rpc_udp;

import com.taobao.rpc.api.RpcFactory;
import com.taobao.rpc_battle.rpc_udp.proxy.Reflect;

/**
 * @author xiaodu
 *
 * 下午2:07:02
 */
public class RpcFactoryImpl implements RpcFactory{
	
	@Override
	public <T> void export(Class<T> arg0, T arg1) {
		
		UdpReceive udp = new UdpReceive();
		udp.setInterface(arg0);
		udp.setService(arg1);
		udp.startup();
		
	}

	@Override
	public String getAuthorId() {
		return "xiaodu-udp";
	}

	@Override
	public int getClientThreads() {
		return 40;
	}

	@Override
	public <T> T getReference(Class<T> arg0, String arg1) {
		return (T)Reflect.getProxy(arg0).newProxyInstance(arg1);
	}

}
