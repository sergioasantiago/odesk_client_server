package com.odesk.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

import com.google.protobuf.Message;
import com.odesk.protobuf.ODeskProtos.DirectlyCommunication;
import com.odesk.protobuf.ODeskProtos.TextMessage;

public class Server implements Runnable {

    private final int port;
    private static final Logger logger = Logger.getLogger(Server.class.getName());
    private Channel channel;
    private ChannelHandler initializer;
    public static final Map<Integer, Channel> clientsMap = new HashMap<Integer, Channel>();
    private static EventLoopGroup bossGroup;
    private static EventLoopGroup workerGroup;

    public Server(int port, ChannelInitializer<SocketChannel> initializer) {
        this.port = port;
        this.initializer = initializer;
    }

    public void run() {
        listen();
    }

    private void prompt() {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            while (true) {
                System.out.println("1 - Send message to client using ClientID");
                System.out.println("2 - Tell clients to communicate directly");
                System.out.println("3 - Quit");
                String line = in.readLine();
                if (line == null) {
                    break;
                }
                if (line.equals("1")) {
                    System.out.print("ClientID: ");
                    Integer id = Integer.parseInt(in.readLine());
                    System.out.print("Text: ");
                    String text = in.readLine();

                    if (clientsMap.containsKey(id)) {
                        TextMessage.Builder msg = TextMessage.newBuilder();
                        msg.setText(text);
                        writeMsg(id, msg.build());
                    }
                } else if (line.equals("2")) {
                    System.out.print("ClientID of server mode: ");
                    Integer id1 = Integer.parseInt(in.readLine());

                    System.out.print("Server Port: ");
                    Integer port = Integer.parseInt(in.readLine());

                    DirectlyCommunication.Builder msg1 = DirectlyCommunication.newBuilder();
                    msg1.setMode(0);
                    msg1.setPort(port);

                    writeMsg(id1, msg1.build());

                    System.out.print("ClientID of client mode: ");
                    Integer id2 = Integer.parseInt(in.readLine());
                    System.out.print("Text: ");
                    String text = in.readLine();
                    DirectlyCommunication.Builder msg2 = DirectlyCommunication.newBuilder();
                    msg2.setMode(1);
                    msg2.setPort(port);
                    msg2.setHost(((InetSocketAddress) clientsMap.get(id1).remoteAddress()).getHostName());
                    msg2.setText(text);

                    writeMsg(id2, msg2.build());

                } else {
                    System.exit(0);
                }
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
        }
    }

    private void writeMsg(Integer id, Message msg) throws InterruptedException {
        Channel ch = clientsMap.get(id);
        ChannelFuture lastWriteFuture = ch.writeAndFlush(msg);

        // Wait until all messages are flushed before closing
        // the channel.
        if (lastWriteFuture != null) {
            lastWriteFuture.sync();
        }
    }

    private void listen() {
        Server.bossGroup = new NioEventLoopGroup();
        Server.workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(Server.bossGroup, Server.workerGroup).channel(NioServerSocketChannel.class).childHandler(this.initializer);
            logger.info("Listening on port: " + this.port);
            this.channel = b.bind(this.port).sync().channel();
            this.channel.closeFuture().sync();
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
        } finally {
            stopServer();
        }
    }

    public static void stopServer() {
        Server.bossGroup.shutdownGracefully();
        Server.workerGroup.shutdownGracefully();
    }

    public static void main(String[] args) throws Exception {
        int port;
        if (args.length != 1) {
            System.err.println("Usage: " + Server.class.getSimpleName() + "<port>");
            return;
        }

        port = Integer.parseInt(args[0]);

        final Server server = new Server(port, new ServerInitializer());
        ExecutorService service = Executors.newSingleThreadExecutor();
        service.execute(new Runnable() {
            public void run() {
                server.listen();
            }
        });
        service.shutdown();
        server.prompt();
    }
}