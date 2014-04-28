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
package com.taobao.gecko.service;

import java.nio.channels.FileChannel;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.taobao.gecko.core.buffer.IoBuffer;
import com.taobao.gecko.core.command.RequestCommand;
import com.taobao.gecko.core.command.ResponseCommand;
import com.taobao.gecko.core.nio.impl.TimerRef;
import com.taobao.gecko.service.exception.NotifyRemotingException;


/**
 * 
 * Notify Remoting��������ӿ�
 * 
 * @author boyan
 * 
 * @since 1.0, 2009-12-16 ����02:15:02
 */

public interface RemotingController {

    /**
     * ��������ѡ������Ĭ��Ϊ���ѡ����
     * 
     * @param selector
     */
    public void setConnectionSelector(ConnectionSelector selector);


    /**
     * ����Remoting������
     * 
     * @throws NotifyRemotingException
     * 
     */
    public void start() throws NotifyRemotingException;


    /**
     * �ر�Remoting������
     * 
     * @throws NotifyRemotingException
     * 
     */
    public void stop() throws NotifyRemotingException;


    /**
     * �ж�ͨѶ����Ƿ�����
     * 
     * @return
     */
    public boolean isStarted();


    /**
     * ע����������
     * 
     * @param <T>
     * @param commandClazz
     * @param processor
     */
    public <T extends RequestCommand> void registerProcessor(Class<T> commandClazz, RequestProcessor<T> processor);


    /**
     * ��ȡcommand��Ӧ�Ĵ�����
     * 
     * @param clazz
     * @return
     */
    public RequestProcessor<? extends RequestCommand> getProcessor(Class<? extends RequestCommand> clazz);


    /**
     * ȡ����������ע��,���ر�ȡ���Ĵ�����
     * 
     * @param clazz
     * @return
     */
    public RequestProcessor<? extends RequestCommand> unreigsterProcessor(Class<? extends RequestCommand> clazz);


    /**
     * ���������������
     * 
     * @param <T>
     * @param map
     */
    public void addAllProcessors(Map<Class<? extends RequestCommand>, RequestProcessor<? extends RequestCommand>> map);


    /**
     * ���һ����ʱ��
     * 
     * @param timeout
     *            ��ʱ��ʱ��
     * @param timeUnit
     *            ʱ�䵥λ
     * @param runnable
     *            ��ʱִ�е�����
     */
    public void insertTimer(TimerRef timerRef);


    /**
     * �첽������Ϣ��������飬ÿ��������ݲ���ѡһ�����ӷ��ͣ�ָ���ص��������ͳ�ʱʱ�䣬��ʱ������һ����ʱӦ����ص�������
     * 
     * @param groupObjects
     *            group->message
     * @param listener
     *            Ӧ������
     * @param timeout
     *            ��ʱʱ��
     * @param timeUnit
     *            ʱ�䵥λ
     * @param args
     *            ����
     */
    public void sendToGroups(Map<String, RequestCommand> groupObjects, MultiGroupCallBackListener listener,
            long timeout, TimeUnit timeUnit, Object... args) throws NotifyRemotingException;


    /**
     * �첽��������Ϣ���������
     * 
     * @param groupObjects
     */
    public void sendToGroups(Map<String, RequestCommand> groupObjects) throws NotifyRemotingException;


    /**
     * �첽�����͸���������
     * 
     * @param command
     */
    public void sendToAllConnections(RequestCommand command) throws NotifyRemotingException;


    /**
     * �첽�����͸�ָ�������е�һ�����ӣ�Ĭ�����������
     * 
     * @param group
     * @param command
     */
    public void sendToGroup(String group, RequestCommand command) throws NotifyRemotingException;


    /**
     * ��ָ��FileChannel��positionλ�ÿ�ʼ����size���ֽڵ�ָ��group��һ��socket,
     * remoting�Ḻ��֤��ָ����С�����ݴ����socket�����file channel������ݲ���size��С������ʵ�ʴ�С���䡣
     * ����head��tail��ָ�ڴ����ļ�֮ǰ����֮����Ҫд������ݣ�����Ϊnull�����Ǻ��ļ�������Ϊһ�����������͡�
     * ����ָ���ĳ�ʱʱ����ȡ������(�����û�п�ʼ����Ļ�,�Ѿ���ʼ���޷���ֹ)����֪ͨlistener��
     * 
     * @param group
     * @param head
     * @param tail
     * @param channel
     * @param position
     * @param size
     * @param opaque
     * @param listener
     * @param time
     * @param unit
     * @throws NotifyRemotingException
     */
    public void transferToGroup(String group, IoBuffer head, IoBuffer tail, FileChannel channel, long position,
            long size, Integer opaque, SingleRequestCallBackListener listener, long time, TimeUnit unit)
            throws NotifyRemotingException;


    /**
     * ���������ݵ�ָ��group��ĳ��socket���ӣ�������Ҫʹ�õ�ʱ��δ֪��Ҳ����ȡ��
     * 
     * @see #transferToGroup(String, IoBuffer, IoBuffer, FileChannel, long,
     *      long, Integer, SingleRequestCallBackListener, long, TimeUnit)
     * @param group
     * @param head
     * @param tail
     * @param channel
     * @param position
     * @param size
     */
    public void transferToGroup(String group, IoBuffer head, IoBuffer tail, FileChannel channel, long position,
            long size) throws NotifyRemotingException;


    /**
     * �첽�����͸�ָ���������������
     * 
     * @param group
     * @param command
     */
    public void sendToGroupAllConnections(String group, RequestCommand command) throws NotifyRemotingException;


    /**
     * �첽���͸�ָ�������е�һ�����ӣ�ָ���ص�������RequestCallBackListener��Ĭ�ϲ����������Ĭ�ϳ�ʱΪ1��,
     * ������ʱʱ�佫����һ����ʱӦ����ص�������
     * 
     * @param group
     *            ��������
     * @param command
     *            ��������
     * @param listener
     *            ��Ӧ������
     */
    public void sendToGroup(String group, RequestCommand command, SingleRequestCallBackListener listener)
            throws NotifyRemotingException;


    /**
     * �첽���͸�ָ�������е�һ�����ӣ�Ĭ�ϲ����������ָ����ʱ,������ʱʱ�佫����һ����ʱӦ����ص�������
     * 
     * @param group
     *            ��������
     * @param command
     *            ��������
     * @param listener
     *            ��Ӧ������
     */
    public void sendToGroup(String group, RequestCommand command, SingleRequestCallBackListener listener, long time,
            TimeUnit timeunut) throws NotifyRemotingException;


    /**
     * ͬ�����÷����е�һ�����ӣ�Ĭ�ϳ�ʱ1��
     * 
     * @param group
     *            ��������
     * @param command
     *            ��������
     * @return
     * @throws InterruptedException
     * @throws TimeoutException
     */
    public ResponseCommand invokeToGroup(String group, RequestCommand command) throws InterruptedException,
            TimeoutException, NotifyRemotingException;


    /**
     * ͬ�����÷����е�һ�����ӣ�ָ����ʱʱ��
     * 
     * @param group
     *            ��������
     * @param command
     *            ��������
     * @param time
     *            ��ʱʱ��
     * @param timeUnit
     *            ʱ�䵥λ
     * @return
     * @throws InterruptedException
     * @throws TimeoutException
     */
    public ResponseCommand invokeToGroup(String group, RequestCommand command, long time, TimeUnit timeUnit)
            throws InterruptedException, TimeoutException, NotifyRemotingException;


    /**
     * �첽���͸�ָ��������������ӣ�Ĭ�ϳ�ʱ1��,������ʱʱ�佫����һ����ʱӦ����ص�������
     * 
     * @param group
     *            ��������
     * @param command
     *            ��������
     * @param listener
     *            ��Ӧ������
     */
    public void sendToGroupAllConnections(String group, RequestCommand command,
            GroupAllConnectionCallBackListener listener) throws NotifyRemotingException;


    /**
     * ͬ�����÷����ڵ��������ӣ�
     * ��ʱ��Ӧ�����ӽ�����һ��BooleanResponseCommand��Ϊ�����������responseStatusΪTIMEOUT
     * ,���������û�����ӽ�����null
     * 
     * @param group
     * @param command
     * @return
     * @throws InterruptedException
     * @throws NotifyRemotingException
     */
    public Map<Connection, ResponseCommand> invokeToGroupAllConnections(String group, RequestCommand command)
            throws InterruptedException, NotifyRemotingException;


    /**
     * ͬ�����÷����ڵ��������ӣ�
     * ��ʱ��Ӧ�����ӽ�����һ��BooleanResponseCommand��Ϊ�����������responseStatusΪTIMEOUT
     * ,���������û�����ӽ�����null
     * 
     * @param group
     * @param command
     * @return
     * @throws InterruptedException
     * @throws NotifyRemotingException
     */
    public Map<Connection, ResponseCommand> invokeToGroupAllConnections(String group, RequestCommand command,
            long time, TimeUnit timeUnit) throws InterruptedException, NotifyRemotingException;


    /**
     * �첽���͸�ָ��������������ӣ�ָ����ʱʱ�䣬������ʱʱ�佫����һ����ʱӦ����ص�������
     * 
     * @param group
     *            ��������
     * @param command
     *            ��������
     * @param listener
     *            ��Ӧ������
     */
    public void sendToGroupAllConnections(String group, RequestCommand command,
            GroupAllConnectionCallBackListener listener, long time, TimeUnit timeUnit) throws NotifyRemotingException;


    /**
     * ��ȡgroup��Ӧ��������
     * 
     * @param group
     * @return
     */
    public int getConnectionCount(String group);


    /**
     * ��ȡgroup����
     * 
     * @return
     */
    public Set<String> getGroupSet();


    /**
     * ��������
     * 
     * @param group
     * 
     * @param key
     * @param value
     */
    public void setAttribute(String group, String key, Object value);


    /**
     * �������ԣ�����ConcurrentHashMap.putIfAbsent
     * 
     * @param group
     * 
     * @param key
     * @param value
     * @return
     */
    public Object setAttributeIfAbsent(String group, String key, Object value);


    /**
     * ��ȡ����
     * 
     * @param group
     * 
     * @param key
     * @return
     */
    public Object getAttribute(String group, String key);


    /**
     * ��������������ڼ�����
     * 
     * @param connectionLifeCycleListener
     */
    public void addConnectionLifeCycleListener(ConnectionLifeCycleListener connectionLifeCycleListener);


    /**
     * ��������������ڼ�����
     * 
     * @param connectionLifeCycleListener
     */
    public void removeConnectionLifeCycleListener(ConnectionLifeCycleListener connectionLifeCycleListener);


    /**
     * �Ƴ�����
     * 
     * @param group
     * 
     * @param key
     * @return
     */
    public Object removeAttribute(String group, String key);


    /**
     * ��ȡȫ��������
     * 
     * @return
     */
    public RemotingContext getRemotingContext();


    /**
     * ���ݲ��Դӷ����е�����ѡ��һ��
     * 
     * @param group
     * @param connectionSelector
     *            ����ѡ����
     * @param request
     *            ���͵�����
     * @return
     */
    public Connection selectConnectionForGroup(String group, ConnectionSelector connectionSelector,
            RequestCommand request) throws NotifyRemotingException;

}