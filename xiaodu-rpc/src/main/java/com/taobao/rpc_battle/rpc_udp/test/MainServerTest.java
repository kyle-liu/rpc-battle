
/**
 * xiaodu-rpc
 */
package com.taobao.rpc_battle.rpc_udp.test;

import com.taobao.rpc.benchmark.service.HelloService;
import com.taobao.rpc.benchmark.service.HelloServiceImpl;
import com.taobao.rpc_battle.rpc_udp.RpcFactoryImpl;

/**
 * @author xiaodu
 *
 * 下午2:55:04
 */
public class MainServerTest {

	/**
	 *@author xiaodu
	 * @param args
	 *TODO
	 */
	public static void main(String[] args) {
		
		RpcFactoryImpl rpc = new RpcFactoryImpl();
		rpc.export(HelloService.class, new HelloServiceImpl());

	}

}
