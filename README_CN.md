# Teambeit Cloud 微服务框架

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Java](https://img.shields.io/badge/Java-8+-green.svg)](https://www.oracle.com/java/)

一个基于高性能反应式架构构建的综合性Java微服务框架，专为企业级后台中间件开发而设计。

## 🚀 架构概览

Teambeit Cloud框架遵循完整的微服务架构模式：

> **浏览器** → **Nginx（负载均衡）** → **网关（缓存|WAF|限流）** → **Web（Tomcat|WebX）** → **框架服务（熔断|鉴权|限流|请求聚合）** → **CacheX（Redis/MySQL）** → **数据库**

## 📦 核心模块

### 🔧 基础组件

#### **util** - 公共工具类
- **AntPathMatcher**: 遵循Ant路径匹配规则，服务于MVC框架的路径匹配  
- **Base64**: Base64编解码工具类
- **ClassUtil**: Java类反射工具
- **Encoder**: 各场景下的字符串转义、解义操作
- **Encryptor**: 数据加密工具类
- **FileUtil/IoUtil**: 文件和I/O流操作工具
- **JsonUtil**: JSON序列化/反序列化
- **NetUtil**: 网络和网卡工具类
- **Param/Table**: JSON对象和数组操作工具类
- **DataBuffer/DataCollector**: 数据响应95/90/85等多线统计

#### **logger** - 高性能日志框架
- 采用异步方式存储输出日志提升性能
- 支持自定义日志输出格式
- 支持多种输出媒介：文件、数据库、网络等

#### **protobuf** - 序列化框架
- Google Protocol Buffer协议封装类
- 支持Java基本类型、对象、Map、List数据序列化&反序列化
- 支持网络传输、JVM内存存储、Redis存储等
- 提供统一的数据操作接口

#### **configure** - 配置管理
- 统一配置序列化组件
- 支持YAML/YML配置文件解析
- 支持基于FileWatch/Nacos的配置热更新

### 🌐 网络通信

#### **okhttp** - HTTP客户端
- **双模式**: 同步、异步调用两种模式
- **负载均衡**: 请求负载均衡（BALANCE）
- **代理转发**: 代理转发（PROXY）
- **故障转移**: 故障转移（FAIL_OVER）
- **React集成**: 响应式编程支持

#### **balance** - 负载均衡组件
- **基于哈希**: 一致性哈希的负载均衡
- **响应时间加权**: 基于请求响应时间加权计算的负载均衡
- **最少连接数**: 基于最少连接数负载均衡
- **随机轮询**: 随机轮询负载均衡策略
- **重试逻辑**: 在指定时间内重试可用服务的负载均衡
- **基于轮询**: 基于轮询的负载均衡
- **通道连接**: 基于通道连接节点的负载均衡

#### **discovery** - 服务发现
- 支持文件、QConf、Zookeeper的服务实例发现
- 支持文件、QConf、Zookeeper配置文件内容加载
- 支持HTTP请求的服务发现，包括同步、异步方式
- 支持MemoryDiscovery，将服务列表直接添加到IDiscovery中

#### **registry** - 服务注册
- 服务实例注册和管理
- 健康检查和监控功能
- 与服务发现机制集成

### 🏗️ 应用框架

#### **bootor** - 微服务MVC框架
- **Netty集成**: 对Netty统一封装，可自由切换底层IO模型
- **MVC架构**: 对MVC框架接口的统一模块封装
- **注解驱动**: 支持Action等各种服务注解
- **插件化管理**: 插件化管理
- **拦截器支持**: 支持请求拦截器自定义
- **参数解析**: 支持参数解析
- **负载均衡**: 支持客户端负载均衡和故障转移
- **高性能**: 单机AB压测能够达到10K/QPS - 12K/QPS

#### **webx** - Web MVC框架
- 传统基于Servlet的Web框架
- 框架原理类似SpringMVC
- 支持异步请求
- Tomcat集成和优化

#### **rest** - RESTful框架
- 对MVC框架接口的统一模块封装
- 支持Action等各种服务注解
- 插件化管理
- 支持请求拦截器自定义
- 支持请求监听和参数解析
- 支持React响应式异步输出
- 支持QPS、Prometheus等多种性能监控

### 💾 数据与缓存

#### **cachex** - 多层数据服务
- **统一接口**: 将Dao数据源和Cache缓存数据进行结合统一封装
- **多数据源**: 支持配置多数据源，维护多种不同的源CacheX实例
- **批量操作**: 支持批量插入和查询功能
- **查询构建器**: 高级查询构造功能
- **缓存策略**: 智能缓存管理

#### **cache** - 缓存框架
- 内存和分布式缓存
- 缓存key管理和批量操作
- 基于模式的缓存失效
- 性能监控和统计

#### **dbutil** - 数据库工具类
- 提供对JDBC的基础封装
- 连接池管理
- 应用于特殊业务需要直接操作数据库的场景

### ⚡ 反应式与并发

#### **react** - 反应式扩展模块
- **RxJava启发**: 反应式编程范式
- **基于Netty**: 异步I/O框架服务
- **流处理**: 数据转换管道
- **错误处理**: 综合异常管理
- **背压处理**: 流量控制机制
- **并行处理**: 并发执行支持

#### **threadx** - 增强线程池组件
- **扩展ThreadPoolExecutor**: java.util.concurrent.ThreadPoolExecutor原线程池所有特性
- **事件监听**: 线程任务执行的事件监听
- **JMX监控**: 线程池JMX监控
- **上下文传递**: 支持线程池执行的上下文参数传递

### 🛡️ 安全与可靠性

#### **guard** - 熔断器与限流
- **流量控制**: 请求速率限制和各种策略
- **熔断器**: 自动故障检测和恢复
- **基于Key限制**: 细粒度访问控制
- **多种策略**: 拒绝、预热和自定义行为
- **实时监控**: 性能和健康指标

#### **ioc** - 依赖注入容器
- **轻量级容器**: 最简化的Spring IOC容器实现
- **生命周期管理**: 自动资源初始化和清理
- **AutoCloseable支持**: 支持实现AutoClosable接口时的对象资源释放
- **Initializable接口**: 支持初始化接口的自定义逻辑

### 📨 消息与集成

#### **queue** - 消息队列服务
- **多提供商**: 支持RocketMQ和Kafka两种模式切换
- **统一接口**: 抽象消息操作
- **生产者/消费者**: 完整的消息传递模式
- **配置驱动**: 简单的提供商切换

## 🌟 核心特性

### 多环境支持
- **开发环境** (`dev`): 本地开发环境
- **预览环境** (`preview`): 预览环境
- **线上环境** (`prod`): 生产环境

### 性能优化
- **异步优先设计**: 一切皆异步的框架设计
- **连接池**: 优化资源管理
- **批量操作**: 批量处理能力
- **缓存策略**: 多级缓存支持
- **负载均衡**: 智能请求分发

### 监控与可观测性
- **健康检查**: 通过注解@Health，自动添加health方法生成微服务健康状态
- **指标收集**: 通过注解@Process，展现当前组件所有调用信息
- **进程监控**: 请求跟踪和统计
- **慢查询检测**: 数据库性能监控，DB调用超过一定时间触发监听报警

### 开发体验
- **注解驱动**: 最小配置方法
- **插件架构**: 可扩展组件系统
- **统一API**: 一致的接口设计
- **完整文档**: 丰富的示例和指南

## 🚦 快速开始

### 前置条件
- Java 8+
- Maven 3.6+
- Redis（可选，用于缓存）
- Zookeeper / Nacos（可选，用于服务发现）

### 快速启动

1. **克隆仓库**
```bash
git clone https://github.com/wayken/cloud.git
cd cloud
```

2. **构建项目**
```bash
mvn clean install
```

3. **编写Action类**
```java
@RestAction
public class UserAction implements Initializable {
    @Request("/")
    public React<String> root() {
        return React.just("Hello Index Html");
    }
}
```

4. **创建简单微服务**
```java
public class DemoApplication {
    public static void main(String[] args) throws Exception {
        HttpApplication.run(DemoApplication.class, args);
    }
}
```

## 📚 文档

## 🎯 设计原则

### 架构设计
- 所有涉及监控、告警的功能均需要通过Listener注册监听来拦截处理
- 禁用将告警业务代码写入到底层框架中，不利于框架简洁和扩展
- 采用异步优先的设计思想，框架改造为纯异步

### 版本管理
- 所有模块版本均为RELEASE版本，废除SNAPSHOT版本
- 同一个团队同一个父模块下有代码更新直接全部编译即可
- 不用依赖maven编译上传和下载

## 🤝 贡献

我们欢迎贡献！请阅读我们的贡献指南并为任何改进提交pull request。

## 📄 许可证

本项目基于Apache License 2.0许可证 - 查看LICENSE文件了解更多详情。

## 🙏 致谢

- 基于Netty构建的高性能网络框架
- 受Spring Framework和RxJava启发
- 集成行业标准工具和协议

---

**Cloud Framework** - 多组件结合构建微服务的未来。
