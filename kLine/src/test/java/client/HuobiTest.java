package client;/*
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

import api.HuobiKlineApi;
import api.ws.BaseWsClient;
import api.ws.WebsocketConfig;
import api.ws.hertbeat.HeartBeat;
import api.ws.message.ReceiveMessage;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import util.GZIPUtils;

public class HuobiTest {
    public static void main(String[] args) throws Exception {

        WebsocketConfig websocketConfig = new WebsocketConfig();

        websocketConfig.setWriterIdleTimeSeconds(5);
        websocketConfig.setReaderIdleTimeSeconds(5);
        websocketConfig.setAllIdleTimeSeconds(5);
        websocketConfig.setUrl("wss://api.huobiasia.vip/ws");
        websocketConfig.setHeartBeat(new HeartBeat() {
            @Override
            public void ping(ChannelHandlerContext ctx) {
                ctx.channel().writeAndFlush(new TextWebSocketFrame("{\"pong\":" + System.currentTimeMillis() + "}"));
            }
        });

        new Thread(() -> {
            try {
                final String[] pong = {""};
                BaseWsClient client = new BaseWsClient(websocketConfig, new ReceiveMessage() {
                    @Override
                    public void onMessage(Channel channel, String text) {
                        System.out.println(text);
                    }

                    @Override
                    public void onMessage(Channel channel, byte[] bytes) {
                        pong[0] = GZIPUtils.uncompress(bytes).toString();
                        System.out.println(pong[0]);
                    }

                    @Override
                    public void onMessage(Channel channel, WebSocketFrame frame) {
                        System.out.println(frame.toString());
                    }
                });
                client.connect();
                HuobiKlineApi huobiKlineApi = new HuobiKlineApi(client);
                huobiKlineApi.subscribe("{\"sub\":\"market.btcusdt.kline.1min\",\"id\": \"id1\"}");

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        while (true) {

        }

    }
}
