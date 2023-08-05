package github.xinan.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * rpc调用中常见错误枚举类
 * @author xinan
 */
@AllArgsConstructor
@Getter
@ToString
public enum RpcErrorMessageEnum {
    /**
     * 客户端失败错误
     */
    CLIENT_CONNECT_SERVER_FAILURE("客户端连接失败!"),
    /**
     * 服务调用失败错误
     */
    SERVICE_INVOCATION_FAILURE("服务调用失败!"),
    /**
     * 服务未发现错误
     */
    SERVICE_CAN_NOT_BE_FOUND("服务未发现!"),
    /**
     * 服务未实现接口错误
     */
    SERVICE_NOT_IMPLEMENTS_ANY_INTERFACE("服务未实现任何接口!"),
    /**
     * 请求和响应不匹配错误
     */
    REQUEST_NOT_MATCH_RESPONSE("请求和响应不匹配!");

    private final String message;
}
