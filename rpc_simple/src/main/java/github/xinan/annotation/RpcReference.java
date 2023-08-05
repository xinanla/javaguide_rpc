package github.xinan.annotation;

import java.lang.annotation.*;

/**
 * Rpc服务的调用方，标记了该注解的服务接口，会被spring自动生成接口的代理类并装配，由代理类去调用服务端的对应方法
 * @author xinan
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Inherited
public @interface RpcReference {
    /**
     * 标明服务的版本
     */
    String version() default "";

    /**
     * 标明服务的组
     */
    String group() default "";
}
