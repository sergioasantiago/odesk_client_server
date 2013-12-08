package com.odesk.server.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import org.apache.log4j.Logger;

import com.odesk.protobuf.ODeskProtos.Handshake;
import com.odesk.server.Server;

public class ServerHandshakeHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = Logger.getLogger(ServerHandshakeHandler.class.getName());

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if(msg instanceof Handshake) {
            Handshake handshake = (Handshake)msg;
            logger.info("New client connected! ClientID: " + handshake.getName());
            Server.clientsMap.put(handshake.getName(), ctx.channel());
            ctx.pipeline().remove("handshake_handler");
            ctx.pipeline().addLast("handler", new ServerHandler());
            Handshake.Builder handshakeBuilder = Handshake.newBuilder();
            handshakeBuilder.setName("Server");
            ctx.channel().writeAndFlush(handshakeBuilder.build());
        }
    }


}
