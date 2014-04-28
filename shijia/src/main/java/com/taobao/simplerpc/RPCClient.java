/**
 * $Id: RPCClient.java 864 2012-12-24 07:21:58Z shijia.wxr $
 */
package com.taobao.simplerpc;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;


/**
 * �ͻ��˽ӿ�
 * 
 * @author vintage.wang@gmail.com shijia.wxr@taobao.com
 */
public interface RPCClient {
    public void start();


    public void shutdown();


    public boolean connect(final InetSocketAddress remote, final int cnt);


    public ByteBuffer call(final byte[] req) throws InterruptedException;
}
