
/**
 * xiaodu-rpc
 */
package com.taobao.rpc_battle.rpc_udp.udp;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import com.taobao.rpc.api.RpcFactory;
import com.taobao.rpc_battle.rpc_udp.DatagramPacketUtil;

/**
 * @author xiaodu
 *
 * 下午10:42:16
 */
public class TestServer {

	/**
	 *@author xiaodu
	 * @param args
	 *TODO
	 */
	public static void main(String[] args) {
		try{
	    InetAddress ip = InetAddress.getLocalHost();  
        int port = RpcFactory.DEFAULT_PORT;  
        DatagramSocket socketserver = new DatagramSocket(port); 
        long time = 0;
        int i=0;
        while(true){
        	// 确定数据报接受的数据的数组大小  
            byte[] buf = new byte[10240];
  
            // 创建接受类型的数据报，数据将存储在buf中  
            DatagramPacket getPacket = new DatagramPacket(buf, buf.length);  
            // 通过套接字接收数据  
            socketserver.receive(getPacket);
            if(time ==0){
            	time = System.currentTimeMillis();
            }
            i++;
            if(getPacket.getLength()==1000){
            	System.out.println(System.currentTimeMillis()-time);
            	System.out.println(i);
            }
            
        }
		}catch(Exception e){
        	e.printStackTrace();
        }
	}

}
