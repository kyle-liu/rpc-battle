import shenfeng.simplerpc.Registry;

import com.taobao.rpc.api.RpcFactory;

public class SimpleRpcFactory implements RpcFactory {

	private static final Registry registry = new Registry();

	@Override
	public <T> void export(Class<T> type, T serviceObject) {
		registry.register(serviceObject, DEFAULT_PORT);
	}

	@Override
	public String getAuthorId() {
		return "shenfeng";
	}

	@Override
	public int getClientThreads() {
		return 150;
	}

	@Override
	public <T> T getReference(Class<T> type, String ip) {
		return registry.lookup(type, ip, DEFAULT_PORT);
	}

}
