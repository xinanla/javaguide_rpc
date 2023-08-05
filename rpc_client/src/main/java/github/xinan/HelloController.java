package github.xinan;

import github.xinan.annotation.RpcReference;
import github.xinan.annotation.RpcService;
import org.springframework.stereotype.Component;

/**
 * 客户端测试类
 *
 * @author xinan
 * @date 2022-05-31 22:35
 */
@Component
public class HelloController {
    @RpcReference(version = "version2", group = "test2")
    private HelloService helloService;

    public void test() throws InterruptedException {
        String hello = this.helloService.hello(new Hello("客户端发送的信息", "滴滴滴"));
        assert "这是来自服务端的HelloServiceImpl方法的响应滴滴滴".equals(hello);
        Thread.sleep(12000);
        for (int i = 0; i < 10; i++) {
            System.out.println(helloService.hello(new Hello("消息" + i, "第" + i+ "条消息")));
        }
    }
}
