//
// MessagePack-RPC for Java
//
// Copyright (C) 2010 FURUHASHI Sadayuki
//
//    Licensed under the Apache License, Version 2.0 (the "License");
//    you may not use this file except in compliance with the License.
//    You may obtain a copy of the License at
//
//        http://www.apache.org/licenses/LICENSE-2.0
//
//    Unless required by applicable law or agreed to in writing, software
//    distributed under the License is distributed on an "AS IS" BASIS,
//    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//    See the License for the specific language governing permissions and
//    limitations under the License.
//
package com.taobao.rpc_battle.rpc_udp.proxy;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.List;

public abstract class ProxyBuilder {

	// Override this method
	public abstract <T> Proxy<T> buildProxy(Class<T> iface, MethodEntry[] entries);

	public <T> Proxy<T> buildProxy(Class<T> iface) {
		checkValidation(iface);
		MethodEntry[] entries = readMethodEntries(iface);
		return buildProxy(iface, entries);
	}

	private static void checkValidation(Class<?> iface) {
		if(!iface.isInterface()) {
			throw new IllegalArgumentException("not interface: "+iface);
		}
		// TODO
	}

	static MethodEntry[] readMethodEntries(Class<?> iface) {
		Method[] methods = selectRpcClientMethod(iface);

		MethodEntry[] result = new MethodEntry[methods.length];
		for(int i=0; i < methods.length; i++) {
			Method method = methods[i];

			ArgumentEntry[] argumentEntries =MethodEntry.readArgumentEntries(method);
			String rpcName = method.getName();
			Type returnType = method.getGenericReturnType();
			result[i] = new MethodEntry(method, rpcName,
					returnType,  argumentEntries);
		}

		return result;
	}
	
	
  public static Method[] selectRpcClientMethod(Class<?> iface) {
  List<Method> methods = new ArrayList<Method>();
  for (Method method : iface.getMethods()) {
      if (isRpcMethod(method)) {
          methods.add(method);
      }
  }
  return methods.toArray(new Method[0]);
}
  
  
  private static boolean isRpcMethod(Method method) {
      int mod = method.getModifiers();
      if (Modifier.isStatic(mod)) {
          return false;
      }
      if (!Modifier.isPublic(mod)) {
          return false;
      }
      return true;
  }
}

