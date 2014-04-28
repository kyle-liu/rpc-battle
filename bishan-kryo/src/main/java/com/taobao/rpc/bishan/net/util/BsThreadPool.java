package com.taobao.rpc.bishan.net.util;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.AbortPolicy;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 一些公用的线程资源
 * @author bishan.ct
 *
 */
public class BsThreadPool {

	/**
	 * REACTOR数量
	 */
	public static final int IO_THREADS = 1<<3;
	public static final int AND_IO_THREADS = IO_THREADS-1;
//	public static final int DEFAULT_IO_THREADS = 
//		Runtime.getRuntime().availableProcessors() * 2;
	

	public static final int MAX_MESSAGE = 64*1024;
	public final static int writeSpinCount = 16;
	/**
	 * 每次select的时间
	 */
	public static final long DEFAULT_SELECT_TIMEOUT = 10;
	public static final long SELECT_TIMEOUT_NANOS = 
		TimeUnit.MILLISECONDS.toNanos(DEFAULT_SELECT_TIMEOUT);
	
	
	public static final AtomicInteger BOSS_COUNT=new AtomicInteger();
	public static final AtomicInteger REACTOR_COUNT=new AtomicInteger();
	 private static final RejectedExecutionHandler defaultHandler =
	        new AbortPolicy();
	/**
	 * reactor线程池资源
	 */
	public static final Executor REACTOR_POOL=Executors.newCachedThreadPool();

	/**
	 * 4个处server处理线程
	 */
	public static final ThreadPoolExecutor DEAL_POOL=new ThreadPoolExecutor(4, 4,
            60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>(),defaultHandler);
	
	public static void main(String[] args){
		System.out.println(IO_THREADS);
		System.out.println(1<<1);
	}
}
