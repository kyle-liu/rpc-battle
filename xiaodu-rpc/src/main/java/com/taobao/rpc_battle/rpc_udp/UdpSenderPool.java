
/**
 * xiaodu-rpc
 */
package com.taobao.rpc_battle.rpc_udp;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author xiaodu
 *
 * 下午1:30:07
 */
public class UdpSenderPool {
	
	static Map<String,UdpSender> threadMap = new ConcurrentHashMap<String, UdpSender>();
	
	public static UdpSender getThreadUdpSender(String ip){
		String currentThreadName = Thread.currentThread().getName()+"_"+ Thread.currentThread().getId();
		UdpSender sender = threadMap.get(currentThreadName);
		if(sender == null){
			synchronized (threadMap) {
				sender = threadMap.get(currentThreadName);
				if(sender == null){
					sender = new UdpSender(ip);
					threadMap.put(currentThreadName, sender);
				}
			}
		}
		return sender;
	}
	

}
