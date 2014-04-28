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
package com.taobao.gecko.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.taobao.gecko.service.Connection;


/**
 * ɨ����Ч���������񣬽����ڷ�����
 * 
 * @author boyan
 * @Date 2010-5-26
 * 
 */
public class InvalidConnectionScanTask implements ScanTask {
    // ���ڷ�������˵�����5����û���κβ�������ô���Ͽ����ӣ���Ϊ�ͻ������ǻᷢ��������⣬��˲���������Ŀ����������С�
    public static long TIMEOUT_THRESHOLD = Long.parseLong(System.getProperty(
        "notify.remoting.connection.timeout_threshold", "300000"));
    static final Log log = LogFactory.getLog(InvalidConnectionScanTask.class);


    public void visit(final long now, final Connection conn) {
        final long lastOpTimestamp = ((DefaultConnection) conn).getSession().getLastOperationTimeStamp();
        if (now - lastOpTimestamp > TIMEOUT_THRESHOLD) {
            log.info("��Ч������" + conn.getRemoteSocketAddress() + "���رգ�����" + TIMEOUT_THRESHOLD + "����û���κ�IO����");
            try {
                conn.close(false);
            }
            catch (final Throwable t) {
                log.error("�ر�����ʧ��", t);
            }
        }
    }
}