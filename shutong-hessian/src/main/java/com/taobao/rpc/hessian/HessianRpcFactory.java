package com.taobao.rpc.hessian;

import com.taobao.rpc.api.RpcException;
import com.taobao.rpc.api.RpcFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.springframework.remoting.caucho.HessianProxyFactoryBean;
import org.springframework.remoting.caucho.HessianServiceExporter;

import java.net.HttpURLConnection;

/**
 * An simple implementation of {@link RpcFactory}.
 * <p/>
 * Just wrap implementation to {@code Jetty/Spring/Hessian}.
 *
 * @author shutong.dy
 */
public class HessianRpcFactory implements RpcFactory {
    @Override
    public <T> void export(Class<T> type, T serviceObject) {
        try {
            Server server = new Server(8080);
            ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
            context.setContextPath("/");
            server.setHandler(context);

            HessianServiceExporter exporter = new HessianServiceExporter();
            exporter.setServiceInterface(type);
            exporter.setService(serviceObject);
            exporter.afterPropertiesSet();

            context.addServlet(new ServletHolder(new HessianServlet(exporter)), "/HessianService");

            server.start();
            server.join();
        } catch (Exception e) {
            throw new RpcException(e);
        }
    }

    @Override
    public <T> T getReference(Class<T> type, String ip) {
        HessianProxyFactoryBean proxy = new HessianProxyFactoryBean();
        proxy.setChunkedPost(true);
        proxy.setServiceInterface(type);
        proxy.setHessian2(true);
        proxy.setServiceUrl("http://" + ip + ":8080/HessianService");
        proxy.afterPropertiesSet();
        return type.cast(proxy.getObject());
    }

    @Override
    public int getClientThreads() {
        return 20;
    }

    @Override
    public String getAuthorId() {
        return "shutong-hessian";
    }
}
