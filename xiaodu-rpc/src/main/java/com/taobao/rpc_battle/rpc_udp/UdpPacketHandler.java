
/**
 * xiaodu-rpc
 */
package com.taobao.rpc_battle.rpc_udp;

import java.net.SocketAddress;

/**
 * @author xiaodu
 *
 * 下午6:02:59
 */
public interface UdpPacketHandler {
	
	public void handler(byte[] packet,SocketAddress sendAddress);
	

}
