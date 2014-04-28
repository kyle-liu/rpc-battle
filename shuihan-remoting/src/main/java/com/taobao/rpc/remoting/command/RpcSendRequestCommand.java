package com.taobao.rpc.remoting.command;

public class RpcSendRequestCommand extends RpcRequestCommand {

	/**
     * 
     */
	private static final long serialVersionUID = 7447353514251797182L;
	private String headerInfo;
	private byte[] data;

	public RpcSendRequestCommand(OpCode opCode) {
		super(opCode);
	}

	public RpcSendRequestCommand(String headerInfo, byte[] data) {
		this.headerInfo = headerInfo;
		this.data = data;
	}

	public void decodeContent() {

	}

	public void encodeContent() {
		this.setHeader(headerInfo.getBytes());
		this.setBody(data);
	}
}
