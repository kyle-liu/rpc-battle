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
package com.taobao.gecko.core.command;

/**
 * Ӧ��״̬
 * 
 * 
 * 
 * @author boyan
 * 
 * @since 1.0, 2009-12-16 ����05:55:23
 */
public enum ResponseStatus {
    NO_ERROR(null), // �����ɹ�
    ERROR("Error by user"), // ������Ӧ����������
    EXCEPTION("Exception occured"), // �쳣
    UNKNOWN("Unknow error"), // û��ע��Listener������CheckMessageListener��MessageListener
    THREADPOOL_BUSY("Thread pool is busy"), // ��Ӧ���̷߳�æ
    ERROR_COMM("Communication error"), // ͨѶ������������
    NO_PROCESSOR("There is no processor to handle this request"), // û�и���������Ĵ�����
    TIMEOUT("Operation timeout"); // ��Ӧ��ʱ

    private String errorMessage;


    private ResponseStatus(final String errorMessage) {
        this.errorMessage = errorMessage;
    }


    public String getErrorMessage() {
        return this.errorMessage;
    }

}