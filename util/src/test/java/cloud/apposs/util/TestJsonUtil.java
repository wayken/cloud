package cloud.apposs.util;

import org.junit.Assert;
import org.junit.Test;

import java.io.InputStream;

public class TestJsonUtil {
    @Test
    public void testStringToTable() {
        String str = "[1, 2, 1001, 2001, \"aa\"]";
        Table<Integer> list = JsonUtil.parseJsonTable(str, Integer.class);
        System.out.println(list);
        Assert.assertTrue(!list.isEmpty());
    }

    @Test
    public void testBigStringJson() {
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("json.txt");
        String str = FileUtil.readString(is);
        Ref<String> messge = new Ref<String>();
        Param json = JsonUtil.parseJsonParam(str, messge);
        json.put("aa", null);
        System.out.println(json);
    }

    @Test
    public void testToJson() {
        MyObject obj = new MyObject();
        obj.setScore(101);
        String json = JsonUtil.toJson(obj);
        System.out.println(json);
    }

    public static class MyObject {
        private int score = 0;

        public int getScore() {
            return score;
        }

        public void setScore(int score) {
            this.score = score;
        }
    }
}
