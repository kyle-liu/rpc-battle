package com.taobao.rpc.codec;

import com.google.common.base.Function;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Test;

import static com.taobao.rpc.codec.LengthFieldBaseFrameCodecUtil.*;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

/** @author <a href="mailto:jushi@taobao.com">jushi<a> */
public class LengthFieldBaseFrameCodecUtilTest {
    @SuppressWarnings("unchecked")
    private final Function<ByteBuf, Boolean> function = mock(Function.class);

    @Test
    public void shouldExtractNothingIfReadableBytesLessThan4() throws Exception {
        ByteBuf in = Unpooled.buffer(3);
        extractFrame(in, function);
        assertThat(in.readerIndex(), is(0));
        verify(function, never()).apply(any(ByteBuf.class));
    }

    @Test
    public void shouldExtractNothingIfReadableBytesLessThanLength() throws Exception {
        ByteBuf in = Unpooled.buffer(5);
        in.writeInt(4);
        in.writeByte(1);
        extractFrame(in, function);
        assertThat(in.readerIndex(), is(0));
        verify(function, never()).apply(any(ByteBuf.class));
    }

    @Test
    public void shouldPeekFrame() throws Exception {
        doReturn(false).when(function).apply(any(ByteBuf.class));
        ByteBuf in = Unpooled.buffer(5);
        in.writeInt(1);
        in.writeByte(1);
        extractFrame(in, function);
        assertThat(in.readerIndex(), is(0));
        verify(function).apply(in.slice(4, 1));
    }

    @Test
    public void shouldReadFrame() throws Exception {
        doReturn(true).when(function).apply(any(ByteBuf.class));
        ByteBuf in = Unpooled.buffer(5);
        in.writeInt(1);
        in.writeByte(1);
        extractFrame(in, function);
        assertThat(in.readerIndex(), is(5));
        verify(function).apply(in.slice(4, 1));
    }

    @Test
    public void shouldReadAllFrames() throws Exception {
        ByteBuf in = Unpooled.buffer(10);
        doReturn(true).when(function).apply(any(ByteBuf.class));
        in.writeInt(1);
        in.writeByte(1);
        in.writeInt(1);
        in.writeByte(1);
        extractFrames(in, function);
        assertThat(in.readerIndex(), is(10));
        verify(function).apply(in.slice(0, 10));
    }

    @Test
    public void shouldReadPartFrames() throws Exception {
        ByteBuf in = Unpooled.buffer(10);
        doReturn(true).when(function).apply(any(ByteBuf.class));
        in.writeInt(1);
        in.writeByte(1);
        in.writeInt(2);
        in.writeByte(1);
        extractFrames(in, function);
        assertThat(in.readerIndex(), is(5));
        verify(function).apply(in.slice(0, 5));
    }

    @Test
    public void shouldPeekFrames() throws Exception {
        ByteBuf in = Unpooled.buffer(10);
        doReturn(false).when(function).apply(any(ByteBuf.class));
        in.writeInt(1);
        in.writeByte(1);
        in.writeInt(1);
        in.writeByte(1);
        extractFrames(in, function);
        assertThat(in.readerIndex(), is(0));
        verify(function).apply(in.slice(0, 10));
    }

    @Test
    public void shouldExtractNoFramesIfReadableBytesLessThan4() throws Exception {
        ByteBuf in = Unpooled.buffer(10);
        doReturn(true).when(function).apply(any(ByteBuf.class));
        in.writeByte(1);
        extractFrames(in, function);
        assertThat(in.readerIndex(), is(0));
        verify(function, never()).apply(any(ByteBuf.class));
    }

    @Test
    public void shouldAppendLength() throws Exception {
        ByteBuf out = Unpooled.buffer(5);
        preappendLength(out, new Function<ByteBuf, Void>() {
            @Override
            public Void apply(ByteBuf byteBuf) {
                byteBuf.writeByte(1);
                return null;
            }
        });
        assertThat(out.getInt(0), is(1));
    }
}
