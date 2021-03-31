package api.ws.message;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;

/**
 * 【接口消息的接口】
 * @author luanxd
 * @date 2020-05-18
 */
public interface ReceiveMessage {

    public void onMessage(Channel channel, String text);

    public void onMessage(Channel channel, byte[] bytes);

    public void onMessage(Channel channel, WebSocketFrame frame);

}
