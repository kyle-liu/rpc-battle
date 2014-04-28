//
///**
// * xiaodu-rpc
// */
//package com.taobao.rpc_battle.rpc_udp;
//
//import java.io.IOException;
//import java.net.DatagramPacket;
//import java.net.DatagramSocket;
//import java.net.InetAddress;
//import java.util.concurrent.locks.Condition;
//import java.util.concurrent.locks.Lock;
//import java.util.concurrent.locks.ReentrantLock;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import com.taobao.rpc.api.RpcFactory;
//
///**
// * @author xiaodu
// *
// * 上午11:55:57
// */
//public class MethodRequest {
//	
//	 protected final static Logger log = LoggerFactory.getLogger(UdpSender.class);
//	
//	private final Lock lock = new ReentrantLock();
//	
//	private final Condition returnWait  = lock.newCondition();
//	
//	private ReturnValue returnValue = null;
//	
//	private DatagramSocket sendSocket = null; 
//	
//	private InetAddress serverAddress;
//	
//	private String methodName;
//	
//	private Object[] args;
//	
//	public MethodRequest(InetAddress serverAddress,DatagramSocket sendSocket,String methodName, Object[] args){
//		this.sendSocket = sendSocket;
//		this.serverAddress = serverAddress;
//		this.methodName = methodName;
//		this.args = args;
//	}
//	
//	
//public class ReturnValue{
//	
//		private Object object;
//
//		public Object getObject() {
//			return object;
//		}
//
//		public void setObject(Object object) {
//			this.object = object;
//		}
//	}
//
//public void setReturnValue(ReturnValue value){
//	this.returnValue = value;
//	
//	
//}
//	
//	public Object invoke(){
//		UdpReqPacket up = new UdpReqPacket();
//		up.setRpcMethodName(methodName);
//		up.setParams(args);
//		
//		byte[] out = KryoUtil.Object2Bytes(up);
//		
//		DatagramPacket[] packets = DatagramPacketUtil.splitDatagramPacket(out);
//		int i = 0;
//        while(returnValue == null){
//        	   try {
//        			for(DatagramPacket packet:packets){
//        				packet.setAddress(serverAddress);
//        				packet.setPort(RpcFactory.DEFAULT_PORT);
//        				sendSocket.send(packet);
//        			}
//   			} catch (IOException e) {
//   				log.error("send DatagramPacket error",e);
//   			}
//        	   lock.lock();
//	        try {
//	        	returnWait.awaitNanos(100);
//			} catch (InterruptedException e) {
//			}finally{
//				lock.unlock();
//			}
//	        
//	        if(i>0){
//	        	log.info("重新发送 methodName:"+methodName+" out.getBuffer().length:"+out.length+" 分拆个数:"+packets.length);
//	        }
//	        i++;
//	}
//        
//        return returnValue.getObject();
//	}
//
//}
