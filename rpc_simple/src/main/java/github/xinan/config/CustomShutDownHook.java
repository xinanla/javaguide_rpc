package github.xinan.config;

import github.xinan.registry.zk.util.CuratorUtils;
import github.xinan.remoting.transport.netty.server.NettyRpcServer;
import github.xinan.utils.concurrent.threadpool.ThreadPoolFactoryUtil;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

/**
 * When the server  is closed, do something such as unregister all services
 *
 * @author xinan
 * @date 2022-05-28 16:42
 */
@Slf4j
public class CustomShutDownHook {
    /**
     * TODO CustomShutDownHook是什么
     */
    private static final CustomShutDownHook CUSTOM_SHUTDOWN_HOOK = new CustomShutDownHook();

    public static CustomShutDownHook getCustomShutDownHook() {
        return CUSTOM_SHUTDOWN_HOOK;
    }

    /**
     * 在系统退出，服务关闭后将所有注册服务关闭，并关闭线程池
     */
    public void clearAl() {
        log.info("添加shutDownHook去清除所有服务");
        // TODO 这是什么，shutDownHook的固定写法么
        Runtime.getRuntime().addShutdownHook(new Thread( () -> {
            try {
                InetSocketAddress inetSocketAddress = new InetSocketAddress(InetAddress.getLocalHost().getHostAddress(), NettyRpcServer.PORT);
                CuratorUtils.clearRegistry(CuratorUtils.getZkClient(),inetSocketAddress);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
            ThreadPoolFactoryUtil.shutDownAllThreadPool();
        }));


    }
}
