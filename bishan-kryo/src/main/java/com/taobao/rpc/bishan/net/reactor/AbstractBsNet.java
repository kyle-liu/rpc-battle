package com.taobao.rpc.bishan.net.reactor;


public abstract class AbstractBsNet {

	volatile int state = ST_INIT;
	
	protected static final int ST_INIT = 1;
	protected static final int ST_CONNECTED = 2;
	protected static final int ST_CLOSED = -1;

	protected BsNetListener msgCallBack;
	
	public void close() {
		state=ST_CLOSED;
		innerClose();
	}
	
	protected abstract void innerClose();
}
