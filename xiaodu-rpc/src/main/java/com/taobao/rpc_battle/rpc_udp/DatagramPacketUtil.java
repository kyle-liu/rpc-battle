
/**
 * xiaodu-rpc
 */
package com.taobao.rpc_battle.rpc_udp;

import java.net.DatagramPacket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author xiaodu
 *
 * 下午4:08:55
 */
public class DatagramPacketUtil {
	
	
	
	public static  byte[] createPacketBytes(){
		return new byte[10240];
	}
	
	
//	public static DatagramPacketBulider createPacketBulider(UdpPacketHandler handler,SocketAddress sendAddress){
//		return new DatagramPacketBulider(handler,sendAddress);
//	}
//	
//	
//	public static  class DatagramPacketBulider{
//		
//		private int packetSize;
//		
//		private AtomicInteger currentSize = new AtomicInteger(0);
//		
//		private byte[] merger = null;
//		
//		private UdpPacketHandler handler = null;
//		
//		private SocketAddress sendAddress;
//		
//		
//		public DatagramPacketBulider(UdpPacketHandler handler){
//			this.handler = handler;
//		}
//		
//		public DatagramPacketBulider(UdpPacketHandler handler,SocketAddress sendAddress){
//			this.handler = handler;
//			this.sendAddress = sendAddress;
//		}
//		
//		
//		public void receive(byte[] pbyte,int len){
//			
//			int num = readPacketNum(pbyte);
//			
//			int index = readPacketIndex(pbyte);
//			
//			int allLen = readPacketLen(pbyte);
//			
//			if(merger == null){
//				merger = new byte[MAX_PACKET_SIZE*num];
//				packetSize = num;
//			}
//			System.arraycopy(pbyte, 16, merger, index*MAX_PACKET_SIZE, len-16);
//			int i= currentSize.incrementAndGet();
//			if( i == packetSize){
//				//填充完成
//				handler.handler(merger,sendAddress);
//			}
//		}
//		
//		
//		public byte[] bulider(){
//			return merger;
//		}
//		
//		
//	}
//	

	private static int MAX_PACKET_SIZE = 1024;
	
//	public static DatagramPacket[] splitDatagramPacket(byte[] packet){
//		
//		byte[] id =UUID.randomUUID().toString().getBytes();
//		int len = packet.length;
//		int size= len/MAX_PACKET_SIZE;
//		int a = size;
//		int remain = len%MAX_PACKET_SIZE;
//		
//		if(size>0&&remain >0){
//			a++;
//		}
//		
//		DatagramPacket[] packets = new DatagramPacket[a];
//		int max = MAX_PACKET_SIZE;
//		for(int i=0;i<a;i++){
//			
//			if(i*MAX_PACKET_SIZE >len){
//				max = remain;
//			}
//			ByteBuffer b = ByteBuffer.allocate(36+4+4+4+max);
//			//唯一标记符
//			b.put(id);
//	        //总共有多少个拆包
//			b.putInt(a);
//	        //当前多少位置
//			b.putInt(i);
//	        //总长度
//			b.putInt(len);
//	        //data
//			b.put(packet, i*MAX_PACKET_SIZE,max);
//			packets[i] = new DatagramPacket(b.array(),0,b.array().length);
//			
//		}
//		return packets;
//		
//	}
	
	
//	public static String readPacketUUID(byte[] readBuffer){
//		  return new String(readBuffer,0,36);
//	}
//	
//	public static int readPacketNum(byte[] readBuffer){
//		  byte ch1 =readBuffer[37];
//		  byte ch2 =readBuffer[38];
//		  byte ch3 =readBuffer[39];
//		  byte ch4 =readBuffer[40];
//	      return ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0));
//	}
//	
//	public static int readPacketIndex(byte[] readBuffer){
//		  byte ch1 =readBuffer[41];
//		  byte ch2 =readBuffer[42];
//		  byte ch3 =readBuffer[43];
//		  byte ch4 =readBuffer[44];
//	      return ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0));
//	}
//	
//	public static int readPacketLen(byte[] readBuffer){
//		  byte ch1 =readBuffer[45];
//		  byte ch2 =readBuffer[46];
//		  byte ch3 =readBuffer[47];
//		  byte ch4 =readBuffer[48];
//	      return ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0));
//	}

}
