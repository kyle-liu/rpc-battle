package com.taobao.shuihan.rpc;

import com.taobao.rpc.api.RpcFactory;
import com.taobao.rpc.benchmark.service.HelloService;
import com.taobao.rpc.benchmark.service.HelloServiceImpl;

/**
 * @author ding.lid
 */
public class ServerMain {

    public static void main(String[] args) throws Exception {
        RpcFactory rpcFactory = Util.getRpcFactoryImpl();

        rpcFactory.export(HelloService.class, new HelloServiceImpl());

        synchronized (ServerMain.class) {
            ServerMain.class.wait(); // block main thread to prevent process exit.
        }
    }
}
