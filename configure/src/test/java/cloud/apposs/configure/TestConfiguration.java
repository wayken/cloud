package cloud.apposs.configure;

import org.junit.Test;

public class TestConfiguration {
    public static final boolean USE_XML = true;
    public static final boolean USE_YAML = true;
    public static final String BEAN_CFG_XML = "application.xml";
    public static final String BEAN_CFG_YAML = "application.yaml";
    public static final String BEAN_CFG_JSON = "application.json";

    @Test
    public void testConfiguration() throws Exception {
        MyConfigBean bean = new MyConfigBean();
        if (!USE_XML) {
            ConfigurationParser cp = ConfigurationFactory.getConfigurationParser(ConfigurationFactory.YAML);
            if (USE_YAML) {
                cp.parse(bean, BEAN_CFG_YAML);
            } else {
                cp.parse(bean, BEAN_CFG_JSON);
            }
        } else {
            ConfigurationParser cp = ConfigurationFactory.getConfigurationParser(ConfigurationFactory.XML);
            cp.parse(bean, BEAN_CFG_XML);
        }
        System.out.println(bean.getName());
    }
}
