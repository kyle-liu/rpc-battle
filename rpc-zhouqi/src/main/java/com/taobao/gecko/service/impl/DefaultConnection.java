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
package com.taobao.gecko.service.impl;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.taobao.gecko.core.buffer.IoBuffer;
import com.taobao.gecko.core.command.CommandHeader;
import com.taobao.gecko.core.command.Constants;
import com.taobao.gecko.core.command.RequestCommand;
import com.taobao.gecko.core.command.ResponseCommand;
import com.taobao.gecko.core.command.ResponseStatus;
import com.taobao.gecko.core.command.kernel.BooleanAckCommand;
import com.taobao.gecko.core.nio.NioSession;
import com.taobao.gecko.core.nio.impl.TimerRef;
import com.taobao.gecko.core.util.ConcurrentHashSet;
import com.taobao.gecko.core.util.RemotingUtils;
import com.taobao.gecko.service.Connection;
import com.taobao.gecko.service.RemotingContext;
import com.taobao.gecko.service.SingleRequestCallBackListener;
import com.taobao.gecko.service.callback.SingleRequestCallBack;
import com.taobao.gecko.service.exception.NotifyRemotingException;


/**
 * 
 * ���ӵķ�װ
 * 
 * @author boyan
 * 
 * @since 1.0, 2009-12-15 ����02:47:00
 */

public class DefaultConnection implements Connection {
    static final Log log = LogFactory.getLog(DefaultConnection.class);

    /**
     * �Ƿ����ÿ��ж�д��������ã���ô�Ϳ������û��߳���IOд������������û��̵߳��жϻ��������ӶϿ���������ʹ�á�Ĭ�ϲ����á�
     */
    private boolean writeInterruptibly = true;

    /**
     * ����������ĳ�ʱ����
     * 
     * 
     * 
     * @author boyan
     * 
     * @since 1.0, 2009-12-18 ����03:00:55
     */
    private static final class SingleRequestCallBackRunner implements Runnable {
        final SingleRequestCallBack requestCallBack;
        final Connection connection;


        public SingleRequestCallBackRunner(final SingleRequestCallBack requestCallBack, final Connection connection) {
            super();
            this.requestCallBack = requestCallBack;
            this.connection = connection;
        }


        public void run() {
            // ֪ͨtimeout
            final DefaultConnection defaultConnection = (DefaultConnection) SingleRequestCallBackRunner.this.connection;
            final BooleanAckCommand timeoutCommand =
                    defaultConnection.createTimeoutCommand(
                        SingleRequestCallBackRunner.this.requestCallBack.getRequestCommandHeader(),
                        SingleRequestCallBackRunner.this.connection.getRemoteSocketAddress());
            this.requestCallBack.cancelWrite(this.connection);
            this.requestCallBack.onResponse(null, timeoutCommand, this.connection);
        }
    }


    private BooleanAckCommand createTimeoutCommand(final CommandHeader header, final InetSocketAddress address) {
        final BooleanAckCommand value =
                this.remotingContext.getCommandFactory().createBooleanAckCommand(header, ResponseStatus.TIMEOUT,
                    "�ȴ���Ӧ��ʱ");
        value.setResponseStatus(ResponseStatus.TIMEOUT);
        value.setResponseTime(System.currentTimeMillis());
        value.setResponseHost(address);
        return value;
    }


    public void setWriteInterruptibly(final boolean writeInterruptibly) {
        this.writeInterruptibly = writeInterruptibly;
    }


    @Override
    public String toString() {
        return RemotingUtils.getAddrString(this.getRemoteSocketAddress());
    }

    /**
     * �Ƿ���������
     */
    private volatile boolean allowReconnect = true;

    /**
     * ������ͻ����ж������Ƿ�������Ӿ����ĺ�����ָRemotingClient.connect���ú����ӳɹ��������Ҽ�����ָ���ķ���
     */
    private volatile boolean ready;

    /**
     * �����������ķ��鼯��
     */
    private final ConcurrentHashSet<String> groupSet = new ConcurrentHashSet<String>();

    /**
     * Opaque��group��ӳ��,�����ڶ���鷢�ͣ����Ӧ�������ĸ�����
     */
    private final ConcurrentHashMap<Integer/* opaque */, String/* group */> opaque2group =
            new ConcurrentHashMap<Integer, String>(128);


    public boolean isConnected() {
        return !this.session.isClosed();
    }

    private final NioSession session;
    private final DefaultRemotingContext remotingContext;


    void addGroup(final String group) {
        this.groupSet.add(group);
    }


    void removeGroup(final String group) {
        this.groupSet.remove(group);
    }


    public Set<String> getGroupSet() {
        return new HashSet<String>(this.groupSet);
    }


    // �ͷ���Դ����callback��ʱ
    void dispose() {
        for (final Integer opaque : this.requestCallBackMap.keySet()) {
            final RequestCallBack requestCallBack = this.requestCallBackMap.get(opaque);
            // ��callBack��ʱ
            if (requestCallBack != null) {
                requestCallBack.onResponse(this.removeOpaqueToGroupMapping(opaque),
                    this.createTimeoutCommand(new CommandHeader() {
                        public int getOpaque() {
                            return opaque;
                        }
                    }, this.getRemoteSocketAddress()), this);
            }

        }
    }


    public ByteOrder readBufferOrder() {
        return this.session.getReadBufferByteOrder();
    }


    public void readBufferOrder(final ByteOrder byteOrder) {
        this.session.setReadBufferByteOrder(byteOrder);
    }


    /**
     * �Ƴ�������Ч������ص�
     */
    void removeAllInvalidRequestCallBack() {
        final Set<Integer> removedOpaqueSet = new HashSet<Integer>();
        final long now = System.currentTimeMillis();
        for (final Map.Entry<Integer, RequestCallBack> entry : this.requestCallBackMap.entrySet()) {
            if (entry.getValue().isInvalid(now)) {
                removedOpaqueSet.add(entry.getKey());
            }
        }
        int count = 0;
        for (final Integer opaque : removedOpaqueSet) {
            // �ٴ�ȷ��
            final RequestCallBack requestCallBack = this.requestCallBackMap.get(opaque);
            if (requestCallBack != null && requestCallBack.isInvalid(now)) {
                // ��callBack��ʱ
                requestCallBack.onResponse(this.removeOpaqueToGroupMapping(opaque),
                    this.createTimeoutCommand(new CommandHeader() {
                        public int getOpaque() {
                            return opaque;
                        }
                    }, this.getRemoteSocketAddress()), this);
                count++;
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("�Ƴ�" + count + "����Ч�ص�");
        }

    }


    boolean isAllowReconnect() {
        return this.allowReconnect;
    }


    void setAllowReconnect(final boolean allowReconnect) {
        this.allowReconnect = allowReconnect;
    }


    public boolean isReady() {
        return this.ready;
    }


    void setReady(final boolean ready) {
        this.ready = ready;
    }


    private void checkFlow() throws NotifyRemotingException {
        if (this.session.getScheduleWritenBytes() > this.remotingContext.getConfig().getMaxScheduleWrittenBytes()) {
            throw new NotifyRemotingException("������Ϣʧ�ܣ�������������["
                    + this.remotingContext.getConfig().getMaxScheduleWrittenBytes() + "�ֽ�]");
        }
    }


    public ResponseCommand invoke(final RequestCommand requestCommand, final long time, final TimeUnit timeUnit)
            throws InterruptedException, TimeoutException, NotifyRemotingException {
        if (requestCommand == null) {
            throw new NotifyRemotingException("Null message");
        }
        //this.checkFlow();
        final SingleRequestCallBack requestCallBack =
                new SingleRequestCallBack(requestCommand.getRequestHeader(), TimeUnit.MILLISECONDS.convert(time,
                    timeUnit));
        this.addRequestCallBack(requestCommand.getOpaque(), requestCallBack);
        try {
            requestCallBack.addWriteFuture(this, this.asyncWriteToSession(requestCommand));
        }
        catch (final Throwable t) {
            this.removeRequestCallBack(requestCommand.getOpaque());
            throw new NotifyRemotingException(t);
        }
        return requestCallBack.getResult(time, timeUnit, this);
    }

    private final ConcurrentHashMap<Integer, RequestCallBack> requestCallBackMap =
            new ConcurrentHashMap<Integer, RequestCallBack>();


    void addRequestCallBack(final Integer opaque, final RequestCallBack requestCallBack) throws NotifyRemotingException {
        if (!this.remotingContext.aquire()) {
            throw new NotifyRemotingException("������������CallBack����["
                    + this.remotingContext.getConfig().getMaxCallBackCount() + "]");
        }
        if (this.requestCallBackMap.containsKey(opaque)) {
            this.remotingContext.release();
            throw new NotifyRemotingException("�벻Ҫ�ظ�����ͬһ�����ͬһ������");
        }
        this.requestCallBackMap.put(opaque, requestCallBack);
    }


    public RequestCallBack getRequestCallBack(final Integer opaque) {
        return this.requestCallBackMap.get(opaque);
    }


    public RequestCallBack removeRequestCallBack(final Integer opaque) {
        final RequestCallBack removed = this.requestCallBackMap.remove(opaque);
        if (removed != null) {
            this.remotingContext.release();
        }
        return removed;
    }


    /**
     * �Ƴ�opaque��group��ӳ�䣬�����ڶ���鷢��
     * 
     * @param opaque
     * @return
     */
    public String removeOpaqueToGroupMapping(final Integer opaque) {
        return this.opaque2group.remove(opaque);
    }


    /**
     * ���opaque��group��ӳ�䣬�����ڶ���鷢��
     * 
     * @param opaque
     * @return
     */
    void addOpaqueToGroupMapping(final Integer opaque, final String group) {
        this.opaque2group.put(opaque, group);
    }


    public ResponseCommand invoke(final RequestCommand request) throws InterruptedException, TimeoutException,
            NotifyRemotingException {
        return this.invoke(request, 1000L, TimeUnit.MILLISECONDS);
    }


    public void send(final RequestCommand requestCommand, final SingleRequestCallBackListener listener)
            throws NotifyRemotingException {
        this.send(requestCommand, listener, 1000, TimeUnit.MILLISECONDS);
    }


    public void send(final RequestCommand requestCommand, final SingleRequestCallBackListener listener,
            final long time, final TimeUnit timeUnit) throws NotifyRemotingException {
        if (requestCommand == null) {
            throw new NotifyRemotingException("Null message");
        }
        if (listener == null) {
            throw new NotifyRemotingException("Null callback listener");
        }
        if (timeUnit == null) {
            throw new NotifyRemotingException("Null TimeUnit");
        }
        this.checkFlow();
        final long timeoutInMills = TimeUnit.MILLISECONDS.convert(time, timeUnit);
        final SingleRequestCallBack requestCallBack =
                new SingleRequestCallBack(requestCommand.getRequestHeader(), timeoutInMills, listener);
        final TimerRef timerRef = new TimerRef(timeoutInMills, new SingleRequestCallBackRunner(requestCallBack, this));
        requestCallBack.setTimerRef(timerRef);
        this.addRequestCallBack(requestCommand.getOpaque(), requestCallBack);
        try {
            requestCallBack.addWriteFuture(this, this.asyncWriteToSession(requestCommand));
            this.session.insertTimer(timerRef);
        }
        catch (final Throwable t) {
            // �м��Ƴ�callBack
            this.removeRequestCallBack(requestCommand.getOpaque());
            throw new NotifyRemotingException(t);
        }

    }


    public void send(final RequestCommand requestCommand) throws NotifyRemotingException {
        if (requestCommand == null) {
            throw new NotifyRemotingException("Null message");
        }
        this.writeToSession(requestCommand);
    }


    public Future<Boolean> asyncSend(final RequestCommand requestCommand) throws NotifyRemotingException {
        if (requestCommand == null) {
            throw new NotifyRemotingException("Null message");
        }
        this.checkFlow();
        return this.asyncWriteToSession(requestCommand);
    }


    private Future<Boolean> asyncWriteToSession(final Object packet) {
        if (this.writeInterruptibly) {
            return this.session.asyncWriteInterruptibly(packet);
        }
        else {
            return this.session.asyncWrite(packet);
        }
    }


    public DefaultConnection(final NioSession ioSession, final DefaultRemotingContext remotingContext) {
        this.session = ioSession;
        this.remotingContext = remotingContext;
        // ����session����������
        this.session.setAttribute(Constants.CONNECTION_ATTR, this);
    }


    NioSession getSession() {
        return this.session;
    }


    public RemotingContext getRemotingContext() {
        return this.remotingContext;
    }


    public synchronized void close(final boolean allowReconnect) throws NotifyRemotingException {
        if (!this.isConnected()) {
            return;
        }
        this.setAllowReconnect(allowReconnect);
        try {
            this.session.close();
        }
        catch (final Exception e) {
            throw new NotifyRemotingException(e);
        }
    }


    public void clearAttributes() {
        this.session.clearAttributes();
    }


    public Object getAttribute(final String key) {
        return this.session.getAttribute(key);
    }


    public InetSocketAddress getRemoteSocketAddress() {
        return this.session.getRemoteSocketAddress();
    }


    public InetAddress getLocalAddress() {
        return this.session.getLocalAddress();
    }


    public void removeAttribute(final String key) {
        this.session.removeAttribute(key);
    }


    public void setAttribute(final String key, final Object value) {
        this.session.setAttribute(key, value);
    }


    public Set<String> attributeKeySet() {
        return this.session.attributeKeySet();
    }


    int getRequstCallBackCount() {
        return this.requestCallBackMap.size();
    }


    public void response(final Object responseCommand) throws NotifyRemotingException {
        this.checkFlow();
        this.writeToSession(responseCommand);
    }


    private void writeToSession(final Object packet) throws NotifyRemotingException {
        try {
            if (this.writeInterruptibly) {
                this.session.writeInterruptibly(packet);
            }
            else {
                this.session.write(packet);
            }
        }
        catch (final Throwable t) {
            throw new NotifyRemotingException(t);
        }
    }


    public Object setAttributeIfAbsent(final String key, final Object value) {
        return this.session.setAttributeIfAbsent(key, value);
    }


    public void transferFrom(final IoBuffer head, final IoBuffer tail, final FileChannel channel, final long position,
            final long size) {
        this.session.transferFrom(head, tail, channel, position, size);
    }


    public void transferFrom(final IoBuffer head, final IoBuffer tail, final FileChannel channel, final long position,
            final long size, final Integer opaque, final SingleRequestCallBackListener listener, final long time,
            final TimeUnit unit) throws NotifyRemotingException {
        if (channel == null) {
            throw new NotifyRemotingException("Null source channel");
        }
        if (listener == null) {
            throw new NotifyRemotingException("Null callback listener");
        }
        if (unit == null) {
            throw new NotifyRemotingException("Null TimeUnit");
        }
        this.checkFlow();
        final long timeoutInMills = TimeUnit.MILLISECONDS.convert(time, unit);
        final SingleRequestCallBack requestCallBack = new SingleRequestCallBack(new CommandHeader() {
            public int getOpaque() {
                return opaque;
            }
        }, timeoutInMills, listener);
        final TimerRef timerRef = new TimerRef(timeoutInMills, new SingleRequestCallBackRunner(requestCallBack, this));
        requestCallBack.setTimerRef(timerRef);
        this.addRequestCallBack(opaque, requestCallBack);
        try {
            requestCallBack.addWriteFuture(this, this.session.transferFrom(head, tail, channel, position, size));
            this.session.insertTimer(timerRef);
        }
        catch (final Throwable t) {
            // �м��Ƴ�callBack
            this.removeRequestCallBack(opaque);
            throw new NotifyRemotingException(t);
        }
    }


    public void notifyClientException(final RequestCommand requestCommand, final Exception e) {
        final RequestCallBack requestCallBack = this.getRequestCallBack(requestCommand.getOpaque());
        if (requestCallBack != null) {
            requestCallBack.setException(e, this, requestCommand);
        }
    }

}