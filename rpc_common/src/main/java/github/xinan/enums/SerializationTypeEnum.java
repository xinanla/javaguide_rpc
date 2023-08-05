package github.xinan.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 序列化器的枚举类型
 * @author xinan
 */
@AllArgsConstructor
@Getter
public enum SerializationTypeEnum {
    /**
     * 序列化器之一：kryo
     */
    KYRO((byte)0x01, "kyro");

    private final byte code;
    private final String name;

    public static String getName(byte code) {
        for (SerializationTypeEnum c : SerializationTypeEnum.values()) {
            if (c.getCode() == code) {
                return c.getName();
            }
        }
        return null;
    }
}
