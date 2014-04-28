package com.taobao.rpc.fish.client;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import com.taobao.gecko.core.command.ResponseCommand;
import com.taobao.gecko.core.command.kernel.BooleanAckCommand;
import com.taobao.gecko.core.core.Session;
import com.taobao.gecko.service.RemotingClient;
import com.taobao.rpc.fish.common.command.RpcRequestCommand;
import com.taobao.rpc.fish.common.command.RpcResponseCommand;
import com.taobao.rpc.fish.common.command.codec.Deserializer;
import com.taobao.rpc.fish.common.command.codec.impl.CodecFacory;
import com.taobao.rpc.fish.common.command.codec.impl.KryoDeserializer;
import com.taobao.rpc.fish.common.util.MD5;
import com.taobao.rpc.fish.server.queue.impl.DisruptorRpcRequestQueue;

public class RpcInvokeHandler implements InvocationHandler,MethodInterceptor{

	final Class cl;
	private final Map<String,MethodInfo> methodMap=new HashMap<String, MethodInfo>();

	final RemotingClient client;
	final AtomicInteger opaGenerator;
	final String group;
	ThreadLocal<Deserializer> desLocal=new ThreadLocal<Deserializer>(){
		protected Deserializer initialValue() {
			return new KryoDeserializer();
		}
	};
	//final KryoDeserializer deserializer=new KryoDeserializer();
	public RpcInvokeHandler(Class face,RemotingClient client,AtomicInteger opaGenerator,String group){
		cl=face;
		this.client=client;
		this.opaGenerator=opaGenerator;
		this.group=group;
		init();
	}
	public void init(){
		Method methods[]=cl.getMethods();
		for(int i=0;i<methods.length;i++){
			Method method=methods[i];
			StringBuilder builder=new StringBuilder();
			builder.append(cl.getName());
			builder.append(method.getName());
			Class types[]=method.getParameterTypes();
			for(int j=0;j<types.length;j++){
				builder.append(types[j].getName());
			}
			byte digests[]=MD5.getDigest(builder.toString());
			Class cl=method.getReturnType();
			MethodInfo info=new MethodInfo();
			info.digests=digests;
			info.returnClass=cl;
			methodMap.put(method.getName(), info);
		}
	}
	@Override
	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		MethodInfo info=methodMap.get(method.getName());
		byte digests[]=info.digests;
		//String hexDigest=MD5.toHex(digests);
		RpcRequestCommand command=new RpcRequestCommand(this.opaGenerator.incrementAndGet());
		command.setTargetDigests(digests);
		command.setParams(args);
		ResponseCommand response=client.invokeToGroup(group, command,1000,TimeUnit.SECONDS);
		if(response==null)return null;
		if(response instanceof BooleanAckCommand){
			//throw new RuntimeException("���ó���:"+((BooleanAckCommand)response).getErrorMsg());
			return null;
		}
		if(response instanceof RpcResponseCommand){
			RpcResponseCommand rpcResponse=(RpcResponseCommand)response;
			//System.out.println("receive one reponse");
			try {
				//System.out.println("rpcResponse length="+rpcResponse.getData().length);
				
				Object o=CodecFacory.getDeserializer().decodeObject(rpcResponse.getData());
				return o;
			} catch (Exception e) {
				return desLocal.get().decodeObject(rpcResponse.getData());
				//return null;
			}
		}else{
			throw new RuntimeException("���ó���,��Ӧ�����޷�ʶ��:"+response.getClass());
		}

	}
	public class MethodInfo{
		public byte[] digests;
		public Class returnClass;
	}
	@Override
	public Object intercept(Object obj, Method method, Object[] args,
			MethodProxy proxy) throws Throwable {		
		return this.invoke(obj, method, args);
	}
	
}
