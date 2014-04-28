
/**
 * xiaodu-rpc
 */
package com.taobao.rpc_battle.rpc_udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


import com.esotericsoftware.kryo.Kryo;
import com.taobao.rpc.api.RpcFactory;

/**
 * @author xiaodu
 *
 * 下午2:18:38
 */
public class UdpSender implements Runnable{
	
	
	private Kryo kryo1 = KryoUtil.createKryo();
	private Kryo kryo2= KryoUtil.createKryo();
	
	private InetAddress ip;
	
	private DatagramSocket sendSocket = null;
	
	private Lock lock = new ReentrantLock();
	
	final Condition returnWait  = lock.newCondition(); 
	
	private Thread thread = null;

	private static AtomicLong atom = new AtomicLong(0);
	
	private static Map<Long,ArrayBlockingQueue<UdpRespPacket>> queueMap = new ConcurrentHashMap<Long,  ArrayBlockingQueue<UdpRespPacket>>();
	
	
	public UdpSender(String ip){
		try {
			this.ip =InetAddress.getByName(ip);
			  // 创建发送方的套接字，IP默认为本地，端口号随机  
		 	sendSocket = new DatagramSocket();  
		} catch (Exception e) {
			System.out.println("创建UDP socket 失败!");
		} 
		
		thread = new Thread(this);
		thread.start();
	}
	
	public Object call(String methodName, Object[] args){
		Long key = atom.incrementAndGet();
		ArrayBlockingQueue<UdpRespPacket> queue= queueMap.get(key);
		if(queue == null){
			queue = new ArrayBlockingQueue<UdpRespPacket>(100);
			queueMap.put(key, queue);
		}
		
		UdpReqPacket up = new UdpReqPacket();
		up.setUuid(key);
		up.setRpcMethodName(methodName);
		up.setParams(args);
		
		byte[] out = KryoUtil.Object2Bytes(kryo2,up);
        	UdpRespPacket resp= null;
        	while(resp == null){
        		try {
            		   DatagramPacket packet = new DatagramPacket(out, out.length);
           				packet.setAddress(ip);
           				packet.setPort(RpcFactory.DEFAULT_PORT);
           				sendSocket.send(packet);
       			} catch (IOException e) {
       			}
        		try {
					resp = queue.poll(10, TimeUnit.MILLISECONDS);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        	}
        	queue.clear();
        return resp.getReturnValue();
	}

	public void startup(){
		
		
		
	}

	@Override
	public void run() {
		 
		while(true){
			try {
				 // 确定数据报接受的数据的数组大小  
		        byte[] buf = DatagramPacketUtil.createPacketBytes();
		        // 创建接受类型的数据报，数据将存储在buf中  
		        DatagramPacket packet = new DatagramPacket(buf, buf.length);  
				sendSocket.receive(packet);
				byte[] receiveBytes = packet.getData();
//				String uuid = DatagramPacketUtil.readPacketUUID(receiveBytes);
//				DatagramPacketBulider db = packetBulider.get(uuid);
//				if(db== null){
//					db = DatagramPacketUtil.createPacketBulider(this,null);
//					packetBulider.put(uuid, db);
//				}
//				db.receive(receiveBytes, packet.getLength());
				
				dohandler(receiveBytes,packet.getLength(),packet.getSocketAddress());
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	
	public class ReturnValue{
		
		
		private Object object;

		public Object getObject() {
			return object;
		}

		public void setObject(Object object) {
			this.object = object;
		}
		
		
	}
	public void dohandler(byte[] packet,int len,SocketAddress sendAddress) {
		
		UdpRespPacket udp = KryoUtil.bytes2Object(kryo1,packet,len, UdpRespPacket.class);
		ArrayBlockingQueue<UdpRespPacket> queue= queueMap.get(udp.getUuid());
		if(queue != null)		
			queue.add(udp);
		
	}

//	@Override
//	public void handler(byte[] packet,SocketAddress sendAddress) {
//		
////		UdpRespPacket udp = KryoUtil.bytes2Object(kryo,packet, UdpRespPacket.class);
////		
////		ReturnValue value = new ReturnValue();
////		value.setObject(udp.getReturnValue());
////		returnValue = value;
////		   lock.lock();
////		   try{
////			   returnWait.signalAll();
////		}finally{
////			lock.unlock();
////		}
//		
//	}

}
