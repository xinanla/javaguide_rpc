package github.xinan.remoting.constants;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Rpc数据传输用到的常量
 *
 * @author xinan
 * @date 2022-05-23 19:54
 */
public class RpcConstants {

    /**
     * Magic number. Verify RpcMessage
     */
    public static final byte[] MAGIC_NUMBER = new byte[]{(byte) 'g', (byte) 'r', (byte) 'p', (byte) 'c' };
    /**
     * 默认编码方式
     */
    public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    /**
     * 版本信息
     */
    public static final byte VERSION = 1;
    /**
     * 消息头长度
     */
    public static final byte TOTAL_LENGTH = 16;
    public static final byte REQUEST_TYPE = 1;
    public static final byte RESPONSE_TYPE = 2;

    public static final byte HEARTBEAT_REQUEST_TYPE = 3;

    public static final byte HEARTBEAT_RESPONSE_TYPE = 4;
    /**
     * 心跳回应消息头长度
     */
    public static final int HEAD_LENGTH = 16;
    public static final String PING = "ping";
    public static final String PONG = "pong";
    /**
     * 最大消息总长度
     */
    public static final int MAX_FRAME_LENGTH = 8 * 1024 * 1024;
}
