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

package api;

import api.ws.BaseWsClient;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.util.List;

public abstract class BaseKlineApi implements KlineApi {
    public BaseWsClient client;

    public BaseKlineApi(BaseWsClient client) {
        this.client = client;
    }

    @Override
    public boolean subscribe(String request) {
        client.channel.writeAndFlush(new TextWebSocketFrame(request));
        return true;
    }


    @Override
    public boolean subscribeBatch(List<String> code) {
        return false;
    }

    public abstract void sendRequest(String request);
}
