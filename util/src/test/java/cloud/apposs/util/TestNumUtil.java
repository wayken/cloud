package cloud.apposs.util;

import org.junit.Assert;
import org.junit.Test;

public class TestNumUtil {
    @Test
    public void testNumLong() {
        long value = Long.MAX_VALUE;
        byte[] bytes = NumUtil.longToBytes(value);
        Assert.assertTrue(value == NumUtil.bytesToLong(bytes));
    }

}
