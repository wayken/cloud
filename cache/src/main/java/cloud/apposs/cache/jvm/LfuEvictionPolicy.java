package cloud.apposs.cache.jvm;

/**
 * 最近最不经常使用内存回收策略，即命中很低的回收
 */
public class LfuEvictionPolicy extends AbstractCacheEvictionPolicy {
	@Override
	public String getName() {
		return CacheEvictionPolicyStrategy.CACHE_POLICY_LFU;
	}
	
	@Override
	public final boolean compare(Element element1, Element element2) {
		return element2.getHitCount() < element1.getHitCount();
	}
}
