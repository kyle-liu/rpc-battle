package com.taobao.rpc.bishan.net.common;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.taobao.rpc.bishan.net.reactor.BsNetListener;

/**
 * Future的默认实现，使用CountDownLatch
 */
public class DefaultFutureResult<E> implements BsFutureDone<E>,BsFutureResult<E>{
	public static final Logger log = Logger.getLogger(DefaultFutureResult.class);

    private BsCommonCallBack firstListener;
    private List<BsCommonCallBack> otherListeners;
    
    //doneͨ通过syn要求可见性，原子性
	private boolean done;

	private CountDownLatch cd=new CountDownLatch(1);
	private volatile Exception exception;
	private volatile E result;
	private long startTime;
    
	public DefaultFutureResult(){
		startTime=System.nanoTime();
	}
	
	
	public DefaultFutureResult(E  result){
		this();
		this.result=result;
	}


	@Override
	public E getAttachObj() throws Exception {
		return getResult();
	}
    
	/**
	 * 保证done的状态，这样只会notifyListener一次
	 */
	@Override
	public boolean setResult(E result) {
		this.result=result;
		if(!setDone())return false;

        notifyListeners();
		return true;
	}

	@Override
	public boolean setException(Exception exception) {
		this.exception=exception;
		if(!setDone())return false;

        notifyListeners();
		return true;
	}

	@Override
	public boolean setDone(){
		synchronized (this) {
			if(done)
				return false;
			done=true;
		}
		cd.countDown();
		notifyListeners();
		return true;
	}

	@Override
	public E getResultSyn(long time, TimeUnit timeUnit) throws Exception {
		if (this.exception != null) {
            throw new Exception("ͬget result exception", this.exception);
		}
		
		synchronized (this) {
			if(done)
				return result;
		}
		cd.await(time, timeUnit);
		
		return result;
	}
	
	@Override
	public void waitDone(long time, TimeUnit timeUnit) throws Exception {
		getResultSyn(time,timeUnit);
	}

	@Override
	public E getResult() throws Exception {
		if (this.exception != null) {
            throw new Exception("ͬget result exception", this.exception);
		}
		return result;
	}


	@Override
	public synchronized boolean isDone() {
		return done;
	}

	@Override
	public synchronized boolean isSuccess() {
		return isDone()&(exception==null);
	}

	@Override
	public Exception getException() {
		return exception;
	}

	public boolean addListener(BsCommonCallBack listener) {
        if (listener == null) {
            throw new NullPointerException("listener");
        }
        synchronized (this) {
            if (done) {
                return false;
            } else {
                if (firstListener == null) {
                    firstListener = listener;
                } else {
                    if (otherListeners == null) {
                        otherListeners = new ArrayList<BsCommonCallBack>(1);
                    }
                    otherListeners.add(listener);
                }
                return true;
            }
        }
    }

    public boolean removeListener(BsCommonCallBack listener) {
        if (listener == null) {
            throw new NullPointerException("listener");
        }

        synchronized (this) {
            if (!done) {
                if (listener == firstListener) {
                    if (otherListeners != null && !otherListeners.isEmpty()) {
                        firstListener = otherListeners.remove(0);
                    } else {
                        firstListener = null;
                    }
                } else if (otherListeners != null) {
                    otherListeners.remove(listener);
                }
                return true;
            }else{
            	return false;
            }
        }
    }
    private void notifyListeners() {
        // This method doesn't need synchronization because:
        // 1) This method is always called after synchronized (this) block.
        //    Hence any listener list modification happens-before this method.
        // 2) This method is called only when 'done' is true.  Once 'done'
        //    becomes true, the listener list is never modified - see add/removeListener()
        if (firstListener != null) {
            notifyListener(firstListener);
            firstListener = null;

            if (otherListeners != null) {
                for (BsCommonCallBack l: otherListeners) {
                    notifyListener(l);
                }
                otherListeners = null;
            }
        }
    }
    private void notifyListener(BsCommonCallBack l) {
        try {
        	if (this.exception != null) {
        		l.onException(exception);
        	}else{
        		l.onMsg(result);
        	}
        } catch (Throwable t) {
            if (log.isEnabledFor(Level.WARN)) {
            	log.warn(
                        "An exception was thrown by " +
                        BsNetListener.class.getSimpleName() + ".", t);
            }
        }
    }

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

}
