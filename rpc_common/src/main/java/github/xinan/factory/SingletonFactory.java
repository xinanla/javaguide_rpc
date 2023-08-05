package github.xinan.factory;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 获取实体类单例的工厂
 *
 * @author xinan
 * @date 2022-05-25 21:25
 */
public class SingletonFactory {
    /**
     * 储存多个实例的map,key是实例的类名，value是实例。
     * 该map定义为final，则储存的value实例同样是单例的
     */
    private static final Map<String, Object> OBJECT_MAP = new ConcurrentHashMap<>();

    private SingletonFactory() {}

    public static <T> T getInstance(Class<T> clazz) {
        if (clazz == null) {
            throw new IllegalArgumentException("参数需要是class对象");
        }

        String key = clazz.toString();
        if (OBJECT_MAP.containsKey(key)) {
            // 如果map中有则获取
            return clazz.cast(OBJECT_MAP.get(key));
        } else {
            // 如果没有则使用类的构造器构建一个并放入map中
            // 因为是concurrentMap所以不用担心普通单例模式线程不安全的问题
            return clazz.cast(OBJECT_MAP.computeIfAbsent(key,k -> {
                try {
                    return clazz.getDeclaredConstructor().newInstance();
                } catch (Exception e) {
                    throw new RuntimeException(e.getMessage(), e);
                }
            }));
        }

    }
}
