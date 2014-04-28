///**
// * xiaodu-rpc
// */
//package com.taobao.rpc_battle.rpc_udp.netty;
//
//import java.net.InetSocketAddress;
//import java.net.SocketAddress;
//import java.nio.ByteOrder;
//import java.util.concurrent.Executors;
//
//import org.jboss.netty.bootstrap.ConnectionlessBootstrap;
//import org.jboss.netty.buffer.ChannelBuffer;
//import org.jboss.netty.buffer.ChannelBuffers;
//import org.jboss.netty.channel.ChannelHandlerContext;
//import org.jboss.netty.channel.ChannelPipeline;
//import org.jboss.netty.channel.ChannelPipelineFactory;
//import org.jboss.netty.channel.Channels;
//import org.jboss.netty.channel.ExceptionEvent;
//import org.jboss.netty.channel.FixedReceiveBufferSizePredictorFactory;
//import org.jboss.netty.channel.MessageEvent;
//import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
//import org.jboss.netty.channel.socket.DatagramChannel;
//import org.jboss.netty.channel.socket.DatagramChannelFactory;
//import org.jboss.netty.channel.socket.nio.NioDatagramChannelFactory;
//import org.jboss.netty.handler.codec.string.StringDecoder;
//import org.jboss.netty.handler.codec.string.StringEncoder;
//import org.jboss.netty.util.CharsetUtil;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import com.taobao.rpc.api.RpcFactory;
//import com.taobao.rpc_battle.rpc_udp.DatagramPacketUtil;
//
///**
// * @author xiaodu
// * 
// *         下午5:05:35
// */
//public class UdpNettyClient {
//	
//	InetSocketAddress inetSocketAddress = new InetSocketAddress("127.0.0.1", RpcFactory.DEFAULT_PORT);
//	private DatagramChannel datagramChannel = null;
//	
//	protected final static Logger log = LoggerFactory.getLogger(UdpNettyClient.class);
//	public class UdpNettyEventHandler extends SimpleChannelUpstreamHandler{
//		@Override
//	     public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
//		             throws Exception {
//		         	ChannelBuffer buffer = (ChannelBuffer)e.getMessage();
//		         
//		     }
//		 
//		     @Override
//		     public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
//		             throws Exception {
//		     }
//
//	}
//	
//	
//	
//	
//	public void sendRequest(byte[] a){
//		datagramChannel.write(ChannelBuffers.wrappedBuffer(ByteOrder.LITTLE_ENDIAN, a), inetSocketAddress);
//		
//	}
//
//	public UdpNettyClient(String ip) {
//		try {
//
//			DatagramChannelFactory datagramChannelFactory = new NioDatagramChannelFactory(Executors.newCachedThreadPool());
//			ConnectionlessBootstrap connectionlessBootstrap = new ConnectionlessBootstrap(datagramChannelFactory);
//			connectionlessBootstrap.setPipelineFactory(new ChannelPipelineFactory() {
//				public ChannelPipeline getPipeline() throws Exception {
//					return Channels.pipeline(new UdpNettyEventHandler());
//				}
//
//			});
//
//			connectionlessBootstrap.setOption("broadcast", "true");
//			
//			connectionlessBootstrap.setOption("receiveBufferSizePredictorFactory", new FixedReceiveBufferSizePredictorFactory(1024));
//			datagramChannel = (DatagramChannel) connectionlessBootstrap.connect(inetSocketAddress).getChannel();
//
//			inetSocketAddress = new InetSocketAddress(ip, RpcFactory.DEFAULT_PORT);
//		} catch (Exception e) {
//
//			log.error(e.getMessage(), e);
//
//
//		}
//
//
//	}
//
//	public static void main(String[] args) {
//		UdpNettyClient client = new UdpNettyClient("127.0.0.1");
//		client.sendRequest(new byte[80000]);
//	}
//}
