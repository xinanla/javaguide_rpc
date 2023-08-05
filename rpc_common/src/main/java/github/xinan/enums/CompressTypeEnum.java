package github.xinan.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 压缩类型枚举类
 *
 * @author xinan
 * @date 2022-05-23 20:12
 */
@AllArgsConstructor
@Getter
public enum CompressTypeEnum {
    /**
     * 压缩类型之一：使用gzip压缩，使用一个字节表示，值为0x01
     */
    GZIP((byte)0x01, "gzip");

    private final byte code;
    private final String name;
    public static String getName(byte code) {
        for (CompressTypeEnum c : CompressTypeEnum.values()) {
            if (c.getCode() == code) {
                return c.name;
            }
        }
        return null;
    }
}
