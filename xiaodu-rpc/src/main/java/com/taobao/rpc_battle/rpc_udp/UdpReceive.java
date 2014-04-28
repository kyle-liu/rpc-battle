
/**
 * xiaodu-rpc
 */
package com.taobao.rpc_battle.rpc_udp;

import java.lang.reflect.Method;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import com.esotericsoftware.kryo.Kryo;
import com.taobao.rpc.api.RpcFactory;

/**
 * @author xiaodu
 *
 * 下午2:20:11
 */
public class UdpReceive {
	
	private ExecutorService executorService = Executors.newFixedThreadPool(100);
	
	private static int threads = 6;
	
	private DealThread[] dealThreads = new DealThread[threads];
	
	private Map<String,Method> objectMap = new HashMap<String, Method>();
	
	private Object serverObj = null;

	
	private DatagramSocket socketserver = null;
	
	
	{
		
		
		for(int i=0;i<threads;i++){
			dealThreads[i] = new DealThread();
			executorService.execute(dealThreads[i]);
		}
		
	}
	
	
	public UdpReceive(){
		
	}
	
	public void setInterface(Class<?> clazz){
		 Method[] ms = clazz.getMethods();
		for(Method m:ms){
			objectMap.put(m.getName(), m);
		}
	}
	
	public void setService(Object obj){
		this.serverObj = obj;
	}
	

	public void startup(){
		 try {  
	            InetAddress ip = InetAddress.getLocalHost();  
	            int port = RpcFactory.DEFAULT_PORT;  
	            socketserver = new DatagramSocket(port);  
	            System.out.println("startup UDP Server ip:"+ip+" port:"+port);
	            
	            while(true){
	            	// 确定数据报接受的数据的数组大小  
		            byte[] buf = DatagramPacketUtil.createPacketBytes();
		  
		            // 创建接受类型的数据报，数据将存储在buf中  
		            DatagramPacket getPacket = new DatagramPacket(buf, buf.length);  
		            // 通过套接字接收数据  
		            socketserver.receive(getPacket);  
		            DataEntry de = new DataEntry();
		            de.setData(buf);
		            de.setLen(getPacket.getLength());
		            de.setAddress(getPacket.getSocketAddress());
		            
//		            String uuid = DatagramPacketUtil.readPacketUUID(buf);
		            int m =de.hashCode()%threads;
		            dealThreads[(m)].addDataEntry(de);
	            }
	  
	        } catch (Exception e) {  
	            e.printStackTrace();  
	        }  
	}
	
	private class DataEntry{
		
		private int len;
		
		private byte[] data;
		
		private SocketAddress address;

		public int getLen() {
			return len;
		}

		public void setLen(int len) {
			this.len = len;
		}

		public byte[] getData() {
			return data;
		}

		public void setData(byte[] data) {
			this.data = data;
		}

		public SocketAddress getAddress() {
			return address;
		}

		public void setAddress(SocketAddress address) {
			this.address = address;
		}
		
		
		
	}
	
	private class DealThread implements Runnable{

		private Kryo kryo = KryoUtil.createKryo();
		
		private LinkedBlockingQueue<DataEntry> quene = new LinkedBlockingQueue<DataEntry>();
		
		
		
		public void addDataEntry(DataEntry de){
			quene.add(de);
		}
		
		public DealThread(){
			
		}
		
		@Override
		public void run() {
			DataEntry packet = null;
			try {
				while((packet =quene.take() )!=null){
					
					byte[] receiveBytes = packet.getData();
//					  String uuid = DatagramPacketUtil.readPacketUUID(receiveBytes);
//					  String key = uuid+"_"+packet.getAddress().toString();
//					  System.out.println(key);
//					DatagramPacketBulider db = packetBulider.get(key);
//					if(db== null){
//						db = DatagramPacketUtil.createPacketBulider(this,packet.getAddress());
//						packetBulider.put(key, db);
//					}
//					db.receive(receiveBytes, packet.getLen());
					
					dohandler(receiveBytes,packet.getLen(),packet.getAddress());
					
				}
			} catch (Exception e) {
				e.printStackTrace();
			} 
			
			
		}

		public void dohandler(byte[] packet,int len,SocketAddress sendAddress) {
			try{
			UdpReqPacket udp = KryoUtil.bytes2Object(kryo,packet,len, UdpReqPacket.class);
			
			if(udp == null){
//				System.out.println("获取请求为空 序列化出错 packet length:"+packet.length);
				return ;
			}
			
			String rpcMethodName = udp.getRpcMethodName();
			
			Method method = objectMap.get(rpcMethodName);
			
			Object re = method.invoke(serverObj,udp.getParams());
			
			UdpRespPacket resppacket = new UdpRespPacket();
			resppacket.setUuid(udp.getUuid());
			resppacket.setReturnValue(re);
			
			byte[] out = KryoUtil.Object2Bytes(kryo,resppacket);
			
            DatagramPacket p = new DatagramPacket(out, out.length);
            	p.setSocketAddress(sendAddress);
            	socketserver.send(p);  
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		
	}
	
	
}
