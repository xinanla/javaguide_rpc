package github.xinan.serialize.kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import github.xinan.exception.SerializeException;
import github.xinan.remoting.dto.RpcResponse;
import lombok.extern.slf4j.Slf4j;
import github.xinan.serialize.Serializer;
import github.xinan.remoting.dto.RpcRequest;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * Kryo序列化器
 *
 * @author xinan
 * @date 2022-05-24 19:17
 */
@Slf4j
public class KryoSerializer implements Serializer {

    private final ThreadLocal<Kryo> kryoThreadLocal = ThreadLocal.withInitial(() -> {
        Kryo kryo = new Kryo();
        kryo.register(RpcResponse.class);
        kryo.register(RpcRequest.class);
        return kryo;
    });
    /**
     * 将对象序列化成字节数据
     *
     * @param obj 序列化的对象
     * @return 序列化后的字节数组
     */
    @Override
    public byte[] serialize(Object obj) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (Output output = new Output(byteArrayOutputStream)) {
            Kryo kryo = kryoThreadLocal.get();
            kryo.writeObject(output, obj);
            kryoThreadLocal.remove();
            return output.toBytes();
        } catch (Exception e) {
            throw new SerializeException("序列化失败");
        }

    }

    /**
     * 将字节数据反序列化成指定的对象
     *
     * @param bytes 反序列化的字节数组
     * @param clazz 需要反序列化成的对象的class类型
     * @return 生成的对象
     */
    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        Object obj;
        try (Input input = new Input(byteArrayInputStream)) {
            Kryo kryo = kryoThreadLocal.get();
            obj = kryo.readObject(input, clazz);
            kryoThreadLocal.remove();
            return clazz.cast(obj);
        } catch (Exception e) {
            throw new SerializeException("反序列化失败");
        }


    }
}
