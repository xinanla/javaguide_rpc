package github.xinan.remoting.handler;

import github.xinan.enums.RpcErrorMessageEnum;
import github.xinan.exception.RpcException;
import github.xinan.factory.SingletonFactory;
import github.xinan.provider.Impl.ZkServiceProviderImpl;
import github.xinan.provider.ServiceProvider;
import github.xinan.remoting.dto.RpcRequest;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Rpc请求处理类，负责将Rpc请求中对申请服务方法的调用结果返回
 *
 * @author xinan
 * @date 2022-05-29 21:47
 */
@Slf4j
public class RpcRequestHandler {
    private final ServiceProvider serviceProvider;

    public RpcRequestHandler() {
        this.serviceProvider = SingletonFactory.getInstance(ZkServiceProviderImpl.class);
    }
    /**
     * Processing rpcRequest: call the corresponding method, and then return the method
     * 处理rpcRequest,调用request请求中想调用的服务
     */
    public Object handle(RpcRequest rpcRequest) {
        Object service = serviceProvider.getService(rpcRequest.getRpcServiceName());
        return invokeTargetMethod(rpcRequest, service);
    }

    /**
     * get method execution results
     * 根据rpcRequest中想调用的服务名，以及参数，利用反射让service对象去调用方法
     *
     * @param rpcRequest client request
     * @param service    service object
     * @return the result of the target method execution
     */
    private Object invokeTargetMethod(RpcRequest rpcRequest, Object service) {
        Object result = null;
        try {
            Method method = service.getClass().getMethod(rpcRequest.getMethodName(), rpcRequest.getParamTypes());
            result = method.invoke(service, rpcRequest.getParameters());
            log.info("服务[{}]成功调用方法: [{}]", rpcRequest.getInterfaceName(), rpcRequest.getMethodName());
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RpcException("方法调用失败",e);
        }
        return result;
    }
}
