package github.xinan.loadbalance.loadbalancer;

import github.xinan.loadbalance.AbstractLoadBalance;
import github.xinan.remoting.dto.RpcRequest;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * refer to dubbo consistent hash load balance: https://github.com/apache/dubbo/blob/2d9583adf26a2d8bd6fb646243a9fe80a77e65d5/dubbo-cluster/src/main/java/org/apache/dubbo/rpc/cluster/loadbalance/ConsistentHashLoadBalance.java
 * 一致性哈希解剖：https://dubbo.apache.org/zh/blog/2019/05/01/dubbo-%E4%B8%80%E8%87%B4%E6%80%A7hash%E8%B4%9F%E8%BD%BD%E5%9D%87%E8%A1%A1%E5%AE%9E%E7%8E%B0%E5%89%96%E6%9E%90/
 *
 * @author xinan
 * @date 2022-05-26 21:38
 */
public class ConsistentHashLoadBalance  extends AbstractLoadBalance {
    private final ConcurrentHashMap<String, ConsistentHashSelector> selectorMap = new ConcurrentHashMap<>();

    /**
     * 使用负载均衡策略选择合适的服务节点
     *
     * @param serviceAddresses
     * @param rpcRequest
     * @return
     */
    @Override
    protected String doSelect(List<String> serviceAddresses, RpcRequest rpcRequest) {
        // 使用系统自带的哈希方法，identityHashCode 用来识别invokers是否发生过变更，无论对象有没有hashcode()方法都会生成哈希值，如果对象为null则返回0
        int identityHashCode = System.identityHashCode(serviceAddresses);
        String rpcServiceName = rpcRequest.getRpcServiceName();
        ConsistentHashSelector selector = selectorMap.get(rpcServiceName);
        if (selector == null || selector.identityHashCode != identityHashCode) {
            // 若不存在"服务名"对应的选择器，或者服务列表的hashcode不一样即服务列表已经发生了变更，则初始化一个选择器,选择器
            selectorMap.put(rpcServiceName, new ConsistentHashSelector(serviceAddresses, 160, identityHashCode));
            selector = selectorMap.get(rpcServiceName);
        }
        // 为啥这里select的参数里，服务名后面还加了请求参数？
        // 原因可能如下：此时的selector内部存储的都是指定服务的节点，不管选哪个节点都可以实现功能，而选节点通过hashCode,hashCode为了公平则由服务名+所有参数生成
        return selector.select(rpcServiceName + Arrays.stream(rpcRequest.getParameters()));

    }


    /**
     * 选择器
     */
    static class ConsistentHashSelector {
        /**
         * 储存Hash值与节点映射关系的TreeMap
         */
        private final TreeMap<Long, String> virtualSelectors;
        /**
         * 用来识别服务列表是否发生变更的Hash码
         */
        private final int identityHashCode;

        ConsistentHashSelector(List<String> invokers, int replicaNumber, int identityHashCode) {
            this.virtualSelectors = new TreeMap<>();
            this.identityHashCode = identityHashCode;

            // 将每个节点分成replicaNumber / 4 份，每一份都用四位的哈希处理并放入selectors中
            for (String invoker : invokers) {
                for (int i = 0; i < replicaNumber / 4; i++) {
                    //对invoker+i进行md5运算得到一个长度为16的字节数组
                    byte[] digest = md5(invoker + i);
                    //对digest部分字节进行4次hash运算得到四个不同的long型正整数
                    for (int h = 0; h < 4; h++) {
                        //h=0时，取digest中下标为0~3的4个字节进行位运算
                        //h=1时，取digest中下标为4~7的4个字节进行位运算
                        //h=2,h=3时过程同上
                        long m = hash(digest, h);
                        virtualSelectors.put(m, invoker);
                    }
                }
            }
        }

        /**
         * TODO 看不懂的哈希方法
         * @param digest
         * @param index
         * @return
         */
        static long hash(byte[] digest, int index) {
            return ((long)(digest[3 + index * 4] & 255) << 24
                    | (long)(digest[2 + index * 4] & 255) << 16
                    | (long)(digest[1 + index * 4] & 255) << 8
                    | (long)(digest[index * 4] & 255)) & 4294967295L;
        }

        /**
         * TODO 看不懂的md5方法
         * @param key
         * @return
         */
        static byte[] md5(String key) {
            MessageDigest md;
            try {
                md = MessageDigest.getInstance("MD5");
                byte[] bytes = key.getBytes(StandardCharsets.UTF_8);
                md.update(bytes);
            } catch (NoSuchAlgorithmException e) {
                throw new IllegalArgumentException(e.getMessage(), e);
            }
            return md.digest();
        }


         public String select(String rpcServiceKey) {
             byte[] digest = md5(rpcServiceKey);
             // TODO 这里hash参数为什么选0
             return selectForKey(hash(digest, 0));
         }

        /**
         * 根据参数字符串的md5编码找到节点
         * @param hashCode
         * @return
         */
        public String selectForKey(long hashCode) {
            // tailMap()返回第一个比参数的哈希code大的entry,如果没有比它大的，就返回null
            Map.Entry<Long, String> entry = virtualSelectors.tailMap(hashCode, true).firstEntry();
            // 如果没有更大的entry,则返回环形哈希的 第一个节点
            if (entry == null) {
                entry = virtualSelectors.firstEntry();
            }
            return entry.getValue();
         }
    }


}
