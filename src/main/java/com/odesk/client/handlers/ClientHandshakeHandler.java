package com.odesk.client.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import com.odesk.client.Client;
import com.odesk.protobuf.ODeskProtos.Handshake;

public class ClientHandshakeHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Handshake.Builder handshake = Handshake.newBuilder();
        handshake.setName(Client.name);
        handshake.setHost(Client.localAddress);
        ctx.channel().writeAndFlush(handshake.build());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if(msg instanceof Handshake) {
            System.out.println("Handshake successfully");
            ctx.pipeline().remove("handshake_handler");
            ctx.pipeline().addLast("handler", new ClientHandler());
        }
    }
}
