/**
 * $Id: MTClient.java 866 2012-12-24 07:26:10Z shijia.wxr $
 */
package com.taobao.simplerpc.benchmark;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicLong;

import com.taobao.simplerpc.DefaultRPCClient;
import com.taobao.simplerpc.RPCClient;


/**
 * ���߳̿ͻ��ˣ�������ѹ��
 * 
 * @author vintage.wang@gmail.com shijia.wxr@taobao.com
 */
public class MTClient {
    private static byte[] buildMessage(final int size) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < size; i++) {
            sb.append("K");
        }

        return sb.toString().getBytes();
    }


    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Useage: mtclient remoteHost remotePort [messageSize] [threadCnt] [connectionCnt]");
            return;
        }

        // args
        String remoteHost = args.length > 0 ? args[0] : "127.0.0.1";
        int remotePort = args.length > 1 ? Integer.valueOf(args[1]) : 2012;
        int messageSize = args.length > 2 ? Integer.valueOf(args[2]) : 1024 * 5;
        int threadCnt = args.length > 3 ? Integer.valueOf(args[3]) : 128;
        int connectionCnt = args.length > 4 ? Integer.valueOf(args[4]) : 1;

        // thread pool
        final ThreadPoolExecutor executorSend = (ThreadPoolExecutor) Executors.newFixedThreadPool(threadCnt);

        // rpcclient
        final RPCClient rpcClient = new DefaultRPCClient();
        final boolean connectOK = rpcClient.connect(new InetSocketAddress(remoteHost, remotePort), connectionCnt);
        System.out.println("connect server " + remoteHost + (connectOK ? " OK" : " Failed"));
        rpcClient.start();

        // status
        final byte[] message = buildMessage(messageSize);
        final AtomicLong callTimesOK = new AtomicLong(0);
        final AtomicLong callTimesFailed = new AtomicLong(0);

        // multi thread call
        for (int i = 0; i < threadCnt; i++) {
            executorSend.execute(new Runnable() {
                public void run() {
                    while (true) {
                        try {
                            ByteBuffer repdata = rpcClient.call(message);
                            if (repdata != null) {
                                callTimesOK.incrementAndGet();
                            }
                            else {
                                callTimesFailed.incrementAndGet();
                            }
                        }
                        catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }

        // stats thread
        Thread statsThread = new Thread(new Runnable() {
            long lastTimestamp = 0;
            long lastCallTimesOK = 0;


            public void run() {
                while (true) {
                    long timestamp = System.currentTimeMillis();
                    long thisCallTimesOK = callTimesOK.get();
                    double interval = (timestamp - this.lastTimestamp) / 1000;

                    System.out.printf("call OK QPS: %.2f Failed Times: %d\n",
                        (thisCallTimesOK - this.lastCallTimesOK) / interval, callTimesFailed.get());

                    this.lastTimestamp = timestamp;
                    this.lastCallTimesOK = thisCallTimesOK;

                    try {
                        Thread.sleep(3000);
                    }
                    catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, "statsThread");

        statsThread.start();
    }
}
