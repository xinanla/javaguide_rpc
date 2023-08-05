package github.xinan.remoting.transport.netty.codec;

import github.xinan.compress.Compress;
import github.xinan.enums.CompressTypeEnum;
import github.xinan.enums.SerializationTypeEnum;
import github.xinan.extension.ExtensionLoader;
import github.xinan.remoting.constants.RpcConstants;
import github.xinan.remoting.dto.RpcMessage;
import github.xinan.remoting.dto.RpcRequest;
import github.xinan.remoting.dto.RpcResponse;
import github.xinan.serialize.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;


/**
 * 将发送的请求RpcMessage进行编码，转成字节流发送出去
 * <p>
 * custom protocol decoder
 * <p>
 * <pre>
 *   0     1     2     3     4        5     6     7     8         9          10      11     12  13  14   15 16
 *   +-----+-----+-----+-----+--------+----+----+----+------+-----------+-------+----- --+-----+-----+-------+
 *   |   magic   code        |version | full length         | messageType| codec|compress|    RequestId       |
 *   +-----------------------+--------+---------------------+-----------+-----------+-----------+------------+
 *   |                                                                                                       |
 *   |                                         body                                                          |
 *   |                                                                                                       |
 *   |                                        ... ...                                                        |
 *   +-------------------------------------------------------------------------------------------------------+
 * 4B  magic code（魔法数）   1B version（版本）   4B full length（消息长度）    1B messageType（消息类型）
 * 1B compress（压缩类型） 1B codec（序列化类型）    4B  requestId（请求的Id）
 * body（object类型数据）
 * </pre>
 *
 * // TODO 通过协议的长度字段解决了粘包问题
 *
 * @author xinan
 * @see <a href="https://zhuanlan.zhihu.com/p/95621344">LengthFieldBasedFrameDecoder解码器</a>
 */
@Slf4j
public class RpcMessageDecoder extends LengthFieldBasedFrameDecoder {
    /**
     * lengthFieldOffset: magic code is 4B, and version is 1B, and then full length. so value is 5
     * lengthFieldLength: full length is 4B. so value is 4
     * lengthAdjustment: full length include all data and read 9 bytes before, so the left length is (fullLength-9). so values is -9
     * initialBytesToStrip: we will check magic code and version manually, so do not strip any bytes. so values is 0
     */
    public RpcMessageDecoder() {
        this(RpcConstants.MAX_FRAME_LENGTH, 5, 4, -9, 0);
    }

    /**
     * @param maxFrameLength      Maximum frame length. It decide the maximum length of data that can be received.
     *                            If it exceeds, the data will be discarded.
     * @param lengthFieldOffset   Length field offset. The length field is the one that skips the specified length of byte.
     * @param lengthFieldLength   The number of bytes in the length field.
     * @param lengthAdjustment    The compensation value to add to the value of the length field
     * @param initialBytesToStrip Number of bytes skipped.
     *                            If you need to receive all of the header+body data, this value is 0
     *                            if you only want to receive the body data, then you need to skip the number of bytes consumed by the header.
     */
    public RpcMessageDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength,
                             int lengthAdjustment, int initialBytesToStrip) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        // 大致意思是将in中的数据根据构造器中的几个参数进行读取然后包装成一个对象
        Object decoded = super.decode(ctx, in);
        if (decoded instanceof ByteBuf) {
            // 将对象转换成字节数组，开始读取
            ByteBuf frame = (ByteBuf) decoded;

            if (frame.readableBytes() >= RpcConstants.TOTAL_LENGTH) {
                try {
                    return decodeFrame(frame);
                } catch (Exception e) {
                    log.error("解帧失败", e);
                } finally {
                    frame.release();
                }
            }
        }
        return decoded;
    }

    /**
     * 将字节数组解码成RpcMessage对象
     * @param in 输入的字节数组
     * @return RpcMessage对象
     */
    private Object decodeFrame(ByteBuf in) {
        checkMagicNumber(in);
        checkVersion(in);

        int fullLength = in.readInt();
        byte messageType = in.readByte();
        byte codecType = in.readByte();
        byte compressType = in.readByte();
        int requestId = in.readInt();


        RpcMessage rpcMessage = RpcMessage.builder()
                .codec(codecType)
                .requestId(requestId)
                .messageType(messageType).build();
        // 判断是否是心跳请求和响应
        if (messageType == RpcConstants.HEARTBEAT_REQUEST_TYPE) {
            rpcMessage.setData(RpcConstants.PING);
            return rpcMessage;
        }
        if (messageType == RpcConstants.HEARTBEAT_RESPONSE_TYPE) {
            rpcMessage.setData(RpcConstants.PONG);
            return rpcMessage;
        }

        int bodyLength = fullLength - RpcConstants.HEAD_LENGTH;
        if (bodyLength > 0) {
            // 取出消息中的body部分数据
            byte[] bs = new byte[bodyLength];
            in.readBytes(bs);
            // 将消息中body解压缩
            String compressName = CompressTypeEnum.getName(compressType);
            Compress compress = ExtensionLoader.getExtensionLoader(Compress.class).getExtension(compressName);
            bs = compress.decompress(bs);
            // 解压缩后反序列化
            String codecName = SerializationTypeEnum.getName(codecType);
            Serializer serializer = ExtensionLoader.getExtensionLoader(Serializer.class).getExtension(codecName);
            // 根据类型创建不同的类型
            if (messageType == RpcConstants.REQUEST_TYPE) {
                RpcRequest tmpValue = serializer.deserialize(bs, RpcRequest.class);
                rpcMessage.setData(tmpValue);
            } else {
                RpcResponse tempValue = serializer.deserialize(bs, RpcResponse.class);
                rpcMessage.setData(tempValue);
            }
        }
        return rpcMessage;

    }

    /**
     * 检查信息中的版本
     * @param in
     */
    private void checkVersion(ByteBuf in) {
        byte version = in.readByte();

        if (version != RpcConstants.VERSION) {
            throw new RuntimeException("版本不一致" + version);
        }
    }

    /**
     * 检查信息中的魔数
     * @param in
     */
    private void checkMagicNumber(ByteBuf in) {
        int len = RpcConstants.MAGIC_NUMBER.length;
        byte[] temp = new byte[len];
        in.readBytes(temp);

        for (int i = 0; i < 4; i++) {
            if (temp[i] != RpcConstants.MAGIC_NUMBER[i]) {
                throw new IllegalArgumentException("错误的魔数" + Arrays.toString(temp));
            }
        }
    }


}
