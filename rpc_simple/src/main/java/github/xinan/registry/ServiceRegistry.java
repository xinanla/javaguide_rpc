package github.xinan.registry;

import github.xinan.extension.SPI;

import java.net.InetSocketAddress;

/**
 * 服务注册接口
 *
 * @author xinan
 * @date 2022-05-25 22:32
 */
@SPI
public interface ServiceRegistry {
    /**
     * 使用服务的服务名和对应的socket注册服务
     * @param rpcServiceName 服务名
     * @param inetSocketAddress 服务端的套接字
     */
    void registerService(String rpcServiceName, InetSocketAddress inetSocketAddress);
}
