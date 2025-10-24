package cloud.apposs.guard.slot.limitkey.rule;

import cloud.apposs.util.StrUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 限制关键字限流规则管理器
 */
public class LimitKeyRuleManager {
    private static Map<String, List<LimitKeyRule>> resourceRulesMap = new ConcurrentHashMap<String, List<LimitKeyRule>>();

    /**
     * 加载规则，不支持并发加载和热加载，支持同一个资源下配置多个规则，如限流规则、异常规则等
     */
    public static void loadRule(LimitKeyRule rule) {
        String resource = rule.getResource();
        if (StrUtil.isEmpty(resource)) {
            throw new NullPointerException("resource is null");
        }
        List<LimitKeyRule> rules = resourceRulesMap.get(resource);
        if (rules == null) {
            resourceRulesMap.put(resource, rules = new ArrayList<LimitKeyRule>());
        }
        rules.add(rule);
    }

    /**
     * 替换规则，主要用于热加载规则，支持同一个资源下配置多个规则，如限流规则、异常规则等
     */
    public static void replaceRule(LimitKeyRule rule) {
        String resource = rule.getResource();
        if (StrUtil.isEmpty(resource)) {
            throw new NullPointerException("resource is null");
        }
        List<LimitKeyRule> rules = resourceRulesMap.get(resource);
        if (rules == null) {
            resourceRulesMap.put(resource, rules = new ArrayList<LimitKeyRule>());
        }
        rules.clear();
        rules.add(rule);
    }

    /**
     * 根据资源获取规则
     */
    public static List<LimitKeyRule> getRules(String resource) {
        return resourceRulesMap.get(resource);
    }
}
