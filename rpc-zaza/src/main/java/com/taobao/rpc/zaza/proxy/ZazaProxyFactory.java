package com.taobao.rpc.zaza.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;
import javassist.util.proxy.ProxyObject;

import com.taobao.rpc.zaza.bytecode.JavassistProxy;
import com.taobao.rpc.zaza.impl.grizzly.GrizzlyClientFactory;
import com.taobao.rpc.zaza.interfaces.ZazaClient;
import com.taobao.rpc.zaza.util.ZazaConfigUtil;
import com.taobao.rpc.zaza.util.ZazaUtil;

public class ZazaProxyFactory {

    @SuppressWarnings("unchecked")
    public static <T> T createJdkDynamicProxy(final Class<T> delegate) {
        try {
            T jdkProxy = (T) Proxy.newProxyInstance(ClassLoader.getSystemClassLoader(), new Class[] { delegate },
                    new JdkHandler(delegate));
            return jdkProxy;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static class JdkHandler implements InvocationHandler {
        final int timeOut = ZazaConfigUtil.getTimeOut();
        final Class<?> delegate;

        JdkHandler(Class<?> delegate) {
            this.delegate = delegate;
        }

        public Object invoke(Object object, Method method, Object[] objects) throws Throwable {
            ZazaClient client = GrizzlyClientFactory.getInstance().get();
            return client.invokeSync(delegate, method, ZazaUtil.getMethodParameter(objects), objects, timeOut);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T createJavassistDynamicProxy(final Class<T> delegate) throws Exception {
        ProxyFactory proxyFactory = new ProxyFactory();
        proxyFactory.setInterfaces(new Class[] { delegate });
        Class<?> proxyClass = proxyFactory.createClass();
        T javassistProxy = (T) proxyClass.newInstance();
        ((ProxyObject) javassistProxy).setHandler(new JavaAssitInterceptor(delegate));
        return javassistProxy;
    }

    private static class JavaAssitInterceptor implements MethodHandler {

        final Class<?> delegate;
        final int timeOut = ZazaConfigUtil.getTimeOut();

        JavaAssitInterceptor(Class<?> delegate) {
            this.delegate = delegate;
        }

        public Object invoke(Object self, Method method, Method proceed, Object[] args) throws Throwable {
            ZazaClient client = GrizzlyClientFactory.getInstance().get();
            return client.invokeSync(delegate, method, ZazaUtil.getMethodParameter(args), args, timeOut);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T createJavassistBytecodeDynamicProxy(Class<T> delegate) throws Exception {
        return (T) JavassistProxy.getProxy(delegate).newInstance(new JdkHandler(delegate));
    }
}
