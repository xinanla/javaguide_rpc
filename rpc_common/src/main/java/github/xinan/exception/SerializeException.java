package github.xinan.exception;

/**
 * 自定义的序列化异常
 *
 * @author xinan
 * @date 2022-05-24 20:20
 */
public class SerializeException extends RuntimeException {
    public SerializeException(String message) {
        super(message);
    }
}
