package cloud.apposs.rest.filter;

import java.util.ArrayList;
import java.util.List;

public class FilterChain<R, P> {
    /**
     * 过滤器列表
     */
    private final List<IFilter<R, P>> filterList = new ArrayList<IFilter<R, P>>();

    public void addFilter(IFilter<R, P> filter) {
        filterList.add(filter);
    }

    public void removeFilter(IFilter<R, P> filter) {
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
    public boolean filter(R request, P response) throws Exception {
        for (IFilter<R, P> filter : filterList) {
            boolean success = filter.filter(request, response);
            if (!success) {
                return false;
            }
        }
        return true;
    }
}
