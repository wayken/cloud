package cloud.apposs.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class StrUtil {
    private static final char PACKAGE_SEPARATOR_CHAR = '.';

    public static boolean isEmpty(Object str) {
        return str == null || StrUtil.isEmpty(str.toString());
    }

    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    public static boolean isEmail(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }
        Pattern pattern = Pattern.compile(
                "^[a-zA-Z0-9][a-zA-Z0-9_=\\&\\-\\.\\+]*[a-zA-Z0-9]*@[a-zA-Z0-9][a-zA-Z0-9_=\\-\\.]+[a-zA-Z0-9]$");
        Matcher matcher = pattern.matcher(email);
        if (!matcher.matches()) {
            return false;
        }
        return true;
    }

    public static boolean isMobile(String mobile) {
        if (mobile == null || mobile.isEmpty()) {
            return false;
        }
        Pattern pattern = Pattern.compile("^1\\d{10}$");
        Matcher matcher = pattern.matcher(mobile);
        if (!matcher.matches()) {
            return false;
        }
        return true;
    }

    /**
     * 将字符串按指定分隔符拆分成字符数组
     *
     * @param string     字符串
     * @param delimiters 分隔字符
     * @param trimTokens 是否截取掉空格字符
     */
    public static String[] toStringArray(String string, String delimiters, boolean trimTokens) {
        if (string == null) {
            return null;
        }

        StringTokenizer tokenizer = new StringTokenizer(string, delimiters);
        List<String> tokens = new ArrayList<String>();
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            if (trimTokens) {
                token = token.trim();
            }
            if (!token.isEmpty()) {
                tokens.add(token);
            }
        }
        return toStringArray(tokens);
    }

    public static String[] toStringArray(Collection<String> collection) {
        if (collection == null) {
            return null;
        }

        return collection.toArray(new String[collection.size()]);
    }

    public static String joinArrayString(List<String> strings, String joinCharactor) {
        String[] sequences = new String[strings.size()];
        strings.toArray(sequences);
        return joinArrayString(sequences, joinCharactor, 0, strings.size());
    }

    public static String joinArrayString(List<String> strings, String joinCharactor, int startIndex) {
        String[] sequences = new String[strings.size()];
        strings.toArray(sequences);
        return joinArrayString(sequences, joinCharactor, startIndex, strings.size());
    }

    public static String joinArrayString(String[] strings, String joinCharactor, int startIndex) {
        return joinArrayString(strings, joinCharactor, startIndex, strings.length);
    }

    public static String joinArrayString(String[] strings, String joinCharactor) {
        return joinArrayString(strings, joinCharactor, 0, strings.length);
    }

    /**
     * 将字符串数组拉拼接成字符串
     *
     * @param strings 原始字符串数组
     * @param joinCharactor 字符串用什么分隔符
     * @param startIndex 字符串数组开始拼接位置
     * @param endIndex 字符串数组结束拼接位置
     * @return 拼接后的字符串
     */
	public static String joinArrayString(String[] strings, String joinCharactor, int startIndex, int endIndex) {
        if (startIndex < 0 || startIndex > strings.length ||
                endIndex < 0 || endIndex > strings.length || startIndex > endIndex) {
            throw new IndexOutOfBoundsException();
        }
        StringBuilder build = new StringBuilder(32);
        for (int i = startIndex; i < endIndex; i++) {
            String string = strings[i];
            build.append(string.trim());
            if (i < endIndex - 1) {
                build.append(joinCharactor);
            }
        }
        return build.toString();
    }

    /**
     * 字符串替换
     *
     * @param inString   原始字符串
     * @param oldPattern 被替换的正则
     * @param newPattern 要替换的正则
     */
    public static String replace(String inString, String oldPattern, String newPattern) {
        if (isEmpty(inString) || isEmpty(oldPattern) || newPattern == null) {
            return inString;
        }
        int index = inString.indexOf(oldPattern);
        if (index == -1) {
            return inString;
        }

        int capacity = inString.length();
        if (newPattern.length() > oldPattern.length()) {
            capacity += 16;
        }
        StringBuilder sb = new StringBuilder(capacity);

        int pos = 0;
        int patLen = oldPattern.length();
        while (index >= 0) {
            sb.append(inString, pos, index);
            sb.append(newPattern);
            pos = index + patLen;
            index = inString.indexOf(oldPattern, pos);
        }

        sb.append(inString.substring(pos));
        return sb.toString();
    }

    public static String lowerFirst(String name) {
        return name.substring(0, 1).toLowerCase() + name.substring(1);
    }

    public static String upperFirst(String name) {
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    /**
     * 对字符串进行首字母大写，例：app-name->AppName
     */
    public static String upperCamelCase(String string) {
        char letter;
        int length = string.length();
        boolean needUpper = true;
        StringBuilder builder = new StringBuilder(length);
        for (int index = 0; index < length; index++) {
            letter = string.charAt(index);
            if (needUpper) {
                needUpper = false;
                letter = Character.toUpperCase(letter);
            }
            if (letter == '-' || letter == '_') {
                needUpper = true;
                continue;
            }
            builder.append(letter);
        }
        return builder.toString();
    }

    public static String combineWithUnderline(String... values) {
        return StrUtil.combine('_', values);
    }

    public static String combine(char seprator, String... values) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            String value = values[i];
            builder.append(value).append(seprator);
            if (i == values.length - 1) {
                builder.setLength(builder.length() - 1);
            }
        }
        return builder.toString();
    }

    /**
     * 字符串反转
     */
    public static String reverse(String str) {
        return str == null ? null : (new StringBuilder(str)).reverse().toString();
    }

    /**
     * 判断字符串是否为数字
     * <pre>
     * StringUtils.isNumeric(null)   = false
     * StringUtils.isNumeric("")     = false
     * StringUtils.isNumeric("  ")   = false
     * StringUtils.isNumeric("123")  = true
     * StringUtils.isNumeric("\u0967\u0968\u0969")  = true
     * StringUtils.isNumeric("12 3") = false
     * StringUtils.isNumeric("ab2c") = false
     * StringUtils.isNumeric("12-3") = false
     * StringUtils.isNumeric("12.3") = false
     * StringUtils.isNumeric("-123") = false
     * StringUtils.isNumeric("+123") = false
     * </pre>
     */
    public static boolean isNumeric(final CharSequence cs) {
        if (isEmpty(cs)) {
            return false;
        }
        final int sz = cs.length();
        for (int i = 0; i < sz; i++) {
            if (!Character.isDigit(cs.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * 判断两个字符串是否相等
     * <pre>
     * StrUtil.equals(null, null)   = true
     * StrUtil.equals(null, "abc")  = false
     * StrUtil.equals("abc", null)  = false
     * StrUtil.equals("abc", "abc") = true
     * StrUtil.equals("abc", "ABC") = false
     * </pre>
     */
    public static boolean equals(final CharSequence cs1, final CharSequence cs2) {
        if (cs1 == cs2) {
            return true;
        }
        if (cs1 == null || cs2 == null) {
            return false;
        }
        if (cs1.length() != cs2.length()) {
            return false;
        }
        if (cs1 instanceof String && cs2 instanceof String) {
            return cs1.equals(cs2);
        }
        final int length = cs1.length();
        for (int i = 0; i < length; i++) {
            if (cs1.charAt(i) != cs2.charAt(i)) {
                return false;
            }
        }
        return true;
    }

    public static boolean hasText(String str) {
        return str != null && !str.isEmpty() && containsText(str);
    }

    private static boolean containsText(CharSequence str) {
        int strLen = str.length();
        for(int i = 0; i < strLen; ++i) {
            if (!Character.isWhitespace(str.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    public static String simpleClassName(Object o) {
        if (o == null) {
            return "null_object";
        } else {
            return simpleClassName(o.getClass());
        }
    }

    public static String simpleClassName(Class<?> clazz) {
        String className = SysUtil.checkNotNull(clazz, "clazz").getName();
        final int lastDotIdx = className.lastIndexOf(PACKAGE_SEPARATOR_CHAR);
        if (lastDotIdx > -1) {
            return className.substring(lastDotIdx + 1);
        }
        return className;
    }

    public static String formatTimeOutput(long time) {
        if (time > 24 * 60 * 60 * 1000) {
            return (time / (24 * 60 * 60 * 1000)) + " Days";
        } else if (time > 60 * 60 * 1000) {
            return (time / (60 * 60 * 1000)) + " Hours";
        } else if (time > 60 * 1000) {
            return (time / (60 * 1000)) + " Minutes";
        }
        return (time / 1000) + " Seconds";
    }

    public static String formatByteOutput(long bytes) {
        String[] units = {"B", "KB", "MB", "GB", "TB", "PB"};
        long v = bytes;
        for (int i = 0; i < units.length; i ++) {
            if (v < 1024L) {
                return String.format("%d%s", v, units[i]);
            }
            v >>= 10L;
        }
        return String.format("%d%s", bytes, units[0]);
    }
}
