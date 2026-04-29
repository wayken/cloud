package cloud.apposs.util;

import org.junit.Assert;
import org.junit.Test;

public class TestParam {
    @Test
    public void testParseJsonParam() {
        Param param = JsonUtil.parseJsonParam("{'name':'qun', 'id':1}");
        Assert.assertTrue(param.getInt("id") == 1);
    }

    @Test
    public void testToHtmlJson() {
        String str = "{\"id\":1,\"value\":{\"key\":\"mykey1\",\"vals\":[\"<script>alert(123)</script>\",\"htmlview\"]}}";
        Param param = JsonUtil.parseJsonParam(str);
        System.out.println(param.toHtmlJson());
    }

    @Test
    public void testParamToString() {
        Param param = Param.builder("key1", 110).setString("key2", "user110").setString("key3", null);
        System.out.println(param.toJson(false, 0, null, false, true));
    }

    @Test
    public void testFormatNestedParam() {
        Param child = Param.builder("x", 1).setInt("y", 2);
        Param parent = Param.builder("name", "bar").setParam("child", child);
        String json = parent.toJson(true);
        System.out.println(json);
    }
}
