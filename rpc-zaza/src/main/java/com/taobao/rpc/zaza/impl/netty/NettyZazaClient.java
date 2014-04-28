package com.taobao.rpc.zaza.impl.netty;

import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;

import com.taobao.rpc.zaza.ZazaRequest;
import com.taobao.rpc.zaza.interfaces.ZazaClient;

public class NettyZazaClient extends ZazaClient {
    private final ChannelFuture cf;

    public NettyZazaClient(ChannelFuture cf) {
        this.cf = cf;
    }

    @Override
    public void sendRequest(final ZazaRequest wrapper, final int timeout) throws Exception {
        ChannelFuture writeFuture = cf.getChannel().write(wrapper);
        // use listener to avoid wait for write & thread context switch
        writeFuture.addListener(new ChannelFutureListener() {
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    return;
                }
            }
        });
    }
}
