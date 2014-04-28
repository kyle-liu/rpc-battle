package com.taobao.rpc.bishan.net.msg;

import java.lang.reflect.Method;

public class RequstPackage extends AbstractPackage{

	private String className;
	private String methodName;
	private Class[] parameterClasses;
	private Object[] parameters;
	public RequstPackage(){
		
	}
	public RequstPackage(String className,Method method,
			Object[] parameter){
		this.className=className;
		 this.methodName = method.getName();
		 this.parameterClasses = method.getParameterTypes();
		 this.parameters = parameter;
	}

	public String getClassName() {
		return className;
	}

	public String getMethodName() {
		return methodName;
	}

	public Class[] getParameterClasses() {
		return parameterClasses;
	}

	public Object[] getParameters() {
		return parameters;
	}

	@Override
	public int version() {
		// TODO Auto-generated method stub
		return 0;
	}
}
