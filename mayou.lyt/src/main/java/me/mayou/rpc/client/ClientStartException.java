package me.mayou.rpc.client;

public class ClientStartException extends RuntimeException {
	
	private static final long serialVersionUID = -5650125518083253235L;

	public ClientStartException(){
		super("客户端已经启动");
	}
}
