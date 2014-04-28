package com.taobao.rpc.zaza.impl.netty;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.DefaultChannelPipeline;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;

public class NettyClientPipelineFactory implements ChannelPipelineFactory {

    private SimpleChannelUpstreamHandler handler;

    public NettyClientPipelineFactory(SimpleChannelUpstreamHandler handler) {
        this.handler = handler;
    }

    public ChannelPipeline getPipeline() throws Exception {
        ChannelPipeline pipeline = new DefaultChannelPipeline();
        pipeline.addLast("decoder", new NettyProtocolDecoder());
        pipeline.addLast("encoder", new NettyProtocolEncoder());
        pipeline.addLast("handler", handler);
        return pipeline;
    }

}
