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

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.taobao.gecko.core.buffer.IoBuffer;
import com.taobao.gecko.core.command.RequestCommand;
import com.taobao.gecko.core.command.ResponseCommand;
import com.taobao.gecko.service.exception.NotifyRemotingException;


/**
 * 
 * ���ӵİ�װ���ṩ���߲�εĳ���
 * 
 * @author boyan
 * 
 * @since 1.0, 2009-12-15 ����02:38:20
 */

public interface Connection {

    /**
     * ��ȡȫ��������
     * 
     * @return
     */
    public RemotingContext getRemotingContext();


    /**
     * �ر�����
     * 
     * @param allowReconnect
     *            ���true���������Զ�����
     * @throws NotifyRemotingException
     */
    public void close(boolean allowReconnect) throws NotifyRemotingException;


    /**
     * �����Ƿ���Ч
     * 
     * @return
     */
    public boolean isConnected();


    /**
     * ͬ�����ã�ָ����ʱʱ��
     * 
     * @param requestCommand
     * @param timeConnection
     * @param timeUnit
     * @return
     * @throws InterruptedException
     * @throws TimeoutException
     */
    public ResponseCommand invoke(final RequestCommand requestCommand, long time, TimeUnit timeUnit)
            throws InterruptedException, TimeoutException, NotifyRemotingException;


    /**
     * ͬ�����ã�Ĭ�ϳ�ʱ1��
     * 
     * @param request
     * @return
     * @throws InterruptedException
     * @throws TimeoutException
     */
    public ResponseCommand invoke(final RequestCommand request) throws InterruptedException, TimeoutException,
            NotifyRemotingException;


    /**
     * �첽���ͣ�ָ���ص���������Ĭ�ϳ�ʱ1�룬��ʱ������һ����ʱӦ����ص�������
     * 
     * @param requestCommand
     * @param listener
     */
    public void send(final RequestCommand requestCommand, SingleRequestCallBackListener listener)
            throws NotifyRemotingException;


    /**
     * �첽���ͣ�ָ���ص��������ͳ�ʱʱ�䣬��ʱ������һ����ʱӦ����ص�������
     * 
     * @param requestCommand
     * @param listener
     */
    public void send(final RequestCommand requestCommand, SingleRequestCallBackListener listener, long time,
            TimeUnit timeUnit) throws NotifyRemotingException;


    /**
     * �첽������
     * 
     * @param requestCommand
     */
    public void send(final RequestCommand requestCommand) throws NotifyRemotingException;


    /**
     * �첽���ͣ������ؿ�ȡ����future
     * 
     * @param requestCommand
     * @return
     * @throws NotifyRemotingException
     */
    public Future<Boolean> asyncSend(final RequestCommand requestCommand) throws NotifyRemotingException;


    /**
     * �����첽Ӧ��
     * 
     * @param responseCommand
     */
    public void response(final Object responseCommand) throws NotifyRemotingException;


    /**
     * ������ӵ���������
     */
    public void clearAttributes();


    /**
     * ��ȡ�����ϵ�ĳ������
     * 
     * @param key
     * @return
     */
    public Object getAttribute(String key);


    /**
     * ��ȡԶ�˵�ַ
     * 
     * @return
     */
    public InetSocketAddress getRemoteSocketAddress();


    /**
     * ��ȡ����IP��ַ
     * 
     * @return
     */
    public InetAddress getLocalAddress();


    /**
     * �Ƴ�����
     * 
     * @param key
     */
    public void removeAttribute(String key);


    /**
     * ��������
     * 
     * @param key
     * @param value
     */
    public void setAttribute(String key, Object value);


    /**
     * �������Ե�key����
     * 
     * @since 1.8.3
     * @return
     */
    public Set<String> attributeKeySet();


    /**
     * �������ӵĶ����������ֽ���
     * 
     * @param byteOrder
     */
    public void readBufferOrder(ByteOrder byteOrder);


    /**
     * ��ȡ���ӵĶ����������ֽ���
     * 
     * @param byteOrder
     * @return TODO
     */
    public ByteOrder readBufferOrder();


    /**
     * �������ԣ�����ConcurrentHashMap.putIfAbsent����
     * 
     * @param key
     * @param value
     * @return
     */
    public Object setAttributeIfAbsent(String key, Object value);


    /**
     * ���ظ��������ڵķ��鼯��
     * 
     * @return
     */
    public Set<String> getGroupSet();


    /**
     * �Ƿ����ÿ��ж�д�������������ã����������û��߳�д��socket
     * buffer������ݵķ���Ч�ʣ������û��̵߳��жϿ����������ӶϿ���������ʹ�á�Ĭ�ϲ����á�
     * 
     * @param writeInterruptibly
     *            true�������� false����������
     */
    public void setWriteInterruptibly(boolean writeInterruptibly);


    /**
     * �����䣬�޳�ʱ
     * 
     * @see #transferFrom(IoBuffer, IoBuffer, FileChannel, long, long, Integer,
     *      SingleRequestCallBackListener, long, TimeUnit)
     * @param head
     * @param tail
     * @param channel
     * @param position
     * @param size
     * @since 1.8.3
     */
    public void transferFrom(IoBuffer head, IoBuffer tail, FileChannel channel, long position, long size);


    /**
     * ��ָ��FileChannel��positionλ�ÿ�ʼ����size���ֽڵ�socket,
     * remoting�Ḻ��֤��ָ����С�����ݴ����socket�����file channel������ݲ���size��С������ʵ�ʴ�С���䡣
     * ������head��tail��ָ�ڴ����ļ�֮ǰ����֮����Ҫд������ݣ�����Ϊnull�����Ǻ��ļ�������Ϊһ�����������͡�
     * ����ָ���ĳ�ʱʱ����ȡ������(�����û�п�ʼ����Ļ�,�Ѿ���ʼ���޷���ֹ)����֪ͨlistener��
     * 
     * @param head
     * @param tail
     * @param channel
     * @param position
     * @param size
     * @param opaque
     * @param listener
     * @param time
     * @param unit
     * @since 1.1.0
     */
    public void transferFrom(IoBuffer head, IoBuffer tail, FileChannel channel, long position, long size,
            Integer opaque, SingleRequestCallBackListener listener, long time, TimeUnit unit)
            throws NotifyRemotingException;

}