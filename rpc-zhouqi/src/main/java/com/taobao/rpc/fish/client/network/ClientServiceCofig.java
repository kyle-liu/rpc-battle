package com.taobao.rpc.fish.client.network;

public class ClientServiceCofig {

	private int recvBuffSize;
	private int sendBuffSize;
	private int connSize;
	public int getRecvBuffSize() {
		return recvBuffSize;
	}
	public void setRecvBuffSize(int recvBuffSize) {
		this.recvBuffSize = recvBuffSize;
	}
	public int getSendBuffSize() {
		return sendBuffSize;
	}
	public void setSendBuffSize(int sendBuffSize) {
		this.sendBuffSize = sendBuffSize;
	}
	public int getConnSize() {
		return connSize;
	}
	public void setConnSize(int connSize) {
		this.connSize = connSize;
	}
	
}
