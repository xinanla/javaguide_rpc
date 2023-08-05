package github.xinan.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * 配置文件读取工具
 *
 * @author xinan
 * @date 2022-05-26 20:08
 */
@Slf4j
public class PropertiesFileUtil {
    private PropertiesFileUtil() {}

    /**
     * 根据文件名来获取配置文件类，文件目录默认是线程的上下文全路径
     * @param fileName 配置文件名称
     * @return 配置文件类
     */
    public static Properties readPropertiesFile(String fileName) {
        URL url = Thread.currentThread().getContextClassLoader().getResource("");
        String rpcConfigPath = "";
        if (url != null) {
            rpcConfigPath = url.getPath() + fileName;
        }

        Properties properties = null;
        try (InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(rpcConfigPath), StandardCharsets.UTF_8)) {
            properties = new Properties();
            properties.load(inputStreamReader);
        } catch (Exception e) {
            log.error("在读取配置文件时发生错误，错误的文件路径为：[{}]",rpcConfigPath);
        }
        return properties;

    }
}
