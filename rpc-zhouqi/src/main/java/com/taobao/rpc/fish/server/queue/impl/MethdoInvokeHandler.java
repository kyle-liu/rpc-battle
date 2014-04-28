package com.taobao.rpc.fish.server.queue.impl;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import com.lmax.disruptor.EventHandler;
import com.taobao.gecko.core.core.Session;
import com.taobao.gecko.service.Connection;
import com.taobao.rpc.fish.common.command.RpcBooleanCommand;
import com.taobao.rpc.fish.common.command.RpcRequest;
import com.taobao.rpc.fish.common.command.RpcRequestCommand;
import com.taobao.rpc.fish.common.command.RpcResponseCommand;
import com.taobao.rpc.fish.common.command.codec.Deserializer;
import com.taobao.rpc.fish.common.command.codec.Serializer;
import com.taobao.rpc.fish.common.command.codec.impl.KryoDeserializer;
import com.taobao.rpc.fish.common.command.codec.impl.KryoSerializer;
import com.taobao.rpc.fish.server.ServerRegister;
import com.taobao.rpc.fish.server.ServerRegister.MethodDigest;
import com.taobao.rpc.fish.server.ServerRegister.RegInfo;

public class MethdoInvokeHandler implements EventHandler<RequestEvent>{
	public volatile int opaque=0;
	private ServerRegister register;
	public final Serializer ser=new KryoSerializer();
	public final Deserializer des=new KryoDeserializer();
	public MethdoInvokeHandler(ServerRegister register){
		this.register=register;
	}
	@Override
	public void onEvent(RequestEvent event, long sequence, boolean endOfBatch)
			throws Exception {
		RpcRequestCommand command=event.getRequest();
		if(command==null){
			System.out.println("invoke command is null,sq="+sequence);
			return;
		}
		this.opaque=command.getOpaque();
		//System.out.println("invoke command opa="+command.getOpaque());
		//String hexDigest  =command.getHexDigests();
		MethodDigest digest=event.getMethodDigest();
		RegInfo info=register.find(digest);
		 if(info==null){
			 Session session=event.getSession();
			 session.asyncWrite(new RpcBooleanCommand(command.getOpaque(), "异常:接口服务不存在"));
			 event.setNeedResponse(false);
			 event.setSession(null);
			 return;
		 }
		 try {
			 command.deserializer(des);
			Object result=info.methodInvoke(command.getParams());
			//RpcResponseCommand response=new RpcResponseCommand(result, command.getOpaque());
			event.setResponse(command.getOpaque(), result);
			event.setNeedResponse(true);
			/*response.serializer();
			Session session=event.getSession();
			session.asyncWrite(response);
			event.setCommand(null);
			event.setSession(null);*/
		} catch (Exception e) {
			Session session=event.getSession();
			 session.asyncWrite(new RpcBooleanCommand(command.getOpaque(), "异常:调用接口异常"));
			 event.setNeedResponse(false);
			 event.setSession(null);
			 return;
		}
		 
		
	}

}
