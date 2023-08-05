package github.xinan.serviceimpl;

import github.xinan.Hello;
import github.xinan.HelloService;
import lombok.extern.slf4j.Slf4j;

/**
 * 注册的另一个服务，该服务通过手动注册进服务端
 *
 * @author xinan
 * @date 2022-05-31 21:33
 */
@Slf4j
public class HelloServiceImpl2 implements HelloService {

    static {
        System.out.println("HelloServiceImpl2被创建");
    }

    @Override
    public String hello(Hello hello) {
        log.info("HelloServiceImpl2收到：{}", hello.getMessage());
        String result = "这是来自服务端的HelloServiceImpl2方法的响应" + hello.getDescription();
        log.info("HelloServiceImpl2返回：{}", result);
        return result;
    }
}
