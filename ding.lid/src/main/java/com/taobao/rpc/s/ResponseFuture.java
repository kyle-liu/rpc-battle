package com.taobao.rpc.s;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ResponseFuture {
        private static Map<Integer, ResponseFuture> responseFutures = new ConcurrentHashMap<Integer, ResponseFuture>();

        public static ResponseFuture findResponseFuture(Integer id) {
            return responseFutures.get(id);
        }

        public static ResponseFuture createResponseFuture(Integer id) {
            ResponseFuture f = new ResponseFuture(id);
            responseFutures.put(id, f);
            return f;
        }

        private final CountDownLatch latch;
        private volatile Object result;
        private final Integer id;

        public ResponseFuture(Integer id) {
            this.id = id;
            latch = new CountDownLatch(1);
        }

        public void done(Object result) {
            this.result = result;
            responseFutures.remove(id);
            latch.countDown();
        }

        public Object get() throws InterruptedException {
            latch.await();
            return result;
        }

        public Object get(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
            boolean isTimeout = !latch.await(timeout, unit);
            if (isTimeout){
                throw new TimeoutException();
            }
            return result;
        }
    }
