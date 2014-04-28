package com.taobao.rpc;

import java.lang.reflect.Proxy;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import com.taobao.rpc.api.RpcFactory;
import com.taobao.rpc.benchmark.main.Util;




public class JiurenRpcFactory implements RpcFactory {

    @Override
    public <T> void export(Class<T> type, T serviceObject) {
        int port = Integer.parseInt(Util.properties.getProperty("port"));

        try {
            DatagramSocket socket = new DatagramSocket(port);
            System.out.println("bind to port " + port + " ...");
            
            final int LENGTH = 65535;
            DatagramPacket recvPack = new DatagramPacket(new byte[LENGTH], 0, LENGTH);
            
            
            for (;;) {
                socket.receive(recvPack);
                socket.send(recvPack);
                recvPack.setLength(LENGTH);
            }
            
            
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        

    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getReference(Class<T> type, String ip) {
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] { type },
                new JiurenClientMultiplexing(ip));
    }

    @Override
    public int getClientThreads() {
        return 100;
    }

    @Override
    public String getAuthorId() {
        return "Jiuren";
    }

}
