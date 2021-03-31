package api.ws;/*
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

import api.ws.hertbeat.DefaultHeartBeatImpl;
import api.ws.message.ReceiveMessage;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketClientCompressionHandler;
import io.netty.handler.proxy.Socks5ProxyHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.internal.SocketUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class BaseWsClient {

    public final Logger LOG = LoggerFactory.getLogger(BaseWsClient.class);

    private static final int MAX_RETRY = 5;

    private final CompletableFuture<Boolean> connectFuture = new CompletableFuture<>();
    private final WebsocketConfig websocketConfig;
    private final ReceiveMessage receiveMessage;
    public Channel channel;
    EventLoopGroup group;

    public BaseWsClient(WebsocketConfig websocketConfig, ReceiveMessage receiveMessage) {
        this.websocketConfig = websocketConfig;
        this.receiveMessage = receiveMessage;
    }


    public void connect() throws Exception {

        URI uri;
        if (StringUtils.isNotEmpty(websocketConfig.getUrl())) {
            uri = new URI(websocketConfig.getUrl());
        } else {
            uri = new URI(getUrl());
        }

        LOG.info("Connecting to {}", uri.toString());

        String scheme = uri.getScheme() == null ? "ws" : uri.getScheme();
        final String host = uri.getHost();

        if (host == null) {
            throw new IllegalArgumentException("Host cannot be null.");
        }

        final int port;
        //如果没有设置端口号
        if (uri.getPort() == -1) {
            if ("ws".equalsIgnoreCase(scheme)) {
                port = 80;
            } else if ("wss".equalsIgnoreCase(scheme)) {
                port = 443;
            } else {
                port = -1;
            }
        } else {
            port = uri.getPort();
        }

        if (!"ws".equalsIgnoreCase(scheme) && !"wss".equalsIgnoreCase(scheme)) {
            throw new IllegalArgumentException("Scheme " + scheme + "was invalid, Only WS(S) is supported.");
        }

        final boolean ssl = "wss".equalsIgnoreCase(scheme);
        final SslContext sslCtx;
        if (ssl) {
            sslCtx = SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build();
            //sslCtx = SslContext.newClientContext(InsecureTrustManagerFactory.INSTANCE); //Only for 4.0.* version
        } else {
            sslCtx = null;
        }


        group = new NioEventLoopGroup();

        // Connect with V13 (RFC 6455 aka HyBi-17). You can change it to V08 or V00.
        // If you change it to V00, ping is not supported and remember to change
        // HttpResponseDecoder to WebSocketHttpResponseDecoder in the pipeline.
        DefaultHttpHeaders entries = new DefaultHttpHeaders();

        final NettyWebsocketClientHandler handler =
                new NettyWebsocketClientHandler(
                        receiveMessage, websocketConfig.getHeartBeat() == null ? new DefaultHeartBeatImpl() : websocketConfig.getHeartBeat());


        Bootstrap bootstrap = new Bootstrap().group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ChannelPipeline pipeline = ch.pipeline();

                        if (websocketConfig.isSocksProxy()) {
                            pipeline.addLast(
                                    new Socks5ProxyHandler(
                                            SocketUtils.socketAddress(websocketConfig.getSocksProxyHost(), websocketConfig.getSocksProxyPort())));
                        }

                        //wss 连接
                        if (sslCtx != null) {
                            pipeline.addLast(sslCtx.newHandler(ch.alloc(), host, port));
                        }
                        pipeline.addLast(
                                new HttpClientCodec(),
                                new HttpObjectAggregator(65536),
                                WebSocketClientCompressionHandler.INSTANCE,
                                //new LoggingHandler(LogLevel.INFO), // only for debug
                                new IdleStateHandler(websocketConfig.getReaderIdleTimeSeconds(),
                                        websocketConfig.getWriterIdleTimeSeconds(),
                                        websocketConfig.getAllIdleTimeSeconds()),
                                new HandshakeHandler(WebSocketClientHandshakerFactory.newHandshaker(
                                        uri, WebSocketVersion.V13, null, true, entries), connectFuture),
                                handler);
                    }
                });
        connect(bootstrap, uri.getHost(), port, handler, MAX_RETRY);

        if (!connectFuture.get(10, TimeUnit.SECONDS)) {
            throw new RuntimeException("connect failed");
        }
    }


    private void connect(Bootstrap bootstrap, String host, int port, NettyWebsocketClientHandler handler, int retry) {
        bootstrap.connect(host, port).addListener(future -> {
            if (future.isSuccess()) {
                LOG.info("{}: 连接 host {} ,port {} 成功", new Date(), host, port);
                channel = ((ChannelFuture) future).channel();
                channel.closeFuture().addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        LOG.info("{} closed....", future.channel().toString());
                    }
                });
            } else if (retry == 0) {
                throw new RuntimeException("重试次数已用完，放弃连接！");
            } else {
                // 第几次重连
                int order = (MAX_RETRY - retry) + 1;
                // 本次重连的间隔
                int delay = 1 << order;
                LOG.warn("{} : 连接失败，第 {} 次重连……", new Date(), order);
                bootstrap.config().group().schedule(() -> connect(bootstrap, host, port, handler, retry - 1), delay, TimeUnit
                        .SECONDS);
            }
        });
    }

    /**
     * 通过配置去创建websocket相关URL以便于连接
     */
    private String getUrl() {

        String url = websocketConfig.getScheme() + "://" + websocketConfig.getHost() + ":" + websocketConfig.getPort() + "/"
                + websocketConfig.getPath();

        if (websocketConfig.getSuffixParams() != null && !websocketConfig.getSuffixParams().isEmpty()) {
            Map<String, Object> params = websocketConfig.getSuffixParams();
            StringBuilder paramsBuilder = new StringBuilder();
            for (String key : params.keySet()) {
                paramsBuilder.append(key);
                paramsBuilder.append("=");
                paramsBuilder.append(params.get(key));
                paramsBuilder.append("&");
            }
            if (paramsBuilder.length() > 0) {
                paramsBuilder.deleteCharAt(paramsBuilder.length() - 1);
            }
            url = url + "?" + paramsBuilder.toString();
        }
        return url;

    }
}
