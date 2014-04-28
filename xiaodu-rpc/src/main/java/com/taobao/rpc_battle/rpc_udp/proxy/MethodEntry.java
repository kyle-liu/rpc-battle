
/**
 * xiaodu-rpc
 */
package com.taobao.rpc_battle.rpc_udp.proxy;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * @author xiaodu
 *
 * 下午1:23:40
 */
public class MethodEntry {

	private Method method;
	private String rpcName;
	private Type genericReturnType;
	private ArgumentEntry[] argumentEntries;

	public MethodEntry(Method method, String rpcName,
			Type genericReturnType, 
			ArgumentEntry[] argumentEntries) {
		this.method = method;
		this.rpcName = rpcName;
		this.genericReturnType = genericReturnType;
		this.argumentEntries = argumentEntries;
	}

	public Method getMethod() {
		return method;
	}

	public String getRpcName() {
		return rpcName;
	}

	public Type getGenericReturnType() {
		return genericReturnType;
	}

	public boolean isReturnTypeVoid() {
		return genericReturnType == void.class || genericReturnType == Void.class;
	}


	public ArgumentEntry[] getArgumentEntries() {
		return argumentEntries;
	}
	
	
    public static ArgumentEntry[] readArgumentEntries(Method targetMethod) {
    	
    	 Type[] types = targetMethod.getGenericParameterTypes();
    	  int paramsOffset = 0;
    	  List<ArgumentEntry> indexed = new ArrayList<ArgumentEntry>();
    	  for (int i = 0 + paramsOffset; i < types.length; i++) {
              Type t = types[i];
              indexed.add( new ArgumentEntry(i, t));
          }
    	
        return indexed.toArray(new  ArgumentEntry[0]);
    }
    
   


}
