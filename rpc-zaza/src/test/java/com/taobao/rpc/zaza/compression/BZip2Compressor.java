/*--------------------------------------------------------------------------
 *  Copyright (c) 2012 by Institute of Computing Technology, 
 *                          Chinese Academic of Sciences, Beijing, China.
 *--------------------------------------------------------------------------*/
//--------------------------------------
// Ruijian Wang
//
// BZip2Compressor.java
// Since: 2011-12-16
//
//--------------------------------------
package com.taobao.rpc.zaza.compression;

import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;

public class BZip2Compressor extends Compressor {
    private final int BUFFER_SIZE = 64 * 1024; 

    public BZip2Compressor() {
    	suffix = ".bz2";
    }
    
    public void compress(InputStream is, OutputStream os) throws Exception {
        BZip2CompressorOutputStream bos = new BZip2CompressorOutputStream(os);
        
        byte[] buf = new byte[BUFFER_SIZE];
        int count;
        while ((count=is.read(buf, 0, BUFFER_SIZE)) != -1) {
            bos.write(buf, 0, count);
        }
        bos.finish();
        bos.flush();
        bos.close();
    }

    public void decompress(InputStream is, OutputStream os) throws Exception {
        BZip2CompressorInputStream bis = new BZip2CompressorInputStream(is);

        byte[] buf = new byte[BUFFER_SIZE];
        int count;
        while ((count=bis.read(buf, 0, BUFFER_SIZE)) != -1) {
            os.write(buf, 0, count);
        }
        os.flush();
        os.close();
    }

    public static void main(String args[]) {
        if (args.length < 2) {
            System.err.println("Usage: BZip2Compressor compress|decompress srcFile [dstFile]");
            System.exit(1);
        }
        
        Compressor comp = new BZip2Compressor();

        String cmd = args[0];
        String srcFile = args[1];
        String dstFile = null;
        if (args.length > 2) {
            dstFile = args[2];
        }
        
        try {
            if (cmd.equals("compress")) {
                if (dstFile != null) {
                    comp.compress(srcFile, dstFile, false);
                } else {
                    comp.compress(srcFile, false); 
                }
            } else if (cmd.equals("decompress")) {
                if (dstFile != null ) {
                    comp.decompress(srcFile, dstFile, false);
                } else {
                    comp.decompress(srcFile, false);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
