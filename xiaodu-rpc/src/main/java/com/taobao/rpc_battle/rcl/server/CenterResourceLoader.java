package com.taobao.rpc_battle.rcl.server;

import java.io.IOException;
import java.io.InputStream;

import org.apache.log4j.Logger;


/**
 * 
 * @author xiaodu
 * @version 
 */
public class CenterResourceLoader {
	
	private static final Logger logger = Logger.getLogger(CenterResourceLoader.class);

	private ClassLoader classLoader = null;
	
	public CenterResourceLoader(){
		this.classLoader = Thread.currentThread().getContextClassLoader();
	}
	
	public ResourceEntry findClassFileResource(String className) throws Exception{
		String classPath = changeClassNameToPath(className);
		
		ResourceEntry resource = findResource(classPath);
		if(resource != null){
			return resource;
		}
		return null;
	}
	
	
	
	
	private String changeClassNameToPath(String className){
		 String tempPath = className.replace('.', '/');
	     String classPath = tempPath + ".class";
	     return classPath;
	}
	
	
	
	public ResourceEntry findResource(String className) throws Exception{
		ResourceEntry e = null;
		if(e == null){
			e = lookupLocal(className);
		}
		if(e == null){
			Thread.currentThread().getContextClassLoader().getResourceAsStream(className);
		}
		if(e != null){
			return e;
		}
		
		throw new Exception();
	}
	
	/**
	 * 通过自身loader 查找资源，
	 * @param name
	 * @return
	 */
	private synchronized ResourceEntry lookupLocal(String name){
		
		ResourceEntry resourceEntry = new ResourceEntry();
		
		if(classLoader != null){
			InputStream binaryStream = classLoader.getResourceAsStream(name);
			if(binaryStream != null){
				byte[] binaryContent = new byte[10240];
				
				byte[] buffer = new byte[2048];
				int write_pos = 0;
				
				try {
					while (true) {
						int len = binaryStream.read(buffer, 0, buffer.length);
						if(len<0){
							break;
						}
						int space_available = binaryContent.length - write_pos; //剩余				
						if (space_available < len){
							int need_space = write_pos + len;
							byte[] new_buffer = new byte[need_space * 2];;
							System.arraycopy(binaryContent, 0, new_buffer, 0, write_pos);
							binaryContent = new_buffer;
						}
						System.arraycopy(buffer, 0, binaryContent, write_pos, len);
						write_pos += len;
					}
					
				} catch (IOException e) {
					return null;
				}finally{
					try {
						binaryStream.close();
					} catch (IOException e) {
					}
				} 
				resourceEntry.setBinaryContent(binaryContent) ;
				resourceEntry.setBinaryLength(write_pos);
				return resourceEntry;
			}
			
		}
		return null;
	}
	
	

	
	
	
	

}
