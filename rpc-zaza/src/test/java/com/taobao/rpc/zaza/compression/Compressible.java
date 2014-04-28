/*--------------------------------------------------------------------------
 *  Copyright (c) 2012 by Institute of Computing Technology, 
 *                          Chinese Academic of Sciences, Beijing, China.
 *--------------------------------------------------------------------------*/
//--------------------------------------
// Jeoygin Wang
//
// Compressible.java
// Since: 2011-12-16
//
//--------------------------------------
package com.taobao.rpc.zaza.compression;

import java.io.InputStream;
import java.io.OutputStream;

public interface Compressible {
    byte[] compress(byte[] data) throws Exception;
    void compress(String path) throws Exception;
    void compress(String path, boolean delete) throws Exception;
    void compress(String srcPath, String dstPath) throws Exception;
    void compress(String srcPath, String dstPath,  boolean delete) throws Exception;
    void compress(InputStream is, OutputStream os) throws Exception;
}
