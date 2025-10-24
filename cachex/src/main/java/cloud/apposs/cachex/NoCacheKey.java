package cloud.apposs.cachex;

import java.util.ArrayList;
import java.util.List;

/**
 * 空缓存KEY，指明指定数据操作不再缓存，主要用于复杂条件查询时传递此KEY用于指明不缓存
 */
public class NoCacheKey extends AbstractCacheKey<Object> {
    public static NoCacheKey getInstance() {
        return new NoCacheKey();
    }

    public static List<NoCacheKey> getInstanceList(int size) {
        List<NoCacheKey> instanceList = new ArrayList<NoCacheKey>(1);
        for (int i = 0; i < size; i++) {
            instanceList.add(new NoCacheKey());
        }
        return instanceList;
    }

    @Override
    public String getCacheKey() {
        return null;
    }
}
