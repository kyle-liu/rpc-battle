/*
 * (C) 2007-2012 Alibaba Group Holding Limited.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.taobao.gecko.service.callback;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.taobao.gecko.core.command.RequestCommand;
import com.taobao.gecko.core.command.ResponseCommand;
import com.taobao.gecko.core.command.ResponseStatus;
import com.taobao.gecko.core.command.kernel.BooleanAckCommand;
import com.taobao.gecko.core.nio.impl.TimerRef;
import com.taobao.gecko.service.Connection;
import com.taobao.gecko.service.impl.DefaultConnection;
import com.taobao.gecko.service.impl.RequestCallBack;


/**
 * 
 * �ص�����
 * 
 * @author boyan
 * 
 * @since 1.0, 2009-12-18 ����04:09:25
 */

public abstract class AbstractRequestCallBack implements RequestCallBack {
    private final long timeout; // ��ʱʱ�䣬��λ����
    private final long timestamp; // ������ʱ���
    private TimerRef timerRef; // ��ʱ������
    private final CountDownLatch countDownLatch; // �������

    private final ConcurrentHashMap<Connection, Future<Boolean>> writeFutureMap =
            new ConcurrentHashMap<Connection, Future<Boolean>>();

    // ��ֹ�ظ���Ӧ����
    protected final Lock responseLock = new ReentrantLock();


    public AbstractRequestCallBack(final CountDownLatch countDownLatch, final long timeout, final long timestamp) {
        super();
        this.countDownLatch = countDownLatch;
        this.timeout = timeout;
        this.timestamp = timestamp;
    }


    public void cancelWrite(final Connection conn) {
        if (conn == null) {
            return;
        }
        final Future<Boolean> future = this.writeFutureMap.remove(conn);
        if (future != null) {
            future.cancel(false);
        }
    }


    public void addWriteFuture(final Connection conn, final Future<Boolean> future) {
        this.writeFutureMap.put(conn, future);
    }


    public void countDownLatch() {
        this.responseLock.lock();
        try {
            this.countDownLatch.countDown();
        }
        finally {
            this.responseLock.unlock();
        }
    }


    public boolean await(final long timeout, final TimeUnit unit) throws InterruptedException {
        return this.countDownLatch.await(timeout, unit);
    }


    /**
     * ȡ����ʱ��
     */
    public void cancelTimer() {
        if (this.timerRef != null) {
            this.timerRef.cancel();
        }
    }


    public void setTimerRef(final TimerRef timerRef) {
        this.timerRef = timerRef;
    }


    public boolean isInvalid(final long now) {
        return this.timeout <= 0 || now - this.timestamp > this.timeout;
    }


    protected static final BooleanAckCommand createComunicationErrorResponseCommand(final Connection conn,
            final Exception e, final RequestCommand requestCommand, final InetSocketAddress address) {
        final StringBuilder sb = new StringBuilder(e.getMessage());
        if (e.getCause() != null) {
            sb.append("\r\nRroot cause by:\r\n").append(e.getCause().getMessage());
        }
        final BooleanAckCommand value =
                conn.getRemotingContext()
                    .getCommandFactory()
                    .createBooleanAckCommand(requestCommand.getRequestHeader(), ResponseStatus.ERROR_COMM,
                        sb.toString());
        value.setResponseStatus(ResponseStatus.ERROR_COMM);
        value.setResponseTime(System.currentTimeMillis());
        value.setResponseHost(address);
        return value;
    }


    public void onResponse(final String group, final ResponseCommand responseCommand, final Connection connection) {
        if (responseCommand != null) {
            this.removeCallBackFromConnection(connection, responseCommand.getOpaque());
        }
        this.onResponse0(group, responseCommand, connection);
    }


    public abstract void onResponse0(String group, ResponseCommand responseCommand, Connection connection);


    public abstract void setException0(Exception e, Connection conn, RequestCommand requestCommand);


    public void setException(final Exception e, final Connection conn, final RequestCommand requestCommand) {
        if (requestCommand != null) {
            this.removeCallBackFromConnection(conn, requestCommand.getOpaque());
        }
        this.setException0(e, conn, requestCommand);
    }


    protected void removeCallBackFromConnection(final Connection conn, final Integer opaque) {
        if (conn != null) {
            ((DefaultConnection) conn).removeRequestCallBack(opaque);
        }
    }


    /**
     * �����Ƿ����
     * 
     * @return
     */
    public abstract boolean isComplete();


    /**
     * ����������
     */
    public abstract void complete();


    /**
     * �����������
     * 
     * @return
     */
    public boolean tryComplete() {
        this.responseLock.lock();
        try {
            // �Ѿ����
            if (this.isComplete()) {
                return false;
            }
            // �������㣬�������
            if (this.countDownLatch.getCount() == 0) {
                // ������
                this.complete();
                // ȡ����ʱ��
                this.cancelTimer();
                return true;
            }
            return false;
        }
        finally {
            this.responseLock.unlock();
        }
    }


    public void dispose() {
        this.writeFutureMap.clear();
        if (this.timerRef != null) {
            this.timerRef.cancel();
        }
    }


    public long getTimestamp() {
        return this.timestamp;
    }

}