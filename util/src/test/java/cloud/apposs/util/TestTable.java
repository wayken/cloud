package cloud.apposs.util;

import org.junit.Test;

public class TestTable {
    @Test
    public void testTableNull() {
        Table<Object> table = Table.builder();
        table.add(null);
        String str = table.toJson();
        System.out.println(str);
    }
}
