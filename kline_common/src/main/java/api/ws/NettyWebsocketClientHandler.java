package api.ws;

import api.ws.hertbeat.DefaultHeartBeatImpl;
import api.ws.hertbeat.HeartBeat;
import api.ws.message.DefaultReceiveMessage;
import api.ws.message.ReceiveMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketHandshakeException;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

/**
 * @author dujie
 * @date 2021-03-26
 */
public class NettyWebsocketClientHandler extends SimpleChannelInboundHandler<Object> {
    private final Logger LOG = LoggerFactory.getLogger(NettyWebsocketClientHandler.class);


    private final ReceiveMessage receiveMessage;

    private final HeartBeat heartBeat;


    public NettyWebsocketClientHandler( ) {
        this( new DefaultReceiveMessage(), new DefaultHeartBeatImpl());
    }

    public NettyWebsocketClientHandler(ReceiveMessage receiveMessage, HeartBeat heartBeat) {
        this.receiveMessage = receiveMessage;
        this.heartBeat = heartBeat;
    }


    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        LOG.warn("WebSocket Client disconnected!");
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        Channel ch = ctx.channel();

        if (msg instanceof FullHttpResponse) {
            FullHttpResponse response = (FullHttpResponse) msg;
            throw new IllegalStateException(
                    "Unexpected FullHttpResponse (getStatus=" + response.status() +
                            ", content=" + response.content().toString(CharsetUtil.UTF_8) + ')');
        }

        WebSocketFrame frame = (WebSocketFrame) msg;
        if (frame instanceof TextWebSocketFrame) {
            TextWebSocketFrame textFrame = (TextWebSocketFrame) frame;
            receiveMessage.onMessage(ch, textFrame.text());
        } else if (frame instanceof PongWebSocketFrame) {
            LOG.debug("WebSocket Client received pong");
            receiveMessage.onMessage(ch, frame);
        } else if (frame instanceof CloseWebSocketFrame) {
            LOG.debug("WebSocket Client received close Frame");
            //执行后将关闭
            receiveMessage.onMessage(ch, frame);

            ch.eventLoop().shutdownGracefully();
            ch.close().sync();
        } else if (frame instanceof BinaryWebSocketFrame) {
            BinaryWebSocketFrame binaryFrame = (BinaryWebSocketFrame) msg;
            ByteBuf buf = binaryFrame.content();
            if (buf.isReadable()) {
                int availableBytesNumber = buf.readableBytes();
                byte[] receivedBytes = new byte[availableBytesNumber];
                buf.readBytes(receivedBytes);
                receiveMessage.onMessage(ch, receivedBytes);
            }
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent idleStateEvent = (IdleStateEvent) evt;
            switch (idleStateEvent.state()) {
                case WRITER_IDLE:
                    handlerWriterIdleEvent(ctx);
                    break;
                case READER_IDLE:
                    handlerReaderIdleEvent(ctx);
                    break;
                case ALL_IDLE:
                    handlerAllIdleEvent(ctx);
                    break;
                default:
                    break;
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }


    protected void handlerWriterIdleEvent(ChannelHandlerContext ctx) {
        heartBeat.ping(ctx);
    }

    protected void handlerReaderIdleEvent(ChannelHandlerContext ctx) {
        heartBeat.ping(ctx);
    }

    protected void handlerAllIdleEvent(ChannelHandlerContext ctx) {
        heartBeat.ping(ctx);
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        LOG.warn("exceptionCaught " + cause.getMessage());
    }
}
