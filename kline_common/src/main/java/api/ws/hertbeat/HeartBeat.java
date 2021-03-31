package api.ws.hertbeat;

import io.netty.channel.ChannelHandlerContext;


/**
 * 【心跳接口】
 * @author luanxd
 * @date 2020-05-19
 */
public interface HeartBeat {

    /**
     * 【心跳发送】
     * @param ctx
     */
    void ping(ChannelHandlerContext ctx);
}
