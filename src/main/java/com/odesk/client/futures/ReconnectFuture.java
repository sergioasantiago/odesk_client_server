package com.odesk.client.futures;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

import org.apache.log4j.Logger;

import com.odesk.client.Client;

public class ReconnectFuture implements ChannelFutureListener {

    private final Client client;
    private static final Logger logger = Logger.getLogger(ReconnectFuture.class.getName());
    private CloseFuture closeFuture;

    public ReconnectFuture(Client client) {
        this.client = client;
    }

    public CloseFuture getCloseFuture() {
        return this.closeFuture;
    }

    public void operationComplete(ChannelFuture future) throws Exception {
        if (future.isSuccess()) {
            Channel channel = future.channel();
            this.closeFuture = new CloseFuture(this.client);
            channel.closeFuture().addListener(this.closeFuture);
            this.client.setChannel(channel);
            this.client.setReconnectDelay(0);
            this.client.connected();
        } else {
            this.client.IncrementReconnectDelay(3000);
            logger.info("Connection refused. Trying to reconnect in " + this.client.getReconnectDelay());
            Thread.sleep(this.client.getReconnectDelay());
            logger.info("Trying to reconnect...");
            this.client.connect();
        }
    }

}
