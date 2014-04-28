package com.taobao.rpc.fish.server.queue.impl;

import com.lmax.disruptor.EventHandler;
import com.taobao.gecko.core.core.Session;
import com.taobao.gecko.service.Connection;
import com.taobao.rpc.fish.common.command.RpcBooleanCommand;
import com.taobao.rpc.fish.common.command.RpcRequestCommand;
import com.taobao.rpc.fish.common.command.codec.Deserializer;
import com.taobao.rpc.fish.common.command.codec.impl.KryoDeserializer;

public class RequestDeserializerHandler implements EventHandler<RequestEvent>{
	public int opaque=0;
	public final Deserializer des=new KryoDeserializer();
	@Override
	public void onEvent(RequestEvent event, long sequence, boolean endOfBatch)
			throws Exception {
		
		 RpcRequestCommand command=(RpcRequestCommand)event.getRequest();
		try {			
			command.deserializer(des);
			this.opaque=command.getOpaque();
			//System.out.println("request opa="+command.getOpaque());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//System.out.println("request sequence="+sequence);
			//Thread.sleep(100);
			Session session=event.getSession();
			 session.asyncWrite(new RpcBooleanCommand(command.getOpaque(), "序列化出错"));
			 //event.;
			// event.setSession(null);
		}
		
	}

}
