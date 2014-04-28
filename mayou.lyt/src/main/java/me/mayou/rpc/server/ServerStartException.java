package me.mayou.rpc.server;

public class ServerStartException extends RuntimeException {

	private static final long serialVersionUID = -3014364388836442856L;

	public ServerStartException() {
		super("服务器已开启");
	}

}
