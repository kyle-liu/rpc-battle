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
package com.taobao.gecko.core.nio.impl;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.FileChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.Future;

import com.taobao.gecko.core.buffer.IoBuffer;
import com.taobao.gecko.core.core.EventType;
import com.taobao.gecko.core.core.WriteMessage;
import com.taobao.gecko.core.core.impl.AbstractSession;
import com.taobao.gecko.core.core.impl.FileWriteMessage;
import com.taobao.gecko.core.core.impl.FutureImpl;
import com.taobao.gecko.core.core.impl.PoisonWriteMessage;
import com.taobao.gecko.core.nio.NioSession;
import com.taobao.gecko.core.nio.NioSessionConfig;
import com.taobao.gecko.core.util.SelectorFactory;


/**
 * Nio���ӳ������
 * 
 * 
 * 
 * @author boyan
 * 
 * @since 1.0, 2009-12-16 ����06:06:25
 */
public abstract class AbstractNioSession extends AbstractSession implements NioSession {

    public SelectableChannel channel() {
        return this.selectableChannel;
    }

    protected SelectorManager selectorManager;
    protected SelectableChannel selectableChannel;


    public AbstractNioSession(final NioSessionConfig sessionConfig) {
        super(sessionConfig);
        this.selectorManager = sessionConfig.selectorManager;
        this.selectableChannel = sessionConfig.selectableChannel;
    }


    /**
     * ע��OP_READ
     */
    public final void enableRead(final Selector selector) {
        final SelectionKey key = this.selectableChannel.keyFor(selector);
        if (key != null && key.isValid()) {
            this.interestRead(key);
        }
        else {
            try {
                this.selectableChannel.register(selector, SelectionKey.OP_READ, this);
            }
            catch (final ClosedChannelException e) {
                // ignore
            }
            catch (final CancelledKeyException e) {
                // ignore
            }
        }
    }


    // Ϊ����NioController�ɼ�
    @Override
    protected final void close0() {
        super.close0();
    }


    private void interestRead(final SelectionKey key) {
        if (key.attachment() == null) {
            key.attach(this);
        }
        int in=key.interestOps();
        if((in&SelectionKey.OP_READ)==0){
        	key.interestOps(in | SelectionKey.OP_READ);
        }
        
    }


    @Override
    protected void start0() {
        this.registerSession();
    }


    public InetAddress getLocalAddress() {
        return ((SocketChannel) this.selectableChannel).socket().getLocalAddress();
    }


    /**
     * ������д����Ϣ���ɱ��жϣ��жϿ����������ӶϿ���������ʹ��
     */
    public void writeInterruptibly(final Object packet) {
        if (packet == null) {
            throw new NullPointerException("Null packet");
        }
        if (this.isClosed()) {
            return;
        }
        final WriteMessage message = this.wrapMessage(packet, null);
        this.scheduleWritenBytes.addAndGet(message.remaining());
        this.write0(message);
    }


    /**
     * �������첽д����Ϣ���ɱ��жϣ��жϿ����������ӶϿ���������ʹ��
     */
    public Future<Boolean> asyncWriteInterruptibly(final Object packet) {
        if (packet == null) {
            throw new NullPointerException("Null packet");
        }
        if (this.isClosed()) {
            final FutureImpl<Boolean> writeFuture = new FutureImpl<Boolean>();
            writeFuture.failure(new IOException("�����Ѿ����ر�"));
            return writeFuture;
        }
        final FutureImpl<Boolean> writeFuture = new FutureImpl<Boolean>();
        final WriteMessage message = this.wrapMessage(packet, writeFuture);
        //this.scheduleWritenBytes.addAndGet(message.remaining());
        this.write0(message);
        return writeFuture;
    }


    private Object writeToChannel(final WriteMessage msg) throws ClosedChannelException, IOException {
        if (msg instanceof PoisonWriteMessage) {
            this.close0();
            return msg;
        }
        else {
            return this.writeToChannel0(msg);
        }
    }


    protected abstract Object writeToChannel0(WriteMessage msg) throws ClosedChannelException, IOException;


    protected boolean schduleWriteMessage(final WriteMessage writeMessage) {
        final boolean offered = this.writeQueue.offer(writeMessage);
        assert offered;
        final Reactor reactor = this.selectorManager.getReactorFromSession(this);
        if (Thread.currentThread() != reactor) {
            this.selectorManager.registerSession(this, EventType.ENABLE_WRITE);
            return true;
        }
        return false;
    }


    protected void onWrite(final SelectionKey key) {
        boolean isLockedByMe = false;
        if (this.currentMessage.get() == null) {
            // ��ȡ��һ����д��Ϣ
            final WriteMessage nextMessage = this.writeQueue.peek();
            if (nextMessage != null && this.writeLock.tryLock()) {
                if (!this.writeQueue.isEmpty() && this.currentMessage.compareAndSet(null, nextMessage)) {
                    this.writeQueue.remove();
                }
            }
            else {
                return;
            }
        }
        else if (!this.writeLock.tryLock()) {
            return;
        }
        this.updateTimeStamp();
        // ����ɹ�
        isLockedByMe = true;
        WriteMessage currentMessage = null;
        // ����ֵ��д����������ΪreadBufferSize*3/2�ܴﵽ������ܣ����Ҳ���̫Ӱ���д�Ĺ�ƽ��
        final long maxWritten = this.readBuffer.capacity() + this.readBuffer.capacity() >>> 1;
        try {
            long written = 0;
            while (this.currentMessage.get() != null) {
                currentMessage = this.currentMessage.get();
                currentMessage = this.preprocessWriteMessage(currentMessage);
                this.currentMessage.set(currentMessage);
                final long before = this.currentMessage.get().remaining();
                Object writeResult = null;
                // ���д�������С�����ֵ������д�������ж�write������ע��OP_WRITE
                if (written < maxWritten) {
                    writeResult = this.writeToChannel(currentMessage);
                    written += before - this.currentMessage.get().remaining();
                }
                else {
                    // ��д��,����ע��OP_WRITE
                }
                // ���ͳɹ�
                if (writeResult != null) {
                    this.currentMessage.set(this.writeQueue.poll());
                    if (currentMessage.isWriting()) {
                        this.onMessageSent(currentMessage);
                    }
                    // ȡ��һ����Ϣ����
                    if (this.currentMessage.get() == null) {
                        if (isLockedByMe) {
                            isLockedByMe = false;
                            this.writeLock.unlock();
                        }
                        // �ٳ���һ��
                        final WriteMessage nextMessage = this.writeQueue.peek();
                        if (nextMessage != null && this.writeLock.tryLock()) {
                            isLockedByMe = true;
                            if (!this.writeQueue.isEmpty() && this.currentMessage.compareAndSet(null, nextMessage)) {
                                this.writeQueue.remove();
                            }
                            continue;
                        }
                        else {
                            break;
                        }
                    }
                }
                else { // ����ȫд��
                    if (isLockedByMe) {
                        isLockedByMe = false;
                        this.writeLock.unlock();
                    }
                    // ����ע��OP_WRITE���ȴ�д
                    this.selectorManager.registerSession(this, EventType.ENABLE_WRITE);
                    break;
                }
            }
        }
        catch (final ClosedChannelException e) {
            this.close0();
            // ignore����֪ͨ�û�
            if (currentMessage != null && currentMessage.getWriteFuture() != null) {
                currentMessage.getWriteFuture().failure(e);
            }

        }
        catch (final Throwable e) {
            this.close0();
            this.handler.onExceptionCaught(this, e);
            if (currentMessage != null && currentMessage.getWriteFuture() != null) {
                currentMessage.getWriteFuture().failure(e);
            }

        }
        finally {
            if (isLockedByMe) {
                this.writeLock.unlock();
            }
        }
    }


    /**
     * ע��OP_WRITE
     */
    public final void enableWrite(final Selector selector) {
        final SelectionKey key = this.selectableChannel.keyFor(selector);
        if (key != null && key.isValid()) {
            this.interestWrite(key);
        }
        else {
            try {
                this.selectableChannel.register(selector, SelectionKey.OP_WRITE, this);
            }
            catch (final ClosedChannelException e) {
                // ignore
            }
            catch (final CancelledKeyException e) {
                // ignore
            }
        }
    }


    private void interestWrite(final SelectionKey key) {
        if (key.attachment() == null) {
            key.attach(this);
        }
        int in=key.interestOps();
        if((in&SelectionKey.OP_WRITE)==0){
        	key.interestOps(in | SelectionKey.OP_WRITE);
        }
    }


    protected void onRead(final SelectionKey key) {
        this.updateTimeStamp();
        this.readFromBuffer();
    }


    protected abstract void readFromBuffer();


    protected final void registerSession() {
        this.selectorManager.registerSession(this, EventType.REGISTER);
    }


    protected void unregisterSession() {
        this.selectorManager.registerSession(this, EventType.UNREGISTER);
    }


    @Override
    protected final void writeFromUserCode(final WriteMessage message) {
        if (this.schduleWriteMessage(message)) {
            return;
        }
        // �������ǰ�߳�һ����IO�߳�
        this.onWrite(null);
    }


    private void write0(WriteMessage message) {
        boolean isLockedByMe = false;
        Object writeResult = null;
        try {
            if (this.currentMessage.get() == null && this.writeLock.tryLock()) {
                isLockedByMe = true;
                if (this.currentMessage.compareAndSet(null, message)) {
                    message = this.preprocessWriteMessage(message);
                    this.currentMessage.set(message);
                    try {
                        writeResult = this.writeToChannel(message);
                    }
                    catch (final ClosedChannelException e) {
                        this.close0();
                        if (message.getWriteFuture() != null) {
                            message.getWriteFuture().failure(e);
                        }
                    }
                    catch (final Throwable e) {
                        this.close0();
                        if (message.getWriteFuture() != null) {
                            message.getWriteFuture().failure(e);
                        }
                        this.onException(e);
                    }
                }
                else {
                    isLockedByMe = false;
                    this.writeLock.unlock();
                }
            }
            // д��ɹ�
            if (isLockedByMe && writeResult != null) {
                if (message.isWriting()) {
                    this.onMessageSent(message);
                }
                // ��ȡ��һ��Ԫ��
                final WriteMessage nextElement = this.writeQueue.poll();
                if (nextElement != null) {
                    this.currentMessage.set(nextElement);
                    isLockedByMe = false;
                    this.writeLock.unlock();
                    // ע��OP_WRITE
                    this.selectorManager.registerSession(this, EventType.ENABLE_WRITE);
                }
                else {
                    this.currentMessage.set(null);
                    isLockedByMe = false;
                    this.writeLock.unlock();
                    // �ٴ�check
                    if (this.writeQueue.peek() != null) {
                        this.selectorManager.registerSession(this, EventType.ENABLE_WRITE);
                    }
                }
            }
            else {
                // д��ʧ��
                boolean isRegisterForWriting = false;
                if (this.currentMessage.get() != message) {
                    // �������
                    this.writeQueue.offer(message);
                    // �ж��Ƿ�����д��û�еĻ�����Ҫע��OP_WRITE
                    if (!this.writeLock.isLocked()) {
                        isRegisterForWriting = true;
                    }
                }
                else {
                    isRegisterForWriting = true;
                    if (isLockedByMe) {
                        isLockedByMe = false;
                        this.writeLock.unlock();
                    }
                }
                if (isRegisterForWriting) {
                    this.selectorManager.registerSession(this, EventType.ENABLE_WRITE);
                }
            }
        }
        finally {
            // ȷ���ͷ���
            if (isLockedByMe) {
                this.writeLock.unlock();
            }
        }
    }


    @Override
    protected void addPoisonWriteMessage(final PoisonWriteMessage poisonWriteMessage) {
        this.writeQueue.offer(poisonWriteMessage);
        this.selectorManager.registerSession(this, EventType.ENABLE_WRITE);
    }


    public void flush() {
        if (this.isClosed()) {
            return;
        }
        this.flush0();
    }


    protected final void flush0() {
        SelectionKey tmpKey = null;
        Selector writeSelector = null;
        int attempts = 0;
        try {
            while (true) {
                if (writeSelector == null) {
                    writeSelector = SelectorFactory.getSelector();
                    if (writeSelector == null) {
                        return;
                    }
                    tmpKey = this.selectableChannel.register(writeSelector, SelectionKey.OP_WRITE);
                }
                if (writeSelector.select(1000) == 0) {
                    attempts++;
                    if (attempts > 2) // ��ೢ��3��
                    {
                        return;
                    }
                }
                else {
                    break;
                }
            }
            this.onWrite(this.selectableChannel.keyFor(writeSelector));
        }
        catch (final ClosedChannelException cce) {
            // ignore
            this.close0();
        }
        catch (final Throwable t) {
            this.close0();
            this.onException(t);
            log.error("Flush error", t);
        }
        finally {
            if (tmpKey != null) {
                // Cancel the key.
                tmpKey.cancel();
                tmpKey = null;
            }
            if (writeSelector != null) {
                try {
                    writeSelector.selectNow();
                }
                catch (final IOException e) {
                    log.error("Temp selector selectNow error", e);
                }
                // return selector
                SelectorFactory.returnSelector(writeSelector);
            }
        }
    }


    /**
     * �ɷ�IO�¼�
     */
    public final void onEvent(final EventType event, final Selector selector) {

        final SelectionKey key = this.selectableChannel.keyFor(selector);

        switch (event) {
        case EXPIRED:
            this.onExpired();
            break;
        case WRITEABLE:
            this.onWrite(key);
            break;
        case READABLE:
            this.onRead(key);
            break;
        case ENABLE_WRITE:
            this.enableWrite(selector);
            break;
        case ENABLE_READ:
            this.enableRead(selector);
            break;
        case IDLE:
            //this.onIdle();
            break;
        case CONNECTED:
            this.onConnected();
            break;
        default:
            log.error("Unknown event:" + event.name());
            break;
        }
    }


    public void insertTimer(final TimerRef timerRef) {
        if (this.isClosed()) {
            return;
        }
        this.selectorManager.insertTimer(timerRef);
    }


    public Future<Boolean> asyncTransferFrom(final IoBuffer head, final IoBuffer tail, final FileChannel src,
            final long position, long size) {
        this.checkParams(src, position);
        size = this.normalSize(src, position, size);
        final FutureImpl<Boolean> future = new FutureImpl<Boolean>();
        final WriteMessage message = new FileWriteMessage(position, size, future, src, head, tail);
        this.scheduleWritenBytes.addAndGet(message.remaining());
        this.writeFromUserCode(message);
        return future;
    }


    private void checkParams(final FileChannel src, final long position) {
        if (src == null) {
            throw new NullPointerException("Null FileChannel");
        }
        try {
            if (position < 0 || position > src.size()) {
                throw new ArrayIndexOutOfBoundsException("Could not write position out of bounds of file channel");
            }
        }
        catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }


    public Future<Boolean> transferFrom(final IoBuffer head, final IoBuffer tail, final FileChannel src,
            final long position, long size) {
        this.checkParams(src, position);
        size = this.normalSize(src, position, size);
        final FutureImpl<Boolean> future = new FutureImpl<Boolean>();
        final WriteMessage message = new FileWriteMessage(position, size, future, src, head, tail);
        this.scheduleWritenBytes.addAndGet(message.remaining());
        this.writeFromUserCode(message);
        return future;
    }


    private long normalSize(final FileChannel src, final long position, long size) {
        try {
            size = Math.min(src.size() - position, size);
            return size;
        }
        catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

}