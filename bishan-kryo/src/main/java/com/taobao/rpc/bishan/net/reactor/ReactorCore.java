package com.taobao.rpc.bishan.net.reactor;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import com.taobao.rpc.bishan.net.msg.WriteObj;
import com.taobao.rpc.bishan.net.util.BsThreadPool;

/**
 * Reactor模式处理NIO请求和消息
 * 
 * @author bishan.ct
 * 
 */
public class ReactorCore extends Thread {
	public static final int BUFFER_SIZE = 16 * 1024;

	final Selector nioSelector;
	protected final Queue<Runnable> tasks = new LinkedBlockingQueue<Runnable>();
	Thread reactorThread;
	private static final int CLEANUP_INTERVAL = 256;
	private volatile int cancelledKeys;
	protected int selectTime = 10;

    protected final AtomicBoolean wakenUp = new AtomicBoolean();
    
	public ReactorCore() throws IOException {
		this.nioSelector = Selector.open();

		startReactor();
	}

	protected void startReactor() {
		BsThreadPool.REACTOR_POOL.execute(this);
	}

	/**
	 * 向这个reactor注册通道的读事件
	 * 
	 * @param netConnect
	 */
	public void register(AbstractBsNet netConnect) {
		final BsNetClient clinetNet = (BsNetClient) netConnect;
		tasks.offer(new Runnable() {
			public void run() {
				try {
					if (clinetNet.isServer) {
						clinetNet.socketChannel.configureBlocking(false);
					}

					clinetNet.socketChannel.register(nioSelector,
							SelectionKey.OP_READ, clinetNet);
				} catch (IOException e) {
					// Failed to register a socket to the selector.
					clinetNet.close();
				}
			}
		});
	}

	@Override
	public void run() {
		this.reactorThread = Thread.currentThread();
		Thread.currentThread().setName(
				"BS REACTOR THREAD#" + BsThreadPool.BOSS_COUNT.getAndIncrement());

		for (;;) {
			try {
				dealTaskQueue();
	            wakenUp.set(false);
				int selected = nioSelector.select(selectTime);
				 wakenUp.set(true);
				if (selected == 0) {
					continue;
				}
				processSelectedKeys();
			} catch (IOException exc) {
				// TODO
				// logger.warn("Unexpected exception in the selector loop.", t);

				// Prevent possible consecutive immediate failures that lead to
				// excessive CPU consumption.
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// Ignore.
				}
			}
		}
	}

	/**
	 * 处理task，包括 1.注册连接
	 * 
	 */
	private void dealTaskQueue() {
		while (true) {
			Runnable task = this.tasks.poll();
			if (task == null)
				break;
			try {
				task.run();
				cleanUpCancelledKeys();
			} catch (Throwable cause) {
				// TODO LOG
			}
		}
	}

	/**
	 * 有感兴趣的事件到达，处理..
	 */
	private final void processSelectedKeys() {
		Iterator<SelectionKey> iter = nioSelector.selectedKeys().iterator();

		while (iter.hasNext()) {
			SelectionKey key = iter.next();
			iter.remove();
			AbstractBsNet ch = (AbstractBsNet) key.attachment();
			try{
				handleKey(key,ch);
			}catch(CancelledKeyException ce){
				close(key);
			}
		}
	}

	void handleKey(SelectionKey key,AbstractBsNet ch) {
		if (key.isReadable()) { // 读信息
			onReadyToRead(key);
		}
		if (key.isWritable()) { // 写事件
			writeInReactor((BsNetClient)ch);
		}

	}

//	protected ByteBuffer clientBuffer = ByteBuffer.allocate(BsThreadPool.MAX_MESSAGE);
	protected ByteBuffer clientBuffer = 
		ByteBuffer.allocateDirect(BsThreadPool.MAX_MESSAGE);
	private void onReadyToRead(SelectionKey key) {
		clientBuffer.clear();
		final BsNetClient ch = (BsNetClient) key.attachment();

		int count = 0;
		// 从客户端读过来的数据块
		try {
			count = ch.socketChannel.read(clientBuffer);
		} catch (IOException io) {
			ch.onException(io);
		}

		if (count > 0) {
			clientBuffer.flip();
			ch.byteStreamDecoder.decodeMsg(ch,clientBuffer);
		} else {
			// 如客户端没有可读事件，关闭管道
			close(key);
		}
	}

	/**
	 * 用户线程写入数据 也有可能在读到数据的时候直接写入，是reactor线程
	 * 
	 * @param ch
	 * @param obj
	 */
	protected void writeInUser(BsNetClient ch, WriteObj wobj) {
		ch.writeBufferQueue.offer(wobj);

		if (scheduleWriteIfNecessary(ch)) {
			return;
		}
		if (ch.inWriteNowLoop) {
			return;
		}
		writeInReactor(ch);
	}

	/**
	 * 没有完善好
	 * 1.可以加个writeBuffer，不用每次创建
	 * @param ch
	 */
	protected void writeInReactor(BsNetClient ch) {
		boolean open = true;
		boolean addOpWrite = false;
		boolean removeOpWrite = false;
		
		synchronized (ch.writeLock) {
			ch.inWriteNowLoop = false;

			for (;;) {
				WriteObj evt = ch.currentWriteMsg;
				try {
					if (evt == null) {
						if ((evt = ch.writeBufferQueue.poll()) == null) {
							removeOpWrite = true;
							break;
						}
					}
					
					int dataLength=evt.getObjBytes().length;	//数据的真实长度
					int byteWithLength=0;	//还没有写出去的总长度
					
					ByteBuffer wbf=null;
					
					int writtenBytes = evt.getWritedOutLength();	//已经写出去的长度
					byteWithLength = dataLength - writtenBytes;
					wbf = ByteBuffer.allocate(byteWithLength);
					System.arraycopy(evt.getObjBytes(), writtenBytes, wbf.array(),0, byteWithLength);
					wbf.limit(byteWithLength);
					wbf.position(0);
					
					int tmpWrite=0;
					int localWrittenBytes = 0;			//本次写出去的数据总长度
					for (int i = BsThreadPool.writeSpinCount; i > 0; i--) {
						tmpWrite = ch.socketChannel.write(wbf);
						localWrittenBytes=localWrittenBytes+tmpWrite;
						if (localWrittenBytes == byteWithLength) {
							break;
						} else {
							writtenBytes += tmpWrite;
						}
					}
					evt.setWriteOutLength(writtenBytes);

					if (wbf.remaining() == 0) {
						evt.getWriteFuture().setDone();
						ch.currentWriteMsg = null;
					} else {
						ch.currentWriteMsg=evt;
						addOpWrite = true;
						break;
					}
				} catch (Exception e) {
					if (evt != null) {
						evt.getWriteFuture().setException(e);
					}
					ch.currentWriteMsg = null;
					ch.onException(e);
					SelectionKey sk=ch.socketChannel.keyFor(nioSelector);
					
					if (e instanceof IOException) {
						close(sk);
						open=false;
					}
				}
			}
			ch.inWriteNowLoop = false;

			if (open) {
				if (addOpWrite) {
					setOpWrite(ch.socketChannel);
				} else if (removeOpWrite) {
					clearOpWrite(ch.socketChannel);
				}
			}
		}
	}

	public static void main(String[] args){
		int i=1000;
		i+=10;
		System.out.println(i);
	}
	
	protected String reactorName="connectChannel";
	
	protected void setOpWrite(SocketChannel channel) {

		SelectionKey key = channel.keyFor(nioSelector);
		if (key == null) {
			return;
		}
		if (!key.isValid()) {
			close(key);
			return;
		}
		int interestOps = key.interestOps();

		if ((interestOps & SelectionKey.OP_WRITE) == 0) {
			interestOps |= SelectionKey.OP_WRITE;
			key.interestOps(interestOps);
		}
	}

	protected void clearOpWrite(SocketChannel channel) {

		SelectionKey key = channel.keyFor(nioSelector);
		if (key == null) {
			return;
		}
		if (!key.isValid()) {
			close(key);
			return;
		}
		int interestOps = key.interestOps();

		if ((interestOps & SelectionKey.OP_WRITE) != 0) {
			interestOps &= ~SelectionKey.OP_WRITE;
			key.interestOps(interestOps);
		}
	}

	protected void close(SelectionKey k) {
		BsNetClient ch = (BsNetClient) k.attachment();
		//内部会调用k.cancel()
		ch.close();
		//k.cancel();
		cancelledKeys++;
		cleanUpCancelledKeys();
		System.out.println(ch.isServer+" close method:"+k);
	}

	/**
	 * 是reactor线程，直接调用write 不是reactor，判断是否要加入writeTask
	 * 
	 * @param ch
	 * @return
	 */
	protected boolean scheduleWriteIfNecessary(final BsNetClient ch) {
		final Thread currentThread = Thread.currentThread();
		final Thread workerThread = reactorThread;
		if (currentThread != workerThread) {
			if (ch.writeTaskInTaskQueue.compareAndSet(false, true)) {
				tasks.add(ch.writeTask);
				
				if (wakenUp.compareAndSet(false, true)) {
	                nioSelector.wakeup();
	            }
			}
			return true;
		}

		return false;
	}

	protected final boolean cleanUpCancelledKeys() {
		try {
			if (cancelledKeys >= CLEANUP_INTERVAL) {
				cancelledKeys = 0;
				nioSelector.selectNow();

				return true;
			}
		} catch (IOException e) {
			// select exception
		}
		return false;
	}
}
