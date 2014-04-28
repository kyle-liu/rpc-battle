package com.taobao.rpc.zaza;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class GenericInfoTest {

    @Test
    public void testGenericTest() {
        List<ZazaRequest> list = new ArrayList<ZazaRequest>();
        System.out.println(list.getClass().getSigners());
    }

    @Test
    public void testGenericByteTest() {
        byte i = 127;
        i++;
        i++;
        System.out.println(i+256);
    }

}
