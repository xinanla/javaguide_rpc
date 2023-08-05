package github.xinan.annotation;

import java.lang.annotation.*;

/**
 * 服务注册标记，标记了该注解的类会被Spring自动注册到RpcServer中的zookeeper中
 *
 * @author xinan
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Inherited
public @interface RpcService {
    /**
     * 服务的版本号
     * @return
     */
    String version() default "";

    /**
     * 服务的组号
     * @return
     */
    String group() default "";

}
