package com.taobao.shuihan.rpc;

import java.util.concurrent.Executors;

import com.google.protobuf.BlockingRpcChannel;
import com.google.protobuf.RpcController;
import com.google.protobuf.ServiceException;
import com.googlecode.protobuf.socketrpc.RpcChannels;
import com.googlecode.protobuf.socketrpc.RpcConnectionFactory;
import com.googlecode.protobuf.socketrpc.RpcServer;
import com.googlecode.protobuf.socketrpc.ServerRpcConnectionFactory;
import com.googlecode.protobuf.socketrpc.SocketRpcConnectionFactories;
import com.googlecode.protobuf.socketrpc.SocketRpcController;
import com.taobao.rpc.api.RpcFactory;
import com.taobao.shuihan.rpc.protos.PersonProtos.HelloService;
import com.taobao.shuihan.rpc.protos.PersonProtos.HelloService.BlockingInterface;
import com.taobao.shuihan.rpc.protos.PersonProtos.Person;
import com.taobao.shuihan.rpc.protos.PersonProtos.callStr;
import com.taobao.shuihan.rpc.protos.util.MessageCorverter;

public class ProtobufRpcFactory implements RpcFactory {

	private RpcServer server = null;
	private HelloServiceInnerImpl helloService = null;

	/*
	 * public void startClient(String ip) { // Create a thread pool
	 * ExecutorService threadPool = Executors.newFixedThreadPool(100);
	 * 
	 * // Create channel RpcConnectionFactory connectionFactory =
	 * SocketRpcConnectionFactories.createRpcConnectionFactory(ip,
	 * RpcFactory.DEFAULT_PORT); RpcChannel channel =
	 * RpcChannels.newRpcChannel(connectionFactory, threadPool);
	 * 
	 * // Call service HelloService myService = HelloService.newStub(channel);
	 * RpcController controller = new SocketRpcController();
	 * myService.helloPerson(controller,
	 * PersonProtos.Person.newBuilder().build(), new RpcCallback<Person>() {
	 * public void run(Person person) { System.out.println("Received Response: "
	 * + person); } });
	 * 
	 * // Check success if (controller.failed()) {
	 * System.err.println(String.format("Rpc failed %s : %s",
	 * ((SocketRpcController) controller).errorReason(),
	 * controller.errorText())); } }
	 */

	public class HelloServiceInnerImpl implements
			com.taobao.rpc.benchmark.service.HelloService {

		private BlockingInterface service;

		private RpcController controller;

		public HelloServiceInnerImpl(BlockingInterface service,
				RpcController controller) {
			this.service = service;
			this.controller = controller;
		}

		@Override
		public com.taobao.rpc.benchmark.dataobject.Person helloPerson(
				com.taobao.rpc.benchmark.dataobject.Person in) {
			Person myResponse = null;
			try {
				myResponse = service.helloPerson(controller,
						MessageCorverter.toProtoPerson(in));
			} catch (ServiceException e) {
				e.printStackTrace();
			}
			return MessageCorverter.toInterfacePerson(myResponse);
		}

		@Override
		public String helloWorld(String in) {
			callStr str = null;
			try {
				str = service.helloWorld(controller, callStr.newBuilder()
						.setIn(in).build());
			} catch (ServiceException e) {
				e.printStackTrace();
			}
			return str.getIn();
		}

	}


	@Override
	public <T> void export(Class<T> type, T serviceObject) {
		if (server == null) {
			ServerRpcConnectionFactory rpcConnectionFactory = SocketRpcConnectionFactories
					.createServerRpcConnectionFactory(RpcFactory.DEFAULT_PORT);
			server = new RpcServer(rpcConnectionFactory,
					Executors.newFixedThreadPool(200), true);
		}
		server.registerService(new HelloServiceImpl());
		server.run();
	}

	@Override
	public <T> T getReference(Class<T> type, String ip) {
		if (helloService == null) {
			RpcConnectionFactory connectionFactory = SocketRpcConnectionFactories
					.createRpcConnectionFactory(ip, RpcFactory.DEFAULT_PORT);
			BlockingRpcChannel channel = RpcChannels
					.newBlockingRpcChannel(connectionFactory);
			// Call service
			BlockingInterface service = HelloService.newBlockingStub(channel);
			RpcController controller = new SocketRpcController();
			this.helloService = new HelloServiceInnerImpl(service, controller);
		}
		return (T) helloService;
	}

	@Override
	public int getClientThreads() {
		return 30;
	}

	@Override
	public String getAuthorId() {
		return "shuihan-protorpc";
	}

}
