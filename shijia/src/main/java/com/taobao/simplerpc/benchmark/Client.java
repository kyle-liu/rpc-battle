/**
 * $Id: Client.java 864 2012-12-24 07:21:58Z shijia.wxr $
 */
package com.taobao.simplerpc.benchmark;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import com.taobao.simplerpc.DefaultRPCClient;
import com.taobao.simplerpc.RPCClient;


/**
 * ºÚµ•π¶ƒ‹≤‚ ‘£¨Client∂À
 * 
 * @author vintage.wang@gmail.com shijia.wxr@taobao.com
 */
public class Client {
    public static void main(String[] args) {
        RPCClient rpcClient = new DefaultRPCClient();
        boolean connectOK = rpcClient.connect(new InetSocketAddress("127.0.0.1", 2012), 1);
        System.out.println("connect server " + (connectOK ? "OK" : "Failed"));
        rpcClient.start();

        for (long i = 0;; i++) {
            try {
                String reqstr = "nice" + i;
                ByteBuffer repdata = rpcClient.call(reqstr.getBytes());
                if (repdata != null) {
                    String repstr =
                            new String(repdata.array(), repdata.position(), repdata.limit() - repdata.position());
                    System.out.println("call result, " + repstr);
                }
                else {
                    return;
                }
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
