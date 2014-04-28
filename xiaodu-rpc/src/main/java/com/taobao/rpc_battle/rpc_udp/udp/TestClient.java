
/**
 * xiaodu-rpc
 */
package com.taobao.rpc_battle.rpc_udp.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.CountDownLatch;

import com.taobao.rpc.api.RpcFactory;

/**
 * @author xiaodu
 *
 * 下午10:46:06
 */
public class TestClient {

	/**
	 *@author xiaodu
	 * @param args
	 *TODO
	 */
	public static void main(String[] args) {
		try {
			
			Thread.sleep(2000);
			long time = System.currentTimeMillis();
			
			final CountDownLatch down = new CountDownLatch(3);
			final DatagramSocket sendSocket = new DatagramSocket();
			Thread thread1 = new Thread(){
				public void run(){
					try{
					for(int i=0;i<1000000;i++){
						DatagramPacket packet = new DatagramPacket(new byte[9999], 9999);
						packet.setAddress(InetAddress.getByName("127.0.0.1"));
						packet.setPort(RpcFactory.DEFAULT_PORT);
						sendSocket.send(packet);
					}}catch(Exception e){
						
					}
					down.countDown();
				}
			};
			Thread thread2 = new Thread(){
				public void run(){
					try{
						for(int i=0;i<1000000;i++){
							DatagramPacket packet = new DatagramPacket(new byte[9999], 9999);
							packet.setAddress(InetAddress.getByName("127.0.0.1"));
							packet.setPort(RpcFactory.DEFAULT_PORT);
							sendSocket.send(packet);
						}}catch(Exception e){
							
						}
					down.countDown();;
				}
			};
			Thread thread3 = new Thread(){
				public void run(){
					try{
						for(int i=0;i<1000000;i++){
							DatagramPacket packet = new DatagramPacket(new byte[9999], 9999);
							packet.setAddress(InetAddress.getByName("127.0.0.1"));
							packet.setPort(RpcFactory.DEFAULT_PORT);
							sendSocket.send(packet);
						}}catch(Exception e){
							
						}
					down.countDown();;
				}
			};
			
			thread1.start();
			thread2.start();
			thread3.start();
			down.await();
			DatagramPacket packet = new DatagramPacket(new byte[1000], 1000);
			packet.setAddress(InetAddress.getByName("127.0.0.1"));
			packet.setPort(RpcFactory.DEFAULT_PORT);
			sendSocket.send(packet);
			System.out.println(System.currentTimeMillis()-time);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
	}

}
