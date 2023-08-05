package github.xinan.remoting.dto;

import lombok.*;

import java.io.Serializable;

/**
 * Rpc请求类
 *
 * @author xinan
 * @date 2022-05-24 19:22
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
@ToString
public class RpcRequest implements Serializable {

    private static final long serialVersionUID = 1905122041950251207L;

    private String requestId;

    private String interfaceName;

    private String methodName;

    private Object[] parameters;

    private Class<?>[] paramTypes;

    private String version;

    private String group;

    /**
     * 获取服务名，格式为接口名+组名+版本号
     * @return
     */
    public String getRpcServiceName() {
        return this.interfaceName + this.group + this.version;
    }
}
