package github.xinan.remoting.transport;

import github.xinan.extension.SPI;
import github.xinan.remoting.dto.RpcRequest;

/**
 * 发送Rpc请求的接口
 */
@SPI
public interface RpcRequestTransport {
    Object sendRpcRequest(RpcRequest rpcRequest);
}
