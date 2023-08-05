package github.xinan.registry;

import github.xinan.extension.SPI;
import github.xinan.remoting.dto.RpcRequest;

import java.net.InetSocketAddress;

/**
 * 发现服务接口
 *
 * @author xinan
 * @date 2022-05-25 22:19
 */
@SPI
public interface ServiceDiscovery {
    /**
     * 通过rpc请求获得服务
     * @param rpcRequest 客户端发送的rpc请求
     * @return 服务地址的套接字？
     */
    InetSocketAddress lookupService(RpcRequest rpcRequest);
}
