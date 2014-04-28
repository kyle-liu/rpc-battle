///**
// * xiaodu-rpc
// */
//package com.taobao.rpc_battle.rpc_udp.netty;
//
//import java.lang.reflect.Method;
//import java.net.DatagramPacket;
//import java.net.DatagramSocket;
//import java.net.InetSocketAddress;
//import java.net.SocketAddress;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//import java.util.concurrent.LinkedBlockingQueue;
//
//import org.jboss.netty.bootstrap.ConnectionlessBootstrap;
//import org.jboss.netty.buffer.ChannelBuffer;
//import org.jboss.netty.channel.ChannelHandlerContext;
//import org.jboss.netty.channel.ChannelPipeline;
//import org.jboss.netty.channel.ChannelPipelineFactory;
//import org.jboss.netty.channel.Channels;
//import org.jboss.netty.channel.ExceptionEvent;
//import org.jboss.netty.channel.MessageEvent;
//import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
//import org.jboss.netty.channel.socket.nio.NioDatagramChannelFactory;
//
//import com.taobao.rpc.api.RpcFactory;
//import com.taobao.rpc_battle.rpc_udp.DatagramPacketUtil;
//import com.taobao.rpc_battle.rpc_udp.KryoUtil;
//import com.taobao.rpc_battle.rpc_udp.UdpPacketHandler;
//import com.taobao.rpc_battle.rpc_udp.UdpReqPacket;
//import com.taobao.rpc_battle.rpc_udp.UdpRespPacket;
//import com.taobao.rpc_battle.rpc_udp.DatagramPacketUtil.DatagramPacketBulider;
//
///**
// * @author xiaodu
// * 
// *         下午4:58:51
// */
//public class UdpNettyServer {
//
//	private ConnectionlessBootstrap udpBootstrap;
//	
//	
//	
//	
//	private class DealThread implements Runnable,UdpPacketHandler{
//
//		private LinkedBlockingQueue<DataEntry> quene = new LinkedBlockingQueue<DataEntry>();
//		
//		private Map<String,DatagramPacketBulider> packetBulider = new HashMap<String,DatagramPacketBulider>();
//		
//		
//		public void addDataEntry(DataEntry de){
//			quene.add(de);
//		}
//		
//		public DealThread(){
//			
//		}
//		
//		@Override
//		public void run() {
//			DataEntry packet = null;
//			try {
//				while((packet =quene.take() )!=null){
//					
//					byte[] receiveBytes = packet.getData();
//					  String uuid = DatagramPacketUtil.readPacketUUID(receiveBytes);
//					  String key = uuid+"_"+packet.getAddress().toString();
//					  System.out.println(key);
//					DatagramPacketBulider db = packetBulider.get(key);
//					if(db== null){
//						db = DatagramPacketUtil.createPacketBulider(this,packet.getAddress());
//						packetBulider.put(key, db);
//					}
//					db.receive(receiveBytes, packet.getLen());
//				}
//			} catch (Exception e) {
//				e.printStackTrace();
//			} 
//			
//			
//		}
//
//		@Override
//		public void handler(byte[] packet,SocketAddress sendAddress) {
//			try{
//			UdpReqPacket udp = KryoUtil.bytes2Object(packet, UdpReqPacket.class);
//			String rpcMethodName = udp.getRpcMethodName();
//			
//			Method method = objectMap.get(rpcMethodName);
//			
//			Object re = method.invoke(serverObj,udp.getParams());
//			
//			UdpRespPacket resppacket = new UdpRespPacket();
//			resppacket.setReturnValue(re);
//			
//			byte[] out = KryoUtil.Object2Bytes(resppacket);
//			
//            DatagramPacket[] packets = DatagramPacketUtil.splitDatagramPacket(out);
//            for(DatagramPacket p:packets){
//            	p.setSocketAddress(sendAddress);
//            	socketserver.send(p);  
//            }
//			}catch(Exception e){
//				e.printStackTrace();
//			}
//		}
//		
//	}
//	
//private class DataEntry{
//		
//		private int len;
//		
//		private byte[] data;
//		
//		private SocketAddress address;
//
//		public int getLen() {
//			return len;
//		}
//
//		public void setLen(int len) {
//			this.len = len;
//		}
//
//		public byte[] getData() {
//			return data;
//		}
//
//		public void setData(byte[] data) {
//			this.data = data;
//		}
//
//		public SocketAddress getAddress() {
//			return address;
//		}
//
//		public void setAddress(SocketAddress address) {
//			this.address = address;
//		}
//		
//		
//		
//	}
//
//private ExecutorService executorService = Executors.newFixedThreadPool(100);
//
//private DealThread[] dealThreads = new DealThread[100];
//
//private Map<String,Method> objectMap = new HashMap<String, Method>();
//
//private Object serverObj = null;
//
//
//private DatagramSocket socketserver = null;
//
//
//{
//	
//	
//	for(int i=0;i<100;i++){
//		dealThreads[i] = new DealThread();
//		executorService.execute(dealThreads[i]);
//	}
//	
//}
//
//
//
//public void setInterface(Class<?> clazz){
//	 Method[] ms = clazz.getMethods();
//	for(Method m:ms){
//		objectMap.put(m.getName(), m);
//	}
//}
//
//public void setService(Object obj){
//	this.serverObj = obj;
//}
//
//	public UdpNettyServer(int port) {
//		NioDatagramChannelFactory channelFactory = new NioDatagramChannelFactory(Executors.newCachedThreadPool());
//		udpBootstrap = new ConnectionlessBootstrap(channelFactory);
//		udpBootstrap.setPipelineFactory(new ChannelPipelineFactory() {
//			@Override
//			public ChannelPipeline getPipeline() throws Exception {
//				return Channels.pipeline(new UdpNettyEventHandler());
//			}
//		});
//		udpBootstrap.bind(new InetSocketAddress(port));
//		System.out.println("udp server started, listening on port:" + port);
//	}
//	
//	public class UdpNettyEventHandler extends SimpleChannelUpstreamHandler{
//		@Override
//	     public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
//		             throws Exception {
//		         	ChannelBuffer buffer = (ChannelBuffer)e.getMessage();
//			         byte[] a = buffer.array();
//			         DataEntry de = new DataEntry();
//		            de.setData(a);
//		            de.setLen(a.length);
//		            de.setAddress(ctx.getChannel().getRemoteAddress());
//		            
//		            String uuid = DatagramPacketUtil.readPacketUUID(a);
//		            int m = uuid.hashCode()%100;
//		            dealThreads[Math.abs(m)].addDataEntry(de);
//		         
//		     }
//		 
//		     @Override
//		     public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
//		             throws Exception {
//		     }
//
//	}
//	
//	
//	public static void main(String[] args) {
//		UdpNettyServer server = new UdpNettyServer(RpcFactory.DEFAULT_PORT);
//	}
//	
//}
