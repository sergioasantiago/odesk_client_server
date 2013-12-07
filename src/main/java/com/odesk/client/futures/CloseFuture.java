package com.odesk.client.futures;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

import org.apache.log4j.Logger;

import com.odesk.client.Client;

public class CloseFuture implements ChannelFutureListener {

    private final Client client;
    private static final Logger logger = Logger.getLogger(CloseFuture.class.getName());

    public CloseFuture(Client client) {
        this.client = client;
    }

    public void operationComplete(ChannelFuture future) throws Exception {
        this.client.IncrementReconnectDelay(3000);
        logger.info("Connection lost. Trying to reconnect in " + this.client.getReconnectDelay());
        Thread.sleep(this.client.getReconnectDelay());
        logger.info("Trying to reconnect...");
        this.client.connect();
    }

}
