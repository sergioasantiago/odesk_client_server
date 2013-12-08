package com.odesk.server;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

import com.odesk.codec.Decoder;
import com.odesk.codec.Encoder;
import com.odesk.server.handlers.ServerHandshakeHandler;

public class ServerInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    public void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();

        pipeline.addLast("decoder", new Decoder());
        pipeline.addLast("encoder", new Encoder());

        pipeline.addLast("handshake_handler", new ServerHandshakeHandler());
    }
}