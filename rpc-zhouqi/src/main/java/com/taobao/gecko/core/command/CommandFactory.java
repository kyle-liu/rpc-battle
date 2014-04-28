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

import com.taobao.gecko.core.command.kernel.BooleanAckCommand;
import com.taobao.gecko.core.command.kernel.HeartBeatRequestCommand;


/**
 * Э�鹤���࣬�κ�Э���ʵ�ֶ�����ʵ�ִ˹����ӿڣ��ṩ����BooleanAckCommand��HeartBeatRequestCommand�ķ���
 * 
 * @author boyan
 * 
 */
public interface CommandFactory {
    /**
     * �����ض���Э���BooleanAckCommand
     * 
     * @param request
     *            ����ͷ
     * @param responseStatus
     *            ��Ӧ״̬
     * @param errorMsg
     *            ������Ϣ
     * @return
     */
    public BooleanAckCommand createBooleanAckCommand(CommandHeader request, ResponseStatus responseStatus,
            String errorMsg);


    /**
     * �����ض���Э�����������
     * 
     * @return
     */
    public HeartBeatRequestCommand createHeartBeatCommand();

}