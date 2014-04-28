package com.taobao.rpc.fish.server.queue.impl;

import com.lmax.disruptor.EventFactory;
import com.taobao.gecko.core.buffer.IoBuffer;
import com.taobao.gecko.core.core.Session;
import com.taobao.gecko.service.Connection;
import com.taobao.rpc.fish.common.command.RpcRequestCommand;
import com.taobao.rpc.fish.common.command.RpcResponseCommand;
import com.taobao.rpc.fish.server.ServerRegister.MethodDigest;

public class RequestEvent {

	private volatile RpcRequestCommand request=new RpcRequestCommand();
	private volatile RpcResponseCommand response=new RpcResponseCommand(); 
	private volatile Session session;
	private volatile boolean needResponse=true;
	private volatile MethodDigest methodDigest=new MethodDigest();{
		methodDigest.digest=new byte[16];
	}
	private  RequestEvent(){}
	
	
	public Session getSession() {
		return session;
	}
	
	public RpcRequestCommand getRequest() {
		return request;
	}


	public void setRequest(RpcRequestCommand request) {
		this.request = request;
	}

	
	public boolean isNeedResponse() {
		return needResponse;
	}


	public void setNeedResponse(boolean needResponse) {
		this.needResponse = needResponse;
	}


	public RpcResponseCommand getResponse() {
		return response;
	}


	public MethodDigest getMethodDigest() {
		return methodDigest;
	}


	public void setMethodDigest(MethodDigest methodDigest) {
		this.methodDigest = methodDigest;
	}


	public void setResponse(RpcResponseCommand response) {
		this.response = response;
	}
	public void setResponse(int opaque,Object result){
		response.setOpaque(opaque);
		response.setResult(result);
	}

	public void setSession(Session session) {
		this.session = session;
	}
	public final static EventFactory<RequestEvent> EVENT_FACTORY = new EventFactory<RequestEvent>()
    {
        public RequestEvent newInstance()
        {
            return new RequestEvent();
        }
    };
}
