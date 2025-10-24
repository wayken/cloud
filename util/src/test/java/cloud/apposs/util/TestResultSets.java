package cloud.apposs.util;

import org.junit.Test;

public class TestResultSets {
    @Test
    public void testAssign() {
        Table<Param> dataList = Table.builder();
        dataList.add(Param.builder("id", 0).setString("name", "data0"));
        dataList.add(Param.builder("id", 1).setString("name", "data1"));
        dataList.add(Param.builder("id", 2).setString("name", "data2"));
        dataList.add(Param.builder("id", 3).setString("name", "data3"));
        dataList.add(Param.builder("id", 4).setString("name", "data4"));
        Table<Integer> idList = ResultSets.assign(dataList, "id");
        System.out.println(idList);
    }

    @Test
    public void testFilter() {
        Table<Param> dataList = Table.builder();
        dataList.add(Param.builder("id", 0).setString("name", "data0"));
        dataList.add(Param.builder("id", 1).setString("name", "data1"));
        dataList.add(Param.builder("id", 2).setString("name", "data2"));
        dataList.add(Param.builder("id", 3).setString("name", "data3"));
        dataList.add(Param.builder("id", 4).setString("name", "data4"));
        Param info = ResultSets.filter(dataList, "id", 2);
        System.out.println(info);
    }
}
