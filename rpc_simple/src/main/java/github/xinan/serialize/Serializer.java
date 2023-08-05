package github.xinan.serialize;

import github.xinan.extension.SPI;
/**
 * 序列化接口，所有序列化类都要实现这个接口
 *
 * @author xinan
 */
@SPI
public interface Serializer {
    /**
     * 将对象序列化成字节数据
     * @param obj 序列化的对象
     * @return 序列化后的字节数组
     */
    byte[] serialize(Object obj);

    /**
     * 将字节数据反序列化成指定的对象
     * @param bytes 反序列化的字节数组
     * @param clazz 需要反序列化成的对象的class类型
     * @return 生成的对象
     */
    <T> T deserialize(byte[] bytes, Class<T> clazz);
}
