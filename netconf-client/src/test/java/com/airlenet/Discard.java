package com.airlenet;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;

import java.net.InetAddress;
import java.util.Date;
import java.util.Scanner;
import java.util.logging.SocketHandler;

public class Discard {
    public static class Server {
        public static void main(String[] args) throws InterruptedException {
            EventLoopGroup bossGroup = new NioEventLoopGroup(); // (1)
            EventLoopGroup workerGroup = new NioEventLoopGroup();
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {

//                            socketChannel.pipeline().addLast(new ChannelInboundHandlerAdapter() {
//                                @Override
//                                public void channelActive(ChannelHandlerContext ctx) throws Exception {
//                                    super.channelActive(ctx);
//                                    System.out.println(ctx.channel().remoteAddress());
//                                    final ByteBuf time = ctx.alloc().buffer(4); // (2)
//                                    time.writeInt((int) (System.currentTimeMillis() / 1000L + 2208988800L));
//
//                                    final ChannelFuture f = ctx.writeAndFlush(time); // (3)
////                                    f.addListener(new ChannelFutureListener() {
////                                        @Override
////                                        public void operationComplete(ChannelFuture future) {
////                                            assert f == future;
////                                            ctx.close();
////                                        }
////                                    }); // (4)
//                                }
//
//                                @Override
//                                public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//                                    super.channelRead(ctx, msg);
//                                }
//                            });

                            socketChannel.pipeline().addLast(new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()));
                            socketChannel.pipeline().addLast("encoder", new StringEncoder());
                            socketChannel.pipeline().addLast("decoder", new StringDecoder());
                            socketChannel.pipeline().addLast(new SimpleChannelInboundHandler<String>() {
                                @Override
                                public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                    Unpooled.copiedBuffer("".getBytes(CharsetUtil.UTF_8));
                                    ctx.write("Welcome to " + InetAddress.getLocalHost().getHostName() + "!\r\n");
                                    ctx.write("It is " + new Date() + " now.\r\n");
                                    ctx.flush();
                                }

                                @Override
                                protected void channelRead0(ChannelHandlerContext ctx, String request) throws Exception {
                                    String response;
                                    boolean close = false;
                                    if (request.isEmpty()) {
                                        response = "Please type something.\r\n";
                                    } else if ("bye".equals(request.toLowerCase())) {
                                        response = "Have a good day!\r\n";
                                        close = true;
                                    } else {
                                        response = "Did you say '" + request + "'?\r\n";
                                    }

                                    // We do not need to write a ChannelBuffer here.
                                    // We know the encoder inserted at TelnetPipelineFactory will do the conversion.
                                    ChannelFuture future = ctx.write(response);

                                    // Close the connection after sending 'Have a good day!'
                                    // if the client has sent 'bye'.
                                    if (close) {
                                        future.addListener(ChannelFutureListener.CLOSE);
                                    }
                                }

                                @Override
                                public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
                                    ctx.flush();
                                }

                                @Override
                                public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                                   ctx.close();
                                }
                            });
                        }
                    }).option(ChannelOption.SO_BACKLOG, 128).childOption(ChannelOption.SO_KEEPALIVE, true)
            ;
            ChannelFuture future = bootstrap.bind(2000).sync();

            future.channel().closeFuture().sync();
        }
    }

    public static class Client {
        public static void main(String[] args) throws InterruptedException {
            EventLoopGroup bossGroup = new NioEventLoopGroup(); // (1)
            EventLoopGroup workerGroup = new NioEventLoopGroup();
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(workerGroup);
            bootstrap.option(ChannelOption.SO_KEEPALIVE, true); // (4)
            bootstrap.channel(NioSocketChannel.class).handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel nioSocketChannel) throws Exception {
                    nioSocketChannel.pipeline().addLast(new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()));
                    nioSocketChannel.pipeline().addLast("encoder", new StringEncoder());
                    nioSocketChannel.pipeline().addLast("decoder", new StringDecoder());
                    nioSocketChannel.pipeline().addLast(new SimpleChannelInboundHandler<String>() {
                        @Override
                        protected void channelRead0(ChannelHandlerContext channelHandlerContext, String s) throws Exception {
                            System.out.println(s);
                            channelHandlerContext.write("2222222\r\n");
                        }


                        @Override
                        public void channelActive(ChannelHandlerContext ctx) throws Exception {
                            super.channelActive(ctx);
//                             System.out.println(ctx.channel().localAddress());
                             final ByteBuf time = ctx.alloc().buffer(40); // (2)
//                             time.writeInt((int) (System.currentTimeMillis() / 1000L + 2208988800L));
                            time.writeBytes("hello\r\n".getBytes());
                            ctx.writeAndFlush(time);

                        }

                        @Override
                        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
                            ctx.flush();
                        }
                    });
                }
            });
            ChannelFuture future = bootstrap.connect("localhost", 2000).sync();


            future.channel().closeFuture().sync();
        }
    }
}
