package com.odesk.client;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import com.odesk.codec.Decoder;
import com.odesk.codec.Encoder;

public class ClientInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    public void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();

        pipeline.addLast("decoder", new Decoder());
        pipeline.addLast("encoder", new Encoder());

        // and then business logic.
        pipeline.addLast("handler", new ClientHandler());
    }
}