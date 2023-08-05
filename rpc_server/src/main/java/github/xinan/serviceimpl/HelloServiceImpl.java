package github.xinan.serviceimpl;

import github.xinan.Hello;
import github.xinan.HelloService;
import github.xinan.annotation.RpcService;
import lombok.extern.slf4j.Slf4j;

/**
 * TODO
 *
 * @author xinan
 * @date 2022-05-31 21:39
 */
@Slf4j
@RpcService(group = "test1", version = "version1")
public class HelloServiceImpl implements HelloService {
    static {
        System.out.println("HelloServiceImpl1被创建");
    }

    @Override
    public String hello(Hello hello) {
        log.info("HelloServiceImpl1收到：{}", hello.getMessage());
        String result = "这是来自服务端的HelloServiceImpl1方法的响应" + hello.getDescription();
        log.info("HelloServiceImpl1返回：{}", result);
        return result;
    }
}
