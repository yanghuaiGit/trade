package api.ws.hertbeat;

import api.Constant;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

/**
 * @Author: dujie
 * @Date: 2021-03-24
 */
public class DefaultHeartBeatImpl implements HeartBeat {

    public DefaultHeartBeatImpl() {
    }

    /**
     * 【心跳发送】
     *
     * @param ctx
     */
    @Override
    public void ping(ChannelHandlerContext ctx) {
        Channel channel = ctx.channel();
        if (channel != null) {
            if (channel.isActive()) {
                channel.writeAndFlush(Constant.PONG);
            }
        }
    }
}
