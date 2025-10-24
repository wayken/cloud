package cloud.apposs.bootor.filter;

import cloud.apposs.bootor.BootorHttpRequest;
import cloud.apposs.bootor.BootorHttpResponse;

import java.util.ArrayList;
import java.util.List;

public class FilterChain {
    /**
     * 过滤器列表
     */
    private final List<IFilter> filterList = new ArrayList<IFilter>();

    public void addFilter(IFilter filter) {
        filterList.add(filter);
    }

    public void removeFilter(IFilter filter) {
        filterList.remove(filter);
    }

    /**
     * 过滤器处理方法
     *
     * @param request  请求对象
     * @param response 响应对象
     * @return 返回true表示继续执行后续的过滤器，返回false表示停止执行后续的过滤器
     * @throws Exception
     */
    public boolean filter(BootorHttpRequest request, BootorHttpResponse response) throws Exception {
        for (IFilter filter : filterList) {
            boolean success = filter.filter(request, response);
            if (!success) {
                return false;
            }
        }
        return true;
    }
}
