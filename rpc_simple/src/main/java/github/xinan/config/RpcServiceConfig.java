package github.xinan.config;

import lombok.*;

/**
 * TODO
 *
 * @author xinan
 * @date 2022-05-28 11:04
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class RpcServiceConfig {
    /**
     * service version
     */
    private String version = "";
    /**
     * when the interface has multiple implementation classes, distinguish by group
     */
    private String group = "";
    /**
     * target service
     */
    private Object service;


    public String getServiceName() {
        return this.service.getClass().getInterfaces()[0].getCanonicalName();
    }

    /**
     * 获取rpc服务名
     * @return
     */
    public String getRpcServiceName() {
        return getServiceName() + this.getGroup() + this.getVersion();
    }
}
