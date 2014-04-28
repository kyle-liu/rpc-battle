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

import java.io.IOException;
import java.net.InetSocketAddress;

import com.taobao.gecko.service.config.ClientConfig;
import com.taobao.gecko.service.exception.NotifyRemotingException;


/**
 * 
 * Notify Remoting�Ŀͻ��˽ӿ�
 * 
 * @author boyan
 * 
 * @since 1.0, 2009-12-16 ����02:04:38
 */

public interface RemotingClient extends RemotingController {
    /**
     * ����URL���ӷ���ˣ��������ʧ�ܽ�ת������ģʽ
     * 
     * @param group
     *            ����˵�URL������schema://host:port���ַ���
     * @throws IOException
     */
    public void connect(String url) throws NotifyRemotingException;


    /**
     * �ȴ����Ӿ��������жϣ����Ӿ����ĺ������£���ָָ���������Ч�������ﵽ�趨ֵ�����ҿ��á�Ĭ�ϵȴ���ʱΪ�������������ӳ�ʱ
     * 
     * @param group
     * @throws NotifyRemotingException
     * @throws InterruptedException
     */
    public void awaitReadyInterrupt(String url) throws NotifyRemotingException, InterruptedException;


    /**
     * �ȴ����Ӿ��������жϣ����Ӿ����ĺ������£���ָָ���������Ч�������ﵽ�趨ֵ�����ҿ��á�Ĭ�ϵȴ���ʱΪ�������������ӳ�ʱ
     * 
     * @param group
     * @throws NotifyRemotingException
     * @throws InterruptedException
     */
    public void awaitReadyInterrupt(String url, long time) throws NotifyRemotingException, InterruptedException;


    /**
     * ����URL���ӷ���ˣ��������ʧ�ܽ�ת������ģʽ
     * 
     * @param url
     *            ����˵�URL������schema://host:port���ַ���
     * @throws IOException
     */
    public void connect(String url, int connCount) throws NotifyRemotingException;


    /**
     * ��ȡԶ�˵�ַ
     * 
     * @param url
     *            ����˵�url������schema://host:port���ַ���
     * @return
     */
    public InetSocketAddress getRemoteAddress(String url);


    /**
     * ��ȡԶ�˵�ַ
     * 
     * @param url
     *            ����˵�group������schema://host:port���ַ���
     * @return
     */
    public String getRemoteAddressString(String url);


    /**
     * �ж�url��Ӧ�������Ƿ���ã�ע�⣬������������ӳأ���ô������ӳ�����һ���ӿ��ã�����Ϊ����
     * 
     * @param url
     *            ����˵�url������schema://host:port���ַ���
     * @return
     */
    public boolean isConnected(String url);


    /**
     * �ر�url��Ӧ������
     * 
     * @param url
     *            ����˵�url������schema:://host:port���ַ���
     * @param allowReconnect
     *            �Ƿ���Ҫ����
     * @throws NotifyRemotingException
     * 
     */
    public void close(String url, boolean allowReconnect) throws NotifyRemotingException;


    /**
     * ���ÿͻ������ã�ֻ��������ǰ���ã�������������Ч
     * 
     * @param clientConfig
     */
    public void setClientConfig(ClientConfig clientConfig);

}