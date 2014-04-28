/*--------------------------------------------------------------------------
 *  Copyright (c) 2012 by Institute of Computing Technology, 
 *                          Chinese Academic of Sciences, Beijing, China.
 *--------------------------------------------------------------------------*/
//--------------------------------------
// Ruijian Wang
//
// Decompressible.java
// Since: 2011-12-16
//
//--------------------------------------
package com.taobao.rpc.zaza.compression;

import java.io.InputStream;
import java.io.OutputStream;

public interface Decompressible {
    byte[] decompress(byte[] data) throws Exception;
    void decompress(String path) throws Exception;
    void decompress(String path, boolean delete) throws Exception;
    void decompress(String srcPath, String dstPath) throws Exception;
    void decompress(String srcPath, String dstPath,  boolean delete) throws Exception;
    void decompress(InputStream is, OutputStream os) throws Exception;
}
