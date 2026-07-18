# 用户管理与数据库监控平台

基于 Spring Boot、MySQL、JdbcTemplate 和 Alibaba Druid 构建的 RESTful 用户管理项目。项目采用分层架构，提供用户 CRUD、分页搜索、参数校验、事务管理、统一异常响应、数据库连接池监控、慢 SQL 分析和开发/生产环境隔离。

## 技术栈

- Java 17、Spring Boot 2.7.18
- Spring Web、Spring JDBC、Bean Validation
- MySQL 8、Alibaba Druid 1.2.28
- Maven、Docker Compose、JUnit 5

## 项目亮点

- 使用 Controller、Service、Repository 分层，降低业务逻辑与 SQL 的耦合
- 使用 JdbcTemplate 和预编译参数执行 SQL，配合 Druid WallFilter 防御 SQL 注入
- 支持用户分页、关键词检索、邮箱唯一性校验和状态管理
- 使用事务保证写操作一致性，统一处理 400、404、409 等异常响应
- 使用 Druid 监控 SQL 执行次数、耗时和慢 SQL，并按环境控制日志与管理后台
- 提供自动建表脚本、Docker MySQL 和集成测试，降低本地运行成本

## 使用 IntelliJ IDEA 运行

1. 打开 IDEA，选择 `File -> Open`，选中本项目根目录。
2. 等待 IDEA 识别 `pom.xml` 并完成 Maven 依赖加载。
3. 确认 Project SDK 为 Java 17。
4. 启动本机 MySQL，或者在 IDEA Terminal 执行：

```powershell
docker compose up -d mysql
```

5. 打开 `DruidDemoApplication.java`，点击类旁边的绿色运行按钮。

默认数据库连接为 `localhost:3306/druid_demo`，用户名 `root`，密码 `123456`。可在 IDEA Run Configuration 中通过环境变量覆盖：

```text
MYSQL_HOST=localhost;MYSQL_PORT=3306;MYSQL_DATABASE=druid_demo;MYSQL_USERNAME=root;MYSQL_PASSWORD=123456
```

## 接口

| 方法 | 地址 | 功能 |
| --- | --- | --- |
| GET | `/api/system/health` | 检查应用和数据库状态 |
| POST | `/api/users` | 创建用户 |
| GET | `/api/users/{id}` | 查询用户详情 |
| GET | `/api/users?keyword=&page=0&size=10` | 分页搜索用户 |
| PUT | `/api/users/{id}` | 更新用户 |
| DELETE | `/api/users/{id}` | 删除用户 |
| GET | `/api/dev/slow-sql` | 仅开发环境：生成慢 SQL |

创建用户示例：

```powershell
curl.exe -X POST http://localhost:8080/api/users `
  -H "Content-Type: application/json" `
  -d '{"name":"Alice","email":"alice@example.com","status":"ACTIVE"}'
```

## Druid 监控

开发环境访问 [http://localhost:8080/druid](http://localhost:8080/druid)：

- 用户名：`admin`
- 密码：`admin123`

生产环境默认关闭 Druid 后台和详细 SQL 日志。账号密码应通过 `DRUID_USERNAME`、`DRUID_PASSWORD` 环境变量设置。

## 构建与测试

```powershell
mvn -s maven-settings.xml clean test
mvn -s maven-settings.xml clean package
```

## 学习代码

第一次阅读本项目时，请参考 [项目代码导读](docs/CODE_GUIDE.md)。文档按照一次请求的执行顺序解释每个类、常用注解、数据库设计、调试方法和面试表达。

## 简历描述参考

> 基于 Spring Boot、MySQL 与 Alibaba Druid 开发用户管理 REST API，采用 Controller-Service-Repository 分层架构；实现分页检索、参数校验、统一异常处理及事务管理，通过 Druid StatFilter、WallFilter 完成慢 SQL 监控与 SQL 注入防护，并使用 Docker Compose 和集成测试提升项目可部署性与可靠性。
