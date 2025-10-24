# Teambeit Cloud Microservices Framework

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Java](https://img.shields.io/badge/Java-8+-green.svg)](https://www.oracle.com/java/)

A comprehensive Java microservices framework built on high-performance reactive architecture, designed for enterprise-level backend middleware development.

## 🚀 Architecture Overview

Teambeit Cloud Framework follows a complete microservices architecture pattern:

> **Browser** → **Nginx (Load Balancer)** → **Gateway (Cache|WAF|Rate Limiting)** → **Web (Tomcat|WebX)** → **Framework Services (Circuit Breaker|Auth|Rate Limiting|Request Aggregation)** → **CacheX (Redis/MySQL)** → **Database**

## 📦 Core Modules

### 🔧 Foundation Components

#### **util** - Utility Library
- **AntPathMatcher**: Ant-style path matching for MVC framework routing
- **Base64**: Base64 encoding/decoding utility
- **ClassUtil**: Java class reflection utilities
- **Encoder**: String escaping and unescaping for various scenarios
- **Encryptor**: Data encryption utility
- **FileUtil/IoUtil**: File and I/O stream operations
- **JsonUtil**: JSON serialization/deserialization
- **NetUtil**: Network and network interface utilities
- **Param/Table**: JSON object and array manipulation tools
- **DataBuffer/DataCollector**: Statistical data collection (95th/90th/85th percentiles)

#### **logger** - High-Performance Logging Framework
- Asynchronous logging for enhanced performance
- Customizable log output formats
- Multiple output targets: files, databases, network

#### **protobuf** - Serialization Framework
- Google Protocol Buffer wrapper for Java
- Support for basic types, objects, Maps, and Lists serialization/deserialization
- Network transmission, JVM memory storage, Redis storage support
- Unified data operation interfaces

#### **configure** - Configuration Management
- Unified configuration serialization component
- YAML/YML configuration file parsing support
- Hot configuration reload via FileWatch/Nacos

### 🌐 Network & Communication

#### **okhttp** - HTTP Client
- **Dual Mode**: Synchronous and asynchronous operations
- **Load Balancing**: Request load balancing (BALANCE)
- **Proxy Support**: Forward proxy capabilities (PROXY)
- **Failover**: Automatic failure recovery (FAIL_OVER)
- **React Integration**: Reactive programming support

#### **balance** - Load Balancer Component
- **Hash-based**: Consistent hashing load balancing
- **Response Time Weighted**: Performance-based routing with response time calculation
- **Least Connections**: Connection count optimization
- **Random/Round Robin**: Various distribution strategies
- **Retry Logic**: Service availability with time-based retries
- **Round Robin**: Round-robin load balancing
- **Channel-based**: Connection node balancing

#### **discovery** - Service Discovery
- Multi-source support: File, QConf, Zookeeper service instance discovery
- Configuration file loading: File, QConf, Zookeeper content loading
- HTTP service discovery with synchronous and asynchronous modes
- MemoryDiscovery support for direct service list registration

#### **registry** - Service Registration
- Service instance registration and management
- Health check and monitoring capabilities
- Integration with service discovery mechanisms

### 🏗️ Application Framework

#### **bootor** - Microservice MVC Framework
- **Netty Integration**: Unified Netty wrapper with flexible I/O model switching
- **MVC Architecture**: Unified MVC framework interface encapsulation
- **Annotation-driven**: Support for Action and various service annotations
- **Plugin Management**: Pluggable management system
- **Interceptor Support**: Custom request interceptor support
- **Parameter Resolution**: Automatic parameter parsing
- **Load Balancing**: Client-side load balancing and failover
- **High Performance**: Single-machine AB testing achieves 10K-12K QPS

#### **webx** - Web MVC Framework
- Traditional Servlet-based web framework
- Framework principles similar to SpringMVC
- Asynchronous request support
- Tomcat integration and optimization

#### **rest** - RESTful Framework
- Unified MVC framework interface encapsulation
- Support for Action and various service annotations
- Plugin management system
- Custom request interceptors
- Request listening and parameter parsing
- React reactive asynchronous output support
- Multiple performance monitoring: QPS, Prometheus, etc.

### 💾 Data & Caching

#### **cachex** - Multi-layered Data Service
- **Unified Interface**: Combined encapsulation of DAO data sources and Cache data
- **Multi-source Support**: Support for multiple data source configurations and different CacheX instances
- **Batch Operations**: Bulk insert and query capabilities
- **Query Builder**: Advanced query construction functionality
- **Cache Strategies**: Intelligent cache management

#### **cache** - Caching Framework
- Memory and distributed caching
- Cache key management and batch operations
- Pattern-based cache invalidation
- Performance monitoring and statistics

#### **dbutil** - Database Utilities
- Basic JDBC encapsulation
- Connection pool management
- Applied to special business scenarios requiring direct database operations

### ⚡ Reactive & Concurrency

#### **react** - Reactive Extensions Module
- **RxJava-inspired**: Reactive programming paradigm
- **Netty-based**: Asynchronous I/O framework services
- **Stream Processing**: Data transformation pipelines
- **Error Handling**: Comprehensive exception management
- **Backpressure**: Flow control mechanisms
- **Parallel Processing**: Concurrent execution support

#### **threadx** - Enhanced Thread Pool Component
- **Extended ThreadPoolExecutor**: All features of java.util.concurrent.ThreadPoolExecutor
- **Event Listening**: Thread task execution event monitoring
- **JMX Monitoring**: Thread pool JMX monitoring
- **Context Propagation**: Context parameter passing support for thread pool execution

### 🛡️ Security & Reliability

#### **guard** - Circuit Breaker & Rate Limiting
- **Flow Control**: Request rate limiting with various strategies
- **Circuit Breaker**: Automatic failure detection and recovery
- **Key-based Limiting**: Fine-grained access control
- **Multiple Strategies**: Reject, WarmUp, and custom behaviors
- **Real-time Monitoring**: Performance and health metrics

#### **ioc** - Dependency Injection Container
- **Lightweight Container**: Minimal Spring IoC container implementation
- **Lifecycle Management**: Automatic resource initialization and cleanup
- **AutoCloseable Support**: Resource disposal for AutoCloseable interface implementations
- **Initializable Interface**: Custom initialization logic support

### 📨 Messaging & Integration

#### **queue** - Message Queue Service
- **Multi-provider**: Support for RocketMQ and Kafka mode switching
- **Unified Interface**: Abstract messaging operations
- **Producer/Consumer**: Complete messaging patterns
- **Configuration-driven**: Simple provider switching

## 🌟 Key Features

### Multi-Environment Support
- **Development** (`dev`): Local development environment
- **Preview** (`preview`): Staging environment
- **Production** (`prod`): Live environment

### Performance Optimizations
- **Async-first Design**: Everything built for asynchronous processing
- **Connection Pooling**: Optimized resource management
- **Batch Operations**: Bulk processing capabilities
- **Caching Strategies**: Multi-level caching support
- **Load Balancing**: Intelligent request distribution

### Monitoring & Observability
- **Health Checks**: Automatic health method generation via @Health annotation for microservice health status
- **Metrics Collection**: Display all component call information via @Process annotation
- **Process Monitoring**: Request tracking and statistics
- **Slow Query Detection**: Database performance monitoring with listener alerts for DB calls exceeding time thresholds

### Developer Experience
- **Annotation-driven**: Minimal configuration approach
- **Plugin Architecture**: Extensible component system
- **Unified APIs**: Consistent interface design
- **Comprehensive Documentation**: Rich examples and guides

## 🚦 Getting Started

### Prerequisites
- Java 8+
- Maven 3.6+
- Redis (optional, for caching)
- Zookeeper / Nacos (optional, for service discovery)

### Quick Start

1. **Clone the repository**
```bash
git clone https://github.com/wayken/cloud.git
cd cloud
```

2. **Build the project**
```bash
mvn clean install
```

3. **Write Action Class**
```java
@RestAction
public class UserAction implements Initializable {
    @Request("/")
    public React<String> root() {
        return React.just("Hello Index Html");
    }
}
```

4. **Create Simple Microservice**
```java
public class DemoApplication {
    public static void main(String[] args) throws Exception {
        HttpApplication.run(DemoApplication.class, args);
    }
}
```

## 📚 Documentation

## 🎯 Design Principles

### Architecture Design
- All monitoring and alerting functions must be processed through Listener registration for interception
- Prohibit writing alert business code into the underlying framework, which is detrimental to framework simplicity and extensibility
- Adopt async-first design philosophy, transforming the framework to be purely asynchronous

### Version Management
- All module versions are RELEASE versions, abolishing SNAPSHOT versions
- For code updates within the same team's parent module, compile everything directly
- No need to depend on Maven compilation, upload, and download

## 🤝 Contributing

We welcome contributions! Please read our contributing guidelines and submit pull requests for any improvements.

## 📄 License

This project is licensed under the Apache License 2.0 - see the LICENSE file for details.

## 🙏 Acknowledgments

- Built on top of Netty for high-performance networking
- Inspired by Spring Framework and RxJava
- Integrates with industry-standard tools and protocols

---

**Cloud Framework** - Building the future of microservices through multi-component integration.
