package github.xinan.utils;

/**
 * 获取运行时某些状态的工具类
 *
 * @author xinan
 * @date 2022-05-28 20:31
 */
public class RuntimeUtil {
    /**
     * 获取CPU的核心数
     *
     * @return cpu的核心数
     */
    public static int cpus() {
        return Runtime.getRuntime().availableProcessors();
    }
}
