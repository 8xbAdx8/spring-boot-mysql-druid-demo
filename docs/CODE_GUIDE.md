# 项目代码导读

这份文档帮助你理解项目中的每个 Java 类，以及一次 HTTP 请求如何经过 Controller、Service、Repository，最终访问 MySQL。

## 1. 先理解整体结构

项目采用常见的三层架构：

```text
浏览器或 Postman
       |
       v
Controller（接收请求、返回响应）
       |
       v
Service（业务规则、事务）
       |
       v
Repository（执行 SQL）
       |
       v
MySQL
```

其他代码负责传递数据、处理异常和配置 Druid：

```text
dto        请求和响应的数据格式
model      数据库中的业务对象
exception  统一处理错误
config     注册 Druid 监控功能
```

建议按以下顺序阅读：

1. `DruidDemoApplication`
2. `UserController`
3. `UserService`
4. `UserRepository`
5. `UserRequest`、`User`、`ApiResponse`、`PageResponse`
6. `GlobalExceptionHandler`
7. `DruidMonitorConfig`、`DruidMonitorProperties`
8. `DruidDemoApplicationTests`

## 2. 启动类

### DruidDemoApplication

位置：`src/main/java/com/example/druiddemo/DruidDemoApplication.java`

这是整个项目的入口。运行 `main` 方法后，Spring Boot 会启动内嵌 Tomcat、扫描组件、创建数据库连接池，并监听 8080 端口。

```java
@SpringBootApplication
public class DruidDemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(DruidDemoApplication.class, args);
    }
}
```

`@SpringBootApplication` 可以理解为三个功能的组合：

- 标记这是配置类
- 开启 Spring Boot 自动配置
- 扫描当前包及其子包中的 Spring 组件

因此，其他类都放在 `com.example.druiddemo` 下面，Spring 才能自动找到它们。

## 3. Controller 层

Controller 是 HTTP 请求进入 Java 程序的第一站。

### UserController

位置：`controller/UserController.java`

负责用户管理接口：

| 注解 | HTTP 请求 | 作用 |
| --- | --- | --- |
| `@PostMapping` | `POST /api/users` | 创建用户 |
| `@GetMapping("/{id}")` | `GET /api/users/1` | 查询单个用户 |
| `@GetMapping` | `GET /api/users?page=0&size=10` | 分页查询 |
| `@PutMapping("/{id}")` | `PUT /api/users/1` | 更新用户 |
| `@DeleteMapping("/{id}")` | `DELETE /api/users/1` | 删除用户 |

以创建用户为例：

```java
@PostMapping
public ResponseEntity<ApiResponse<User>> create(
        @Valid @RequestBody UserRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.created(userService.create(request)));
}
```

- `@RequestBody`：把请求中的 JSON 转换为 `UserRequest`
- `@Valid`：执行 `UserRequest` 上的参数校验
- `userService.create(request)`：把真正的业务处理交给 Service
- `HttpStatus.CREATED`：创建成功返回 HTTP 201
- `ApiResponse<User>`：保持所有成功响应的结构一致

Controller 不直接写 SQL，也不负责判断邮箱是否重复。这样可以避免一个类承担太多职责。

### SystemController

位置：`controller/SystemController.java`

提供 `/api/system/health`。它执行 `SELECT 1`，用于确认应用和数据库连接是否正常。

### DevToolsController

位置：`controller/DevToolsController.java`

提供 `/api/dev/slow-sql`，故意执行约 1.2 秒的 SQL，方便观察 Druid 的慢 SQL 监控。

```java
@Profile("dev")
```

这个注解表示该类只在 `dev` 环境中生效。使用 `prod` 环境启动时，这个危险的测试接口不会存在。

## 4. Service 层

### UserService

位置：`service/UserService.java`

Service 负责业务规则，例如：

- 邮箱统一转换为小写
- 姓名去掉首尾空格
- 检查邮箱是否已被使用
- 检查用户是否存在
- 限制分页参数范围
- 控制数据库事务

创建方法的核心逻辑：

```java
@Transactional
public User create(UserRequest request) {
    String email = normalizeEmail(request.email());
    ensureEmailAvailable(email, null);
    return userRepository.save(
            request.name().trim(),
            email,
            request.normalizedStatus()
    );
}
```

`@Transactional` 表示这个方法在一个事务中执行。方法正常结束时提交；抛出运行时异常时回滚。

查询方法使用：

```java
@Transactional(readOnly = true)
```

它表示这是只读事务，有助于表达意图，也方便数据库和框架进行优化。

## 5. Repository 层

### UserRepository

位置：`repository/UserRepository.java`

Repository 只负责数据库访问，使用 `JdbcTemplate` 执行 SQL。

```java
return jdbcTemplate.query(
        "SELECT * FROM app_user WHERE id = ?",
        USER_ROW_MAPPER,
        id
).stream().findFirst();
```

SQL 中的 `?` 是预编译参数。不要使用字符串拼接用户输入，否则可能产生 SQL 注入风险。

`USER_ROW_MAPPER` 负责把数据库的一行转换为 Java `User` 对象：

```text
数据库 id          -> User.id
数据库 name        -> User.name
数据库 created_at  -> User.createdAt
```

`save` 方法使用 `GeneratedKeyHolder` 获取 MySQL 自动生成的主键。插入成功后，再根据主键查询完整用户并返回。

分页查询使用：

```sql
LIMIT ? OFFSET ?
```

- `LIMIT` 表示本页最多返回多少条
- `OFFSET` 表示跳过前面多少条
- 第 2 页、每页 10 条时，offset 为 `2 * 10 = 20`

项目中的页码从 0 开始。

## 6. DTO 和 Model

### UserRequest

位置：`dto/UserRequest.java`

表示客户端创建或更新用户时提交的数据。

```java
public record UserRequest(String name, String email, String status)
```

这里使用 Java `record`，适合只保存数据的对象。编译器会自动生成构造方法和字段访问方法。

常见校验注解：

- `@NotBlank`：不能是 null、空字符串或只有空格
- `@Email`：必须符合邮箱格式
- `@Size`：限制字符串长度
- `@Pattern`：状态只能是 `ACTIVE` 或 `DISABLED`

### User

位置：`model/User.java`

表示系统中的完整用户数据，对应数据库 `app_user` 表的一行。它比 `UserRequest` 多了 ID、创建时间和更新时间。

### ApiResponse

位置：`dto/ApiResponse.java`

统一包装接口响应：

```json
{
  "success": true,
  "data": {},
  "message": null,
  "timestamp": "2026-07-18T17:00:00"
}
```

静态方法 `ok`、`created` 和 `error` 避免每个 Controller 重复构造响应。

### PageResponse

位置：`dto/PageResponse.java`

分页响应包含：

- `content`：当前页的数据
- `page`：当前页码
- `size`：每页数量
- `total`：总记录数
- `totalPages`：总页数

## 7. 异常处理

### NotFoundException

查询或删除不存在的用户时抛出，最终返回 HTTP 404。

### ConflictException

创建重复邮箱时抛出，最终返回 HTTP 409。

### GlobalExceptionHandler

位置：`exception/GlobalExceptionHandler.java`

`@RestControllerAdvice` 会拦截 Controller 抛出的异常，并转换成统一 JSON。

例如，`UserService` 抛出 `NotFoundException` 后，Controller 不需要 `try/catch`，全局处理器会返回：

```json
{
  "success": false,
  "data": null,
  "message": "用户不存在：99",
  "timestamp": "2026-07-18T17:00:00"
}
```

这种设计让正常业务代码和错误响应逻辑分离。

## 8. Druid 配置类

### DruidMonitorProperties

位置：`config/DruidMonitorProperties.java`

使用 `@ConfigurationProperties(prefix = "druid.monitor")`，把 YAML 中的配置绑定到 Java 对象。

例如：

```yaml
druid:
  monitor:
    stat-view-servlet:
      enabled: true
```

会绑定到 `statViewServlet.enabled`。

### DruidMonitorConfig

位置：`config/DruidMonitorConfig.java`

负责注册两个 Druid Web 组件：

- `StatViewServlet`：提供 `/druid` 监控后台
- `WebStatFilter`：统计 URI、Session 和 SQL 调用链

配置类负责“如何创建组件”，Properties 类负责“组件使用什么配置”。

## 9. 数据库与环境配置

### schema.sql

位置：`src/main/resources/schema.sql`

应用在开发环境启动时执行该文件，用 `CREATE TABLE IF NOT EXISTS` 创建 `app_user` 表。

表中的重要设计：

- 邮箱唯一索引：从数据库层防止重复邮箱
- 姓名索引：帮助姓名查询
- 状态和创建时间联合索引：为后续按状态查询预留
- `utf8mb4`：完整支持中文和常用 Unicode 字符

### application.yml

公共配置，包括端口、MySQL、Druid 连接池和监控参数。

### application-dev.yml

开发配置，开启 SQL 日志、连接泄漏排查和 Druid 后台。

### application-prod.yml

生产配置，默认关闭 Druid 后台、详细 SQL 日志和自动建表，降低安全与性能风险。

## 10. 一次创建用户请求的完整旅程

请求：

```http
POST /api/users
Content-Type: application/json

{
  "name": "Alice",
  "email": "ALICE@example.com",
  "status": "ACTIVE"
}
```

执行顺序：

1. Tomcat 收到 HTTP 请求。
2. `UserController.create` 接收 JSON。
3. `@Valid` 校验姓名、邮箱和状态。
4. `UserService.create` 去除空格并将邮箱转为小写。
5. `UserRepository.existsByEmail` 查询邮箱是否重复。
6. `UserRepository.save` 执行参数化 INSERT。
7. Repository 把数据库记录转换为 `User`。
8. Service 提交事务。
9. Controller 使用 `ApiResponse.created` 包装结果。
10. 客户端收到 HTTP 201 和 JSON 数据。

任何一步抛出业务异常，都会由 `GlobalExceptionHandler` 转换为对应的错误响应。

## 11. 测试类

### DruidDemoApplicationTests

位置：`src/test/java/com/example/druiddemo/DruidDemoApplicationTests.java`

`@SpringBootTest(webEnvironment = RANDOM_PORT)` 会启动完整 Spring 容器，并随机选择测试端口，避免与本机 8080 冲突。

测试覆盖：

- 数据源确实是 `DruidDataSource`
- Druid 登录页面可以访问
- 健康检查可以连接数据库
- 用户创建、搜索和删除流程正常
- 非法用户参数返回 HTTP 400

这类测试比单纯调用一个方法更接近真实运行过程，所以称为集成测试。

## 12. 在 IDEA 中阅读代码的方法

- 按住 `Ctrl` 并点击类名或方法名：跳到定义
- `Alt + 左方向键`：回到上一个位置
- `Ctrl + Alt + H`：查看某个方法被谁调用
- `Ctrl + F12`：查看当前类的方法列表
- 在方法左侧点击：添加断点
- 使用 Debug 启动：逐行观察变量变化

第一次调试建议在以下位置打断点：

1. `UserController.create`
2. `UserService.create`
3. `UserRepository.save`
4. `GlobalExceptionHandler.handleValidation`

然后发送创建用户请求，观察 `request`、`email`、SQL 参数和最终返回值如何变化。

## 13. 面试时怎么讲

可以用下面的顺序介绍项目：

1. 先说项目解决了什么问题：用户管理与数据库运行监控。
2. 再说架构：Controller、Service、Repository 分层。
3. 说明数据一致性：事务、邮箱业务校验和数据库唯一索引双重保障。
4. 说明安全：参数校验、预编译 SQL、Druid WallFilter、生产环境关闭监控后台。
5. 说明性能：Druid 连接池、分页查询、索引和慢 SQL 监控。
6. 说明质量：自动建表、环境隔离和完整链路集成测试。

不要只背技术名词。最好能打开 `UserService.create` 和 `UserRepository.save`，结合实际代码解释为什么这样分层。
