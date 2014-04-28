/*--------------------------------------------------------------------------
 *  Copyright (c) 2012 by Institute of Computing Technology, 
 *                          Chinese Academic of Sciences, Beijing, China.
 *--------------------------------------------------------------------------*/
//--------------------------------------
// Ruijian Wang
//
// SnappyCompressor.java
// Since: 2011-12-16
//
//--------------------------------------
package com.taobao.rpc.zaza.compression;

import java.io.InputStream;
import java.io.OutputStream;
import org.xerial.snappy.SnappyInputStream;
import org.xerial.snappy.SnappyOutputStream;

public class SnappyCompressor extends Compressor {
	private final int BUFFER_SIZE = 64* 1024;

	public SnappyCompressor() {
		suffix = ".snappy";
	}

	public void compress(InputStream is, OutputStream os) throws Exception {
		SnappyOutputStream sos = new SnappyOutputStream(os);

		byte[] buf = new byte[BUFFER_SIZE];

		int count;
		while ((count = is.read(buf, 0, BUFFER_SIZE)) != -1) {
			sos.write(buf, 0, count);
		}
		
		sos.flush();
		sos.close();
	}

	public void decompress(InputStream is, OutputStream os) throws Exception {
		SnappyInputStream sis = new SnappyInputStream(is);
		
		byte[] buf = new byte[BUFFER_SIZE];

		int count;
		while ((count = sis.read(buf, 0, BUFFER_SIZE)) != -1) {
			os.write(buf, 0, count);
		}

		sis.close();
	}

	public static void main(String args[]) {
		if (args.length < 2) {
			System.err
					.println("Usage: SnappyCompressor compress|decompress srcFile [dstFile]");
			System.exit(1);
		}

		Compressor comp = new SnappyCompressor();

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
				if (dstFile != null) {
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
