/*--------------------------------------------------------------------------
 *  Copyright (c) 2012 by Institute of Computing Technology, 
 *                          Chinese Academic of Sciences, Beijing, China.
 *--------------------------------------------------------------------------*/
//--------------------------------------
// Ruijian Wang
//
// GZipCompressor.java
// Since: 2011-12-16
//
//--------------------------------------
package com.taobao.rpc.zaza.compression;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class GZipCompressor extends Compressor {
	private final int BUFFER_SIZE = 64 * 1024;

	public GZipCompressor() {
		suffix = ".gz";
	}

	public void compress(InputStream is, OutputStream os) throws Exception {
		GZIPOutputStream gos = new GZIPOutputStream(os);

		byte[] buf = new byte[BUFFER_SIZE];
		int count;
		while ((count = is.read(buf, 0, BUFFER_SIZE)) != -1) {
			gos.write(buf, 0, count);
		}
		gos.flush();
		gos.close();
	}

	public void decompress(InputStream is, OutputStream os) throws Exception {
		GZIPInputStream gis = new GZIPInputStream(is);

		byte[] buf = new byte[BUFFER_SIZE];
		int count;
		while ((count = gis.read(buf, 0, BUFFER_SIZE)) != -1) {
			os.write(buf, 0, count);
		}
	}

	public static void main(String args[]) {
		if (args.length < 2) {
			System.err
					.println("Usage: GZipCompressor compress|decompress srcFile [dstFile]");
			System.exit(1);
		}

		Compressor comp = new GZipCompressor();

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
