/*--------------------------------------------------------------------------
 *  Copyright (c) 2012 by Institute of Computing Technology, 
 *                          Chinese Academic of Sciences, Beijing, China.
 *--------------------------------------------------------------------------*/
//--------------------------------------
// Ruijian Wang
//
// QuickLZCompressor.java
// Since: 2011-12-16
//
//--------------------------------------
package com.taobao.rpc.zaza.compression;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;

public class QuickLZCompressor extends Compressor {
	private final int BUFFER_SIZE = 64 * 1024;

	public QuickLZCompressor() {
		suffix = ".qz";
	}

	public void compress(InputStream is, OutputStream os) throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		byte[] buf = new byte[BUFFER_SIZE];

		int count;
		while ((count = is.read(buf, 0, BUFFER_SIZE)) != -1) {
			baos.write(buf, 0, count);
		}

		baos.flush();
		byte[] source = baos.toByteArray();
		baos.close();

		byte[] output = QuickLZ.compress(source, 1);
		os.write(output, 0, output.length);
	}

	public void decompress(InputStream is, OutputStream os) throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		byte[] buf = new byte[BUFFER_SIZE];

		int count;
		while ((count = is.read(buf, 0, BUFFER_SIZE)) != -1) {
			baos.write(buf, 0, count);
		}

		baos.flush();
		byte[] source = baos.toByteArray();
		baos.close();

		byte[] output = QuickLZ.decompress(source);
		os.write(output, 0, output.length);
	}

	public static void main(String args[]) {
		if (args.length < 2) {
			System.err
					.println("Usage: QuickLZCompressor compress|decompress srcFile [dstFile]");
			System.exit(1);
		}

		Compressor comp = new QuickLZCompressor();

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
