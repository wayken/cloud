package cloud.apposs.webx.sample.action;

import cloud.apposs.ioc.annotation.Autowired;
import cloud.apposs.okhttp.FormEntity;
import cloud.apposs.okhttp.OkRequest;
import cloud.apposs.okhttp.OkHttp;
import cloud.apposs.okhttp.OkResponse;
import cloud.apposs.react.IoSubscriber;
import cloud.apposs.react.React;
import cloud.apposs.rest.annotation.Action;
import cloud.apposs.rest.annotation.Model;
import cloud.apposs.rest.annotation.Request;
import cloud.apposs.rest.validator.checker.Digits;
import cloud.apposs.rest.validator.checker.Digits64;
import cloud.apposs.rest.validator.checker.Id;
import cloud.apposs.util.StandardResult;
import cloud.apposs.webx.WebUtil;
import cloud.apposs.webx.resolver.parameter.ModelParametric;

@Action
public class StoryAction {
    private static final String SERVICE_MISSION = "SERVICE_MISSION";

    private final OkHttp okHttp;

    @Autowired
    public StoryAction(OkHttp okHttp) {
        this.okHttp = okHttp;
    }

    @Request.Read(value = "/story/list")
    public React<StandardResult> list(@Model List request) throws Exception {
        for (int i = 0; i < 40; i++) {
            FormEntity formEntity = WebUtil.buildFormEntity(request)
                    .add("aid", request.getAid())
                    .add("pid", request.getPid())
                    .add("start", request.getStart())
                    .add("limit", request.getLimit());
            OkRequest ioRequest = WebUtil.buildIORequest(SERVICE_MISSION, "/mission/story/list").post(formEntity);
            okHttp.execute(ioRequest).subscribe(new IoSubscriber<OkResponse>() {
                @Override
                public void onNext(OkResponse value) throws Exception {
                }
                @Override
                public void onCompleted() {
                }
                @Override
                public void onError(Throwable cause) {
                    System.out.println("error");
                }
            }).start();
        }
        return React.just(StandardResult.success());
    }

    public static class List extends ModelParametric {
        @Id
        private long aid;

        @Digits64
        private long mid;

        @Digits64
        private long pid;

        @Digits
        private int start;

        @Digits
        private int limit;

        public long getAid() {
            return aid;
        }

        public void setAid(long aid) {
            this.aid = aid;
        }

        public long getMid() {
            return mid;
        }

        public void setMid(long mid) {
            this.mid = mid;
        }

        public long getPid() {
            return pid;
        }

        public void setPid(long pid) {
            this.pid = pid;
        }

        public int getStart() {
            return start;
        }

        public void setStart(int start) {
            this.start = start;
        }

        public int getLimit() {
            return limit;
        }

        public void setLimit(int limit) {
            this.limit = limit;
        }
    }
}
