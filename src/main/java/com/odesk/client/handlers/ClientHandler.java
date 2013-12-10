package com.odesk.client.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

import com.odesk.client.DirectlyCommunicationTask;
import com.odesk.protobuf.ODeskProtos.DirectlyCommunication;

public class ClientHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = Logger.getLogger(ClientHandler.class.getName());

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.info("Connection established: " + ctx.channel().remoteAddress());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof DirectlyCommunication) {
            DirectlyCommunication directlyComm = (DirectlyCommunication)msg;
            ExecutorService service = Executors.newSingleThreadExecutor();
            service.submit(new DirectlyCommunicationTask(directlyComm, ctx));
            service.shutdown();
        } else {
            logger.info("Message Received: " + msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("Unexpected exception from downstream.", cause);
        ctx.close();
    }
}