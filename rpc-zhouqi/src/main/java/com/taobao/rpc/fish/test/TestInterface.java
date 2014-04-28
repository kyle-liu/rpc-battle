package com.taobao.rpc.fish.test;

public interface TestInterface {

	public String getName(long id);
	public User insertUser(String name,int age,String sex,String address,byte attach[]);
	public User getUser();
}
