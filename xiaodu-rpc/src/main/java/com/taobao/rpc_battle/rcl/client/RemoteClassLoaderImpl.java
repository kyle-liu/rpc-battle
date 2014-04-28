
/**
 * xiaodu-rpc
 */
package com.taobao.rpc_battle.rcl.client;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.CodeSigner;
import java.security.CodeSource;
import java.security.PrivilegedExceptionAction;
import java.util.Enumeration;

import com.taobao.rpc_battle.rcl.ClassEntry;



/**
 * @author xiaodu
 *
 * ����11:06:52
 */
public class RemoteClassLoaderImpl extends ClassLoader {
	
	private RemoteResourceLoader remoteResourceLoader;
	
	private AccessControlContext acc;
	
	public RemoteClassLoaderImpl(RemoteResourceLoader remoteResourceLoader){
		super(Thread.currentThread().getContextClassLoader());
		SecurityManager security = System.getSecurityManager();
		if (security != null) {
		    security.checkCreateClassLoader();
		}
		acc = AccessController.getContext();
		this.remoteResourceLoader = remoteResourceLoader;
	}

	@Override
	public InputStream getResourceAsStream(String name) {
		InputStream in = super.getResourceAsStream(name);
		if(in == null){
			ClassEntry classEntry = null;
			try {
				classEntry = remoteResourceLoader.findClassFile(name);
			} catch (Exception e) {
				return null;
			}
			if(classEntry !=null){
				in = new ByteArrayInputStream(classEntry.getClassContent());
				return in;
			}
		}
		return in;
	}


	@Override
	public Enumeration<URL> getResources(String name) throws IOException {
		return super.getResources(name);
	}


	/**
	 * 
	 * @param className
	 * @return
	 */
	public ClassEntry findClassEntryInClassCenter(String className){
		try {
			
			System.out.println("find class by class center:"+className);
			
			return remoteResourceLoader.findClassFile(className);
		} catch (Exception e) {
			return null;
		}
		
	}
	

	@Override
	protected Class<?> findClass(final String classname) throws ClassNotFoundException {
		try {
		    return 
			AccessController.doPrivileged(new PrivilegedExceptionAction<Class<?>>() {
			    public Class<?> run() throws ClassNotFoundException {
			    	return defineClass(classname);
			    }
			}, acc);
		} catch (java.security.PrivilegedActionException pae) {
		    throw (ClassNotFoundException) pae.getException();
		}
	}
	
	
	
	private Class<?> defineClass(String classname) throws  ClassNotFoundException {
		
		 try{
			 int i = classname.lastIndexOf('.');
			 if (i != -1) {
				    String pkgname = classname.substring(0, i);
				    // Check if package already loaded.
				    Package pkg = getPackage(pkgname);
				    if (pkg == null) {
				    	  definePackage(pkgname, null, null, null, null, null, null, null);
				    }
				}
			 
			 ClassEntry ce = findClassEntryInClassCenter(classname);
			if(ce == null){
				throw new ClassNotFoundException(classname+"class center can not find !");
			}		
			
			 //�� �����ⲿ���ڵ�������� slave ����jar��·����
			URL url = RemoteClassLoaderImpl.class.getProtectionDomain().getCodeSource().getLocation();
		    CodeSigner[] signers = RemoteClassLoaderImpl.class.getProtectionDomain().getCodeSource().getCodeSigners();
		    CodeSource cs = new CodeSource(url, signers);
			Class<?> clazz = this.defineClass(ce.getClassName(), ce.getClassContent(), 0, ce.getContentLen(),RemoteClassLoaderImpl.class.getProtectionDomain());
			return clazz;
		 }catch (Exception e) {
			throw new ClassNotFoundException(classname+"class center can not find !");
		}
		 
	 }

	@Override
	protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
		
		return super.loadClass(name, resolve);
	}
	
	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		return loadClass(name,false);
	}





}
