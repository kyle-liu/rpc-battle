
/**
 * xiaodu-rpc
 */
package com.taobao.rpc_battle.rpc.proxy;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtNewConstructor;
import javassist.CtNewMethod;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;


/**
 * @author xiaodu
 *
 * ÏÂÎç5:07:37
 */
public class Proxy {
	
	 private static <T> T createJavassistBytecodeDynamicProxy(Class<T> clazz) throws Exception {  
	        ClassPool mPool = new ClassPool(true);  
	        CtClass mCtc = mPool.makeClass(clazz.getName() + "JavaassistProxy");  
	        mCtc.addInterface(mPool.get(clazz.getName()));  
	        mCtc.addConstructor(CtNewConstructor.defaultConstructor(mCtc));  
	        
	        
	        
	        mCtc.addMethod(CtNewMethod.make("public int count() { return delegate.count(); }", mCtc));  
	        Class<T> pc = mCtc.toClass();  
	        T bytecodeProxy = (T) pc.newInstance();  
	        return bytecodeProxy;  
	    }  

	private static <T> T  createAsmBytecodeDynamicProxy(Class<T> clazz) throws Exception {  
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);  
        String className = clazz.getName() +  "AsmProxy";  
        String classPath = className.replace('.', '/');  
        String interfacePath =clazz.getName().replace('.', '/');  
        classWriter.visit(Opcodes.V1_5, Opcodes.ACC_PUBLIC, classPath, null, "java/lang/Object", new String[] {interfacePath});  
          
        MethodVisitor initVisitor = classWriter.visitMethod(Opcodes.ACC_PUBLIC, "&lt;init&gt;", "()V", null, null);  
        initVisitor.visitCode();  
        initVisitor.visitVarInsn(Opcodes.ALOAD, 0);  
        initVisitor.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "&lt;init&gt;", "()V");  
        initVisitor.visitInsn(Opcodes.RETURN);  
        initVisitor.visitMaxs(0, 0);  
        initVisitor.visitEnd();  
          
        FieldVisitor fieldVisitor = classWriter.visitField(Opcodes.ACC_PUBLIC, "delegate", "L" + interfacePath + ";", null, null);  
        fieldVisitor.visitEnd();  
          
        MethodVisitor methodVisitor = classWriter.visitMethod(Opcodes.ACC_PUBLIC, "count", "()I", null, null);  
        methodVisitor.visitCode();  
        methodVisitor.visitVarInsn(Opcodes.ALOAD, 0);  
        methodVisitor.visitFieldInsn(Opcodes.GETFIELD, classPath, "delegate", "L" + interfacePath + ";");  
        methodVisitor.visitMethodInsn(Opcodes.INVOKEINTERFACE, interfacePath, "count", "()I");  
        methodVisitor.visitInsn(Opcodes.IRETURN);  
        methodVisitor.visitMaxs(0, 0);  
        methodVisitor.visitEnd();  
          
        classWriter.visitEnd();  
        byte[] code = classWriter.toByteArray();  
        T bytecodeProxy = (T) new ByteArrayClassLoader().getClass(className, code).newInstance();  
        Field filed = bytecodeProxy.getClass().getField("delegate");  
        filed.set(bytecodeProxy, "");  
        return bytecodeProxy;  
    }  
	
	
	
//	private List<Method> getClassMethods(Class clazz){
//		
//		Method[] methods = clazz.getMethods();
//		
//		
//	}
	
	
	private static class ByteArrayClassLoader extends ClassLoader {  
		  
        public ByteArrayClassLoader() {  
            super(ByteArrayClassLoader.class.getClassLoader());  
        }  
  
        public synchronized Class getClass(String name, byte[] code) {  
            if (name == null) {  
                throw new IllegalArgumentException("");  
            }  
            return defineClass(name, code, 0, code.length);  
        }  
  
    }  
}
