
/**
 * xiaodu-rpc
 */
package com.taobao.rpc_battle.rcl.server;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.springframework.remoting.caucho.HessianServiceExporter;

import com.taobao.rpc.api.RpcException;
import com.taobao.rpc.api.RpcFactory;
import com.taobao.rpc.benchmark.service.HelloService;
import com.taobao.rpc.benchmark.service.HelloServiceImpl;
import com.taobao.rpc_battle.rcl.client.RemoteResourceLoader;

/**
 * @author xiaodu
 *
 * ÉÏÎç10:09:16
 */
public class RclMainServer {
	
	private RemoteResourceLoaderImpl impl = new RemoteResourceLoaderImpl();
	
	public  RclMainServer(Class type, Object serviceObject){
		impl.registClass(type, serviceObject);
	}
	
	
	public void startup(){
//		 EventLoop loop = EventLoop.defaultEventLoop();
//	        try {
//	            Server svr = new Server();
//	            svr.serve(impl);
//	            svr.listen(RpcFactory.DEFAULT_PORT);
//	            loop.join();
//	        } catch (Exception e) {
//	            throw new RpcException(e);
//	        }
		
	        try {
	            org.eclipse.jetty.server.Server server = new Server(RpcFactory.DEFAULT_PORT);
	            ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
	            context.setContextPath("/");
	            server.setHandler(context);

	            HessianServiceExporter exporter = new HessianServiceExporter();
	            exporter.setServiceInterface(RemoteResourceLoader.class);
	            exporter.setService(impl);
	            exporter.afterPropertiesSet();

	            context.addServlet(new ServletHolder(new HessianServlet(exporter)), "/HessianService");

	            server.start();
	            server.join();
	        } catch (Exception e) {
	            throw new RpcException(e);
	        }
	        
		
	}
	
	
	public static void main(String[] args) {
		
		RclMainServer rcl = new RclMainServer(HelloService.class,new HelloServiceImpl());
		rcl.startup();
	}



}
