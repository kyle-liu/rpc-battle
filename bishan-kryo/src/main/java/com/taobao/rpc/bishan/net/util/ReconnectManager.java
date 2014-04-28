//package com.taobao.rpc.bishan.net.util;
//
//import java.util.concurrent.TimeUnit;
//
//import com.taobao.xu.gongan.dataflow.netty.ServiceChannelManager;
//import com.taobao.xu.gongan.util.EnvUtil;
//
//public class ReconnectManager  extends Thread{
//	private ServiceChannelManager client;
//	private String address;
//	
//	public ReconnectManager(ServiceChannelManager client,String address){
//		this.client=client;	
//		this.address=address;
//	}
//	
//	@Override
//	public void run() {
//		int i=0;
//		//��������һ�Σ���Ȼ��ʱ�׳��쳣
//		while(true){
//			try{
//				client.connect(address);
//			}catch(NettyNetException e){
//				EnvUtil.ROOT_LOG.error(i+",reconnectexception,"+address,e);
//			}
//			if(client.isConnect(address)){
//				EnvUtil.ROOT_LOG.error(i+",reconnectsuccess,"+address);
//				break;
//			}
//			i++;
//			try {
//				TimeUnit.SECONDS.sleep(2);
//			} catch (InterruptedException e) {
//				EnvUtil.ROOT_LOG.error("reconInterrupted",e);
//			}
//		}
//	}
//}
