package com.odesk.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.log4j.Logger;

import com.odesk.client.futures.ReconnectFuture;
import com.odesk.protobuf.ODeskProtos.TextMessage;

public class Client implements Runnable {

    private final String server_host;
    private final int port;
    public static String name;
    public static String localAddress;
    private String text;
    private int reconnectDelay;
    private Channel channel;
    private final Object connectionMonitor = new Object();
    private static final Logger logger = Logger.getLogger(Client.class.getName());
    private ChannelInitializer<SocketChannel> initializer;
    private ReconnectFuture reconnectFuture;

    public Client(String server_host, int port, String name, String localAddress, ChannelInitializer<SocketChannel> initializer) {
        this.server_host = server_host;
        this.port = port;
        Client.name = name;
        Client.localAddress = localAddress;
        this.initializer = initializer;
        this.reconnectDelay = 0;
    }

    public Client(String server_host, int port, String name, String text, String localAddress, ChannelInitializer<SocketChannel> initializer) {
        this.server_host = server_host;
        this.port = port;
        this.text = text;
        Client.name = name;
        Client.localAddress = localAddress;
        this.initializer = initializer;
        this.reconnectDelay = 0;
    }

    public void IncrementReconnectDelay(int i) {
        this.reconnectDelay += i;
    }

    public int getReconnectDelay() {
        return this.reconnectDelay;
    }

    public void setReconnectDelay(int reconnectDelay) {
        this.reconnectDelay = reconnectDelay;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public void connected() {
        synchronized (Client.this.connectionMonitor) {
            Client.this.connectionMonitor.notify();
        }
    }

    public void run() {
        try {
            connect();
            waitConnection();
            writeMessage(this.text);
            this.channel.closeFuture().removeListener(this.reconnectFuture.getCloseFuture());
            this.channel.close();
            this.channel.eventLoop().shutdownGracefully();
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void prompt() {
        try {
            System.out.println("Type a message to send to the server: ");

            // Read commands from the stdin.
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            while (true) {
                String line = in.readLine();
                if (line == null) {
                    break;
                }

                writeMessage(line);
            }
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }

    }

    private boolean writeMessage(String text) throws InterruptedException {
     // Sends the received line to the server.
        TextMessage.Builder msg = TextMessage.newBuilder();
        msg.setText(text);

        ChannelFuture lastWriteFuture = this.channel.writeAndFlush(msg.build());

        // If user typed the 'bye' command, wait until the server closes
        // the connection.
        if ("bye".equals(text.toLowerCase())) {
            this.channel.closeFuture().sync();
            // Wait until all messages are flushed before closing the channel.
            if (lastWriteFuture != null) {
                lastWriteFuture.sync();
            }
            return false;
        }
        return true;
    }

    public void connect() {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group).channel(NioSocketChannel.class).handler(this.initializer);

            // Start the connection attempt.
            this.reconnectFuture = new ReconnectFuture(this);
            b.connect(this.server_host, this.port).addListener(this.reconnectFuture).await();

        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
        }

    }

    private void waitConnection() throws InterruptedException {
        synchronized (this.connectionMonitor) {
            if (this.channel == null || !this.channel.isActive())
                this.connectionMonitor.wait();
        }
    }

    public static void main(String[] args) throws Exception {
        // Print usage if no argument is specified.
        if (args.length != 4) {
            System.err.println("Usage: " + Client.class.getSimpleName() + " <server_host> <port> <name> <client_host>");
            return;
        }

        // Parse options.
        String server_host = args[0];
        int port = Integer.parseInt(args[1]);
        String name = args[2];
        String localAddress = args[3];

        Client client = new Client(server_host, port, name, localAddress, new ClientInitializer());
        client.connect();
        client.waitConnection();
        client.prompt();
    }

}