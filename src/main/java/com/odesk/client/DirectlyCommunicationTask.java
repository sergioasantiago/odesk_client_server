package com.odesk.client;

import io.netty.channel.ChannelHandlerContext;

import com.odesk.client.inner.InnerClientInitializer;
import com.odesk.protobuf.ODeskProtos.DirectlyCommunication;
import com.odesk.protobuf.ODeskProtos.Success;
import com.odesk.server.Server;
import com.odesk.server.inner.InnerServerInitializer;

public class DirectlyCommunicationTask implements Runnable {

    private DirectlyCommunication directlyComm;
    private ChannelHandlerContext ctx;

    public DirectlyCommunicationTask(DirectlyCommunication directlyComm, ChannelHandlerContext ctx) {
        this.directlyComm = directlyComm;
        this.ctx = ctx;
    }

    public void run() {
        switch (this.directlyComm.getMode()) {
            case 0: // Server mode
                new Server(this.directlyComm.getPort(), new InnerServerInitializer()).run();
                break;
            case 1: //Client mode
                new Client(this.directlyComm.getHost(), this.directlyComm.getPort(), this.directlyComm.getName(), this.directlyComm.getText(), Client.localAddress, new InnerClientInitializer()).run();
                break;
        }
        this.ctx.channel().writeAndFlush(Success.newBuilder().setSuccess(true).build());
    }

}
