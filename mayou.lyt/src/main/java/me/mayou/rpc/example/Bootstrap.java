package me.mayou.rpc.example;

import me.mayou.rpc.proxy.ProxyFactory;

public class Bootstrap {

	public static void main(String[] args) throws InterruptedException {
		// System.out.println(testRpc.test());
		for(int i = 0; i < 30; ++i){
			Thread thread = new Thread(new Runnable(){

				@Override
				public void run() {
					byte[] data = new byte[5 * 1024];
					TestRpc testRpc = (TestRpc) ProxyFactory
							.getConsumerProxy(TestRpc.class, "127.0.0.1");
					while (true) {
						testRpc.test(data);
					}
				}
				
			});
			thread.start();
		}

	}

}
