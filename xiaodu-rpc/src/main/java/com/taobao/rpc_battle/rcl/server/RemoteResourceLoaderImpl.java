
/**
 * xiaodu-rpc
 */
package com.taobao.rpc_battle.rcl.server;

import java.util.HashMap;
import java.util.Map;

import com.taobao.rpc_battle.rcl.ClassEntry;
import com.taobao.rpc_battle.rcl.client.RemoteResourceLoader;

/**
 * @author xiaodu
 *
 * ÉÏÎç10:42:53
 */
public class RemoteResourceLoaderImpl implements RemoteResourceLoader{
	
	private Map<String, Object> objectMap = new HashMap<String, Object>();
	
	private CenterResourceLoader centerResourceLoader = new CenterResourceLoader();
	
	@Override
	public ClassEntry findClassFile(String classname) throws Exception {
		Object object = objectMap.get(classname);
		if(object != null){
			String name = object.getClass().getCanonicalName();
			ResourceEntry re = centerResourceLoader.findClassFileResource(name);
			ClassEntry classEntry = new ClassEntry();
			classEntry.setClassName(name);
			classEntry.setClassContent(re.getBinaryContent());
			classEntry.setContentLen(re.getBinaryLength());
			return classEntry;
		}else{
			ResourceEntry re =  centerResourceLoader.findClassFileResource(classname);
			ClassEntry classEntry = new ClassEntry();
			classEntry.setClassName(classname);
			classEntry.setClassContent(re.getBinaryContent());
			classEntry.setContentLen(re.getBinaryLength());
			return classEntry;
		}
	}
	
	
	public void registClass(Class claszz,Object object){
		objectMap.put(claszz.getCanonicalName(), object);
	}


}
