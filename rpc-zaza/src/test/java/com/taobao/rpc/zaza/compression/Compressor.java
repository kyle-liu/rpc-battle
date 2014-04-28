/*--------------------------------------------------------------------------
 *  Copyright (c) 2012 by Institute of Computing Technology, 
 *                          Chinese Academic of Sciences, Beijing, China.
 *--------------------------------------------------------------------------*/
//--------------------------------------
// Ruijian Wang
//
// Compressor.java
// Since: 2011-12-16
//
//--------------------------------------
package com.taobao.rpc.zaza.compression;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public abstract class Compressor implements Compressible, Decompressible {
    protected String suffix;

    public byte[] compress(byte[] data) throws Exception {
        ByteArrayInputStream is = new ByteArrayInputStream(data);
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        compress(is, os);
        os.flush();
        byte[] output = os.toByteArray();
        os.close();
        is.close();

        return output;
    }

    public void compress(String path) throws Exception {
        compress(path, true);
    }

    public void compress(String path, boolean delete) throws Exception {
        compress(path, path + suffix, delete);
    }

    public void compress(String srcPath, String dstPath) throws Exception {
        compress(srcPath, dstPath, true);
    }

    public void compress(String srcPath, String dstPath,  boolean delete) throws Exception {
        FileInputStream is = new FileInputStream(srcPath);
        FileOutputStream os = new FileOutputStream(dstPath);
        
        compress(is, os);

        os.flush();
        os.close();
        is.close();

        if (delete) {
            File srcFile = new File(srcPath);
            srcFile.delete();
        }
    }

    public abstract void compress(InputStream is, OutputStream os) throws Exception;

    public byte[] decompress(byte[] data) throws Exception {
        ByteArrayInputStream is = new ByteArrayInputStream(data);
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        decompress(is, os);
        os.flush();
        byte[] output = os.toByteArray();
        os.close();
        is.close();

        return output;
    }

    public void decompress(String path) throws Exception {
        compress(path, true);
    }

    public void decompress(String path, boolean delete) throws Exception {
        if (path.endsWith(suffix)) {
            decompress(path, path.substring(0, path.lastIndexOf(".")), delete);
        }
    }

    public void decompress(String srcPath, String dstPath) throws Exception {
        decompress(srcPath, dstPath, true);    
    }

    public void decompress(String srcPath, String dstPath,  boolean delete) throws Exception {
        FileInputStream is = new FileInputStream(srcPath);
        FileOutputStream os = new FileOutputStream(dstPath);

        decompress(is, os);

        os.flush();
        os.close();
        is.close();

        if (delete) {
            File srcFile = new File(srcPath);
            srcFile.delete();
        }
    }
    public abstract void decompress(InputStream is, OutputStream os) throws Exception;
}
