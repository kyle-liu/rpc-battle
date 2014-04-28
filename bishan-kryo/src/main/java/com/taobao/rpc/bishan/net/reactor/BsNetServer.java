package com.taobao.rpc.bishan.net.reactor;

import java.io.IOException;
import java.nio.channels.ServerSocketChannel;

import com.taobao.rpc.bishan.net.common.BsFutureDone;
import com.taobao.rpc.bishan.net.common.DefaultFutureResult;


/**
 *
 * 
 * @author bishan.ct
 *
 */
public class BsNetServer extends AbstractBsNet{

	ServerSocketChannel serverSocketChannel;
	BsFutureDone<BsNetServer> serverFuture;
	
	public BsNetServer(BsNetListener msgCallBack) throws IOException{
		//TODO CONFIG
		serverSocketChannel = ServerSocketChannel.open();

		serverSocketChannel.configureBlocking(false);
		serverFuture=new DefaultFutureResult<BsNetServer>(this);
		this.msgCallBack=msgCallBack;
	}

	@Override
	protected void innerClose() {
		// TODO Auto-generated method stub
		try {
			serverSocketChannel.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
