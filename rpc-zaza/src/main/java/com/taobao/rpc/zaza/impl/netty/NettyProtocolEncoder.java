package com.taobao.rpc.zaza.impl.netty;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;

public class NettyProtocolEncoder extends OneToOneEncoder {
	
	protected Object encode(ChannelHandlerContext ctx, Channel channel,Object message) throws Exception {
	    return NettyZazaProtocol.encode(message);
	}

}
