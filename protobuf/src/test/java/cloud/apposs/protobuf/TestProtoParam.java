package cloud.apposs.protobuf;

import cloud.apposs.util.Param;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Calendar;

public class TestProtoParam {
    public static class Story {
        /** ID，提供快速查询功能，BIGINT(11) */
        public static final String ID = "id";
        /** 所属企业AID，BIGINT(11) */
        public static final String AID = "aid";
        /** 所属项目ID，BIGINT(11) */
        public static final String PID = "pid";
        /** 所属迭代ID，BIGINT(11) */
        public static final String IID = "iid";
        /** 创建者ID，BIGINT(11) */
        public static final String RID = "rid";
        /** 负责人ID，BIGINT(11) */
        public static final String DID = "did";
        /** 所属父需求ID，BIGINT(11) */
        public static final String SID = "sid";
        /** 所属分类ID，BIGINT(11) */
        public static final String CID = "cid";
        /** 需求标题，VARCHAR(64) */
        public static final String TITLE = "title";
        /** 需求状态，TINYINT(4) */
        public static final String STATUS = "status";
        /** 需求标志位，TINYINT(4)，如删除、激活等 */
        public static final String FLAG = "flag";
        /** 需求优先级，TINYINT(4) */
        public static final String PRIORITY = "priority";
        /** 需求描述，TEXT */
        public static final String CONTENT = "content";
        /** 关注人ID列表，TEXT */
        public static final String FOLLOW = "follow";
        /** 需求余额，DOUBLE(11,2) */
        public static final String BALANCE = "balance";
        /** 创建时间，DATETIME */
        public static final String CREATE_TIME = "create_time";
        /** 开始时间，DATETIME */
        public static final String START_TIME = "start_time";
        /** 结束时间，DATETIME */
        public static final String COMPLETE_TIME = "complete_time";
        public static final String VAR1 = "var1";
        public static final String VAR2 = "var2";
        public static final String VAR3 = "var3";
        public static final String VAR4 = "var4";
    }

    private static final ProtoSchema INFO_STORY_SCHEMA = ProtoSchema.mapSchema();
    static {
        INFO_STORY_SCHEMA.addKey(Story.ID, Long.class);
        INFO_STORY_SCHEMA.addKey(Story.AID, Long.class);
        INFO_STORY_SCHEMA.addKey(Story.PID, Long.class);
        INFO_STORY_SCHEMA.addKey(Story.IID, Long.class);
        INFO_STORY_SCHEMA.addKey(Story.RID, Long.class);
        INFO_STORY_SCHEMA.addKey(Story.DID, Long.class);
        INFO_STORY_SCHEMA.addKey(Story.SID, Long.class);
        INFO_STORY_SCHEMA.addKey(Story.CID, Long.class);
        INFO_STORY_SCHEMA.addKey(Story.TITLE, String.class);
        INFO_STORY_SCHEMA.addKey(Story.STATUS, Integer.class);
        INFO_STORY_SCHEMA.addKey(Story.FLAG, Integer.class);
        INFO_STORY_SCHEMA.addKey(Story.PRIORITY, Integer.class);
        INFO_STORY_SCHEMA.addKey(Story.CONTENT, String.class);
        INFO_STORY_SCHEMA.addKey(Story.FOLLOW, String.class);
        INFO_STORY_SCHEMA.addKey(Story.BALANCE, BigDecimal.class);
        INFO_STORY_SCHEMA.addKey(Story.CREATE_TIME, Calendar.class);
        INFO_STORY_SCHEMA.addKey(Story.START_TIME, Calendar.class);
        INFO_STORY_SCHEMA.addKey(Story.COMPLETE_TIME, Calendar.class);
        INFO_STORY_SCHEMA.addKey(Story.VAR1, Long.class);
        INFO_STORY_SCHEMA.addKey(Story.VAR2, Long.class);
        INFO_STORY_SCHEMA.addKey(Story.VAR3, Long.class);
        INFO_STORY_SCHEMA.addKey(Story.VAR4, Long.class);
    }

    @Test
    public void testProtoParam() {
        Param param = Param.builder().setLong(Story.ID, 1040305324764041216L)
                .setLong(Story.AID, 971414734249267200L)
                .setLong(Story.PID, 1013374753903349760L)
                .setLong(Story.RID, 971414734672891904L)
                .setLong(Story.SID, 0L)
                .setLong(Story.CID, 0L)
                .setString(Story.TITLE, "我的需求")
                .setInt(Story.STATUS, 1)
                .setInt(Story.FLAG, 0)
                .setObject(Story.BALANCE, new BigDecimal(1000.00))
                .setCalendar(Story.CREATE_TIME, Calendar.getInstance())
                .setCalendar(Story.START_TIME, Calendar.getInstance())
                .setCalendar(Story.COMPLETE_TIME, Calendar.getInstance())
                ;
        ProtoBuf buffer = new ProtoBuf(false);
        buffer.putParam(param, INFO_STORY_SCHEMA);
        Param value = buffer.getParam(INFO_STORY_SCHEMA);
        System.out.println(value);
    }
}
