package com.taobao.rpc.bishan.net.util;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * 
 * @author <a href="mailto:bishan.ct@taobao.com">bishan.ct</a>
 * @version 1.0
 * @since 2012-7-24
 */
public class NetWorkUtil {
	
	public static InetSocketAddress getAddrFromAdress(String address)  {
        URI uri;
		try {
			uri = new URI(address);
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException("URI格式错误",e);
		}
        return new InetSocketAddress(uri.getHost(), uri.getPort());
    }

}
