package cloud.apposs.util;

import org.junit.Test;

public class TestStrUtil {
    @Test
    public void testCombine() {
        String message = StrUtil.combineWithUnderline("aa", "bb");
        System.out.println(message);
    }
}
