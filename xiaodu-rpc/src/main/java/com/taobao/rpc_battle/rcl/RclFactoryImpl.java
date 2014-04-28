
/**
 * xiaodu-rpc
 */
package com.taobao.rpc_battle.rcl;

import com.taobao.rpc.api.RpcFactory;
import com.taobao.rpc_battle.rcl.client.RclMainClient;
import com.taobao.rpc_battle.rcl.server.RclMainServer;

/**
 * @author xiaodu
 *
 * ÏÂÎç12:48:05
 */
public class RclFactoryImpl  implements RpcFactory{

	@Override
	public <T> void export(Class<T> arg0, T arg1) {

		RclMainServer server = new RclMainServer(arg0,arg1);
		server.startup();
		
	}

	@Override
	public String getAuthorId() {
		return "xiaodu-rcl";
	}

	@Override
	public int getClientThreads() {
		return 30;
	}

	@Override
	public <T> T getReference(Class<T> arg0, String arg1) {
		RclMainClient rcl = new RclMainClient(arg1);
		try {
			return rcl.getRpcImpl(arg0);
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}


}
