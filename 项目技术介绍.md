# 智能建筑能源管理系统 — 项目详细介绍

## 一、项目概述

基于 Spring Boot 3.2 + Spring AI 1.1 的智能建筑能源管理系统，采用 MCP（Model Context Protocol）架构，将建筑能耗监控、设备故障诊断、COP 能效分析、异常检测、工单管理等核心业务能力以 Tool 形式暴露给大模型，实现"AI + 能源管理"的深度融合。系统不仅支持 AI 查询数据，还支持 AI 执行写操作（创建工单、更新设备状态、调整阈值、关闭告警），真正实现"AI 驱动的智能运维"。系统支持管理员端（McpServer）和客户端（webapp）双端架构，通过 SSE 流式推送实现实时 AI 对话，通过定时任务实现 7×24 小时设备异常自动巡检。

**项目背景**：建筑能耗占社会总能耗的 30% 以上，传统能源管理系统依赖人工巡检和阈值告警，响应慢、误报多、缺乏智能分析能力。本系统通过 MCP 协议将建筑能源管理的 20+ 个业务能力标准化暴露给大模型，让 AI 能够自主调用能耗查询、COP 计算、异常诊断等工具，结合 RAG 知识库（建筑节能规范、设备维护手册）给出专业的优化建议，实现从"被动监控"到"主动运维"的转变。

---

## 二、技术栈

### 后端框架
| 技术 | 版本 | 用途 |
|------|------|------|
| Spring Boot | 3.2.5 | 微服务基础框架，自动配置、依赖注入 |
| Spring AI | 1.1.4 | AI 集成框架（MCP Server + Client） |
| Spring WebFlux | - | 响应式编程，SSE 流式推送 |
| MyBatis-Plus | 3.5.11 | ORM 框架，代码生成、分页、乐观锁 |
| Spring Data JPA | - | WebSocket 模块数据持久化 |
| OpenFeign | 4.1.5 | 微服务间 HTTP 调用（UserChatClient → McpServer） |

### AI 与大模型
| 技术 | 用途 |
|------|------|
| 阿里通义千问（Qwen-Plus） | 主力大模型，负责对话和工具调用决策 |
| Spring AI MCP Server (WebFlux) | 将业务逻辑封装为 @Tool 供大模型调用 |
| Spring AI MCP Client | 客户端连接 MCP Server，获取工具能力 |
| 阿里百炼知识库 | RAG 知识检索（建筑节能规范、设备维护手册） |
| 百度千帆 AI 搜索 | 联网搜索补充实时信息 |
| 百度百科 | 设备和建筑术语知识补充 |

### 数据存储与缓存
| 技术 | 版本 | 用途 |
|------|------|------|
| MySQL | 8.x | 主数据库（建筑、设备、能耗、工单、告警） |
| Redis | - | 分布式缓存（阈值配置、设备信息、能耗统计） |
| Redisson | 3.26 | 分布式锁（定时任务防重复执行）、布隆过滤器 |
| Caffeine | - | 本地缓存（UserChatClient 端，500 条，3 分钟 TTL） |

### 前端与接口
| 技术 | 用途 |
|------|------|
| SSE（Server-Sent Events） | AI 对话流式推送 |
| WebSocket | 实时消息通信模块 |
| RESTful API | 前后端接口规范 |
| Swagger/OpenAPI | 接口文档 |

### 安全与认证
| 技术 | 用途 |
|------|------|
| JWT（JJWT） | 用户认证 Token |
| Easy Captcha | 图片验证码防机器人 |
| BCrypt | 密码加密存储（从 MD5 升级） |

### 工具与中间件
| 技术 | 版本 | 用途 |
|------|------|------|
| EasyExcel | 4.0.3 | Excel 导入导出（能耗数据批量导入、报表导出） |
| Apache POI | 5.2.3 | Excel 底层支持 |
| Apache PDFBox | 2.0.29 | PDF 文档解析 |
| Thymeleaf | - | HTML 报告模板渲染 |
| Hutool | 5.8.22 | 工具库（Excel、加密、HTTP） |
| 和风天气 API | - | 天气数据获取（用于优化策略生成） |

---

## 三、系统架构

### 3.1 整体架构图

```
┌─────────────────────────────────────────────────────────────────┐
│                          前端（Vue/React）                        │
│                   管理员端 + 客户端                                │
└──────────────┬──────────────────────────────┬───────────────────┘
               │ HTTP/SSE                     │ HTTP/SSE
               ▼                              ▼
┌──────────────────────────┐    ┌──────────────────────────┐
│     UserChatClient       │    │         webapp           │
│     (端口 8080)          │    │       (端口 9092)        │
│                          │    │                          │
│  ┌────────────────────┐  │    │  ┌────────────────────┐  │
│  │    AI 对话模块      │  │    │  │   客户端业务模块    │  │
│  │  - SSE 流式输出     │  │    │  │  - 能耗查询        │  │
│  │  - 思考/回答标签解析 │  │    │  │  - 工单管理        │  │
│  │  - 会话记忆(JDBC)   │  │    │  │  - 报表导出        │  │
│  └────────────────────┘  │    │  └────────────────────┘  │
│  ┌────────────────────┐  │    │  ┌────────────────────┐  │
│  │   MCP Client 模块   │  │    │  │   AI 对话模块      │  │
│  │  - 连接 McpServer   │  │    │  │  - ChatClient      │  │
│  │  - 获取 Tool 能力   │  │    │  │  - 9 个本地 Tool   │  │
│  │  - 120s 超时配置    │  │    │  │  - 会话记忆(JDBC)  │  │
│  └────────────────────┘  │    │  └────────────────────┘  │
│  ┌────────────────────┐  │    │  ┌────────────────────┐  │
│  │   认证模块          │  │    │  │   认证模块          │  │
│  │  - JWT Token        │  │    │  │  - JWT Token        │  │
│  │  - 图片验证码       │  │    │  │  - 图片验证码       │  │
│  │  - 用户注册/登录    │  │    │  │  - 用户注册/登录    │  │
│  └────────────────────┘  │    │  └────────────────────┘  │
│  ┌────────────────────┐  │    │  ┌────────────────────┐  │
│  │   缓存层            │  │    │  │   数据库层          │  │
│  │  - Caffeine 本地缓存│  │    │  │  - MySQL 直连      │  │
│  │  - 500条/3分钟TTL   │  │    │  │  - MyBatis-Plus    │  │
│  └────────────────────┘  │    │  └────────────────────┘  │
└──────────┬───────────────┘    └──────────┬────────────────┘
           │ OpenFeign (HTTP)              │
           ▼                               ▼
┌────────────────────────────────────────────────────────────────┐
│                    McpServer (端口 8014)                        │
│                                                                │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │                 MCP Server (WebFlux + SSE)                │  │
│  │                                                          │  │
│  │   @Tool 方法（20+ 个，暴露给大模型调用）：                  │  │
│  │   ┌─────────────┐ ┌─────────────┐ ┌─────────────┐       │  │
│  │   │ 能耗查询     │ │ COP 计算    │ │ 异常诊断    │       │  │
│  │   │ 5 个方法     │ │ 3 个方法    │ │ 2 个方法    │       │  │
│  │   └─────────────┘ └─────────────┘ └─────────────┘       │  │
│  │   ┌─────────────┐ ┌─────────────┐ ┌─────────────┐       │  │
│  │   │ 设备分析     │ │ 天气查询    │ │ 知识库检索  │       │  │
│  │   │ 2 个方法     │ │ 1 个方法    │ │ 3 个方法    │       │  │
│  │   └─────────────┘ └─────────────┘ └─────────────┘       │  │
│  │   ┌─────────────┐ ┌─────────────┐ ┌─────────────┐       │  │
│  │   │ 工单查询     │ │ 建筑优化    │ │ 系统工具    │       │  │
│  │   │ 3 个方法     │ │ 1 个方法    │ │ 3 个方法    │       │  │
│  │   └─────────────┘ └─────────────┘ └─────────────┘       │  │
│  │   ┌─────────────────────────────────────────────┐       │  │
│  │   │ 事务操作（AI 可执行写操作）                   │       │  │
│  │   │ 创建工单 / 更新状态 / 更新阈值 / 关闭告警    │       │  │
│  │   │ 6 个方法，全部 @Transactional 事务保护       │       │  │
│  │   └─────────────────────────────────────────────┘       │  │
│  └──────────────────────────────────────────────────────────┘  │
│                                                                │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │                  定时任务模块                              │  │
│  │  - DeviceMonitorTask: 每 4 小时扫描所有设备               │  │
│  │  - 9 项指标检测 + 连续越界判断 + 告警去重                  │  │
│  │  - Redisson 分布式锁防重复执行                            │  │
│  │  - @Transactional 事务保护                                │  │
│  └──────────────────────────────────────────────────────────┘  │
│                                                                │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │                  业务 Service 层                          │  │
│  │  - EnergyReadingsService: 能耗数据查询与统计              │  │
│  │  - CopService / CopAnalysisService: COP 计算与诊断       │  │
│  │  - ThresholdRangeService: 三级阈值管理（Redis 缓存）     │  │
│  │  - EnhancedAnomalyService: 深度根因分析                   │  │
│  │  - AnomalyAnalysisService: 异常检测流水线                 │  │
│  │  - WeatherService: 和风天气 API（WebClient 响应式）       │  │
│  │  - ReportExportService: HTML 报告生成（Thymeleaf）        │  │
│  │  - EnergyImportService: Excel 数据导入（清洗+去重+upsert）│  │
│  └──────────────────────────────────────────────────────────┘  │
│                                                                │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │                  缓存与锁层                               │  │
│  │  - Redis: @Cacheable 缓存（阈值/设备/建筑/统计/趋势）    │  │
│  │  - Redisson: 分布式锁（定时任务）+ 布隆过滤器（导入去重） │  │
│  │  - Lettuce 连接池: max-active=32                          │  │
│  └──────────────────────────────────────────────────────────┘  │
└────────────────────────┬───────────────────────────────────────┘
                         │
                         ▼
┌────────────────────────────────────────────────────────────────┐
│                     数据层                                      │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────────┐  │
│  │   MySQL      │  │   Redis      │  │   外部 API           │  │
│  │  主数据库     │  │  缓存+锁     │  │  阿里百炼/百度千帆   │  │
│  │  9 张核心表   │  │  6 类缓存    │  │  和风天气            │  │
│  └──────────────┘  └──────────────┘  └──────────────────────┘  │
└────────────────────────────────────────────────────────────────┘
```

### 3.2 MCP 工具调用流程

当用户在前端发送一条消息（如"查看建筑 A 的能耗情况"）时，完整调用链如下：

```
用户发送消息
    │
    ▼
UserChatClient (SSE 流式接口)
    │  1. 从 JDBC ChatMemory 加载会话历史
    │  2. 构建 Prompt（系统提示词 + 历史消息 + 用户消息）
    │  3. 调用 Qwen-Plus 大模型
    │
    ▼
Qwen-Plus 大模型决策
    │  分析用户意图，决定调用哪个 Tool
    │  输出: tool_call: get_building_latest_energy(buildingId=1)
    │
    ▼
MCP Client → MCP Server (HTTP SSE)
    │  通过 MCP 协议将 tool_call 转发给 McpServer
    │
    ▼
McpServer 执行 @Tool 方法
    │  1. 查 Redis 缓存（有则直接返回）
    │  2. 缓存未命中 → 查 MySQL
    │  3. 结果写入 Redis 缓存
    │  4. 返回结构化数据
    │
    ▼
Qwen-Plus 大模型生成回复
    │  基于工具返回的数据，生成自然语言回复
    │  输出: <thinking>分析数据...</thinking><answer>建筑A的能耗...</answer>
    │
    ▼
UserChatClient 流式解析
    │  解析 <thinking> 和 <answer> 标签
    │  通过 SSE 逐 chunk 推送给前端
    │
    ▼
前端实时展示
    思考过程和回答内容分开展示
```

---

## 四、核心业务功能详细说明

### 4.1 能耗监控与查询

**功能描述**：实时监控建筑的电力、水耗、空调能耗、水流量等指标，支持多维度查询和趋势分析。

**核心接口**：
| 接口 | 说明 |
|------|------|
| `/energy/acPower` | 空调系统能耗趋势（按建筑/设备筛选） |
| `/energy/waterPower` | 水耗趋势 |
| `/energy/energyPower` | 电耗趋势 |
| `/energy/waterFlow` | 水流量趋势 |
| `/energy/acPowerByBuilding` | 按建筑分组的空调能耗 |
| `/api/energy` | 客户端看板数据（当前用户的建筑+设备） |
| `/api/energyByYear` | 年度月度能耗趋势（环比分析） |

**MCP Tool**：
| Tool 名称 | 功能 |
|-----------|------|
| `get_device_latest_energy` | 获取设备最新能耗数据 |
| `get_building_latest_energy` | 获取建筑最新能耗数据 |
| `get_device_energy_range` | 按时间范围查询设备能耗 |
| `get_building_energy_by_id/name/type/code` | 多种方式查询建筑能耗 |

**技术实现**：
- 能耗数据存储在 `energy_readings` 表，单表 10 万+ 记录
- 查询结果通过 `@Cacheable` 缓存到 Redis（TTL 20 分钟）
- 支持按时间范围、设备类型、设备状态、建筑类型等多维度筛选
- 年度趋势分析计算环比变化率，自动判断上升/下降/持平

---

### 4.2 COP 能效分析

**功能描述**：基于热力学公式计算空调系统的 COP（性能系数），评估设备能效水平，诊断效率下降原因。

**核心算法**：
```
COP = (c × 流量 × ΔT × 时间) / (功率 × 时间)

其中：
- c = 1.163 Wh/(kg·℃)（水的比热容）
- 流量 = 水流量 (m³/h)
- ΔT = 回水温度 - 出水温度 (℃)
- 功率 = 空调功率 (kW)
```

**健康评分体系**（50 分制）：
| 维度 | 分值 | 评分标准 |
|------|------|---------|
| COP 分 | 0-35 | COP ≥ 5.0 满分，< 2.0 零分 |
| 温差分 | 0-15 | ΔT 在 4-8℃ 范围内满分 |
| 总分 | 0-50 | ≥ 45 优秀，35-45 良好，25-35 合格，< 25 异常 |

**诊断逻辑**：
1. 计算当前时段 COP
2. 查询上月同期 COP（相同长度时段）
3. 比较变化幅度：下降超过 10% 触发 WARNING，COP < 2.0 触发 CRITICAL
4. 联动 RAG 知识库查询可能原因（冷凝器、制冷剂等）
5. 结合环境温度给出优化建议

**MCP Tool**：
| Tool 名称 | 功能 |
|-----------|------|
| `compute_cop` | 即时 COP 计算 |
| `diagnose_cop` | COP 诊断（当前 vs 历史） |
| `diagnose_cop_efficiency` | 增强 COP 诊断（含根因分析） |

---

### 4.3 智能异常检测（两级架构）

**功能描述**：两级异常检测架构，第一级快速筛查，第二级深度分析根因。

#### 第一级：基础筛查（定时任务）

**触发方式**：`@Scheduled(cron = "${monitor.task.cron:0 0 0/4 * * ?}")`，每 4 小时自动执行

**检测流程**：
```
1. 查询所有设备最新 7 条读数
    │
    ▼
2. 加载生效中的阈值规则（三级：设备级 > 建筑级 > 全局）
    │
    ▼
3. 遍历每个设备，检查 9 项指标
    │  指标列表：power_consumption, ac_power_consumption,
    │  water_consumption, ac_outlet_temp, ac_inlet_temp,
    │  env_temp, humidity, occupancy_density, water_flow_rate
    │
    ▼
4. 连续越界判断（连续 2 次越界才触发）
    │
    ▼
5. 告警去重（10 分钟内同设备同指标不重复）
    │
    ▼
6. 批量写入告警记录 + 更新设备状态
    │
    ▼
7. 设备恢复正常时，自动关闭待处理告警
```

**防御性设计**：
- 分布式锁（Redisson）：多实例部署时只有一台执行
- 事务保护（@Transactional）：告警插入、状态更新、告警关闭在同一事务中
- 异常隔离：每个设备独立 try-catch，一个失败不影响其他
- 反射缓存（ConcurrentHashMap）：避免重复反射调用
- 批量操作：告警批量插入（saveBatch），减少 DB 交互

#### 第二级：深度根因分析

**分析维度**：
| 分析项 | 方法 | 触发条件 |
|--------|------|---------|
| 温度影响 | 统计异常中高温时段占比 | 占比 > 50% |
| COP 衰减 | 调用 COP 诊断服务 | COP < 2.0 或环比下降 10% |
| 人员密度 | 统计异常中高密度时段占比 | 占比 > 40% |
| 运行时段 | 统计异常中夜间时段占比 | 占比 > 60% |

**输出**：每个假设带置信度评分（0-1），按置信度排序生成优先级行动方案。

---

### 4.4 三级阈值体系

**功能描述**：支持设备级、建筑级、全局级三级阈值配置，优先级递减。

**匹配逻辑**：
```
1. 查找设备级阈值（deviceId + buildingId）
    │ 找到 → 使用
    │ 未找到 ↓
2. 查找建筑级阈值（null + buildingId）
    │ 找到 → 使用
    │ 未找到 ↓
3. 查找全局默认阈值（null + null）
    │ 找到 → 使用
    │ 未找到 → 跳过该设备
```

**缓存策略**：
- 阈值配置通过 `@Cacheable` 缓存到 Redis
- Key 格式：`mcp:threshold::{metricName}::{deviceId}::{buildingId}`
- TTL：20 分钟
- 支持生效时间段配置（effectiveFrom / effectiveTo）

---

### 4.5 AI 智能对话（MCP 架构）

**功能描述**：用户通过自然语言与系统交互，AI 自动调用相关工具获取数据并生成专业回复。

**MCP Tool 完整列表**：

| 分类 | Tool 名称 | 功能说明 |
|------|-----------|---------|
| 能耗查询 | `get_device_latest_energy` | 获取设备最新能耗 |
| | `get_building_latest_energy` | 获取建筑最新能耗 |
| | `get_device_energy_range` | 按时间范围查询设备能耗 |
| | `get_building_energy_by_id` | 按 ID 查询建筑能耗 |
| | `get_building_energy_by_name` | 按名称查询建筑能耗 |
| COP 计算 | `compute_cop` | 即时 COP 计算 |
| | `diagnose_cop` | COP 诊断 |
| | `diagnose_cop_efficiency` | 增强 COP 诊断 |
| 异常检测 | `smart_analyze_device` | 设备智能分析 |
| | `export_anomaly_report` | 导出异常报告 |
| | `check_abnormal_devices` | 检查异常设备 |
| | `export_abnormal_devices_excel` | 导出异常设备 Excel |
| 设备分析 | `analyze_device_energy` | 设备能耗分析 |
| | `building_optimization_strategy` | 建筑优化策略 |
| 知识库 | `search_knowledge_base` | 阿里百炼知识库搜索 |
| | `search_baidu_baike` | 百度百科搜索 |
| | `search_baidu_web` | 百度网页搜索 |
| 天气 | `get_weather_forecast` | 和风天气 7 天预报 |
| 工单查询 | `countWorkerOrder` | 工单数量统计 |
| | `queryWorkerOrder` | 工单查询 |
| | `queryWorkerOrderLog` | 工单日志查询 |
| 事务操作 | `create_work_order` | 创建工单（事务） |
| | `update_work_order_status` | 更新工单状态（乐观锁） |
| | `update_device_status` | 更新设备状态（事务） |
| | `update_threshold_range` | 更新告警阈值（事务） |
| | `close_alert` | 关闭单条告警（事务） |
| | `batch_close_alerts` | 批量关闭设备告警（事务） |
| 系统 | `get_current_date` | 获取当前日期 |
| | `get_current_datetime` | 获取当前日期时间 |
| | `parse_relative_time` | 解析相对时间（如"昨天"） |

**SSE 流式输出格式**：
```
think_start:
think:让我分析一下建筑A的能耗数据...
[THINK_END]
answer_start:
answer:根据查询结果，建筑A的总用电量为...
[DONE]
```

**会话记忆**：
- 使用 JDBC ChatMemoryRepository 持久化会话
- UserChatClient：保留最近 20 条消息
- webapp：保留最近 10 条消息
- 每个用户独立会话（conversationId = username）

---

### 4.6 工单管理

**功能描述**：客户端提交设备故障工单，管理员处理工单，支持状态流转和并发控制。

**状态流转**：
```
待处理 → 处理中 → 已完成 → 已关闭
  │                              │
  └──────── 可回退 ──────────────┘
```

**并发控制**：
- 乐观锁（version 字段）：两个管理员同时处理同一工单时，后提交的会收到"该工单已被其他人处理，请刷新后重试"
- 事务保护：工单创建涉及 3 个表操作（创建工单 + 更新设备状态 + 记录日志），在同一事务中

**核心接口**：
| 接口 | 方法 | 说明 |
|------|------|------|
| `/workerOrder/getWorkOrders` | GET | 分页查询工单（支持状态/优先级/类型/建筑/处理人筛选） |
| `/workerOrder/UpdateById` | POST | 更新工单状态（乐观锁） |
| `/workerOrder/saveOrUpdate` | POST | 创建工单 |
| `/workerOrder/CountByType` | GET | 按类型统计工单 |
| `/workerOrder/CountByStatus` | GET | 按状态统计工单 |
| `/workerOrder/errorOrder` | GET | 获取超时工单 |

---

### 4.7 数据导入导出

**Excel 批量导入流程**：
```
1. 上传 Excel 文件
    │
    ▼
2. EasyExcel 解析为 DTO 列表
    │
    ▼
3. 数据清洗
    │  - 编码 trim + 大写
    │  - 默认值填充（设备状态默认"正常"）
    │  - 数值归一化（负值归零）
    │  - 监控时间校验（不能为空、不能是未来）
    │  - 去重（buildingCode + deviceCode + monitoringTime）
    │
    ▼
4. Upsert 建筑信息（存在则更新，不存在则插入）
    │
    ▼
5. Upsert 设备信息（存在则更新，不存在则插入）
    │
    ▼
6. 批量插入能耗读数
    │  - 事务保护（任一步骤失败全部回滚）
    │  - 行级错误跟踪（每行记录成功/失败/跳过状态）
    │
    ▼
7. 返回导入结果（总数、成功数、失败数、跳过数、详细错误列表）
```

**Excel 导出**：
- 多 Sheet 导出：COP 分析、能耗数据、健康评估、综合分析
- 大数据量导出使用 EasyExcel 流式写入
- 文件名包含设备编码和日期

---

### 4.8 AI 事务操作（创新点）

**功能描述**：大模型不仅能查询数据，还能执行写操作（创建工单、更新状态、调整阈值等），真正实现"AI 驱动的智能运维"。

**事务操作工具列表**：
| Tool 名称 | 功能 | 事务保护 | 乐观锁 |
|-----------|------|---------|--------|
| `create_work_order` | 创建工单（关联设备、建筑、记录日志） | 有 | - |
| `update_work_order_status` | 更新工单状态 | 有 | 有 |
| `update_device_status` | 更新设备状态 | 有 | - |
| `update_threshold_range` | 更新告警阈值（三级阈值） | 有 | - |
| `close_alert` | 关单条告警 | 有 | - |
| `batch_close_alerts` | 批量关闭设备的所有待处理告警 | 有 | - |

**使用场景示例**：
```
用户："建筑A的空调设备好像有问题，帮我创建一个工单"
AI：→ 调用 create_work_order → "工单创建成功，工单号 WO1715xxxxx"

用户："把设备 D001 的状态改成维护保养"
AI：→ 调用 update_device_status → "设备 D001 状态已更新为：维护保养"

用户："环境温度的阈值太严了，改成 10-38 度"
AI：→ 调用 update_threshold_range → "阈值配置已更新"

用户："设备 D001 的告警都处理完了，帮我关掉"
AI：→ 调用 batch_close_alerts → "已关闭设备 D001 的 3 条告警"
```

**技术实现**：
- 所有写操作都加了 `@Transactional(rollbackFor = Exception.class)` 事务保护
- 工单状态更新使用乐观锁（version 字段），防并发冲突
- 工单创建自动记录操作日志
- 每个操作返回结构化结果（success/message/data），大模型可解析后回复用户

---

### 4.9 PDF 文档管理

**功能描述**：管理建筑节能标准、设备使用说明书等 PDF 文档，支持在线预览和下载。

**核心接口**：
| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/documents` | GET | 查询文档列表（支持关键词和分类筛选） |
| `/api/pdf/{id}` | GET | 在线预览 PDF（支持 Range 分片加载） |
| `/api/pdf/download/{id}` | GET | 下载 PDF 文件 |

**技术实现**：
- 文档元数据存储在 `pdf_document` 表
- PDF 文件存储在本地磁盘（`pdf.storage.path` 配置）
- 在线预览支持 HTTP Range 请求，实现大文件分片加载
- 下载接口设置 `Content-Disposition: attachment` 触发浏览器下载

---

### 4.10 告警管理

**告警生命周期**：
```
异常检测触发 → 创建告警（PENDING）
                    │
                    ▼
            设备恢复正常 → 自动关闭（RESOLVED）
                    │
                    ▼
            管理员处理 → 手动关闭
```

**告警级别计算**：
| 超标幅度 | 级别 |
|---------|------|
| < 10% | 级别 1（轻微） |
| 10% - 20% | 级别 2（中等） |
| > 20% | 级别 3（严重） |

**告警去重机制**：
- 时间窗口：10 分钟
- 去重维度：设备 ID + 指标名称
- 实现：批量查询已有 PENDING 告警 → Set 内存过滤

---

## 五、技术亮点详细说明

### 5.1 线程与并发

| 技术点 | 实现方式 | 解决的问题 |
|--------|---------|-----------|
| SSE 流式推送 | Spring WebFlux Flux + 响应式流 | AI 对话实时输出，用户无需等待完整响应（LLM 生成需要 10-30 秒） |
| @Async 异步处理 | Spring 异步线程池 | 用户登录时异步触发异常检测，不阻塞登录响应 |
| 定时任务线程池 | `spring.task.scheduling.pool.size=2` | 支持多个定时任务并行执行 |
| ConcurrentHashMap 缓存 | 反射 Field 对象缓存 | 避免定时任务中重复反射调用（9 指标 × 7 读数 = 63 次反射 → 缓存后 0 次） |
| AtomicBoolean/AtomicReference | SSE 流式标签解析 | 无锁线程安全的状态管理（思考/回答标签切换） |
| 单线程 SSE + 响应式 | WebFlux 非阻塞 I/O | 单线程可处理多个 SSE 连接，节省线程资源 |

### 5.2 Redis 缓存策略

**缓存架构**：
```
请求 → Caffeine 本地缓存（3min）→ Redis 分布式缓存（20min）→ MySQL
         │ 命中返回                │ 命中返回                  │ 查询
         └─────────────────────────┴───────────────────────────┘
```

**缓存项明细**：
| 缓存项 | 缓存方式 | TTL | Key 格式 | 说明 |
|--------|---------|-----|---------|------|
| 阈值配置 | @Cacheable + Redis | 20 分钟 | `mcp:threshold::*` | 三级阈值查询结果 |
| 设备信息 | @Cacheable + Redis | 20 分钟 | `mcp:device::*` | 设备基础信息 |
| 建筑信息 | @Cacheable + Redis | 20 分钟 | `mcp:building::*` | 建筑基础信息 |
| 能耗统计 | @Cacheable + Redis | 20 分钟 | `mcp:energy:stats::*` | 聚合查询结果 |
| 能耗趋势 | @Cacheable + Redis | 20 分钟 | `mcp:energy:trend::*` | 趋势数据 |
| 会话上下文 | @Cacheable + Redis | 20 分钟 | `mcp:chat:context::*` | AI 对话上下文 |
| 用户信息 | Caffeine 本地缓存 | 3 分钟 | 内存 | 减少 Feign 调用 |

**缓存配置优化**：
```yaml
spring:
  data:
    redis:
      lettuce:
        pool:
          max-active: 32    # 最大连接数
          max-idle: 16      # 最大空闲连接
          min-idle: 8       # 最小空闲连接
  cache:
    type: redis
    redis:
      cache-null-values: false    # 不缓存空值
      key-prefix: "mcp:"         # 统一前缀
      time-to-live: 1200000      # 20 分钟
```

### 5.3 分布式锁

| 场景 | 实现方式 | 配置 | 说明 |
|------|---------|------|------|
| 定时任务防重复执行 | Redisson RLock | tryLock(0, 30min) | 多实例部署时只有一台执行，锁 30 分钟自动释放 |
| 工单并发处理 | MyBatis-Plus 乐观锁 | version 字段 | updateById 自动带 WHERE version=旧值，冲突返回 0 行 |
| 数据导入去重 | Redisson RBloomFilter | 预计容量 100 万 | 快速判断能耗数据是否已导入 |

**分布式锁代码示例**：
```java
RLock lock = redissonClient.getLock("lock:device-monitor-task");
try {
    if (!lock.tryLock(0, 30, TimeUnit.MINUTES)) {
        log.info("另一个实例正在执行，跳过");
        return;
    }
    executeTask();  // 执行业务逻辑
} finally {
    if (lock.isHeldByCurrentThread()) {
        lock.unlock();
    }
}
```

**乐观锁代码示例**：
```java
// 实体类
@Column(name = "version")
private Integer version = 0;

// 更新时 MyBatis-Plus 自动生成：
// UPDATE work_order SET status=?, version=version+1 WHERE id=? AND version=旧值
boolean ok = workOrderService.updateById(workOrder);
if (!ok) {
    return Result.error("该工单已被其他人处理，请刷新后重试");
}
```

### 5.4 代码质量优化

| 优化项 | 说明 |
|--------|------|
| 公共方法提取 | `ThresholdCheckUtil` 工具类：阈值检查、反射字段访问、告警级别计算等公共方法，消除 DeviceMonitorTask 和 DeviceAnomalyDetector 的重复代码 |
| 用户信息统一查询 | `UserContextUtil` 工具类：统一获取当前用户的 deviceCode/buildingCode，消除 6+ 处重复代码 |
| 枚举类型规范 | 工单状态（WorkOrderStatus）、优先级（Priority）使用枚举，避免魔法字符串 |
| SQL 注解完整 | 所有 Mapper 方法都有 @Select/@Insert 注解，无遗漏 |

### 5.5 性能优化措施

| 优化项 | 优化前 | 优化后 | 提升倍数 |
|--------|--------|--------|---------|
| 异常检测 N+1 查询 | 每个异常点查一次 DB（200 次） | 一次批量查询 + TreeMap.floorEntry() 内存查找 | **200x** |
| 告警去重 N+1 查询 | 每条告警查一次 DB（50 次） | 一次查询 + HashSet 内存过滤 | **50x** |
| 反射字段访问 | 每次调用都 getDeclaredField() | ConcurrentHashMap.computeIfAbsent() 缓存 Field | **10x** |
| 告警批量插入 | 逐条 insert（50 次 SQL） | saveBatch 批量插入（1 次 SQL） | **50x** |
| 用户信息查询 | 用 name 查询 + LambdaQueryWrapper | 用 id 查询 + selectById（主键索引） | **2x** |
| 阈值三级匹配 | 每次查数据库 | Redis @Cacheable 缓存 20 分钟 | 命中时 **0ms** |
| HikariCP 连接池 | 默认 10 连接 | maximum-pool-size=20 | 并发 **2x** |
| Redis Lettuce 连接池 | 默认 8 连接 | max-active=32 | 并发 **4x** |
| 用户信息重复代码 | 每个 Controller 重复 5 行查询 | UserContextUtil 统一方法 | 可维护性 ↑ |

### 5.6 防御性编程

| 措施 | 实现位置 | 说明 |
|------|---------|------|
| 全局异常处理 | @RestControllerAdvice | 统一捕获异常，返回友好 JSON 错误信息，不暴露堆栈 |
| 输入校验 | @Valid + @NotNull/@NotBlank/@Size | 实体字段校验，防止恶意输入和空指针 |
| 事务保护 | @Transactional(rollbackFor=Exception.class) | 工单创建、数据导入等多表操作在同一事务中 |
| 异常隔离 | 定时任务 try-catch | 每个设备独立处理，一个失败不影响其他设备继续检查 |
| 告警去重 | 10 分钟窗口 + 设备+指标维度 | 防止告警风暴，避免重复通知 |
| 连续越界检测 | CONSECUTIVE_OUT_OF_RANGE=2 | 连续 2 次越界才触发，避免瞬时波动误报 |
| 乐观锁防并发 | version 字段 + updateById | 工单多人同时操作时，后提交者收到冲突提示 |
| 分布式锁防重 | Redisson RLock | 定时任务多实例只执行一次，防止重复告警 |
| 空值防护 | buildings != null ? getLocation() : null | 查询结果可能为空时的安全访问 |
| 设备状态校验 | STATUS_MAINTENANCE 跳过 | 维护中的设备不参与异常检测 |

---

## 六、数据库核心表

| 表名 | 说明 | 核心字段 | 数据量级 |
|------|------|---------|---------|
| buildings | 建筑信息 | building_id, building_code, building_name, type, location | ~10 |
| devices | 设备信息 | device_id, device_code, building_id, device_type, device_status | ~50 |
| energy_readings | 能耗读数 | reading_id, device_id, building_id, monitoring_time, 9 项指标 | ~10 万+ |
| work_order | 工单 | id, order_no, type, status, handler_id, version（乐观锁） | ~1000 |
| work_order_log | 工单日志 | id, order_id, action, content, created_at | ~3000 |
| alert_record | 告警记录 | id, device_id, metric_name, abnormal_value, alert_level, status | ~5000 |
| threshold_range | 阈值配置 | id, metric_name, min_value, max_value, device_id, building_id | ~100 |
| chat_message | AI 对话 | id, conversation_id, message_type, content | ~1 万+ |
| customer | 客户用户 | id, name, password, device_code, building_code | ~100 |
| pdf_document | PDF 文档 | id, file_name, category | ~50 |

---

## 七、项目成果（可量化指标）

- 实现 **27+ 个 MCP Tool** 方法，覆盖能耗查询、COP 计算、异常诊断、工单管理、事务操作等 9 大业务领域
- **创新点**：AI 不仅能查询数据，还能执行写操作（创建工单、更新状态、调整阈值），实现真正的"AI 驱动运维"
- 支持 **9 项能耗指标**的实时监控和异常检测
- 三级阈值体系，支持 **设备级 > 建筑级 > 全局** 的精细化配置
- 两级异常检测架构：第一级快速筛查 + 第二级深度根因分析（4 个假设维度，带置信度评分）
- 异常检测从 N+1 查询优化为 **单次批量查询**，性能提升 **200 倍**
- 定时任务支持 **Redisson 分布式锁 + MyBatis-Plus 乐观锁**，可水平扩展部署
- SSE 流式推送实现 **秒级响应**的 AI 对话体验，支持思考过程实时展示
- Excel 批量导入支持 **数据清洗、去重、upsert**，单次可处理万级数据
- COP 能效分析基于热力学公式，结合 RAG 知识库给出专业优化建议
- 会话记忆持久化到数据库，支持跨会话上下文保持
