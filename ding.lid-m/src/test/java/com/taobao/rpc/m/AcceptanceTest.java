package com.taobao.rpc.m;

import com.taobao.rpc.api.RpcFactory;
import com.taobao.rpc.benchmark.dataobject.*;
import com.taobao.rpc.benchmark.service.HelloService;
import com.taobao.rpc.benchmark.service.HelloServiceImpl;
import com.taobao.rpc.m.MRpcFactory;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

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
        final RpcFactory factory = new MRpcFactory();
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

    @Test
    public void showCompress() throws Exception {

        compare("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890");
        compare(person);

        List<Person> list = new ArrayList<Person>();
        for(int i = 0; i<10; ++i)
        list.add(genPerson());

        compare(list);
    }

    static byte[] bytes = new byte[3000];
    static Random random = new Random();
    static {
        random.nextBytes(bytes);
    }

    static AtomicInteger counter = new AtomicInteger();

    private static Person genPerson() {
        Person person = new Person();
        person.setPersonId("id1");
        person.setLoginName("name1");
        person.setStatus(random.nextBoolean() ? PersonStatus.ENABLED : PersonStatus.DISABLED);

        byte[] clone = bytes.clone();
        clone[0] += counter.getAndIncrement();
        person.setAttachment(clone);

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
        info.setDepartment("mw");
        info.setHomepageUrl("www.taobao.com");
        info.setJobTitle("dev");
        info.setName("name2");

        person.setInfo(info);

        return person;
    }

    private static void scribblePerson(Person p) {
        p.setStatus(p.getStatus() == PersonStatus.ENABLED ? PersonStatus.DISABLED : PersonStatus.ENABLED);
        p.getAttachment()[0]++;
    }

    private void compare(Object obj) throws IOException {
        ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
        MRpcFactory.objectToBytes0(obj, baos1, true);

        ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
        MRpcFactory.objectToBytes0(obj, baos2, false);

        System.out.println(baos1.size() + ":" + baos2.size());
    }

}
