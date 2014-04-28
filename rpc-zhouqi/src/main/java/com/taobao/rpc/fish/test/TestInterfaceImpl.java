package com.taobao.rpc.fish.test;

import java.util.concurrent.atomic.AtomicLong;

public class TestInterfaceImpl implements TestInterface {
	@Override
	public String getName(long id) {
		// TODO Auto-generated method stub
		return "test-user";
	}

	@Override
	public User insertUser(String name, int age, String sex, String address,byte attach[]) {
		User user=new User();
		user.setName(name);
		user.setAge(age);
		user.setSex(sex);
		user.setAddress(address);
		user.setAttach(attach);
		//System.out.println("invoke count="+count.get());
		return user;
	}
	public User getUser(){
		User user=new User();
		user.setName("zz");
		user.setAge(26);
		user.setSex("ÄÐ");
		user.setAddress("");
		user.setAttach(new byte[4*1224]);
		return user;
	}
}
