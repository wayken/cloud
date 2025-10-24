package cloud.apposs.configure;

import cloud.apposs.util.ReflectUtil;
import cloud.apposs.util.ResourceUtil;
import cloud.apposs.util.StrUtil;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class XmlConfigParser implements ConfigurationParser {
    public static final String XML_NODE_PROPERTY = "property";
    public static final String XML_NODE_ATTR_NAME = "name";
    public static final String XML_NODE_LIST_VALUE = "value";
    public static final String XML_NODE_MAP_KEY = "key";
    public static final String XML_NODE_MAP_VALUE = "value";

    @Override
    public void parse(Object model, String filename) throws Exception {
        parse(model, ResourceUtil.getResource(filename));
    }

    @Override
    public void parse(Object model, InputStream resource) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = dbf.newDocumentBuilder();
        Document document = builder.parse(resource);
        document.getDocumentElement().normalize();
        Class<?> clazz = model.getClass();
        do {
            Map<String, Method> methods = ReflectUtil.getDeclaredMethodMap(clazz);
            String section = null;
            // 获取类上的注解，通过注解读取配置节点下各子节点值并反射到类对象中
            Value modelValue = clazz.getAnnotation(Value.class);
            if (modelValue != null) {
                section = modelValue.value();
            }
            NodeList nodeList = null;
            if (StrUtil.isEmpty(section)) {
                nodeList = document.getChildNodes();
            } else {
                nodeList = document.getElementsByTagName(section);
            }
            Node node = nodeList.item(0);
            doParseOptional((Element) node, null, methods, model, clazz);
            clazz = clazz.getSuperclass();
        } while(clazz != null);
    }

    private boolean doParseOptional(Element element, String section,
                Map<String, Method> methods, Object model, Class<?> clazz) throws Exception {
        List<Element> childElementList = null;
        // 是否有指定要读取的XML节点，默认从根节点开始解析
        if (!StrUtil.isEmpty(section)) {
            childElementList = getChildrenByTagName(element, section);
            if (childElementList == null || childElementList.isEmpty()) {
                return false;
            }
            element = childElementList.get(0);
        }
        Map<String, Node> propertyNodeList = getPropertyNodeList(element);
        Map<String, Node> unPropertyNodeList = getUnPropertyNodeList(element);
        for (Map.Entry<String, Method> entry : methods.entrySet()) {
            String methodName = entry.getKey();
            Field propertyField = clazz.getDeclaredField(entry.getKey());
            Value methodAnnotation = propertyField.getAnnotation(Value.class);
            String propertyName = methodAnnotation != null ? methodAnnotation.value(): methodName;
            if (propertyNodeList.containsKey(propertyName)) {
                Node childNode = propertyNodeList.get(propertyName);
                if (childNode.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }
                handlePropertyNodeParse(element, childNode, model, clazz, entry.getValue());
            } else if (unPropertyNodeList.containsKey(methodName)) {
                // 通过读取方法名在XML中的配置递归解析XML节点
                Field field = clazz.getDeclaredField(methodName);
                Class<?> fieldType = field.getType();
                // 先获取属性上的值，没有则new一个对象，注意属性对象必须提供空构造函数
                field.setAccessible(true);
                Object fieldObject = field.get(model);
                // 属性值为空并且没有setXXX方法则不走反射
                if (fieldObject == null && !methods.containsKey(field.getName())) {
                    continue;
                }
                if (fieldObject == null) {
                    fieldObject = fieldType.newInstance();
                    methods.get(field.getName()).invoke(model, fieldObject);
                }
                // 有可能属性类继承新增了方法，需要重新获取
                Class<?> fieldClazz = fieldObject.getClass();
                Map<String, Method> fieldMethods = ReflectUtil.getDeclaredMethodMap(fieldClazz);
                doParseOptional(element, methodName, fieldMethods, fieldObject, fieldClazz);
            } else if (ReflectUtil.isFieldExist(clazz, methodName)) {
                // 通过读取注解方式在XML中的配置递归解析XML节点
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
                if (!StrUtil.isEmpty(annotationName)) {
                    Map<String, Method> fieldObjectMethods = ReflectUtil.getDeclaredMethodMap(fieldType);
                    // 先获取属性上的值，没有则new一个对象，注意属性对象必须提供空构造函数
                    field.setAccessible(true);
                    Object fieldObject = field.get(model);
                    // 属性值为空并且没有setXXX方法则不走反射
                    if (fieldObject == null && !methods.containsKey(field.getName())) {
                        continue;
                    }
                    if (fieldObject == null) {
                        fieldObject = field.getType().newInstance();
                        methods.get(field.getName()).invoke(model, fieldObject);
                    }
                    doParseOptional(element, annotationName, fieldObjectMethods, fieldObject, fieldObject.getClass());
                }
            }
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    private boolean handlePropertyNodeParse(Element element, Node node,
                Object model, Class<?> modelClazz, Method method) throws Exception {
        Element piece = (Element) node;
        NamedNodeMap attributes = piece.getAttributes();
        if (attributes == null || attributes.getLength() <= 0) {
            return false;
        }
        String propName = attributes.getNamedItem(XML_NODE_ATTR_NAME).getNodeValue();
        String propVal = piece.getTextContent();
        if (propVal == null) {
            return false;
        }
        // 解析XML PROPERTY属性值并反射调用到类中
        Class<?>[] methodTypes = method.getParameterTypes();
        // setXXX(Object obj)方法必须要有参数
        if (methodTypes.length != 1) {
            return false;
        }
        if (methodTypes[0].equals(List.class)) {
            Field field = modelClazz.getDeclaredField(propName);
            field.setAccessible(true);
            List<Object> fieldList = (List<Object>) field.get(model);
            boolean nullable = false;
            if (fieldList == null) {
                nullable = true;
                fieldList = new ArrayList<Object>();
            }
            // 获取List泛型类型
            ParameterizedType pt = (ParameterizedType)field.getGenericType();
            Class<?> genericClazz = (Class<?>)pt.getActualTypeArguments()[0];
            if (ReflectUtil.isGenericType(genericClazz)) {
                // List值为普通数据类型，直接赋值List
                List<Element> childElementList = getChildrenByTagName(piece, XML_NODE_LIST_VALUE);
                for (int m = 0; m < childElementList.size(); m++) {
                    Element childElement = childElementList.get(m);
                    String elementMText = childElement.getTextContent();
                    Object elementMVal = ReflectUtil.parseObject(elementMText, genericClazz);
                    if (elementMVal != null) {
                        fieldList.add(elementMVal);
                    }
                }
            } else {
                // List值为自定义对象类型，递归解析对象并添加到List中
                List<Element> childElementList = getChildrenByTagName(piece, XML_NODE_LIST_VALUE);
                Map<String, Method> ptObjectMethods = ReflectUtil.getDeclaredMethodMap(genericClazz);
                for (int m = 0; m < childElementList.size(); m++) {
                    Element childElement = childElementList.get(m);
                    // 先获取属性上的值，没有则new一个对象，注意属性对象必须提供空构造函数
                    Object ptObject = genericClazz.newInstance();
                    doParseOptional(childElement, null, ptObjectMethods, ptObject, ptObject.getClass());
                    fieldList.add(ptObject);
                }
            }
            // 如果配置类字段为空，则在列表初始化并添加完所有元素之后再set进去
            if (nullable) {
                method.invoke(model, fieldList);
            }
        } else if (methodTypes[0].equals(Map.class)) {
            Field field = modelClazz.getDeclaredField(propName);
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
            Class<?> keyGenericClazz = (Class<?>)mapTypes[0];
            Class<?> valGenericClazz = (Class<?>)mapTypes[1];
            if (ReflectUtil.isGenericType(valGenericClazz)) {
                // Map值为普通数据类型，继续解析Map其下的键值对
                List<Element> childElementList = getChildrenByTagName(piece, XML_NODE_LIST_VALUE);
                for (int m = 0; m < childElementList.size(); m++) {
                    Element childElement = childElementList.get(m);
                    NamedNodeMap attributes2 = childElement.getAttributes();
                    String mapValText = childElement.getTextContent();
                    if (attributes2 != null && attributes2.getLength() > 0) {
                        Node keyNode = attributes2.getNamedItem(XML_NODE_MAP_KEY);
                        if (keyNode == null) {
                            continue;
                        }
                        String mapKey = keyNode.getNodeValue();
                        Object mapKeyVal = ReflectUtil.parseObject(mapKey, keyGenericClazz);
                        Object mapValVal = ReflectUtil.parseObject(mapValText, valGenericClazz);
                        fieldMap.put(mapKeyVal, mapValVal);
                    }
                }
            } else {
                // Map值为自定义对象类型，递归解析对象并添加到Map中
                Map<String, Method> ptObjectMethods = ReflectUtil.getDeclaredMethodMap(valGenericClazz);
                List<Element> childElementList = getChildrenByTagName(piece, XML_NODE_MAP_VALUE);
                for (int m = 0; m < childElementList.size(); m++) {
                    Element childElement = childElementList.get(m);
                    NamedNodeMap attributes2 = childElement.getAttributes();
                    Object mapValObject = valGenericClazz.newInstance();
                    if (attributes2 != null && attributes2.getLength() > 0) {
                        Node keyNode = attributes2.getNamedItem(XML_NODE_MAP_KEY);
                        if (keyNode == null) {
                            continue;
                        }
                        String mapKey = keyNode.getNodeValue();
                        doParseOptional(childElement, null, ptObjectMethods, mapValObject, mapValObject.getClass());
                        Object mapKeyVal = ReflectUtil.parseObject(mapKey, keyGenericClazz);
                        fieldMap.put(mapKeyVal, mapValObject);
                    }
                }
            }
        } else {
            Object nodeVal = ReflectUtil.parseObject(propVal, methodTypes[0]);
            if (nodeVal != null) {
                method.invoke(model, nodeVal);
            }
        }
        return true;
    }

    /**
     * 获取顶层Element其下的子元素，不递归查找
     */
    private static List<Element> getChildrenByTagName(Element parent, String name) {
        List<Element> nodeList = new ArrayList<Element>();
        for (Node child = parent.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child.getNodeType() == Node.ELEMENT_NODE &&
                    name.equals(child.getNodeName())) {
                nodeList.add((Element) child);
            }
        }
        return nodeList;
    }

    /**
     * 获取节点下同级所有property子节点
     */
    private Map<String, Node> getPropertyNodeList(Element element) {
        Map<String, Node> nodeList = new ConcurrentHashMap<String, Node>();
        NodeList childNodeList = element.getElementsByTagName(XML_NODE_PROPERTY);
        for (int i = 0; i < childNodeList.getLength(); i++) {
            Node node = childNodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE && node.getParentNode() == element) {
                Element piece = (Element) node;
                NamedNodeMap attributes = piece.getAttributes();
                String attrName = attributes.getNamedItem(XML_NODE_ATTR_NAME).getNodeValue();
                nodeList.put(attrName, node);
            }
        }
        return nodeList;
    }

    /**
     * 获取节点下同级所有非property子节点，用于递归解析
     */
    private Map<String, Node> getUnPropertyNodeList(Element element) {
        Map<String, Node> nodeList = new ConcurrentHashMap<String, Node>();
        NodeList childNodeList = element.getChildNodes();
        for (int i = 0; i < childNodeList.getLength(); i++) {
            Node node = childNodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE && !node.getNodeName().equals(XML_NODE_PROPERTY)) {
                nodeList.put(node.getNodeName(), node);
            }
        }
        return nodeList;
    }
}
