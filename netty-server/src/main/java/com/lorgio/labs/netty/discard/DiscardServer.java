package com.lorgio.labs.netty.discard;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * Created by lorgiotrinidad on 15-05-16.
 */
public class DiscardServer {

    private int port;

    public DiscardServer(int port){
        this.port = port;
    }

    public void run() throws Exception{
        // (1)
        // NioEventLoopGroup is a multithreaded event loop that handles I/O operation. Netty provides various
        // EventLoopGroup implementations for different kind of transport. We are implementing a server-side
        // application in this exameple, and therefore two NioEventLoopGroup will be used. The first one, offen
        // called 'boss', accepts and incoming connection. The second one, offen called 'worked', handles the traffic
        // of the accepted connection once the boss accepts the connection and registers the accepted connection
        // to the worker. How many Threads are used and how they are mapped to the created Channels depends on the
        // EventLoopGroup implementation and may be even configurable via constructor.
        EventLoopGroup bossGroup = new NioEventLoopGroup(); // (1)
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try{
            // (2)
            // ServerBootstrap is a helper class that sets up a server. You can set up the server using a
            // Channel directly. However, please note that this is a tedious process, and you do not need to to
            // that in most cases.

            // (3)
            // Here, we specify to use the NioServerSocketChannel class which is used to instantiate a new Channel
            // to accept incoming connections.

            // (4)
            // The handler specify here will always be evaluated by a newly accepted Channel. The ChannelInitializer
            // is a special handler that is purposed to help a user configure a new Channel. It is most likely that
            // you want to configure the ChannelPipeline of the new Channel by adding some handlers such as
            // DiscardServerHandler to implement your network application. As the application gets complicated, it is
            // likely that you will add more handlers to the pipeline and extract this anonymous class into a top level
            // class eventually.

            // (5)
            // You can also set the parameters which are specify to the Channel implementation. We are writing a
            // TCP/IP server, so we are allowed to set the socket options such as tcpNoDelay and keepAlive. Please
            // refer to the apidocs of ChannelOption and the specify ChannelConfig implementations to get an overview
            // about the supported ChannelOptions.

            // (6)
            // Did you notice option() and childOption()? option() is for the NioServerSocketChannel that accepts
            // incoming connections. childOption() is for the Channels accpeted by the parent ServerChannel, which
            // is NioServerSocketChannel in this case.
            ServerBootstrap serverBootstrap = new ServerBootstrap(); // (2)
            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class) // (3)
                    .childHandler(new ChannelInitializer<SocketChannel>() { // (4)
                        @Override
                        public void initChannel(SocketChannel sc) throws Exception{
                            sc.pipeline().addLast(new EchoServerHandler());
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128) // (5)
                    .childOption(ChannelOption.SO_KEEPALIVE, true); // (6)

            // bind and start to accept incoming connections.
            ChannelFuture channelFuture = serverBootstrap.bind(port).sync(); // (7)

            // (7)
            // We are ready to go now. What's left is to bind to the port and to start the server. Here, we bind to
            // the port 8989 of all NICs (network interface cards) in the machine. You can now call the bind()
            // methods as many times as you want (with different bind addresses).

            // Wait until the server socket is closed.
            // In this example, this does not happen, but you can do that to gracefully
            // shut down your server.
            channelFuture.channel().closeFuture().sync();
        }finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception{
        int port;
        if(args.length > 0){
            port = Integer.parseInt(args[0]);
        }else{
            port = 8989;
        }
        new DiscardServer(port).run();
    }
}
