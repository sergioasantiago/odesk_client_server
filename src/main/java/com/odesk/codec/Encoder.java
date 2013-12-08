package com.odesk.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

import com.odesk.protobuf.ODeskProtos.DirectlyCommunication;
import com.odesk.protobuf.ODeskProtos.Handshake;
import com.odesk.protobuf.ODeskProtos.Success;
import com.odesk.protobuf.ODeskProtos.TextMessage;
import com.odesk.protobuf.Types;

public class Encoder extends ChannelOutboundHandlerAdapter {

    private static final int HEADER_LENGTH = 6;

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        byte[] bytes = null;
        short msgType = 0;

        if(msg instanceof TextMessage) {
            TextMessage textMessage = (TextMessage) msg;
            bytes = textMessage.toByteArray();
            msgType = (short) Types.TEXT_MESSAGE.ordinal();
        } else if (msg instanceof DirectlyCommunication) {
            DirectlyCommunication directlyComm = (DirectlyCommunication) msg;
            bytes = directlyComm.toByteArray();
            msgType = (short) Types.DIRECTLY_COMMUNICATION.ordinal();
        } else if (msg instanceof Success) {
            Success success = (Success) msg;
            bytes = success.toByteArray();
            msgType = (short) Types.SUCCESS.ordinal();
        } else if (msg instanceof Handshake) {
            Handshake handshake = (Handshake) msg;
            bytes = handshake.toByteArray();
            msgType = (short) Types.HANDSHAKE.ordinal();
        }

        if(bytes == null)
            return;

        int msgLenght = bytes.length;
        ByteBuf encoded = ctx.alloc().buffer(bytes.length + HEADER_LENGTH);
        encoded.writeInt(msgLenght);
        encoded.writeShort(msgType);
        encoded.writeBytes(bytes);

        ctx.write(encoded, promise);
    }

}