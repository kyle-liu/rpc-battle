package com.taobao.rpc.zaza;

import java.util.Map;

import org.slf4j.LoggerFactory;

import com.taobao.rpc.api.RpcFactory;
import com.taobao.rpc.benchmark.service.HelloService;
import com.taobao.rpc.zaza.impl.ZazaInnerServiceImpl;
import com.taobao.rpc.zaza.impl.grizzly.GrizzlyClientFactory;
import com.taobao.rpc.zaza.impl.grizzly.GrizzlyZazaServer;
import com.taobao.rpc.zaza.interfaces.ZazaInnerService;
import com.taobao.rpc.zaza.interfaces.ZazaServer;
import com.taobao.rpc.zaza.model.ZazaMethodDataModel;
import com.taobao.rpc.zaza.proxy.ZazaProxyFactory;
import com.taobao.rpc.zaza.util.ZazaConfigUtil;

public class ZazaRpcFactory implements RpcFactory {
    private final static org.slf4j.Logger logger = LoggerFactory.getLogger(ZazaRpcFactory.class);

    public static final int CLIENT_NUM = ZazaConfigUtil.getClentNum();
    private static final int CLIENT_THREAD_NUM = ZazaConfigUtil.getClentThreadNum();
    public static final String ZAZA_METHOD_CODES = "com.taobao.rpc.zaza.methodcode";
    public static final String ZAZA_GROUP = "zaza";

    public <T> void export(Class<T> type, T serviceObject) {
        ZazaMethodDataModel.instance.initClassCodes("com.taobao.rpc.benchmark.dataobject");
        ZazaServer zazaServer = new GrizzlyZazaServer();
        zazaServer.register(ZazaInnerService.class, new ZazaInnerServiceImpl());
        zazaServer.register(type, serviceObject);
    }

    @SuppressWarnings("unchecked")
    public <T> T getReference(Class<T> type, String ip) {
        try {
            ZazaMethodDataModel.instance.initClassCodes("com.taobao.rpc.benchmark.dataobject");
            GrizzlyClientFactory.getInstance().init(ip);
            ZazaMethodDataModel.instance.initMethodCodesOfClientSide((Map<String, Byte>) GrizzlyClientFactory
                    .getInstance().get().invokeSyncIntern(new ZazaRequest((byte) 0, null, null), 1000));
            warmupBeforeInvoke();
            return ZazaProxyFactory.createJavassistBytecodeDynamicProxy(type);
        } catch (Exception e) {
            logger.error("[getReference error]", e);
            throw new RuntimeException(e);
        }
    }

    private void warmupBeforeInvoke() throws Exception {
        HelloService helloService = ZazaProxyFactory.createJavassistBytecodeDynamicProxy(HelloService.class);
        for (int i = 0; i < 100; i++) {
            helloService.helloPerson(ZazaMethodDataModel.person);
        }
    }

    public int getClientThreads() {
        return CLIENT_THREAD_NUM;
    }

    public String getAuthorId() {
        return "kongming";
    }

}
