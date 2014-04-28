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

import java.net.InetSocketAddress;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.taobao.gecko.core.command.Constants;
import com.taobao.gecko.core.command.RequestCommand;
import com.taobao.gecko.core.command.ResponseCommand;
import com.taobao.gecko.core.command.ResponseStatus;
import com.taobao.gecko.core.command.kernel.HeartBeatRequestCommand;
import com.taobao.gecko.core.core.Handler;
import com.taobao.gecko.core.core.Session;
import com.taobao.gecko.core.nio.NioSession;
import com.taobao.gecko.core.nio.impl.TimerRef;
import com.taobao.gecko.core.util.ExceptionMonitor;
import com.taobao.gecko.core.util.RemotingUtils;
import com.taobao.gecko.service.Connection;
import com.taobao.gecko.service.ConnectionLifeCycleListener;
import com.taobao.gecko.service.RemotingController;
import com.taobao.gecko.service.RemotingServer;
import com.taobao.gecko.service.RequestProcessor;
import com.taobao.gecko.service.SingleRequestCallBackListener;
import com.taobao.gecko.service.exception.IllegalMessageException;
import com.taobao.gecko.service.exception.NotifyRemotingException;


/**
 * 
 * ������ҵ������
 * 
 * @author boyan
 * 
 * @since 1.0, 2009-12-15 ����11:14:51
 */

public class GeckoHandler implements Handler {

    /**
     * ���������������װ
     * 
     * @author boyan
     * 
     */
    private static final class ProcessorRunner<T extends RequestCommand> implements Runnable {
        private final DefaultConnection defaultConnection;
        private final RequestProcessor<T> processor;
        private final T message;


        private ProcessorRunner(final DefaultConnection defaultConnection, final RequestProcessor<T> processor,
                final T message) {
            this.defaultConnection = defaultConnection;
            this.processor = processor;
            this.message = message;
        }


        public void run() {
            this.processor.handleRequest(this.message, this.defaultConnection);
        }
    }

    /**
     * ����������첽������
     * 
     * @author boyan
     * 
     */
    private final static class HeartBeatListener implements SingleRequestCallBackListener {
        private final Connection conn;
        static final String HEARBEAT_FAIL_COUNT = "connection_heartbeat_fail_count";


        public ThreadPoolExecutor getExecutor() {
            return null;
        }


        private HeartBeatListener(final Connection conn) {
            this.conn = conn;
        }


        public void onException(final Exception e) {
            this.innerCloseConnection(this.conn);
        }


        public void onResponse(final ResponseCommand responseCommand, final Connection conn) {
            if (responseCommand == null || responseCommand.getResponseStatus() != ResponseStatus.NO_ERROR) {
                Integer count = (Integer) this.conn.setAttributeIfAbsent(HEARBEAT_FAIL_COUNT, 1);
                if (count != null) {
                    count++;
                    if (count < 3) {
                        conn.setAttribute(HEARBEAT_FAIL_COUNT, count);
                    }
                    else {
                        this.innerCloseConnection(conn);
                    }
                }
            }
            else {
                this.conn.removeAttribute(HEARBEAT_FAIL_COUNT);
            }
        }


        private void innerCloseConnection(final Connection conn) {
            log.info("�������ʧ�ܣ��ر�����" + conn.getRemoteSocketAddress() + ",������Ϣ" + conn.getGroupSet());
            try {
                conn.close(true);
            }
            catch (final NotifyRemotingException e) {
                log.error("�ر�����ʧ��", e);
            }
        }
    }

    private final DefaultRemotingContext remotingContext;
    private final RemotingController remotingController;
    private ReconnectManager reconnectManager;
    private static final Log log = LogFactory.getLog(GeckoHandler.class);


    public void setReconnectManager(final ReconnectManager reconnectManager) {
        this.reconnectManager = reconnectManager;
    }


    private void responseThreadPoolBusy(final Session session, final Object msg,
            final DefaultConnection defaultConnection) {
        if (defaultConnection != null && msg instanceof RequestCommand) {
            try {
                defaultConnection.response(defaultConnection
                    .getRemotingContext()
                    .getCommandFactory()
                    .createBooleanAckCommand(((RequestCommand) msg).getRequestHeader(), ResponseStatus.THREADPOOL_BUSY,
                        "�̳߳ط�æ"));
            }
            catch (final NotifyRemotingException e) {
                this.onExceptionCaught(session, e);
            }
        }
    }


    public GeckoHandler(final RemotingController remotingController) {
        this.remotingContext = (DefaultRemotingContext) remotingController.getRemotingContext();
        this.remotingController = remotingController;
    }


    public void onExceptionCaught(final Session session, final Throwable throwable) {
        if (throwable.getCause() != null) {
            ExceptionMonitor.getInstance().exceptionCaught(throwable.getCause());
        }
        else {
            ExceptionMonitor.getInstance().exceptionCaught(throwable);
        }
    }


    public void onMessageReceived(final Session session, final Object message) {
        final DefaultConnection defaultConnection = this.remotingContext.getConnectionBySession((NioSession) session);
        if (defaultConnection == null) {
            log.error("Connection[" + RemotingUtils.getAddrString(session.getRemoteSocketAddress()) + "]�Ѿ����رգ��޷�������Ϣ");
            session.close();
            return;
        }
        if (message instanceof RequestCommand) {
            this.processRequest(session, message, defaultConnection);
        }
        else if (message instanceof ResponseCommand) {
            this.processResponse(message, defaultConnection);
        }
        else {
            throw new IllegalMessageException("δ֪����Ϣ����" + message);
        }

    }


    private void processResponse(final Object message, final DefaultConnection defaultConnection) {
        final ResponseCommand responseCommand = (ResponseCommand) message;
        responseCommand.setResponseHost(defaultConnection.getRemoteSocketAddress());
        responseCommand.setResponseTime(System.currentTimeMillis());
        final RequestCallBack requestCallBack = defaultConnection.getRequestCallBack(responseCommand.getOpaque());
        if (requestCallBack != null) {
            requestCallBack.onResponse(null, responseCommand, defaultConnection);
        }
    }


    @SuppressWarnings("unchecked")
    private <T extends RequestCommand> void processRequest(final Session session, final Object message,
            final DefaultConnection defaultConnection) {
        final RequestProcessor<T> processor = this.getProcessorByMessage(message);
        if (processor == null) {
            log.error("δ�ҵ�" + message.getClass().getCanonicalName() + "��Ӧ�Ĵ�����");
            this.responseNoProcessor(session, message, defaultConnection);
            return;
        }
        else {
            this.executeProcessor(session, (T) message, defaultConnection, processor);
        }
    }


    @SuppressWarnings("unchecked")
    private <T extends RequestCommand> RequestProcessor<T> getProcessorByMessage(final Object message) {
        final RequestProcessor<T> processor;
        if (message instanceof HeartBeatRequestCommand) {
            processor = (RequestProcessor<T>) this.remotingContext.processorMap.get(HeartBeatRequestCommand.class);
        }
        else {
            processor = (RequestProcessor<T>) this.remotingContext.processorMap.get(message.getClass());
        }
        return processor;
    }


    /**
     * ִ��ʵ�ʵ�Processor
     * 
     * @param session
     * @param message
     * @param defaultConnection
     * @param processor
     */
    private <T extends RequestCommand> void executeProcessor(final Session session, final T message,
            final DefaultConnection defaultConnection, final RequestProcessor<T> processor) {
        if (processor.getExecutor() == null) {
            processor.handleRequest(message, defaultConnection);
        }
        else {
            try {
                processor.getExecutor().execute(new ProcessorRunner<T>(defaultConnection, processor, message));
            }
            catch (final RejectedExecutionException e) {
                this.responseThreadPoolBusy(session, message, defaultConnection);
            }
        }
    }


    private void responseNoProcessor(final Session session, final Object message,
            final DefaultConnection defaultConnection) {
    	int i=1;
    	if(i==1)return;
        if (defaultConnection != null && message instanceof RequestCommand) {
            try {
                defaultConnection.response(defaultConnection
                    .getRemotingContext()
                    .getCommandFactory()
                    .createBooleanAckCommand(((RequestCommand) message).getRequestHeader(),
                        ResponseStatus.NO_PROCESSOR, "δע����������������������Ϊ" + message.getClass().getCanonicalName()));
            }
            catch (final NotifyRemotingException e) {
                this.onExceptionCaught(session, e);
            }
        }
    }


    public void onMessageSent(final Session session, final Object msg) {

    }


    public void onSessionClosed(final Session session) {
        final InetSocketAddress remoteSocketAddress = session.getRemoteSocketAddress();
        final DefaultConnection conn = this.remotingContext.getConnectionBySession((NioSession) session);

        if (conn == null) {
            session.close();
            return;
        }

        log.debug("Զ������" + RemotingUtils.getAddrString(remoteSocketAddress) + "�Ͽ�,������Ϣ" + conn.getGroupSet());

        // ���������������ǿͻ��ˣ�������������
        if (conn.isAllowReconnect() && this.reconnectManager != null) {
            this.waitForReady(conn);
            this.addReconnectTask(remoteSocketAddress, conn);
        }
        // �ӷ������Ƴ�
        this.removeFromGroups(conn);
        // ����ʣ���callBack
        conn.dispose();
        // �Ƴ�session��connectionӳ��
        this.remotingContext.removeSession2ConnectionMapping((NioSession) session);
        this.adjustMaxScheduleWrittenBytes();
        this.remotingContext.notifyConnectionClosed(conn);
    }


    private void removeFromGroups(final DefaultConnection conn) {
        // �����з������Ƴ�
        for (final String group : conn.getGroupSet()) {
            this.remotingContext.removeConnectionFromGroup(group, conn);

        }
    }


    private void addReconnectTask(final InetSocketAddress remoteSocketAddress, final DefaultConnection conn) {
        // make a copy
        final Set<String> groupSet = conn.getGroupSet();
        log.info("Զ������" + RemotingUtils.getAddrString(remoteSocketAddress) + "�رգ�������������");
        // ���¼��
        synchronized (conn) {
            if (!groupSet.isEmpty() && !this.hasOnlyDefaultGroup(groupSet) && conn.isAllowReconnect()) {
                this.reconnectManager.addReconnectTask(new ReconnectTask(groupSet, remoteSocketAddress));
                // ���������������񣬷�ֹ�ظ�
                conn.setAllowReconnect(false);
            }
        }
    }


    private boolean hasOnlyDefaultGroup(final Set<String> groupSet) {
        return groupSet.size() == 1 && groupSet.contains(Constants.DEFAULT_GROUP);
    }


    private void waitForReady(final DefaultConnection conn) {
        /**
         * �˴���ͬ���������ȴ����Ӿ�������ֹ������ʱ����©������Ϣ
         */
        synchronized (conn) {
            int count = 0;
            while (!conn.isReady() && conn.isAllowReconnect() && count++ < 3) {
                try {
                    conn.wait(5000);
                }
                catch (final InterruptedException e) {
                    // �����ж�״̬���ϲ㴦��
                    Thread.currentThread().interrupt();
                }
            }
        }
    }


    @SuppressWarnings("unchecked")
    public void onSessionConnected(final Session session, final Object... args) {
        final Set<String> groupSet = (Set<String>) args[0];
        if (args.length >= 3) {
            final TimerRef timerRef = (TimerRef) args[2];
            if (timerRef != null) {
                timerRef.cancel();
            }
        }
        final DefaultConnection conn = this.remotingContext.getConnectionBySession((NioSession) session);
        try {
            // �����Ѿ����رգ�����groupSetΪ�գ���ر�session����������
            if (conn == null || groupSet.isEmpty()) {
                // ���ܹر���
                session.close();
                log.error("����������û�ж�Ӧ��connection");
            }
            else {
                this.addConnection2Group(conn, groupSet);
            }
        }
        finally {
            // һ��Ҫ֪ͨ����
            if (conn != null && conn.isConnected()) {
                this.notifyConnectionReady(conn);
            }
        }
    }


    private void addConnection2Group(final DefaultConnection conn, final Set<String> groupSet) {
        if (groupSet.isEmpty() || this.hasOnlyDefaultGroup(groupSet)) {
            this.closeConnectionWithoutReconnect(conn);
            return;
        }
        // �����������Ӽ������
        for (final String group : groupSet) {
            final Object attribute = this.remotingController.getAttribute(group, Constants.CONNECTION_COUNT_ATTR);
            if (attribute == null) {
                // û�з������������Ҳ���Ĭ�Ϸ��飬ǿ�ƹر�
                log.info("���ӱ�ǿ�ƶϿ������ڷ���" + group + "û�з������������");
                this.closeConnectionWithoutReconnect(conn);
                return;
            }
            else {
                final int maxConnCount = (Integer) attribute;
                /**
                 * �жϷ����������ͼ���������ͬһ��ͬ���飬��ֹ��������
                 */
                synchronized (this) {
                    // �������
                    if (this.remotingController.getConnectionCount(group) < maxConnCount) {
                        this.addConnectionToGroup(conn, group, maxConnCount);
                    }
                    else {
                        // �����Ƴ��Ͽ������ӣ��ٴμ���
                        if (this.removeDisconnectedConnection(group)) {
                            this.addConnectionToGroup(conn, group, maxConnCount);
                        }
                        else {
                            // ȷ���Ƕ���ģ��ر�
                            log.warn("������(" + conn.getRemoteSocketAddress() + ")�����趨ֵ" + maxConnCount + "�����ӽ����ر�");
                            this.closeConnectionWithoutReconnect(conn);
                        }
                    }
                }
            }
        }
    }


    private void closeConnectionWithoutReconnect(final DefaultConnection conn) {
        try {
            conn.close(false);
        }
        catch (final NotifyRemotingException e) {
            log.error("�ر�����ʧ��", e);
        }
    }


    private void notifyConnectionReady(final DefaultConnection conn) {
        // ֪ͨ�����Ѿ��������Ͽ����ӵ�ʱ���Զ��������Ӽ���÷���
        if (conn != null) {
            synchronized (conn) {
                conn.setReady(true);
                conn.notifyAll();
            }
            // ֪ͨ���������Ӿ���
            for (final ConnectionLifeCycleListener listener : this.remotingContext.connectionLifeCycleListenerList) {
                try {
                    listener.onConnectionReady(conn);
                }
                catch (final Throwable t) {
                    log.error("����ConnectionLifeCycleListener.onConnectionReady�쳣", t);
                }
            }
        }
    }


    private boolean removeDisconnectedConnection(final String group) {
        // ���������Ŀ���ƣ������������ӣ��Ƴ��Ͽ���connection������û�б���ʱ�Ƴ�)
        final List<Connection> currentConnList =
                this.remotingController.getRemotingContext().getConnectionsByGroup(group);
        Connection disconnectedConn = null;
        if (currentConnList != null) {

            synchronized (currentConnList) {
                final ListIterator<Connection> it = currentConnList.listIterator();
                while (it.hasNext()) {
                    final Connection currentConn = it.next();
                    if (!currentConn.isConnected()) {
                        disconnectedConn = currentConn;
                        break;
                    }
                    else {
                        // ��ǰ�������ӣ�ȷ���Ѿ��Ǿ���״̬������Ϊ�˷�ֹ���г�����
                        // ���ӽ����ɹ������ǳ����˹涨�ĳ�ʱʱ�䣬ȴ��Ȼ�������˷��飬û��֪ͨ����
                        if (!((DefaultConnection) currentConn).isReady() && !currentConn.getGroupSet().isEmpty()) {
                            this.notifyConnectionReady((DefaultConnection) currentConn);
                        }
                    }
                }
            }
        }
        if (disconnectedConn != null) {
            return currentConnList.remove(disconnectedConn);
        }
        else {
            return false;
        }
    }


    private void addConnectionToGroup(final DefaultConnection conn, final String group, final int maxConnCount) {
        conn.getRemotingContext().addConnectionToGroup(group, conn);
        // ��ȡ�������Ӿ�����
        final Object readyLock = this.remotingController.getAttribute(group, Constants.GROUP_CONNECTION_READY_LOCK);
        if (readyLock != null) {
            // ֪ͨ�����������Ӿ���
            synchronized (readyLock) {
                if (this.remotingController.getConnectionCount(group) >= maxConnCount) {
                    readyLock.notifyAll();
                }
            }
        }
    }


    public void onSessionCreated(final Session session) {
        log.debug("���ӽ�����Զ����Ϣ:" + RemotingUtils.getAddrString(session.getRemoteSocketAddress()));
        final DefaultConnection connection = new DefaultConnection((NioSession) session, this.remotingContext);
        // ����Ĭ�Ϸ���
        this.remotingContext.addConnection(connection);
        // ����session��connection��ӳ��
        this.remotingContext.addSession2ConnectionMapping((NioSession) session, connection);
        this.remotingContext.notifyConnectionCreated(connection);
        this.adjustMaxScheduleWrittenBytes();
    }


    private void adjustMaxScheduleWrittenBytes() {
        // Server�����������Զ������������������
        if (this.remotingController instanceof RemotingServer) {
            final List<Connection> connections = this.remotingContext.getConnectionsByGroup(Constants.DEFAULT_GROUP);
            final int connectionCount = connections != null ? connections.size() : 0;
            if (connectionCount > 0) {
                this.remotingContext.getConfig().setMaxScheduleWrittenBytes(
                    Runtime.getRuntime().maxMemory() / 3 / connectionCount);
            }
        }
    }


    public void onSessionExpired(final Session session) {

    }


    public void onSessionIdle(final Session session) {
        final Connection conn = this.remotingContext.getConnectionBySession((NioSession) session);
        try {
            conn.send(conn.getRemotingContext().getCommandFactory().createHeartBeatCommand(), new HeartBeatListener(
                conn), 5000, TimeUnit.MILLISECONDS);
        }
        catch (final NotifyRemotingException e) {
            log.error("������������ʧ��", e);
        }

    }


    public void onSessionStarted(final Session session) {

    }

}