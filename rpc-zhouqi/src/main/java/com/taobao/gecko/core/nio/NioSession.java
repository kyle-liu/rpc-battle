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
/**
 *Copyright [2009-2010] [dennis zhuang]
 *Licensed under the Apache License, Version 2.0 (the "License");
 *you may not use this file except in compliance with the License.
 *You may obtain a copy of the License at
 *             http://www.apache.org/licenses/LICENSE-2.0
 *Unless required by applicable law or agreed to in writing,
 *software distributed under the License is distributed on an "AS IS" BASIS,
 *WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 *either express or implied. See the License for the specific language governing permissions and limitations under the License
 */
package com.taobao.gecko.core.nio;

import java.nio.channels.FileChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.Selector;
import java.util.concurrent.Future;

import com.taobao.gecko.core.buffer.IoBuffer;
import com.taobao.gecko.core.core.EventType;
import com.taobao.gecko.core.core.Session;
import com.taobao.gecko.core.nio.impl.TimerRef;


/**
 * Nio�����ӽӿ�
 * 
 * @author boyan
 * 
 */
public interface NioSession extends Session {
    /*
     * �ɷ��¼�
     * 
     * @param event
     * 
     * @param selector
     */
    public void onEvent(EventType event, Selector selector);


    /**
     * ע���
     * 
     * @param selector
     */
    public void enableRead(Selector selector);


    /**
     * ע��д
     * 
     * @param selector
     */
    public void enableWrite(Selector selector);


    /**
     * ���һ����ʱ��
     * 
     * @param timeout
     * @param runnable
     * @return TODO
     */
    public void insertTimer(TimerRef timerRef);


    /**
     * ��������д����Ϣ���ɱ��жϣ��жϿ����������ӵĹرգ�����ʹ��
     * 
     * @param message
     */
    public void writeInterruptibly(Object message);


    /**
     * ��������д����Ϣ���ɱ��жϣ��жϿ����������ӵĹرգ�����ʹ��
     * 
     * @param message
     */
    public Future<Boolean> asyncWriteInterruptibly(Object message);


    /**
     * ������Ӷ�Ӧ��channel
     * 
     * @return
     */
    public SelectableChannel channel();


    /**
     * ��ָ��FileChannel��positionλ�ÿ�ʼ����size���ֽڵ�socket������future�����ѯ״̬,
     * ����head��tail���ڴ����ļ�ǰ��д������ݣ�����Ϊnull
     * 
     * @param src
     * @param position
     * @param size
     * @return
     */
    public Future<Boolean> asyncTransferFrom(IoBuffer head, IoBuffer tail, FileChannel src, long position, long size);


    /**
     * ��ָ��FileChannel��positionλ�ÿ�ʼ����size���ֽڵ�socket������head��tail���ڴ����ļ�ǰ��д������ݣ�
     * ����Ϊnull
     * 
     * @param src
     * @param position
     * @param size
     */
    public Future<Boolean> transferFrom(IoBuffer head, IoBuffer tail, FileChannel src, long position, long size);
}