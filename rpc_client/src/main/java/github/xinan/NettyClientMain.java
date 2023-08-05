package github.xinan;

import github.xinan.annotation.RpcScan;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * TODO
 *
 * @author xinan
 * @date 2022-05-31 22:20
 */
@RpcScan(basePackage = {"github.xinan"})
public class NettyClientMain {
    public static void main(String[] args) throws InterruptedException {
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(NettyClientMain.class);
        HelloController helloController = (HelloController)applicationContext.getBean("helloController");
        helloController.test();
    }
}
