
/**
 * xiaodu-rpc
 */
package com.taobao.rpc_battle.rpc.server;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;

/**
 * @author xiaodu
 *
 * ����11:58:56
 */
public class MainServer {
	
	
	
	
	public class RpcEntry{
		private long id;//������� rpc����
		
		private String className;
		
		private String methodName;

		public long getId() {
			return id;
		}

		public void setId(long id) {
			this.id = id;
		}

		public String getClassName() {
			return className;
		}

		public void setClassName(String className) {
			this.className = className;
		}

		public String getMethodName() {
			return methodName;
		}

		public void setMethodName(String methodName) {
			this.methodName = methodName;
		}
	}
	
	
	
	
	
	public void startup(int port){
		try {
			ServerSocket server = new ServerSocket(port);
			server.setReuseAddress(true);
			//server.setReceiveBufferSize(size)
			//server.setPerformancePreferences(connectionTime, latency, bandwidth)
			//server.setSoTimeout(timeout)
			
			
			
			
		
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	private void service(ServerSocket server){
		try {
			Socket socket = null;
			while((socket = server.accept())!=null){
				//socket.setKeepAlive(on)
				//socket.setOOBInline(on)
				//socket.setPerformancePreferences(connectionTime, latency, bandwidth)
				//socket.setReceiveBufferSize(size)
				//socket.setReuseAddress(on)
				//socket.setSendBufferSize(size)
				//socket.setSoLinger(on, linger)
				//socket.setSoTimeout(timeout)
				//socket.setTcpNoDelay(on)
				//socket.setTrafficClass(tc)
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private class SocketHandle extends Thread{
		private Socket socket = null;
		
		public SocketHandle(Socket socket ){
			this.socket = socket;
		}
		
		public void run(){
			
//			try {
//				ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
////				ByteArrayInputStream in = new ByteArrayInputStream( socket.getInputStream());
//				
//				
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
			
			
			
		}
		
	}
	

	/**
	 *@author xiaodu
	 * @param args
	 *TODO
	 */
	public static void main(String[] args) {

	}

}
