package github.xinan.registry.zk;

import github.xinan.registry.ServiceRegistry;
import github.xinan.registry.zk.util.CuratorUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;

import java.net.InetSocketAddress;

/**
 * TODO
 *
 * @author xinan
 * @date 2022-05-27 21:56
 */
@Slf4j
public class ZkServiceRegistryImpl implements ServiceRegistry {

    /**
     * 使用服务的服务名和对应的socket注册服务
     *
     * @param rpcServiceName    服务名
     * @param inetSocketAddress 服务端的套接字
     */
    @Override
    public void registerService(String rpcServiceName, InetSocketAddress inetSocketAddress) {
        //生成服务对应的节点名称，节点名称格式为:zookeeper根节点名称/服务名称/服务地址
        // inetSocket生成的字符串格式为/hostname:port
        String servicePath = CuratorUtils.ZK_REGISTER_ROOT_PATH + "/" + rpcServiceName + inetSocketAddress.toString();
        CuratorFramework zkClient = CuratorUtils.getZkClient();
        CuratorUtils.createPersistentNode(zkClient, servicePath);
    }
}
