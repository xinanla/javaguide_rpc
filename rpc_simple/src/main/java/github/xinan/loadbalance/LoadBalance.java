package github.xinan.loadbalance;

import github.xinan.extension.SPI;
import github.xinan.remoting.dto.RpcRequest;

import java.util.List;

/**
 * 负载均衡策略接口
 * @author xinan
 */
@SPI
public interface LoadBalance {

    /**
     * 选择合适的服务节点
     * @param serviceAddresses 所有的服务节点
     * @param rpcRequest rpc请求
     * @return 选中的服务节点
     */
    String selectServiceAddress(List<String> serviceAddresses, RpcRequest rpcRequest);
}
