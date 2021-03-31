/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package api.ws;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketHandshakeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

public class HandshakeHandler extends ChannelInboundHandlerAdapter {
    private final Logger LOG = LoggerFactory.getLogger(HandshakeHandler.class);


    private final WebSocketClientHandshaker handshake;
    private final CompletableFuture completableFuture;

    public HandshakeHandler(WebSocketClientHandshaker handshake, CompletableFuture completableFuture) {
        this.handshake = handshake;
        this.completableFuture = completableFuture;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        LOG.debug("channelActive and handshaking......");
        handshake.handshake(ctx.channel());

    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Channel ch = ctx.channel();

        if (!handshake.isHandshakeComplete()) {
            try {
                handshake.finishHandshake(ch, (FullHttpResponse) msg);
                LOG.info("WebSocket Client connected!");
                completableFuture.complete(true);
                ctx.pipeline().remove(this);

            } catch (WebSocketHandshakeException e) {
                LOG.error("WebSocket Client failed to connect, " + e.getMessage());
                completableFuture.complete(false);
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if (!completableFuture.isDone()) {
            completableFuture.complete(false);
        }
        LOG.warn("exceptionCaught " + cause.getMessage());
    }
}
