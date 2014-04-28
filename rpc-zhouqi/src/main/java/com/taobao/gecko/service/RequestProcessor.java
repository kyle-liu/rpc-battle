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

import com.taobao.gecko.core.command.RequestCommand;


/**
 * 
 * 
 * ��������
 * 
 * @author boyan
 * 
 * @since 1.0, 2009-12-15 ����02:45:43
 */

public interface RequestProcessor<T extends RequestCommand> {
    /**
     * ��������
     * 
     * @param request
     *            ��������
     * @param conn
     *            ������Դ������
     */
    public void handleRequest(T request, Connection conn);


    /**
     * �û��Զ�����̳߳أ�����ṩ����ô����Ĵ������ڸ��̳߳���ִ��
     * 
     * @return
     */
    public ThreadPoolExecutor getExecutor();
}