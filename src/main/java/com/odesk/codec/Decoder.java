package com.odesk.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;

import java.util.List;

import org.apache.log4j.Logger;

import com.google.protobuf.InvalidProtocolBufferException;
import com.odesk.protobuf.ODeskProtos.DirectlyCommunication;
import com.odesk.protobuf.ODeskProtos.Success;
import com.odesk.protobuf.ODeskProtos.TextMessage;
import com.odesk.protobuf.Types;

public class Decoder extends ReplayingDecoder<DecoderState> {

    private int length;
    private short msgType;
    private final Logger logger = Logger.getLogger(Decoder.class);

    public Decoder() {
        super(DecoderState.READ_LENGTH);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf buf, List<Object> out) {
        if (buf.isReadable()) {
            switch (state()) {
                case READ_LENGTH:
                    this.length = buf.readInt();
                    checkpoint(DecoderState.READ_MSG_TYPE);
                case READ_MSG_TYPE:
                    this.msgType = buf.readShort();
                    checkpoint(DecoderState.READ_MSG);
                case READ_MSG:
                    byte[] frame = new byte[this.length];
                    buf.readBytes(frame);
                    checkpoint(DecoderState.READ_LENGTH);

                    try {
                        switch (Types.values()[this.msgType]) {
                            case TEXT_MESSAGE:
                                out.add(TextMessage.parseFrom(frame));
                                break;
                            case DIRECTLY_COMMUNICATION:
                                out.add(DirectlyCommunication.parseFrom(frame));
                                break;
                            case SUCCESS:
                                out.add(Success.parseFrom(frame));
                                break;
                        }
                    } catch (InvalidProtocolBufferException e) {
                        this.logger.error(e.getMessage(), e);
                    }
            }
        }
    }
}
