# 校园二手交易平台

## 项目简介

这是一个基于 **Vue3 + Element Plus + Spring Boot** 开发的校园二手交易平台，面向在校学生，提供一个安全、便捷的校园二手物品交易场所。

## 技术栈

### 前端
- **Vue 3** - 渐进式 JavaScript 框架
- **Element Plus** - 基于 Vue 3 的组件库
- **Vue Router** - 官方路由管理器
- **Pinia** - Vue 3 状态管理工具
- **Axios** - HTTP 客户端
- **Vite** - 下一代前端构建工具

### 后端
- **Spring Boot 3.2.0** - Java 应用开发框架
- **Spring Data JPA** - 数据持久化
- **MySQL** - 关系型数据库
- **JWT** - 身份认证
- **BCrypt** - 密码加密
- **Maven** - 项目管理工具

## 功能特性

### 用户功能
- ✅ 用户注册、登录（JWT 认证）
- ✅ 个人信息管理
- ✅ 修改个人资料
- ✅ **头像上传**：支持本地上传头像图片

### 商品功能
- ✅ 商品发布、编辑、删除
- ✅ 商品浏览（分页、分类筛选）
- ✅ 商品搜索
- ✅ 商品详情查看
- ✅ 商品上架/下架
- ✅ **库存数量管理**：商品支持设置库存数量，购买后自动减库存
- ✅ **多图上传**：支持本地上传商品图片，最多5张

### 交易功能
- ✅ 下单购买
- ✅ 订单管理（买家/卖家）
- ✅ 订单状态跟踪
- ✅ 订单确认/取消/完成

### 收藏功能
- ✅ 收藏商品
- ✅ 取消收藏
- ✅ 查看我的收藏

### 实时聊天功能
- ✅ **联系卖家**：商品详情页可直接联系卖家
- ✅ **即时通讯**：基于WebSocket的实时聊天
- ✅ **聊天历史**：保存聊天记录，可随时查看
- ✅ **消息通知**：未读消息提醒

## 数据库设计

### 主要表结构

#### 用户表 (user)
- id - 主键
- username - 用户名（唯一）
- password - 密码（加密）
- nickname - 昵称
- email - 邮箱
- phone - 手机号
- avatar - 头像
- role - 角色（USER/ADMIN）
- status - 状态（ACTIVE/DISABLED）
- created_at - 创建时间
- updated_at - 更新时间

#### 商品表 (product)
- id - 主键
- title - 商品标题
- description - 商品描述
- price - 价格
- original_price - 原价
- quantity - 库存数量（新增）
- category_id - 分类ID
- images - 商品图片（JSON 数组）
- status - 状态（ON_SALE/SOLD/OFF_SHELF）
- view_count - 浏览次数
- favorite_count - 收藏次数
- seller_id - 卖家ID
- location - 所在地
- contact - 联系方式
- created_at - 创建时间
- updated_at - 更新时间

#### 订单表 (order)
- id - 主键
- order_no - 订单号（唯一）
- product_id - 商品ID
- buyer_id - 买家ID
- seller_id - 卖家ID
- price - 成交价格
- status - 订单状态（PENDING/CONFIRMED/CANCELLED/COMPLETED）
- buyer_message - 买家留言
- created_at - 创建时间
- updated_at - 更新时间

#### 分类表 (category)
- id - 主键
- name - 分类名称
- description - 分类描述
- icon - 分类图标
- sort_order - 排序
- created_at - 创建时间
- updated_at - 更新时间

#### 收藏表 (favorite)
- id - 主键
- user_id - 用户ID
- product_id - 商品ID
- created_at - 创建时间

#### 消息表 (message)
- id - 主键
- sender_id - 发送者ID
- receiver_id - 接收者ID
- product_id - 关联商品ID
- content - 消息内容
- is_read - 是否已读
- created_at - 创建时间

## 项目结构

```
大作业/
├── backend/                    # 后端项目
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/lll_cxt/marketplace/secondhand/
│   │   │   │   ├── entity/           # 实体类
│   │   │   │   ├── dto/              # 数据传输对象
│   │   │   │   ├── repository/       # 数据访问层
│   │   │   │   ├── service/          # 业务逻辑层
│   │   │   │   ├── controller/       # 控制器层
│   │   │   │   ├── config/           # 配置类
│   │   │   │   ├── util/             # 工具类
│   │   │   │   ├── exception/        # 异常处理
│   │   │   │   └── SecondHandApplication.java
│   │   │   └── resources/
│   │   │       ├── application.yml   # 应用配置
│   │   │       └── schema.sql        # 数据库脚本
│   │   └── test/
│   └── pom.xml                        # Maven 配置
├── frontend/                   # 前端项目
│   ├── src/
│   │   ├── cxt_lll/
│   │   │   ├── api/                  # API 接口
│   │   │   ├── assets/               # 静态资源
│   │   │   ├── components/           # 公共组件
│   │   │   ├── router/               # 路由配置
│   │   │   ├── store/                # 状态管理
│   │   │   ├── utils/                # 工具函数
│   │   │   └── views/                # 页面组件
│   │   ├── App.vue                   # 根组件
│   │   └── main.js                   # 入口文件
│   ├── index.html
│   ├── vite.config.js                # Vite 配置
│   └── package.json                  # npm 配置
├── .gitignore
└── README.md
```

## 环境要求

### 后端
- JDK 17 或更高版本
- Maven 3.6 或更高版本
- MySQL 8.0 或更高版本

### 前端
- Node.js 16 或更高版本
- npm 或 yarn

## 安装部署

### 1. 数据库配置

#### 创建数据库
```sql
CREATE DATABASE secondhand_platform DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

#### 执行数据库脚本
执行 `backend/src/main/resources/schema.sql` 文件中的 SQL 语句，创建表结构并插入初始数据。

#### 修改数据库配置
编辑 `backend/src/main/resources/application.yml` 文件，修改数据库连接信息：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/secondhand_platform?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: root          # 修改为你的数据库用户名
    password: root          # 修改为你的数据库密码
```

### 2. 后端部署

#### 进入后端目录
```bash
cd backend
```

#### 安装依赖并编译
```bash
mvn clean install
```

#### 运行后端服务
```bash
mvn spring-boot:run
```

后端服务将在 `http://localhost:8080/api` 启动。

### 3. 前端部署

#### 进入前端目录
```bash
cd frontend
```

#### 安装依赖
```bash
npm install
```

#### 运行开发服务器
```bash
npm run dev
```

前端服务将在 `http://localhost:3000` 启动。

#### 构建生产版本
```bash
npm run build
```

构建后的文件将输出到 `frontend/dist` 目录。

## 使用说明

### 用户系统说明
- **统一登录入口**：系统采用统一的用户登录系统，不区分管理员和普通用户
- **用户注册**：任何人都可以注册账号成为平台用户
- **昵称设置**：注册时可以设置昵称，如果不填写则使用用户名作为昵称
- **用户权限**：所有注册用户都可以发布商品、购买商品、收藏商品等
- **个人数据隔离**：用户只能管理自己发布的商品和订单

### 注册与登录
1. 访问 `http://localhost:3000`
2. 点击"立即注册"创建账号
3. 使用注册的账号登录系统

### 浏览商品
- 在首页可以浏览所有在售商品
- 使用分类导航筛选不同类别的商品
- 使用搜索框搜索商品

### 发布商品
1. 登录后点击用户头像 -> "发布商品"
2. 填写商品信息（标题、分类、价格、图片、描述等）
3. **设置库存数量**：输入商品的可售数量（默认1件）
4. **上传商品图片**：点击上传区域，选择本地图片（最多5张，每张最大5MB）
5. 点击"发布商品"按钮

### 购买商品
1. 进入商品详情页
2. 查看商品信息和库存状态
3. 点击"立即购买"按钮
4. 填写留言（可选）
5. 确认下单
6. **库存自动扣减**：下单成功后，商品库存-1，库存为0时自动变为已售出状态

### 管理订单
1. 点击用户头像 -> "个人中心"
2. 选择"我的订单"
3. 可以查看我购买的订单和我卖出的订单
4. 买家可以取消待确认的订单，确认已完成的交易
5. 卖家可以确认待处理的订单

### 个人资料
1. 点击用户头像 -> "个人中心" -> "个人资料"
2. 可以修改昵称、邮箱、手机号
3. **上传头像**：点击"上传头像"按钮，选择本地图片（最大2MB）
4. 点击"保存修改"更新信息
5. **头像自动同步**：上传头像后自动保存并同步到全站显示

### 联系卖家（实时聊天）
1. **发送消息**：
   - 进入商品详情页
   - 点击"联系卖家"按钮
   - 在聊天窗口中输入消息
   - 按Enter或点击"发送"按钮发送消息
   - 消息会保存到数据库

2. **查看消息**：
   - 点击用户头像 -> "个人中心" -> "我的消息"
   - 查看所有聊天会话列表
   - 点击任一会话查看完整聊天历史
   - 可以直接回复消息

3. **消息特性**：
   - 消息自动保存，刷新后依然可见
   - 双方都可以在"我的消息"中查看和回复
   - 消息按时间顺序显示
   - 支持关联商品信息

### 收藏商品
- 在商品详情页点击"收藏"按钮
- 在"个人中心" -> "我的收藏"查看所有收藏的商品

## API 接口文档

### 认证接口
- `POST /api/auth/register` - 用户注册
- `POST /api/auth/login` - 用户登录

### 用户接口
- `GET /api/users/current` - 获取当前用户信息（需登录）
- `PUT /api/users/update` - 更新用户信息（需登录）

### 商品接口
- `GET /api/products/list` - 获取商品列表
- `GET /api/products/search` - 搜索商品
- `GET /api/products/detail/{id}` - 获取商品详情
- `POST /api/products/create` - 发布商品（需登录）
- `PUT /api/products/update/{id}` - 更新商品（需登录）
- `DELETE /api/products/delete/{id}` - 删除商品（需登录）
- `PUT /api/products/off-shelf/{id}` - 下架商品（需登录）
- `GET /api/products/my-products` - 获取我的商品（需登录）

### 分类接口
- `GET /api/categories/list` - 获取分类列表

### 订单接口
- `POST /api/orders/create` - 创建订单（需登录）
- `GET /api/orders/my-buy` - 获取我购买的订单（需登录）
- `GET /api/orders/my-sell` - 获取我卖出的订单（需登录）
- `PUT /api/orders/update-status/{id}` - 更新订单状态（需登录）

### 收藏接口
- `POST /api/favorites/add/{productId}` - 添加收藏（需登录）
- `DELETE /api/favorites/remove/{productId}` - 取消收藏（需登录）
- `GET /api/favorites/my-favorites` - 获取我的收藏（需登录）

### 文件上传接口
- `POST /api/upload/image` - 上传单张图片（需登录）
- `POST /api/upload/images` - 上传多张图片（需登录）

### 消息接口
- `POST /api/messages/send` - 发送消息（HTTP接口，需登录）
- `POST /app/chat` - 发送消息（WebSocket）
- `GET /api/messages/history/{partnerId}` - 获取聊天历史（需登录）
- `GET /api/messages/conversations` - 获取聊天会话列表（需登录）
- `GET /api/messages/unread-count` - 获取未读消息数（需登录）
- `PUT /api/messages/mark-read/{partnerId}` - 标记消息为已读（需登录）

## 核心功能实现

### JWT 认证
- 用户登录后返回 JWT Token
- 前端将 Token 存储在 localStorage
- 需要认证的请求在 Header 中携带 `Authorization: Bearer {token}`
- 后端使用拦截器验证 Token 有效性

### 分页查询
- 所有列表接口都支持分页
- 参数：`current`（当前页，默认1）、`size`（每页数量，默认10）
- 返回：`records`（数据列表）、`total`（总数）、`current`、`size`

### 文件上传
- **本地图片上传**：支持从本地选择图片上传到服务器
- 图片验证：限制 5MB 大小，仅支持图片格式
- 多图上传：最多支持 5 张图片
- 自动生成 UUID 文件名，防止文件名冲突
- 商品图片使用 JSON 数组格式存储图片 URL
- 图片预览：上传后实时预览，支持删除
- 图片轮播展示

### 状态管理
- 商品状态：ON_SALE（在售）、SOLD（已售出）、OFF_SHELF（已下架）
- 订单状态：PENDING（待确认）、CONFIRMED（已确认）、CANCELLED（已取消）、COMPLETED（已完成）
- 用户状态：ACTIVE（活跃）、DISABLED（禁用）

## 系统特点

### 安全性
- 密码使用 BCrypt 加密存储
- JWT Token 身份验证
- 权限校验（用户只能操作自己的数据）
- SQL 注入防护
- XSS 攻击防护

### 用户体验
- 响应式设计，适配多种设备
- 优雅的 UI 界面
- 友好的错误提示
- 流畅的交互动画
- 分页加载，提升性能

### 代码质量
- 前后端分离架构
- RESTful API 设计
- 分层架构（Controller -> Service -> Repository）
- 统一异常处理
- 统一响应格式
- 代码注释完善

## 测试账号

系统已预置管理员账号（在 schema.sql 中）：
- 用户名：`admin`
- 密码：`admin123`

## 常见问题

### 1. 后端启动失败
- 检查 JDK 版本是否为 17 或更高
- 检查 MySQL 数据库是否正常运行
- 检查数据库连接配置是否正确
- 检查端口 8080 是否被占用

### 2. 前端启动失败
- 检查 Node.js 版本是否为 16 或更高
- 删除 `node_modules` 目录，重新执行 `npm install`
- 检查端口 3000 是否被占用

### 3. 前后端联调失败
- 检查后端服务是否正常启动
- 检查前端代理配置（vite.config.js）
- 打开浏览器开发者工具查看网络请求

### 4. 图片无法显示
- 确保后端服务正常启动
- 确保前端代理配置正确（vite.config.js）
- 图片URL格式应为：`/api/uploads/filename.jpg`（注意 `/api` 前缀）
- 检查浏览器开发者工具的Network面板，查看图片请求状态
- 确保uploads目录存在且有读取权限

## 未来扩展

- [ ] WebSocket实时聊天完整集成（当前为基础版本）
- [ ] 商品评价系统
- [ ] 后台管理系统
- [ ] 数据统计与分析
- [ ] 邮件通知
- [ ] 短信验证
- [ ] 支付功能
- [ ] 推荐系统

## 开发团队

本项目是期末大作业项目，采用前后端分离架构，使用主流技术栈开发。

## 开源协议

MIT License

## 更新日志

### v1.0.0 (2024-12-16)
- ✅ 完成基础功能开发
- ✅ 用户注册登录
- ✅ 商品发布与管理
- ✅ 订单交易流程
- ✅ 收藏功能
- ✅ 个人中心
- ✅ 本地图片上传功能（支持多图上传、预览、验证）
- ✅ **商品库存管理**：添加数量字段，支持库存自动扣减
- ✅ **头像上传**：个人中心支持本地上传头像，自动同步到全站
- ✅ **界面优化**：统一登录注册页面风格，与主界面保持一致
- ✅ **实时聊天**：完整的聊天系统，消息持久化保存，双向通信
  - 商品详情页联系卖家
  - 个人中心查看所有消息
  - 聊天历史自动保存
  - 支持消息已读标记

