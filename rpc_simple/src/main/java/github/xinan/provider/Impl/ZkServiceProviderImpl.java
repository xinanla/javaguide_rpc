package github.xinan.provider.Impl;

import github.xinan.config.RpcServiceConfig;
import github.xinan.enums.RpcErrorMessageEnum;
import github.xinan.exception.RpcException;
import github.xinan.extension.ExtensionLoader;
import github.xinan.provider.ServiceProvider;
import github.xinan.registry.ServiceRegistry;
import github.xinan.remoting.transport.netty.server.NettyRpcServer;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * TODO
 *
 * @author xinan
 * @date 2022-05-28 11:16
 */
@Slf4j
public class ZkServiceProviderImpl implements ServiceProvider {
    /**
     * 储存所有服务的map,rpcServiceName为key,service对象为value,service对象来自于传入的rpcServiceConfig中的service
     */
    private final Map<String, Object> serviceMap;
    /**
     * 储存所有已经添加的服务
     */
    private final Set<String> registeredService;

    private final ServiceRegistry serviceRegistry;

    public ZkServiceProviderImpl() {
        this.serviceMap = new ConcurrentHashMap<>();
        this.registeredService = ConcurrentHashMap.newKeySet();
        this.serviceRegistry = ExtensionLoader.getExtensionLoader(ServiceRegistry.class).getExtension("zk");
    }

    /**
     * 根据rpc服务配置类添加服务
     *
     * @param rpcServiceConfig rpc服务相关的属性配置
     */
    @Override
    public void addService(RpcServiceConfig rpcServiceConfig) {
        String rpcServiceName = rpcServiceConfig.getRpcServiceName();
        if (registeredService.contains(rpcServiceName)) {
            return;
        }
        registeredService.add(rpcServiceName);
        serviceMap.put(rpcServiceName, rpcServiceConfig.getService());
        log.info("已经添加服务:{},服务接口为:{}", rpcServiceName, rpcServiceConfig.getService().getClass().getInterfaces());
    }

    /**
     * 根据rpc服务名获取对应的服务
     *
     * @param rpcServiceName
     * @return
     */
    @Override
    public Object getService(String rpcServiceName) {
        Object service = serviceMap.get(rpcServiceName);
        if (null == service) {
            throw new RpcException(RpcErrorMessageEnum.SERVICE_CAN_NOT_BE_FOUND);
        }
        return service;
    }

    /**
     * 根据rpc服务配置类发布服务
     *
     * @param rpcServiceConfig
     */
    @Override
    public void publishService(RpcServiceConfig rpcServiceConfig) {
        try {
            String host = InetAddress.getLocalHost().getHostAddress();
            this.addService(rpcServiceConfig);
            serviceRegistry.registerService(rpcServiceConfig.getRpcServiceName(), new InetSocketAddress(host, NettyRpcServer.PORT));

        } catch (UnknownHostException e) {
            log.error("在获取本机地址时发生错误", e);
        }
    }
}
