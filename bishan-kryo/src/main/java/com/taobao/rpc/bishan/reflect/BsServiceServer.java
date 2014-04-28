package com.taobao.rpc.bishan.reflect;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import com.esotericsoftware.reflectasm.MethodAccess;
import com.taobao.rpc.benchmark.service.HelloServiceImpl;
import com.taobao.rpc.bishan.net.common.BsFutureDone;
import com.taobao.rpc.bishan.net.msg.RequstPackage;
import com.taobao.rpc.bishan.net.msg.ResponsePackage;
import com.taobao.rpc.bishan.net.reactor.BsFactory;
import com.taobao.rpc.bishan.net.reactor.BsNetClient;
import com.taobao.rpc.bishan.net.reactor.BsNetListener;
import com.taobao.rpc.bishan.net.reactor.BsNetServer;

public class BsServiceServer {

	public static final BsFactory netFactory=new BsFactory();
	MethodAccess	access = MethodAccess.get(HelloServiceImpl.class);
	
	private BsNetListener bsCallBack=new BsNetListener() {

		@Override
		public void onMsg(BsNetClient client, Object responseResult) {
			RequstPackage requestObj=(RequstPackage)responseResult;
			ResponsePackage response=new ResponsePackage();
			response.setId(requestObj.getId());
			response.setSuccess(false);
			String serviceName=requestObj.getClassName();
			
			Object service=services.get(serviceName);
			if(service==null){
				response.setE(new Exception("servie not exist serviceName"));
			}else{
				Object objReturn=access.invoke(service,
						requestObj.getMethodName(), requestObj.getParameters());
				response.setResponseObj(objReturn);
				response.setSuccess(true);
				try {
					client.write(response);
				} catch (ClosedChannelException e) {
					// TODO 通道关闭
					e.printStackTrace();
				} catch (IOException e) {
					// TODO 发送数据异常，一般是序列化异常
					e.printStackTrace();
				}
//
//				Method m;
//				try {
//					m = service.getClass().getMethod(requestObj.getMethodName(),
//							requestObj.getParameterClasses());
//					m.setAccessible(false);
//					Object objReturn=m.invoke(service, requestObj.getParameters());
//					response.setResponseObj(objReturn);
//					response.setSuccess(true);
//					
//				} catch (Exception e) {
//					response.setE(new Exception("servie deal exception",e));
//				}
//				try {
//					client.write(response);
//				} catch (ClosedChannelException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
			}
		}
		@Override
		public void onException(Exception cause) {
			// TODO Auto-generated method stub
			cause.printStackTrace();
			System.out.println("server excepetion");
		}
	};
	
	private final Map<String, Object> services=new ConcurrentHashMap<String,Object>();
	private BsNetServer netServer;

	public void init(int port) throws Exception{
		netFactory.initServer();
		BsFutureDone<BsNetServer> serverFuture=netFactory.serverListen(port,bsCallBack);
		
		serverFuture.waitDone(2, TimeUnit.SECONDS);
		if(serverFuture.isSuccess()){
			netServer=serverFuture.getAttachObj();
		}else{
			throw new Exception("server startup exception");
		}
	}
	
	public void registerService(Object service) {
		services.put(service.getClass().getInterfaces()[0].getName(),service);
	}
}
