package com.taobao.rpc.bishan.reflect;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import com.taobao.rpc.bishan.net.common.BsCommonCallBack;
import com.taobao.rpc.bishan.net.common.BsFutureDone;
import com.taobao.rpc.bishan.net.common.BsFutureResult;
import com.taobao.rpc.bishan.net.common.DefaultFutureResult;
import com.taobao.rpc.bishan.net.msg.RequstPackage;
import com.taobao.rpc.bishan.net.msg.ResponsePackage;
import com.taobao.rpc.bishan.net.reactor.BsFactory;
import com.taobao.rpc.bishan.net.reactor.BsNetClient;
import com.taobao.rpc.bishan.net.reactor.BsNetListener;
import com.taobao.rpc.bishan.net.util.SequenceIdGenerator;

public class BsServiceClient{

	public static final BsFactory netFactory=new BsFactory();
	
	private BsNetListener bsCallBack=new BsNetListener() {
		@Override
		public void onException(Exception cause) {
			// TODO Auto-generated method stub
			cause.printStackTrace();
			System.out.println("fuck exception");
		}

		@Override
		public void onMsg(BsNetClient client, Object responseResult) {
			ResponsePackage response=(ResponsePackage)responseResult;
			if(response==null){
				System.out.println("aa");
			}
			BsFutureResult<ResponsePackage> cb=requestCallBackMap.get(response.getId());
			requestCallBackMap.remove(response.getId());
			cb.setResult(response);
		}
	};
	private BsNetClient netClient;
	
	private final ConcurrentHashMap<Integer, BsFutureResult<ResponsePackage>> requestCallBackMap =
		new ConcurrentHashMap<Integer, BsFutureResult<ResponsePackage>>();
	
	public BsServiceClient(){
	}
	
	public Object send(RequstPackage datas) throws Exception{
		int id=SequenceIdGenerator.getNextSid();
		BsFutureResult<ResponsePackage> cb=new DefaultFutureResult<ResponsePackage>();
		requestCallBackMap.put(id, cb);
		datas.setId(id);
		
		//TODO 可是使用writeFuture确保数据已写入网卡缓冲区
		BsFutureDone<Boolean>  wfuture=netClient.write(datas);
//		wfuture.addListener(new BsCommonCallBack() {
//			
//			@Override
//			public void onMsg(Object responseResult) {
//
//				System.out.println("bb"+(System.currentTimeMillis()-startTime));
//			}
//			
//			@Override
//			public void onException(Exception cause) {
//				// TODO Auto-generated method stub
//				
//			}
//		});
		
		ResponsePackage res=cb.getResultSyn(100, TimeUnit.SECONDS);
		if(res==null){
			throw new Exception("get data timeout");
			//System.out.println("resule is null");
			//return null;
		}
		
		if(!res.isSuccess()){
			throw new Exception("server response exception",res.getE());
		}
		
		return res.getResponseObj();
	}

	public void setNetClient(BsNetClient netClient) {
		this.netClient = netClient;
	}

	/**
	 * 初始化连接
	 * 
	 * @param ip
	 * @throws Exception 
	 */
	public void initConnect(String ip) throws Exception {
		BsFutureDone<BsNetClient> connectFuture=netFactory.clientConnect(ip, 1985,bsCallBack);
		connectFuture.waitDone(2, TimeUnit.SECONDS);
		netClient=connectFuture.getAttachObj();
	}
	
}
