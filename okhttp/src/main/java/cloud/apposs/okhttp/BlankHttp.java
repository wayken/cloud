package cloud.apposs.okhttp;

import cloud.apposs.react.IoFunction;
import cloud.apposs.react.OnSubscribeHandle;
import cloud.apposs.react.React;
import cloud.apposs.react.SafeIoSubscriber;
import cloud.apposs.util.CachedFileStream;
import cloud.apposs.util.StandardResult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BlankHttp {
    /**
     * 不执行HTTP请求，直接返回{@link StandardResult}结果，
     * 之所以设计此逻辑在于业务有可能判断某些条件下是不需要再执行HTTP请求，
     * 直接跳过{@link React#handle(IoFunction)}处理，由再下一层业务处理
     */
    public static React<OkResponse> execute(StandardResult result) throws Exception {
        return BlankHttp.execute(200, result);
    }

    public static React<OkResponse> execute(int status, StandardResult result) throws Exception {
        return React.create(new React.OnSubscribe<OkResponse>() {
            @Override
            public void call(SafeIoSubscriber<? super OkResponse> subscriber) throws Exception {
                OkResponse response = new RxResponse(status, result);
                subscriber.onNext(response);
            }
        });
    }

    public static React<OkResponse> execute(Object data) throws Exception {
        return BlankHttp.execute(200, StandardResult.success(data));
    }

    public static React<OkResponse> execute(int status, Object data) throws Exception {
        return BlankHttp.execute(status, StandardResult.success(data));
    }

    public static React<List<OkResponse>> executeBatch(StandardResult result) throws Exception {
        return React.create(new React.OnSubscribe<List<OkResponse>>() {
            @Override
            public void call(SafeIoSubscriber<? super List<OkResponse>> safeIoSubscriber) throws Exception {
                List<OkResponse> response = new RxResponseList(result);
                safeIoSubscriber.onNext(response);
            }
        });
    }

    public static React<List<OkResponse>> executeBatch(Object data) throws Exception {
        return BlankHttp.executeBatch(StandardResult.success(data));
    }

    static class RxResponseList extends ArrayList<OkResponse> implements OnSubscribeHandle.ISkip {
        private final StandardResult result;

        public RxResponseList(StandardResult result) throws IOException {
            this.result = result;
        }

        @Override
        public StandardResult result() {
            return result;
        }
    }

    static class RxResponse extends OkResponse implements OnSubscribeHandle.ISkip {
        private final StandardResult result;

        public RxResponse(StandardResult result) throws IOException {
            this(200, result);
        }

        public RxResponse(int status, StandardResult result) throws IOException {
            super(null, status, null, CachedFileStream.wrap(result.toJson().getBytes()));
            this.result = result;
        }

        @Override
        public StandardResult result() {
            return result;
        }
    }
}
