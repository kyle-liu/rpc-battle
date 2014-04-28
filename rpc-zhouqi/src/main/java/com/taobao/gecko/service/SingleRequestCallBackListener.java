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

import java.util.concurrent.ThreadPoolExecutor;

import com.taobao.gecko.core.command.ResponseCommand;


/**
 * 
 * 
 * ��������ĵ������ӵ�Ӧ��ص�������
 * 
 * @author boyan
 * 
 * @since 1.0, 2009-12-15 ����04:13:17
 */

public interface SingleRequestCallBackListener {

    /**
     * ����Ӧ��
     * 
     * @param responseCommand
     *            Ӧ������
     * @param conn
     *            Ӧ������
     */
    public void onResponse(ResponseCommand responseCommand, Connection conn);


    /**
     * �쳣������ʱ��ص�
     * 
     * @param e
     */
    public void onException(Exception e);


    /**
     * onResponse�ص�ִ�е��̳߳�
     * 
     * @return
     */
    public ThreadPoolExecutor getExecutor();
}