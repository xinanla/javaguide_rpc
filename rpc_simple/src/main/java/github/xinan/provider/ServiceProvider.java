package github.xinan.provider;

import github.xinan.config.RpcServiceConfig;

public interface ServiceProvider {
    /**
     * 根据rpc服务配置类添加服务
     * @param rpcServiceConfig rpc服务相关的属性配置
     */
    void addService(RpcServiceConfig rpcServiceConfig);

    /**
     * 根据rpc服务名获取对应的服务
     * @param rpcServiceName
     * @return
     */
    Object getService(String rpcServiceName);

    /**
     * 根据rpc服务配置类发布服务
     * @param rpcServiceConfig
     */
    void publishService(RpcServiceConfig rpcServiceConfig);
}
