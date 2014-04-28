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

import java.util.List;
import java.util.Set;

import com.taobao.gecko.core.command.CommandFactory;
import com.taobao.gecko.service.config.BaseConfig;


/**
 * 
 * Remoting��ȫ��������
 * 
 * @author boyan
 * 
 * @since 1.0, 2009-12-15 ����03:54:01
 */

public interface RemotingContext {

    /**
     * ������ӵ�ָ������
     * 
     * @param group
     * @param connection
     * @return
     */
    public abstract boolean addConnectionToGroup(String group, Connection connection);


    /**
     * ��ȡ��ǰ����������ö���
     * 
     * @return
     */
    public abstract BaseConfig getConfig();


    /**
     * ��ӵ�Ĭ�Ϸ���
     * 
     * @param connection
     */
    public abstract void addConnection(Connection connection);


    /**
     * ��Ĭ�Ϸ����Ƴ�
     * 
     * @param connection
     */
    public abstract void removeConnection(Connection connection);


    /**
     * ����Group�õ�connection����
     * 
     * @param group
     * @return
     */
    public abstract List<Connection> getConnectionsByGroup(String group);


    /**
     * �Ƴ�����
     * 
     * @param group
     * @param connection
     * @return
     */
    public abstract boolean removeConnectionFromGroup(String group, Connection connection);


    public abstract Object getAttribute(Object key, Object defaultValue);


    public abstract Object getAttribute(Object key);


    public abstract Set<Object> getAttributeKeys();


    public abstract Object setAttribute(Object key, Object value);


    public abstract Object setAttributeIfAbsent(Object key, Object value);


    public abstract Object setAttributeIfAbsent(Object key);


    /**
     * ��ȡ��ǰ�ͻ��˻��߷����������з�������
     * 
     * @return
     */
    public Set<String> getGroupSet();


    /**
     * ��ȡ��ǰʹ�õ�Э�鹤��
     * 
     * @return
     */
    public CommandFactory getCommandFactory();

}