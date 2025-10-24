package cloud.apposs.ioc;

/**
 * IOC容器中实现此接口的类在BeanFactory初始化时会自动调用
 */
public interface BeanFactoryPostProcessor {
    /**
     * BeanFactory初始化时的后置处理调用，可以在此方法中对BeanFactory进行处理，
     * 例如注册BeanDefinition，修改BeanDefinition等，添加Bean对象等
     */
    void postProcessBeanFactory(BeanFactory beanFactory) throws BeansException;
}
