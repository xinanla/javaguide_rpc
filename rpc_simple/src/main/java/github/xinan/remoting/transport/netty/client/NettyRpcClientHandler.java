package github.xinan.remoting.transport.netty.client;

import github.xinan.enums.CompressTypeEnum;
import github.xinan.enums.SerializationTypeEnum;
import github.xinan.factory.SingletonFactory;
import github.xinan.remoting.constants.RpcConstants;
import github.xinan.remoting.dto.RpcMessage;
import github.xinan.remoting.dto.RpcResponse;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

/**
 * Customize the client ChannelHandler to process the data sent by the server
 *
 * <p>
 * 如果继承自 SimpleChannelInboundHandler 的话就不要考虑 ByteBuf 的释放 ，{@link io.netty.channel.SimpleChannelInboundHandler} 内部的
 * channelRead 方法会替你释放 ByteBuf ，避免可能导致的内存泄露问题。详见《Netty进阶之路 跟着案例学 Netty》
 */
@Slf4j
public class NettyRpcClientHandler extends ChannelInboundHandlerAdapter {
    private final UnprocessedRequests unprocessedRequests;
    private final NettyRpcClient nettyRpcClient;

    public NettyRpcClientHandler() {
        this.nettyRpcClient = SingletonFactory.getInstance(NettyRpcClient.class);
        this.unprocessedRequests = SingletonFactory.getInstance(UnprocessedRequests.class);
    }

    /**
     *  TODO
     *  读取服务端传来的信息
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            log.info("客户端收到消息：[{}]", msg);
            if (msg instanceof RpcMessage) {
                // 将收到的消息转成RpcMessage包装类对象
                RpcMessage rpcMessage = (RpcMessage)msg;
                byte type = rpcMessage.getMessageType();
                if (type == RpcConstants.HEARTBEAT_RESPONSE_TYPE) {
                    // 如果是心跳响应则打印日志
                    log.info("心跳响应[{}]",rpcMessage.getData());
                } else if (type == RpcConstants.RESPONSE_TYPE) {
                    // 如果是Rpc响应则取出包装对象中的数据，并转成RpcResponse对象
                    RpcResponse<Object> rpcResponse = (RpcResponse<Object>) rpcMessage.getData();
                    // TODO 这里的complete功能是什么
                    // 已经收到响应，将未完成请求中，收到的响应的requestId对应的future容器移除，表示这个请求已经完成
                    unprocessedRequests.complete(rpcResponse);
                }
            }
        } finally {
            // TODO 网上是说解除引用，方便回收？具体原因是什么
            ReferenceCountUtil.release(msg);
        }

    }

    /**
     * Netty心跳机制相关，保证客户端和服务端的连接不会断掉，避免重连
     * 自定义心跳包内容
     * TODO 为啥这样搞
     * @param ctx
     * @param evt
     * @throws Exception
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception{
        if (evt instanceof IdleStateEvent) {
            // TODO 获取心跳连接的状态？
            IdleState state = ((IdleStateEvent) evt).state();
            if (state == IdleState.WRITER_IDLE) {
                log.info("写idle发生了[{}]", ctx.channel().remoteAddress());
                Channel channel = nettyRpcClient.getChannel((InetSocketAddress) ctx.channel().remoteAddress());
                RpcMessage rpcMessage = RpcMessage.builder()
                        .codec(SerializationTypeEnum.KYRO.getCode())
                        .compress(CompressTypeEnum.GZIP.getCode())
                        .messageType(RpcConstants.HEARTBEAT_REQUEST_TYPE)
                        .data(RpcConstants.PING).build();

                channel.writeAndFlush(rpcMessage).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    /**
     * TODO 这是干嘛
     * @param ctx
     * @param cause
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("客户端捕获异常:", cause);
        cause.printStackTrace();
        ctx.close();
    }



}
