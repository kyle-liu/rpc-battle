package example.hello;

import shenfeng.simplerpc.Registry;

import com.taobao.rpc.benchmark.service.HelloService;
import com.taobao.rpc.benchmark.service.HelloServiceImpl;

public class Server implements Hello {

	public static void main(String[] args) {
		Registry registry = new Registry();
		registry.register(HelloService.class, new HelloServiceImpl(), 1313);
		System.err.println("Server ready");
	}

	@Override
	public String sayHello() {
		return "Hello, world!";
	}
}
