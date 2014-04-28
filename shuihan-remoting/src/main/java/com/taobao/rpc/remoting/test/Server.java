package com.taobao.rpc.remoting.test;

import com.taobao.rpc.remoting.adapter.RpcServer;

public class Server {

    public static void main(String[] args) {
        RpcServer server = new RpcServer();
        server.start(9777);
    }
}
