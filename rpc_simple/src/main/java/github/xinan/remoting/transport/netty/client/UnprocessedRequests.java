package github.xinan.remoting.transport.netty.client;

import github.xinan.remoting.dto.RpcResponse;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * unprocessed requests by the server.存放未被处理的请求
 * 所有发送给服务端的请求都会放到这个类中，如果从服务端收到了响应，就说明这个请求已经被处理了，那么就根据请求id将对应的未处理请求id删除
 *
 * @author xinan
 * @date 2022-05-25 20:58
 */
public class UnprocessedRequests {
    // TODO CompleteFuture是什么
    private static final Map<String, CompletableFuture<RpcResponse<Object>>> UNPROCESSED_RESPONSE_FUTURES = new ConcurrentHashMap<>();

    public void put(String requestId, CompletableFuture<RpcResponse<Object>> future) {
        UNPROCESSED_RESPONSE_FUTURES.put(requestId,future);
    }

    public void complete(RpcResponse<Object> rpcResponse) {
        CompletableFuture<RpcResponse<Object>> future = UNPROCESSED_RESPONSE_FUTURES.remove(rpcResponse.getRequestId());
        if (null != future) {
            // TODO complete什么作用
            future.complete(rpcResponse);
        } else {
            throw new IllegalArgumentException("unprocessedRequests出现问题");
        }
    }
}
