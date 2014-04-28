package com.taobao.rpc.ri;

import com.taobao.rpc.api.RpcException;
import com.taobao.rpc.api.RpcFactory;
import org.springframework.remoting.rmi.RmiProxyFactoryBean;
import org.springframework.remoting.rmi.RmiServiceExporter;

import java.rmi.RemoteException;

/**
 * An simple implementation of {@link RpcFactory}.
 * <p>
 * Just wrap implementation to {@code Spring RMI}.
 *
 * @author ding.lid
 */
public class RiRpcFactory implements RpcFactory{
    @Override
    public <T> void export(Class<T> type, T serviceObject) {
        try {
            RmiServiceExporter exporter = new RmiServiceExporter();
            exporter.setServiceInterface(type);
            exporter.setService(serviceObject);
            exporter.setServiceName(type.getName());

            exporter.afterPropertiesSet();
        } catch (RemoteException e) {
            throw new RpcException(e);
        }
    }

    @Override
    public <T> T getReference(Class<T> type, String ip) {
        RmiProxyFactoryBean proxy = new RmiProxyFactoryBean();
        proxy.setServiceInterface(type);
        proxy.setServiceUrl("rmi://" + ip + "/" + type.getName());
        proxy.setCacheStub(true);
        proxy.setLookupStubOnStartup(true);
        proxy.setRefreshStubOnConnectFailure(true);
        proxy.afterPropertiesSet();
        return type.cast(proxy.getObject());
    }

    @Override
    public int getClientThreads() {
        return 10;
    }

    @Override
    public String getAuthorId() {
        return "Code4Fun(demo)";
    }
}
