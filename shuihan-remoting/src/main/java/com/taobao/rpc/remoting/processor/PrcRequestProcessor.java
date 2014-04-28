package com.taobao.rpc.remoting.processor;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

import com.taobao.gecko.core.command.ResponseStatus;
import com.taobao.gecko.service.Connection;
import com.taobao.gecko.service.RequestProcessor;
import com.taobao.gecko.service.exception.NotifyRemotingException;
import com.taobao.rpc.benchmark.dataobject.Person;
import com.taobao.rpc.remoting.command.RpcAckCommand;
import com.taobao.rpc.remoting.command.RpcInvokeCommand;
import com.taobao.rpc.remoting.test.SerializeUtil;

public class PrcRequestProcessor implements
		RequestProcessor<RpcInvokeCommand> {

	private ThreadPoolExecutor threadPoolExecutor;

	private Map<String, Object> services = new HashMap<String, Object>();

	public void handleRequest(RpcInvokeCommand request, Connection conn) {
		process(request, conn);

	}

	private void test(RpcInvokeCommand request, Connection conn) {
		System.out.println("收到数据:  header="
				+ new String(request.getHeader() + ",body="
						+ new String(request.getBody())));
		
		 try {
			 RpcAckCommand command = new RpcAckCommand(request,
						ResponseStatus.NO_ERROR, request.getBody());
			 conn.response(command);
			 } catch (NotifyRemotingException e) {
		 }
		 
	}

	private void process(RpcInvokeCommand request, Connection conn) {
		String clazz = new String(request.getHeader());
		String[] invokeInfo = clazz.split(":");
		Object obj = null;
		try {
			obj = SerializeUtil.decodeObject(request.getBody());
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		RpcAckCommand command = null;
		if (obj == null) {
			System.out.println("反序列化失败");
		} else {
			Object object = services.get(invokeInfo[0]);
			try {
				 invokeMethod(invokeInfo[1],
						getParameterClass(invokeInfo[2]), object,
						object.getClass(), obj);
				command = new RpcAckCommand(request,
						ResponseStatus.NO_ERROR, request.getBody());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		try {
			conn.response(command);
		} catch (NotifyRemotingException e) {
			e.printStackTrace();
		}
	}

	private static Class<?> getParameterClass(String parameter) {
		if (parameter.equals("Person")) {
			return Person.class;
		} else {
			return String.class;
		}
	}

	public void registerService(Class<?> clazz, Object obj) {
		this.services.put(clazz.getSimpleName(), obj);
	}

	public ThreadPoolExecutor getExecutor() {
		return this.threadPoolExecutor;
	}

	public void setExecutor(ThreadPoolExecutor threadPoolExecutor) {
		this.threadPoolExecutor = threadPoolExecutor;
	}

	public static Object invokeMethod(String md, Class<?> parameterTypes,
			Object obj, Class<?> clazz, Object... args) throws Exception {
		Method method = clazz.getMethod(md, parameterTypes);
		return method.invoke(obj, args);
	}

}
