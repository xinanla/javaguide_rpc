package github.xinan.annotation;

import github.xinan.spring.CustomScannerRegistrar;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * scan custom annotations
 * 这是自定义RPC的扫描注解，该注解类中有basePackage属性，在注解时填写需要扫描的路径basePackage
 * spring就会使用该自定义的扫描器去额外扫描对应路径下标识了@PpcService的类，并加入到bean中
 * TODO @Import注解的作用？https://zhuanlan.zhihu.com/p/147025312
 *
 *
 * @author xinan
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Import(CustomScannerRegistrar.class)
public @interface RpcScan {
    String[] basePackage();
}
