package com.taobao.rpc.fish.test;

import java.io.Serializable;

public class User implements Serializable{

	private String name;
	private int age;
	private String sex;
	private String address;
	private byte attach[];
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getAge() {
		return age;
	}
	public void setAge(int age) {
		this.age = age;
	}
	public String getSex() {
		return sex;
	}
	public void setSex(String sex) {
		this.sex = sex;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public byte[] getAttach() {
		return attach;
	}
	public void setAttach(byte[] attach) {
		this.attach = attach;
	}
	
}
