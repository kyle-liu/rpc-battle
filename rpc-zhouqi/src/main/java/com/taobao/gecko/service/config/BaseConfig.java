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

import com.taobao.gecko.core.util.SystemUtils;


/**
 * 
 * 
 * 
 * @author boyan
 * 
 * @since 1.0, 2009-12-16 ����05:15:01
 */

public class BaseConfig {

    public BaseConfig() {

    }

    /**
     * Э������
     */
    private WireFormatType wireFormatType = WireFormatType.valueOf("NOTIFY_V1");

    /**
     * �Ƿ����ö˿�
     */
    private boolean reuseAddr = true;

    /**
     * socket SO_KEEPALIVEѡ��
     */
    private boolean keepAlive = true;
    /**
     * �Ƿ��ֹNagle�㷨��Ĭ��Ϊtrue
     */
    private boolean tcpNoDelay = true;
    /**
     * ���Ӷ���������С
     */
    private int readBufferSize = 128 * 1024;
    /**
     * ��Ϊ����Idle�ļ��ʱ�䣬��λ��
     */
    private int idleTime;
    /**
     * ���Ķ���������С����ֹ�ڴ����
     */
    private int maxReadBufferSize;
    /**
     * ������¼����߳���
     */
    private int readThreadCount = (int) (2.5f * SystemUtils.getCpuProcessorCount());
    /**
     * ������Ϣ���߳���
     */
    private int dispatchMessageThreadCount = 0;
    /**
     * ����д�¼����߳���
     */
    private int writeThreadCount = 0;
    /**
     * Socket�Ľ��ջ�������С
     */
    private int rcvBufferSize = 64 * 1024;
    /**
     * Socket���ͻ�������С
     */
    private int sndBufferSize = 64 * 1024;
    /**
     * Socket SO_LINGERѡ��
     */
    private boolean soLinger = true;
    /**
     * lingerֵ
     */
    private int linger = 0;
    /**
     * Selector�ش�С
     */
    private int selectorPoolSize = 2 * SystemUtils.getCpuProcessorCount();

    /**
     * �ص�������̳߳ش�С
     */
    private int callBackExecutorPoolSize = SystemUtils.getSystemThreadCount();

    /**
     * �ص�������̳߳�����С
     */
    private int maxCallBackExecutorPoolSize = 30;

    /**
     * �ص��̳߳ض��д�С
     */
    private int callBackExecutorQueueSize = 20000;

    /**
     * ���ͻ����������ֽ�����Ĭ��Ϊ����ڴ��1/3������������Ϊ1000
     */
    private volatile long maxScheduleWrittenBytes = Runtime.getRuntime().maxMemory() / 3 / 1000;

    /**
     * ��ʱɨ���������ӵ�ʱ������Ĭ��Ϊ5���ӣ���λ��
     */
    private int scanAllConnectionInterval = 5 * 60;

    /**
     * ��������ص�����,Ĭ��1�����
     */
    private int maxCallBackCount = 1000000;


    public int getCallBackExecutorPoolSize() {
        return this.callBackExecutorPoolSize;
    }


    public void setCallBackExecutorPoolSize(final int callBackExecutorPoolSize) {
        this.callBackExecutorPoolSize = callBackExecutorPoolSize;
        this.maxCallBackExecutorPoolSize = 3 * this.callBackExecutorPoolSize;
    }


    public WireFormatType getWireFormatType() {
        return this.wireFormatType;
    }


    public void setWireFormatType(final WireFormatType wireFormatType) {
        WireFormatType.registerWireFormatType(wireFormatType);
        this.wireFormatType = wireFormatType;
    }


    public int getMaxCallBackCount() {
        return this.maxCallBackCount;
    }


    public void setMaxCallBackCount(final int maxCallBackCountPerConnection) {
        this.maxCallBackCount = maxCallBackCountPerConnection;
    }


    public int getScanAllConnectionInterval() {
        return this.scanAllConnectionInterval;
    }


    public void setScanAllConnectionInterval(final int scanInvalidCallBackInterval) {
        this.scanAllConnectionInterval = scanInvalidCallBackInterval;
    }


    public long getMaxScheduleWrittenBytes() {
        return this.maxScheduleWrittenBytes;
    }


    public void setMaxScheduleWrittenBytes(final long maxScheduleWrittenBytes) {
        this.maxScheduleWrittenBytes = maxScheduleWrittenBytes;
    }


    public int getMaxCallBackExecutorPoolSize() {
        return this.maxCallBackExecutorPoolSize;
    }


    public boolean isKeepAlive() {
        return this.keepAlive;
    }


    public void setKeepAlive(final boolean keepAlive) {
        this.keepAlive = keepAlive;
    }


    public void setMaxCallBackExecutorPoolSize(final int maxCallBackExecutorPoolSize) {
        this.maxCallBackExecutorPoolSize = maxCallBackExecutorPoolSize;
    }


    public int getCallBackExecutorQueueSize() {
        return this.callBackExecutorQueueSize;
    }


    public void setCallBackExecutorQueueSize(final int callBackExecutorQueueSize) {
        this.callBackExecutorQueueSize = callBackExecutorQueueSize;
    }


    public boolean isTcpNoDelay() {
        return this.tcpNoDelay;
    }


    public boolean isReuseAddr() {
        return this.reuseAddr;
    }


    public void setReuseAddr(final boolean reuseAddr) {
        this.reuseAddr = reuseAddr;
    }


    public void setTcpNoDelay(final boolean tcpNoDelay) {
        this.tcpNoDelay = tcpNoDelay;
    }


    public void setSoLinger(final boolean soLinger, final int linger) {
        this.soLinger = soLinger;
        this.linger = linger;
    }


    public boolean isSoLinger() {
        return this.soLinger;
    }


    public int getLinger() {
        return this.linger;
    }


    public int getReadBufferSize() {
        return this.readBufferSize;
    }


    public void setReadBufferSize(final int readBufferSize) {
        this.readBufferSize = readBufferSize;
    }


    public int getIdleTime() {
        return this.idleTime;
    }


    public void setIdleTime(final int idleTime) {
        this.idleTime = idleTime;
    }


    public int getMaxReadBufferSize() {
        return this.maxReadBufferSize;
    }


    public void setMaxReadBufferSize(final int maxReadBufferSize) {
        this.maxReadBufferSize = maxReadBufferSize;
    }


    public int getReadThreadCount() {
        return this.readThreadCount;
    }


    public void setReadThreadCount(final int readThreadCount) {
        this.readThreadCount = readThreadCount;
    }


    public int getDispatchMessageThreadCount() {
        return this.dispatchMessageThreadCount;
    }


    public void setDispatchMessageThreadCount(final int dispatchMessageThreadCount) {
        this.dispatchMessageThreadCount = dispatchMessageThreadCount;
    }


    public int getWriteThreadCount() {
        return this.writeThreadCount;
    }


    public void setWriteThreadCount(final int writeThreadCount) {
        this.writeThreadCount = writeThreadCount;
    }


    public int getRcvBufferSize() {
        return this.rcvBufferSize;
    }


    public void setRcvBufferSize(final int rcvBufferSize) {
        this.rcvBufferSize = rcvBufferSize;
    }


    public int getSndBufferSize() {
        return this.sndBufferSize;
    }


    public void setSndBufferSize(final int sndBufferSize) {
        this.sndBufferSize = sndBufferSize;
    }


    public int getSelectorPoolSize() {
        return this.selectorPoolSize;
    }


    public void setSelectorPoolSize(final int selectorPoolSize) {
        this.selectorPoolSize = selectorPoolSize;
    }


    @Override
    public String toString() {
        return "BaseConfig [callBackExecutorPoolSize=" + this.callBackExecutorPoolSize + ", callBackExecutorQueueSize="
                + this.callBackExecutorQueueSize + ", dispatchMessageThreadCount=" + this.dispatchMessageThreadCount
                + ", idleTime=" + this.idleTime + ", keepAlive=" + this.keepAlive + ", linger=" + this.linger
                + ", maxCallBackCountPerConnection=" + this.maxCallBackCount + ", maxCallBackExecutorPoolSize="
                + this.maxCallBackExecutorPoolSize + ", maxReadBufferSize=" + this.maxReadBufferSize
                + ", maxScheduleWrittenBytes=" + this.maxScheduleWrittenBytes + ", rcvBufferSize=" + this.rcvBufferSize
                + ", readBufferSize=" + this.readBufferSize + ", readThreadCount=" + this.readThreadCount
                + ", reuseAddr=" + this.reuseAddr + ", scanInvalidCallBackInterval=" + this.scanAllConnectionInterval
                + ", selectorPoolSize=" + this.selectorPoolSize + ", sndBufferSize=" + this.sndBufferSize
                + ", soLinger=" + this.soLinger + ", tcpNoDelay=" + this.tcpNoDelay + ", wireFormatType="
                + this.wireFormatType + ", writeThreadCount=" + this.writeThreadCount + "]";
    }

}