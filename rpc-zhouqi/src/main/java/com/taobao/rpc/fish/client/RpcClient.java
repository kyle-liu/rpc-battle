package com.taobao.rpc.fish.client;



import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import net.sf.cglib.proxy.Enhancer;

import com.taobao.gecko.service.RemotingClient;
import com.taobao.gecko.service.RemotingFactory;
import com.taobao.gecko.service.config.ClientConfig;
import com.taobao.gecko.service.exception.NotifyRemotingException;
import com.taobao.rpc.fish.common.wireformat.SimpleRpcWireFormatType;
import com.taobao.rpc.fish.test.TestInterface;
import com.taobao.rpc.fish.test.User;

public class RpcClient {

	private RemotingClient client;
	private int connsize;
	private int defaultPort=8079;
	private Set<String> groupSet=new HashSet<String>();
	private final AtomicInteger opaGenerator=new AtomicInteger(0);
	private final Set<String> urls=new HashSet<String>();
	public RpcClient(int connSize){
		 final ClientConfig clientConfig = new ClientConfig();
	        clientConfig.setTcpNoDelay(false);
	        clientConfig.setWireFormatType(new SimpleRpcWireFormatType());
	        clientConfig.setMaxScheduleWrittenBytes(Runtime.getRuntime().maxMemory() / 3);
	        clientConfig.setSndBufferSize(1024*1024*2);
	        clientConfig.setRcvBufferSize(1024*1024*2);
	        //clientConfig.setWriteThreadCount(writeThreadCount)
	        clientConfig.setSelectorPoolSize(4);
	        clientConfig.setIdleTime(1000);
	        clientConfig.setKeepAlive(true);
	        clientConfig.setMaxScheduleWrittenBytes(1024*1024*1024);
	        clientConfig.setMaxReadBufferSize(1024*1024*1024);
	        //clientConfig.setReadThreadCount(4);
	       try {
			this.client=RemotingFactory.connect(clientConfig);
			
		} catch (NotifyRemotingException e) {
			// TODO Auto-generated catch block
			throw new RuntimeException(e);
		}
		this.connsize=connSize;
		
		
	}
	public <T> T getReference(Class<T> type,String ip){
		if(type==null)return null;
		String url="rpc://"+ip+":"+defaultPort;
		if(urls.add(url)){
			try {
				this.client.connect(url, connsize);
				this.client.awaitReadyInterrupt(url);
				this.groupSet.add(url);
			} catch (Exception e) {
			// TODO Auto-generated catch block
			throw new RuntimeException(e);
			}
		}
		Enhancer enhancer = new Enhancer(); 
        enhancer.setSuperclass(type); 
        enhancer.setCallback(new RpcInvokeHandler(type, this.client,this.opaGenerator , url)); 
        T proxy=(T)enhancer.create(); 
		//T ref=(T)Proxy.newProxyInstance(this.getClass().getClassLoader(),new Class[]{type},);
		return proxy;
	}
	public void stop(){
		for(String group:groupSet){
			try {
				this.client.close(group, false);
			} catch (NotifyRemotingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	public static void main(String agrs[]){
		String ip="localhost";
		RpcClient client=new RpcClient(4);
		//CodecFacory.getDeserializer();
		//CodecFacory.getSerializer();
		
		int threadSize=10;
		final TestInterface references[]=new TestInterface[threadSize];
		for(int i=0;i<threadSize;i++){
			references[i]=client.getReference(TestInterface.class, ip);
		}
		final AtomicLong counter = new AtomicLong();
		 final AtomicBoolean correct = new AtomicBoolean(true);
	     final Thread mainThread = Thread.currentThread();
		ExecutorService executorService = Executors.newFixedThreadPool(threadSize);
		final AtomicBoolean stopped=new AtomicBoolean(false);
        for(int i = 0; i < threadSize; ++i) {
            final int idx = i;
            Runnable invoker = new Runnable() {
                boolean tag = idx % 2 == 0;
                final TestInterface reference=references[idx];
                @Override
                public void run() {
                	
                    while(!stopped.get()) {
                        if(tag) {
                            User user = reference.getUser();
                            if(user==null) {
                                System.err.println("Wrong Result!");
                                correct.set(false);
                                stopped.set(true);
                                mainThread.interrupt();
                            }
                            
                        }
                        else {
                        	String address="address"+System.currentTimeMillis();
                            User user=reference.insertUser("zhouqi", 26, "��", address, new byte[1024*2]);
                            if(user==null||!user.getAddress().equals(address)) { // FIXME DO NOT check every time!
                                System.err.println("Wrong Result!");
                                correct.set(false);
                                stopped.set(true);
                                mainThread.interrupt();
                            }
                        }
                       // System.out.println("receive response="+counter.get());
                        counter.incrementAndGet();
                    }
                }
            };

            executorService.execute(invoker);
        }

       // Thread.sleep(10000);
        long runDuration=1000*10;
        final long beginCount = counter.get();
        final long startTime = System.currentTimeMillis();
        while (true) {
            long now = System.currentTimeMillis();
            long left = runDuration - (now - startTime);
            if(left <= 0) break;
            try {
                Thread.sleep(left);
            } catch (InterruptedException e) {
                // ignore
            }
        }
        final long invokeTime = counter.get() - beginCount;
        final long costTime=(System.currentTimeMillis()-startTime)/1000;
        System.out.println("rpc qps:"+invokeTime/costTime);
        stopped.set(true);
        executorService.shutdown();
        client.stop();
	}
}
