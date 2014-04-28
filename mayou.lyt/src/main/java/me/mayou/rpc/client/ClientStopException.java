package me.mayou.rpc.client;

public class ClientStopException extends RuntimeException {

	private static final long serialVersionUID = -359493289318535329L;

	public ClientStopException(){
		super("客户端已关闭");
	}
	
}
