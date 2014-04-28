package com.taobao.rpc.bishan.net.reactor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;

/**
 * 只处理连接事件
 * 
 * @author bishan.ct
 *
 */
public class ReactorClient extends ReactorCore{

	public ReactorClient() throws IOException {
		super();
	}
	protected String getReactorName(){
		return "clientChannel";
	}
	@Override
	void handleKey(SelectionKey key,AbstractBsNet attachObj) {
		BsNetClient ch = (BsNetClient) attachObj;
        try {
        	//连接失败会抛出异常
			if (ch.socketChannel.finishConnect()) {
				key.cancel();
   
				ch.connectFuture.setDone();
			    ch.reactor.register(ch);
			}
		} catch (IOException e) {
			// TODO LOG
			ch.connectFuture.setException(e);
			ch.close();
		}
		
	}
	
	/**
	 * 注册通道connect事件
	 */
	public void register(final AbstractBsNet netConnect){
		final BsNetClient clientConnect=(BsNetClient)netConnect;
		tasks.offer(new Runnable() {
			
			@Override
			public void run() {
				try {
					//register read
					clientConnect.socketChannel.register(
							nioSelector,SelectionKey.OP_CONNECT, netConnect);
				} catch (ClosedChannelException e) {
					clientConnect.connectFuture.setException(e);
				}
			}
		});
	}
	
	public void connect(BsNetClient client,
			InetSocketAddress remoteAdr) throws Exception{
		
        if (client.socketChannel.connect(remoteAdr)) {
        	/**
    		 * 本地连接直接成功
    		 * 注册读事件
    		 */
        	client.reactor.register(client);
        	client.connectFuture.setDone();
        } else {
            this.register(client);
        }
	}

}
