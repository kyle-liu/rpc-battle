package com.taobao.rpc.fish.server;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;


import net.sf.cglib.reflect.FastClass;
import net.sf.cglib.reflect.FastMethod;

import com.taobao.rpc.fish.common.util.MD5;
/**
 * 服务注册器
 * @author zhouqi.zhm
 *
 */
public class ServerRegister {
	public Map<String, RegInfo> regMap=new HashMap<String, RegInfo>();
	public Map<MethodDigest, RegInfo> methodRegMap=new HashMap<ServerRegister.MethodDigest, RegInfo>();
	public Map<String, Class> classMap=new HashMap<String, Class>();
	public boolean registe(String interfaceName,Object target)throws ClassNotFoundException{
		if(regMap.containsKey(interfaceName)){
			return false;
		}
		Class cl=Class.forName(interfaceName);
		if(!cl.isInterface()){
			throw new RuntimeException("必须为接口："+interfaceName);
		}
		if(target==null){
			throw new RuntimeException("target不能为空");
		}
		if(!cl.isAssignableFrom(target.getClass())){
			throw new RuntimeException("target不是"+interfaceName+"的实现对象!");
		}
		this.registMethods(cl, target);
		return true;
	}
	public RegInfo find(String methodHexDigest){
		return regMap.get(methodHexDigest);
	}
	public RegInfo find(MethodDigest method){
		return methodRegMap.get(method);
	}
	public RegInfo remove(String methodHexDigest){
		return regMap.remove(methodHexDigest);
	}
	
	private void registMethods(Class cl,Object target){
		FastClass fastClass=FastClass.create(cl);
		Method methods[]=cl.getMethods();
		String name=cl.getName();
		for(int i=0;i<methods.length;i++){
			this.registMethod(fastClass,name, methods[i],target);
		}
	}
	private void registMethod(FastClass pcl,String interfaceName,Method method,Object target){
		String methodName=method.getName();
		Class cls[]=method.getParameterTypes();
		StringBuilder builder=new StringBuilder();
		builder.append(interfaceName);
		builder.append(methodName);
		for(Class cl:cls){
			builder.append(cl.getName());
		}
		byte digest[]=MD5.getDigest(builder.toString());
		MethodDigest methodDigest=new MethodDigest();
		methodDigest.digest=digest;
		String hexDigest=MD5.toHex(digest);
		RegInfo info=new RegInfo();
		info.target=target;
		info.interfaceName=interfaceName;
		info.method=method;
		info.fastMethod=pcl.getMethod(method);
		regMap.put(hexDigest, info);
		methodRegMap.put(methodDigest, info);
		
	}
	public class RegInfo{
		public Object target;
		public String interfaceName;
		public Method method;
		public FastMethod fastMethod;
		public byte digest[];
		public Object methodInvoke(Object params[]) throws Exception{
			return fastMethod.invoke(target, params);
		}
	}
	public static class MethodDigest{
		public byte digest[];
		@Override
		public int hashCode(){
			int code=digest.length;
			for(int i=0;i<digest.length;i++){
				code=code+digest[i];
			}
			return code;
		}
		@Override
		public boolean equals(Object o){
			if(o==null)return false;
			try {
				if(!(o instanceof MethodDigest)){
					return false;
				}
				byte target[]=((MethodDigest)o).digest;
				if(target.length!=digest.length){
					return false;
				}
				for(int i=0;i<digest.length;i++){
					if(digest[i]!=target[i]){
						return false;
					}
				}
				return true;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				return false;
			}
		}
	}
}
