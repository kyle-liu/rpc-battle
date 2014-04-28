package com.taobao.rpc.remoting.factory;

import com.taobao.gecko.core.command.kernel.BooleanAckCommand;
import com.taobao.rpc.api.RpcFactory;
import com.taobao.rpc.benchmark.dataobject.Person;
import com.taobao.rpc.benchmark.service.HelloService;
import com.taobao.rpc.remoting.adapter.RpcClient;
import com.taobao.rpc.remoting.adapter.RpcServer;
import com.taobao.rpc.remoting.command.RpcResponseCommand;
import com.taobao.rpc.remoting.command.RpcInvokeCommand;
import com.taobao.rpc.remoting.test.SerializeUtil;

public class RemotingRpcFactory implements RpcFactory {

	RpcServer server = null;
	RpcClient client = null;
	private InnerHelloWorldImpl instance;

	@Override
	public <T> void export(Class<T> type, T serviceObject) {
		if (server == null) {
			server = new RpcServer();
			server.start(DEFAULT_PORT);
		}
		server.export(type, serviceObject);
	}

	@Override
	public <T> T getReference(Class<T> type, String ip) {
		if (client == null) {
			client = new RpcClient();
			client.start();
			client.connect(ip, DEFAULT_PORT, 6);
		}
		if (instance == null) {
			instance = new InnerHelloWorldImpl(this.client);
		}
		return (T) instance;

	}

	class InnerHelloWorldImpl implements HelloService {

		RpcClient client;

		public InnerHelloWorldImpl(RpcClient client) {
			this.client = client;
		}

		@Override
		public Person helloPerson(Person in) {
			String header = "HelloService:helloPerson:Person";
			try {
				byte[] data = SerializeUtil.encodeObject(in);
				RpcInvokeCommand command = new RpcInvokeCommand(header,data);
				RpcResponseCommand responseCommand = client.send(command);
				if(responseCommand == null){
					return in;
				}
				else if (responseCommand instanceof BooleanAckCommand) {
					throw new RuntimeException(new String(
							responseCommand.getBody()));
				} else {
					return (Person) SerializeUtil.decodeObject(responseCommand
							.getBody());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return in;
		}

		@Override
		public String helloWorld(String in) {
			String header = "HelloService:helloWorld:String";
			try {
				byte[] data = SerializeUtil.encodeObject(in);
				RpcInvokeCommand command = new RpcInvokeCommand(header,data);
				RpcResponseCommand responseCommand = client.send(command);
				if(responseCommand == null){
					return in;
				}
				else if (responseCommand instanceof BooleanAckCommand) {
					throw new RuntimeException(new String(
							responseCommand.getBody()));
				} else {
					return (String) SerializeUtil.decodeObject(responseCommand
							.getBody());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return in;
		}

	}

	@Override
	public int getClientThreads() {
		return 100;
	}

	@Override
	public String getAuthorId() {
		return "shuihan-remoting";
	}

}
