# 建筑能源管理系统 MCP 工具使用手册

---

## 目录

1. [MCP 协议概述](#一mcp-协议概述)
2. [工具总览](#二工具总览)
3. [系统辅助类工具](#三系统辅助类工具mcp-systemservice)
4. [建筑信息类工具](#四建筑信息类工具mcp-buildingservice)
5. [设备管理类工具](#五设备管理类工具mcp-devicesservice)
6. [能耗数据类工具](#六能耗数据类工具mcp-energyservice)
7. [COP 能效类工具](#七cop-能效类工具mcp-copcomputemcp-copservice)
8. [异常分析类工具](#八异常分析类工具mcp-deviceanalysisservice--mcp-anomalyanalysisservice--mcp-energyscantool)
9. [工单管理类工具](#九工单管理类工具workordermcpservice)
10. [知识检索类工具](#十知识检索类工具mcp-bailianknowledgeservice--mcp-baike-service)
11. [策略优化类工具](#十一策略优化类工具buildingoptimizationmcptool)
12. [事务操作工具（写操作）](#十二事务操作工具mcp-transaction-service)
13. [工具选择决策树](#十三工具选择决策树)
14. [标准操作流程（SOP）](#十四标准操作流程sop)
15. [输出格式规范](#十五输出格式规范)
16. [特殊场景处理](#十六特殊场景处理)
17. [附录](#附录)

---

## 一、MCP 协议概述

### 1.1 什么是 MCP

Model Context Protocol（模型上下文协议）是连接 AI 大模型与外部工具、数据源的标准化通信协议。本系统基于 MCP 架构，将建筑能源管理领域的专业能力封装为标准化工具，使大模型能够通过自然语言理解并调用复杂的系统功能。

### 1.2 核心特性

| 特性 | 说明 |
|------|------|
| **工具发现** | 动态 Schema 注册，新增工具自动感知 |
| **类型安全** | JSON-RPC 2.0 通信，强类型参数校验 |
| **上下文保持** | 多轮调用状态共享，支持复杂任务链 |
| **错误传播** | 标准化错误码，前置失败自动阻断后续调用 |
| **链式编排** | AI 自动拆解复杂请求为有序工具调用序列 |
| **事务安全** | 写操作工具支持 `@Transactional` 事务回滚 |

### 1.3 调用模式

```
用户自然语言请求
    ↓
意图识别 + 参数提取
    ↓
工具选择（决策树）
    ↓
MCP 工具调用（JSON-RPC 2.0）
    ↓
数据验证与处理
    ↓
结构化结果输出
```

### 1.4 工具分类说明

本系统通过 MCP 协议暴露 **10 大类、45+ 个专业工具**，完整覆盖建筑能源管理业务链路：

- **查询类工具**（Read）：数据检索、状态查询、历史分析
- **诊断类工具**（Analyze）：智能诊断、异常检测、能效评估
- **事务类工具**（Write）：工单创建、状态更新、阈值调整、告警关闭
- **导出类工具**（Export）：报告生成、Excel 导出
- **知识类工具**（Knowledge）：规范检索、术语解释

---

## 二、工具总览

### 2.1 工具分类速查表

| 服务类别 | 服务标识 | 工具数量 | 核心功能 | 操作类型 |
|---------|---------|---------|---------|---------|
| 系统辅助 | `McpSystemService` | 3 | 时间获取与解析 | 查询 |
| 建筑信息 | `McpBuildingService` | 4 | 建筑档案查询 | 查询 |
| 设备管理 | `McpDevicesService` | 8 | 设备台账与状态 | 查询 |
| 能耗数据 | `McpEnergyService` | 8 | 能耗记录查询与分析 | 查询 |
| COP 能效 | `McpCopCompute` / `McpCopService` | 3 | 空调性能系数计算与诊断 | 诊断 |
| 异常分析 | `McpDeviceAnalysisService` / `McpAnomalyAnalysisService` / `McpEnergyScanTool` | 4 | 故障检测与报告生成 | 诊断/导出 |
| 工单管理 | `WorkOrderMcpService` | 3 | 运维工单查询与统计 | 查询 |
| 知识检索 | `McpBailianKnowledgeService` / `McpBaikeService` | 2 | 专业规范与术语查询 | 查询 |
| 策略优化 | `BuildingOptimizationMcpTool` | 1 | 能源系统优化策略生成 | 诊断 |
| **事务操作** | **`McpTransactionService`** | **5** | **工单/设备/阈值/告警的写操作** | **事务** |

### 2.2 工具命名规范

- **查询类**：`query_xxx` / `get_xxx` / `query_xxx_by_yyy`
- **诊断类**：`diagnose_xxx` / `smart_analyze_xxx` / `check_xxx`
- **导出类**：`export_xxx_report` / `export_xxx_excel`
- **计算类**：`compute_xxx`
- **事务类**：`create_xxx` / `update_xxx` / `close_xxx` / `batch_xxx`

---

## 三、系统辅助类工具（McpSystemService）

> **用途**：时间获取与解析，是所有时间相关查询的前置基础。  
> **调用模式**：无状态查询，无需前置条件。

### 3.1 工具清单

| 工具名称 | 功能描述 | 典型场景 |
|---------|---------|---------|
| `get_current_date` | 获取当前系统日期 | 需要时间基准的所有操作 |
| `get_current_datetime` | 获取当前完整时间戳 | 需要精确时刻的诊断任务 |
| `parse_relative_time` | 将口语化时间转换为标准日期 | "昨天"、"上周"、"上月"等 |

### 3.2 强制使用规范

⚠️ **任何包含相对时间词（"昨天"、"上周"、"上月"、"最近7天"等）的请求，必须先调用 `parse_relative_time` 转换为标准日期格式，再传递给能耗查询工具。**

### 3.3 调用示例

**请求**：查询"昨天"的日期范围

```json
{
  "jsonrpc": "2.0",
  "method": "tools/call",
  "params": {
    "name": "parse_relative_time",
    "arguments": {
      "time_expression": "昨天"
    }
  }
}
```

**响应**：

```json
{
  "standard_date": "2026-05-06",
  "date_range": {
    "start": "2026-05-06T00:00:00",
    "end": "2026-05-06T23:59:59"
  },
  "weekday": "星期三"
}
```

---

## 四、建筑信息类工具（McpBuildingService）

> **用途**：查询建筑基础档案。  
> **查询优先级**：`by_id` > `by_code` > `by_name`

### 4.1 工具清单

| 工具名称 | 查询维度 | 匹配方式 | 返回结果 |
|---------|---------|---------|---------|
| `query_buildings` | 全局 | 列出所有 | 建筑基础信息列表 |
| `query_building_by_id` | 建筑 ID | 精确匹配 | 单条详细信息 |
| `query_building_by_code` | 建筑编号 | 精确匹配 | 单条详细信息 |
| `query_building_by_name` | 建筑名称 | 模糊匹配 | 可能多条 |

### 4.2 查询路径选择

```
用户提供了精确 ID/编号？
    ├── 是 → 使用 by_id / by_code（跳过模糊查询）
    └── 否 → 使用 by_name
              └── 返回多条？→ 向用户确认具体对象后再继续
```

### 4.3 关键字段说明

| 字段 | 类型 | 说明 |
|------|------|------|
| `building_id` | string | 系统内部唯一标识（如 `BLD-2023-001`） |
| `building_code` | string | 建筑标准化编号（如 `XZL-01`） |
| `building_name` | string | 建筑中文名称（如 "行政楼"） |
| `building_type` | string | 建筑类型（办公/商业/住宅/工业） |
| `area_sqm` | number | 建筑面积（平方米） |

---

## 五、设备管理类工具（McpDevicesService）

> **用途**：查询设备档案与运行状态。支持通过建筑关联查询或直接查询设备。  
> **关联查询**：建筑 → 设备列表 → 单设备详情

### 5.1 工具清单

| 工具名称 | 查询维度 | 匹配方式 |
|---------|---------|---------|
| `query_devices_by_building_id` | 建筑 ID | 精确关联 |
| `query_devices_by_building_code` | 建筑编号 | 精确关联 |
| `query_devices_by_building_name` | 建筑名称 | 模糊关联 |
| `query_all_devices` | 全局 | 列出所有 |
| `query_device_by_id` | 设备 ID | 精确匹配 |
| `query_device_by_code` | 设备铭牌编号 | 精确匹配 |
| `query_devices_by_type` | 设备类型 | 批量筛选 |
| `query_by_deviceStatus` | 设备运行状态 | 状态筛选 |

### 5.2 设备查询路径

```
已知设备 ID？
    ├── 是 → query_device_by_id（最精确）
    └── 否 → 已知设备铭牌编号？
              ├── 是 → query_device_by_code
              └── 否 → 已知所属建筑？
                        ├── 是 → query_devices_by_building_id（推荐）
                        └── 否 → 已知设备类型？
                                  ├── 是 → query_devices_by_type
                                  └── 否 → query_all_devices（全量，慎用）
```

### 5.3 设备状态查询

| 工具名称 | 功能 | 参数 |
|---------|------|------|
| `query_by_deviceStatus` | 按运行状态筛选设备 | `status`: running / stopped / fault / offline |

---

## 六、能耗数据类工具（McpEnergyService）

> **用途**：查询设备与建筑的能耗记录，是数据分析的核心数据源。  
> **时间处理**：相对时间必须先通过 `parse_relative_time` 转换。

### 6.1 工具清单

| 工具名称 | 查询对象 | 时间范围 | 典型场景 |
|---------|---------|---------|---------|
| `get_device_latest_energy` | 单设备 | 最新一条 | 实时监控 |
| `get_building_latest_energy` | 建筑下所有设备 | 最新一条 | 建筑总能耗快照 |
| `get_device_full_profile` | 单设备 | 完整档案 | 设备综合信息 |
| `get_device_energy_range` | 单设备 | 指定范围 | 历史趋势分析 |
| `get_building_energy_by_id` | 建筑 | 可选范围 | 按建筑 ID 统计 |
| `get_building_energy_by_name` | 建筑 | 可选范围 | 按名称模糊查询 |
| `get_building_energy_by_type` | 建筑类型 | 可选范围 | 同类建筑对比 |
| `get_building_energy_by_code` | 建筑编码 | 可选范围 | 按编码精确查询 |

### 6.2 时间参数规范

| 用户表述 | 处理方式 | 工具调用 |
|---------|---------|---------|
| "最新" / "当前" | 无需时间参数 | `get_xxx_latest_energy` |
| "昨天" / "上周" | 先 `parse_relative_time` | 再传入 `get_xxx_energy_range` |
| "2026-04-01 到 2026-04-07" | 直接解析为标准格式 | 传入 `get_xxx_energy_range` |

### 6.3 返回数据结构

```json
{
  "device_id": "DEV-HVAC-001",
  "device_name": "行政楼一层空调主机",
  "data_points": 96,
  "total_kwh": 2847.5,
  "peak_kw": 420,
  "average_kw": 118.6,
  "unit": "kWh",
  "start_time": "2026-04-19T00:00:00",
  "end_time": "2026-04-19T23:59:59"
}
```

---

## 七、COP 能效类工具（McpCopCompute / McpCopService）

> **用途**：空调系统性能系数计算与诊断。优先使用增强版工具。  
> **联动建议**：发现异常后主动建议导出报告、查看工单、获取优化策略。

### 7.1 工具清单

| 工具名称 | 功能 | 优先级 | 适用场景 |
|---------|------|--------|---------|
| `diagnose_cop_efficiency` | **COP 能效专项诊断**（增强版） | ⭐ **优先使用** | 自动计算时间范围并联动知识库 |
| `diagnose_cop` | COP 智能诊断 | 次选 | 分析当前时段并与上月同期对比 |
| `compute_cop` | COP 基础计算 | 仅需数值时使用 | 计算指定时刻的性能系数 |

### 7.2 诊断维度

`diagnose_cop_efficiency` 自动输出：
- 当前时段 COP 值
- 与上月同期对比（变化率）
- 健康状态评级（优秀/良好/一般/差）
- 优化建议（基于知识库）

### 7.3 联动建议

调用 COP 诊断工具发现异常后，应主动建议：
1. 导出详细报告：`export_anomaly_report`
2. 查看关联工单：`getWorkOrderList`
3. 获取优化策略：`building_optimization_strategy`

---

## 八、异常分析类工具（McpDeviceAnalysisService / McpAnomalyAnalysisService / McpEnergyScanTool）

> **用途**：设备异常检测与报告生成。  
> **前置检查**：导出工具前必须先执行数据源查询确认有数据。

### 8.1 工具清单

| 工具名称 | 输出 | 功能描述 |
|---------|------|---------|
| `smart_analyze_device` | 诊断结果 | 自动检测设备异常，智能判断分析深度 |
| `check_abnormal_devices` | 摘要统计 | 全局扫描，统计异常设备数量和异常点数量 |
| `export_anomaly_report` | HTML 文件 | 导出能耗异常深度分析报告 |
| `export_abnormal_devices_excel` | Excel 文件 | 导出异常设备明细表，返回下载链接 |

### 8.2 使用流程

```
单设备深度体检
    → smart_analyze_device(device_id)

系统快速巡检
    → check_abnormal_devices()

发现异常后
    → export_anomaly_report() [生成 HTML 报告]
    → export_abnormal_devices_excel() [生成 Excel 明细]
    → getWorkOrderList() [查看关联工单]
```

### 8.3 报告导出前置检查

⚠️ **调用导出工具前，必须先执行数据源查询确认有数据，再调用导出工具。**

---

## 九、工单管理类工具（WorkOrderMcpService）

> **用途**：运维工单查询与统计。  
> **注意**：本服务仅提供查询功能，写操作请使用 `McpTransactionService`。

### 9.1 工具清单

| 工具名称 | 功能 | 过滤条件 |
|---------|------|---------|
| `getWorkOrder` | 统计超时未处理工单数量 | 无 |
| `getWorkOrderList` | 查询待处理工单信息 | 建筑 ID、设备 ID（可选） |
| `getWorkOrderLogList` | 查询工单操作日志 | 工单 ID、动作类型、操作人、时间范围 |

### 9.2 典型查询场景

| 场景 | 工具组合 |
|------|---------|
| 查看系统整体工单积压 | `getWorkOrder` |
| 查看某建筑待处理工单 | `getWorkOrderList(building_id="xxx")` |
| 查看某设备维修历史 | `getWorkOrderList(device_id="xxx")` |
| 追踪具体工单处理流程 | `getWorkOrderLogList(work_order_id="xxx")` |

---

## 十、知识检索类工具（McpBailianKnowledgeService / McpBaike Service）

> **用途**：专业规范与术语解释查询。  
> **数据源**：阿里云百炼知识库（内部规范）+ 百度百科（通用术语）。

### 10.1 工具清单

| 工具名称 | 数据源 | 返回内容 |
|---------|--------|---------|
| `knowledge` | 阿里云百炼知识库 | 专业规范、术语解释、标准条文 |
| `search_baike` | 百度百科 | 词条摘要、详细内容、图片链接 |

### 10.2 使用场景

- **运维规范查询**："GB 50189 对空调系统能效的要求是什么？"
- **术语解释**："什么是 IPLV？"
- **故障排查参考**："冷水机组高压报警的可能原因"

---

## 十一、策略优化类工具（BuildingOptimizationMcpTool）

> **用途**：基于天气预报和历史数据生成建筑能源系统优化策略。  
> **输入依赖**：需要天气预报数据 + 历史环境/能耗数据。

### 11.1 工具清单

| 工具名称 | 输入 | 输出 |
|---------|------|------|
| `building_optimization_strategy` | 未来 7 天天气预报 + 历史环境/能耗数据 | 建筑能源系统优化策略 |

### 11.2 策略内容

- 空调系统启停时间优化
- 冷热源负荷预测与调度
- 设备运行参数调整建议
- 预期节能效果评估

---

## 十二、事务操作工具（McpTransaction Service）

> ⚠️ **重要**：本服务为**写操作工具**，所有方法均标注 `@Transactional(rollbackFor = Exception.class)`，支持事务回滚。  
> **用途**：让大模型可以执行写操作，包括工单管理、设备状态管理、阈值管理、告警管理。  
> **安全原则**：写操作前必须确认目标对象存在，操作后必须记录日志。

### 12.1 工具清单

| 工具名称 | 功能 | 业务场景 | 事务安全 |
|---------|------|---------|---------|
| `create_work_order` | 创建工单 | 设备故障报修、保养申请 | ✅ 事务回滚 |
| `update_work_order_status` | 更新工单状态 | 接单、完成、关闭工单 | ✅ 乐观锁 + 事务 |
| `update_device_status` | 更新设备状态 | 标记故障、恢复运行 | ✅ 事务回滚 |
| `update_threshold_range` | 更新告警阈值 | 调整能耗告警上下限 | ✅ 事务回滚 |
| `close_alert` | 关闭单条告警 | 确认异常已处理/误报 | ✅ 事务回滚 |
| `batch_close_alerts` | 批量关闭告警 | 设备维修后批量消警 | ✅ 事务回滚 |

### 12.2 工单操作

#### 12.2.1 `create_work_order` — 创建工单

**触发场景**：用户报告设备故障或需要维修时调用。

**参数说明**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `deviceCode` | string | ✅ | 设备编号（铭牌编号） |
| `buildingId` | integer | ✅ | 建筑 ID |
| `type` | string | ✅ | 工单类型，如"设备故障"、"设备保养"、"其他" |
| `description` | string | ✅ | 故障描述 |
| `priority` | string | ❌ | 优先级："高"、"中"、"低"（默认"中"） |
| `operatorId` | long | ❌ | 操作人 ID |

**业务逻辑**：
1. 校验设备编号是否存在
2. 自动生成工单号（格式：`WO` + 时间戳）
3. 创建工单记录（状态：待处理）
4. 记录操作日志（`AI 助手创建工单`）
5. **联动更新设备状态**：根据工单类型自动映射设备状态
    - 含"故障" → 设备状态变为"故障"
    - 含"保养/维护" → 设备状态变为"维护保养"

**返回结果**：

```json
{
  "success": true,
  "message": "工单创建成功",
  "orderNo": "WO1715067842000",
  "orderId": 12345,
  "status": "待处理"
}
```

**失败处理**：
- 设备不存在 → 返回 `success: false`，终止后续操作
- 数据库异常 → 事务回滚，返回错误信息

---

#### 12.2.2 `update_work_order_status` — 更新工单状态

**触发场景**：处理工单时调用，更新工单状态。

**参数说明**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `orderId` | long | ✅ | 工单 ID |
| `status` | string | ✅ | 新状态："处理中"、"已完成"、"已关闭" |
| `handlerId` | long | ❌ | 处理人 ID |
| `remark` | string | ❌ | 处理备注 |

**状态流转规则**：

```
待处理 → 处理中 → 已完成
      ↘ 已关闭
```

**业务逻辑**：
1. 校验工单是否存在
2. 校验状态值合法性（枚举校验）
3. **乐观锁更新**（基于 `updatedAt` 或版本号）
4. 状态为"已完成"时，自动填充 `completedTime`
5. 记录操作日志

**返回结果**：

```json
{
  "success": true,
  "message": "工单状态已更新为：已完成",
  "orderNo": "WO1715067842000",
  "newStatus": "已完成"
}
```

**并发控制**：
- 乐观锁冲突时返回：`"工单已被其他人处理，请刷新后重试"`

---

### 12.3 设备操作

#### 12.3.1 `update_device_status` — 更新设备状态

**触发场景**：当需要修改设备运行状态时调用。

**参数说明**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `deviceCode` | string | ✅ | 设备编号 |
| `status` | string | ✅ | 新状态："正常"、"故障"、"维护保养" |

**业务逻辑**：
1. 校验设备编号是否存在
2. 直接更新设备状态字段

**返回结果**：

```json
{
  "success": true,
  "message": "设备 DEV-HVAC-001 状态已更新为：故障"
}
```

---

### 12.4 阈值操作

#### 12.4.1 `update_threshold_range` — 更新告警阈值

**触发场景**：当需要调整设备或建筑的能耗告警阈值时调用。

**参数说明**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `metricName` | string | ✅ | 指标名称，如`power_consumption`、`ac_power`、`env_temp` |
| `minValue` | decimal | ❌ | 最小值（`null` 表示不限下限） |
| `maxValue` | decimal | ❌ | 最大值（`null` 表示不限上限） |
| `deviceId` | integer | ❌ | 设备 ID（`null` 表示建筑级或全局级） |
| `buildingId` | integer | ❌ | 建筑 ID（`null` 表示全局级） |
| `unit` | string | ❌ | 单位 |

**阈值级别**：

| deviceId | buildingId | 级别 |
|---------|-----------|------|
| 有值 | 有值/无值 | **设备级**（最优先） |
| `null` | 有值 | **建筑级** |
| `null` | `null` | **全局级** |

**业务逻辑**：
1. 根据 `metricName` + `deviceId` + `buildingId` 查询已有配置
2. 存在则更新，不存在则创建新配置
3. 自动填充 `createdAt`

**返回结果**：

```json
{
  "success": true,
  "message": "阈值配置已更新",
  "metricName": "power_consumption",
  "minValue": 100.0,
  "maxValue": 500.0,
  "level": "设备级"
}
```

---

### 12.5 告警操作

#### 12.5.1 `close_alert` — 关闭单条告警

**触发场景**：当确认异常已处理或为误报时，关闭告警记录。

**参数说明**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `alertId` | long | ✅ | 告警 ID |
| `remark` | string | ❌ | 关闭原因（默认"AI 助手手动关闭"） |

**业务逻辑**：
1. 校验告警是否存在
2. 更新状态为 `STATUS_RESOLVED`
3. 填充 `handledAt` 为当前时间

**返回结果**：

```json
{
  "success": true,
  "message": "告警已关闭",
  "alertId": 8888
}
```

---

#### 12.5.2 `batch_close_alerts` — 批量关闭告警

**触发场景**：批量关闭某个设备的所有待处理告警。

**参数说明**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `deviceCode` | string | ✅ | 设备编号 |
| `remark` | string | ❌ | 关闭原因（默认"AI 助手批量关闭"） |

**业务逻辑**：
1. 校验设备编号是否存在
2. 查询该设备所有 `STATUS_PENDING` 状态的告警
3. 批量更新为 `STATUS_RESOLVED`

**返回结果**：

```json
{
  "success": true,
  "message": "已关闭设备 DEV-HVAC-001 的 5 条告警",
  "closedCount": 5
}
```

---

### 12.6 事务操作安全规范

#### 12.6.1 调用前检查清单

```markdown
✅ 已确认目标对象存在（设备/工单/告警）
✅ 参数值在合法枚举范围内（如状态值）
✅ 用户已明确授权执行写操作
✅ 操作影响范围已告知用户
```

#### 12.6.2 异常处理

| 异常类型 | 处理方式 | 返回信息 |
|---------|---------|---------|
| 目标不存在 | 立即返回，不执行数据库操作 | `"设备/工单/告警 xxx 不存在"` |
| 状态值非法 | 枚举校验失败，返回可选值列表 | `"无效的工单状态: xxx，可选值：待处理、处理中、已完成、已关闭"` |
| 乐观锁冲突 | 返回冲突提示，建议用户刷新 | `"工单已被其他人处理，请刷新后重试"` |
| 数据库异常 | `@Transactional` 自动回滚 | `"更新失败：{异常信息}"` |

#### 12.6.3 设备状态映射规则

创建工单时，系统自动根据工单类型映射设备状态：

| 工单类型包含关键词 | 映射后的设备状态 |
|------------------|---------------|
| "故障" | "故障" |
| "保养" / "维护" | "维护保养" |
| 其他 | "正常" |

---

## 十三、工具选择决策树

### 13.1 场景化决策流程

```
用户请求
    │
    ├─► 提供了精确 ID 或编号？
    │       ├─► 是 → 直接使用 by_id / by_code（跳过模糊查询）
    │       └─► 否 → 继续判断
    │
    ├─► 提供了建筑/设备名称？
    │       ├─► 是 → 使用 by_name，若返回多条需用户确认
    │       └─► 否 → 继续判断
    │
    ├─► 询问"最新"能耗/状态？
    │       ├─► 是 → 使用 latest 类工具（无需时间范围）
    │       └─► 否 → 继续判断
    │
    ├─► 询问某段时间的能耗趋势？
    │       ├─► 是 → 使用 range / by_time 类工具（需确认时间范围）
    │       └─► 否 → 继续判断
    │
    ├─► 要求诊断/分析？
    │       ├─► 是 → 优先使用联动知识库的诊断工具
    │       │            （如 diagnose_cop_efficiency）
    │       └─► 否 → 继续判断
    │
    ├─► 要求创建/更新/关闭？（写操作）
    │       ├─► 是 → 使用 McpTransactionService 对应工具
    │       │            确认用户授权后再执行
    │       └─► 否 → 继续判断
    │
    └─► 要求导出报告？
            ├─► 是 → 先执行数据源查询确认有数据
            │            再调用导出工具
            └─► 否 → 按通用查询处理
```

### 13.2 快速决策表

| 场景 | 首选工具类型 | 示例 |
|------|------------|------|
| 精确查询 | `by_id` / `by_code` | `query_building_by_id` |
| 模糊查询 | `by_name` | `query_building_by_name` |
| 最新数据 | `latest` | `get_device_latest_energy` |
| 历史趋势 | `range` | `get_device_energy_range` |
| 智能诊断 | `diagnose_xxx_efficiency` | `diagnose_cop_efficiency` |
| 全局扫描 | `check_xxx` | `check_abnormal_devices` |
| 报告导出 | `export_xxx` | `export_anomaly_report` |
| 创建工单 | `create_xxx` | `create_work_order` |
| 更新状态 | `update_xxx` | `update_work_order_status` |
| 关闭告警 | `close_xxx` / `batch_xxx` | `close_alert` |

---

## 十四、标准操作流程（SOP）

### 14.1 时间处理规范（强制）

**规则**：任何包含相对时间词的请求，必须在工具调用前完成时间转换。

**禁止行为**：将"昨天"、"上周"等自然语言直接传递给能耗查询工具。

**标准流程**：

```
用户输入："查一下上周的能耗"
    ↓
Step 1: parse_relative_time("上周")
    ↓
返回：{ "start": "2026-04-28", "end": "2026-05-04" }
    ↓
Step 2: get_device_energy_range(device_id, "2026-04-28", "2026-05-04")
```

### 14.2 链式调用规范

**单用户请求可能需要组合多个工具完成。**

**示例**：查询"行政楼昨天的空调能耗"

```
Step 1: parse_relative_time("昨天")
        → 2026-05-06

Step 2: query_building_by_name("行政楼")
        → building_id: "BLD-2023-001"

Step 3: query_devices_by_building_id("BLD-2023-001")
        → 筛选 type="空调机组" 的设备列表

Step 4: get_device_energy_range(device_id, "2026-05-06", "2026-05-06")
        → 返回能耗数据
```

### 14.3 写操作 SOP

**规则**：执行写操作前必须获得用户明确授权。

**标准流程**：

```
用户输入："把设备 DEV-001 的状态改为故障"
    ↓
Step 1: 向用户确认操作意图
        "确认将设备 DEV-001 的状态更新为'故障'吗？此操作会同步创建关联工单。"
    ↓
用户确认后
    ↓
Step 2: 执行 update_device_status("DEV-001", "故障")
    ↓
Step 3: 返回操作结果 + 后续建议
        "设备状态已更新。建议创建维修工单以跟踪处理进度。"
```

### 14.4 失败即停原则

**规则**：前置工具调用失败时，不得继续后续调用。

**处理流程**：

```
Step 2 失败（未找到"行政楼"）
    ↓
停止后续调用
    ↓
向用户说明："未找到名为'行政楼'的建筑，请确认名称或提供建筑编号"
    ↓
等待用户澄清后重新执行
```

---

## 十五、输出格式规范

### 15.1 两段式输出结构

每次处理用户请求后，必须严格按以下结构输出：

#### 第一段：`<thinking>`（内部推理过程）

```markdown
<thinking>
1. **意图识别**：
   类别：[A-查询 / B-诊断 / C-导出 / D-事务 / E-系统辅助]
   需求概括：[一句话精准概括用户核心诉求]

2. **参数分析**：
   - 已提供参数：[列出提取的参数及值]
   - 缺失参数：[列出必填但未提供的参数]
   - 模糊参数：[指出需澄清的参数及转换后的值]

3. **工具决策**：
   - 首选工具：[tool_name]
   - 决策理由：[基于适用场景和约束解释]
   - 调用链：[step1→step2→step3]
   - 备选/后续工具：[下一步可能调用的工具]

4. **数据验证**：
   - 调用状态：[成功 / 失败 / 部分成功]
   - 返回关键数据摘要：[如"查询到3条记录"]
   - 有效性检查：[数据非空/返回错误/服务异常]

5. **执行结果**：[成功 / 失败 / 需用户确认]
</thinking>
```

#### 第二段：`<answer>`（面向用户的结论）

```markdown
<answer>
[友好结论性语句]

- **数据呈现**：
  [Markdown 表格或列表，字段名与工具返回一致]

- **异常/缺失说明**：
  [若部分字段为 null、查询无结果，明确说明原因]

- **专业分析与建议**：
  [基于返回数据给出业务层面解读和运维建议]

- **后续引导**：
  [提示可进行的下一步操作]
</answer>
```

### 15.2 数据呈现规范

- **多条记录**：使用 Markdown 表格，表头与工具返回字段名保持一致
- **单条详情**：使用无序列表，键值对形式展示
- **数值格式化**：能耗保留 1 位小数，百分比保留 2 位小数
- **时间格式化**：统一使用 `YYYY-MM-DD HH:mm:ss`

---

## 十六、特殊场景处理

### 16.1 多义词与歧义处理

**场景**："空调"可能指设备类型或建筑系统。

**处理流程**：

```
thinking 中列出歧义点：
  - "空调"可能指：
    1. 设备类型：空调机组、风机盘管等
    2. 建筑系统：暖通空调系统（HVAC）

answer 中向用户确认：
  "您提到的'空调'是指具体的空调设备，还是整个暖通系统？
   如果是指设备，我可以为您查询该建筑下的所有空调类设备。"
```

### 16.2 空结果处理

**禁止行为**：编造数据或假设返回值。

**标准回复模板**：

```markdown
**查询结果为空**

- **查询条件**：[建筑名称="xxx"，时间范围="2026-04-01 至 2026-04-07"]
- **系统状态**：当前无匹配记录
- **可能原因**：
  1. 该时间段内设备未运行或无数据上报
  2. 建筑/设备名称输入有误
  3. 该设备尚未接入能耗监测系统
- **建议操作**：
  - 扩大时间范围（如改为"最近30天"）
  - 使用模糊查询确认建筑/设备名称
  - 联系系统管理员确认数据接入状态
```

### 16.3 诊断类工具联动

**规则**：调用 `diagnose_cop_efficiency` 或 `smart_analyze_device` 后，若结果包含异常，应主动建议后续操作。

**联动建议模板**：

```markdown
**检测到异常，建议进行以下操作：**

1. 📄 **导出详细报告**：调用 `export_anomaly_report` 生成 HTML 深度分析
2. 🔧 **查看关联工单**：调用 `getWorkOrderList` 检查是否有待处理维修任务
3. 📊 **获取优化策略**：调用 `building_optimization_strategy` 生成节能方案
4. 📝 **创建维修工单**：如确认需要维修，调用 `create_work_order` 创建工单
```

### 16.4 模糊查询结果多条

**处理流程**：

```
query_building_by_name("办公楼")
    ↓
返回 3 条记录：
  1. 行政办公楼（ID: BLD-001, 面积: 12000㎡）
  2. 研发办公楼（ID: BLD-002, 面积: 8000㎡）
  3. 旧办公楼（ID: BLD-003, 面积: 5000㎡）
    ↓
向用户确认："找到 3 个匹配结果，请确认您要查询的是哪一栋？"
    ↓
用户确认后，使用确定的 ID 继续后续查询
```

### 16.5 写操作确认流程

**规则**：涉及数据修改的操作，必须向用户确认后再执行。

**确认模板**：

```markdown
**操作确认**

即将执行以下操作：
- **操作类型**：更新设备状态
- **目标对象**：设备 DEV-HVAC-001
- **变更内容**：状态从 "正常" 更新为 "故障"
- **影响范围**：该设备将标记为故障状态，可能触发告警

请确认是否继续？（回复"确认"以执行）
```

---

## 附录

### 附录 A：工具快速索引

#### A.1 按服务分类索引

| 服务名 | 工具列表 |
|--------|---------|
| `McpSystemService` | `get_current_date`, `get_current_datetime`, `parse_relative_time` |
| `McpBuildingService` | `query_buildings`, `query_building_by_id`, `query_building_by_name`, `query_building_by_code` |
| `McpDevicesService` | `query_devices_by_building_id`, `query_devices_by_building_code`, `query_devices_by_building_name`, `query_all_devices`, `query_device_by_id`, `query_device_by_code`, `query_devices_by_type`, `query_by_deviceStatus` |
| `McpEnergyService` | `get_device_latest_energy`, `get_building_latest_energy`, `get_device_full_profile`, `get_device_energy_range`, `get_building_energy_by_id`, `get_building_energy_by_name`, `get_building_energy_by_type`, `get_building_energy_by_code` |
| `McpCopCompute` | `compute_cop` |
| `McpCopService` | `diagnose_cop`, `diagnose_cop_efficiency` |
| `McpDeviceAnalysisService` | `smart_analyze_device` |
| `McpAnomalyAnalysisService` | `export_anomaly_report` |
| `McpEnergyScanTool` | `check_abnormal_devices`, `export_abnormal_devices_excel` |
| `WorkOrderMcpService` | `getWorkOrder`, `getWorkOrderList`, `getWorkOrderLogList` |
| `McpBailianKnowledgeService` | `knowledge` |
| `McpBaikeService` | `search_baike` |
| `BuildingOptimizationMcpTool` | `building_optimization_strategy` |
| **`McpTransactionService`** | **`create_work_order`, `update_work_order_status`, `update_device_status`, `update_threshold_range`, `close_alert`, `batch_close_alerts`** |

#### A.2 按场景索引

| 用户意图 | 推荐工具 |
|---------|---------|
| 查建筑信息 | `query_building_by_id` / `query_building_by_name` |
| 查设备列表 | `query_devices_by_building_id` |
| 查实时能耗 | `get_device_latest_energy` |
| 查历史能耗 | `get_device_energy_range` |
| 诊断空调能效 | `diagnose_cop_efficiency` |
| 检查系统异常 | `check_abnormal_devices` |
| 导出异常报告 | `export_anomaly_report` |
| 查工单状态 | `getWorkOrderList` |
| 查专业规范 | `knowledge` |
| 获取优化建议 | `building_optimization_strategy` |
| **创建维修工单** | **`create_work_order`** |
| **更新工单状态** | **`update_work_order_status`** |
| **更新设备状态** | **`update_device_status`** |
| **调整告警阈值** | **`update_threshold_range`** |
| **关闭告警** | **`close_alert` / `batch_close_alerts`** |

### 附录 B：事务操作枚举值参考

#### B.1 工单优先级（Priority）

| 枚举值 | 说明 |
|--------|------|
| `高` | 紧急故障，需立即处理 |
| `中` | 一般故障，正常排队处理 |
| `低` | 轻微问题，可延后处理 |

#### B.2 工单状态（WorkOrderStatus）

| 枚举值 | 说明 | 可流转至 |
|--------|------|---------|
| `待处理` | 新创建工单，等待分配 | 处理中、已关闭 |
| `处理中` | 已分配处理人，正在维修 | 已完成、已关闭 |
| `已完成` | 维修完成，已验收 | — |
| `已关闭` | 工单关闭（无需处理或重复报修） | — |

#### B.3 设备状态

| 状态值 | 说明 |
|--------|------|
| `正常` | 设备运行正常 |
| `故障` | 设备发生故障，需维修 |
| `维护保养` | 设备处于保养/维护状态 |

#### B.4 告警状态

| 状态常量 | 值 | 说明 |
|---------|-----|------|
| `STATUS_PENDING` | 0 | 待处理（活跃告警） |
| `STATUS_RESOLVED` | 1 | 已解决 |

### 附录 C：MCP 技术规范参考

本系统基于 Spring AI MCP 架构实现，相关技术规范：

- **MCP Protocol**：Model Context Protocol 2025-03-26
- **Spring AI 版本**：1.1.3+ / 2.0.0-M3+
- **通信协议**：JSON-RPC 2.0 over HTTP/SSE/STDIO
- **注解规范**：`@Tool` / `@ToolParam`（Spring AI 通用）或 `@McpTool` / `@McpToolParam`（MCP 专用）
- **事务管理**：Spring `@Transactional` 声明式事务


---


