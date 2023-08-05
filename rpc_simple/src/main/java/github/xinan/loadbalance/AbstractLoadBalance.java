package github.xinan.loadbalance;

import github.xinan.remoting.dto.RpcRequest;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * TODO
 *
 * @author xinan
 * @date 2022-05-26 21:37
 */
public abstract class AbstractLoadBalance implements LoadBalance{
    /**
     * 选择合适的服务节点
     *
     * @param serviceAddresses 指定服务的所有子节点列表：eg:127.0.0.1:9999
     * @param rpcRequest     rpc请求
     * @return 选中的服务节点
     */
    @Override
    public String selectServiceAddress(List<String> serviceAddresses, RpcRequest rpcRequest) {
        if (CollectionUtils.isEmpty(serviceAddresses)) {
            return null;
        } else if (serviceAddresses.size() == 1) {
            return serviceAddresses.get(0);
        } else {
            return doSelect(serviceAddresses, rpcRequest);
        }
    }

    /**
     * 使用负载均衡策略选择合适的服务节点
     * @param serviceAddresses
     * @param rpcRequest
     * @return
     */
    protected abstract String doSelect(List<String> serviceAddresses, RpcRequest rpcRequest);
}
