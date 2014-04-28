package com.taobao.rpc.bishan.net.util;

import java.util.concurrent.atomic.AtomicInteger;

public class SequenceIdGenerator {
	private static AtomicInteger sid = new AtomicInteger(Integer.MIN_VALUE);
	public static final int getNextSid() {
		return sid.incrementAndGet();
	}
}
