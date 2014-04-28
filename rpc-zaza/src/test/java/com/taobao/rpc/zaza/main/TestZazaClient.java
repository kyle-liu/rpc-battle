package com.taobao.rpc.zaza.main;

import java.util.ArrayList;
import java.util.Random;

import org.junit.Test;

import com.taobao.rpc.api.RpcFactory;
import com.taobao.rpc.benchmark.dataobject.FullAddress;
import com.taobao.rpc.benchmark.dataobject.Person;
import com.taobao.rpc.benchmark.dataobject.PersonInfo;
import com.taobao.rpc.benchmark.dataobject.PersonStatus;
import com.taobao.rpc.benchmark.dataobject.Phone;
import com.taobao.rpc.benchmark.service.HelloService;
import com.taobao.rpc.zaza.ZazaRpcFactory;

public class TestZazaClient {
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
    public void testZazaClientString() {
        RpcFactory factory = new ZazaRpcFactory();
        System.out.println(factory.getReference(HelloService.class, "127.0.0.1").helloWorld("kongming"));
    }

    @Test
    public void testZazaClientPojo() {
        RpcFactory factory = new ZazaRpcFactory();
        System.out.println(factory.getReference(HelloService.class, "127.0.0.1").helloPerson(person));
    }

}
