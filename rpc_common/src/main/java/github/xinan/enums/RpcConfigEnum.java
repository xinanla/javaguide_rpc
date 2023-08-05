package github.xinan.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * rpc相关的配置文件路径枚举类
 * @author xinan
 */
@AllArgsConstructor
@Getter
public enum RpcConfigEnum {
    /**
     * rpc配置文件的路径
     */
    RPC_CONFIG_PATH("rpc.properties"),
    /**
     * 配置的zookeeper地址路径
     */
    ZK_ADDRESS("rpc.zookeeper.address");

    private final String propertyValue;
}
