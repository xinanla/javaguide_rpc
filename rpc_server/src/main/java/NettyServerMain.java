import github.xinan.annotation.RpcScan;
import github.xinan.config.RpcServiceConfig;
import github.xinan.remoting.transport.netty.server.NettyRpcServer;
import github.xinan.serviceimpl.HelloServiceImpl;
import github.xinan.serviceimpl.HelloServiceImpl2;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * 通过
 *
 * @author xinan
 * @date 2022-05-31 20:57
 */
@RpcScan(basePackage = {"github.xinan"})
public class NettyServerMain {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(NettyServerMain.class);
        NettyRpcServer nettyRpcServer = (NettyRpcServer)applicationContext.getBean("nettyRpcServer");

        HelloServiceImpl2 helloServiceImpl2 = new HelloServiceImpl2();
        RpcServiceConfig rpcServiceConfig = RpcServiceConfig.builder()
                .group("test2")
                .version("version2")
                .service(helloServiceImpl2).build();
        nettyRpcServer.registerService(rpcServiceConfig);
        nettyRpcServer.start();
    }
}
