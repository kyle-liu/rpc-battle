package com.taobao.rpc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import com.taobao.rpc.benchmark.main.Util;



public class JiurenClientMultiplexing implements InvocationHandler {

    public JiurenClientMultiplexing(String ip) {
        try {
            int port = Integer.parseInt(Util.properties.getProperty("port"));
            target = new InetSocketAddress(ip, port);
            socket = new DatagramSocket();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        
        responseFutures = new ConcurrentHashMap<Long, ArrayBlockingQueue<Object>>();
        startReaderThread();
    }
    
    
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        final Object param = args[0];
        try {
            return sendMsg(param);
        } catch (Exception e) {
            e.printStackTrace();
            return param;
        }
    }
    
    public Object sendMsg(Object obj) throws Exception {
        long msgId = sequence.incrementAndGet();
        setMsgId(msgId);

        byte[] buf = long2bytes(msgId);
        DatagramPacket sendPack = new DatagramPacket(buf, buf.length, target);
        socket.send(sendPack);
        Object respMsgId = waitResponse(msgId);

        if (!respMsgId.equals(msgId)) {
            System.err.println("resp != request, " + msgId + ", " + respMsgId);
        }
        
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ObjectOutputStream jout = new ObjectOutputStream(bout);
        jout.writeObject(obj);
        jout.flush();
        byte[] objbytes = bout.toByteArray();
        ObjectInputStream jin = new ObjectInputStream(new ByteArrayInputStream(objbytes));
        return jin.readObject();
    }
    
    
    public void setMsgId(long msgId) {
        responseFutures.put(msgId, new ArrayBlockingQueue<Object>(1));
    }
    
    public Object waitResponse(long msgId) throws InterruptedException {
        return responseFutures.get(msgId).poll(3000L, TimeUnit.MILLISECONDS);
    }

    private byte[] long2bytes(long v) {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(bout);
        try {
            dout.writeLong(v);
            return bout.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private long bytes2long(byte[] readBuffer) {
        ByteArrayInputStream bin = new ByteArrayInputStream(readBuffer);
        try {
            return new DataInputStream(bin).readLong();
        } catch (IOException ioe) {
            ioe.printStackTrace();
            return -1;
        }
    }
    
    private void startReaderThread() {
        Thread reader = new Thread(new Runnable() {
            public void run() {
                readThreadRun0();
            }
        });
        reader.setName("com.taobao.rpc.clientReader");
        reader.start();
    }
    
    private void readThreadRun0() {
        byte[] buf = new byte[8];
        DatagramPacket recvPack = new DatagramPacket(buf, buf.length);

        for (;;) {
            try {
                socket.receive(recvPack);
                long msgId = bytes2long(recvPack.getData());
                BlockingQueue<Object> queue = responseFutures.get(msgId);
                if (null != queue) {
                    queue.offer(msgId);
                } else {
                    System.err.println("discard response msgId " + msgId);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    // ====================
    static final AtomicLong sequence = new AtomicLong(0);
    
    final DatagramSocket socket;
    final SocketAddress target;
    final ConcurrentHashMap<Long, ArrayBlockingQueue<Object>> responseFutures;
}
