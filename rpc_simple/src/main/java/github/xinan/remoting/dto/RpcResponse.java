package github.xinan.remoting.dto;

import github.xinan.enums.RpcResponseCodeEnum;
import lombok.*;

import java.io.Serializable;

/**
 * Rpc响应类，用于回应客户端的Rpc请求，提供响应的结果，客户端通过该类判断请求是否成功，以及响应成功的内容
 *
 * @author xinan
 * @date 2022-05-24 19:22
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class RpcResponse<T> implements Serializable {

    private static final long serialVersionUID = 715745410605631233L;
    /**
     * 响应的请求id，通过判断客户端发送的请求id和收到响应中的请求id是否相等，从而确定是否是对应的响应
     */
    private String requestId;
    /**
     * 响应的状态码，响应成功和失败的状态码不同
     */
    private Integer code;
    /**
     * 响应附带的通知消息
     */
    private String message;
    /**
     * 如果响应成功，返回RpcRequest中想到得到数据
     */
    private T data;

    /**
     * 返回成功状态的响应
     * @param data 响应给客户端的数据
     * @param requestId 响应的id
     * @return 响应对象
     */
    public static <T> RpcResponse<T> success(T data, String requestId) {
        RpcResponse<T> rpcResponse = new RpcResponse<>();
        rpcResponse.setCode(RpcResponseCodeEnum.SUCCESS.getCode());
        rpcResponse.setMessage(RpcResponseCodeEnum.SUCCESS.getMessage());
        rpcResponse.setRequestId(requestId);
        if (null != data) {
            rpcResponse.setData(data);
        }
        return rpcResponse;
    }

    public static <T> RpcResponse<T> fail() {
        RpcResponse<T> rpcResponse = new RpcResponse<>();
        rpcResponse.setCode(RpcResponseCodeEnum.FAIL.getCode());
        rpcResponse.setMessage(RpcResponseCodeEnum.FAIL.getMessage());
        return rpcResponse;
    }
}
