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
 * ���������ȴ���Ϣ���ͽ����Listener��
 * 
 * <p>
 * ��ʵ��SingleRequestCallBackListener�ӿ����ṩCALLBACKģʽ�� ͬʱ���ṩ�����ȴ���Ӧ�ķ�����ʵ����FUTUREģʽ��
 * 
 * @author JIUREN
 */
public class BlockingGetResponseListener implements SingleRequestCallBackListener {

    /**
     * �����ȴ�������Ӧ��
     */
    public ResponseCommand waitForResponse() throws InterruptedException, TimeoutException, NotifyRemotingException,
            Exception {
        synchronized (this) {
            while (null == response) {
                wait();
            }
        }

        final Object maybeResponse = response;

        // ������Ϣ����������Ӧ������ʵ�ַ�ʽ���쳣��ResponseCommand
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
     * ������Ϣ����������Ӧ������ʵ�ַ�ʽ���쳣��ResponseCommand
     */
    public void onResponse(ResponseCommand responseCommand, Connection conn) {
        synchronized (this) {
            response = responseCommand;
            notifyAll();
        }
    }


    /**
     * ������Ϣ����������Ӧ������ʵ�ַ�ʽ���쳣��ResponseCommand
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
     * �������߳��Լ�ִ��������̳߳ء�
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
