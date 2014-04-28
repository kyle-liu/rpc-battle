package com.taobao.rpc.remoting.command;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.taobao.gecko.core.command.ResponseCommand;
import com.taobao.gecko.core.command.ResponseStatus;
import com.taobao.gecko.service.Connection;
import com.taobao.gecko.service.SingleRequestCallBackListener;
import com.taobao.gecko.service.exception.NotifyRemotingException;


/**
 * 用于阻塞等待消息发送结果的Listener。
 * 
 * <p>
 * 它实现SingleRequestCallBackListener接口以提供CALLBACK模式， 同时又提供阻塞等待响应的方法，实现了FUTURE模式。
 * 
 * @author JIUREN
 */
public class BlockingGetResponseListener implements SingleRequestCallBackListener {

    /**
     * 阻塞等待发送响应。
     */
    public ResponseCommand waitForResponse() throws InterruptedException, TimeoutException, NotifyRemotingException,
            Exception {
        synchronized (this) {
            while (null == response) {
                wait();
            }
        }

        final Object maybeResponse = response;

        // 发送消息错误的语义对应有两种实现方式：异常和ResponseCommand
        if (maybeResponse instanceof TimeoutException) {
            throw new TimeoutException();
        }

        if (maybeResponse instanceof NotifyRemotingException) {
            throw (NotifyRemotingException) maybeResponse;
        }

        if (maybeResponse instanceof Exception) {
            throw (Exception) maybeResponse;
        }

        ResponseCommand resp = (ResponseCommand) maybeResponse;
        ResponseStatus respStatus = resp.getResponseStatus();

        if (ResponseStatus.TIMEOUT == respStatus) {
            throw new TimeoutException();
        }

        if (ResponseStatus.NO_ERROR != respStatus) {
            if (resp instanceof RpcBooleanAckCommand) {
                String errorMsg = ((RpcBooleanAckCommand) resp).getErrorMsg();
                throw new NotifyRemotingException(errorMsg);
            }
        }

        return resp;
    }


    /**
     * 发送消息错误的语义对应有两种实现方式：异常和ResponseCommand
     */
    public void onResponse(ResponseCommand responseCommand, Connection conn) {
        synchronized (this) {
            response = responseCommand;
            notifyAll();
        }
    }


    /**
     * 发送消息错误的语义对应有两种实现方式：异常和ResponseCommand
     */
    public void onException(Exception e) {
        synchronized (this) {
            response = e;
            notifyAll();
        }
    }


    public ThreadPoolExecutor getExecutor() {
        return diyExecutor;
    }

    /**
     * 由请求线程自己执行任务的线程池。
     */
    static class DIYExecutor extends ThreadPoolExecutor {
        DIYExecutor() {
            super(0, 1, 0, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(1));
        }


        @Override
        public void execute(Runnable task) {
            if (null != task) {
                task.run();
            }
        }
    }

    // ====================

    static ThreadPoolExecutor diyExecutor = new DIYExecutor();

    private volatile Object response = null;

}
