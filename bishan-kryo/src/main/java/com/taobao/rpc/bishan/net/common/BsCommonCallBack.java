package com.taobao.rpc.bishan.net.common;

import com.taobao.rpc.bishan.net.reactor.BsNetClient;

/**
 * 通用回调
 * @author bishan.ct
 */
public interface BsCommonCallBack {

	/**
     * 当响应到达的时，触发此方法
     * 
     */
    public void onMsg(Object responseResult);
    
    /**
     * 发生异常时，触发此方法
     * @return 正常情况下返回true，在已经有应答的情况下，返回false
     */
    public void onException(Exception cause);
    
}
