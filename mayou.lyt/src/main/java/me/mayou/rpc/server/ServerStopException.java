package me.mayou.rpc.server;

public class ServerStopException extends RuntimeException {

	private static final long serialVersionUID = -5936051461406395747L;

	public ServerStopException() {
		super("服务器已关闭");
	}
}
