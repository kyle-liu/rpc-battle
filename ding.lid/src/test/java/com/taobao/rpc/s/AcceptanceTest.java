package com.taobao.rpc.s;

import com.taobao.rpc.api.RpcFactory;
import com.taobao.rpc.benchmark.dataobject.*;
import com.taobao.rpc.benchmark.service.HelloService;
import com.taobao.rpc.benchmark.service.HelloServiceImpl;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/** @author <a href="mailto:jushi@taobao.com">jushi<a> */
public class AcceptanceTest {
    static final String constant = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890";
    static final Person person;
    static {
        person = new Person();
        person.setPersonId("id1");
        person.setLoginName("name1");
        person.setStatus(PersonStatus.ENABLED);

        byte[] attachment = new byte[4000]; // 4K
        Random random = new Random();
        random.nextBytes(attachment);
        person.setAttachment(attachment);

        ArrayList<Phone> phones = new ArrayList<Phone>();
        Phone phone1 = new Phone("86", "0571", "11223344", "001");
        Phone phone2 = new Phone("86", "0571", "11223344", "002");
        phones.add(phone1);
        phones.add(phone2);

        PersonInfo info = new PersonInfo();
        info.setPhones(phones);
        Phone fax = new Phone("86", "0571", "11223344", null);
        info.setFax(fax);
        FullAddress addr = new FullAddress("CN", "zj", "1234", "Road1", "333444");
        info.setFullAddress(addr);
        info.setMobileNo("1122334455");
        info.setMale(true);
        info.setDepartment("b2b");
        info.setHomepageUrl("www.abc.com");
        info.setJobTitle("dev");
        info.setName("name2");

        person.setInfo(info);
    }

    @Test
    public void shouldBeDone() throws Exception {
        final RpcFactory factory = new SRpcFactory();
        final CountDownLatch latch = new CountDownLatch(1);
        new Thread(new Runnable() {
            @Override
            public void run() {
                factory.export(HelloService.class, new HelloServiceImpl());
                System.out.println("serving...");
                latch.countDown();
            }
        }).start();

        latch.await();
        HelloService ref = factory.getReference(HelloService.class, "localhost");

        Object feedback = ref.helloWorld("there");
        assertThat(feedback.toString(), is("there"));

        Person p = ref.helloPerson(person);
        assertEquals(person, p);
    }
}
