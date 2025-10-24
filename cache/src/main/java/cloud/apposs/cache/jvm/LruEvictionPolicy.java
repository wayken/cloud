package cloud.apposs.cache.jvm;

/**
 * 最近最少使用内存回收策略，即很久之前访问过的回收
 */
public class LruEvictionPolicy extends AbstractCacheEvictionPolicy {
	@Override
	public String getName() {
		return CacheEvictionPolicyStrategy.CACHE_POLICY_LRU;
	}
	
	@Override
	public final boolean compare(Element element1, Element element2) {
		return element2.getLastAccessTime() < element1.getLastAccessTime();
	}
}
