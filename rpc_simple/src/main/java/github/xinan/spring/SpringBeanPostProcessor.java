package github.xinan.spring;

import github.xinan.annotation.RpcReference;
import github.xinan.annotation.RpcService;
import github.xinan.config.RpcServiceConfig;
import github.xinan.extension.ExtensionLoader;
import github.xinan.factory.SingletonFactory;
import github.xinan.provider.Impl.ZkServiceProviderImpl;
import github.xinan.provider.ServiceProvider;
import github.xinan.proxy.RpcClientProxy;
import github.xinan.remoting.transport.RpcRequestTransport;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;

/**
 * 当bean构造完成后的进行我们自定义注解的相关操作，主要目的是将@RpcService注解的对象注册进zookeeper
 * 将@RpcReference注解的对象使用代理对象代替，代理对象实现对server端的请求
 * 参考 https://www.jianshu.com/p/dcc990d47df1
 *
 * @author xinan
 * @date 2022-05-30 21:13
 */
@Slf4j
@Component
public class SpringBeanPostProcessor implements BeanPostProcessor {
    private final ServiceProvider serviceProvider;
    private final RpcRequestTransport rpcClient;
    public SpringBeanPostProcessor() {
        this.serviceProvider = SingletonFactory.getInstance(ZkServiceProviderImpl.class);
        this.rpcClient = ExtensionLoader.getExtensionLoader(RpcRequestTransport.class).getExtension("netty");
    }

    /**
     * 这里@SneakyThrows注解的作用是解决抛出的异常<p>
     * Spring bean 在实例化之前会调用 postProcessBeforeInitialization()方法，在bean实例化之前将有@RpcService注解的服务注册服务
     * https://www.jianshu.com/p/efa98910fb97
     * @param bean
     * @param beanName
     * @return
     */
    @SneakyThrows
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {

        if (bean.getClass().isAnnotationPresent(RpcService.class)) {
            // 获取有@RpcService注解bean对象
            log.info("[{}]上注解了[{}],对该服务进行注册",bean.getClass().getName(),RpcService.class.getCanonicalName());
            RpcService rpcService = bean.getClass().getAnnotation(RpcService.class);
            // bean指的就是HelloServiceImpl对象，因为自定义了包扫描器，所以有@RpcService注解的类也会生成bean对象
            RpcServiceConfig rpcServiceConfig = RpcServiceConfig.builder()
                    .group(rpcService.group())
                    .version(rpcService.version())
                    .service(bean).build();
            // 将服务注册到zookeeper中
            serviceProvider.publishService(rpcServiceConfig);
        }
        return bean;
    }

    /**
     * 在bean实例化之后将有@RpcReference注解的类进行实例化,生成一个代理类，通过代理类去调用服务端的方法
     * @param bean
     * @param beanName
     * @return
     * @throws BeansException
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> targetClass = bean.getClass();
        Field[] declaredFields = targetClass.getDeclaredFields();
        for (Field declaredField : declaredFields) {
            RpcReference rpcReference = declaredField.getAnnotation(RpcReference.class);
            if (rpcReference != null) {
                RpcServiceConfig rpcServiceConfig = RpcServiceConfig.builder()
                        .group(rpcReference.group())
                        .version(rpcReference.version()).build();
                // 获取代理类对象，代理对象中实现了发送rpcRequest的功能
                RpcClientProxy rpcClientProxy = new RpcClientProxy(rpcClient, rpcServiceConfig);
                Object clientProxy = rpcClientProxy.getProxy(declaredField.getType());
                declaredField.setAccessible(true);
                try {
                    // 将生成的代理类注入到bean对象中，bean对象原本是一个HelloService接口，现在变成了一个HelloService接口的代理类
                    // 后面该对象使用hello方法时就是调用了代理对象的invoke方法
                    declaredField.set(bean, clientProxy);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return bean;
    }
}
