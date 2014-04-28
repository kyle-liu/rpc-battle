package com.taobao.rpc.bishan.net.common;

import java.util.concurrent.TimeUnit;

public interface BsFutureResult<E>  extends BsFuture<E>{
	/**
     * 当响应到达的时，触发此方法
     * 
     * @param responseCommand 应答命令
     * @param channel 通道
     * 
     * @return 正常情况下返回true，在已经有应答的情况下，返回false
     */
    public boolean setResult(E responseCommand);
    
    /**
     * 等待获取结果
     * @param time
     * @param timeUnit
     * @return
     * @throws NettyNetException 
     */
    public E getResultSyn(long time,TimeUnit timeUnit) 
    	throws Exception;
    
    /**
     * 获取结果，在没有返回或者发生异常的情况下，返回null
     * 
     * @return
     */
    public E getResult() throws Exception;
    
}
