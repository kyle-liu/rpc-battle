package com.taobao.rpc.bishan.net.reactor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

/**
 * 只处理接收请求事件
 * @author bishan.ct
 *
 */
public class ReactorServer extends ReactorCore {

	public ReactorServer() throws IOException {
		super();
		selectTime=500;
	}

	@Override
	void handleKey(SelectionKey key,AbstractBsNet attachObj) {
		BsNetServer server = (BsNetServer)attachObj;
		try{
			SocketChannel channel = server.serverSocketChannel.accept();

			BsNetClient bc = new BsNetClient(BsFactory.nextReactor(),
					channel,server.msgCallBack);
			bc.isServer=true;
			bc.reactor.register(bc);
		}catch(IOException e){
			server.close();
		}
	}
	
	public void register(AbstractBsNet netConnect,final int port){
		final BsNetServer serverConnect=(BsNetServer)netConnect;
		
		tasks.offer(new Runnable() {
			@Override
			public void run() {
				try {
					serverConnect.serverSocketChannel.socket().bind(new InetSocketAddress(port));
					serverConnect.serverFuture.setDone();
					
					//register accept
					serverConnect.serverSocketChannel.register(
							nioSelector,SelectionKey.OP_ACCEPT, serverConnect);
				}catch (IOException e) {
					serverConnect.serverFuture.setException(e);
				}
			}
		});
	}
	
	public void listen(BsNetServer server,int port){
		register(server,port);
	}
}
