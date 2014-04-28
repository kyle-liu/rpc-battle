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
package com.taobao.gecko.service.config;

import java.util.HashMap;
import java.util.Map;

import com.taobao.gecko.core.command.CommandFactory;
import com.taobao.gecko.core.core.CodecFactory;


/**
 * 
 * 
 * wireЭ�����ͣ��κ���Ҫʹ��gecko��Э�鶼��Ҫ�̳д��ಢʵ����Ӧ����
 * 
 * @author boyan
 * 
 * @since 1.0, 2010-1-27 ����05:46:27
 */

public abstract class WireFormatType {
    private static Map<String, WireFormatType> registeredWireFormatType = new HashMap<String, WireFormatType>();


    /**
     * ע��Э������
     * 
     * @param wireFormatType
     */
    public static void registerWireFormatType(final WireFormatType wireFormatType) {
        if (wireFormatType == null) {
            throw new IllegalArgumentException("Null wire format");
        }
        registeredWireFormatType.put(wireFormatType.name(), wireFormatType);
    }


    /**
     * ȡ��Э�����͵�ע��
     * 
     * @param wireFormatType
     */
    public static void unregisterWireFormatType(final WireFormatType wireFormatType) {
        if (wireFormatType == null) {
            throw new IllegalArgumentException("Null wire format");
        }
        registeredWireFormatType.remove(wireFormatType.name());
    }


    @Override
    public String toString() {
        return this.name();
    }


    public static WireFormatType valueOf(final String name) {
        return registeredWireFormatType.get(name);

    }


    /**
     * Э���scheme
     * 
     * @return
     */
    public abstract String getScheme();


    /**
     * Э��ı���빤��
     * 
     * @return
     */
    public abstract CodecFactory newCodecFactory();


    /**
     * Э��������
     * 
     * @return
     */
    public abstract CommandFactory newCommandFactory();


    /**
     * Э������
     * 
     * @return
     */
    public abstract String name();
}