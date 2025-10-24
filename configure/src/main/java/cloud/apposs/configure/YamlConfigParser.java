package cloud.apposs.configure;

import cloud.apposs.util.*;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

/**
 * 配置解析器，基于 YAML/JSON 配置文件解析
 */
public class YamlConfigParser implements ConfigurationParser {
    /**
     * 将 YAML文件解析为指定对象数据结构
     *
     * @param  model    原始对象
     * @param  filename YAML文件
     */
    @Override
    public final void parse(Object model, String filename) throws Exception {
        parse(model, ResourceUtil.getResource(filename));
    }

    @Override
    public void parse(Object model, InputStream resource) throws Exception {
        Param properties = load(resource);
        Class<?> clazz = model.getClass();
        do {
            Map<String, Method> methods = ReflectUtil.getDeclaredMethodMap(clazz);
            doParseOptional(properties, methods, model, clazz);
            clazz = clazz.getSuperclass();
        } while (clazz != null);
    }

    /**
     * 将 YAML文件解析为 Param 数据结构
     *
     * @param  resource YAML文件
     * @return Param 数据结构
     */
    public static Param load(InputStream resource) throws IOException {
        Param result = Param.builder();
        Yaml yaml = new Yaml();
        result.putAll(getFlattenedMap(yaml.loadAs(resource, Map.class)));
        return result;
    }

    private static Map<String, Object> getFlattenedMap(Map<String, Object> source) {
        Map<String, Object> result = new LinkedHashMap<>();
        doBuildFlattenedMap(result, source, null);
        return result;
    }

    @SuppressWarnings("unchecked")
    private static void doBuildFlattenedMap(Map<String, Object> result, Map<String, Object> source, String path) {
        source.forEach((key, value) -> {
            if (StrUtil.hasText(path)) {
                if (key.startsWith("[")) {
                    key = path + key;
                }
                else {
                    key = path + '.' + key;
                }
            }
            if (value instanceof String) {
                result.put(key, value);
            } else if (value instanceof Map) {
                Map<String, Object> map = (Map<String, Object>) value;
                result.put(key, map);
                doBuildFlattenedMap(result, map, key);
            } else {
                result.put(key, (value != null ? value : ""));
            }
        });
    }

    @SuppressWarnings("unchecked")
    private static void doParseOptional(Map<String, Object> document, Map<String, Method> methods, Object model, Class<?> clazz) throws Exception {
        // 是否有指定要读取的JSON节点，默认从根节点开始解析
        if (document == null || document.size() <= 0) {
            return;
        }
        for (Map.Entry<String, Method> entry : methods.entrySet()) {
            // 读取每个方法对应的 YAML 属性名
            String methodName = entry.getKey();
            Field propertyField = clazz.getDeclaredField(entry.getKey());
            Value methodAnnotation = propertyField.getAnnotation(Value.class);
            String propertyName = methodAnnotation != null ? methodAnnotation.value(): methodName;
            if (document.containsKey(propertyName)) {
                doParsePropertyNode(document, methodName, propertyName, model, clazz, entry.getValue());
                continue;
            }
            // 通过读取注解方式在JSON中的配置递归解析JSON节点
            Field field = clazz.getDeclaredField(methodName);
            Class<?> fieldType = field.getType();
            Value fieldOptional = field.getAnnotation(Value.class);
            Value typeOptional = fieldType.getAnnotation(Value.class);
            // 优先属性上的注解
            String annotationName = null;
            if (fieldOptional != null) {
                annotationName = fieldOptional.value();
            } else if (typeOptional != null) {
                annotationName = typeOptional.value();
            }
            if (StrUtil.isEmpty(annotationName)) {
                continue;
            }
            // 先获取属性上的值，没有则new一个对象，注意属性对象必须提供空构造函数
            field.setAccessible(true);
            Object fieldObject = field.get(model);
            if (fieldObject == null) {
                fieldObject = fieldType.newInstance();
                methods.get(field.getName()).invoke(model, fieldObject);
            }
            Map<String, Object> childDoc = (Map<String, Object>) document.get(annotationName);
            Map<String, Method> modelMethods = ReflectUtil.getDeclaredMethodMap(fieldType);
            doParseOptional(childDoc, modelMethods, fieldObject, fieldObject.getClass());
        }
    }

    @SuppressWarnings("unchecked")
    private static boolean doParsePropertyNode(Map<String, Object> document, String methodName,
                String propertyName, Object model, Class<?> modelClazz, Method method) throws Exception {
        // 解析XML PROPERTY属性值并反射调用到类中
        Class<?>[] methodTypes = method.getParameterTypes();
        // setXXX(Object obj)方法必须要有参数
        if (methodTypes.length != 1) {
            return false;
        }
        if (ReflectUtil.isGenericType(methodTypes[0])) {
            // 对象属性为普通数据类型
            Object nodeVal = document.get(propertyName);
            if (nodeVal != null) {
                method.invoke(model, nodeVal);
            }
        } else if (methodTypes[0].equals(List.class)) {
            // 对象属性为List对象
            Field field = modelClazz.getDeclaredField(methodName);
            field.setAccessible(true);
            // 获取List泛型类型
            ParameterizedType pt = (ParameterizedType)field.getGenericType();
            Class<?> genericClazz = (Class<?>)pt.getActualTypeArguments()[0];
            if (ReflectUtil.isGenericType(genericClazz)) {
                // List值为普通数据类型，直接赋值List
                Object nodeVal = document.get(propertyName);
                if (nodeVal != null) {
                    method.invoke(model, nodeVal);
                }
            } else {
                // List值为自定义对象类型，递归解析对象并添加到List中
                List<Object> fieldList = (List<Object>) field.get(model);
                if (fieldList == null) {
                    fieldList = new LinkedList<Object>();
                    method.invoke(model, fieldList);
                }
                List<Map<String, Object>> childDocList = (List<Map<String, Object>>) document.get(propertyName);
                Map<String, Method> modelMethods = ReflectUtil.getDeclaredMethodMap(genericClazz);
                for (Map<String, Object> childDoc : childDocList) {
                    Object fieldObject = genericClazz.newInstance();
                    doParseOptional(childDoc, modelMethods, fieldObject, fieldObject.getClass());
                    fieldList.add(fieldObject);
                }
            }
        } else if (methodTypes[0].equals(Map.class)) {
            // 对象属性为Map对象
            Field field = modelClazz.getDeclaredField(methodName);
            field.setAccessible(true);
            Map<Object, Object> fieldMap = (Map<Object, Object>) field.get(model);
            if (fieldMap == null) {
                fieldMap = new HashMap<Object, Object>();
                method.invoke(model, fieldMap);
            }
            // 获取Map泛型类型
            Type mapType = field.getGenericType();
            if (!ParameterizedType.class.isAssignableFrom(mapType.getClass()) &&
                    ((ParameterizedType) mapType).getActualTypeArguments().length != 2) {
                return false;
            }
            Type[] mapTypes = ((ParameterizedType) mapType).getActualTypeArguments();
            Class<?> valGenericClazz = (Class<?>)mapTypes[1];
            if (ReflectUtil.isGenericType(valGenericClazz)) {
                // Map值为普通数据类型，继续解析Map其下的键值对
                Map<String, Object> param = (Map<String, Object>) document.get(propertyName);
                for (String key : param.keySet()) {
                    fieldMap.put(key, param.get(key));
                }
            } else {
                // Map值为自定义对象类型，递归解析对象并添加到Map中
                Map<String, Object> param = (Map<String, Object>) document.get(propertyName);
                Map<String, Method> modelMethods = ReflectUtil.getDeclaredMethodMap(valGenericClazz);
                for (String key : param.keySet()) {
                    Object fieldObject = valGenericClazz.newInstance();
                    Map<String, Object> childDoc = (Map<String, Object>) param.get(key);
                    doParseOptional(childDoc, modelMethods, fieldObject, fieldObject.getClass());
                    fieldMap.put(key, fieldObject);
                }
            }
        } else {
            // 对象属性为自定义对象
            // 通过读取方法名在JSON中的配置递归解析JSON节点
            Field field = modelClazz.getDeclaredField(methodName);
            // 先获取属性上的值，没有则new一个对象，注意属性对象必须提供空构造函数
            field.setAccessible(true);
            Object fieldObject = field.get(model);
            // 属性值为空并且没有setXXX方法则不走反射
            if (fieldObject == null) {
                fieldObject = field.getType().newInstance();
                method.invoke(model, fieldObject);
            }
            // 有可能属性类继承新增了方法，需要重新获取
            Class<?> fieldClazz = fieldObject.getClass();
            Map<String, Object> childDoc = (Map<String, Object>) document.get(propertyName);
            Map<String, Method> fieldMethods = ReflectUtil.getDeclaredMethodMap(fieldClazz);
            doParseOptional(childDoc, fieldMethods, fieldObject, fieldClazz);
        }
        return true;
    }
}
