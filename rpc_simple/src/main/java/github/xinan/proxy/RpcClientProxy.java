package github.xinan.proxy;

import github.xinan.config.RpcServiceConfig;
import github.xinan.enums.RpcErrorMessageEnum;
import github.xinan.enums.RpcResponseCodeEnum;
import github.xinan.exception.RpcException;
import github.xinan.remoting.dto.RpcRequest;
import github.xinan.remoting.dto.RpcResponse;
import github.xinan.remoting.transport.RpcRequestTransport;
import github.xinan.remoting.transport.netty.client.NettyRpcClient;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * TODO
 *
 * @author xinan
 * @date 2022-05-30 21:55
 */
@Slf4j
public class RpcClientProxy implements InvocationHandler {
    private final RpcRequestTransport rpcRequestTransport;
    private final RpcServiceConfig rpcServiceConfig;
    public RpcClientProxy(RpcRequestTransport rpcRequestTransport, RpcServiceConfig rpcServiceConfig) {
        this.rpcRequestTransport = rpcRequestTransport;
        this.rpcServiceConfig = rpcServiceConfig;
    }

    /**
     * 根据接口名获取接口的代理对象
     * @param clazz
     * @param <T>
     * @return
     */
    public <T> T getProxy(Class<T> clazz) {
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(),new Class<?>[]{clazz},this);
    }

    @SneakyThrows
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        log.info("接口的代理对象调用方法[{}]", method.getName());
        // 代理对象需要帮助原对象完成rpcRequest的发送
        // 构建rpcRequest
        RpcRequest rpcRequest = RpcRequest.builder()
                .methodName(method.getName())
                .version(rpcServiceConfig.getVersion())
                .group(rpcServiceConfig.getGroup())
                .requestId(UUID.randomUUID().toString())
                .parameters(args)
                .paramTypes(method.getParameterTypes())
                .interfaceName(method.getDeclaringClass().getName()).build();
        // 发送rpcRequest并返回rpcResponse
        RpcResponse<Object> rpcResponse = null;
        if (rpcRequestTransport instanceof NettyRpcClient) {
            CompletableFuture<RpcResponse<Object>> completableFuture = (CompletableFuture<RpcResponse<Object>>) rpcRequestTransport.sendRpcRequest(rpcRequest);
            rpcResponse = completableFuture.get();
        }
        check(rpcResponse, rpcRequest);

        return rpcResponse.getData();
    }

    /**
     * 检查收到的响应是否正确，通过判断响应的响应码是否成功以及是否和请求的请求id相同
     * @param rpcResponse
     * @param rpcRequest
     */
    private void check(RpcResponse<Object> rpcResponse, RpcRequest rpcRequest) {
        if (rpcResponse == null) {
            throw new RpcException(RpcErrorMessageEnum.SERVICE_INVOCATION_FAILURE, "interfaceName" + ":" + rpcRequest.getInterfaceName());
        }
        if (!rpcRequest.getRequestId().equals(rpcResponse.getRequestId())) {
            throw new RpcException(RpcErrorMessageEnum.REQUEST_NOT_MATCH_RESPONSE, "interfaceName" + ":" + rpcRequest.getInterfaceName());
        }
        if (rpcResponse.getCode() == null || !rpcResponse.getCode().equals(RpcResponseCodeEnum.SUCCESS.getCode())) {
            throw new RpcException(RpcErrorMessageEnum.SERVICE_INVOCATION_FAILURE, "interfaceName" + ":" + rpcRequest.getInterfaceName());
        }
    }
}
