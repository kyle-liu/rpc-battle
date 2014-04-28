package com.taobao.rpc.zaza.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import org.slf4j.LoggerFactory;
import org.xerial.snappy.SnappyInputStream;
import org.xerial.snappy.SnappyOutputStream;

public class ZazaCompressionUtil {
    private final static org.slf4j.Logger logger = LoggerFactory.getLogger(ZazaCompressionUtil.class);
    private final int BUFFER_SIZE = 64 * 1024;

    public static byte[] compress(byte[] data) {

        byte[] temp = new byte[5000];
        Deflater compresser = new Deflater();
        compresser.setInput(data);
        compresser.finish();
        int n = compresser.deflate(temp);
        return Arrays.copyOf(temp, n);

    }

    public static byte[] decompress(byte[] data) throws IOException {

        byte[] temp = new byte[5000];
        Inflater decompresser = new Inflater();
        decompresser.setInput(data);
        int n = 0;
        try {
            n = decompresser.inflate(temp);
            decompresser.end();
        } catch (DataFormatException e) {
            logger.error("[decompress error]", e);
        }
        return Arrays.copyOf(temp, n);

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
}
