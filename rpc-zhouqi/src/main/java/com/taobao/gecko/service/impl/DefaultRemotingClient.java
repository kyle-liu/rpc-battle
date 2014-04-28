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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.taobao.gecko.core.command.Constants;
import com.taobao.gecko.core.config.Configuration;
import com.taobao.gecko.core.extension.ConnectFailListener;
import com.taobao.gecko.core.extension.GeckoTCPConnectorController;
import com.taobao.gecko.core.nio.NioSession;
import com.taobao.gecko.core.nio.impl.SocketChannelController;
import com.taobao.gecko.core.nio.impl.TimerRef;
import com.taobao.gecko.core.util.RemotingUtils;
import com.taobao.gecko.core.util.StringUtils;
import com.taobao.gecko.service.Connection;
import com.taobao.gecko.service.RemotingClient;
import com.taobao.gecko.service.config.ClientConfig;
import com.taobao.gecko.service.exception.NotifyRemotingException;


/**
 * 
 * RemotingClient��Ĭ��ʵ��
 * 
 * @author boyan
 * 
 * @since 1.0, 2009-12-16 ����03:42:14
 */

public class DefaultRemotingClient extends BaseRemotingController implements RemotingClient, ConnectFailListener {

    private ReconnectManager reconnectManager;

    private static final Log log = LogFactory.getLog(DefaultRemotingClient.class);


    public DefaultRemotingClient(final ClientConfig clientConfig) {
        super(clientConfig);
        this.config = clientConfig;
        // Ĭ�Ϸ�����������������ΪInteger.MAX_VALUE
        this.setAttribute(Constants.DEFAULT_GROUP, Constants.CONNECTION_COUNT_ATTR, Integer.MAX_VALUE);

    }


    public void close(final String group, final boolean allowReconnect) throws NotifyRemotingException {
        if (!this.started) {
            throw new NotifyRemotingException("The controller has been stopped");
        }
        if (group == null) {
            throw new IllegalArgumentException("null group");
        }
        if (!allowReconnect) {
            // ȡ����������
            this.reconnectManager.cancelReconnectGroup(group);
            // ɾ������
            this.attributes.remove(group);
        }
        final List<Connection> connections = this.remotingContext.getConnectionsByGroup(group);
        if (connections != null) {
            for (final Connection conn : connections) {
                if (conn.isConnected()) {
                    conn.close(allowReconnect);
                }
            }
        }

    }


    /**
     * ������Ҫͬ������ֹ��ͬһ�����鷢��������
     */
    public synchronized void connect(String group, final int connCount) throws NotifyRemotingException {
        if (connCount <= 0) {
            throw new IllegalArgumentException("�Ƿ����������������0");
        }
        group = group.trim();
        if (this.isGroupConnectPending(group)) {
            return;
        }

        final InetSocketAddress remoteAddress = this.getSocketAddrFromGroup(group);

        final Set<String> groupSet = new HashSet<String>();
        groupSet.add(group);
        this.reconnectManager.removeCanceledGroup(group);
        // ��������������
        if (this.setAttributeIfAbsent(group, Constants.CONNECTION_COUNT_ATTR, connCount) != null) {
            return;
        }
        // �������Ӿ�����
        if (this.setAttributeIfAbsent(group, Constants.GROUP_CONNECTION_READY_LOCK, new Object()) != null) {
            return;
        }
        for (int i = 0; i < connCount; i++) {
            try {
                final TimerRef timerRef = new TimerRef(((ClientConfig) this.config).getConnectTimeout(), null);
                final Future<NioSession> future =
                        ((GeckoTCPConnectorController) this.controller).connect(remoteAddress, groupSet,
                            remoteAddress, timerRef);
                final CheckConnectFutureRunner runnable =
                        new CheckConnectFutureRunner(future, remoteAddress, groupSet, this);
                timerRef.setRunnable(runnable);
                this.insertTimer(timerRef);
            }
            catch (final Exception e) {
                log.error("����" + RemotingUtils.getAddrString(remoteAddress) + "ʧ��,������������", e);
                this.reconnectManager.addReconnectTask(new ReconnectTask(groupSet, remoteAddress));
            }
        }
    }


    /**
     * �жϷ����Ƿ������������
     * 
     * @param group
     * @return
     */
    private boolean isGroupConnectPending(final String group) {
        final Object readyLock = this.getAttribute(group, Constants.GROUP_CONNECTION_READY_LOCK);
        final Object attribute = this.getAttribute(group, Constants.CONNECTION_COUNT_ATTR);
        return readyLock != null && attribute != null;
    }


    public ReconnectManager getReconnectManager() {
        return this.reconnectManager;
    }

    /**
     * ������ӽ����Ƿ�ɹ�
     * 
     * 
     * 
     * @author boyan
     * 
     * @since 1.0, 2009-12-23 ����01:49:41
     */
    public static final class CheckConnectFutureRunner implements Runnable {
        final Future<NioSession> future;
        final InetSocketAddress remoteAddress;
        final Set<String> groupSet;
        final DefaultRemotingClient remotingClient;


        public CheckConnectFutureRunner(final Future<NioSession> future, final InetSocketAddress remoteAddress,
                final Set<String> groupSet, final DefaultRemotingClient remotingClient) {
            super();
            this.future = future;
            this.remoteAddress = remoteAddress;
            this.groupSet = groupSet;
            this.remotingClient = remotingClient;
        }


        public void run() {
            try {
                if (!this.future.isDone() && this.future.get(10, TimeUnit.MILLISECONDS) == null) {
                    final ReconnectManager reconnectManager = this.remotingClient.getReconnectManager();
                    reconnectManager.addReconnectTask(new ReconnectTask(this.groupSet, this.remoteAddress));
                }
            }
            catch (final Exception e) {
                log.error("����" + this.remoteAddress + "ʧ��", e);
            }
        }

    }


    private InetSocketAddress getSocketAddrFromGroup(String group) throws NotifyRemotingException {
        if (group == null) {
            throw new IllegalArgumentException("Null group");
        }
        group = group.trim();
        if (!group.startsWith(this.config.getWireFormatType().getScheme())) {
            throw new NotifyRemotingException("�Ƿ���Group��ʽ��û����" + this.config.getWireFormatType().getScheme() + "��ͷ");
        }
        try {
            final URI uri = new URI(group);
            return new InetSocketAddress(uri.getHost(), uri.getPort());
        }
        catch (final Exception e) {
            throw new NotifyRemotingException("��uri���ɷ�������ַ����,url=" + group, e);
        }
    }


    public void connect(final String group) throws NotifyRemotingException {
        this.connect(group, 1);

    }


    public void awaitReadyInterrupt(final String group) throws NotifyRemotingException, InterruptedException {
        final Object readyLock = this.getAttribute(group, Constants.GROUP_CONNECTION_READY_LOCK);
        final Object attribute = this.getAttribute(group, Constants.CONNECTION_COUNT_ATTR);
        if (readyLock == null || attribute == null) {
            throw new IllegalStateException("�Ƿ�״̬���㻹û�е���connect�����������Ӳ�����");
        }
        final long defaultConnectTimeout = ((ClientConfig) this.config).getConnectTimeout();
        this.awaitReadyInterrupt(group, defaultConnectTimeout * (Integer) attribute);
    }


    public void awaitReadyInterrupt(final String group, final long time) throws NotifyRemotingException,
            InterruptedException {
        if (StringUtils.isBlank(group)) {
            throw new IllegalArgumentException("Blank group");
        }
        // ��ȡ�������Ӿ�����
        final Object readyLock = this.getAttribute(group, Constants.GROUP_CONNECTION_READY_LOCK);
        final Object attribute = this.getAttribute(group, Constants.CONNECTION_COUNT_ATTR);
        if (readyLock == null || attribute == null) {
            throw new IllegalStateException("�Ƿ�״̬���㻹û�е���connect�����������Ӳ�����");
        }
        else {
            final int maxConnCount = (Integer) attribute;
            long totalTime = 0;
            synchronized (readyLock) {
                while (this.getConnectionCount(group) != maxConnCount) {
                    final long start = System.currentTimeMillis();
                    readyLock.wait(1000);
                    totalTime += System.currentTimeMillis() - start;
                    if (totalTime >= time) {
                        throw new NotifyRemotingException("�ȴ����Ӿ�����ʱ����ʱʱ��Ϊ" + time + "����");
                    }
                }
            }
        }

    }


    public InetSocketAddress getRemoteAddress(final String group) {
        if (this.remotingContext == null) {
            return null;
        }
        final List<Connection> connections = this.remotingContext.getConnectionsByGroup(group);
        if (connections == null || connections.size() == 0) {
            return null;
        }
        for (final Connection conn : connections) {
            if (conn.getRemoteSocketAddress() != null) {
                return conn.getRemoteSocketAddress();
            }
        }
        return null;
    }


    public String getRemoteAddressString(final String group) {
        return RemotingUtils.getAddrString(this.getRemoteAddress(group));
    }


    public boolean isConnected(final String group) {
        if (this.remotingContext == null) {
            return false;
        }
        final List<Connection> connections = this.remotingContext.getConnectionsByGroup(group);
        if (connections == null || connections.size() == 0) {
            return false;
        }
        for (final Connection conn : connections) {
            if (conn.isConnected()) {
                return true;
            }
        }
        return false;
    }


    public void setClientConfig(final ClientConfig clientConfig) {
        if (this.controller != null && this.controller.isStarted()) {
            throw new IllegalStateException("RemotingClient�Ѿ�������������Ч");
        }
        this.config = clientConfig;
    }


    @Override
    protected void doStart() throws NotifyRemotingException {
        this.startController();
        this.startReconnectManager();
    }


    private void startReconnectManager() {
        // ��������������
        this.reconnectManager =
                new ReconnectManager((GeckoTCPConnectorController) this.controller, (ClientConfig) this.config, this);
        ((GeckoHandler) this.controller.getHandler()).setReconnectManager(this.reconnectManager);
        this.reconnectManager.start();
    }


    private void startController() throws NotifyRemotingException {
        try {
            this.controller.start();
        }
        catch (final IOException e) {
            throw new NotifyRemotingException("��������������", e);
        }
    }


    @Override
    protected void doStop() throws NotifyRemotingException {
        this.stopReconnectManager();
        this.closeAllConnection();
    }


    private void closeAllConnection() throws NotifyRemotingException {
        // �ر���������
        final List<Connection> connections = this.remotingContext.getConnectionsByGroup(Constants.DEFAULT_GROUP);
        if (connections != null) {
            for (final Connection conn : connections) {
                ((DefaultConnection) conn).setReady(true);// ǿ��Ϊ����״̬
                conn.close(false);
            }
        }
    }


    private void stopReconnectManager() {
        this.reconnectManager.stop();
    }


    /**
     * ������ʧ�ܵ�ʱ��ص�
     */
    @SuppressWarnings("unchecked")
    public void onConnectFail(final Object... args) {
        if (args.length >= 2) {
            final Set<String> groupSet = (Set<String>) args[0];
            final InetSocketAddress remoteAddr = (InetSocketAddress) args[1];
            this.reconnectManager.addReconnectTask(new ReconnectTask(groupSet, remoteAddr));
            if (args.length >= 3) {
                final TimerRef timerRef = (TimerRef) args[2];
                timerRef.cancel();
            }
        }

    }


    @Override
    protected SocketChannelController initController(final Configuration conf) {
        final GeckoTCPConnectorController notifyTCPConnectorController = new GeckoTCPConnectorController(conf);
        // ��������ʧ�ܼ�����
        notifyTCPConnectorController.setConnectFailListener(this);
        return notifyTCPConnectorController;
    }

}