package com.lorgio.labs.netty.time;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * Created by lorgiotrinidad on 15-05-16.
 */
public class TimeClient {

    public static void main(String[] args) throws Exception{
        String host;
        int port;
        if(args.length > 0){
            host = args[0];
            port = Integer.parseInt(args[1]);
        }else{
            host = "127.0.0.1";
            port = 9191;
        }

        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try{
            // (1)
            // Bootstrap is similar to ServerBootstrap except that it's non-server channel such as a client-side or
            // connectionless.
            Bootstrap b = new Bootstrap(); // (1)

            // (2)
            // If you specify only one EventLoopGroup, it will be used both as a boss group and as a worker group.
            // The boss worker is not used for the client side though.
            b.group(workerGroup); // (2)

            // (3)
            // Instead of NioServerSocketChannel, NioSocketChannel is being used to create a client-side Channel.
            b.channel(NioSocketChannel.class); // (3)

            // (4)
            // Not that we do not use childOption() here unlike we did with ServerBootstrap because the client-side
            // SocketChannel does not have a parent.
            b.option(ChannelOption.SO_KEEPALIVE, true); // (4)
            b.handler(new ChannelInitializer<SocketChannel>() {
               @Override
               public void initChannel(SocketChannel ch) throws Exception {
                   ch.pipeline().addLast(new TimeClientHandler());
               }
            });

            // (5)
            // We should call the connect method instead of the bind method.
            // Start the client
            ChannelFuture f = b.connect(host, port).sync(); // (5)

            // wait until the connection is closed.
            f.channel().closeFuture().sync();
        }finally {
            workerGroup.shutdownGracefully();
        }
    }
}
