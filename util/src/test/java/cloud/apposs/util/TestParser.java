package cloud.apposs.util;

import org.junit.Assert;
import org.junit.Test;

public class TestParser {
    @Test
    public void testParseLong() {
        int value = Parser.parseInt("971414734249267200");
        Assert.assertTrue(value < 0);
    }
}
