package cloud.apposs.ioc.sample.bean;

import cloud.apposs.ioc.BeanFactory;
import cloud.apposs.ioc.BeanFactoryPostProcessor;
import cloud.apposs.ioc.BeansException;
import cloud.apposs.ioc.annotation.Component;

@Component
public class MyBeanPostProcessor implements BeanFactoryPostProcessor {
    @Override
    public void postProcessBeanFactory(BeanFactory beanFactory) throws BeansException {
        System.out.println("MyBeanPostProcessor postProcessBeanFactory");
        beanFactory.addBean(new MyBeanPost());
    }

    public static class MyBeanPost {
        public MyBeanPost() {
            System.out.println("MyBeanPost Constructor");
        }
    }
}
