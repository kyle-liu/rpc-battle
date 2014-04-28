package com.taobao.shuihan.rpc;

import com.taobao.rpc.api.RpcFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author ding.lid
 */
public class Util {


    public static final String RPC_PROPERTIES = "rpc.properties";

    public static final String RPC_FACTORY_IMPL_KEY = "factory.impl";

    public static Properties properties;
    public static final Object lock = new Object();

    private static void initProperties() throws IOException {
        if(properties == null) {
            synchronized (lock) {

                if(properties == null) {

                    properties = new Properties();
                    InputStream is = ServerMain.class.getClassLoader().getResourceAsStream(RPC_PROPERTIES);
                    properties.load(is);
                    try {
                        is.close();
                    }
                    catch (Throwable t) {
                        // ignore
                    }
                }
            }
        }
    }

    public static RpcFactory getRpcFactoryImpl() throws IOException, ClassNotFoundException, IllegalAccessException, InstantiationException {
       return new ProtobufRpcFactory();
    }
}