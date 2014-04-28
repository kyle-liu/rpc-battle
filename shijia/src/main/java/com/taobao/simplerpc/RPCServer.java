/**
 * $Id: RPCServer.java 839 2012-12-22 04:55:58Z shijia.wxr $
 */
package com.taobao.simplerpc;

/**
 * Ò»¸ö¼òµ¥RPC Server
 * 
 * @author vintage.wang@gmail.com  shijia.wxr@taobao.com
 */
public interface RPCServer {
    public void start();


    public void shutdown();


    public void registerProcessor(final RPCProcessor processor);
}
