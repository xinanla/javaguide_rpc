package github.xinan.remoting.transport.netty.client;

import github.xinan.enums.CompressTypeEnum;
import github.xinan.enums.SerializationTypeEnum;
import github.xinan.extension.ExtensionLoader;
import github.xinan.factory.SingletonFactory;
import github.xinan.registry.ServiceDiscovery;
import github.xinan.remoting.constants.RpcConstants;
import github.xinan.remoting.dto.RpcMessage;
import github.xinan.remoting.dto.RpcRequest;
import github.xinan.remoting.dto.RpcResponse;
import github.xinan.remoting.transport.RpcRequestTransport;
import github.xinan.remoting.transport.netty.codec.RpcMessageDecoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import github.xinan.remoting.transport.netty.codec.RpcMessageEncoder;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * netty客户端
 *
 * @author xinan
 * @date 2022-05-23 19:30
 */
@Slf4j
public class NettyRpcClient implements RpcRequestTransport {
    private final Bootstrap bootstrap;
    private final EventLoopGroup eventLoopGroup;
    private final ServiceDiscovery serviceDiscovery;
    private final UnprocessedRequests unprocessedRequests;
    private final ChannelProvider channelProvider;

    public NettyRpcClient() {
        eventLoopGroup = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                // 连接超时的策略，如果连接时间超过5000ms,则断开连接
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .handler(new ChannelInitializer<SocketChannel>() {

                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline p = ch.pipeline();
                        // 如果连接没有数据发送长达15s, 发送心跳请求
                        p.addLast(new IdleStateHandler(0, 5, 0, TimeUnit.SECONDS));
                        p.addLast(new RpcMessageEncoder());
                        p.addLast(new RpcMessageDecoder());
                        p.addLast(new NettyRpcClientHandler());
                    }
                });

        this.serviceDiscovery = ExtensionLoader.getExtensionLoader(ServiceDiscovery.class).getExtension("zk");
        this.unprocessedRequests = SingletonFactory.getInstance(UnprocessedRequests.class);
        this.channelProvider = SingletonFactory.getInstance(ChannelProvider.class);
    }


    /**
     * 客户端与服务端建立连接并获取信道
     * @param inetSocketAddress
     * @return
     */
    @SneakyThrows
    public Channel doConnect(InetSocketAddress inetSocketAddress) {
        // TODO completableFuture有什么用
        CompletableFuture<Channel> completableFuture = new CompletableFuture<>();
        // TODO 这是在干嘛
        bootstrap.connect(inetSocketAddress).addListener( (ChannelFutureListener)future -> {
            if (future.isSuccess()) {
                log.info("客户端连接成功，socket为:[{}]", inetSocketAddress.toString());
                completableFuture.complete(future.channel());
            } else {
                throw new IllegalStateException();
            }
        });
        return completableFuture.get();
    }

    /**
     * 获取建立连接后的信道
     * @param inetSocketAddress
     * @return
     */
    public Channel getChannel(InetSocketAddress inetSocketAddress) {
        Channel channel = channelProvider.get(inetSocketAddress);
        if (channel == null) {
            channel = doConnect(inetSocketAddress);
            channelProvider.set(inetSocketAddress, channel);
        }
        return channel;
    }

    /**
     * TODO 这是在关闭连接？
     */
    public void close() {
        eventLoopGroup.shutdownGracefully();
    }

    @Override
    public Object sendRpcRequest(RpcRequest rpcRequest) {

        // 感觉completableFuture像是一个容器，可以存RpcResponse
        CompletableFuture<RpcResponse<Object>> resultFuture = new CompletableFuture<>();

        // 获取服务所在的socket
        InetSocketAddress inetSocketAddress = serviceDiscovery.lookupService(rpcRequest);
        // 获取对应socket的信道
        Channel channel = getChannel(inetSocketAddress);

        if (channel.isActive()) {
            // 将储存RpcResponse的容器放入到未发送请求map中保存
            unprocessedRequests.put(rpcRequest.getRequestId(), resultFuture);
            RpcMessage rpcMessage = RpcMessage.builder()
                    .data(rpcRequest)
                    .codec(SerializationTypeEnum.KYRO.getCode())
                    .compress(CompressTypeEnum.GZIP.getCode())
                    .messageType(RpcConstants.REQUEST_TYPE).build();
            channel.writeAndFlush(rpcMessage).addListener( (ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    log.info("客户端信息发送成功:[{}]",rpcMessage);
                } else {
                    // TODO 关闭信道？completeExceptionally()有什么用
                    future.channel().close();
                    resultFuture.completeExceptionally(future.cause());
                    log.error("发送失败：",future.cause());
                }
            });
        } else {
            throw new IllegalStateException();
        }
        return resultFuture;

    }
}
