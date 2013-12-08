package com.odesk.client.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

import com.odesk.client.Client;
import com.odesk.client.inner.InnerClientInitializer;
import com.odesk.protobuf.ODeskProtos.DirectlyCommunication;
import com.odesk.protobuf.ODeskProtos.Success;
import com.odesk.server.Server;
import com.odesk.server.inner.InnerServerInitializer;

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
            switch (directlyComm.getMode()) {
                case 0: // Server mode
                    service.submit(new Server(directlyComm.getPort(), new InnerServerInitializer())).get();
                    break;
                case 1: //Client mode
                    service.submit(new Client(directlyComm.getHost(), directlyComm.getPort(), directlyComm.getName(), directlyComm.getText(), new InnerClientInitializer())).get();
                    break;
            }
            ctx.channel().writeAndFlush(Success.newBuilder().setSuccess(true).build());
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