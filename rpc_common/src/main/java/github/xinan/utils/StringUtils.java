package github.xinan.utils;

/**
 * 字符串工具类
 *
 * @author xinan
 * @date 2022-05-23 21:21
 */
public class StringUtils {
    public static boolean isBlank(String s) {
        if (s == null || s.length() == 0) {
            return true;
        }
        // 排除掉s中都是空格的情况
        for (int i = 0; i < s.length(); i++) {
            if (!Character.isWhitespace(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }
}
