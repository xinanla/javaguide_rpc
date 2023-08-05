package github.xinan.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString
public enum RpcResponseCodeEnum {

    /**
     * 响应成功的状态码及信息
     */
    SUCCESS(200,"请求响应成功"),
    /**
     * 响应失败的状态码及信息
     */
    FAIL(500, "请求响应失败");

    private final int code;
    private final String message;
}
