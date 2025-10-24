package cloud.apposs.react;

/**
 * Io数据变换器，由业务方调用实现对数据的转换，有返回值
 */
public interface IoReduce<T1, T2, R> {
	R call(T1 t1, T2 t2) throws Exception;
}
