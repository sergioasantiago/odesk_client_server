package com.odesk.server.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import org.apache.log4j.Logger;

import com.odesk.server.Server;


public class ServerHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = Logger.getLogger(ServerHandler.class.getName());

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        while (Server.clientsMap.values().remove(ctx.channel()));
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        logger.info("Message Received: " + msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("Unexpected exception from downstream.", cause);
        ctx.close();
    }
}