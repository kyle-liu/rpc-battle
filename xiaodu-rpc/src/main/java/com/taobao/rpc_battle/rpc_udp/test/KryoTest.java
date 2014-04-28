
/**
 * xiaodu-rpc
 */
package com.taobao.rpc_battle.rpc_udp.test;

import java.io.ByteArrayOutputStream;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;
import com.taobao.rpc_battle.rpc_udp.UdpReqPacket;

/**
 * @author xiaodu
 *
 * 下午3:22:38
 */
public class KryoTest {

	/**
	 *@author xiaodu
	 * @param args
	 *TODO
	 */
	public static void main(String[] args) {
		
		 Kryo kryo = new  Kryo();
		 kryo.setRegistrationRequired(true);
		 
		 kryo.register(UdpReqPacket.class);
		 kryo.register(String.class);
		 kryo.register(String.class);
		 kryo.register(String.class);
		 kryo.register(String.class);
		 kryo.register(String.class);
		 kryo.register(String.class);
		 kryo.register(String.class);
		 kryo.register(String.class);
		 
		 
		 
		 
			UdpReqPacket up = new UdpReqPacket();
			up.setRpcMethodName("asdfasdfsdaf");
			up.setParams(args);
			
			Output out = new Output(new ByteArrayOutputStream() );
			kryo.writeObject(out, up);
			
			
			System.out.println(out.getBuffer().length);
	}

}
