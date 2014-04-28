package com.taobao.rpc.benchmark.service;

import com.taobao.rpc.benchmark.dataobject.Person;

/**
 * @author ding.lid
 */
public class HelloServiceImpl implements HelloService {
    @Override
    public Person helloPerson(Person in) {
        return in;
    }
    @Override
    public String helloWorld(String in) {
        return in;
    }
}
