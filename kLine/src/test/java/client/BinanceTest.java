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

import api.BinanceKlineApi;
import api.ws.BaseWsClient;
import api.ws.WebsocketConfig;
import api.ws.message.ReceiveMessage;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import util.GZIPUtils;

public class BinanceTest {
    public static void main(String[] args) throws Exception {

        String sub = "{\"method\":\"SUBSCRIBE\",\"params\":[\"!miniTicker@arr@3000ms\",\"btcbusd@aggTrade\",\"btcbusd@depth\"],\"id\":2}";
        String sub1 = "{\"method\":\"SUBSCRIBE\",\"params\":[\"btcbusd@kline_3m\"],\"id\":3}";
        String sub12 = "{\"method\":\"SUBSCRIBE\",\"params\":[\"aavebtc@kline_3m\"],\"id\":5}";
        String sub2 = "{\"method\":\"SUBSCRIBE\",\"params\":[\"aavebtc@aggTrade\"],\"id\":6}";
        String sub3 = "{\"method\":\"SUBSCRIBE\",\"params\":[\"aavebtc@depth\"],\"id\":8}";
        String sub34 = "{\"method\":\"SUBSCRIBE\",\"params\":[\"!miniTicker@arr@3000ms\",\"btcbusd@aggTrade\",\"btcbusd@depth\",\"btcbusd@kline_3m\",\"aavebtc@kline_3m\",\"aavebtc@aggTrade\",\"aavebtc@depth\"],\"id\":9}";
        String a = "{\"method\": \"LIST_SUBSCRIPTIONS\",\"id\": 3}";

        String combined = "{\"method\": \"SET_PROPERTY\",\"params\":[\"combined\",true],\"id\": 5}";
        WebsocketConfig websocketConfig = new WebsocketConfig();
        websocketConfig.setSocksProxy(true);

        websocketConfig.setWriterIdleTimeSeconds(30);
        websocketConfig.setReaderIdleTimeSeconds(30);
        websocketConfig.setAllIdleTimeSeconds(30);

        websocketConfig.setUrl("wss://sfstream.binance.com/stream/btcusdt@depth");


        new Thread(() -> {
            try {
                final boolean[] flag = {false};
                BaseWsClient client = new BaseWsClient(websocketConfig, new ReceiveMessage() {
                    @Override
                    public void onMessage(Channel channel, String text) {
                        //如果相应内容中的 result 为 null，表示请求发送成功。
                        //响应内容中的id是无符号整数，作为往来信息的唯一标识。

                        if (!flag[0]) {
                            try {
                                Thread.sleep(5000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            if (text.contains("2")) {
                                channel.writeAndFlush(new TextWebSocketFrame(a));
                            } else if (text.contains("3")) {
                                channel.writeAndFlush(new TextWebSocketFrame(combined));
                            } else if (text.contains("5")) {
                                channel.writeAndFlush(new TextWebSocketFrame(sub));
                                flag[0] = true;

                            }
                        }

                    }

                    @Override
                    public void onMessage(Channel channel, byte[] bytes) {
                        System.out.println(GZIPUtils.uncompress(bytes));
                    }

                    @Override
                    public void onMessage(Channel channel, WebSocketFrame frame) {
                        System.out.println(frame.toString());
                    }
                });
                client.connect();
                BinanceKlineApi api = new BinanceKlineApi(client);
                api.subscribe(sub);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        while (true) {

        }

    }
}
