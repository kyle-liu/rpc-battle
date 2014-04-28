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
package com.taobao.gecko.core.nio;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.concurrent.Future;


/**
 * 
 * �ͻ��˵����ӹ������������ӵ���������
 * 
 * @author boyan
 * 
 * @since 1.0, 2009-12-25 ����01:12:47
 */

public interface SingleConnector {

    public Future<Boolean> connect(SocketAddress socketAddress,Object...args) throws IOException;


    public Future<Boolean> send(Object msg);


    public boolean isConnected();


    public void awaitConnectUnInterrupt() throws IOException;


    public void disconnect() throws IOException;
}