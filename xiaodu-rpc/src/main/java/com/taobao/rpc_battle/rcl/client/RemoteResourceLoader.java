
/**
 * xiaodu-rpc
 */
package com.taobao.rpc_battle.rcl.client;

import com.taobao.rpc_battle.rcl.ClassEntry;

/**
 * @author xiaodu
 *
 * ионГ10:41:59
 */
public interface RemoteResourceLoader {
	
	public ClassEntry findClassFile(String className)throws Exception;

}
