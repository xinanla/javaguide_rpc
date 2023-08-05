package github.xinan.registry.zk;

import github.xinan.enums.RpcErrorMessageEnum;
import github.xinan.exception.RpcException;
import github.xinan.extension.ExtensionLoader;
import github.xinan.loadbalance.LoadBalance;
import github.xinan.registry.ServiceDiscovery;
import github.xinan.registry.zk.util.CuratorUtils;
import github.xinan.remoting.dto.RpcRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.util.CollectionUtils;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * service discovery based on zookeeper
 * 基于zookeeper的服务发现类
 *
 * @author xinan
 * @date 2022-05-26 19:27
 */
@Slf4j
public class ZkServiceDiscoveryImpl implements ServiceDiscovery {

    private final LoadBalance loadBalance;

    public ZkServiceDiscoveryImpl() {
        this.loadBalance = ExtensionLoader.getExtensionLoader(LoadBalance.class).getExtension("loadBalance");
    }
    /**
     * 通过rpc请求获得服务
     *
     * @param rpcRequest 客户端发送的rpc请求
     * @return 服务地址的套接字
     */
    @Override
    public InetSocketAddress lookupService(RpcRequest rpcRequest){
        // 获取rpc请求中想要获取的服务名
        String rpcServiceName = rpcRequest.getRpcServiceName();
        CuratorFramework zkClient = CuratorUtils.getZkClient();
        List<String> serviceUrlList = CuratorUtils.getChildrenNodes(zkClient, rpcServiceName);
        if (CollectionUtils.isEmpty(serviceUrlList)) {
            throw new RpcException(RpcErrorMessageEnum.SERVICE_CAN_NOT_BE_FOUND, rpcServiceName);
        }
        // 通过均衡获取指定服务下的某个节点，节点的值为hostname:port
        String targetServiceUrl = loadBalance.selectServiceAddress(serviceUrlList,rpcRequest);
        log.info("负载均衡成功，选取的服务url为:[{}]", targetServiceUrl);
        String[] socketAddressArray = targetServiceUrl.split(":");
        String host = socketAddressArray[0];
        int port = Integer.parseInt(socketAddressArray[1]);
        return new InetSocketAddress(host, port);

    }
}
