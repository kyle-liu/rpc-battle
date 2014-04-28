package com.taobao.rpc.remoting.test;

import com.taobao.gecko.core.command.kernel.BooleanAckCommand;
import com.taobao.rpc.remoting.adapter.RpcClient;
import com.taobao.rpc.remoting.command.RpcResponseCommand;
import com.taobao.rpc.remoting.command.RpcInvokeCommand;


public class Client {

    public static void main(String[] args) throws InterruptedException {
        RpcClient client = new RpcClient();
        client.start();
        client.connect("localhost", 9777, 1);
        for (int i = 0; i < 100000; i++) {
            String header = "HelloWorld.class";
            byte [] body = "invoke".getBytes();
            RpcInvokeCommand command = new RpcInvokeCommand(header,body);
            RpcResponseCommand response = client.send(command);
            if(response instanceof BooleanAckCommand){
                System.out.println(((BooleanAckCommand)response).getErrorMsg());
            }
            else if(response != null) {
                System.out.println("client " + new String(response.getBody()));
            }
        }
    }
}
