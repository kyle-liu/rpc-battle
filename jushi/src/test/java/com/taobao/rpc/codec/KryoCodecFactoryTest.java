package com.taobao.rpc.codec;

import com.taobao.rpc.benchmark.dataobject.*;
import com.taobao.rpc.service.Request;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.Random;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/** @author <a href="mailto:jushi@taobao.com">jushi<a> */
public class KryoCodecFactoryTest {
    @Test
    public void shouldCodecString() throws Exception {
        Codec codec = new KryoCodecFactory().create();
        String s = "abc";
        ByteBuf encode = codec.encode(s).copy();
        encode.skipBytes(4);
        codec.prepare(encode);
        assertThat(codec.decode(String.class), is(s));
    }

    @Test
    public void shouldCodecFlex() throws Exception {
        Codec codec = new KryoCodecFactory().create();
        ByteBuf encode = codec.encode(new Request(2, new Object[]{person})).copy();
        encode.skipBytes(4);
        codec.prepare(encode);
        Request request = codec.decode(Request.class);
        assertThat(request.id(), is(1L));
        assertThat(request.serviceIndex(), is(2));
        assertThat((Person) request.args()[0], is(person));
    }

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

}
