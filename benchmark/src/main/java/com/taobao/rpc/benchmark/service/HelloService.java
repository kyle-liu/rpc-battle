package com.taobao.rpc.benchmark.service;

import com.taobao.rpc.benchmark.dataobject.Person;

/**
 * @author ding.lid
 */
public interface HelloService {
     Person helloPerson(Person in);
     String helloWorld(String in);
}
