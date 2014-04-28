package com.taobao.rpc.codec;

import com.google.common.base.Function;
import io.netty.buffer.ByteBuf;

/** @author <a href="mailto:jushi@taobao.com">jushi<a> */
public final class LengthFieldBaseFrameCodecUtil {
    private static final int MAX_FRAME_LENGTH = 65536;
    private static final int LENGTH_FIELD_LENGTH = 4;
    private static final byte[] LENGTH_PLACEHOLDER = new byte[LENGTH_FIELD_LENGTH];

    private LengthFieldBaseFrameCodecUtil() {}

    public static void extractFrame(ByteBuf in, Function<ByteBuf, Boolean> andThen) {
        while (in.readableBytes() > LENGTH_FIELD_LENGTH) {
            int start = in.readerIndex();
            int length = in.getInt(start);
            // TODO skip too large frame
            if (length > in.readableBytes() - LENGTH_FIELD_LENGTH) return;
            if (!andThen.apply(in.slice(start + LENGTH_FIELD_LENGTH, length))) return;
            in.skipBytes(LENGTH_FIELD_LENGTH + length);
        }
    }

    public static void extractFrames(ByteBuf in, Function<ByteBuf, Boolean> andThen) {
        int begin, end;
        begin = end = in.readerIndex();
        while (in.readableBytes() - end > LENGTH_FIELD_LENGTH) {
            int length = in.getInt(end);
            if (length > in.readableBytes() - end - LENGTH_FIELD_LENGTH) break;
            end += LENGTH_FIELD_LENGTH + length;
        }
        int length = end - begin;
        if (length == 0) return;
        if (!andThen.apply(in.slice(begin, length))) return;
        in.skipBytes(length);
    }

    public static ByteBuf preappendLength(ByteBuf buf, Function<ByteBuf, Void> function) {
        buf.writeBytes(LENGTH_PLACEHOLDER);
        function.apply(buf);
        int end = buf.writerIndex();
        buf.setInt(0, end - LENGTH_FIELD_LENGTH);
        return buf;
    }

}
