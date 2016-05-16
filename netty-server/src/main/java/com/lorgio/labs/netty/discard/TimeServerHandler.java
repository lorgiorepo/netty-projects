package com.lorgio.labs.netty.discard;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * Created by lorgiotrinidad on 15-05-16.
 */
public class TimeServerHandler extends ChannelInboundHandlerAdapter{

    // (1)
    // As explained, the channelActive() method will be invoked when a connection is established and ready to
    // generate traffic. Let's write a 32-bit integer that represents the current time in this method.

    @Override
    public void channelActive(final ChannelHandlerContext ctx){ // (1)
        // (2)
        // To send a new message, we need to allocate a new buffer which will contain the message. we are going to
        // write a 32-bit integer, and therefore we need a ByteBuf whose capacity is at least 4 bytes. Get the current
        // ByteBufAllocator via ChannelHandlerContext.alloc() and allocate a new buffer.
        final ByteBuf time = ctx.alloc().buffer(4); // (2)
        time.writeInt((int) (System.currentTimeMillis() / 1000L + 2208988800L));

        // (3)
        // As usual we write the constructed message.
        // but wait, where's the flip?
        final ChannelFuture channelFuture = ctx.writeAndFlush(time); // (3)
        channelFuture.addListener(new ChannelFutureListener() {
            public void operationComplete(ChannelFuture future) {
                assert channelFuture == future;
                ctx.close();
            }
        }); // (4)
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause){
        // Close the connection when an exception is raised
        cause.printStackTrace();
        ctx.close();
    }
}
