package serialize;

import github.xinan.remoting.dto.RpcRequest;
import github.xinan.serialize.kryo.KryoSerializer;
import org.junit.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * TODO
 *
 * @author xinan
 * @date 2022-05-25 20:41
 */
public class KryoSerializerTest {
    @Test
    public void kryoTest() {
        RpcRequest target = RpcRequest.builder()
                .methodName("hello")
                .parameters(new Object[]{"sayhello", "saygoodbye"})
                .paramTypes(new Class<?>[]{String.class, String.class})
                .requestId(123 + "")
                .group("group1")
                .version("version1")
                .build();
        KryoSerializer kryoSerializer = new KryoSerializer();
        byte[] bytes = kryoSerializer.serialize(target);
        RpcRequest result = kryoSerializer.deserialize(bytes, RpcRequest.class);
        assertEquals(target.getGroup(),result.getGroup());
        assertEquals(target.getVersion(), result.getVersion());
        assertEquals(target.getRequestId(), result.getRequestId());
    }
}
