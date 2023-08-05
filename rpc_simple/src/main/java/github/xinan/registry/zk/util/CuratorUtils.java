package github.xinan.registry.zk.util;

import github.xinan.enums.RpcConfigEnum;
import github.xinan.utils.PropertiesFileUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;


/**
 * Curator(zookeeper client) utils
 * zookeeper的客户端工具，实现了获取client客户端、注册监听、获取服务子节点、创建节点、清除注册服务等功能
 *
 * @author xinan
 * @date 2022-05-26 19:32
 */
@Slf4j
public class CuratorUtils {
    private static final int BASE_SLEEP_TIME = 1000;
    private static final int MAX_RETRIES = 3;
    public static final String ZK_REGISTER_ROOT_PATH = "/my-rpc";
    /**
     * 储存所有服务的子节点map,一个服务可能会有多个节点
     */
    private static final Map<String, List<String>> SERVICE_ADDRESS_MAP = new ConcurrentHashMap<>();
    /**
     * 储存zookeeper中已存在服务节点路径的set,每个元素代表一个服务的路径，格式为 /my-rpc/github.xinan.HelloService/127.0.0.1:9999
     */
    private static final Set<String> REGISTERED_PATH_SET = ConcurrentHashMap.newKeySet();
    private static CuratorFramework zkClient;
    private static final String DEFAULT_ZOOKEEPER_ADDRESS = "127.0.0.1:2181";

    private CuratorUtils(){}

    /**
     * Create persistent nodes. Unlike temporary nodes, persistent nodes are not removed when the client disconnects
     *
     * @param path node path
     */
    public static void createPersistentNode(CuratorFramework zkClient, String path) {
        try {
            if (REGISTERED_PATH_SET.contains(path) || zkClient.checkExists().forPath(path) != null) {
                // 如果节点已经存在则打印提示
                log.info("该节点已存在，节点为：[{}]",path);
            } else {
                // 节点不存在则加节点添加到zookeeper中，并加入set保存
                zkClient.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(path);
                log.info("该节点创建成功，节点为：[{}]", path);
            }
            REGISTERED_PATH_SET.add(path);
        } catch (Exception e) {
            log.error("创建节点失败，失败的节点是：[{}]",path);
        }
    }
    /**
     * Gets the children under a node
     * 获得指定rpc服务下的所有节点(因为一个服务可能会有多个服务端实现)
     *
     * @param rpcServiceName rpc service name. eg:github.xinan.HelloServicetest2version1
     * @return All child nodes under the specified node
     */
    public static List<String> getChildrenNodes(CuratorFramework zkClient, String rpcServiceName) {
        if (SERVICE_ADDRESS_MAP.containsKey((rpcServiceName))) {
            return SERVICE_ADDRESS_MAP.get(rpcServiceName);
        }
        List<String> result = null;
        String servicePath = ZK_REGISTER_ROOT_PATH + "/" + rpcServiceName;

        try {
            result = zkClient.getChildren().forPath(servicePath);
            SERVICE_ADDRESS_MAP.put(rpcServiceName, result);
            registerWatcher(rpcServiceName, zkClient);
        } catch (Exception e) {
            log.error("获取子节点失败，失败的路径为：[{}]", servicePath);
        }
        return result;
    }

    /**
     * 将服务名路径下的所有子节点加入监听
     * TODO 加入监听的是啥
     * @param rpcServiceName
     * @param zkClient
     */
    private static void registerWatcher(String rpcServiceName, CuratorFramework zkClient) throws Exception {
        String servicePath = ZK_REGISTER_ROOT_PATH + "/" + rpcServiceName;
        // 监听servicePath路径下子节点的变化情况，自定义监听器，如果该路径下节点发生变化，则将新的子节点List更新到map中
        // cacheData为true表示会在节点列表更新时获取节点内容
        PathChildrenCache pathChildrenCache = new PathChildrenCache(zkClient, servicePath, true);
        PathChildrenCacheListener pathChildrenCacheListener = (curatorFramework, pathChildrenCacheEvent) -> {
            List<String> serviceAddress = curatorFramework.getChildren().forPath(servicePath);
            SERVICE_ADDRESS_MAP.put(rpcServiceName, serviceAddress);
        };
        pathChildrenCache.getListenable().addListener(pathChildrenCacheListener);
        pathChildrenCache.start();
    }

    /**
     * Empty the registry of data <p>
     * 清空所有已注册的服务
     */
    public static void clearRegistry(CuratorFramework zkClient, InetSocketAddress inetSocketAddress) {
        REGISTERED_PATH_SET.stream().parallel().forEach(k -> {
            try {
                if (k.endsWith(inetSocketAddress.toString())) {
                    zkClient.delete().forPath(k);
                }

            } catch (Exception e) {
                log.info("路径[{}]下的注册清除失败",k);
            }
        });
        log.info("所有注册的节点已经全部清除:[{}]",REGISTERED_PATH_SET.toString());
    }

    /**
     * 创建zkClient客户端
     * @return
     */
    public static CuratorFramework getZkClient() {
        // check if user has set zk address 先判断用户是否在配置文件中自定义了zookeeper的配置文件
        Properties properties = PropertiesFileUtil.readPropertiesFile(RpcConfigEnum.RPC_CONFIG_PATH.getPropertyValue());
        String zookeeperAddress = "";
        if (properties != null && properties.getProperty(RpcConfigEnum.ZK_ADDRESS.getPropertyValue()) != null) {
            zookeeperAddress = properties.getProperty(RpcConfigEnum.ZK_ADDRESS.getPropertyValue());
        } else {
            zookeeperAddress = DEFAULT_ZOOKEEPER_ADDRESS;
        }

        if (zkClient != null && zkClient.getState() == CuratorFrameworkState.STARTED) {
            return zkClient;
        }

        // 创建zkClient并启动
        // Retry strategy. Retry 3 times, and will increase the sleep time between retries.
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(BASE_SLEEP_TIME, MAX_RETRIES);
        zkClient = CuratorFrameworkFactory.builder()
                .connectString(zookeeperAddress)
                .retryPolicy(retryPolicy)
                .build();
        zkClient.start();

        try {
            // 等待zookeeper连接30s,如果30s还未连接成功则报错
            if (!zkClient.blockUntilConnected(30, TimeUnit.SECONDS)) {
                throw new RuntimeException("连接zookeeper超时");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return zkClient;
    }



}
