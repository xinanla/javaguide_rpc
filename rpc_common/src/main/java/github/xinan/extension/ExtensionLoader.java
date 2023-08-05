package github.xinan.extension;

import lombok.extern.slf4j.Slf4j;
import github.xinan.utils.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * TODO 为什么要使用dubbo的SPI机制呢
 * 实现了使用ExtensionLoader加载类获取对象的功能
 * refer to dubbo spi: https://dubbo.apache.org/zh/docs/v2.7/dev/source/dubbo-spi/
 *
 * @author xinan
 * @date 2022-05-23 20:58
 */
@Slf4j
public final class ExtensionLoader<T> {
    // TODO 为啥有些用static有些不用呢

    /**
     * 储存每个接口class对应的ExtensionLoader，当想加载接口A的实现类时，通过接口A的class类型映射到相应的ExtensionLoader&lt;A&gt;
     * <p> 每个ExtensionLoader负责对应接口class实现类的加载
     */
    private static final Map<Class<?>, ExtensionLoader<?>> EXTENSION_LOADERS = new ConcurrentHashMap<>();
    /**
     * 存储 <接口实现类名，储存实现类实例对象的holder>的map
     */
    private final Map<String, Holder<Object>> cachedInstances = new ConcurrentHashMap<>();
    /**
     * 存储<接口实现类的class,实现类的实例对象>的map,根据接口实现类的class获取该class的实例对象
     */
    private static final Map<Class<?>, Object> EXTENSION_INSTANCES = new ConcurrentHashMap<>();
    /**
     * 存储map的holder容器，map中保存的是<接口的实现类的变量名,实现类的class>的key-value键值对
     */
    private final Holder<Map<String, Class<?>>> cachedClasses = new Holder<>();
    /**
     * 需要加载实例化的接口class类型
     */
    private final Class<?> type;
    /**
     * 用于加载spi配置文件的目录
     */
    private static final String SERVICE_DIRECTORY = "META-INF/extensions/";

    private ExtensionLoader(Class<?> type) {
        this.type = type;
    }
    /**
     * 根据传入的class类类型，返回包含对应类型的ExtensionLoader
     * @param type class类型
     * @return  返回的ExtensionLoader带有的类型为&lt;S&gt;
     */
    public static <S> ExtensionLoader<S> getExtensionLoader(Class<S> type) {
        if (type == null) {
            throw new IllegalArgumentException("扩展接口参数不能为空");
        }
        if (!type.isInterface()) {
            throw new IllegalArgumentException("扩展接口参数类型需要是接口");
        }

        if (type.getAnnotation(SPI.class) == null) {
            throw new IllegalArgumentException("扩展接口类型必须带有SPI注解");
        }

        ExtensionLoader<S> extensionLoader = (ExtensionLoader<S>)EXTENSION_LOADERS.get(type);
        if (extensionLoader == null) {
            EXTENSION_LOADERS.putIfAbsent(type,new ExtensionLoader<S>(type));
            extensionLoader = (ExtensionLoader<S>) EXTENSION_LOADERS.get(type);
        }
        return extensionLoader;

    }

    /**
     * 根据对接口实现类名获取实现类的实例对象
     * @param name 接口名
     * @return 扩展类
     */
    public T getExtension(String name) {
        if (StringUtils.isBlank(name)) {
            throw new IllegalArgumentException("扩展类不应该为空");
        }
        // 从缓存中获取实现接口的实例，缓存没有则创建并放入缓存
        Holder<Object> holder = cachedInstances.get(name);
        if (holder == null) {
            cachedInstances.putIfAbsent(name, new Holder<>());
            holder = cachedInstances.get(name);
        }

        Object instance = holder.get();

        // 这里使用单例模式的创建方式，因为instance只会存在一个，且保存在holder中，当instance第一次创建时需要保证安全性
        // 使用双重检查锁避免单例模式出问题 https://www.cnblogs.com/xz816111/p/8470048.html
        if (instance == null) {
            // 可能会有多个线程同时进入第一个判null语句，这时使用synchronize让线程进行同步进行
            synchronized (holder) {
                // 如果前一个线程已经创建了instance实例并放入到了holder中，则后续的线程从holder中取出的实例肯定不为null，自然就退去判断语句
                instance = holder.get();
                if (instance == null) {
                    instance = createExtension(name);
                    holder.set(instance);
                }
            }
        }
        return (T)instance;
    }

    /**
     * 通过缓存获取接口实现类的名称创建实例对象
     * @param name 接口实现类的名称
     * @return name对应的实例对象
     */
    private T createExtension(String name) {
        // 去缓存中获取name对应的全类名
        Class<?> clazz = getExtensionClasses().get(name);
        if (clazz == null) {
            throw new IllegalArgumentException("未获取到扩展类" + name);
        }

        T instance = (T)EXTENSION_INSTANCES.get(clazz);
        if (instance == null) {
            try {
                // 通过全类名生成实例对象并放入缓存中
                EXTENSION_INSTANCES.putIfAbsent(clazz,clazz.newInstance());
                instance = (T)EXTENSION_INSTANCES.get(clazz);
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }
        return instance;
    }
    /**
     * 通过缓存根据获取指定类对象，如果缓存中没有，则去目录加载并设置到缓存中
     * @return 储存 < 接口实现类名,接口实现类的class>的map
     */
    private Map<String, Class<?>> getExtensionClasses() {
        // 去缓存中获取自定义名称对应的全类名，没有则去对应目录下加载
        Map<String, Class<?>> classes = cachedClasses.get();
        if (classes == null) {
            synchronized(cachedClasses) {
                classes = cachedClasses.get();
                if (classes == null) {
                    classes = new HashMap<>();
                    // 去目录加载配置文件并将数据放入缓存
                    loadDirectory(classes);
                    cachedClasses.set(classes);
                }
            }
        }
        return classes;
    }
    /**
     * 将目录下指定接口名字的配置文件中的配置的所有实现类加载
     * @param extensionClasses 存放< 接口实现类名,接口实现类的class>的map
     */
    private void loadDirectory(Map<String, Class<?>> extensionClasses) {
        //type.getName()获取的是全类名，包括整体的目录结构
        String fileName = ExtensionLoader.SERVICE_DIRECTORY + type.getName();

        try {
            // 获取指定路径文件的所有url，因为文件可能有重名?
            Enumeration<URL> urls;
            ClassLoader classLoader = ExtensionLoader.class.getClassLoader();
            urls = classLoader.getResources(fileName);
            if (urls != null) {
                while (urls.hasMoreElements()) {
                    URL resourceUrl = urls.nextElement();
                    // 针对每个配置文件的url，将文件中设置的键值对内容读出并保存在缓存中
                    loadResource(extensionClasses, classLoader, resourceUrl);
                }
            }

        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    /**
     * 加载指定配置文件url的中设置的左右key=value,key是自定义名称，value是类的全路径名
     * @param extensionClasses
     * @param classLoader
     * @param resourceUrl
     */
    private void loadResource(Map<String, Class<?>> extensionClasses, ClassLoader classLoader,URL resourceUrl) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resourceUrl.openStream(), UTF_8))) {
            String line;
            // 读取每一行
            while ((line = reader.readLine()) != null) {
                // 排除注释内容
                final int ci = line.indexOf('#');
                if (ci >= 0) {
                    line = line.substring(0, ci);
                }
                line = line.trim();

                if (line.length() > 0) {
                    try {
                        // 分别获取自定义名称和全路径名，并通过全路径名的字符串去加载类，并放入缓存中
                        final int ei = line.indexOf('=');
                        String name = line.substring(0, ei).trim();
                        String clazzName = line.substring(ei + 1).trim();

                        if (name.length() > 0 && clazzName.length() > 0) {
                            Class<?> clazz = classLoader.loadClass(clazzName);
                            extensionClasses.put(name, clazz);
                        }
                    } catch (Exception e) {
                        log.error(e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

}
