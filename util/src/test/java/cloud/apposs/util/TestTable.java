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

    @Test
    public void testFormatTableOfParams() {
        Table<Object> table = Table.builder();
        table.add(Param.builder("id", 1).setString("name", "alice"));
        table.add(Param.builder("id", 2).setString("name", "bob"));
        String json = table.toJson(true);
        System.out.println(json);
    }

    @Test
    public void testFormatNestedTable() {
        Table<Object> inner = Table.builder();
        inner.add(1);
        inner.add(2);
        Param param = new Param().setTable("nums", inner).setString("label", "test");
        String json = param.toJson(true);
        System.out.println(json);
    }
}
