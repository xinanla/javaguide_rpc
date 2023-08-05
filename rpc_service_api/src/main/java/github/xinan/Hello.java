package github.xinan;

import lombok.*;

import java.io.Serializable;

/**
 * 服务方法需要的参数
 *
 * @author xinan
 * @date 2022-05-31 20:51
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class Hello implements Serializable {
    private String message;
    private String description;
}
