package com.taobao.rpc.bishan.net.common;

import com.taobao.rpc.bishan.net.reactor.BsNetListener;

/**
 * Future语义的总接口，不过具体分两种类型：
 * 1.等待未知结果		BsFutureResult
 * 2.等待其他某项操作完成	BsFutureDone
 * 
 * @author bishan.ct
 */
public interface BsFuture<E> {
	  
    /**
     * 发生异常时，触发此方法
     * @return 正常情况下返回true，在已经有应答的情况下，返回false
     */
    public boolean setException(Exception cause);
	
    /**
     * 是否完成
     * @return
     */
    public boolean isDone();

    /**
     * 在完成，并且没有发生异常的情况下，返回true
     * @return
     */
    public boolean isSuccess();
    
    /**
     * 获得异常
     * @return 正常完成或者没有完成的情况下返回null
     */
    public Exception getException();
	
    boolean addListener(BsCommonCallBack listener);
    
    boolean removeListener(BsCommonCallBack listener);
	
}
