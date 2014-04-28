package com.taobao.rpc.bishan.net.common;

import java.util.concurrent.TimeUnit;


/**
 * 异步调用后返回的结果，可以用户同步获取结果或者非阻塞调用
 * 两种Future：
 * 1.计算未知结果
 * 2.结果已知，只是得带某项操作完成
 * 
 * @author bishan.ct
 *
 * @param <E>
 */
public interface BsFutureDone<E> extends BsFuture<E>{

    /**
     * 当操作结束时，触发此方法
     * @return
     */
    public boolean setDone();
    
    /**
     * 
     * @param time
     * @param timeUnit
     * @throws Exception
     */
    public void waitDone(long time,TimeUnit timeUnit)throws Exception;;
  
    public E getAttachObj() throws Exception;
}
