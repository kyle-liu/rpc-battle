package com.taobao.rpc.fish.server;

import java.util.concurrent.atomic.AtomicBoolean;

import com.taobao.gecko.service.RemotingFactory;
import com.taobao.gecko.service.RemotingServer;
import com.taobao.gecko.service.config.ServerConfig;
import com.taobao.gecko.service.exception.NotifyRemotingException;
import com.taobao.gecko.service.impl.DefaultRemotingServer;
import com.taobao.rpc.fish.common.wireformat.SimpleRpcWireFormatType;
import com.taobao.rpc.fish.server.queue.impl.DisruptorQueueFactory;
import com.taobao.rpc.fish.server.queue.impl.DisruptorRpcRequestQueue;
import com.taobao.rpc.fish.test.TestInterface;
import com.taobao.rpc.fish.test.TestInterfaceImpl;

public class RpcServer {

	public RemotingServer server;
	public RpcServerConfig rpcServerConfig;
	private ServerRegister register;
	private DisruptorRpcRequestQueue queue;
	private AtomicBoolean start=new AtomicBoolean(false);
	public RpcServer(){
		rpcServerConfig=new RpcServerConfig();
		rpcServerConfig.port=8079;
		this.register=new ServerRegister();
		DisruptorQueueFactory.register=this.register;
		final ServerConfig serverConfig = new ServerConfig();
        serverConfig.setWireFormatType(new SimpleRpcWireFormatType());
        serverConfig.setPort(rpcServerConfig.port);
        serverConfig.setSelectorPoolSize(4);
        serverConfig.setReadThreadCount(0);
        serverConfig.setRcvBufferSize(1024*1024*3);
        serverConfig.setSndBufferSize(1024*1024*3);
        serverConfig.setMaxReadBufferSize(1024*1024*8024);
        serverConfig.setMaxReadBufferSize(1024*1024*8024);
		server=RemotingFactory.newRemotingServer(serverConfig);
	}
	public void register(Class cls,Object target){
		try {
			this.register.registe(cls.getName(), target);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			throw new RuntimeException(e);
		}
	}
	public void start(){
		if(!start.compareAndSet(false, true))return;		
		try {			
			server.start();
		} catch (NotifyRemotingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	public void stop(){
		if(!start.compareAndSet(true, false))return;
		queue.dispose();
		try {
			server.stop();
		} catch (NotifyRemotingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	public static void main(String args[]) throws InterruptedException{
		Object object=new Object();
		RpcServer server=new RpcServer();
		server.start();
		server.register(TestInterface.class, new TestInterfaceImpl());
		//server.start();
		synchronized(object){
			object.wait();
		}
	}
}
