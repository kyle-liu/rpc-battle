
/**
 * xiaodu-rpc
 */
package com.taobao.rpc_battle.rcl.client;

import org.springframework.remoting.caucho.HessianProxyFactoryBean;

import com.taobao.rpc.api.RpcFactory;
import com.taobao.rpc.benchmark.service.HelloService;

/**
 * @author xiaodu
 *
 * ����10:44:06
 */
public class RclMainClient {
	private HessianProxyFactoryBean proxy;
	private RemoteClassLoaderImpl remoteClassLoader = null;
	
	private RemoteResourceLoader remoteResource;
	
	public RclMainClient(String ip){
//	        rpcClient = null;
//	        try {
//	        	rpcClient = new Client(ip, RpcFactory.DEFAULT_PORT);
//	        } catch (UnknownHostException e) {
//	            e.printStackTrace();
//	        }
	        
			proxy = new HessianProxyFactoryBean();
	        proxy.setChunkedPost(true);
	        proxy.setServiceInterface(RemoteResourceLoader.class);
	        proxy.setHessian2(true);
	        proxy.setServiceUrl("http://" + ip + ":"+RpcFactory.DEFAULT_PORT+"/HessianService");
	        proxy.afterPropertiesSet();
	        
	        
	        
	        remoteResource = RemoteResourceLoader.class.cast(proxy.getObject());
	        remoteClassLoader = new RemoteClassLoaderImpl(remoteResource);
	        
	        Thread.currentThread().setContextClassLoader(remoteClassLoader);
	        
		
	}
	
	public <T> T getRpcImpl(Class<T> className) throws InstantiationException, IllegalAccessException{
		
		try {
			return className.cast(remoteClassLoader.findClass(className.getCanonicalName()).newInstance());
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	
	public static void main(String[] args) {
		
		RclMainClient client = new RclMainClient("127.0.0.1");
		try {
			HelloService hello = client.getRpcImpl(HelloService.class);
			
			hello.helloWorld("1");
			
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	

}
