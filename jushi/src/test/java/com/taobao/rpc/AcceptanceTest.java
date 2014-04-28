package com.taobao.rpc;

import com.taobao.rpc.api.RpcFactory;
import com.taobao.rpc.benchmark.service.HelloService;
import com.taobao.rpc.benchmark.service.HelloServiceImpl;
import java.util.concurrent.CountDownLatch;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/** @author <a href="mailto:jushi@taobao.com">jushi<a> */
public class AcceptanceTest {
    @Test
    public void shouldBeDone() throws Exception {
        final RpcFactory factory = new OneRpcFactory();
        final CountDownLatch latch = new CountDownLatch(1);
        new Thread(new Runnable() {
            @Override
            public void run() {
                factory.export(HelloService.class, new HelloServiceImpl());
                latch.countDown();
            }
        }).start();

        latch.await();
        HelloService ref = factory.getReference(HelloService.class, "localhost");

        Object feedback = ref.helloWorld("there");

        assertThat(feedback.toString(), is("there"));
    }

}
