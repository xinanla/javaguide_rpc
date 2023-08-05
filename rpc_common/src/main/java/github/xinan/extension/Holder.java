package github.xinan.extension;

/**
 * 数据持有包装类
 *
 * @author xinan
 * @date 2022-05-23 20:56
 */
public class Holder<T> {
    private volatile T value;
    public T get() {
        return value;
    }

    public void set(T value) {
        this.value = value;
    }

}
