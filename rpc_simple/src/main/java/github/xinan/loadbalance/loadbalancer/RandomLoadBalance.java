package github.xinan.loadbalance.loadbalancer;

import github.xinan.loadbalance.AbstractLoadBalance;
import github.xinan.remoting.dto.RpcRequest;

import java.util.List;
import java.util.Random;

/**
 * TODO
 *
 * @author xinan
 * @date 2022-06-05 15:45
 */
public class RandomLoadBalance extends AbstractLoadBalance {

    @Override
    protected String doSelect(List<String> serviceAddresses, RpcRequest rpcRequest) {
        Random random = new Random();
        return serviceAddresses.get(random.nextInt(serviceAddresses.size()));
    }
}
