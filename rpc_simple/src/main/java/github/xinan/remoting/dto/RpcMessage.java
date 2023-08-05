package github.xinan.remoting.dto;

import lombok.*;

/**
 * rpc通信过程中信息包装类，其中包含了信息类型，信息编码方式，信息压缩方式，请求id，具体内容,目的是为了生成消息头的信息
 *
 * @author xinan
 * @date 2022-05-23 19:49
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class RpcMessage {

    /**
     * rpc message type
     */
    private byte messageType;
    /**
     * serialization type
     */
    private byte codec;
    /**
     * compress type
     */
    private byte compress;
    /**
     * request id
     */
    private int requestId;
    /**
     * request data
     */
    private Object data;

}
